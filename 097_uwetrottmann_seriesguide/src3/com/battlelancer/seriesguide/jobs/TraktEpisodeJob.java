package com.battlelancer.seriesguide.jobs;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.battlelancer.seriesguide.SgApp;
import com.battlelancer.seriesguide.jobs.episodes.JobAction;
import com.battlelancer.seriesguide.modules.ServicesComponent;
import com.battlelancer.seriesguide.sync.NetworkJobProcessor;
import com.battlelancer.seriesguide.traktapi.SgTrakt;
import com.battlelancer.seriesguide.traktapi.TraktCredentials;
import com.battlelancer.seriesguide.ui.episodes.EpisodeFlags;
import com.battlelancer.seriesguide.ui.episodes.EpisodeTools;
import com.battlelancer.seriesguide.ui.shows.ShowTools;
import com.battlelancer.seriesguide.util.Errors;
import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.ShowIds;
import com.uwetrottmann.trakt5.entities.SyncEpisode;
import com.uwetrottmann.trakt5.entities.SyncErrors;
import com.uwetrottmann.trakt5.entities.SyncItems;
import com.uwetrottmann.trakt5.entities.SyncResponse;
import com.uwetrottmann.trakt5.entities.SyncSeason;
import com.uwetrottmann.trakt5.entities.SyncShow;
import com.uwetrottmann.trakt5.services.Sync;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.threeten.bp.Instant;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneOffset;
import retrofit2.Call;
import retrofit2.Response;

public class TraktEpisodeJob extends BaseNetworkEpisodeJob {

    private final long actionAtMs;

    public TraktEpisodeJob(JobAction action, SgJobInfo jobInfo, long actionAtMs) {
        super(action, jobInfo);
        this.actionAtMs = actionAtMs;
    }

    @NonNull
    @Override
    public NetworkJobProcessor.JobResult execute(Context context) {
        // Do not send if show has no trakt id (was not on trakt last time we checked).
        Integer showTraktId = ShowTools.getShowTraktId(context, jobInfo.showTvdbId());
        boolean canSendToTrakt = showTraktId != null;
        if (!canSendToTrakt) {
            return buildResult(context, NetworkJob.ERROR_TRAKT_NOT_FOUND);
        }

        int result = upload(context, showTraktId);
        return buildResult(context, result);
    }

    private int upload(Context context, @NonNull Integer showTraktId) {
        final int flagValue = jobInfo.flagValue();

        // skipped flag not supported by trakt
        if (EpisodeTools.isSkipped(flagValue)) {
            return NetworkJob.SUCCESS;
        }

        boolean isAddNotDelete = flagValue
                != EpisodeFlags.UNWATCHED; // 0 for not watched or not collected
        List<SyncSeason> seasons = getEpisodesForTrakt(isAddNotDelete);
        if (seasons.isEmpty()) {
            return NetworkJob.SUCCESS; // nothing to upload, done.
        }

        if (!TraktCredentials.get(context).hasCredentials()) {
            return NetworkJob.ERROR_TRAKT_AUTH;
        }

        // outer wrapper and show are always required
        SyncShow show = new SyncShow().id(ShowIds.trakt(showTraktId));
        SyncItems items = new SyncItems().shows(show);
        show.seasons(seasons);

        // determine network call
        String errorLabel;
        Call<SyncResponse> call;
        ServicesComponent component = SgApp.getServicesComponent(context);
        TraktV2 trakt = component.trakt();
        Sync traktSync = component.traktSync();
        switch (action) {
            case EPISODE_WATCHED_FLAG:
                if (isAddNotDelete) {
                    errorLabel = "set episodes watched";
                    call = traktSync.addItemsToWatchedHistory(items);
                } else {
                    errorLabel = "set episodes not watched";
                    call = traktSync.deleteItemsFromWatchedHistory(items);
                }
                break;
            case EPISODE_COLLECTION:
                if (isAddNotDelete) {
                    errorLabel = "add episodes to collection";
                    call = traktSync.addItemsToCollection(items);
                } else {
                    errorLabel = "remove episodes from collection";
                    call = traktSync.deleteItemsFromCollection(items);
                }
                break;
            default:
                throw new IllegalArgumentException("Action " + action + " not supported.");
        }

        // execute call
        try {
            Response<SyncResponse> response = call.execute();
            if (response.isSuccessful()) {
                // check if any items were not found
                if (!isSyncSuccessful(response.body())) {
                    return NetworkJob.ERROR_TRAKT_NOT_FOUND;
                }
            } else {
                if (SgTrakt.isUnauthorized(context, response)) {
                    return NetworkJob.ERROR_TRAKT_AUTH;
                }
                Errors.logAndReport(errorLabel, response,
                        SgTrakt.checkForTraktError(trakt, response));

                int code = response.code();
                if (code == 429 /* Rate Limit Exceeded */ || code >= 500) {
                    return NetworkJob.ERROR_TRAKT_SERVER;
                } else {
                    return NetworkJob.ERROR_TRAKT_CLIENT;
                }
            }
        } catch (Exception e) {
            Errors.logAndReport(errorLabel, e);
            return NetworkJob.ERROR_CONNECTION;
        }

        return NetworkJob.SUCCESS;
    }

    /**
     * Builds a list of {@link com.uwetrottmann.trakt5.entities.SyncSeason} objects to submit to
     * trakt.
     */
    @NonNull
    private List<SyncSeason> getEpisodesForTrakt(boolean isAddNotDelete) {
        // send time of action to avoid adding duplicate plays/collection events at trakt
        // if this job re-runs due to failure, but trakt already applied changes (it happens)
        // also if execution is delayed to due being offline this will ensure
        // the actual action time is stored at trakt
        Instant instant = Instant.ofEpochMilli(actionAtMs);
        OffsetDateTime actionAtDateTime = instant.atOffset(ZoneOffset.UTC);

        List<SyncSeason> seasons = new ArrayList<>();

        SyncSeason currentSeason = null;
        for (int i = 0; i < jobInfo.episodesLength(); i++) {
            EpisodeInfo episodeInfo = jobInfo.episodes(i);

            int seasonNumber = episodeInfo.season();

            // start new season?
            if (currentSeason == null || seasonNumber > currentSeason.number) {
                currentSeason = new SyncSeason().number(seasonNumber);
                currentSeason.episodes = new LinkedList<>();
                seasons.add(currentSeason);
            }

            // add episode
            SyncEpisode episode = new SyncEpisode().number(episodeInfo.number());
            if (isAddNotDelete) {
                // only send timestamp if adding, not if removing to save data
                if (action == JobAction.EPISODE_WATCHED_FLAG) {
                    episode.watchedAt(actionAtDateTime);
                } else {
                    episode.collectedAt(actionAtDateTime);
                }
            }
            currentSeason.episodes.add(episode);
        }

        return seasons;
    }

    /**
     * If the {@link SyncErrors} indicates any show, season or episode was not found returns {@code
     * false}.
     */
    private static boolean isSyncSuccessful(@Nullable SyncResponse response) {
        if (response == null || response.not_found == null) {
            return true;
        }

        if (response.not_found.shows != null && !response.not_found.shows.isEmpty()) {
            // show not found
            return false;
        }
        if (response.not_found.seasons != null && !response.not_found.seasons.isEmpty()) {
            // show exists, but seasons not found
            return false;
        }
        //noinspection RedundantIfStatement
        if (response.not_found.episodes != null && !response.not_found.episodes.isEmpty()) {
            // show and season exists, but episodes not found
            return false;
        }
        return true;
    }
}
