/*
 * Copyright 2016 OpenMarket Ltd
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

package org.matrix.androidsdk.crypto.data;

import java.io.Serializable;

public class MXOlmSessionResult implements Serializable {
    /**
     * the device
     */
    public final MXDeviceInfo mDevice;

    /**
     * Base64 olm session id.
     * null if no session could be established.
     */
    public String mSessionId;

    /**
     * Constructor
     *
     * @param device    the device
     * @param sessionId the olm session id
     */
    public MXOlmSessionResult(MXDeviceInfo device, String sessionId) {
        mDevice = device;
        mSessionId = sessionId;
    }
}