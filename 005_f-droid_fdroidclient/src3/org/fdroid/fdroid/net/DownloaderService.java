/*
 * Copyright (C) 2008 The Android Open Source Project
 * Copyright (C) 2016 Hans-Christoph Steiner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fdroid.fdroid.net;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PatternMatcher;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.LogPrinter;
import org.fdroid.fdroid.BuildConfig;
import org.fdroid.fdroid.FDroidApp;
import org.fdroid.fdroid.ProgressListener;
import org.fdroid.fdroid.R;
import org.fdroid.fdroid.Utils;
import org.fdroid.fdroid.data.RepoProvider;
import org.fdroid.fdroid.data.SanitizedFile;
import org.fdroid.fdroid.installer.ApkCache;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLKeyException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLProtocolException;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpRetryException;
import java.net.NoRouteToHostException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * DownloaderService is a service that handles asynchronous download requests
 * (expressed as {@link Intent}s) on demand.  Clients send download requests
 * through {@link #queue(Context, String, long, String)} calls.  The
 * service is started as needed, it handles each {@code Intent} using a worker
 * thread, and stops itself when it runs out of work.  Requests can be canceled
 * using {@link #cancel(Context, String)}.  If this service is killed during
 * operation, it will receive the queued {@link #queue(Context, String, long, String)}
 * and {@link #cancel(Context, String)} requests again due to
 * {@link Service#START_REDELIVER_INTENT}.  Bad requests will be ignored,
 * including on restart after killing via {@link Service#START_NOT_STICKY}.
 * <p>
 * This "work queue processor" pattern is commonly used to offload tasks
 * from an application's main thread.  The DownloaderService class exists to
 * simplify this pattern and take care of the mechanics. DownloaderService
 * will receive the Intents, launch a worker thread, and stop the service as
 * appropriate.
 * <p>
 * All requests are handled on a single worker thread -- they may take as
 * long as necessary (and will not block the application's main loop), but
 * only one request will be processed at a time.
 * <p>
 * The Canonical URL for the file to download is also used as the unique ID to
 * represent the download itself throughout F-Droid.  This follows the model
 * of {@link Intent#setData(Uri)}, where the core data of an {@code Intent} is
 * a {@code Uri}.  For places that need an {@code int} ID,
 * {@link String#hashCode()} should be used to get a reproducible, unique {@code int}
 * from any {@code canonicalUrl}.  That full URL is guaranteed to be unique since
 * it points to a file on a filesystem.  This is more important with media files
 * than with APKs since there is not reliable standard for a unique ID for
 * media files, unlike APKs with {@code packageName} and {@code versionCode}.
 *
 * @see android.app.IntentService
 * @see org.fdroid.fdroid.installer.InstallManagerService
 */
public class DownloaderService extends Service {
    private static final String TAG = "DownloaderService";

    private static final String ACTION_QUEUE = "org.fdroid.fdroid.net.DownloaderService.action.QUEUE";
    private static final String ACTION_CANCEL = "org.fdroid.fdroid.net.DownloaderService.action.CANCEL";

    private volatile Looper serviceLooper;
    private static volatile ServiceHandler serviceHandler;
    private static volatile Downloader downloader;
    private static volatile String activeCanonicalUrl;
    private LocalBroadcastManager localBroadcastManager;
    private static volatile int timeout;

    private final class ServiceHandler extends Handler {
        static final String TAG = "ServiceHandler";

        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Utils.debugLog(TAG, "Handling download message with ID of " + msg.what);
            handleIntent((Intent) msg.obj);
            stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.debugLog(TAG, "Creating downloader service.");

        HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        serviceLooper = thread.getLooper();
        if (BuildConfig.DEBUG) {
            serviceLooper.setMessageLogging(new LogPrinter(Log.DEBUG, ServiceHandler.TAG));
        }
        serviceHandler = new ServiceHandler(serviceLooper);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utils.debugLog(TAG, "Received Intent for downloading: " + intent + " (with a startId of " + startId + ")");

        if (intent == null) {
            return START_NOT_STICKY;
        }

        String downloadUrl = intent.getDataString();
        if (downloadUrl == null) {
            Utils.debugLog(TAG, "Received Intent with no URI: " + intent);
            return START_NOT_STICKY;
        }
        String canonicalUrl = intent.getStringExtra(Downloader.EXTRA_CANONICAL_URL);
        if (canonicalUrl == null) {
            Utils.debugLog(TAG, "Received Intent with no EXTRA_CANONICAL_URL: " + intent);
            return START_NOT_STICKY;
        }

        if (ACTION_CANCEL.equals(intent.getAction())) {
            Utils.debugLog(TAG, "Cancelling download of " + canonicalUrl.hashCode() + "/" + canonicalUrl
                    + " downloading from " + downloadUrl);
            Integer whatToRemove = canonicalUrl.hashCode();
            if (serviceHandler.hasMessages(whatToRemove)) {
                Utils.debugLog(TAG, "Removing download with ID of " + whatToRemove
                        + " from service handler, then sending interrupted event.");
                serviceHandler.removeMessages(whatToRemove);
                sendCancelledBroadcast(intent.getData(), canonicalUrl);
            } else if (isActive(canonicalUrl)) {
                downloader.cancelDownload();
            } else {
                Utils.debugLog(TAG, "ACTION_CANCEL called on something not queued or running"
                        + " (expected to find message with ID of " + whatToRemove + " in queue).");
            }
        } else if (ACTION_QUEUE.equals(intent.getAction())) {
            Message msg = serviceHandler.obtainMessage();
            msg.arg1 = startId;
            msg.obj = intent;
            msg.what = canonicalUrl.hashCode();
            serviceHandler.sendMessage(msg);
            Utils.debugLog(TAG, "Queued download of " + canonicalUrl.hashCode() + "/" + canonicalUrl
                    + " using " + downloadUrl);
        } else {
            Utils.debugLog(TAG, "Received Intent with unknown action: " + intent);
        }

        return START_REDELIVER_INTENT; // if killed before completion, retry Intent
    }

    @Override
    public void onDestroy() {
        Utils.debugLog(TAG, "Destroying downloader service. Will move to background and stop our Looper.");
        serviceLooper.quit(); //NOPMD - this is copied from IntentService, no super call needed
    }

    /**
     * This service does not use binding, so no need to implement this method
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same DownloaderService, but it will not hold up anything else.
     * When all requests have been handled, the DownloaderService stops itself,
     * so you should not ever call {@link #stopSelf}.
     * <p/>
     * Downloads are put into subdirectories based on hostname/port of each repo
     * to prevent files with the same names from conflicting.  Each repo enforces
     * unique APK file names on the server side.
     *
     * @param intent The {@link Intent} passed via {@link
     *               android.content.Context#startService(Intent)}.
     * @see org.fdroid.fdroid.IndexV1Updater#update()
     */
    private void handleIntent(Intent intent) {
        final Uri uri = intent.getData();
        final long repoId = intent.getLongExtra(Downloader.EXTRA_REPO_ID, 0);
        final Uri canonicalUrl = Uri.parse(intent.getStringExtra(Downloader.EXTRA_CANONICAL_URL));
        final SanitizedFile localFile = ApkCache.getApkDownloadPath(this, canonicalUrl);
        sendBroadcast(uri, Downloader.ACTION_STARTED, localFile, repoId, canonicalUrl);

        try {
            activeCanonicalUrl = canonicalUrl.toString();
            downloader = DownloaderFactory.create(this, uri, localFile);
            downloader.setListener(new ProgressListener() {
                @Override
                public void onProgress(long bytesRead, long totalBytes) {
                    Intent intent = new Intent(Downloader.ACTION_PROGRESS);
                    intent.setData(canonicalUrl);
                    intent.putExtra(Downloader.EXTRA_BYTES_READ, bytesRead);
                    intent.putExtra(Downloader.EXTRA_TOTAL_BYTES, totalBytes);
                    localBroadcastManager.sendBroadcast(intent);
                }
            });
            downloader.setTimeout(timeout);
            downloader.download();
            if (downloader.isNotFound()) {
                sendBroadcast(uri, Downloader.ACTION_INTERRUPTED, localFile, getString(R.string.download_404),
                        repoId, canonicalUrl);
            } else {
                sendBroadcast(uri, Downloader.ACTION_COMPLETE, localFile, repoId, canonicalUrl);
            }
        } catch (InterruptedException e) {
            sendBroadcast(uri, Downloader.ACTION_INTERRUPTED, localFile, repoId, canonicalUrl);
        } catch (ConnectException | HttpRetryException | NoRouteToHostException | SocketTimeoutException
                | SSLHandshakeException | SSLKeyException | SSLPeerUnverifiedException | SSLProtocolException
                | ProtocolException | UnknownHostException e) {
            // if the above list of exceptions changes, also change it in IndexV1Updater.update()
            Log.e(TAG, "CONNECTION_FAILED: " + e.getLocalizedMessage());
            sendBroadcast(uri, Downloader.ACTION_CONNECTION_FAILED, localFile, repoId, canonicalUrl);
        } catch (IOException e) {
            e.printStackTrace();
            sendBroadcast(uri, Downloader.ACTION_INTERRUPTED, localFile,
                    e.getLocalizedMessage(), repoId, canonicalUrl);
        } finally {
            if (downloader != null) {
                downloader.close();
            }
        }
        downloader = null;
        activeCanonicalUrl = null;
    }

    private void sendCancelledBroadcast(Uri uri, String canonicalUrl) {
        sendBroadcast(uri, Downloader.ACTION_INTERRUPTED, null, 0, Uri.parse(canonicalUrl));
    }

    private void sendBroadcast(Uri uri, String action, File file, long repoId, Uri canonicalUrl) {
        sendBroadcast(uri, action, file, null, repoId, canonicalUrl);
    }

    private void sendBroadcast(Uri uri, String action, File file, String errorMessage, long repoId,
                               Uri canonicalUrl) {
        Intent intent = new Intent(action);
        if (canonicalUrl != null) {
            intent.setData(canonicalUrl);
        }
        if (file != null) {
            intent.putExtra(Downloader.EXTRA_DOWNLOAD_PATH, file.getAbsolutePath());
        }
        if (!TextUtils.isEmpty(errorMessage)) {
            intent.putExtra(Downloader.EXTRA_ERROR_MESSAGE, errorMessage);
        }
        intent.putExtra(Downloader.EXTRA_REPO_ID, repoId);
        intent.putExtra(Downloader.EXTRA_MIRROR_URL, uri.toString());
        localBroadcastManager.sendBroadcast(intent);
    }

    /**
     * Add a URL to the download queue.
     * <p>
     * All notifications are sent as an {@link Intent} via local broadcasts to be received by
     *
     * @param context      this app's {@link Context}
     * @param mirrorUrl    The URL to add to the download queue
     * @param repoId       the database ID number representing one repo
     * @param canonicalUrl the URL used as the unique ID throughout F-Droid
     * @see #cancel(Context, String)
     */
    public static void queue(Context context, String mirrorUrl, long repoId, String canonicalUrl) {
        if (TextUtils.isEmpty(mirrorUrl)) {
            return;
        }
        Utils.debugLog(TAG, "Queue download " + canonicalUrl.hashCode() + "/" + canonicalUrl
                + " using " + mirrorUrl);
        Intent intent = new Intent(context, DownloaderService.class);
        intent.setAction(ACTION_QUEUE);
        intent.setData(Uri.parse(mirrorUrl));
        intent.putExtra(Downloader.EXTRA_REPO_ID, repoId);
        intent.putExtra(Downloader.EXTRA_CANONICAL_URL, canonicalUrl);
        context.startService(intent);
    }

    /**
     * Add a package to the download queue, choosing a random mirror to
     * download from.
     *
     * @param canonicalUrl the URL used as the unique ID throughout F-Droid,
     *                     needed here to support canceling active downloads
     */
    public static void queueUsingRandomMirror(Context context, long repoId, String canonicalUrl) {
        String mirrorUrl = FDroidApp.switchUrlToNewMirror(canonicalUrl,
                RepoProvider.Helper.findById(context, repoId));
        queue(context, mirrorUrl, repoId, canonicalUrl);
    }

    /**
     * Tries to return a version of {@code urlString} from a mirror, if there
     * is an error, it just returns {@code urlString}.
     *
     * @see FDroidApp#getNewMirrorOnError(String, org.fdroid.fdroid.data.Repo)
     */
    public static void queueUsingDifferentMirror(Context context, long repoId, String canonicalUrl) {
        try {
            String mirrorUrl = FDroidApp.getNewMirrorOnError(canonicalUrl,
                    RepoProvider.Helper.findById(context, repoId));
            queue(context, mirrorUrl, repoId, canonicalUrl);
        } catch (IOException e) {
            queue(context, canonicalUrl, repoId, canonicalUrl);
        }
    }

    /**
     * Remove a URL to the download queue, even if it is currently downloading.
     * <p>
     * All notifications are sent as an {@link Intent} via local broadcasts to be received by
     *
     * @param context      this app's {@link Context}
     * @param canonicalUrl The URL to remove from the download queue
     * @see #queue(Context, String, long, String)
     */
    public static void cancel(Context context, String canonicalUrl) {
        if (TextUtils.isEmpty(canonicalUrl)) {
            return;
        }
        Utils.debugLog(TAG, "Send cancel for " + canonicalUrl.hashCode() + "/" + canonicalUrl);
        Intent intent = new Intent(context, DownloaderService.class);
        intent.setAction(ACTION_CANCEL);
        intent.setData(Uri.parse(canonicalUrl));
        intent.putExtra(Downloader.EXTRA_CANONICAL_URL, canonicalUrl);
        context.startService(intent);
    }

    /**
     * Check if a URL is waiting in the queue for downloading or if actively being downloaded.
     * This is useful for checking whether to re-register {@link android.content.BroadcastReceiver}s
     * in {@link android.app.Activity#onResume()}.
     */
    public static boolean isQueuedOrActive(String canonicalUrl) {
        if (TextUtils.isEmpty(canonicalUrl)) { //NOPMD - suggests unreadable format
            return false;
        }
        if (serviceHandler == null) {
            return false; // this service is not even running
        }
        return serviceHandler.hasMessages(canonicalUrl.hashCode()) || isActive(canonicalUrl);
    }

    /**
     * Check if a URL is actively being downloaded.
     */
    private static boolean isActive(String downloadUrl) {
        return downloader != null && TextUtils.equals(downloadUrl, activeCanonicalUrl);
    }

    public static void setTimeout(int ms) {
        timeout = ms;
    }

    /**
     * Get a prepared {@link IntentFilter} for use for matching this service's action events.
     *
     * @param canonicalUrl the URL used as the unique ID for the specific package
     */
    public static IntentFilter getIntentFilter(String canonicalUrl) {
        Uri uri = Uri.parse(canonicalUrl);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Downloader.ACTION_STARTED);
        intentFilter.addAction(Downloader.ACTION_PROGRESS);
        intentFilter.addAction(Downloader.ACTION_COMPLETE);
        intentFilter.addAction(Downloader.ACTION_INTERRUPTED);
        intentFilter.addAction(Downloader.ACTION_CONNECTION_FAILED);
        intentFilter.addDataScheme(uri.getScheme());
        intentFilter.addDataAuthority(uri.getHost(), String.valueOf(uri.getPort()));
        intentFilter.addDataPath(uri.getPath(), PatternMatcher.PATTERN_LITERAL);
        return intentFilter;
    }
}
