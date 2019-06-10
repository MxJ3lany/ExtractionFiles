package org.fdroid.fdroid.views.installed;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import org.fdroid.fdroid.AppUpdateStatusManager;
import org.fdroid.fdroid.R;
import org.fdroid.fdroid.data.App;
import org.fdroid.fdroid.data.AppPrefs;
import org.fdroid.fdroid.views.apps.AppListItemController;
import org.fdroid.fdroid.views.apps.AppListItemState;

/**
 * Shows the currently installed version name, and whether or not it is the recommended version.
 * Also shows whether the user has previously asked to ignore updates for this app entirely, or for
 * a specific version of this app.
 */
public class InstalledAppListItemController extends AppListItemController {
    public InstalledAppListItemController(Activity activity, View itemView) {
        super(activity, itemView);
    }

    @NonNull
    @Override
    protected AppListItemState getCurrentViewState(
            @NonNull App app, @Nullable AppUpdateStatusManager.AppUpdateStatus appStatus) {
        return new AppListItemState(app)
                .setStatusText(getInstalledVersion(app))
                .setSecondaryStatusText(getIgnoreStatus(app));
    }

    /**
     * Either "Version X" or "Version Y (Recommended)", depending on the installed version.
     */
    private CharSequence getInstalledVersion(@NonNull App app) {
        int statusStringRes = (app.suggestedVersionCode == app.installedVersionCode)
                ? R.string.app_recommended_version_installed
                : R.string.app_version_x_installed;

        return activity.getString(statusStringRes, app.installedVersionName);
    }

    /**
     * Show whether the user has ignored a specific version ("Updates ignored for Version X"), or
     * all versions ("Updates ignored").
     */
    @Nullable
    private CharSequence getIgnoreStatus(@NonNull App app) {
        AppPrefs prefs = app.getPrefs(activity);
        if (prefs.ignoreAllUpdates) {
            return activity.getString(R.string.installed_app__updates_ignored);
        } else if (prefs.ignoreThisUpdate > 0 && prefs.ignoreThisUpdate == app.suggestedVersionCode) {
            return activity.getString(
                    R.string.installed_app__updates_ignored_for_suggested_version,
                    app.getSuggestedVersionName());
        }

        return null;
    }
}
