package com.battlelancer.seriesguide.jobs.episodes;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateUtils;
import androidx.annotation.NonNull;
import com.battlelancer.seriesguide.R;
import com.battlelancer.seriesguide.appwidget.ListWidgetProvider;
import com.battlelancer.seriesguide.provider.SeriesGuideContract;
import com.battlelancer.seriesguide.ui.episodes.EpisodeTools;
import com.battlelancer.seriesguide.util.TextTools;

public class SeasonWatchedJob extends SeasonBaseJob {

    private final long currentTime;

    public SeasonWatchedJob(int showTvdbId, int seasonTvdbId, int season,
            int episodeFlags, long currentTime) {
        super(showTvdbId, seasonTvdbId, season, episodeFlags, JobAction.EPISODE_WATCHED_FLAG);
        this.currentTime = currentTime;
    }

    @Override
    public String getDatabaseSelection() {
        if (EpisodeTools.isUnwatched(getFlagValue())) {
            // set unwatched
            // include watched or skipped episodes
            return SeriesGuideContract.Episodes.SELECTION_WATCHED_OR_SKIPPED;
        } else {
            // set watched or skipped
            // do NOT mark watched episodes again to avoid trakt adding a new watch
            // only mark episodes that have been released until within the hour
            return SeriesGuideContract.Episodes.FIRSTAIREDMS + "<=" + (currentTime
                    + DateUtils.HOUR_IN_MILLIS)
                    + " AND " + SeriesGuideContract.Episodes.SELECTION_HAS_RELEASE_DATE
                    + " AND " + SeriesGuideContract.Episodes.SELECTION_UNWATCHED_OR_SKIPPED;
        }
    }

    @Override
    protected String getDatabaseColumnToUpdate() {
        return SeriesGuideContract.Episodes.WATCHED;
    }

    private int getLastWatchedEpisodeTvdbId(Context context) {
        if (EpisodeTools.isUnwatched(getFlagValue())) {
            // unwatched season
            // just reset
            return 0;
        } else {
            // watched or skipped season
            int lastWatchedId = -1;

            // get the last flagged episode of the season
            final Cursor seasonEpisodes = context.getContentResolver().query(
                    SeriesGuideContract.Episodes.buildEpisodesOfSeasonUri(
                            String.valueOf(seasonTvdbId)),
                    BaseEpisodesJob.PROJECTION_EPISODE,
                    SeriesGuideContract.Episodes.FIRSTAIREDMS + "<=" + (currentTime
                            + DateUtils.HOUR_IN_MILLIS), null,
                    SeriesGuideContract.Episodes.NUMBER + " DESC"
            );
            if (seasonEpisodes != null) {
                if (seasonEpisodes.moveToFirst()) {
                    lastWatchedId = seasonEpisodes.getInt(0);
                }

                seasonEpisodes.close();
            }

            return lastWatchedId;
        }
    }

    @Override
    public boolean applyLocalChanges(Context context, boolean requiresNetworkJob) {
        if (!super.applyLocalChanges(context, requiresNetworkJob)) {
            return false;
        }

        // set a new last watched episode
        // set last watched time to now if marking as watched or skipped
        updateLastWatched(context, getLastWatchedEpisodeTvdbId(context),
                !EpisodeTools.isUnwatched(getFlagValue()));

        ListWidgetProvider.notifyDataChanged(context);

        return true;
    }

    @NonNull
    @Override
    public String getConfirmationText(Context context) {
        int actionResId;
        int flagValue = getFlagValue();
        if (EpisodeTools.isSkipped(flagValue)) {
            actionResId = R.string.action_skip;
        } else if (EpisodeTools.isWatched(flagValue)) {
            actionResId = R.string.action_watched;
        } else {
            actionResId = R.string.action_unwatched;
        }
        // format like '6x · Set watched'
        String number = TextTools.getEpisodeNumber(context, season, -1);
        return TextTools.dotSeparate(number, context.getString(actionResId));
    }
}
