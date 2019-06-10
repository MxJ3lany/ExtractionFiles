/*
 * Copyright (C) 2019 Peter Gregus for GravityBox Project (C3C076@xda)
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
package com.ceco.pie.gravitybox;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class ModExpandedDesktop {
    private static final String TAG = "GB:ModExpandedDesktop";
    private static final String CLASS_PHONE_WINDOW_MANAGER = "com.android.server.policy.PhoneWindowManager";
    private static final String CLASS_WINDOW_MANAGER_FUNCS = "com.android.server.policy.WindowManagerPolicy.WindowManagerFuncs";
    private static final String CLASS_IWINDOW_MANAGER = "android.view.IWindowManager";
    private static final String CLASS_POLICY_WINDOW_STATE = "com.android.server.policy.WindowManagerPolicy$WindowState";
    private static final String CLASS_POLICY_CONTROL = "com.android.server.policy.PolicyControl";

    private static final boolean DEBUG = false;

    public static final String SETTING_EXPANDED_DESKTOP_STATE = "gravitybox_expanded_desktop_state";

    private static class ViewConst {
        static final int STATUS_BAR_TRANSLUCENT = 0x40000000;
        static final int NAVIGATION_BAR_TRANSLUCENT = 0x80000000;
    }

    private static class NavbarDimensions {
        int wPort, hPort, hLand;
        NavbarDimensions(int wp, int hp, int hl) {
            wPort = wp;
            hPort = hp;
            hLand = hl;
        }
    }

    private static Context mContext;
    private static Object mPhoneWindowManager;
    private static SettingsObserver mSettingsObserver;
    private static boolean mExpandedDesktop;
    private static int mExpandedDesktopMode;
    private static boolean mNavbarOverride;
    private static float mNavbarHeightScaleFactor = 1;
    private static float mNavbarHeightLandscapeScaleFactor = 1;
    private static float mNavbarWidthScaleFactor = 1;
    private static NavbarDimensions mNavbarDimensions;

    private static void log(String message) {
        XposedBridge.log(TAG + ": " + message);
    }

    static class SettingsObserver extends ContentObserver {

        public SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            final ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.Global.getUriFor(
                    SETTING_EXPANDED_DESKTOP_STATE), false, this);
            updateSettings();
        }

        @Override 
        public void onChange(boolean selfChange) {
            updateSettings();
        }
    }

    private static BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) log("Broadcast received: " + intent.toString());
            if (intent.getAction().equals(GravityBoxSettings.ACTION_PREF_EXPANDED_DESKTOP_MODE_CHANGED)
                    && intent.hasExtra(GravityBoxSettings.EXTRA_ED_MODE)) {
                mExpandedDesktopMode = intent.getIntExtra(
                        GravityBoxSettings.EXTRA_ED_MODE, GravityBoxSettings.ED_DISABLED);
                updateSettings();
            } else if (intent.getAction().equals(ModStatusBar.ACTION_PHONE_STATUSBAR_VIEW_MADE)) {
                updateSettings();
            } else if (intent.getAction().equals(GravityBoxSettings.ACTION_PREF_NAVBAR_CHANGED)) {
                if (intent.hasExtra(GravityBoxSettings.EXTRA_NAVBAR_HEIGHT)) {
                    mNavbarHeightScaleFactor = 
                            (float)intent.getIntExtra(GravityBoxSettings.EXTRA_NAVBAR_HEIGHT, 100) / 100f;
                    updateNavbarDimensions(true);
                }
                if (intent.hasExtra(GravityBoxSettings.EXTRA_NAVBAR_HEIGHT_LANDSCAPE)) {
                    mNavbarHeightLandscapeScaleFactor = (float)intent.getIntExtra(
                                    GravityBoxSettings.EXTRA_NAVBAR_HEIGHT_LANDSCAPE,  100) / 100f;
                    updateNavbarDimensions(true);
                }
                if (intent.hasExtra(GravityBoxSettings.EXTRA_NAVBAR_WIDTH)) {
                    mNavbarWidthScaleFactor = 
                            (float)intent.getIntExtra(GravityBoxSettings.EXTRA_NAVBAR_WIDTH, 100) / 100f;
                    updateNavbarDimensions(true);
                }
            }
        }
    };

    private static void updateSettings() {
        if (mContext == null || mPhoneWindowManager == null) return;

        try {
            final boolean expandedDesktop = Settings.Global.getInt(mContext.getContentResolver(), 
                    SETTING_EXPANDED_DESKTOP_STATE, 0) == 1;
            if (mExpandedDesktopMode == GravityBoxSettings.ED_DISABLED && expandedDesktop) {
                    Settings.Global.putInt(mContext.getContentResolver(),
                            SETTING_EXPANDED_DESKTOP_STATE, 0);
                    return;
            }

            if (mExpandedDesktop != expandedDesktop) {
                mExpandedDesktop = expandedDesktop;
            }

            XposedHelpers.callMethod(mPhoneWindowManager, "updateSettings");

            int[] navigationBarWidthForRotation = (int[]) XposedHelpers.getObjectField(
                    mPhoneWindowManager, "mNavigationBarWidthForRotationDefault");
            int[] navigationBarHeightForRotation = (int[]) XposedHelpers.getObjectField(
                    mPhoneWindowManager, "mNavigationBarHeightForRotationDefault");
            final int portraitRotation = XposedHelpers.getIntField(mPhoneWindowManager, "mPortraitRotation");
            final int upsideDownRotation = XposedHelpers.getIntField(mPhoneWindowManager, "mUpsideDownRotation");
            final int landscapeRotation = XposedHelpers.getIntField(mPhoneWindowManager, "mLandscapeRotation");
            final int seascapeRotation = XposedHelpers.getIntField(mPhoneWindowManager, "mSeascapeRotation");

            if (isNavbarHidden()) {
                navigationBarWidthForRotation[portraitRotation]
                        = navigationBarWidthForRotation[upsideDownRotation]
                        = navigationBarWidthForRotation[landscapeRotation]
                        = navigationBarWidthForRotation[seascapeRotation]
                        = navigationBarHeightForRotation[portraitRotation]
                        = navigationBarHeightForRotation[upsideDownRotation]
                        = navigationBarHeightForRotation[landscapeRotation]
                        = navigationBarHeightForRotation[seascapeRotation] = 0;
            } else if (mNavbarDimensions != null) {
                navigationBarHeightForRotation[portraitRotation] =
                navigationBarHeightForRotation[upsideDownRotation] =
                        mNavbarDimensions.hPort;
                navigationBarHeightForRotation[landscapeRotation] =
                navigationBarHeightForRotation[seascapeRotation] =
                        mNavbarDimensions.hLand;

                navigationBarWidthForRotation[portraitRotation] =
                navigationBarWidthForRotation[upsideDownRotation] =
                navigationBarWidthForRotation[landscapeRotation] =
                navigationBarWidthForRotation[seascapeRotation] =
                        mNavbarDimensions.wPort;
            }

            XposedHelpers.callMethod(mPhoneWindowManager, "updateRotation", false);
        } catch (Throwable t) {
            GravityBox.log(TAG, t);
        }
    }

    private static void updateNavbarDimensions(boolean updateSettings) {
        if (mContext == null) return;
        try {
            Resources res = mContext.getResources();
            int resWidthId = res.getIdentifier(
                    "navigation_bar_width", "dimen", "android");
            int resHeightId = res.getIdentifier(
                    "navigation_bar_height", "dimen", "android");
            int resHeightLandscapeId = res.getIdentifier(
                    "navigation_bar_height_landscape", "dimen", "android");
            mNavbarDimensions = new NavbarDimensions(
                    (int) (res.getDimensionPixelSize(resWidthId) * mNavbarWidthScaleFactor),
                    (int) (res.getDimensionPixelSize(resHeightId) * mNavbarHeightScaleFactor),
                    (int) (res.getDimensionPixelSize(resHeightLandscapeId) *mNavbarHeightLandscapeScaleFactor));
            if (updateSettings) {
                updateSettings();
            }
        } catch (Throwable t) {
            GravityBox.log(TAG, t);
        }
    }

    public static void initAndroid(final XSharedPreferences prefs, final ClassLoader classLoader) {
        try {
            final Class<?> classPhoneWindowManager = XposedHelpers.findClass(CLASS_PHONE_WINDOW_MANAGER, classLoader);

            mNavbarOverride = prefs.getBoolean(GravityBoxSettings.PREF_KEY_NAVBAR_OVERRIDE, false);
            if (mNavbarOverride) {
                mNavbarHeightScaleFactor = 
                        (float) prefs.getInt(GravityBoxSettings.PREF_KEY_NAVBAR_HEIGHT, 100) / 100f;
                mNavbarHeightLandscapeScaleFactor = 
                        (float) prefs.getInt(GravityBoxSettings.PREF_KEY_NAVBAR_HEIGHT_LANDSCAPE, 100) / 100f;
                mNavbarWidthScaleFactor = 
                        (float) prefs.getInt(GravityBoxSettings.PREF_KEY_NAVBAR_WIDTH, 100) / 100f;
            }

            mExpandedDesktopMode = GravityBoxSettings.ED_DISABLED;
            try {
                mExpandedDesktopMode = Integer.valueOf(prefs.getString(
                        GravityBoxSettings.PREF_KEY_EXPANDED_DESKTOP, "0"));
            } catch (NumberFormatException nfe) {
                GravityBox.log(TAG, "Invalid value for PREF_KEY_EXPANDED_DESKTOP preference");
            }

            XposedHelpers.findAndHookMethod(classPhoneWindowManager, "init",
                Context.class, CLASS_IWINDOW_MANAGER, CLASS_WINDOW_MANAGER_FUNCS, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                        mPhoneWindowManager = param.thisObject;

                        IntentFilter intentFilter = new IntentFilter();
                        intentFilter.addAction(GravityBoxSettings.ACTION_PREF_EXPANDED_DESKTOP_MODE_CHANGED);
                        intentFilter.addAction(ModStatusBar.ACTION_PHONE_STATUSBAR_VIEW_MADE);
                        if (mNavbarOverride) {
                            intentFilter.addAction(GravityBoxSettings.ACTION_PREF_NAVBAR_CHANGED);
                        }
                        mContext.registerReceiver(mBroadcastReceiver, intentFilter);

                        mSettingsObserver = new SettingsObserver(
                                (Handler) XposedHelpers.getObjectField(param.thisObject, "mHandler"));
                        mSettingsObserver.observe();

                        if (DEBUG) log("Phone window manager initialized");
                    } catch (Throwable t) {
                        GravityBox.log(TAG, t);
                    }
                }
            });

            XposedBridge.hookAllMethods(classPhoneWindowManager, "onConfigurationChanged", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    updateNavbarDimensions(mNavbarOverride || isNavbarHidden());
                }
            });

            XposedHelpers.findAndHookMethod(CLASS_POLICY_CONTROL, classLoader,"getSystemUiVisibility",
                    CLASS_POLICY_WINDOW_STATE, WindowManager.LayoutParams.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    int vis = (int) param.getResult();
                    if (isStatusbarImmersive()) {
                        vis |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                                View.SYSTEM_UI_FLAG_FULLSCREEN |
                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                        vis &= ~(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                                 ViewConst.STATUS_BAR_TRANSLUCENT);
                    }
                    if (isNavbarImmersive() || isNavbarHidden()) {
                        vis |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                        vis &= ~(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                                 ViewConst.NAVIGATION_BAR_TRANSLUCENT);
                    }
                    param.setResult(vis);
                }
            });

            XposedHelpers.findAndHookMethod(CLASS_POLICY_CONTROL, classLoader, "getWindowFlags",
                    CLASS_POLICY_WINDOW_STATE, WindowManager.LayoutParams.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    int flags = (int) param.getResult();
                    if (isStatusbarImmersive()) {
                        flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                        flags &= ~(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN |
                                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    }
                    if (isNavbarImmersive() || isNavbarHidden()) {
                        flags &= ~WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
                    }
                    param.setResult(flags);
                }
            });

            XposedHelpers.findAndHookMethod(CLASS_POLICY_CONTROL, classLoader, "adjustClearableFlags",
                    CLASS_POLICY_WINDOW_STATE, int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    int clearableFlags = (int) param.getResult();
                    if (isStatusbarImmersive()) {
                        clearableFlags &= ~View.SYSTEM_UI_FLAG_FULLSCREEN;
                    }
                    param.setResult(clearableFlags);
                }
            });

            XposedHelpers.findAndHookMethod(classPhoneWindowManager, "requestTransientBars",
                    CLASS_POLICY_WINDOW_STATE, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (param.args[0] == XposedHelpers.getObjectField(param.thisObject, "mNavigationBar")
                            && isNavbarHidden()) {
                        if (DEBUG) log("requestTransientBars: ignoring since navbar is hidden");
                        param.setResult(null);
                    }
                }
            });
        } catch (Throwable t) {
            GravityBox.log(TAG, t);
        }
    }

    private static boolean isStatusbarImmersive() {
        return (mExpandedDesktop
                && (mExpandedDesktopMode == GravityBoxSettings.ED_SEMI_IMMERSIVE ||
                    mExpandedDesktopMode == GravityBoxSettings.ED_IMMERSIVE_STATUSBAR ||
                    mExpandedDesktopMode == GravityBoxSettings.ED_IMMERSIVE));
    }

    private static boolean isNavbarImmersive() {
        return (mExpandedDesktop
                && (mExpandedDesktopMode == GravityBoxSettings.ED_IMMERSIVE ||
                mExpandedDesktopMode == GravityBoxSettings.ED_IMMERSIVE_NAVBAR));
    }

    private static boolean isNavbarHidden() {
        return (mExpandedDesktop && 
                    (mExpandedDesktopMode == GravityBoxSettings.ED_HIDE_NAVBAR ||
                            mExpandedDesktopMode == GravityBoxSettings.ED_SEMI_IMMERSIVE));
    }
}
