/*
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

package org.matrix.androidsdk.crypto.cryptostore.db.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.matrix.androidsdk.crypto.OutgoingRoomKeyRequest
import org.matrix.androidsdk.crypto.cryptostore.db.deserializeFromRealm
import org.matrix.androidsdk.crypto.cryptostore.db.serializeForRealm
import org.matrix.androidsdk.crypto.model.crypto.RoomKeyRequestBody

internal open class OutgoingRoomKeyRequestEntity(
        @PrimaryKey var requestId: String? = null,
        var cancellationTxnId: String? = null,
        // Serialized Json
        var recipientsData: String? = null,
        // RoomKeyRequestBody fields
        var requestBodyAlgorithm: String? = null,
        var requestBodyRoomId: String? = null,
        var requestBodySenderKey: String? = null,
        var requestBodySessionId: String? = null,
        // State
        var state: Int = 0
) : RealmObject() {

    /**
     * Convert to OutgoingRoomKeyRequest
     */
    fun toOutgoingRoomKeyRequest(): OutgoingRoomKeyRequest {
        return OutgoingRoomKeyRequest(
                RoomKeyRequestBody().apply {
                    algorithm = requestBodyAlgorithm
                    roomId = requestBodyRoomId
                    senderKey = requestBodySenderKey
                    sessionId = requestBodySessionId
                },
                getRecipients(),
                requestId,
                OutgoingRoomKeyRequest.RequestState.from(state)
        ).apply {
            this.mCancellationTxnId = cancellationTxnId
        }
    }

    private fun getRecipients(): List<MutableMap<String, String>>? {
        return deserializeFromRealm(recipientsData)
    }

    fun putRecipients(recipients: List<MutableMap<String, String>>?) {
        recipientsData = serializeForRealm(recipients)
    }

    fun putRequestBody(requestBody: RoomKeyRequestBody?) {
        requestBody?.let {
            requestBodyAlgorithm = it.algorithm
            requestBodyRoomId = it.roomId
            requestBodySenderKey = it.senderKey
            requestBodySessionId = it.sessionId
        }
    }
}


