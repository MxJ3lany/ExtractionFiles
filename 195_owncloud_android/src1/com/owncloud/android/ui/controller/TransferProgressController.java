/**
 * ownCloud Android client application
 *
 * @author David A. Velasco
 * Copyright (C) 2016 ownCloud GmbH.
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
package com.owncloud.android.ui.controller;

import android.accounts.Account;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.UiThread;
import com.owncloud.android.datamodel.OCFile;
import com.owncloud.android.files.services.FileDownloader;
import com.owncloud.android.files.services.FileUploader;
import com.owncloud.android.lib.common.network.OnDatatransferProgressListener;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.ui.activity.ComponentsGetter;

/**
 * Controller updating a progress bar with the progress of a file transfer
 * reported from upload or download service.
 */
public class TransferProgressController implements OnDatatransferProgressListener {

    private static final String TAG = TransferProgressController.class.getCanonicalName();

    private ProgressBar mProgressBar = null;
    private ComponentsGetter mComponentsGetter = null;
    private int mLastPercent = 0;

    public TransferProgressController(ComponentsGetter componentsGetter) {
        if (componentsGetter == null) {
            throw new IllegalArgumentException("Received NULL componentsGetter");
        }
        mComponentsGetter = componentsGetter;
    }

    /**
     * Sets the progress bar that will updated with file transfer progress
     *
     * Accepts null input to stop updating any view.
     *
     * @param progressBar   Progress bar to update with progress transfer.
     */
    public void setProgressBar(ProgressBar progressBar) {
        mProgressBar = progressBar;
        if (mProgressBar != null) {
            reset();
        }
    }

    @UiThread
    public void showProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    public void hideProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Subscribes the controller to monitor transfers of the received file both in {@link FileDownloader} and
     * {@link FileUploader} services, if available.
     *
     * This method may be called several times for the same file, resulting in a single subscription.
     *
     * @param file          File to monitor in transfer services.
     * @param account       ownCloud account containing file.
     */
    @UiThread
    public void startListeningProgressFor(OCFile file, Account account) {
        FileDownloader.FileDownloaderBinder downloaderBinder = mComponentsGetter.getFileDownloaderBinder();
        FileUploader.FileUploaderBinder uploaderBinder = mComponentsGetter.getFileUploaderBinder();

        if (downloaderBinder != null) {
            downloaderBinder.addDatatransferProgressListener(this, account, file);
            if (mProgressBar != null && downloaderBinder.isDownloading(account, file)) {
                mProgressBar.setIndeterminate(true);
            }
        } else {
            Log_OC.i(TAG, "Download service not ready to notify progress");
        }

        if (uploaderBinder != null) {
            uploaderBinder.addDatatransferProgressListener(this, account, file);
            if (mProgressBar != null && uploaderBinder.isUploading(account, file)) {
                mProgressBar.setIndeterminate(true);
            }
        } else {
            Log_OC.i(TAG, "Upload service not ready to notify progress");
        }
    }

    /**
     * Unsubscribes the controller from {@link FileDownloader} and {@link FileUploader} services.
     *
     * @param file          File to stop monitoring in transfer services.
     * @param account       ownCloud account containing file.
     */
    @UiThread
    public void stopListeningProgressFor(OCFile file, Account account) {
        if (mComponentsGetter.getFileDownloaderBinder() != null) {
            mComponentsGetter.getFileDownloaderBinder().
                    removeDatatransferProgressListener(this, account, file);
        }
        if (mComponentsGetter.getFileUploaderBinder() != null) {
            mComponentsGetter.getFileUploaderBinder().
                    removeDatatransferProgressListener(this, account, file);
        }
        if (mProgressBar != null) {
            mProgressBar.setIndeterminate(false);
        }
    }

    /**
     * Implementation of {@link OnDatatransferProgressListener}, called from {@link FileUploader} or
     * {@link FileDownloader} to report the trasnfer progress of a monitored file.
     *
     * @param progressRate              Bytes transferred from the previous call.
     * @param totalTransferredSoFar     Total of bytes transferred so far.
     * @param totalToTransfer           Total of bytes to transfer.
     * @param filename                  Name of the transferred file.
     */
    @Override
    public void onTransferProgress(
            long progressRate,
            long totalTransferredSoFar,
            long totalToTransfer,
            String filename
    ) {
        if (mProgressBar != null) {
            final int percent = (int) (100.0 * ((double) totalTransferredSoFar) / ((double) totalToTransfer));
            if (percent != mLastPercent) {
                mProgressBar.post(
                        new Runnable() {
                            @Override
                            public void run() {
                                mProgressBar.setVisibility(View.VISIBLE);
                                mProgressBar.setIndeterminate(false);
                                mProgressBar.setProgress(percent);
                                mProgressBar.invalidate();
                            }
                        }
                );
            }
            mLastPercent = percent;
        }
    }

    /**
     * Initializes the properties of the linked progress bar, if any.
     */
    private void reset() {
        mLastPercent = -1;
        if (mProgressBar != null) {
            mProgressBar.setMax(100);
            mProgressBar.setProgress(0);
            mProgressBar.setIndeterminate(false);
        }
    }

}
