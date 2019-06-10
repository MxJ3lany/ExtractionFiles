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
import org.matrix.androidsdk.core.Log
import org.matrix.androidsdk.crypto.verification.SASVerificationTransaction

/**
 * Sent by Bob to accept a verification from a previously sent m.key.verification.start message.
 */
class KeyVerificationAccept : SendToDeviceObject {

    /**
     * string to identify the transaction.
     * This string must be unique for the pair of users performing verification for the duration that the transaction is valid.
     * Alice’s device should record this ID and use it in future messages in this transaction.
     */
    @JvmField
    @SerializedName("transaction_id")
    var transactionID: String? = null

    /**
     * The key agreement protocol that Bob’s device has selected to use, out of the list proposed by Alice’s device
     */
    @JvmField
    @SerializedName("key_agreement_protocol")
    var keyAgreementProtocol: String? = null

    /**
     * The hash algorithm that Bob’s device has selected to use, out of the list proposed by Alice’s device
     */
    @JvmField
    var hash: String? = null

    /**
     * The message authentication code that Bob’s device has selected to use, out of the list proposed by Alice’s device
     */
    @JvmField
    @SerializedName("message_authentication_code")
    var messageAuthenticationCode: String? = null

    /**
     * An array of short authentication string methods that Bob’s client (and Bob) understands.  Must be a subset of the list proposed by Alice’s device
     */
    @JvmField
    @SerializedName("short_authentication_string")
    var shortAuthenticationStrings: List<String>? = null


    /**
     * The hash (encoded as unpadded base64) of the concatenation of the device’s ephemeral public key (QB, encoded as unpadded base64)
     *  and the canonical JSON representation of the m.key.verification.start message.
     */
    @JvmField
    var commitment: String? = null


    fun isValid(): Boolean {
        if (transactionID.isNullOrBlank()
                || keyAgreementProtocol.isNullOrBlank()
                || hash.isNullOrBlank()
                || commitment.isNullOrBlank()
                || messageAuthenticationCode.isNullOrBlank()
                || shortAuthenticationStrings.isNullOrEmpty()) {
            Log.e(SASVerificationTransaction.LOG_TAG, "## received invalid verification request")
            return false
        }
        return true
    }

    companion object {
        fun create(tid: String,
                   keyAgreementProtocol: String,
                   hash: String,
                   commitment: String,
                   messageAuthenticationCode: String,
                   shortAuthenticationStrings: List<String>): KeyVerificationAccept {
            return KeyVerificationAccept().apply {
                this.transactionID = tid
                this.keyAgreementProtocol = keyAgreementProtocol
                this.hash = hash
                this.commitment = commitment
                this.messageAuthenticationCode = messageAuthenticationCode
                this.shortAuthenticationStrings = shortAuthenticationStrings
            }
        }
    }
}
