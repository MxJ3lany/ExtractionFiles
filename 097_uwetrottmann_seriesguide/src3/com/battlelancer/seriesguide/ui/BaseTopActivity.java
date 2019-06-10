
package com.battlelancer.seriesguide.ui;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.annotation.IdRes;
import androidx.appcompat.app.ActionBar;
import com.battlelancer.seriesguide.R;
import com.battlelancer.seriesguide.SgApp;
import com.battlelancer.seriesguide.backend.CloudSetupActivity;
import com.battlelancer.seriesguide.backend.HexagonTools;
import com.battlelancer.seriesguide.dataliberation.DataLiberationActivity;
import com.battlelancer.seriesguide.dataliberation.DataLiberationTools;
import com.battlelancer.seriesguide.sync.AccountUtils;
import com.google.android.material.snackbar.Snackbar;
import timber.log.Timber;

/**
 * Activities at the top of the navigation hierarchy, display the nav drawer upon pressing the
 * up/home action bar button.
 *
 * <p>Also provides support for an optional sync progress bar (see {@link
 * #setupSyncProgressBar(int)}).
 */
public abstract class BaseTopActivity extends BaseNavDrawerActivity {

    private View syncProgressBar;
    private Object syncObserverHandle;
    private Snackbar snackbar;

    @Override
    protected void setupActionBar() {
        super.setupActionBar();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void setupNavDrawer() {
        super.setupNavDrawer();

        // show a drawer indicator
        setDrawerIndicatorEnabled();
    }

    /**
     * Implementing classes may call this in {@link #onCreate(android.os.Bundle)} to setup a
     * progress bar which displays when syncing.
     */
    protected void setupSyncProgressBar(@IdRes int progressBarId) {
        syncProgressBar = findViewById(progressBarId);
        syncProgressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (syncProgressBar != null) {
            // watch for sync state changes
            syncStatusObserver.onStatusChanged(0);
            final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                    ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
            syncObserverHandle = ContentResolver.addStatusChangeListener(mask, syncStatusObserver);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // stop listening to sync state changes
        if (syncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(syncObserverHandle);
            syncObserverHandle = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // dismiss any snackbar to avoid it getting restored
        // if condition that led to its display is no longer true
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // use special animation when navigating away from a top activity
        // but not when exiting the app (use the default system animations)
        if (!isTaskRoot()) {
            overridePendingTransition(R.anim.activity_fade_enter_sg, R.anim.activity_fade_exit_sg);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // check if we should toggle the navigation drawer (app icon was touched)
        return toggleDrawer(item) || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onShowAutoBackupMissingFilesWarning() {
        if (snackbar != null && snackbar.isShown()) {
            Timber.d("NOT showing backup files warning: existing snackbar.");
            return;
        }

        Snackbar newSnackbar = Snackbar
                .make(getSnackbarParentView(),
                        R.string.autobackup_files_missing, Snackbar.LENGTH_LONG);
        setUpAutoBackupSnackbar(newSnackbar);
        newSnackbar.show();

        snackbar = newSnackbar;
    }

    @Override
    protected void onShowAutoBackupPermissionWarning() {
        if (snackbar != null && snackbar.isShown()) {
            Timber.d("NOT showing backup permission warning: existing snackbar.");
            return;
        }

        Snackbar newSnackbar = Snackbar
                .make(getSnackbarParentView(),
                        R.string.autobackup_permission_missing, Snackbar.LENGTH_INDEFINITE);
        setUpAutoBackupSnackbar(newSnackbar);
        newSnackbar.show();

        snackbar = newSnackbar;
    }

    private void setUpAutoBackupSnackbar(Snackbar snackbar) {
        TextView textView = snackbar.getView().findViewById(
                com.google.android.material.R.id.snackbar_text);
        textView.setMaxLines(5);
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                if (event == Snackbar.Callback.DISMISS_EVENT_SWIPE) {
                    // user has acknowledged warning
                    // disable auto backup so warning is not shown again
                    DataLiberationTools.setAutoBackupDisabled(BaseTopActivity.this);
                }
            }
        }).setAction(R.string.preferences,
                v -> startActivity(new Intent(BaseTopActivity.this, DataLiberationActivity.class)
                        .putExtra(DataLiberationActivity.InitBundle.EXTRA_SHOW_AUTOBACKUP, true))
        );
    }

    @Override
    protected void onShowCloudAccountWarning() {
        if (snackbar != null && snackbar.isShown()) {
            Timber.d("NOT showing Cloud account warning: existing snackbar.");
            return;
        }

        Snackbar newSnackbar = Snackbar
                .make(getSnackbarParentView(), R.string.hexagon_signed_out,
                        Snackbar.LENGTH_INDEFINITE);
        newSnackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                if (event == Snackbar.Callback.DISMISS_EVENT_SWIPE) {
                    // user has dismissed warning, so disable Cloud
                    HexagonTools hexagonTools = SgApp.getServicesComponent(BaseTopActivity.this)
                            .hexagonTools();
                    hexagonTools.setDisabled();
                }
            }
        }).setAction(R.string.hexagon_signin, v -> {
            // forward to cloud setup which can help fix the account issue
            startActivity(new Intent(BaseTopActivity.this, CloudSetupActivity.class));
        }).show();

        snackbar = newSnackbar;
    }

    /**
     * Shows or hides the indeterminate sync progress indicator inside this activity layout.
     */
    private void setSyncProgressVisibility(boolean isVisible) {
        if (syncProgressBar == null ||
                syncProgressBar.getVisibility() == (isVisible ? View.VISIBLE : View.GONE)) {
            // not enabled or already in desired state, avoid replaying animation
            return;
        }
        syncProgressBar.startAnimation(AnimationUtils.loadAnimation(syncProgressBar.getContext(),
                isVisible ? R.anim.fade_in : R.anim.fade_out));
        syncProgressBar.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    /**
     * Create a new anonymous SyncStatusObserver. It's attached to the app's ContentResolver in
     * onResume(), and removed in onPause(). If a sync is active or pending, a progress bar is
     * shown.
     */
    private SyncStatusObserver syncStatusObserver = new SyncStatusObserver() {
        /** Callback invoked with the sync adapter status changes. */
        @Override
        public void onStatusChanged(int which) {
            runOnUiThread(new Runnable() {
                /**
                 * The SyncAdapter runs on a background thread. To update the
                 * UI, onStatusChanged() runs on the UI thread.
                 */
                @Override
                public void run() {
                    Account account = AccountUtils.getAccount(BaseTopActivity.this);
                    if (account == null) {
                        // no account setup
                        setSyncProgressVisibility(false);
                        return;
                    }

                    // Test the ContentResolver to see if the sync adapter is active.
                    boolean syncActive = ContentResolver.isSyncActive(
                            account, SgApp.CONTENT_AUTHORITY);
                    setSyncProgressVisibility(syncActive);
                }
            });
        }
    };
}
