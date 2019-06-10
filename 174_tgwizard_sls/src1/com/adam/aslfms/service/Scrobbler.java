/**
 * This file is part of Simple Last.fm Scrobbler.
 * <p/>
 * https://github.com/tgwizard/sls
 * <p/>
 * Copyright 2011 Simple Last.fm Scrobbler Team
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.adam.aslfms.service;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.adam.aslfms.R;
import com.adam.aslfms.service.Handshaker.HandshakeResult;
import com.adam.aslfms.util.AppSettings;
import com.adam.aslfms.util.AuthStatus;
import com.adam.aslfms.util.AuthStatus.BadSessionException;
import com.adam.aslfms.util.AuthStatus.TemporaryFailureException;
import com.adam.aslfms.util.MD5;
import com.adam.aslfms.util.ScrobblesDatabase;
import com.adam.aslfms.util.Track;
import com.adam.aslfms.util.Util;
import com.adam.aslfms.util.enums.PowerOptions;
import com.adam.aslfms.util.enums.SubmissionType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

/**
 * @author tgwizard
 */
public class Scrobbler extends AbstractSubmitter {

    private static final String TAG = "Scrobbler";

    // private final Context mCtx;
    private final ScrobblesDatabase mDb;

    private final Context mCtx;

    private final AppSettings settings;

    public static final int MAX_SCROBBLE_LIMIT = 50;

    public Scrobbler(NetApp napp, Context ctx, Networker net,
                     ScrobblesDatabase db) {
        super(napp, ctx, net);
        this.mDb = db;
        this.mCtx = ctx;
        this.settings = new AppSettings(ctx);
    }

    @Override
    public boolean doRun(HandshakeResult hInfo) {
        boolean ret;
        try {

            NetApp netApp = getNetApp();
            String netAppName = netApp.getName();

            Log.d(TAG, "Scrobbling: " + netAppName);
            Track[] tracks = mDb.fetchTracksArray(netApp, MAX_SCROBBLE_LIMIT);

            if (tracks.length == 0) {
                Log.d(TAG, "Retrieved 0 tracks from db, no scrobbling: " + netAppName);
                return true;
            }
            Log.d(TAG, "Retrieved " + tracks.length + " tracks from db: " + netAppName);

            for (Track track : tracks) {
                Log.d(TAG, netAppName + ": " + track.toString());
            }

            scrobbleCommit(hInfo, tracks); // throws if unsuccessful

            // delete scrobbles (not tracks) from db (not array)
            for (Track track : tracks) {
                mDb.deleteScrobble(netApp, track.getRowId());
            }

            // clean up tracks if no one else wants to scrobble them
            mDb.cleanUpTracks();

            // there might be more tracks in the db
            if (tracks.length == MAX_SCROBBLE_LIMIT) {
                Log.d(TAG, "Relaunching scrobbler, might be more tracks in db");
                relaunchThis();
            }

            // status stuff
            notifySubmissionStatusSuccessful(tracks[tracks.length - 1],
                    tracks.length);

            ret = true;
        } catch (BadSessionException e) {
            Log.i(TAG, "BadSession: " + e.getMessage() + ": "
                    + getNetApp().getName());
            settings.setSessionKey(getNetApp(), "");
            getNetworker().launchHandshaker();
            relaunchThis();
            notifySubmissionStatusFailure(getContext().getString(
                    R.string.auth_just_error));
            e.getStackTrace();
            Util.myNotify(mCtx, getNetApp().getName(),
                    mCtx.getString(R.string.auth_bad_auth), 39201);
            ret = true;
        } catch (TemporaryFailureException e) {
            Log.i(TAG, "Tempfail: " + e.getMessage() + ": "
                    + getNetApp().getName());
            notifySubmissionStatusFailure(getContext().getString(
                    R.string.auth_network_error_retrying));
            e.getStackTrace();
            ret = false;
        } catch (AuthStatus.ClientBannedException e) {
            Log.e(TAG, "This version of the client has been banned!!" + ": "
                    + getNetApp().getName());
            Log.e(TAG, e.getMessage());
            // TODO: what??  notify user
            notifyAuthStatusUpdate(AuthStatus.AUTHSTATUS_CLIENTBANNED);
            Util.myNotify(mCtx, getNetApp().getName(),
                    mCtx.getString(R.string.auth_client_banned), 39201);
            e.getStackTrace();
            ret = true;
        } catch (AuthStatus.UnknownResponseException e) {
            if (Util.checkForOkNetwork(getContext()) != Util.NetworkStatus.OK) {
                // no more sleeping, network down
                Log.e(TAG, "Network status: " + Util.checkForOkNetwork(getContext()));
                getNetworker().resetSleeper();
                getNetworker().launchNetworkWaiter();
                relaunchThis();
            } else {
                getNetworker().launchSleeper();
                relaunchThis();
            }
            e.getStackTrace();
            ret = false;
        }
        return ret;
    }

    @Override
    protected void relaunchThis() {
        getNetworker().launchScrobbler();
    }

    private void notifySubmissionStatusFailure(String reason) {
        super.notifySubmissionStatusFailure(SubmissionType.SCROBBLE, reason);
    }

    private void notifySubmissionStatusSuccessful(Track track, int statsInc) {
        super.notifySubmissionStatusSuccessful(SubmissionType.SCROBBLE, track,
                statsInc);
        NetworkerManager mNetManager = new NetworkerManager(mCtx, mDb);
    }

    private void notifyAuthStatusUpdate(int st) {
        settings.setAuthStatus(getNetApp(), st);
        Intent i = new Intent(ScrobblingService.BROADCAST_ONAUTHCHANGED);
        i.putExtra("netapp", getNetApp().getIntentExtraValue());
        getContext().sendBroadcast(i);
    }

    /**
     * @return a {@link ScrobbleResult} struct with some info
     * @throws BadSessionException
     * @throws TemporaryFailureException
     */
    public void scrobbleCommit(HandshakeResult hInfo, Track[] tracks)
            throws BadSessionException, TemporaryFailureException, AuthStatus.ClientBannedException, AuthStatus.UnknownResponseException {

        NetApp netApp = getNetApp();
        String netAppName = netApp.getName();
        NetworkerManager mNetManager = new NetworkerManager(mCtx, mDb);
        PowerOptions pow = Util.checkPower(mCtx);

        if (netApp == NetApp.LISTENBRAINZ || netApp == NetApp.CUSTOM2) {
            URL url;
            HttpsURLConnection conn = null;
            try {
                url = new URL(getNetApp().getWebserviceUrl(settings) + "submit-listens");
                /**
                 *
                 */

                final String userPwd = "token " + settings.getListenBrainzToken(netApp);

                // Create the SSL connection
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, null, new java.security.SecureRandom());

                SSLSocketFactory customSockets = new SecureSSLSocketFactory(sslContext.getSocketFactory(), new MyHandshakeCompletedListener());

                conn = (HttpsURLConnection) url.openConnection();
                conn.setSSLSocketFactory(customSockets);

                /*String[] strArr = customSockets.getDefaultCipherSuites();
                for (String str : strArr) {
                    Log.e(TAG, str);
                }
                Log.e(TAG, strArr.length + " ..\n ..\n");
                Log.e(TAG, "HERE");
                strArr = customSockets.getSupportedCipherSuites();
                for (String str : strArr) {
                    Log.e(TAG, str);
                }*/

                JSONObject baseObj = new JSONObject();
                baseObj.put("listen_type", "import");
                JSONArray payArray = new JSONArray();
                for (int i = 0; i < tracks.length; i++) {
                    Track track = tracks[i];

                    JSONObject trackInfo = new JSONObject();
                    trackInfo.put("listened_at", Long.toString(track.getWhen()));

                    JSONObject trackMetaData = new JSONObject();
                    trackMetaData.put("artist_name", track.getArtist());
                    trackMetaData.put("track_name", track.getTrack());
                    if (track.getAlbum() != null && !track.getAlbum().equals("")) {
                        trackMetaData.put("release_name", track.getAlbum());
                    }
                    /*
                    if (track.getMbid() != null && !track.getMbid().equals("")) {
                        JSONObject additionalInfo = new JSONObject();
                        additionalInfo.put("recording_mbid", track.getMbid());
                        trackMetaData.put("additional_info", additionalInfo);
                    }*/

                    trackInfo.put("track_metadata", trackMetaData);

                    payArray.put(trackInfo);
                }

                baseObj.put("payload", payArray);

                // set Timeout and method
                conn.setReadTimeout(7000);
                conn.setConnectTimeout(7000);
                conn.setRequestMethod("POST");

                //conn.setUseCaches(false);

                conn.addRequestProperty("Authorization", userPwd);
                conn.addRequestProperty("Content-Type", "application/json; charset=UTF-8");

                conn.setDoInput(true);
                conn.setDoOutput(true);

                DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
                //Log.e(TAG, baseObj.toString());
                outStream.write(baseObj.toString().getBytes("UTF-8"));
                outStream.flush();
                outStream.close();

                conn.connect();
                int resCode = conn.getResponseCode();
                BufferedReader r;
                if (resCode == -1) {
                    throw new AuthStatus.UnknownResponseException("Empty response");
                } else if (resCode == 200) {
                    r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    r = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    stringBuilder.append(line).append('\n');
                }
                String response = stringBuilder.toString();
                Log.d(TAG, response);
                if (resCode == 401) {
                    settings.setListenBrainzToken(netApp, "");
                    throw new BadSessionException("ListenBrainz submission failed because of bad token.");
                }
                if (response.equals("")) {
                    throw new AuthStatus.UnknownResponseException("Empty response");
                }
                JSONObject jObject = new JSONObject(response);
                if (jObject.has("status")) {
                    Log.i(TAG, "Listen success: " + netAppName);
                } else if (jObject.has("error")) {
                    Log.i(TAG, "Listen failed: " + response);
                    if (resCode != 400) {
                        // don't throw if code is 400 because it's a badly formatted
                        // submission and we don't want to cache/try again
                        throw new AuthStatus.UnknownResponseException("Invalid Response");
                    }
                } else {
                    throw new AuthStatus.UnknownResponseException("Invalid Response");
                }
            } catch (KeyManagementException | NoSuchAlgorithmException | IOException | JSONException e) {
                e.printStackTrace();
                throw new AuthStatus.UnknownResponseException("Invalid Response");
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB && netApp == NetApp.LIBREFM) {

            URL url;
            HttpURLConnection conn = null;
            try {
                url = new URL(hInfo.scrobbleUri);

                Map<String, Object> params = new TreeMap<>();
                params.put("s", hInfo.sessionId);
                for (int i = 0; i < tracks.length; i++) {
                    Track track = tracks[i];
                    String is = "[" + i + "]";
                    params.put("a" + is, track.getArtist());
                    params.put("b" + is, track.getAlbum());
                    params.put("t" + is, track.getTrack());
                    params.put("i" + is, Long.toString(track
                            .getWhen()));
                    params.put("o" + is, track.getSource());
                    params.put("l" + is, Integer.toString(track
                            .getDuration()));
                    params.put("n" + is, track.getTrackNr());
                    params.put("m" + is, track.getMbid());
                    params.put("r" + is, track.getRating());
                }
                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    if (param.getValue() == null) {
                        param.setValue("");
                    } else {
                        postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                    }
                }
                byte[] postDataBytes = postData.toString().getBytes("UTF-8");

                conn = (HttpURLConnection) url.openConnection();
                // Log.d(TAG,conn.toString());
                conn.setReadTimeout(7000);
                conn.setConnectTimeout(7000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.getOutputStream().write(postDataBytes);
                //Log.i(TAG, params.toString());
                int resCode = conn.getResponseCode();
                Log.d(TAG, "Response code: " + resCode);
                BufferedReader r;
                if (resCode == 200) {
                    r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    r = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }
                StringBuilder rsponse = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    rsponse.append(line).append('\n');
                }
                r.close();
                String response = rsponse.toString();
                // some redundancy here ?
                String[] lines = response.split("\n");
                //Log.d(TAG, "Scrobbler Result: " + lines.length + " : " + response);
                if (response.startsWith("OK")) {
                    Log.i(TAG, "Scrobbler success: " + netAppName);
                } else if (response.startsWith("BADSESSION")) {
                    throw new BadSessionException("Scrobble failed because of badsession");
                } else if (response.startsWith("FAILED")) {
                    String reason = lines[0].substring(7);
                    throw new TemporaryFailureException("Scrobble failed: " + reason);
                } else {
                    throw new AuthStatus.UnknownResponseException("Invalid Response");
                }

            } catch (IOException e) {
                e.printStackTrace();
                throw new AuthStatus.UnknownResponseException("Invalid Response");
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        } else {

            URL url;
            HttpURLConnection conn = null;

            try {
                url = new URL(getNetApp().getWebserviceUrl(settings));

                Map<String, Object> params = new TreeMap<>();
                String sign = "";
                params.put("method", "track.scrobble");
                params.put("api_key", settings.rcnvK(settings.getAPIkey()));
                params.put("sk", settings.getSessionKey(netApp));
                for (int i = 0; i < tracks.length; i++) {
                    Track track = tracks[i];
                    String is = "[" + i + "]";

                    if (track.getAlbum() != null) {
                        params.put("album" + is, track.getAlbum());
                    }
                    params.put("artist" + is, track.getArtist());
                    if (track.getSource().equals("R") || track.getSource().equals("E")) {
                        params.put("chosenByUser" + is, 0);
                    }
                    if (track.getDuration() != -1) {
                        params.put("duration" + is, Integer.toString(track.getDuration()));
                    }
                    if (track.getMbid() != null) {
                        params.put("mbid" + is, track.getMbid());
                    }
                    params.put("timestamp" + is, track.getWhen());
                    params.put("track" + is, track.getTrack());
                    if (track.getTrackNr() != null) {
                        params.put("trackNumber" + is, track.getTrackNr());
                    }

                    if (track.getRating().equals("L")) {
                        mNetManager.launchHeartTrack(track, netApp);
                    }
                }
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    sign += param.getKey() + String.valueOf(param.getValue());
                }

                String signature = MD5.getHashString(sign + settings.rcnvK(settings.getSecret()));
                params.put("api_sig", signature);
                params.put("format", "json");

                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }
                byte[] postDataBytes = postData.toString().getBytes("UTF-8");

                conn = (HttpURLConnection) url.openConnection();
                // Log.d(TAG,conn.toString());
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.getOutputStream().write(postDataBytes);
                //Log.i(TAG, params.toString());
                int resCode = conn.getResponseCode();
                Log.d(TAG, "Response code: " + resCode);
                BufferedReader r;
                if (resCode == -1) {
                    throw new AuthStatus.UnknownResponseException("Empty response");
                } else if (resCode == 200) {
                    r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    r = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    stringBuilder.append(line).append('\n');
                }
                String response = stringBuilder.toString();
                Log.d(TAG, response);
                if (response.equals("")) {
                    throw new AuthStatus.UnknownResponseException("Empty response");
                }
                JSONObject jObject = new JSONObject(response);
                if (jObject.has("scrobbles")) {
                    int scrobsIgnored = jObject.getJSONObject("scrobbles").getJSONObject("@attr").getInt("ignored");
                    if (settings.isNowPlayingEnabled(pow)) {
                        mNetManager.launchGetUserInfo(getNetApp());
                    }
                    Log.i(TAG, "Scrobble success: " + netAppName + ": Ignored Count: " + Integer.toString(scrobsIgnored));
                } else if (jObject.has("error")) {
                    int code = jObject.getInt("error");
                    if (code == 26 || code == 10) {
                        Log.e(TAG, "Scobble failed: client banned: " + netApp.getName());
                        settings.setSessionKey(netApp, "");
                        throw new AuthStatus.ClientBannedException("Now Playing failed because of client banned");
                    } else if (code == 9) {
                        Log.i(TAG, "Scrobble failed: bad auth: " + netApp.getName());
                        settings.setSessionKey(netApp, "");
                        throw new BadSessionException("Now Playing failed because of badsession");
                    } else {
                        Log.e(TAG, "Scrobble fails: FAILED " + response + ": " + netApp.getName());
                        //settings.setSessionKey(netApp, "");

                        throw new TemporaryFailureException("Now playing failed because of " + response);
                    }
                } else {
                    throw new AuthStatus.UnknownResponseException("Invalid Response");
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                throw new AuthStatus.UnknownResponseException("JSON ERROR");
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
    }
}