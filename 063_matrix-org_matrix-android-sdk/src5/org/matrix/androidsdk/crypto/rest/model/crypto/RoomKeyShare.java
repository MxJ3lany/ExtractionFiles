/*
 * Copyright 2019 New Vector Ltd
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
package org.matrix.androidsdk.crypto.rest.model.crypto;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

/**
 * Parent class representing an room key action request
 * Note: this class cannot be abstract because of {@link org.matrix.androidsdk.core.JsonUtils#toRoomKeyShare(JsonElement)}
 */
public class RoomKeyShare implements SendToDeviceObject {
    public static final String ACTION_SHARE_REQUEST = "request";
    public static final String ACTION_SHARE_CANCELLATION = "request_cancellation";

    public String action;

    @SerializedName("requesting_device_id")
    public String requestingDeviceId;

    @SerializedName("request_id")
    public String requestId;
}