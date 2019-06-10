package com.battlelancer.seriesguide.jobs.movies;

import android.content.Context;
import androidx.annotation.Nullable;
import com.battlelancer.seriesguide.jobs.BaseJob;
import com.battlelancer.seriesguide.jobs.FlagJob;
import com.battlelancer.seriesguide.jobs.SgJobInfo;
import com.battlelancer.seriesguide.jobs.episodes.JobAction;
import com.battlelancer.seriesguide.ui.movies.MovieTools;
import com.google.flatbuffers.FlatBufferBuilder;
import org.greenrobot.eventbus.EventBus;

public abstract class MovieJob extends BaseJob implements FlagJob {

    private final int movieTmdbId;

    public MovieJob(JobAction action, int movieTmdbId) {
        super(action);
        this.movieTmdbId = movieTmdbId;
    }

    @Override
    public boolean supportsHexagon() {
        return true;
    }

    @Override
    public boolean supportsTrakt() {
        return true;
    }

    @Override
    public boolean applyLocalChanges(Context context, boolean requiresNetworkJob) {
        // prepare network job
        byte[] networkJobInfo = null;
        if (requiresNetworkJob) {
            networkJobInfo = prepareNetworkJob();
            if (networkJobInfo == null) {
                return false;
            }
        }

        if (!applyDatabaseUpdate(context, movieTmdbId)) {
            return false;
        }

        // persist network job after successful local updates
        if (requiresNetworkJob) {
            if (!persistNetworkJob(context, networkJobInfo)) {
                return false;
            }
        }

        // post event to update button states
        EventBus.getDefault().post(new MovieTools.MovieChangedEvent(movieTmdbId));

        return true;
    }

    protected abstract boolean applyDatabaseUpdate(Context context, int movieTmdbId);

    @Nullable
    private byte[] prepareNetworkJob() {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);

        int jobInfo = SgJobInfo.createSgJobInfo(builder, 0, 0, 0, movieTmdbId);

        builder.finish(jobInfo);
        return builder.sizedByteArray();
    }

}
