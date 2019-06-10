package com.battlelancer.seriesguide.traktapi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import com.battlelancer.seriesguide.SgApp;
import com.battlelancer.seriesguide.enums.Result;
import com.battlelancer.seriesguide.sync.NetworkJobProcessor;
import com.battlelancer.seriesguide.sync.SgSyncAdapter;
import com.battlelancer.seriesguide.util.Errors;
import com.uwetrottmann.androidutils.AndroidUtils;
import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.AccessToken;
import com.uwetrottmann.trakt5.entities.Settings;
import com.uwetrottmann.trakt5.services.Users;
import java.io.IOException;
import javax.inject.Inject;
import org.greenrobot.eventbus.EventBus;
import retrofit2.Response;

/**
 * Expects a valid trakt OAuth auth code. Retrieves the access token and username for the associated
 * user. If successful, the credentials are stored.
 */
public class ConnectTraktTask extends AsyncTask<String, Void, Integer> {

    public class FinishedEvent {
        /**
         * One of {@link com.battlelancer.seriesguide.enums.NetworkResult}.
         */
        public int resultCode;

        FinishedEvent(int resultCode) {
            this.resultCode = resultCode;
        }
    }

    @SuppressLint("StaticFieldLeak") // using application context
    private final Context context;
    @Inject TraktV2 trakt;
    @Inject Users traktUsers;

    ConnectTraktTask(Context context) {
        this.context = context.getApplicationContext();
        SgApp.getServicesComponent(context).inject(this);
    }

    @Override
    protected Integer doInBackground(String... params) {
        // check for connectivity
        if (!AndroidUtils.isNetworkConnected(context)) {
            return TraktResult.OFFLINE;
        }

        // get account data
        String authCode = params[0];

        // check if we have any usable data
        if (TextUtils.isEmpty(authCode)) {
            return TraktResult.AUTH_ERROR;
        }

        // get access token
        String accessToken = null;
        String refreshToken = null;
        long expiresIn = -1;
        try {
            Response<AccessToken> response = trakt.exchangeCodeForAccessToken(authCode);
            AccessToken body = response.body();
            if (response.isSuccessful() && body != null) {
                accessToken = body.access_token;
                refreshToken = body.refresh_token;
                expiresIn = body.expires_in;
            } else {
                Errors.logAndReport("get access token", response);
            }
        } catch (IOException e) {
            Errors.logAndReport("get access token", e);
        }

        // did we obtain all required data?
        if (TextUtils.isEmpty(accessToken) || TextUtils.isEmpty(refreshToken) || expiresIn < 1) {
            return TraktResult.AUTH_ERROR;
        }

        // reset sync state before hasCredentials may return true
        new NetworkJobProcessor(context).removeObsoleteJobs();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context)
                .edit();
        // make next sync merge local watched and collected episodes with those on trakt
        editor.putBoolean(TraktSettings.KEY_HAS_MERGED_EPISODES, false);
        // make next sync merge local movies with those on trakt
        editor.putBoolean(TraktSettings.KEY_HAS_MERGED_MOVIES, false);

        // make sure the next sync will run a full episode sync
        editor.putLong(TraktSettings.KEY_LAST_FULL_EPISODE_SYNC, 0);
        // make sure the next sync will download all watched movies
        editor.putLong(TraktSettings.KEY_LAST_MOVIES_WATCHED_AT, 0);
        // make sure the next sync will download all ratings
        editor.putLong(TraktSettings.KEY_LAST_SHOWS_RATED_AT, 0);
        editor.putLong(TraktSettings.KEY_LAST_EPISODES_RATED_AT, 0);
        editor.putLong(TraktSettings.KEY_LAST_MOVIES_RATED_AT, 0);
        editor.apply();

        // store the access token, refresh token and expiry time
        TraktCredentials.get(context).storeAccessToken(accessToken);
        if (!TraktCredentials.get(context).hasCredentials()) {
            return Result.ERROR; // saving access token failed, abort.
        }
        if (!TraktOAuthSettings.storeRefreshData(context, refreshToken, expiresIn)) {
            TraktCredentials.get(context).removeCredentials();
            return Result.ERROR; // saving refresh token failed, abort.
        }

        // get user and display name
        String username = null;
        String displayname = null;
        try {
            Response<Settings> response = traktUsers.settings().execute();
            Settings body = response.body();
            if (response.isSuccessful() && body != null) {
                if (body.user != null) {
                    username = body.user.username;
                    displayname = body.user.name;
                }
            } else {
                Errors.logAndReport("get user settings", response);
                if (SgTrakt.isUnauthorized(response)) {
                    // access token already is invalid, remove it :(
                    TraktCredentials.get(context).removeCredentials();
                    return TraktResult.AUTH_ERROR;
                }
            }
        } catch (Exception e) {
            Errors.logAndReport("get user settings", e);
            return AndroidUtils.isNetworkConnected(context)
                    ? TraktResult.API_ERROR : TraktResult.OFFLINE;
        }

        // did we obtain a username (display name is not required)?
        if (TextUtils.isEmpty(username)) {
            return TraktResult.API_ERROR;
        }
        TraktCredentials.get(context).storeUsername(username, displayname);

        return Result.SUCCESS;
    }

    @Override
    protected void onPostExecute(Integer resultCode) {
        if (resultCode == Result.SUCCESS) {
            // trigger a sync, notifies user via toast
            SgSyncAdapter.requestSyncDeltaImmediate(context, true);
        }

        EventBus.getDefault().post(new FinishedEvent(resultCode));
    }
}
