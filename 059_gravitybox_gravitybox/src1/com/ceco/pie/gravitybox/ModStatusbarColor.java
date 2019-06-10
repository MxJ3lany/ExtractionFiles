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

import com.ceco.pie.gravitybox.managers.SysUiManagers;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class ModStatusbarColor {
    private static final String TAG = "GB:ModStatusbarColor";
    public static final String PACKAGE_NAME = "com.android.systemui";
    private static final String CLASS_SB_TRANSITIONS = "com.android.systemui.statusbar.phone.PhoneStatusBarTransitions";
    private static final String CLASS_SB_DARK_ICON_DISPATCHER = "com.android.systemui.statusbar.phone.DarkIconDispatcherImpl";
    private static final String CLASS_HEADSUP_APPEARANCE_CTRL = "com.android.systemui.statusbar.phone.HeadsUpAppearanceController";
    private static final boolean DEBUG = false;

    private static void log(String message) {
        XposedBridge.log(TAG + ": " + message);
    }

    // in process hooks
    public static void init(final ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod(CLASS_SB_TRANSITIONS, classLoader,
                    "applyMode", int.class, boolean.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (SysUiManagers.IconManager != null) {
                        final float signalClusterAlpha = (Float) XposedHelpers.callMethod(
                                param.thisObject, "getNonBatteryClockAlphaFor", (Integer) param.args[0]);
                        final float textAndBatteryAlpha = (Float) XposedHelpers.callMethod(
                                param.thisObject, "getBatteryClockAlpha", (Integer) param.args[0]);
                        SysUiManagers.IconManager.setIconAlpha(signalClusterAlpha, textAndBatteryAlpha);
                    }
                }
            });
        } catch (Throwable t) {
            GravityBox.log(TAG, "Error hooking applyMode:", t);
        }

        try {
            XposedBridge.hookAllMethods(XposedHelpers.findClass(CLASS_SB_DARK_ICON_DISPATCHER, classLoader),
                    "setIconTintInternal", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (SysUiManagers.IconManager != null) {
                        SysUiManagers.IconManager.setIconTint(
                                XposedHelpers.getIntField(param.thisObject, "mIconTint"));
                    }
                }
            });
        } catch (Throwable t) {
            GravityBox.log(TAG, "Error hooking setIconTintInternal:", t);
        }

        try {
            XposedHelpers.findAndHookMethod(CLASS_HEADSUP_APPEARANCE_CTRL, classLoader,
                    "setShown", boolean.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (SysUiManagers.IconManager != null) {
                        SysUiManagers.IconManager.setHeadsUpVisible(
                                (boolean)param.args[0]);
                    }
                }
            });
        } catch (Throwable t) {
            GravityBox.log(TAG, "Error hooking setShown:", t);
        }
    }
}
