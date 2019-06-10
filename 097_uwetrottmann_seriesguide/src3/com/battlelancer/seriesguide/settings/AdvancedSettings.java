
package com.battlelancer.seriesguide.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Access advanced settings for auto backup and auto update.
 */
public class AdvancedSettings {

    public static final String KEY_AUTOBACKUP = "com.battlelancer.seriesguide.autobackup";

    public static final String KEY_LASTBACKUP = "com.battlelancer.seriesguide.lastbackup";

    private static final String KEY_LAST_SUPPORTER_STATE
            = "com.battlelancer.seriesguide.lastupgradestate";

    public static final String KEY_UPCOMING_LIMIT = "com.battlelancer.seriesguide.upcominglimit";

    public static boolean isAutoBackupEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_AUTOBACKUP,
                true);
    }

    public static long getLastAutoBackupTime(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        long time = prefs.getLong(KEY_LASTBACKUP, 0);
        if (time == 0) {
            // use now as default value, so a re-install won't overwrite the old
            // auto-backup right away
            time = System.currentTimeMillis();
            prefs.edit().putLong(KEY_LASTBACKUP, time).apply();
        }

        return time;
    }

    /**
     * Returns if the user was a supporter through an in-app purchase the last time we checked.
     */
    public static boolean getLastSupporterState(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                KEY_LAST_SUPPORTER_STATE, false);
    }

    /**
     * Set if the user currently has an active purchase to support the app.
     */
    public static void setSupporterState(Context context, boolean isSubscribedToX) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(
                KEY_LAST_SUPPORTER_STATE, isSubscribedToX).apply();
    }

    /**
     * Returns the maximum number of days from today on an episode can air for its show to be
     * considered as upcoming.
     */
    public static int getUpcomingLimitInDays(Context context) {
        int upcomingLimit = 3;
        try {
            upcomingLimit = Integer.parseInt(PreferenceManager
                    .getDefaultSharedPreferences(context).getString(
                            KEY_UPCOMING_LIMIT, "3"));
        } catch (NumberFormatException ignored) {
        }

        return upcomingLimit;
    }
}
