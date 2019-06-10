/*
 * Copyright (C) 2017 The JackKnife Open Source Project
 *
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

package com.lwh.jackknife.util;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.SystemClock;

import java.util.List;

public class WifiAdmin {

    private WifiInfo mWifiInfo;
    private WifiLock mWifiLock;
    private WifiManager mWifiManager;
    private List<ScanResult> mScanResults;
    private List<WifiConfiguration> mWifiConfigurations;

    public enum WifiCipherType {
        WIFI_CIPHER_WEP,
        WIFI_CIPHER_WPA,
        WIFI_CIPHER_NO_PASS,
        WIFI_CIPHER_INVALID
    }

    public WifiAdmin(Context context) {
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWifiInfo = mWifiManager.getConnectionInfo();
    }

    public boolean openWifi() {
        return !mWifiManager.isWifiEnabled() && mWifiManager.setWifiEnabled(true);
    }

    public boolean closeWifi() {
        return !mWifiManager.isWifiEnabled() && mWifiManager.setWifiEnabled(false);
    }

    public WifiInfo getCurrentWifiInfo() {
        return mWifiManager.getConnectionInfo();
    }

    public int getWifiState() {
        return mWifiManager.getWifiState();
    }

    public void acquireWifiLock() {
        mWifiLock.acquire();
    }

    public void releaseWifiLock() {
        if (mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
    }

    public void createWifiLock(String tag) {
        mWifiLock = mWifiManager.createWifiLock(tag);
    }

    public void applyConfiguration(int index) {
        if (index > mWifiConfigurations.size()) {
            return;
        }
        mWifiManager.enableNetwork(mWifiConfigurations.get(index).networkId, true);
    }

    public void startScan() {
        mWifiManager.startScan();
        mScanResults = mWifiManager.getScanResults();
        mWifiConfigurations = mWifiManager.getConfiguredNetworks();
    }

    public List<ScanResult> getScanResults() {
        return mScanResults;
    }

    public List<WifiConfiguration> getConfigurations() {
        return mWifiConfigurations;
    }

    public String getMacAddress() {
        return mWifiInfo == null ? "" : mWifiInfo.getMacAddress();
    }

    public String getBSSID() {
        return mWifiInfo == null ? "" : mWifiInfo.getBSSID();
    }

    public int getIpAddress() {
        return mWifiInfo == null ? 0 : mWifiInfo.getIpAddress();
    }

    public String getIpAddressString() {
        return NetworkUtils.getHostAddress(getIpAddress());
    }

    public int getNetworkId() {
        return mWifiInfo == null ? 0 : mWifiInfo.getNetworkId();
    }

    private WifiConfiguration exists(String SSID) {
        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (configs != null && configs.size() > 0) {
            for (WifiConfiguration config : configs) {
                if (config.SSID.equals(SSID)) {
                    return config;
                }
            }
        }
        return null;
    }

    public WifiConfiguration createWifiInfo(ScanResult result, String password) {
        WifiConfiguration config = new WifiConfiguration();
        config.hiddenSSID = false;
        config.status = WifiConfiguration.Status.ENABLED;
        if (result.capabilities.contains("WEP")) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);
            config.SSID = "\"" + result.SSID + "\"";
            config.wepTxKeyIndex = 0;
            config.wepKeys[0] = password;
        } else if (result.capabilities.contains("PSK")) {
            config.SSID = "\"" + result.SSID + "\"";
            config.preSharedKey = "\"" + password + "\"";
        } else if (result.capabilities.contains("EAP")) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.SSID = "\"" + result.SSID + "\"";
            config.preSharedKey = "\"" + password + "\"";
        } else {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.SSID = "\"" + result.SSID + "\"";
            config.preSharedKey = null;
        }
        return config;
    }

    private WifiConfiguration createWifiInfo(String SSID, WifiCipherType type, String password) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        if (type == WifiCipherType.WIFI_CIPHER_NO_PASS) {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == WifiCipherType.WIFI_CIPHER_WEP) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == WifiCipherType.WIFI_CIPHER_WPA) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
        } else {
            return null;
        }
        return config;
    }

    public boolean connect(String SSID, WifiCipherType type, String password) {
        while (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
            SystemClock.sleep(100);
        }
        if (TextUtils.isEmpty(SSID) || password == null) {
            return false;
        }
        WifiConfiguration currentConfig = createWifiInfo(SSID, type, password);
        if (currentConfig == null) {
            return false;
        }
        WifiConfiguration tempConfig = exists(SSID);
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }
        int networkId = addNetwork(currentConfig);
        return networkId != -1 && mWifiManager.reconnect();
    }

    public int addNetwork(WifiConfiguration config) {
        if (config == null) {
            return -1;
        }
        int networkId = mWifiManager.addNetwork(config);
        if (mWifiManager.enableNetwork(networkId, true)) {
            boolean isOk = mWifiManager.saveConfiguration();
            if (isOk) {
                return networkId;
            }
        }
        return -1;
    }

    public boolean reconnect(ScanResult result, String password) {
        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        boolean enabling = false;
        boolean isOk = false;
        mWifiManager.disconnect();
        for (WifiConfiguration config : configs) {
            if (config.BSSID != null && config.BSSID.equals(result.BSSID)) {
                isOk = mWifiManager.enableNetwork(config.networkId, true);
                enabling = true;
                break;
            }
        }
        if (isOk && !enabling) {
            WifiConfiguration tempConfig = exists(result.BSSID);
            if (tempConfig == null) {
                mWifiManager.removeNetwork(tempConfig.networkId);
            }
            WifiConfiguration config = createWifiInfo(result, password);
            int networkId = addNetwork(config);
            isOk = mWifiManager.enableNetwork(networkId, true);
            if (isOk) {
                mWifiManager.reconnect();
            }
        }
        return isOk;
    }

    public void disconnect(int networkId) {
        mWifiManager.disableNetwork(networkId);
        mWifiManager.disconnect();
    }

    public boolean isConnected(ScanResult result) {
        if (result == null) {
            return false;
        }
        mWifiInfo = mWifiManager.getConnectionInfo();
        String suffixSSID = "\"" + result.SSID + "\"";
        return mWifiInfo.getSSID() != null && mWifiInfo.getSSID().endsWith(suffixSSID);
    }
}  