/*
 * Copyright 2016 OpenMarket Ltd
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

package org.matrix.androidsdk.crypto;


import org.matrix.androidsdk.crypto.interfaces.CryptoEvent;
import org.matrix.androidsdk.crypto.model.crypto.RoomKeyRequestBody;
import org.matrix.androidsdk.crypto.rest.model.crypto.RoomKeyShareRequest;

import java.io.Serializable;

/**
 * IncomingRoomKeyRequest class defines the incoming room keys request.
 *
 * Keep Serializable for legacy FileStore (which will be removed in the future)
 */
public class IncomingRoomKeyRequest implements Serializable {
    /**
     * The user id
     */
    public String mUserId;

    /**
     * The device id
     */
    public String mDeviceId;

    /**
     * The request id
     */
    public String mRequestId;

    /**
     * The request body
     */
    public RoomKeyRequestBody mRequestBody;

    /**
     * The runnable to call to accept to share the keys
     */
    public transient Runnable mShare;

    /**
     * The runnable to call to ignore the key share request.
     */
    public transient Runnable mIgnore;

    /**
     * Constructor
     *
     * @param event the event
     */
    public IncomingRoomKeyRequest(CryptoEvent event) {
        mUserId = event.getSender();

        RoomKeyShareRequest roomKeyShareRequest = event.toRoomKeyShareRequest();
        mDeviceId = roomKeyShareRequest.requestingDeviceId;
        mRequestId = roomKeyShareRequest.requestId;
        mRequestBody = (null != roomKeyShareRequest.body) ? roomKeyShareRequest.body : new RoomKeyRequestBody();
    }

    /**
     * Constructor for object creation from crypto store
     */
    public IncomingRoomKeyRequest() {

    }
}

