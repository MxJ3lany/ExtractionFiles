package com.battlelancer.seriesguide.ui.episodes;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.NavUtils;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.viewpager.widget.ViewPager;
import com.battlelancer.seriesguide.R;
import com.battlelancer.seriesguide.dataliberation.model.Season;
import com.battlelancer.seriesguide.thetvdbapi.TvdbImageTools;
import com.battlelancer.seriesguide.ui.BaseNavDrawerActivity;
import com.battlelancer.seriesguide.ui.OverviewActivity;
import com.battlelancer.seriesguide.ui.SeriesGuidePreferences;
import com.battlelancer.seriesguide.ui.ShowsActivity;
import com.battlelancer.seriesguide.util.SeasonTools;
import com.battlelancer.seriesguide.util.ThemeUtils;
import com.battlelancer.seriesguide.util.Utils;
import com.uwetrottmann.seriesguide.widgets.SlidingTabLayout;
import java.util.List;

/**
 * Hosts a {@link ViewPager} displaying an episode per fragment of a complete season. Used on
 * smaller screens which do not allow for multi-pane layouts or if coming from a search result
 * selection.
 */
public class EpisodeDetailsActivity extends BaseNavDrawerActivity {

    private static final int LOADER_EPISODE_ID = 100;
    private static final int LOADER_SEASON_ID = 101;
    private static final String STATE_EPISODE_TVDB_ID = "episodeTvdbId";

    private ImageView imageViewBackground;
    private Spinner toolbarSpinner;
    private SlidingTabLayout tabs;
    private ViewPager viewPager;

    private SeasonSpinnerAdapter spinnerAdapter;
    @Nullable private EpisodePagerAdapter episodePagerAdapter;
    private int episodeTvdbId;
    private int seasonTvdbId;
    private int showTvdbId;
    private String showTitle;
    private boolean updateShow;

    /**
     * Data which has to be passed when creating this activity. All Bundle extras are integer.
     */
    public interface InitBundle {

        String EPISODE_TVDBID = "episode_tvdbid";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int initialEpisodeTvdbId = getIntent().getIntExtra(InitBundle.EPISODE_TVDBID, -1);
        if (savedInstanceState == null) {
            episodeTvdbId = initialEpisodeTvdbId;
            updateShow = true;
        } else {
            episodeTvdbId = savedInstanceState.getInt(STATE_EPISODE_TVDB_ID, initialEpisodeTvdbId);
        }

        if (episodeTvdbId == -1) {
            finish(); // nothing to display, give up.
            return;
        }

        // support transparent status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            findViewById(android.R.id.content).setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }

        setContentView(R.layout.activity_episode);
        setupActionBar();
        setupNavDrawer();

        setupViews();

        // start loading data
        LoaderManager.getInstance(this)
                .restartLoader(LOADER_EPISODE_ID, null, basicInfoLoaderCallbacks);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (episodePagerAdapter != null) {
            Integer displayedEpisodeTvdbId = episodePagerAdapter.getItemEpisodeTvdbId(
                    viewPager.getCurrentItem());
            if (displayedEpisodeTvdbId != null) {
                episodeTvdbId = displayedEpisodeTvdbId;
            }
        }
        outState.putInt(STATE_EPISODE_TVDB_ID, episodeTvdbId);
    }

    @Override
    protected void setCustomTheme() {
        // use a special immersive theme
        ThemeUtils.setImmersiveTheme(this);
    }

    @Override
    protected void setupActionBar() {
        super.setupActionBar();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private void setupViews() {
        toolbarSpinner = findViewById(R.id.sgToolbarSpinner);
        // prevent spinner from restoring selection, we do that ourselves
        toolbarSpinner.setSaveEnabled(false);

        imageViewBackground = findViewById(R.id.imageViewEpisodeDetailsBackground);
        tabs = findViewById(R.id.tabsEpisodeDetails);
        viewPager = findViewById(R.id.pagerEpisodeDetails);

        // setup tabs
        tabs.setCustomTabView(R.layout.tabstrip_item_transparent, R.id.textViewTabStripItem);
        //noinspection ResourceType
        tabs.setSelectedIndicatorColors(ContextCompat.getColor(this,
                SeriesGuidePreferences.THEME == R.style.Theme_SeriesGuide_DarkBlue ? R.color.white
                        : Utils.resolveAttributeToResourceId(getTheme(), R.attr.colorPrimary)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            if (seasonTvdbId == 0) {
                return true; // season tvdb not determined yet, have no idea where to go up to.
            }
            Intent upIntent = OverviewActivity.intentSeasons(this, showTvdbId);
            if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                // This activity is not part of the application's task, so
                // create a new task with a synthesized back stack.
                TaskStackBuilder
                        .create(this)
                        .addNextIntent(new Intent(this, ShowsActivity.class))
                        .addNextIntent(upIntent)
                        .startActivities();
                finish();
            } else {
                /*
                 * This activity is part of the application's task, so simply
                 * navigate up to the hierarchical parent activity.
                 * NavUtils.navigateUpTo() does not seem to work here.
                 */
                upIntent.addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(upIntent);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private LoaderManager.LoaderCallbacks<SeasonsLoader.Result> basicInfoLoaderCallbacks
            = new LoaderManager.LoaderCallbacks<SeasonsLoader.Result>() {
        @Override
        public Loader<SeasonsLoader.Result> onCreateLoader(int id, Bundle args) {
            return new SeasonsLoader(EpisodeDetailsActivity.this, episodeTvdbId);
        }

        @Override
        public void onLoadFinished(Loader<SeasonsLoader.Result> loader, SeasonsLoader.Result data) {
            populateBasicInfo(data);
        }

        @Override
        public void onLoaderReset(Loader<SeasonsLoader.Result> loader) {
            // do nothing, keep existing data
        }
    };

    private void populateBasicInfo(@Nullable SeasonsLoader.Result basicInfo) {
        if (basicInfo == null || basicInfo.seasonsOfShow.isEmpty()) {
            // do not have minimal data, give up.
            finish();
            return;
        }

        showTvdbId = basicInfo.showTvdbId;
        showTitle = basicInfo.showTitle;

        // set show poster as background
        TvdbImageTools.loadShowPosterAlpha(this, imageViewBackground, basicInfo.showPoster);

        // set up season switcher
        spinnerAdapter = new SeasonSpinnerAdapter(this, basicInfo.seasonsOfShow);
        toolbarSpinner.setAdapter(spinnerAdapter);
        //  display the season of the given episode
        Season initialSeason = basicInfo.seasonsOfShow.get(basicInfo.seasonIndexOfEpisode);
        toolbarSpinner.setSelection(basicInfo.seasonIndexOfEpisode, false);
        loadSeason(initialSeason);

        // start listening to spinner selection changes
        toolbarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Season season = spinnerAdapter.getItem(position);
                if (season.tvdbId == seasonTvdbId) {
                    // guard against firing after layout completes
                    // still happening on custom ROMs despite workaround described at
                    // http://stackoverflow.com/a/17336944/1000543
                    return;
                }
                loadSeason(season);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // ignored
            }
        });

        if (updateShow) {
            // when shown initially, schedule a show update
            if (showTvdbId != 0) {
                updateShowDelayed(showTvdbId);
            }
        }
    }

    private void loadSeason(Season season) {
        seasonTvdbId = season.tvdbId;

        // update the activity title for accessibility
        setTitle(getString(R.string.episodes) + " " + showTitle + " "
                + SeasonTools.getSeasonString(this, season.season));

        LoaderManager.getInstance(this)
                .restartLoader(LOADER_SEASON_ID, null, seasonLoaderCallbacks);
    }

    private LoaderManager.LoaderCallbacks<SeasonEpisodesLoader.Result> seasonLoaderCallbacks
            = new LoaderManager.LoaderCallbacks<SeasonEpisodesLoader.Result>() {
        @Override
        public Loader<SeasonEpisodesLoader.Result> onCreateLoader(int id, Bundle args) {
            return new SeasonEpisodesLoader(EpisodeDetailsActivity.this, seasonTvdbId,
                    episodeTvdbId);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<SeasonEpisodesLoader.Result> loader,
                SeasonEpisodesLoader.Result data) {
            populateSeason(data);
        }

        @Override
        public void onLoaderReset(@NonNull Loader<SeasonEpisodesLoader.Result> loader) {
            // do nothing, keep existing data
        }
    };

    private void populateSeason(SeasonEpisodesLoader.Result data) {
        // setup adapter
        episodePagerAdapter = new EpisodePagerAdapter(this, getSupportFragmentManager(),
                data.episodes, false);
        viewPager.setAdapter(episodePagerAdapter);
        tabs.setViewPager(viewPager);

        viewPager.setCurrentItem(data.requestedEpisodeIndex, false);
    }

    @Override
    protected View getSnackbarParentView() {
        return findViewById(R.id.coordinatorLayoutEpisode);
    }

    public static class SeasonSpinnerAdapter extends BaseAdapter implements SpinnerAdapter {

        private final Context context;
        private final LayoutInflater inflater;
        private final List<Season> seasons;

        public SeasonSpinnerAdapter(Context context, List<Season> seasons) {
            this.context = context;
            inflater = LayoutInflater.from(context);
            this.seasons = seasons;
        }

        @Override
        public int getCount() {
            return seasons.size();
        }

        @Override
        public Season getItem(int position) {
            return seasons.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return createViewFromResource(position, convertView, parent,
                    R.layout.item_spinner_title);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return createViewFromResource(position, convertView, parent,
                    android.R.layout.simple_spinner_dropdown_item);
        }

        @NonNull
        private View createViewFromResource(int position, View convertView, ViewGroup parent,
                int resource) {
            TextView view;
            if (convertView == null) {
                view = (TextView) inflater.inflate(resource, parent, false);
            } else {
                view = (TextView) convertView;
            }

            Season item = getItem(position);
            view.setText(SeasonTools.getSeasonString(context, item.season));

            return view;
        }
    }
}
