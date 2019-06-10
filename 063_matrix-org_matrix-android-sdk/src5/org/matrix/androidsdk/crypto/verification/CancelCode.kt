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
package org.matrix.androidsdk.crypto.verification

enum class CancelCode(val value: String, val humanReadable: String) {
    User("m.user", "the user cancelled the verification"),
    Timeout("m.timeout", "the verification process timed out"),
    UnknownTransaction("m.unknown_transaction", "the device does not know about that transaction"),
    UnknownMethod("m.unknown_method", "the device can’t agree on a key agreement, hash, MAC, or SAS method"),
    MismatchedCommitment("m.mismatched_commitment", "the hash commitment did not match"),
    MismatchedSas("m.mismatched_sas", "the SAS did not match"),
    UnexpectedMessage("m.unexpected_message", "the device received an unexpected message"),
    InvalidMessage("m.invalid_message", "an invalid message was received"),
    MismatchedKeys("m.key_mismatch", "Key mismatch"),
    UserMismatchError("m.user_error", "User mismatch")
}

fun safeValueOf(code: String?): CancelCode {
    return CancelCode.values().firstOrNull { code == it.value } ?: CancelCode.User
}