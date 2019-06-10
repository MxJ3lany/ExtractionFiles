/*
 * Copyright (c) 2017 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.douya.util;

import android.app.Activity;
import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.NightModeAccessor;

import java.util.HashMap;
import java.util.Map;

import me.zhanghai.android.douya.functional.extension.TriConsumer;
import me.zhanghai.android.douya.settings.info.Settings;

public class NightModeHelper {

    @NonNull
    private static final ActivityHelper sActivityHelper = new ActivityHelper();

    private NightModeHelper() {}

    public static void setup(@NonNull Application application) {
        syncDefaultNightMode();
        application.registerActivityLifecycleCallbacks(sActivityHelper);
    }

    private static void syncDefaultNightMode() {
        setDefaultNightMode(Settings.NIGHT_MODE.getEnumValue());
    }

    private static int getDefaultNightMode() {
        return AppCompatDelegate.getDefaultNightMode();
    }

    private static void setDefaultNightMode(@NonNull Settings.NightMode nightMode) {
        int nightModeValue = nightMode.getValue();
        if (getDefaultNightMode() == nightModeValue) {
            return;
        }
        AppCompatDelegate.setDefaultNightMode(nightModeValue);
    }

    public static void updateNightMode(@NonNull Activity activity) {
        syncDefaultNightMode();
        sActivityHelper.onActivityStarted(activity);
    }

    /**
     * Should be called before super.onConfigurationChanged() to avoid activity recreation by
     * AppCompat.
     *
     * @see androidx.appcompat.app.AppCompatDelegateImpl#updateForNightMode(int)
     */
    public static void onConfigurationChanged(
            @NonNull Activity activity,
            @NonNull TriConsumer<Resources.Theme, Integer, Boolean> onApplyThemeResource,
            @StyleRes int themeRes) {
        boolean isInNightMode = sActivityHelper.isActivityInNightMode(activity);
        int uiModeNight = isInNightMode ? Configuration.UI_MODE_NIGHT_YES
                : Configuration.UI_MODE_NIGHT_NO;
        Resources resources = activity.getResources();
        Configuration newConfiguration = new Configuration(resources.getConfiguration());
        newConfiguration.uiMode = (newConfiguration.uiMode & ~Configuration.UI_MODE_NIGHT_MASK)
                | uiModeNight;
        //noinspection deprecation
        resources.updateConfiguration(newConfiguration, resources.getDisplayMetrics());
        ResourcesFlusher.flush(resources);
        onApplyThemeResource.accept(activity.getTheme(), themeRes, true);
        sActivityHelper.onActivityCreated(activity, null);
    }

    /**
     * AppCompatDelegateImplV14.updateForNightMode() won't update when multiple Activities share a
     * Resources and a Configuration instance. We do this ourselves.
     */
    private static class ActivityHelper implements Application.ActivityLifecycleCallbacks {

        @NonNull
        private final Map<Activity, Boolean> mActivityIsInNightModeMap = new HashMap<>();

        @Override
        public void onActivityCreated(@NonNull Activity activity,
                                      @Nullable Bundle savedInstanceState) {
            // This runs after AppCompatActivity calls AppCompatDelegate.applyDayNight().
            int uiModeNight = activity.getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK;
            boolean isInNightMode = uiModeNight == Configuration.UI_MODE_NIGHT_YES;
            // Night mode normally won't change once an Activity is created.
            mActivityIsInNightModeMap.put(activity, isInNightMode);
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
            if (!(activity instanceof AppCompatActivity)) {
                return;
            }
            AppCompatActivity appCompatActivity = (AppCompatActivity) activity;
            // This runs before AppCompatDelegateImplV14.onStart() calls
            // AppCompatDelegate.applyDayNight().
            // And we don't care about things below V14 where this is a no-op returning false.
            if (appCompatActivity.getDelegate().applyDayNight()) {
                return;
            }
            boolean isInNightMode = mActivityIsInNightModeMap.get(activity);
            int nightMode = NightModeAccessor.mapNightMode(appCompatActivity.getDelegate(),
                    // We don't use local night mode.
                    getDefaultNightMode());
            if (nightMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
                // Let our future system handle it.
                return;
            }
            boolean shouldBeInNightMode = nightMode == AppCompatDelegate.MODE_NIGHT_YES;
            if (isInNightMode != shouldBeInNightMode) {
                // Need this for certain drawables to be updated correctly.
                ResourcesFlusher.flush(activity.getResources());
                activity.recreate();
            }
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {}

        @Override
        public void onActivityPaused(@NonNull Activity activity) {}

        @Override
        public void onActivityStopped(@NonNull Activity activity) {}

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity,
                                                @NonNull Bundle outState) {}

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            mActivityIsInNightModeMap.remove(activity);
        }

        public boolean isActivityInNightMode(@NonNull Activity activity) {
            return mActivityIsInNightModeMap.get(activity);
        }
    }
}
