/*
 * Copyright 2015 OpenMarket Ltd
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

package org.matrix.androidsdk.call;

import org.matrix.androidsdk.crypto.data.MXDeviceInfo;
import org.matrix.androidsdk.crypto.data.MXUsersDevicesMap;

/**
 * This class is the default implementation of MXCallsManagerListener
 */
public class MXCallsManagerListener implements IMXCallsManagerListener {

    @Override
    public void onIncomingCall(IMXCall call, MXUsersDevicesMap<MXDeviceInfo> unknownDevices) {
    }

    @Override
    public void onOutgoingCall(IMXCall call) {
    }

    @Override
    public void onCallHangUp(IMXCall call) {
    }

    @Override
    public void onVoipConferenceStarted(String roomId) {
    }

    @Override
    public void onVoipConferenceFinished(String roomId) {
    }
}
