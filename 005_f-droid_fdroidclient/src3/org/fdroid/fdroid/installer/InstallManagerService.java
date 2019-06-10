package org.fdroid.fdroid.installer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.fdroid.fdroid.AppUpdateStatusManager;
import org.fdroid.fdroid.FDroidApp;
import org.fdroid.fdroid.Hasher;
import org.fdroid.fdroid.Utils;
import org.fdroid.fdroid.compat.PackageManagerCompat;
import org.fdroid.fdroid.data.Apk;
import org.fdroid.fdroid.data.App;
import org.fdroid.fdroid.data.RepoProvider;
import org.fdroid.fdroid.net.Downloader;
import org.fdroid.fdroid.net.DownloaderService;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * Manages the whole process when a background update triggers an install or the user
 * requests an APK to be installed.  It handles checking whether the APK is cached,
 * downloading it, putting up and maintaining a {@link Notification}, and more. This
 * {@code Service} tracks packages that are in the process as "Pending Installs".
 * Then {@link DownloaderService} and {@link InstallerService} individually track
 * packages for those phases of the whole install process.  Each of those
 * {@code Services} have their own related events.  For tracking status during the
 * whole process, {@link AppUpdateStatusManager} tracks the status as represented by
 * {@link AppUpdateStatusManager.AppUpdateStatus}.
 * <p>
 * The {@link App} and {@link Apk} instances are sent via
 * {@link Intent#putExtra(String, android.os.Bundle)}
 * so that Android handles the message queuing and {@link Service} lifecycle for us.
 * For example, if this {@code InstallManagerService} gets killed, Android will cache
 * and then redeliver the {@link Intent} for us, which includes all of the data needed
 * for {@code InstallManagerService} to do its job for the whole lifecycle of an install.
 * This {@code Service} never stops itself after completing the action, e.g.
 * {@code {@link #stopSelf(int)}}, so {@code Intent}s are sometimes redelivered even
 * though they are no longer valid.  {@link #onStartCommand(Intent, int, int)} checks
 * first that the incoming {@code Intent} is not an invalid, redelivered {@code Intent}.
 * {@link #isPendingInstall(String)} and other checks are used to check whether to
 * process the redelivered {@code Intent} or not.
 * <p>
 * The canonical URL for the APK file to download is also used as the unique ID to
 * represent the download itself throughout F-Droid.  This follows the model
 * of {@link Intent#setData(Uri)}, where the core data of an {@code Intent} is
 * a {@code Uri}.  The full download URL is guaranteed to be unique since it
 * points to files on a filesystem, where there cannot be multiple files with
 * the same name.  This provides a unique ID beyond just {@code packageName}
 * and {@code versionCode} since there could be different copies of the same
 * APK on different servers, signed by different keys, or even different builds.
 * <p><ul>
 * <li>for a {@link Uri} ID, use {@code Uri}, {@link Intent#getData()}
 * <li>for a {@code String} ID, use {@code canonicalUrl}, {@link Uri#toString()}, or
 * {@link Intent#getDataString()}
 * <li>for an {@code int} ID, use {@link String#hashCode()} or {@link Uri#hashCode()}
 * <li>for an {@link Intent} extra, use {@link org.fdroid.fdroid.net.Downloader#EXTRA_CANONICAL_URL}
 * </ul></p>
 * The implementations of {@link Uri#toString()} and {@link Intent#getDataString()} both
 * include caching of the generated {@code String}, so it should be plenty fast.
 * <p>
 * This also handles downloading OBB "APK Extension" files for any APK that has one
 * assigned to it.  OBB files are queued up for download before the APK so that they
 * are hopefully in place before the APK starts.  That is not guaranteed though.
 * <p>
 * There may be multiple, available APK files with the same hash. Although it
 * is not a security issue to install one or the other, they may have different
 * metadata to display in the client.  Thus, it may result in weirdness if one
 * has a different name/description/summary, etc).
 *
 * @see <a href="https://developer.android.com/google/play/expansion-files.html">APK Expansion Files</a>
 */
@SuppressWarnings("LineLength")
public class InstallManagerService extends Service {
    private static final String TAG = "InstallManagerService";

    private static final String ACTION_INSTALL = "org.fdroid.fdroid.installer.action.INSTALL";
    private static final String ACTION_CANCEL = "org.fdroid.fdroid.installer.action.CANCEL";

    private static final String EXTRA_APP = "org.fdroid.fdroid.installer.extra.APP";
    private static final String EXTRA_APK = "org.fdroid.fdroid.installer.extra.APK";

    private static SharedPreferences pendingInstalls;

    private LocalBroadcastManager localBroadcastManager;
    private AppUpdateStatusManager appUpdateStatusManager;
    private boolean running = false;

    /**
     * This service does not use binding, so no need to implement this method
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        appUpdateStatusManager = AppUpdateStatusManager.getInstance(this);
        running = true;
        pendingInstalls = getPendingInstalls(this);
    }

    /**
     * If this {@link Service} is stopped, then all of the various
     * {@link BroadcastReceiver}s need to unregister themselves if they get
     * called.  There can be multiple {@code BroadcastReceiver}s registered,
     * so it can't be done with a simple call here. So {@link #running} is the
     * signal to all the existing {@code BroadcastReceiver}s to unregister.
     */
    @Override
    public void onDestroy() {
        running = false;
        super.onDestroy();
    }

    /**
     * This goes through a series of checks to make sure that the incoming
     * {@link Intent} is still valid.  The default {@link Intent#getAction() action}
     * in the logic is {@link #ACTION_INSTALL} since it is the most complicate
     * case.  Since the {@code Intent} will be redelivered by Android if the
     * app was killed, this needs to check that it still makes sense to handle.
     * <p>
     * For example, if F-Droid is killed while installing, it might not receive
     * the message that the install completed successfully. The checks need to be
     * as specific as possible so as not to block things like installing updates
     * with the same {@link PackageInfo#versionCode}, which happens sometimes,
     * and is allowed by Android.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utils.debugLog(TAG, "onStartCommand " + intent);

        String canonicalUrl = intent.getDataString();
        if (TextUtils.isEmpty(canonicalUrl)) {
            Utils.debugLog(TAG, "empty canonicalUrl, nothing to do");
            return START_NOT_STICKY;
        }

        String action = intent.getAction();

        if (ACTION_CANCEL.equals(action)) {
            DownloaderService.cancel(this, canonicalUrl);
            Apk apk = appUpdateStatusManager.getApk(canonicalUrl);
            if (apk != null) {
                Utils.debugLog(TAG, "also canceling OBB downloads");
                DownloaderService.cancel(this, apk.getPatchObbUrl());
                DownloaderService.cancel(this, apk.getMainObbUrl());
            }
            return START_NOT_STICKY;
        } else if (ACTION_INSTALL.equals(action)) {
            if (!isPendingInstall(canonicalUrl)) {
                Log.i(TAG, "Ignoring INSTALL that is not Pending Install: " + intent);
                return START_NOT_STICKY;
            }
        } else {
            Log.i(TAG, "Ignoring unknown intent action: " + intent);
            return START_NOT_STICKY;
        }

        if (!intent.hasExtra(EXTRA_APP) || !intent.hasExtra(EXTRA_APK)) {
            Utils.debugLog(TAG, canonicalUrl + " did not include both an App and Apk instance, ignoring");
            return START_NOT_STICKY;
        }

        if ((flags & START_FLAG_REDELIVERY) == START_FLAG_REDELIVERY
                && !DownloaderService.isQueuedOrActive(canonicalUrl)) {
            Utils.debugLog(TAG, canonicalUrl + " finished downloading while InstallManagerService was killed.");
            appUpdateStatusManager.removeApk(canonicalUrl);
            return START_NOT_STICKY;
        }

        App app = intent.getParcelableExtra(EXTRA_APP);
        Apk apk = intent.getParcelableExtra(EXTRA_APK);
        if (app == null || apk == null) {
            Utils.debugLog(TAG, "Intent had null EXTRA_APP and/or EXTRA_APK: " + intent);
            return START_NOT_STICKY;
        }

        PackageInfo packageInfo = Utils.getPackageInfo(this, apk.packageName);
        if ((flags & START_FLAG_REDELIVERY) == START_FLAG_REDELIVERY
                && packageInfo != null && packageInfo.versionCode == apk.versionCode
                && TextUtils.equals(packageInfo.versionName, apk.versionName)) {
            Log.i(TAG, "INSTALL Intent no longer valid since its installed, ignoring: " + intent);
            return START_NOT_STICKY;
        }

        FDroidApp.resetMirrorVars();
        DownloaderService.setTimeout(FDroidApp.getTimeout());

        appUpdateStatusManager.addApk(apk, AppUpdateStatusManager.Status.Downloading, null);

        registerPackageDownloaderReceivers(canonicalUrl);
        getMainObb(canonicalUrl, apk);
        getPatchObb(canonicalUrl, apk);

        File apkFilePath = ApkCache.getApkDownloadPath(this, apk.getCanonicalUrl());
        long apkFileSize = apkFilePath.length();
        if (!apkFilePath.exists() || apkFileSize < apk.size) {
            Utils.debugLog(TAG, "download " + canonicalUrl + " " + apkFilePath);
            DownloaderService.queueUsingRandomMirror(this, apk.repoId, canonicalUrl);
        } else if (ApkCache.apkIsCached(apkFilePath, apk)) {
            Utils.debugLog(TAG, "skip download, we have it, straight to install " + canonicalUrl + " " + apkFilePath);
            sendBroadcast(intent.getData(), Downloader.ACTION_STARTED, apkFilePath);
            sendBroadcast(intent.getData(), Downloader.ACTION_COMPLETE, apkFilePath);
        } else {
            Utils.debugLog(TAG, "delete and download again " + canonicalUrl + " " + apkFilePath);
            apkFilePath.delete();
            DownloaderService.queueUsingRandomMirror(this, apk.repoId, canonicalUrl);
        }

        return START_REDELIVER_INTENT; // if killed before completion, retry Intent
    }

    private void sendBroadcast(Uri uri, String action, File file) {
        Intent intent = new Intent(action);
        intent.setData(uri);
        intent.putExtra(Downloader.EXTRA_DOWNLOAD_PATH, file.getAbsolutePath());
        localBroadcastManager.sendBroadcast(intent);
    }

    private void getMainObb(final String canonicalUrl, Apk apk) {
        getObb(canonicalUrl, apk.getMainObbUrl(), apk.getMainObbFile(), apk.obbMainFileSha256, apk.repoId);
    }

    private void getPatchObb(final String canonicalUrl, Apk apk) {
        getObb(canonicalUrl, apk.getPatchObbUrl(), apk.getPatchObbFile(), apk.obbPatchFileSha256, apk.repoId);
    }

    /**
     * Check if any OBB files are available, and if so, download and install them. This
     * also deletes any obsolete OBB files, per the spec, since there can be only one
     * "main" and one "patch" OBB installed at a time.
     *
     * @see <a href="https://developer.android.com/google/play/expansion-files.html">APK Expansion Files</a>
     */
    private void getObb(final String canonicalUrl, String obbUrlString,
                        final File obbDestFile, final String hash, final long repoId) {
        if (obbDestFile == null || obbDestFile.exists() || TextUtils.isEmpty(obbUrlString)) {
            return;
        }
        final BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!running) {
                    localBroadcastManager.unregisterReceiver(this);
                    return;
                }
                String action = intent.getAction();
                if (Downloader.ACTION_STARTED.equals(action)) {
                    Utils.debugLog(TAG, action + " " + intent);
                } else if (Downloader.ACTION_PROGRESS.equals(action)) {

                    long bytesRead = intent.getLongExtra(Downloader.EXTRA_BYTES_READ, 0);
                    long totalBytes = intent.getLongExtra(Downloader.EXTRA_TOTAL_BYTES, 0);
                    appUpdateStatusManager.updateApkProgress(canonicalUrl, totalBytes, bytesRead);
                } else if (Downloader.ACTION_COMPLETE.equals(action)) {
                    localBroadcastManager.unregisterReceiver(this);
                    File localFile = new File(intent.getStringExtra(Downloader.EXTRA_DOWNLOAD_PATH));
                    Uri localApkUri = Uri.fromFile(localFile);
                    Utils.debugLog(TAG, "OBB download completed " + intent.getDataString()
                            + " to " + localApkUri);

                    try {
                        if (Hasher.isFileMatchingHash(localFile, hash, "sha256")) {
                            Utils.debugLog(TAG, "Installing OBB " + localFile + " to " + obbDestFile);
                            FileUtils.forceMkdirParent(obbDestFile);
                            FileUtils.copyFile(localFile, obbDestFile);
                            FileFilter filter = new WildcardFileFilter(
                                    obbDestFile.getName().substring(0, 4) + "*.obb");
                            for (File f : obbDestFile.getParentFile().listFiles(filter)) {
                                if (!f.equals(obbDestFile)) {
                                    Utils.debugLog(TAG, "Deleting obsolete OBB " + f);
                                    FileUtils.deleteQuietly(f);
                                }
                            }
                        } else {
                            Utils.debugLog(TAG, localFile + " deleted, did not match hash: " + hash);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        FileUtils.deleteQuietly(localFile);
                    }
                } else if (Downloader.ACTION_INTERRUPTED.equals(action)) {
                    localBroadcastManager.unregisterReceiver(this);
                } else if (Downloader.ACTION_CONNECTION_FAILED.equals(action)) {
                    DownloaderService.queueUsingDifferentMirror(context, repoId, canonicalUrl);
                } else {
                    throw new RuntimeException("intent action not handled!");
                }
            }
        };
        DownloaderService.queueUsingRandomMirror(this, repoId, obbUrlString);
        localBroadcastManager.registerReceiver(downloadReceiver,
                DownloaderService.getIntentFilter(obbUrlString));
    }

    /**
     * Register a {@link BroadcastReceiver} for tracking download progress for a
     * give {@code canonicalUrl}.  There can be multiple of these registered at a time.
     */
    private void registerPackageDownloaderReceivers(String canonicalUrl) {

        BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!running) {
                    localBroadcastManager.unregisterReceiver(this);
                    return;
                }
                Uri canonicalUri = intent.getData();
                String canonicalUrl = intent.getDataString();
                long repoId = intent.getLongExtra(Downloader.EXTRA_REPO_ID, 0);

                switch (intent.getAction()) {
                    case Downloader.ACTION_STARTED:
                        // App should currently be in the "PendingDownload" state, so this changes it to "Downloading".
                        Intent intentObject = new Intent(context, InstallManagerService.class);
                        intentObject.setAction(ACTION_CANCEL);
                        intentObject.setData(canonicalUri);
                        PendingIntent action = PendingIntent.getService(context, 0, intentObject, 0);
                        appUpdateStatusManager.updateApk(canonicalUrl,
                                AppUpdateStatusManager.Status.Downloading, action);
                        break;
                    case Downloader.ACTION_PROGRESS:
                        long bytesRead = intent.getLongExtra(Downloader.EXTRA_BYTES_READ, 0);
                        long totalBytes = intent.getLongExtra(Downloader.EXTRA_TOTAL_BYTES, 0);
                        appUpdateStatusManager.updateApkProgress(canonicalUrl, totalBytes, bytesRead);
                        break;
                    case Downloader.ACTION_COMPLETE:
                        File localFile = new File(intent.getStringExtra(Downloader.EXTRA_DOWNLOAD_PATH));
                        Uri localApkUri = Uri.fromFile(localFile);

                        Utils.debugLog(TAG, "download completed of "
                                + intent.getStringExtra(Downloader.EXTRA_MIRROR_URL) + " to " + localApkUri);
                        appUpdateStatusManager.updateApk(canonicalUrl,
                                AppUpdateStatusManager.Status.ReadyToInstall, null);

                        localBroadcastManager.unregisterReceiver(this);
                        registerInstallReceiver(canonicalUrl);

                        Apk apk = appUpdateStatusManager.getApk(canonicalUrl);
                        if (apk != null) {
                            InstallerService.install(context, localApkUri, canonicalUri, apk);
                        }
                        break;
                    case Downloader.ACTION_INTERRUPTED:
                        appUpdateStatusManager.setDownloadError(canonicalUrl,
                                intent.getStringExtra(Downloader.EXTRA_ERROR_MESSAGE));
                        localBroadcastManager.unregisterReceiver(this);
                        break;
                    case Downloader.ACTION_CONNECTION_FAILED:
                        // TODO move this logic into DownloaderService to hide the mirror URL stuff from this class
                        try {
                            String currentUrlString = FDroidApp.getNewMirrorOnError(
                                    intent.getStringExtra(Downloader.EXTRA_MIRROR_URL),
                                    RepoProvider.Helper.findById(InstallManagerService.this, repoId));
                            DownloaderService.queue(context, currentUrlString, repoId, canonicalUrl);
                            DownloaderService.setTimeout(FDroidApp.getTimeout());
                        } catch (IOException e) {
                            appUpdateStatusManager.setDownloadError(canonicalUrl,
                                    intent.getStringExtra(Downloader.EXTRA_ERROR_MESSAGE));
                            localBroadcastManager.unregisterReceiver(this);
                        }
                        break;
                    default:
                        throw new RuntimeException("intent action not handled!");
                }
            }
        };

        localBroadcastManager.registerReceiver(downloadReceiver,
                DownloaderService.getIntentFilter(canonicalUrl));
    }

    /**
     * Register a {@link BroadcastReceiver} for tracking install progress for a
     * give {@link Uri}.  There can be multiple of these registered at a time.
     */
    private void registerInstallReceiver(String canonicalUrl) {

        BroadcastReceiver installReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!running) {
                    localBroadcastManager.unregisterReceiver(this);
                    return;
                }
                String canonicalUrl = intent.getDataString();
                Apk apk;
                switch (intent.getAction()) {
                    case Installer.ACTION_INSTALL_STARTED:
                        appUpdateStatusManager.updateApk(canonicalUrl,
                                AppUpdateStatusManager.Status.Installing, null);
                        break;
                    case Installer.ACTION_INSTALL_COMPLETE:
                        appUpdateStatusManager.updateApk(canonicalUrl,
                                AppUpdateStatusManager.Status.Installed, null);
                        Apk apkComplete = appUpdateStatusManager.getApk(canonicalUrl);

                        if (apkComplete != null && apkComplete.isApk()) {
                            try {
                                PackageManagerCompat.setInstaller(context, getPackageManager(), apkComplete.packageName);
                            } catch (SecurityException e) {
                                // Will happen if we fell back to DefaultInstaller for some reason.
                            }
                        }
                        localBroadcastManager.unregisterReceiver(this);
                        break;
                    case Installer.ACTION_INSTALL_INTERRUPTED:
                        apk = intent.getParcelableExtra(Installer.EXTRA_APK);
                        String errorMessage =
                                intent.getStringExtra(Installer.EXTRA_ERROR_MESSAGE);
                        if (!TextUtils.isEmpty(errorMessage)) {
                            appUpdateStatusManager.setApkError(apk, errorMessage);
                        } else {
                            appUpdateStatusManager.removeApk(canonicalUrl);
                        }
                        localBroadcastManager.unregisterReceiver(this);
                        break;
                    case Installer.ACTION_INSTALL_USER_INTERACTION:
                        apk = intent.getParcelableExtra(Installer.EXTRA_APK);
                        PendingIntent installPendingIntent = intent.getParcelableExtra(Installer.EXTRA_USER_INTERACTION_PI);
                        appUpdateStatusManager.addApk(apk, AppUpdateStatusManager.Status.ReadyToInstall, installPendingIntent);
                        break;
                    default:
                        throw new RuntimeException("intent action not handled!");
                }
            }
        };

        localBroadcastManager.registerReceiver(installReceiver,
                Installer.getInstallIntentFilter(canonicalUrl));
    }

    /**
     * Install an APK, checking the cache and downloading if necessary before
     * starting the process.  All notifications are sent as an {@link Intent}
     * via local broadcasts to be received by {@link BroadcastReceiver}s per
     * {@code urlString}.  This also marks a given APK as in the process of
     * being installed, with the {@code urlString} of the download used as the
     * unique ID,
     * <p>
     * and the file hash used to verify that things are the same.
     *
     * @param context this app's {@link Context}
     */
    public static void queue(Context context, App app, @NonNull Apk apk) {
        String canonicalUrl = apk.getCanonicalUrl();
        AppUpdateStatusManager.getInstance(context).addApk(apk, AppUpdateStatusManager.Status.PendingInstall, null);
        putPendingInstall(context, canonicalUrl, apk.packageName);
        Utils.debugLog(TAG, "queue " + app.packageName + " " + apk.versionCode + " from " + canonicalUrl);
        Intent intent = new Intent(context, InstallManagerService.class);
        intent.setAction(ACTION_INSTALL);
        intent.setData(Uri.parse(canonicalUrl));
        intent.putExtra(EXTRA_APP, app);
        intent.putExtra(EXTRA_APK, apk);
        context.startService(intent);
    }

    public static void cancel(Context context, String canonicalUrl) {
        removePendingInstall(context, canonicalUrl);
        Intent intent = new Intent(context, InstallManagerService.class);
        intent.setAction(ACTION_CANCEL);
        intent.setData(Uri.parse(canonicalUrl));
        context.startService(intent);
    }

    /**
     * Is the APK that matches the provided {@code hash} still waiting to be
     * installed?  This restarts the install process for this APK if it was
     * interrupted somehow, like if F-Droid was killed before the download
     * completed, or the device lost power in the middle of the install
     * process.
     */
    public boolean isPendingInstall(String canonicalUrl) {
        return pendingInstalls.contains(canonicalUrl);
    }

    /**
     * Mark a given APK as in the process of being installed, with
     * the {@code canonicalUrl} of the download used as the unique ID,
     * and the file hash used to verify that things are the same.
     *
     * @see #isPendingInstall(String)
     */
    public static void putPendingInstall(Context context, String canonicalUrl, String packageName) {
        if (pendingInstalls == null) {
            pendingInstalls = getPendingInstalls(context);
        }
        pendingInstalls.edit().putString(canonicalUrl, packageName).apply();
    }

    public static void removePendingInstall(Context context, String canonicalUrl) {
        if (pendingInstalls == null) {
            pendingInstalls = getPendingInstalls(context);
        }
        pendingInstalls.edit().remove(canonicalUrl).apply();
    }

    private static SharedPreferences getPendingInstalls(Context context) {
        return context.getSharedPreferences("pending-installs", Context.MODE_PRIVATE);
    }
}
