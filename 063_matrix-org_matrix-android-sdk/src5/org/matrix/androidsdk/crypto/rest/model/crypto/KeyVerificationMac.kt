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
package org.matrix.androidsdk.crypto.rest.model.crypto

import com.google.gson.annotations.SerializedName

/**
 * Sent by both devices to send the MAC of their device key to the other device.
 */
class KeyVerificationMac : SendToDeviceObject {

    /**
     * the ID of the transaction that the message is part of
     */
    @SerializedName("transaction_id")
    @JvmField
    var transactionID: String? = null

    /**
     * A map of key ID to the MAC of the key, as an unpadded base64 string, calculated using the MAC key
     */
    @JvmField
    var mac: Map<String, String>? = null

    /**
     *  The MAC of the comma-separated, sorted list of key IDs given in the mac property,
     *  as an unpadded base64 string, calculated using the MAC key.
     *  For example, if the mac property gives MACs for the keys ed25519:ABCDEFG and ed25519:HIJKLMN, then this property will
     *  give the MAC of the string “ed25519:ABCDEFG,ed25519:HIJKLMN”.
     */
    @JvmField
    var keys: String? = null

    fun isValid(): Boolean {
        if (transactionID.isNullOrBlank() || keys.isNullOrBlank() || mac.isNullOrEmpty()) {
            return false
        }
        return true
    }

    companion object {
        fun create(tid: String, mac: Map<String, String>, keys: String): KeyVerificationMac {
            return KeyVerificationMac().apply {
                this.transactionID = tid
                this.mac = mac
                this.keys = keys
            }
        }
    }

}