/*
 * Copyright 2015 OpenMarket Ltd
 * Copyright 2017 Vector Creations Ltd
 * Copyright 2018 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.matrix.androidsdk.crypto.algorithms.olm;

import android.text.TextUtils;

import com.google.gson.JsonElement;

import org.matrix.androidsdk.core.JsonUtility;
import org.matrix.androidsdk.core.callback.ApiCallback;
import org.matrix.androidsdk.core.callback.SimpleApiCallback;
import org.matrix.androidsdk.crypto.algorithms.IMXEncrypting;
import org.matrix.androidsdk.crypto.data.MXDeviceInfo;
import org.matrix.androidsdk.crypto.data.MXOlmSessionResult;
import org.matrix.androidsdk.crypto.data.MXUsersDevicesMap;
import org.matrix.androidsdk.crypto.interfaces.CryptoSession;
import org.matrix.androidsdk.crypto.internal.MXCryptoImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MXOlmEncryption implements IMXEncrypting {
    private CryptoSession mSession;
    private MXCryptoImpl mCrypto;
    private String mRoomId;

    @Override
    public void initWithMatrixSession(CryptoSession matrixSession, MXCryptoImpl crypto, String roomId) {
        mSession = matrixSession;
        mCrypto = crypto;
        mRoomId = roomId;
    }

    /**
     * @return the stored device keys for a user.
     */
    private List<MXDeviceInfo> getUserDevices(final String userId) {
        Map<String, MXDeviceInfo> map = mCrypto.getCryptoStore().getUserDevices(userId);
        return (null != map) ? new ArrayList<>(map.values()) : new ArrayList<MXDeviceInfo>();
    }

    @Override
    public void encryptEventContent(final JsonElement eventContent,
                                    final String eventType,
                                    final List<String> userIds,
                                    final ApiCallback<JsonElement> callback) {
        // pick the list of recipients based on the membership list.
        //
        // TODO: there is a race condition here! What if a new user turns up
        ensureSession(userIds, new SimpleApiCallback<Void>(callback) {
                    @Override
                    public void onSuccess(Void info) {
                        List<MXDeviceInfo> deviceInfos = new ArrayList<>();

                        for (String userId : userIds) {
                            List<MXDeviceInfo> devices = getUserDevices(userId);

                            if (null != devices) {
                                for (MXDeviceInfo device : devices) {
                                    String key = device.identityKey();

                                    if (TextUtils.equals(key, mCrypto.getOlmDevice().getDeviceCurve25519Key())) {
                                        // Don't bother setting up session to ourself
                                        continue;
                                    }

                                    if (device.isBlocked()) {
                                        // Don't bother setting up sessions with blocked users
                                        continue;
                                    }

                                    deviceInfos.add(device);
                                }
                            }
                        }

                        Map<String, Object> messageMap = new HashMap<>();
                        messageMap.put("room_id", mRoomId);
                        messageMap.put("type", eventType);
                        messageMap.put("content", eventContent);

                        mCrypto.encryptMessage(messageMap, deviceInfos);
                        callback.onSuccess(JsonUtility.getGson(false).toJsonTree(messageMap));
                    }
                }
        );
    }

    /**
     * Ensure that the session
     *
     * @param users    the user ids list
     * @param callback the asynchronous callback
     */
    private void ensureSession(final List<String> users, final ApiCallback<Void> callback) {
        mCrypto.getDeviceList().downloadKeys(users, false, new SimpleApiCallback<MXUsersDevicesMap<MXDeviceInfo>>(callback) {
            @Override
            public void onSuccess(MXUsersDevicesMap<MXDeviceInfo> info) {
                mCrypto.ensureOlmSessionsForUsers(users, new SimpleApiCallback<MXUsersDevicesMap<MXOlmSessionResult>>(callback) {
                    @Override
                    public void onSuccess(MXUsersDevicesMap<MXOlmSessionResult> result) {
                        if (null != callback) {
                            callback.onSuccess(null);
                        }
                    }
                });
            }
        });
    }
}
