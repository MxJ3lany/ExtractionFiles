package com.battlelancer.seriesguide.ui.episodes;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.Unbinder;
import com.battlelancer.seriesguide.R;
import com.battlelancer.seriesguide.api.Action;
import com.battlelancer.seriesguide.backend.settings.HexagonSettings;
import com.battlelancer.seriesguide.extensions.ActionsHelper;
import com.battlelancer.seriesguide.extensions.EpisodeActionsContract;
import com.battlelancer.seriesguide.extensions.EpisodeActionsLoader;
import com.battlelancer.seriesguide.extensions.ExtensionManager;
import com.battlelancer.seriesguide.provider.SeriesGuideContract.Episodes;
import com.battlelancer.seriesguide.provider.SeriesGuideContract.ListItemTypes;
import com.battlelancer.seriesguide.provider.SeriesGuideContract.Seasons;
import com.battlelancer.seriesguide.provider.SeriesGuideContract.Shows;
import com.battlelancer.seriesguide.provider.SeriesGuideDatabase.Tables;
import com.battlelancer.seriesguide.settings.DisplaySettings;
import com.battlelancer.seriesguide.streaming.StreamingSearch;
import com.battlelancer.seriesguide.streaming.StreamingSearchConfigureDialog;
import com.battlelancer.seriesguide.thetvdbapi.TvdbImageTools;
import com.battlelancer.seriesguide.thetvdbapi.TvdbLinks;
import com.battlelancer.seriesguide.traktapi.CheckInDialogFragment;
import com.battlelancer.seriesguide.traktapi.RateDialogFragment;
import com.battlelancer.seriesguide.traktapi.TraktCredentials;
import com.battlelancer.seriesguide.traktapi.TraktRatingsTask;
import com.battlelancer.seriesguide.traktapi.TraktTools;
import com.battlelancer.seriesguide.ui.BaseNavDrawerActivity;
import com.battlelancer.seriesguide.ui.FullscreenImageActivity;
import com.battlelancer.seriesguide.ui.SeriesGuidePreferences;
import com.battlelancer.seriesguide.ui.comments.TraktCommentsActivity;
import com.battlelancer.seriesguide.ui.lists.ManageListsDialogFragment;
import com.battlelancer.seriesguide.util.ClipboardTools;
import com.battlelancer.seriesguide.util.LanguageTools;
import com.battlelancer.seriesguide.util.ServiceUtils;
import com.battlelancer.seriesguide.util.ShareUtils;
import com.battlelancer.seriesguide.util.TextTools;
import com.battlelancer.seriesguide.util.TimeTools;
import com.battlelancer.seriesguide.util.Utils;
import com.battlelancer.seriesguide.util.ViewTools;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.uwetrottmann.androidutils.CheatSheet;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import timber.log.Timber;

/**
 * Displays details about a single episode like summary, ratings and episode image if available.
 */
public class EpisodeDetailsFragment extends Fragment implements EpisodeActionsContract {

    private static final String ARG_EPISODE_TVDBID = "episode_tvdbid";
    private static final String ARG_IS_IN_MULTIPANE_LAYOUT = "multipane";
    private static final String KEY_EPISODE_TVDB_ID = "episodeTvdbId";

    private Handler handler = new Handler();
    private TraktRatingsTask ratingsTask;

    private boolean isInMultipane;
    private int episodeTvdbId;
    private int showTvdbId;
    @Nullable private String showTvdbSlug;
    private int seasonTvdbId;
    private int seasonNumber;
    private int episodeNumber;
    private int episodeFlag;
    private boolean collected;
    private String episodeTitle;
    private String showTitle;
    private int showRunTime;
    private long episodeReleaseTime;

    @BindView(R.id.containerEpisode) View containerEpisode;
    @BindView(R.id.containerRatings) View containerRatings;
    @BindView(R.id.containerEpisodeActions) LinearLayout containerActions;

    @BindView(R.id.containerEpisodeImage) View containerImage;
    @BindView(R.id.imageViewEpisode) ImageView imageViewEpisode;

    @BindView(R.id.textViewEpisodeTitle) TextView textViewTitle;
    @BindView(R.id.textViewEpisodeDescription) TextView textViewDescription;
    @BindView(R.id.textViewEpisodeReleaseTime) TextView textViewReleaseTime;
    @BindView(R.id.textViewEpisodeReleaseDate) TextView textViewReleaseDate;
    @BindView(R.id.labelEpisodeGuestStars) View textViewGuestStarsLabel;
    @BindView(R.id.textViewEpisodeGuestStars) TextView textViewGuestStars;
    @BindView(R.id.textViewEpisodeDirectors) TextView textViewDirectors;
    @BindView(R.id.textViewEpisodeWriters) TextView textViewWriters;
    @BindView(R.id.labelEpisodeDvd) View textViewDvdLabel;
    @BindView(R.id.textViewEpisodeDvd) TextView textViewDvd;
    @BindView(R.id.textViewRatingsValue) TextView textViewRating;
    @BindView(R.id.textViewRatingsRange) TextView textViewRatingRange;
    @BindView(R.id.textViewRatingsVotes) TextView textViewRatingVotes;
    @BindView(R.id.textViewRatingsUser) TextView textViewRatingUser;

    @BindView(R.id.dividerEpisodeButtons) View dividerEpisodeButtons;
    @BindView(R.id.buttonEpisodeCheckin) Button buttonCheckin;
    @BindView(R.id.buttonEpisodeStreamingSearch) Button buttonStreamingSearch;
    @BindView(R.id.buttonEpisodeWatched) Button buttonWatch;
    @BindView(R.id.buttonEpisodeCollected) Button buttonCollect;
    @BindView(R.id.buttonEpisodeSkip) Button buttonSkip;

    @BindView(R.id.buttonEpisodeImdb) Button imdbButton;
    @BindView(R.id.buttonEpisodeTvdb) Button tvdbButton;
    @BindView(R.id.buttonEpisodeTrakt) Button traktButton;
    @BindView(R.id.buttonEpisodeComments) Button commentsButton;

    private Unbinder unbinder;

    public static EpisodeDetailsFragment newInstance(int episodeId, boolean isInMultiPaneLayout) {
        EpisodeDetailsFragment f = new EpisodeDetailsFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt(ARG_EPISODE_TVDBID, episodeId);
        args.putBoolean(ARG_IS_IN_MULTIPANE_LAYOUT, isInMultiPaneLayout);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            isInMultipane = args.getBoolean(ARG_IS_IN_MULTIPANE_LAYOUT);
            episodeTvdbId = args.getInt(ARG_EPISODE_TVDBID);
        } else {
            throw new IllegalArgumentException("Missing arguments");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_episode, container, false);
        unbinder = ButterKnife.bind(this, v);

        containerEpisode.setVisibility(View.GONE);

        textViewRatingRange.setText(getString(R.string.format_rating_range, 10));

        // episode buttons
        Resources.Theme theme = requireActivity().getTheme();
        ViewTools.setVectorIconTop(theme, buttonWatch, R.drawable.ic_watch_black_24dp);
        ViewTools.setVectorIconTop(theme, buttonCollect, R.drawable.ic_collect_black_24dp);
        ViewTools.setVectorIconTop(theme, buttonSkip, R.drawable.ic_skip_black_24dp);
        ViewTools.setVectorIconLeft(theme, buttonCheckin, R.drawable.ic_checkin_black_24dp);
        ViewTools.setVectorIconLeft(theme, buttonStreamingSearch,
                R.drawable.ic_play_arrow_black_24dp);

        // comments button
        ViewTools.setVectorIconLeft(theme, commentsButton, R.drawable.ic_forum_black_24dp);

        // other bottom buttons
        ViewTools.setVectorIconLeft(theme, imdbButton, R.drawable.ic_link_black_24dp);
        ViewTools.setVectorIconLeft(theme, tvdbButton, R.drawable.ic_link_black_24dp);
        ViewTools.setVectorIconLeft(theme, traktButton, R.drawable.ic_link_black_24dp);

        // set up long-press to copy text to clipboard (d-pad friendly vs text selection)
        ClipboardTools.copyTextToClipboardOnLongClick(textViewTitle);
        ClipboardTools.copyTextToClipboardOnLongClick(textViewReleaseTime);
        ClipboardTools.copyTextToClipboardOnLongClick(textViewDescription);
        ClipboardTools.copyTextToClipboardOnLongClick(textViewGuestStars);
        ClipboardTools.copyTextToClipboardOnLongClick(textViewDirectors);
        ClipboardTools.copyTextToClipboardOnLongClick(textViewWriters);
        ClipboardTools.copyTextToClipboardOnLongClick(textViewDvd);
        ClipboardTools.copyTextToClipboardOnLongClick(textViewReleaseDate);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LoaderManager.getInstance(this)
                .initLoader(EpisodesActivity.EPISODE_LOADER_ID, null, episodeLoaderCallbacks);

        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        BaseNavDrawerActivity.ServiceActiveEvent event = EventBus.getDefault()
                .getStickyEvent(BaseNavDrawerActivity.ServiceActiveEvent.class);
        setEpisodeButtonsEnabled(event == null);

        EventBus.getDefault().register(this);
        loadEpisodeActionsDelayed();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (handler != null) {
            handler.removeCallbacks(actionsRunnable);
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Always cancel the request here, this is safe to call even if the image has been loaded.
        // This ensures that the anonymous callback we have does not prevent the fragment from
        // being garbage collected. It also prevents our callback from getting invoked even after the
        // fragment is destroyed.
        Picasso.get().cancelRequest(imageViewEpisode);
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ratingsTask != null) {
            ratingsTask.cancel(true);
            ratingsTask = null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        boolean isLightTheme = SeriesGuidePreferences.THEME == R.style.Theme_SeriesGuide_Light;
        // multi-pane layout has non-transparent action bar, adjust icon color
        inflater.inflate(isLightTheme && !isInMultipane
                ? R.menu.episodedetails_menu_light : R.menu.episodedetails_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_share) {
            shareEpisode();
            return true;
        } else if (itemId == R.id.menu_manage_lists) {
            ManageListsDialogFragment
                    .show(getFragmentManager(), episodeTvdbId, ListItemTypes.EPISODE);
            return true;
        } else if (itemId == R.id.menu_action_episode_calendar) {
            ShareUtils.suggestCalendarEvent(requireActivity(), showTitle,
                    TextTools.getNextEpisodeString(requireActivity(), seasonNumber, episodeNumber,
                            episodeTitle), episodeReleaseTime, showRunTime);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * If episode was watched, flags as unwatched. Otherwise, flags as watched.
     */
    private void onToggleWatched() {
        boolean watched = EpisodeTools.isWatched(episodeFlag);
        changeEpisodeFlag(watched ? EpisodeFlags.UNWATCHED : EpisodeFlags.WATCHED);
    }

    /**
     * If episode was skipped, flags as unwatched. Otherwise, flags as skipped.
     */
    private void onToggleSkipped() {
        boolean skipped = EpisodeTools.isSkipped(episodeFlag);
        changeEpisodeFlag(skipped ? EpisodeFlags.UNWATCHED : EpisodeFlags.SKIPPED);
    }

    private void changeEpisodeFlag(int episodeFlag) {
        this.episodeFlag = episodeFlag;
        EpisodeTools.episodeWatched(requireContext(), showTvdbId, episodeTvdbId,
                seasonNumber, episodeNumber, episodeFlag);
    }

    private void onToggleCollected() {
        collected = !collected;
        EpisodeTools.episodeCollected(requireContext(), showTvdbId, episodeTvdbId,
                seasonNumber, episodeNumber, collected);
    }

    @OnClick(R.id.buttonEpisodeStreamingSearch)
    void onButtonStreamingSearchClick() {
        if (showTitle == null) {
            return;
        }
        if (StreamingSearch.isNotConfigured(requireContext())) {
            showStreamingSearchConfigDialog();
        } else {
            StreamingSearch.searchForShow(requireContext(), showTitle);
        }
    }

    @OnLongClick(R.id.buttonEpisodeStreamingSearch)
    boolean onButtonStreamingSearchLongClick() {
        showStreamingSearchConfigDialog();
        return true;
    }

    private void showStreamingSearchConfigDialog() {
        StreamingSearchConfigureDialog.show(requireFragmentManager());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStreamingSearchConfigured(
            StreamingSearchConfigureDialog.StreamingSearchConfiguredEvent event) {
        if (event.getTurnedOff()) {
            buttonStreamingSearch.setVisibility(View.GONE);
        } else {
            onButtonStreamingSearchClick();
        }
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ExtensionManager.EpisodeActionReceivedEvent event) {
        if (episodeTvdbId == event.episodeTvdbId) {
            loadEpisodeActionsDelayed();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventEpisodeTask(BaseNavDrawerActivity.ServiceActiveEvent event) {
        setEpisodeButtonsEnabled(false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventEpisodeTask(BaseNavDrawerActivity.ServiceCompletedEvent event) {
        setEpisodeButtonsEnabled(true);
    }

    private void setEpisodeButtonsEnabled(boolean enabled) {
        buttonWatch.setEnabled(enabled);
        buttonCollect.setEnabled(enabled);
        buttonSkip.setEnabled(enabled);
        buttonCheckin.setEnabled(enabled);
        buttonStreamingSearch.setEnabled(enabled);
    }

    private LoaderManager.LoaderCallbacks<Cursor> episodeLoaderCallbacks
            = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(requireContext(), Episodes.buildEpisodeWithShowUri(String
                    .valueOf(episodeTvdbId)), DetailsQuery.PROJECTION, null, null, null);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
            if (!isAdded()) {
                return;
            }
            populateEpisodeData(data);
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Cursor> loader) {
            // do nothing (we are never holding onto the cursor
        }
    };

    private void populateEpisodeData(Cursor cursor) {
        if (cursor == null || !cursor.moveToFirst()) {
            // no data to display
            if (containerEpisode != null) {
                containerEpisode.setVisibility(View.GONE);
            }
            return;
        }

        showTvdbId = cursor.getInt(DetailsQuery.SHOW_ID);
        showTvdbSlug = cursor.getString(DetailsQuery.SHOW_SLUG);
        seasonNumber = cursor.getInt(DetailsQuery.SEASON);
        episodeNumber = cursor.getInt(DetailsQuery.NUMBER);
        showRunTime = cursor.getInt(DetailsQuery.SHOW_RUNTIME);
        episodeReleaseTime = cursor.getLong(DetailsQuery.FIRST_RELEASE_MS);

        // title and description
        episodeFlag = cursor.getInt(DetailsQuery.WATCHED);
        episodeTitle = TextTools.getEpisodeTitle(requireContext(),
                cursor.getString(DetailsQuery.TITLE), episodeNumber);
        boolean hideDetails = EpisodeTools.isUnwatched(episodeFlag)
                && DisplaySettings.preventSpoilers(requireContext());
        textViewTitle.setText(
                TextTools.getEpisodeTitle(requireContext(), hideDetails ? null : episodeTitle,
                        episodeNumber));
        String overview = cursor.getString(DetailsQuery.OVERVIEW);
        if (TextUtils.isEmpty(overview)) {
            // no description available, show no translation available message
            String languageCode = cursor.getString(DetailsQuery.SHOW_LANGUAGE);
            overview = getString(R.string.no_translation,
                    LanguageTools.getShowLanguageStringFor(getContext(), languageCode),
                    getString(R.string.tvdb));
        } else if (hideDetails) {
            overview = getString(R.string.no_spoilers);
        }
        long lastEditSeconds = cursor.getLong(DetailsQuery.LAST_EDITED);
        textViewDescription.setText(
                TextTools.textWithTvdbSource(textViewDescription.getContext(), overview,
                        lastEditSeconds));

        // show title
        showTitle = cursor.getString(DetailsQuery.SHOW_TITLE);

        // release date, also build release time and day
        boolean isReleased;
        String timeText;
        if (episodeReleaseTime != -1) {
            Date actualRelease = TimeTools.applyUserOffset(requireContext(), episodeReleaseTime);
            isReleased = TimeTools.isReleased(actualRelease);
            textViewReleaseDate.setText(
                    TimeTools.formatToLocalDateAndDay(requireContext(), actualRelease));

            String dateTime;
            if (DisplaySettings.isDisplayExactDate(requireContext())) {
                // "31. October 2010"
                dateTime = TimeTools.formatToLocalDate(requireContext(), actualRelease);
            } else {
                // "in 15 mins"
                dateTime = TimeTools.formatToLocalRelativeTime(requireContext(), actualRelease);
            }
            // append day: "in 15 mins (Fri)"
            timeText = getString(R.string.format_date_and_day, dateTime,
                    TimeTools.formatToLocalDay(actualRelease)).toUpperCase(Locale.getDefault());
        } else {
            textViewReleaseDate.setText(R.string.unknown);
            timeText = getString(R.string.episode_firstaired_unknown);
            isReleased = false;
        }
        // absolute number (e.g. relevant for Anime): "ABSOLUTE 142"
        int absoluteNumber = cursor.getInt(DetailsQuery.NUMBER_ABSOLUTE);
        String absoluteNumberText = null;
        if (absoluteNumber > 0) {
            absoluteNumberText = NumberFormat.getIntegerInstance().format(absoluteNumber);
        }
        textViewReleaseTime.setText(TextTools.dotSeparate(timeText, absoluteNumberText));

        // dim text color for title if not released
        TextViewCompat.setTextAppearance(textViewTitle, isReleased
                ? R.style.TextAppearance_Title : R.style.TextAppearance_Title_Dim);
        if (!isReleased) {
            TextViewCompat.setTextAppearance(textViewReleaseTime,
                    R.style.TextAppearance_Caption_Dim);
        }

        // guest stars
        ViewTools.setLabelValueOrHide(textViewGuestStarsLabel, textViewGuestStars,
                TextTools.splitAndKitTVDBStrings(cursor.getString(DetailsQuery.GUESTSTARS))
        );
        // DVD episode number
        ViewTools.setLabelValueOrHide(textViewDvdLabel, textViewDvd,
                cursor.getDouble(DetailsQuery.NUMBER_DVD));
        // directors
        ViewTools.setValueOrPlaceholder(textViewDirectors, TextTools.splitAndKitTVDBStrings(cursor
                .getString(DetailsQuery.DIRECTORS)));
        // writers
        ViewTools.setValueOrPlaceholder(textViewWriters, TextTools.splitAndKitTVDBStrings(cursor
                .getString(DetailsQuery.WRITERS)));

        // ratings
        containerRatings.setOnClickListener(v -> rateEpisode());
        CheatSheet.setup(containerRatings, R.string.action_rate);

        // trakt rating
        textViewRating.setText(
                TraktTools.buildRatingString(cursor.getDouble(DetailsQuery.RATING_GLOBAL)));
        textViewRatingVotes.setText(TraktTools.buildRatingVotesString(requireContext(),
                cursor.getInt(DetailsQuery.RATING_VOTES)));

        // user rating
        textViewRatingUser.setText(TraktTools.buildUserRatingString(requireContext(),
                cursor.getInt(DetailsQuery.RATING_USER)));

        // episode image
        final String imagePath = cursor.getString(DetailsQuery.IMAGE);
        containerImage.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), FullscreenImageActivity.class);
            intent.putExtra(FullscreenImageActivity.EXTRA_IMAGE,
                    TvdbImageTools.fullSizeUrl(imagePath));
            Utils.startActivityWithAnimation(requireActivity(), intent, v);
        });
        loadImage(imagePath, hideDetails);

        // check in button
        buttonCheckin.setOnClickListener(
                v -> CheckInDialogFragment.show(getContext(), getFragmentManager(), episodeTvdbId));
        CheatSheet.setup(buttonCheckin);

        // hide check-in if not connected to trakt or hexagon is enabled
        boolean isConnectedToTrakt = TraktCredentials.get(requireContext()).hasCredentials();
        boolean displayCheckIn = isConnectedToTrakt && !HexagonSettings.isEnabled(requireContext());
        buttonCheckin.setVisibility(displayCheckIn ? View.VISIBLE : View.GONE);
        buttonStreamingSearch
                .setNextFocusUpId(displayCheckIn ? R.id.buttonCheckIn : R.id.buttonEpisodeWatched);
        // hide streaming search if turned off
        boolean displayStreamingSearch = !StreamingSearch.isTurnedOff(requireContext());
        buttonStreamingSearch.setVisibility(displayStreamingSearch ? View.VISIBLE : View.GONE);
        dividerEpisodeButtons.setVisibility(displayCheckIn || displayStreamingSearch
                ? View.VISIBLE : View.GONE);

        // watched button
        Resources.Theme theme = requireActivity().getTheme();
        boolean isWatched = EpisodeTools.isWatched(episodeFlag);
        if (isWatched) {
            ViewTools.setVectorDrawableTop(theme, buttonWatch, R.drawable.ic_watched_24dp);
        } else {
            ViewTools.setVectorIconTop(theme, buttonWatch, R.drawable.ic_watch_black_24dp);
        }
        buttonWatch.setOnClickListener(v -> onToggleWatched());
        buttonWatch.setText(isWatched ? R.string.action_unwatched : R.string.action_watched);
        CheatSheet.setup(buttonWatch, isWatched ? R.string.action_unwatched
                : R.string.action_watched);

        // collected button
        collected = cursor.getInt(DetailsQuery.COLLECTED) == 1;
        if (collected) {
            ViewTools.setVectorDrawableTop(theme, buttonCollect, R.drawable.ic_collected_24dp);
        } else {
            ViewTools.setVectorIconTop(theme, buttonCollect, R.drawable.ic_collect_black_24dp);
        }
        buttonCollect.setOnClickListener(v -> onToggleCollected());
        buttonCollect.setText(collected
                ? R.string.action_collection_remove : R.string.action_collection_add);
        CheatSheet.setup(buttonCollect, collected
                ? R.string.action_collection_remove : R.string.action_collection_add);

        // skip button
        if (isWatched) {
            // if watched do not allow skipping
            buttonSkip.setVisibility(View.INVISIBLE);
        } else {
            buttonSkip.setVisibility(View.VISIBLE);

            boolean isSkipped = EpisodeTools.isSkipped(episodeFlag);
            if (isSkipped) {
                ViewTools.setVectorDrawableTop(theme, buttonSkip, R.drawable.ic_skipped_24dp);
            } else {
                ViewTools.setVectorIconTop(theme, buttonSkip, R.drawable.ic_skip_black_24dp);
            }
            buttonSkip.setOnClickListener(v -> onToggleSkipped());
            buttonSkip.setText(isSkipped ? R.string.action_dont_skip : R.string.action_skip);
            CheatSheet.setup(buttonSkip,
                    isSkipped ? R.string.action_dont_skip : R.string.action_skip);
        }

        // service buttons
        // trakt
        String traktLink = TraktTools.buildEpisodeUrl(episodeTvdbId);
        ViewTools.openUriOnClick(traktButton, traktLink);
        ClipboardTools.copyTextToClipboardOnLongClick(traktButton, traktLink);

        // IMDb
        String imdbId = cursor.getString(DetailsQuery.IMDBID);
        if (TextUtils.isEmpty(imdbId)) {
            // fall back to show IMDb id
            imdbId = cursor.getString(DetailsQuery.SHOW_IMDBID);
        }
        ServiceUtils.setUpImdbButton(imdbId, imdbButton);

        // TVDb
        seasonTvdbId = cursor.getInt(DetailsQuery.SEASON_ID);
        String tvdbLink = TvdbLinks.episode(showTvdbSlug, showTvdbId, seasonTvdbId, episodeTvdbId);
        ViewTools.openUriOnClick(tvdbButton, tvdbLink);
        ClipboardTools.copyTextToClipboardOnLongClick(tvdbButton, tvdbLink);
        // trakt comments
        commentsButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), TraktCommentsActivity.class);
            intent.putExtras(TraktCommentsActivity.createInitBundleEpisode(episodeTitle,
                    episodeTvdbId));
            Utils.startActivityWithAnimation(requireActivity(), intent, v);
        });

        containerEpisode.setVisibility(View.VISIBLE);

        loadDetails();
    }

    private void loadDetails() {
        // update trakt ratings
        if (ratingsTask == null || ratingsTask.getStatus() == AsyncTask.Status.FINISHED) {
            ratingsTask = new TraktRatingsTask(requireContext(), showTvdbId, episodeTvdbId,
                    seasonNumber, episodeNumber);
            ratingsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void rateEpisode() {
        RateDialogFragment.newInstanceEpisode(episodeTvdbId)
                .safeShow(getContext(), getFragmentManager());
    }

    private void shareEpisode() {
        if (episodeTitle == null || showTitle == null) {
            return;
        }
        ShareUtils.shareEpisode(requireActivity(), showTvdbSlug, showTvdbId, seasonTvdbId,
                episodeTvdbId, seasonNumber, episodeNumber, showTitle, episodeTitle);
    }

    private void loadImage(String imagePath, boolean hideDetails) {
        // immediately hide container if there is no image
        if (TextUtils.isEmpty(imagePath)) {
            containerImage.setVisibility(View.GONE);
            return;
        }

        if (hideDetails) {
            // show image placeholder
            imageViewEpisode.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageViewEpisode.setImageResource(R.drawable.ic_photo_gray_24dp);
        } else {
            // try loading image
            containerImage.setVisibility(View.VISIBLE);
            ServiceUtils.loadWithPicasso(requireContext(), TvdbImageTools.fullSizeUrl(imagePath))
                    .error(R.drawable.ic_photo_gray_24dp)
                    .into(imageViewEpisode,
                            new Callback() {
                                @Override
                                public void onSuccess() {
                                    imageViewEpisode.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                }

                                @Override
                                public void onError(Exception e) {
                                    imageViewEpisode.setScaleType(
                                            ImageView.ScaleType.CENTER_INSIDE);
                                }
                            }
                    );
        }
    }

    private LoaderManager.LoaderCallbacks<List<Action>> actionsLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<List<Action>>() {
                @Override
                public Loader<List<Action>> onCreateLoader(int id, Bundle args) {
                    int episodeTvdbId = args.getInt(KEY_EPISODE_TVDB_ID);
                    return new EpisodeActionsLoader(requireContext(), episodeTvdbId);
                }

                @Override
                public void onLoadFinished(@NonNull Loader<List<Action>> loader,
                        List<Action> data) {
                    if (!isAdded()) {
                        return;
                    }
                    if (data == null) {
                        Timber.e("onLoadFinished: did not receive valid actions for %s",
                                episodeTvdbId);
                    } else {
                        Timber.d("onLoadFinished: received %s actions for %s", data.size(),
                                episodeTvdbId);
                    }
                    ActionsHelper.populateActions(requireActivity().getLayoutInflater(),
                            requireActivity().getTheme(), containerActions, data);
                }

                @Override
                public void onLoaderReset(@NonNull Loader<List<Action>> loader) {
                    // do nothing, we are not holding onto the actions list
                }
            };

    public void loadEpisodeActions() {
        Bundle args = new Bundle();
        args.putInt(KEY_EPISODE_TVDB_ID, episodeTvdbId);
        LoaderManager.getInstance(this)
                .restartLoader(EpisodesActivity.ACTIONS_LOADER_ID, args, actionsLoaderCallbacks);
    }

    Runnable actionsRunnable = this::loadEpisodeActions;

    public void loadEpisodeActionsDelayed() {
        handler.removeCallbacks(actionsRunnable);
        handler.postDelayed(actionsRunnable,
                EpisodeActionsContract.ACTION_LOADER_DELAY_MILLIS);
    }

    interface DetailsQuery {

        String[] PROJECTION = new String[]{
                Tables.EPISODES + "." + Episodes._ID,
                Episodes.NUMBER,
                Episodes.ABSOLUTE_NUMBER,
                Episodes.DVDNUMBER,
                Seasons.REF_SEASON_ID,
                Episodes.SEASON,
                Episodes.IMDBID,
                Episodes.TITLE,
                Episodes.OVERVIEW,
                Episodes.FIRSTAIREDMS,
                Episodes.DIRECTORS,
                Episodes.GUESTSTARS,
                Episodes.WRITERS,
                Episodes.IMAGE,
                Tables.EPISODES + "." + Episodes.RATING_GLOBAL,
                Episodes.RATING_VOTES,
                Episodes.RATING_USER,
                Episodes.WATCHED,
                Episodes.COLLECTED,
                Episodes.LAST_EDITED,
                Shows.REF_SHOW_ID,
                Shows.IMDBID,
                Shows.TITLE,
                Shows.RUNTIME,
                Shows.LANGUAGE,
                Shows.SLUG
        };

        int _ID = 0;
        int NUMBER = 1;
        int NUMBER_ABSOLUTE = 2;
        int NUMBER_DVD = 3;
        int SEASON_ID = 4;
        int SEASON = 5;
        int IMDBID = 6;
        int TITLE = 7;
        int OVERVIEW = 8;
        int FIRST_RELEASE_MS = 9;
        int DIRECTORS = 10;
        int GUESTSTARS = 11;
        int WRITERS = 12;
        int IMAGE = 13;
        int RATING_GLOBAL = 14;
        int RATING_VOTES = 15;
        int RATING_USER = 16;
        int WATCHED = 17;
        int COLLECTED = 18;
        int LAST_EDITED = 19;
        int SHOW_ID = 20;
        int SHOW_IMDBID = 21;
        int SHOW_TITLE = 22;
        int SHOW_RUNTIME = 23;
        int SHOW_LANGUAGE = 24;
        int SHOW_SLUG = 25;
    }
}
