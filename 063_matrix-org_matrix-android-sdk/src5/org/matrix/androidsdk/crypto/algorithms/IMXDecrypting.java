/*
 * Copyright 2015 OpenMarket Ltd
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

package org.matrix.androidsdk.crypto.algorithms;

import android.support.annotation.Nullable;

import org.matrix.androidsdk.crypto.IncomingRoomKeyRequest;
import org.matrix.androidsdk.crypto.MXDecryptionException;
import org.matrix.androidsdk.crypto.MXEventDecryptionResult;
import org.matrix.androidsdk.crypto.interfaces.CryptoEvent;
import org.matrix.androidsdk.crypto.interfaces.CryptoSession;
import org.matrix.androidsdk.crypto.internal.MXCryptoImpl;

/**
 * An interface for decrypting data
 */
public interface IMXDecrypting {
    /**
     * Init the object fields
     *
     * @param matrixSession the session
     */
    void initWithMatrixSession(CryptoSession matrixSession, MXCryptoImpl crypto);

    /**
     * Decrypt an event
     *
     * @param event    the raw event.
     * @param timeline the id of the timeline where the event is decrypted. It is used to prevent replay attack.
     * @return the decryption information, or null in case of error
     * @throws MXDecryptionException the decryption failure reason
     */
    @Nullable
    MXEventDecryptionResult decryptEvent(CryptoEvent event, String timeline) throws MXDecryptionException;

    /**
     * Handle a key event.
     *
     * @param event the key event.
     */
    void onRoomKeyEvent(CryptoEvent event);

    /**
     * Check if the some messages can be decrypted with a new session
     *
     * @param senderKey the session sender key
     * @param sessionId the session id
     */
    void onNewSession(String senderKey, String sessionId);

    /**
     * Determine if we have the keys necessary to respond to a room key request
     *
     * @param request keyRequest
     * @return true if we have the keys and could (theoretically) share
     */
    boolean hasKeysForKeyRequest(IncomingRoomKeyRequest request);

    /**
     * Send the response to a room key request.
     *
     * @param request keyRequest
     */
    void shareKeysWithDevice(IncomingRoomKeyRequest request);
}
