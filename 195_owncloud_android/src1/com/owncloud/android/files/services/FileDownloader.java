/**
 * ownCloud Android client application
 *
 * @author Bartek Przybylski
 * @author Christian Schabesberger
 * @author David González Verdugo
 * Copyright (C) 2012 Bartek Przybylski
 * Copyright (C) 2019 ownCloud GmbH.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2,
 * as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.owncloud.android.files.services;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Pair;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.owncloud.android.R;
import com.owncloud.android.authentication.AccountUtils;
import com.owncloud.android.authentication.AuthenticatorActivity;
import com.owncloud.android.datamodel.FileDataStorageManager;
import com.owncloud.android.datamodel.OCFile;
import com.owncloud.android.lib.common.OwnCloudAccount;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientManagerFactory;
import com.owncloud.android.lib.common.network.OnDatatransferProgressListener;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.files.FileUtils;
import com.owncloud.android.operations.DownloadFileOperation;
import com.owncloud.android.ui.activity.FileActivity;
import com.owncloud.android.ui.activity.FileDisplayActivity;
import com.owncloud.android.ui.errorhandling.ErrorMessageAdapter;
import com.owncloud.android.ui.notifications.NotificationUtils;
import com.owncloud.android.ui.preview.PreviewImageActivity;
import com.owncloud.android.ui.preview.PreviewImageFragment;
import com.owncloud.android.utils.Extras;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

public class FileDownloader extends Service
        implements OnDatatransferProgressListener, OnAccountsUpdateListener {

    public static final String KEY_ACCOUNT = "ACCOUNT";
    public static final String KEY_FILE = "FILE";
    public static final String KEY_IS_AVAILABLE_OFFLINE_FILE = "KEY_IS_AVAILABLE_OFFLINE_FILE";
    public static final String KEY_RETRY_DOWNLOAD = "KEY_RETRY_DOWNLOAD";

    private static final String DOWNLOAD_ADDED_MESSAGE = "DOWNLOAD_ADDED";
    private static final String DOWNLOAD_FINISH_MESSAGE = "DOWNLOAD_FINISH";
    private static final String DOWNLOAD_NOTIFICATION_CHANNEL_ID = "DOWNLOAD_NOTIFICATION_CHANNEL";

    private static final String TAG = FileDownloader.class.getSimpleName();

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private IBinder mBinder;
    private OwnCloudClient mDownloadClient = null;
    private Account mCurrentAccount = null;
    private FileDataStorageManager mStorageManager;

    private IndexedForest<DownloadFileOperation> mPendingDownloads = new IndexedForest<>();

    private DownloadFileOperation mCurrentDownload = null;

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;
    private int mLastPercent;

    private LocalBroadcastManager mLocalBroadcastManager;

    public static String getDownloadAddedMessage() {
        return FileDownloader.class.getName() + DOWNLOAD_ADDED_MESSAGE;
    }

    public static String getDownloadFinishMessage() {
        return FileDownloader.class.getName() + DOWNLOAD_FINISH_MESSAGE;
    }

    /**
     * Service initialization
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log_OC.d(TAG, "Creating service");

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotificationBuilder = NotificationUtils.newNotificationBuilder(this);

        // Configure notification channel
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mNotificationChannel;
            // The user-visible name of the channel.
            CharSequence name = getString(R.string.download_notification_channel_name);
            // The user-visible description of the channel.
            String description = getString(R.string.download_notification_channel_description);
            // Set importance low: show the notification everywhere but with no sound
            int importance = NotificationManager.IMPORTANCE_LOW;
            mNotificationChannel = new NotificationChannel(DOWNLOAD_NOTIFICATION_CHANNEL_ID,
                    name, importance);
            // Configure the notification channel.
            mNotificationChannel.setDescription(description);
            mNotificationManager.createNotificationChannel(mNotificationChannel);
        }

        HandlerThread thread = new HandlerThread("FileDownloaderThread",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper, this);
        mBinder = new FileDownloaderBinder();

        // add AccountsUpdatedListener
        AccountManager am = AccountManager.get(getApplicationContext());
        am.addOnAccountsUpdatedListener(this, null, false);

        // create manager for local broadcasts
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    /**
     * Service clean up
     */
    @Override
    public void onDestroy() {
        Log_OC.v(TAG, "Destroying service");
        mBinder = null;
        mServiceHandler = null;
        mServiceLooper.quit();
        mServiceLooper = null;
        mNotificationManager = null;

        // remove AccountsUpdatedListener
        AccountManager am = AccountManager.get(getApplicationContext());
        am.removeOnAccountsUpdatedListener(this);

        super.onDestroy();
    }

    /**
     * Entry point to add one or several files to the queue of downloads.
     *
     * New downloads are added calling to startService(), resulting in a call to this method.
     * This ensures the service will keep on working although the caller activity goes away.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log_OC.d(TAG, "Starting command with id " + startId);

        boolean isAvailableOfflineFile = intent.getBooleanExtra(KEY_IS_AVAILABLE_OFFLINE_FILE, false);
        boolean retryDownload = intent.getBooleanExtra(KEY_RETRY_DOWNLOAD, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && (isAvailableOfflineFile || retryDownload)) {
            /**
             * We have to call this within five seconds after the service is created with startForegroundService when:
             * - Checking available offline files in background
             * - Retry downloads in background, e.g. when recovering wifi connection
             */
            Log_OC.d(TAG, "Starting FileDownloader service in foreground");
            startForeground(1, mNotificationBuilder.build());
        }

        if (!intent.hasExtra(KEY_ACCOUNT) ||
                !intent.hasExtra(KEY_FILE)
        ) {
            Log_OC.e(TAG, "Not enough information provided in intent");
            return START_NOT_STICKY;
        } else {
            final Account account = intent.getParcelableExtra(KEY_ACCOUNT);
            final OCFile file = intent.getParcelableExtra(KEY_FILE);
            AbstractList<String> requestedDownloads = new Vector<>();
            try {
                DownloadFileOperation newDownload = new DownloadFileOperation(account, file);
                newDownload.addDatatransferProgressListener(this);
                newDownload.addDatatransferProgressListener((FileDownloaderBinder) mBinder);
                Pair<String, String> putResult = mPendingDownloads.putIfAbsent(
                        account.name, file.getRemotePath(), newDownload);
                if (putResult != null) {
                    String downloadKey = putResult.first;
                    requestedDownloads.add(downloadKey);
                    sendBroadcastNewDownload(newDownload, putResult.second);
                }   // else, file already in the queue of downloads; don't repeat the request

            } catch (IllegalArgumentException e) {
                Log_OC.e(TAG, "Not enough information provided in intent: " + e.getMessage());
                return START_NOT_STICKY;
            }

            if (requestedDownloads.size() > 0) {
                Message msg = mServiceHandler.obtainMessage();
                msg.arg1 = startId;
                msg.obj = requestedDownloads;
                mServiceHandler.sendMessage(msg);
            }
        }

        return START_NOT_STICKY;
    }

    /**
     * Provides a binder object that clients can use to perform operations on the queue of downloads,
     * excepting the addition of new files.
     * <p/>
     * Implemented to perform cancellation, pause and resume of existing downloads.
     */
    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    /**
     * Called when ALL the bound clients were onbound.
     */
    @Override
    public boolean onUnbind(Intent intent) {
        ((FileDownloaderBinder) mBinder).clearListeners();
        return false;   // not accepting rebinding (default behaviour)
    }

    @Override
    public void onAccountsUpdated(Account[] accounts) {
        //review the current download and cancel it if its account doesn't exist
        if (mCurrentDownload != null &&
                !AccountUtils.exists(mCurrentDownload.getAccount().name, getApplicationContext())) {
            mCurrentDownload.cancel();
        }
        // The rest of downloads are cancelled when they try to start
    }

    /**
     * Binder to let client components to perform operations on the queue of downloads.
     * <p/>
     * It provides by itself the available operations.
     */
    public class FileDownloaderBinder extends Binder implements OnDatatransferProgressListener {

        /**
         * Map of listeners that will be reported about progress of downloads from a
         * {@link FileDownloaderBinder}
         * instance.
         */
        private Map<Long, WeakReference<OnDatatransferProgressListener>> mBoundListeners =
                new HashMap<>();

        /**
         * Cancels a pending or current download of a remote file.
         *
         * @param account ownCloud account where the remote file is stored.
         * @param file    A file in the queue of pending downloads
         */
        public void cancel(Account account, OCFile file) {
            Pair<DownloadFileOperation, String> removeResult =
                    mPendingDownloads.remove(account.name, file.getRemotePath());
            DownloadFileOperation download = removeResult.first;
            if (download != null) {
                download.cancel();
            } else {
                if (mCurrentDownload != null && mCurrentAccount != null &&
                        mCurrentDownload.getRemotePath().startsWith(file.getRemotePath()) &&
                        account.name.equals(mCurrentAccount.name)) {
                    mCurrentDownload.cancel();
                }
            }
        }

        /**
         * Cancels all the downloads for an account
         *
         * @param account   ownCloud account.
         */
        public void cancel(Account account) {
            Log_OC.d(TAG, "Account= " + account.name);

            if (mCurrentDownload != null) {
                Log_OC.d(TAG, "Current Download Account= " + mCurrentDownload.getAccount().name);
                if (mCurrentDownload.getAccount().name.equals(account.name)) {
                    mCurrentDownload.cancel();
                }
            }
            // Cancel pending downloads
            cancelDownloadsForAccount(account);
        }

        public void clearListeners() {
            mBoundListeners.clear();
        }

        /**
         * Returns True when the file described by 'file' in the ownCloud account 'account'
         * is downloading or waiting to download.
         *
         * If 'file' is a directory, returns 'true' if any of its descendant files is downloading or
         * waiting to download.
         *
         * @param account ownCloud account where the remote file is stored.
         * @param file    A file that could be in the queue of downloads.
         */
        public boolean isDownloading(Account account, OCFile file) {
            if (account == null || file == null) {
                return false;
            }
            return (mPendingDownloads.contains(account.name, file.getRemotePath()));
        }

        /**
         * Adds a listener interested in the progress of the download for a concrete file.
         *
         * @param listener Object to notify about progress of transfer.
         * @param account  ownCloud account holding the file of interest.
         * @param file     {@link OCFile} of interest for listener.
         */
        public void addDatatransferProgressListener(
                OnDatatransferProgressListener listener, Account account, OCFile file
        ) {
            if (account == null || file == null || listener == null) {
                return;
            }
            mBoundListeners.put(file.getFileId(), new WeakReference<>(listener));
        }

        /**
         * Removes a listener interested in the progress of the download for a concrete file.
         *
         * @param listener      Object to notify about progress of transfer.
         * @param account       ownCloud account holding the file of interest.
         * @param file          {@link OCFile} of interest for listener.
         */
        public void removeDatatransferProgressListener(
                OnDatatransferProgressListener listener, Account account, OCFile file
        ) {
            if (account == null || file == null || listener == null) {
                return;
            }
            Long fileId = file.getFileId();
            if (mBoundListeners.get(fileId) == listener) {
                mBoundListeners.remove(fileId);
            }
        }

        @Override
        public void onTransferProgress(long progressRate, long totalTransferredSoFar,
                                       long totalToTransfer, String fileName) {
            WeakReference<OnDatatransferProgressListener> boundListenerRef =
                    mBoundListeners.get(mCurrentDownload.getFile().getFileId());
            if (boundListenerRef != null && boundListenerRef.get() != null) {
                boundListenerRef.get().onTransferProgress(
                        progressRate,
                        totalTransferredSoFar,
                        totalToTransfer,
                        fileName
                );
            }
        }

    }

    /**
     * Download worker. Performs the pending downloads in the order they were requested.
     * Created with the Looper of a new thread, started in {@link FileUploader#onCreate()}.
     */
    private static class ServiceHandler extends Handler {
        // don't make it a final class, and don't remove the static ; lint will warn about a
        // possible memory leak
        FileDownloader mService;

        public ServiceHandler(Looper looper, FileDownloader service) {
            super(looper);
            if (service == null) {
                throw new IllegalArgumentException("Received invalid NULL in parameter 'service'");
            }
            mService = service;
        }

        @Override
        public void handleMessage(Message msg) {
            @SuppressWarnings("unchecked")
            AbstractList<String> requestedDownloads = (AbstractList<String>) msg.obj;
            if (msg.obj != null) {
                Iterator<String> it = requestedDownloads.iterator();
                while (it.hasNext()) {
                    String next = it.next();
                    mService.downloadFile(next);
                }
            }
            Log_OC.d(TAG, "Stopping after command with id " + msg.arg1);
            mService.stopForeground(true);
            mService.stopSelf(msg.arg1);
        }
    }

    /**
     * Core download method: requests a file to download and stores it.
     *
     * @param downloadKey Key to access the download to perform, contained in mPendingDownloads
     */
    private void downloadFile(String downloadKey) {

        mCurrentDownload = mPendingDownloads.get(downloadKey);

        if (mCurrentDownload != null) {

            /// Check account existence
            if (!AccountUtils.exists(mCurrentDownload.getAccount().name, this)) {
                Log_OC.w(
                        TAG,
                        "Account " + mCurrentDownload.getAccount().name +
                                " does not exist anymore -> cancelling all its downloads"
                );
                cancelDownloadsForAccount(mCurrentDownload.getAccount());
                return;
            }

            notifyDownloadStart(mCurrentDownload);

            RemoteOperationResult downloadResult = null;

            try {
                /// prepare client object to send the request to the ownCloud server
                if (mCurrentAccount == null ||
                        !mCurrentAccount.equals(mCurrentDownload.getAccount())) {
                    mCurrentAccount = mCurrentDownload.getAccount();
                    mStorageManager = new FileDataStorageManager(
                            this, mCurrentAccount,
                            getContentResolver()
                    );
                }   // else, reuse storage manager from previous operation

                // always get client from client manager to get fresh credentials in case of update
                OwnCloudAccount ocAccount = new OwnCloudAccount(
                        mCurrentAccount,
                        this
                );
                mDownloadClient = OwnCloudClientManagerFactory.getDefaultSingleton().
                        getClientFor(ocAccount, this);

                /// perform the download
                downloadResult = mCurrentDownload.execute(mDownloadClient);
                if (downloadResult.isSuccess()) {
                    saveDownloadedFile();
                }

            } catch (Exception e) {
                Log_OC.e(TAG, "Error downloading", e);
                downloadResult = new RemoteOperationResult(e);

            } finally {
                Pair<DownloadFileOperation, String> removeResult =
                        mPendingDownloads.removePayload(
                                mCurrentAccount.name,
                                mCurrentDownload.getRemotePath()
                        );

                if (!downloadResult.isSuccess() && downloadResult.getException() != null) {

                    // if failed due to lack of connectivity, schedule an automatic retry
                    TransferRequester requester = new TransferRequester();
                    if (requester.shouldScheduleRetry(this, downloadResult.getException())) {
                        int jobId = mPendingDownloads.buildKey(
                                mCurrentAccount.name,
                                mCurrentDownload.getRemotePath()
                        ).hashCode();
                        requester.scheduleDownload(
                                this,
                                jobId,
                                mCurrentAccount.name,
                                mCurrentDownload.getRemotePath()
                        );
                        downloadResult = new RemoteOperationResult(
                                ResultCode.NO_NETWORK_CONNECTION);
                    } else {
                        Log_OC.v(
                                TAG,
                                String.format(
                                        "Exception in download, network is OK, no retry scheduled for %1s in %2s",
                                        mCurrentDownload.getRemotePath(),
                                        mCurrentAccount.name
                                )
                        );
                    }
                } else {
                    Log_OC.v(
                            TAG,
                            String.format(
                                    "Success OR fail without exception for %1s in %2s",
                                    mCurrentDownload.getRemotePath(),
                                    mCurrentAccount.name
                            )
                    );
                }

                /// notify result
                notifyDownloadResult(mCurrentDownload, downloadResult);

                sendBroadcastDownloadFinished(mCurrentDownload, downloadResult, removeResult.second);
            }

        }
    }

    /**
     * Updates the OC File after a successful download.
     *
     * TODO move to DownloadFileOperation
     */
    private void saveDownloadedFile() {
        OCFile file = mStorageManager.getFileById(mCurrentDownload.getFile().getFileId());
        long syncDate = System.currentTimeMillis();
        file.setLastSyncDateForProperties(syncDate);
        file.setLastSyncDateForData(syncDate);
        file.setNeedsUpdateThumbnail(true);
        file.setModificationTimestamp(mCurrentDownload.getModificationTimestamp());
        file.setModificationTimestampAtLastSyncForData(mCurrentDownload.getModificationTimestamp());
        file.setEtag(mCurrentDownload.getEtag());
        file.setMimetype(mCurrentDownload.getMimeType());
        file.setStoragePath(mCurrentDownload.getSavePath());
        file.setFileLength((new File(mCurrentDownload.getSavePath()).length()));
        file.setRemoteId(mCurrentDownload.getFile().getRemoteId());
        mStorageManager.saveFile(file);
        mStorageManager.triggerMediaScan(file.getStoragePath());
        mStorageManager.saveConflict(file, null);
    }

    /**
     * Creates a status notification to show the download progress
     *
     * @param download Download operation starting.
     */
    private void notifyDownloadStart(DownloadFileOperation download) {

        /// create status notification with a progress bar
        mLastPercent = 0;
        mNotificationBuilder
                .setSmallIcon(R.drawable.notification_icon)
                .setTicker(getString(R.string.downloader_download_in_progress_ticker))
                .setContentTitle(getString(R.string.downloader_download_in_progress_ticker))
                .setOngoing(true)
                .setProgress(100, 0, download.getSize() < 0)
                .setContentText(
                        String.format(getString(R.string.downloader_download_in_progress_content), 0,
                                new File(download.getSavePath()).getName()))
                .setChannelId(DOWNLOAD_NOTIFICATION_CHANNEL_ID)
                .setWhen(System.currentTimeMillis());

        /// includes a pending intent in the notification showing the details view of the file
        Intent showDetailsIntent = null;
        if (PreviewImageFragment.canBePreviewed(download.getFile())) {
            showDetailsIntent = new Intent(this, PreviewImageActivity.class);
        } else {
            showDetailsIntent = new Intent(this, FileDisplayActivity.class);
        }

        showDetailsIntent.putExtra(FileActivity.EXTRA_FILE, download.getFile());
        showDetailsIntent.putExtra(FileActivity.EXTRA_ACCOUNT, download.getAccount());
        showDetailsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        mNotificationBuilder.setContentIntent(PendingIntent.getActivity(
                this, (int) System.currentTimeMillis(), showDetailsIntent, 0
        ));

        mNotificationManager.notify(R.string.downloader_download_in_progress_ticker, mNotificationBuilder.build());
    }

    /**
     * Callback method to update the progress bar in the status notification.
     */
    @Override
    public void onTransferProgress(long progressRate, long totalTransferredSoFar,
                                   long totalToTransfer, String filePath) {
        int percent = (int) (100.0 * ((double) totalTransferredSoFar) / ((double) totalToTransfer));
        if (percent != mLastPercent) {
            mNotificationBuilder.setProgress(100, percent, totalToTransfer < 0);
            String fileName = filePath.substring(filePath.lastIndexOf(FileUtils.PATH_SEPARATOR) + 1);
            String text = String.format(getString(R.string.downloader_download_in_progress_content), percent, fileName);
            mNotificationBuilder.setContentText(text);
            mNotificationBuilder.setChannelId(DOWNLOAD_NOTIFICATION_CHANNEL_ID);
            mNotificationManager.notify(R.string.downloader_download_in_progress_ticker, mNotificationBuilder.build());
        }
        mLastPercent = percent;
    }

    /**
     * Updates the status notification with the result of a download operation.
     *
     * @param downloadResult Result of the download operation.
     * @param download       Finished download operation
     */
    private void notifyDownloadResult(DownloadFileOperation download,
                                      RemoteOperationResult downloadResult) {
        mNotificationManager.cancel(R.string.downloader_download_in_progress_ticker);
        if (!downloadResult.isCancelled()) {
            int tickerId = (downloadResult.isSuccess()) ? R.string.downloader_download_succeeded_ticker :
                    R.string.downloader_download_failed_ticker;

            boolean needsToUpdateCredentials = (ResultCode.UNAUTHORIZED.equals(downloadResult.getCode()));
            tickerId = (needsToUpdateCredentials) ?
                    R.string.downloader_download_failed_credentials_error : tickerId;

            mNotificationBuilder
                    .setTicker(getString(tickerId))
                    .setContentTitle(getString(tickerId))
                    .setAutoCancel(true)
                    .setOngoing(false)
                    .setProgress(0, 0, false);

            if (needsToUpdateCredentials) {

                // let the user update credentials with one click
                Intent updateAccountCredentials = new Intent(this, AuthenticatorActivity.class);
                updateAccountCredentials.putExtra(AuthenticatorActivity.EXTRA_ACCOUNT,
                        download.getAccount());
                updateAccountCredentials.putExtra(
                        AuthenticatorActivity.EXTRA_ACTION,
                        AuthenticatorActivity.ACTION_UPDATE_EXPIRED_TOKEN
                );
                updateAccountCredentials.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                updateAccountCredentials.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                updateAccountCredentials.addFlags(Intent.FLAG_FROM_BACKGROUND);
                mNotificationBuilder
                        .setContentIntent(PendingIntent.getActivity(
                                this, (int) System.currentTimeMillis(), updateAccountCredentials,
                                PendingIntent.FLAG_ONE_SHOT));

            } else {
                // TODO put something smart in showDetailsIntent
                Intent showDetailsIntent = new Intent();
                mNotificationBuilder
                        .setContentIntent(PendingIntent.getActivity(
                                this, (int) System.currentTimeMillis(), showDetailsIntent, 0));
            }

            mNotificationBuilder.setContentText(
                    ErrorMessageAdapter.getResultMessage(downloadResult, download,
                            getResources())
            );
            mNotificationBuilder.setChannelId(DOWNLOAD_NOTIFICATION_CHANNEL_ID);
            mNotificationManager.notify(tickerId, mNotificationBuilder.build());

            // Remove success notification
            if (downloadResult.isSuccess()) {
                // Sleep 2 seconds, so show the notification before remove it
                NotificationUtils.cancelWithDelay(
                        mNotificationManager,
                        R.string.downloader_download_succeeded_ticker,
                        2000);
            }

        }
    }

    /**
     * Sends a broadcast when a download finishes in order to the interested activities can
     * update their view
     *
     * @param download               Finished download operation
     * @param downloadResult         Result of the download operation
     * @param unlinkedFromRemotePath Path in the downloads tree where the download was unlinked from
     */
    private void sendBroadcastDownloadFinished(
            DownloadFileOperation download,
            RemoteOperationResult downloadResult,
            String unlinkedFromRemotePath) {

        Intent end = new Intent(getDownloadFinishMessage());
        end.putExtra(Extras.EXTRA_DOWNLOAD_RESULT, downloadResult.isSuccess());
        end.putExtra(Extras.EXTRA_ACCOUNT_NAME, download.getAccount().name);
        end.putExtra(Extras.EXTRA_REMOTE_PATH, download.getRemotePath());
        end.putExtra(Extras.EXTRA_FILE_PATH, download.getSavePath());
        if (unlinkedFromRemotePath != null) {
            end.putExtra(Extras.EXTRA_LINKED_TO_PATH, unlinkedFromRemotePath);
        }
        mLocalBroadcastManager.sendBroadcast(end);
    }

    /**
     * Sends a broadcast when a new download is added to the queue.
     *
     * @param download           Added download operation
     * @param linkedToRemotePath Path in the downloads tree where the download was linked to
     */
    private void sendBroadcastNewDownload(DownloadFileOperation download,
                                          String linkedToRemotePath) {
        Intent added = new Intent(getDownloadAddedMessage());
        added.putExtra(Extras.EXTRA_ACCOUNT_NAME, download.getAccount().name);
        added.putExtra(Extras.EXTRA_REMOTE_PATH, download.getRemotePath());
        added.putExtra(Extras.EXTRA_FILE_PATH, download.getSavePath());
        added.putExtra(Extras.EXTRA_LINKED_TO_PATH, linkedToRemotePath);
        mLocalBroadcastManager.sendBroadcast(added);
    }

    /**
     * Remove downloads of an account
     *
     * @param account       Downloads account to remove
     */
    private void cancelDownloadsForAccount(Account account) {
        // Cancel pending downloads
        mPendingDownloads.remove(account.name);
    }
}
