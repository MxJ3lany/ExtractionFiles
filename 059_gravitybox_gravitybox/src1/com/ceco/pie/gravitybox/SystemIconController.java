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

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.UserHandle;

import java.lang.reflect.Constructor;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class SystemIconController implements BroadcastSubReceiver {
    private static final String TAG = "GB:SystemIconController";
    private static final boolean DEBUG = false;

    private static final String CLASS_PHONE_STATUSBAR_POLICY =
            "com.android.systemui.statusbar.phone.PhoneStatusBarPolicy";
    private static final String CLASS_STATUS_BAR_ICON = "com.android.internal.statusbar.StatusBarIcon";
    private static final String CLASS_SB_ICON_CONTROLLER =
            "com.android.systemui.statusbar.phone.StatusBarIconControllerImpl";

    public static final String SLOT_BLUETOOTH = "bluetooth";
    public static final String SLOT_VOLUME = "volume";
    public static final String SLOT_DATA_SAVER = "data_saver";
    public static final String SLOT_ALARM_CLOCK = "alarm_clock";

    private enum BtMode { DEFAULT, CONNECTED, HIDDEN }

    private Object mSbPolicy;
    private Object mIconCtrl;
    private Object mPolicyManager;
    private Context mContext;
    private BtMode mBtMode;
    private boolean mHideVibrateIcon;
    private boolean mHideDataSaverIcon;
    private boolean mHideAlarmIcon;

    private static void log(String message) {
        XposedBridge.log(TAG + ": " + message);
    }

    public SystemIconController(ClassLoader classLoader, XSharedPreferences prefs) {
        mBtMode = BtMode.valueOf(prefs.getString(
                GravityBoxSettings.PREF_KEY_STATUSBAR_BT_VISIBILITY, "HIDDEN"));
        mHideVibrateIcon = prefs.getBoolean(
                GravityBoxSettings.PREF_KEY_STATUSBAR_HIDE_VIBRATE_ICON, false);
        mHideDataSaverIcon = prefs.getBoolean(
                GravityBoxSettings.PREF_KEY_STATUSBAR_HIDE_DATA_SAVER_ICON, false);
        mHideAlarmIcon = prefs.getBoolean(GravityBoxSettings.PREF_KEY_STATUSBAR_CLOCK_MASTER_SWITCH, true) &&
                prefs.getBoolean(GravityBoxSettings.PREF_KEY_ALARM_ICON_HIDE, false);

        createHooks(classLoader);
    }

    private void createHooks(final ClassLoader classLoader) {
        try {
            XposedBridge.hookAllConstructors(XposedHelpers.findClass(
                    CLASS_PHONE_STATUSBAR_POLICY, classLoader), new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    mSbPolicy = param.thisObject;
                    if (DEBUG) log ("Phone statusbar policy created");
                }
            });
        } catch (Throwable t) {
            GravityBox.log(TAG, t);
        }

        try {
            XposedBridge.hookAllConstructors(XposedHelpers.findClass(
                    CLASS_SB_ICON_CONTROLLER, classLoader), new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    mIconCtrl = param.thisObject;
                    mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                    Class<?> PolicyManagerClazz = XposedHelpers.findClass("android.net.NetworkPolicyManager", classLoader);
                    mPolicyManager = XposedHelpers.callStaticMethod(PolicyManagerClazz, "from", mContext);
                    if (DEBUG) log ("Phone statusbar icon controller created");
                }
            });
        } catch (Throwable t) {
            GravityBox.log(TAG, t);
        }

        try {
            XposedHelpers.findAndHookMethod(CLASS_SB_ICON_CONTROLLER, classLoader,
                    "setIconVisibility", String.class, boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    boolean origVisibility = (boolean)param.args[1];
                    boolean newVisibility = onSetIconVisibility((String)param.args[0], origVisibility);
                    if (DEBUG) log("setIconVisibility: slot=" + param.args[0] +
                            "; orig=" + origVisibility + "; new=" + newVisibility);
                    param.args[1] = newVisibility;
                }
            });
        } catch (Throwable t) {
            GravityBox.log(TAG, t);
        }
    }

    private boolean onSetIconVisibility(String slot, boolean visible) {
        switch (slot) {
            case SLOT_BLUETOOTH:
                return getBtIconVisibility(visible);
            case SLOT_VOLUME:
                return getVolumeIconVisibility(visible);
            case SLOT_DATA_SAVER:
                return (visible && !mHideDataSaverIcon);
            case SLOT_ALARM_CLOCK:
                return (visible && !mHideAlarmIcon);
            default:
                return visible;
        }
    }

    @SuppressLint("MissingPermission")
    private boolean getBtIconVisibility(boolean defaultVisibility) {
        boolean visible = defaultVisibility;
        if (mIconCtrl == null || mBtMode == null)
            return visible;

        switch (mBtMode) {
            case DEFAULT:
            case CONNECTED:
                try {
                    BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (btAdapter != null) {
                        boolean enabled = btAdapter.getState() == BluetoothAdapter.STATE_ON;
                        boolean connected = (Integer) XposedHelpers.callMethod(btAdapter,
                                "getConnectionState") ==  BluetoothAdapter.STATE_CONNECTED;
                        visible = (mBtMode == BtMode.DEFAULT && enabled) ||
                                (mBtMode == BtMode.CONNECTED && connected);
                    }
                } catch (Throwable t) { GravityBox.log(TAG, t); }
                break;
            case HIDDEN:
                visible = false;
                break;
        }

        return visible;
    }

    private boolean getVolumeIconVisibility(boolean defaultVisibility) {
        boolean visible = defaultVisibility;
        if (mIconCtrl == null || mContext == null || Utils.isOxygenOsRom())
            return visible;

        try {
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            if (am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
                visible &= !mHideVibrateIcon;
            }
        } catch (Throwable t) {
            GravityBox.log(TAG, t);
        }

        return visible;
    }

    public void setIcon(String slot, int iconId) {
        try {
            XposedHelpers.callMethod(mIconCtrl, "setIcon",
                    slot, createStatusBarIcon(iconId));
            if (DEBUG) log("setIcon: slot=" + slot + "; id=" + iconId);
        } catch (Throwable t) {
            GravityBox.log(TAG, t);
        }
    }

    public void removeIcon(String slot) {
        try {
            XposedHelpers.callMethod(mIconCtrl, "removeIcon", slot);
            if (DEBUG) log("removeIcon: slot=" + slot);
        } catch (Throwable t) {
            GravityBox.log(TAG, t);
        }
    }

    private Object createStatusBarIcon(int iconId) {
        try {
            Constructor<?> c = XposedHelpers.findConstructorExact(CLASS_STATUS_BAR_ICON,
                    mContext.getClassLoader(), String.class, UserHandle.class,
                    int.class, int.class, int.class, CharSequence.class);
            Object icon = c.newInstance(GravityBox.PACKAGE_NAME,
                    Utils.getUserHandle(Utils.getCurrentUser()),
                    iconId, 0, 0, (CharSequence)null);
            if (DEBUG) log("createStatusBarIcon: " + icon);
            return icon;
        } catch (Throwable t) {
            GravityBox.log(TAG, t);
            return null;
        }
    }

    public void setIconVisibility(String slot, boolean visible) {
        try {
            XposedHelpers.callMethod(mIconCtrl, "setIconVisibility",
                    slot, visible);
            if (DEBUG) log("setIconVisibility: slot=" + slot + "; visible=" + visible);
        } catch (Throwable t) {
            GravityBox.log(TAG, t);
        }
    }

    private void updateBluetooth() {
        if (mSbPolicy == null) return;
        try {
            XposedHelpers.callMethod(mSbPolicy, "updateBluetooth");
        } catch (Throwable t) {
            GravityBox.log(TAG, t);
        }
    }

    private void updateVolumeZen() {
        if (mSbPolicy == null) return;
        try {
            XposedHelpers.setBooleanField(mSbPolicy, "mVolumeVisible",
                    !XposedHelpers.getBooleanField(mSbPolicy, "mVolumeVisible"));
        } catch (Throwable ignore) {}
        try {
            XposedHelpers.callMethod(mSbPolicy, "updateVolumeZen");
        } catch (Throwable t) {
            GravityBox.log(TAG, t);
        }
    }

    private void updateDataSaver() {
        if (mIconCtrl == null || mPolicyManager == null) return;
        try {
            boolean isDataSaving = (boolean) XposedHelpers.callMethod(mPolicyManager, "getRestrictBackground");
            XposedHelpers.callMethod(mIconCtrl, "setIconVisibility", SLOT_DATA_SAVER, isDataSaving);
        } catch (Throwable t) {
            GravityBox.log(TAG, t);
        }
    }

    private void updateAlarm() {
        if (mSbPolicy == null) return;
        try {
            XposedHelpers.callMethod(mSbPolicy, "updateAlarm");
        } catch (Throwable t) {
            GravityBox.log(TAG, t);
        }
    }

    @Override
    public void onBroadcastReceived(Context context, Intent intent) {
        if (intent.getAction().equals(GravityBoxSettings.ACTION_PREF_SYSTEM_ICON_CHANGED)) {
            if (intent.hasExtra(GravityBoxSettings.EXTRA_SB_BT_VISIBILITY)) {
                try {
                    mBtMode = BtMode.valueOf(intent.getStringExtra(GravityBoxSettings.EXTRA_SB_BT_VISIBILITY));
                } catch (Throwable t) { 
                    GravityBox.log(TAG, "Invalid Mode value: ", t);
                }
                updateBluetooth();
            }
            if (intent.hasExtra(GravityBoxSettings.EXTRA_SB_HIDE_VIBRATE_ICON)) {
                mHideVibrateIcon = intent.getBooleanExtra(
                        GravityBoxSettings.EXTRA_SB_HIDE_VIBRATE_ICON, false);
                updateVolumeZen();
            }
            if (intent.hasExtra(GravityBoxSettings.EXTRA_SB_HIDE_DATA_SAVER_ICON)) {
                mHideDataSaverIcon = intent.getBooleanExtra(
                        GravityBoxSettings.EXTRA_SB_HIDE_DATA_SAVER_ICON, false);
                updateDataSaver();
            }
        } else if (intent.getAction().equals(GravityBoxSettings.ACTION_PREF_CLOCK_CHANGED)) {
            if (intent.hasExtra(GravityBoxSettings.EXTRA_ALARM_HIDE)) {
                mHideAlarmIcon = intent.getBooleanExtra(GravityBoxSettings.EXTRA_ALARM_HIDE, false);
                updateAlarm();
            }
        }
    }
}
