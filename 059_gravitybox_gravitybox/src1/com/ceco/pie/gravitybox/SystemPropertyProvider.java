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
import com.ceco.pie.gravitybox.managers.TunerManager;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.provider.Settings;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class SystemPropertyProvider {
    private static final String TAG = "GB:SystemPropertyProvider";
    public static final String PACKAGE_NAME = "com.android.systemui";
    private static final boolean DEBUG = false;

    public static final String ACTION_GET_SYSTEM_PROPERTIES = 
            "gravitybox.intent.action.ACTION_GET_SYSTEM_PROPERTIES";
    public static final int RESULT_SYSTEM_PROPERTIES = 1025;
    public static final String ACTION_REGISTER_UUID = 
            "gravitybox.intent.action.ACTION_REGISTER_UUID";
    public static final String EXTRA_UUID = "uuid";
    public static final String EXTRA_UUID_TYPE = "uuidType";
    private static final String SETTING_GRAVITYBOX_UUID = "gravitybox_uuid";
    private static final String SETTING_GRAVITYBOX_UUID_TYPE = "gravitybox_uuid_type";
    public static final String SETTING_UNC_TRIAL_COUNTDOWN = "gravitybox_unc_trial_countdown_v2";

    private static String mSettingsUuid;

    private static void log(String message) {
        XposedBridge.log(TAG + ": " + message);
    }

    // Resources
    public static boolean getSystemConfigBool(Resources res, String name) {
        final int resId = res.getIdentifier(name, "bool", "android");
        return (resId != 0 && res.getBoolean(resId));
    }

    public static int getSystemConfigInteger(Resources res, String name) {
        final int resId = res.getIdentifier(name, "integer", "android");
        return (resId == 0 ? -1 : res.getInteger(resId));
    }

    // System properties
    /**
     * Get the value for the given key
     * @param key key to lookup
     * @return null if the key isn't found
     */
    public static String get(String key) {
        String ret = null;
        try {
            Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
            ret = (String) callStaticMethod(classSystemProperties, "get", key);
        } catch (Throwable t) {
            GravityBox.log(TAG, "SystemProp.get failed:", t);
        }
        return ret;
    }

    /**
     * Get the value for the given key
     * @param key: key to lookup
     * @param def: default value to return
     * @return if the key isn't found, return def if it isn't null, or an empty string otherwise
     */
    public static String get(String key, String def) {
        String ret = def;
        try {
            Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
            ret = (String) callStaticMethod(classSystemProperties, "get", key, def);
        } catch (Throwable t) {
            GravityBox.log(TAG, "SystemProp.get failed: ", t);
        }
        return ret;
    }

    /**
     * Get the value for the given key, and return as an integer
     * @param key: key to lookup
     * @param def: default value to return
     * @return the key parsed as an integer, or def if the key isn't found or cannot be parsed
     */
    public static int getInt(String key, int def) {
        int ret = def;
        try {
            Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
            ret = (int) callStaticMethod(classSystemProperties, "getInt", key, def);
        } catch (Throwable t) {
            GravityBox.log(TAG, "SystemProp.getInt failed: ", t);
        }
        return ret;
    }

    /**
     * Get the value for the given key, and return as a long
     * @param key: key to lookup
     * @param def: default value to return
     * @return the key parsed as a long, or def if the key isn't found or cannot be parsed
     */
    public static long getLong(String key, long def) {
        long ret = def;
        try {
            Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
            ret = (long) callStaticMethod(classSystemProperties, "getLong", key, def);
        } catch (Throwable t) {
            GravityBox.log(TAG, "SystemProp.getLong failed: ", t);
        }
        return ret;
    }

    /**
     * Get the value (case insensitive) for the given key, returned as a boolean<br>
     *     Values 'n', 'no', '0', 'false' or 'off' are considered false<br>
     *     Values 'y', 'yes', '1', 'true' or 'on' are considered true<br>
     *     If the key does not exist, or has any other value, then the default result is returned
     * @param key: key to lookup
     * @param def: default value to return
     * @return the key parsed as a boolean, or def if the key isn't found or cannot be parsed
     */
    public static boolean getBoolean(String key, boolean def) {
        boolean ret = def;
        try {
            Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
            ret = (boolean) callStaticMethod(classSystemProperties, "getBoolean", key, def);
        } catch (Throwable t) {
            GravityBox.log(TAG, "SystemProp.getBoolean failed: ", t);
        }
        return ret;
    }

    /**
     * Set the value for the given key
     */
    public static void set(String key, String val) {
        try{
            Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
            callStaticMethod(classSystemProperties, "set", key, val);
        } catch (Throwable t) {
            GravityBox.log(TAG, "SystemProp.set failed: ", t);
        }
    }

    // SystemUI service hook that provides system properties to GravityBoxSettings
    public static void init(final XSharedPreferences prefs, final XSharedPreferences qhPrefs,
                            final XSharedPreferences tunerPrefs, final ClassLoader classLoader) {
        try {
            final Class<?> classSystemUIService = XposedHelpers.findClass(
                    "com.android.systemui.SystemUIService", classLoader);
            XposedHelpers.findAndHookMethod(classSystemUIService, "onCreate", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) {
                    Context context = (Context) param.thisObject;
                    try {
                        if (DEBUG) log("Initializing SystemUI managers");
                        SysUiManagers.init(context, prefs, qhPrefs, tunerPrefs);
                    } catch(Throwable t) {
                        GravityBox.log(TAG, "Error initializing SystemUI managers: ", t);
                    }
                }
                @Override
                protected void afterHookedMethod(final MethodHookParam param) {
                    Context context = (Context) param.thisObject;
                    if (context != null) {
                        if (DEBUG) log("SystemUIService created. Registering BroadcastReceiver");
                        final ContentResolver cr = context.getContentResolver();

                        IntentFilter intentFilter = new IntentFilter();
                        intentFilter.addAction(ACTION_GET_SYSTEM_PROPERTIES);
                        intentFilter.addAction(ACTION_REGISTER_UUID);
                        context.registerReceiver(new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                if (DEBUG) log("Broadcast received: " + intent.toString());
                                if (intent.getAction().equals(ACTION_GET_SYSTEM_PROPERTIES)
                                        && intent.hasExtra("receiver")) {
                                    mSettingsUuid = intent.getStringExtra("settings_uuid");
                                    final Resources res = context.getResources();
                                    ResultReceiver receiver = intent.getParcelableExtra("receiver");
                                    Bundle data = new Bundle();
                                    data.putBoolean("hasGeminiSupport", Utils.hasGeminiSupport());
                                    data.putBoolean("isTablet", Utils.isTablet());
                                    data.putBoolean("hasNavigationBar",
                                            getSystemConfigBool(res, "config_showNavigationBar"));
                                    data.putBoolean("unplugTurnsOnScreen", 
                                            getSystemConfigBool(res, "config_unplugTurnsOnScreen"));
                                    data.putInt("defaultNotificationLedOff",
                                            getSystemConfigInteger(res, "config_defaultNotificationLedOff"));
                                    data.putBoolean("uuidRegistered", (mSettingsUuid != null &&
                                            mSettingsUuid.equals(Settings.System.getString(
                                                    cr, SETTING_GRAVITYBOX_UUID))));
                                    data.putString("uuidType", Settings.System.getString(
                                            cr, SETTING_GRAVITYBOX_UUID_TYPE));
                                    data.putInt("uncTrialCountdown", Settings.System.getInt(cr,
                                            SETTING_UNC_TRIAL_COUNTDOWN, 100));
                                    data.putInt("tunerTrialCountdown", Settings.System.getInt(cr,
                                            TunerManager.SETTING_TUNER_TRIAL_COUNTDOWN, 100));
                                    data.putBoolean("hasMsimSupport", PhoneWrapper.hasMsimSupport());
                                    data.putBoolean("supportsFingerprint", Utils.supportsFingerprint(context));
                                    if (SysUiManagers.FingerprintLauncher != null) {
                                        data.putIntArray("fingerprintIds",
                                                SysUiManagers.FingerprintLauncher.getEnrolledFingerprintIds());
                                    }
                                    data.putBoolean("isOxygenOsRom", Utils.isOxygenOsRom());
                                    if (DEBUG) {
                                        log("hasGeminiSupport: " + data.getBoolean("hasGeminiSupport"));
                                        log("isTablet: " + data.getBoolean("isTablet"));
                                        log("hasNavigationBar: " + data.getBoolean("hasNavigationBar"));
                                        log("unplugTurnsOnScreen: " + data.getBoolean("unplugTurnsOnScreen"));
                                        log("defaultNotificationLedOff: " + data.getInt("defaultNotificationLedOff"));
                                        log("uuidRegistered: " + data.getBoolean("uuidRegistered"));
                                        log("uuidType: " + data.getString("uuidType"));
                                        log("uncTrialCountdown: " + data.getInt("uncTrialCountdown"));
                                        log("tunerTrialCountdown: " + data.getInt("tunerTrialCountdown"));
                                        log("hasMsimSupport: " + data.getBoolean("hasMsimSupport"));
                                        log("xposedBridgeVersion: " + data.getInt("xposedBridgeVersion"));
                                        log("supportsFingerprint: " + data.getBoolean("supportsFingerprint"));
                                        if (data.containsKey("fingerprintIds")) {
                                            log("fingerprintIds: " + data.getIntArray("fingerprintIds"));
                                        }
                                        log("isOxygenOsRom: " + data.getBoolean("isOxygenOsRom"));
                                    }
                                    receiver.send(RESULT_SYSTEM_PROPERTIES, data);
                                } else if (intent.getAction().equals(ACTION_REGISTER_UUID) && 
                                            intent.hasExtra(EXTRA_UUID) && 
                                            intent.getStringExtra(EXTRA_UUID).equals(mSettingsUuid) &&
                                            intent.hasExtra(EXTRA_UUID_TYPE)) {
                                    Settings.System.putString(cr, SETTING_GRAVITYBOX_UUID, mSettingsUuid);
                                    Settings.System.putString(cr, SETTING_GRAVITYBOX_UUID_TYPE,
                                            intent.getStringExtra(EXTRA_UUID_TYPE));
                                }
                            }
                        }, intentFilter);
                    }
                }
            });
        } catch (Throwable t) {
            GravityBox.log(TAG, t);
        }
    }
}
