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

package org.matrix.androidsdk.crypto.api

import org.matrix.androidsdk.crypto.model.keys.*
import org.matrix.androidsdk.crypto.model.rest.keys.BackupKeysResult
import org.matrix.androidsdk.crypto.model.rest.keys.UpdateKeysBackupVersionBody
import retrofit2.Call
import retrofit2.http.*

/**
 * Ref: https://github.com/uhoreg/matrix-doc/blob/e2e_backup/proposals/1219-storing-megolm-keys-serverside.md
 */
interface RoomKeysApi {

    /* ==========================================================================================
     * Backup versions management
     * ========================================================================================== */

    /**
     * Create a new keys backup version.
     */
    @POST("room_keys/version")
    fun createKeysBackupVersion(@Body createKeysBackupVersionBody: CreateKeysBackupVersionBody): Call<KeysVersion>

    /**
     * Get information about the last version.
     */
    @GET("room_keys/version")
    fun getKeysBackupLastVersion(): Call<KeysVersionResult>

    /**
     * Get information about the given version.
     */
    @GET("room_keys/version/{version}")
    fun getKeysBackupVersion(@Path("version") version: String): Call<KeysVersionResult>

    /**
     * Update information about the given version.
     */
    @PUT("room_keys/version/{version}")
    fun updateKeysBackupVersion(@Path("version") version: String,
                                @Body keysBackupVersionBody: UpdateKeysBackupVersionBody): Call<Void>

    /* ==========================================================================================
     * Storing keys
     * ========================================================================================== */

    /**
     * Store the key for the given session in the given room, using the given backup version.
     *
     *
     * If the server already has a backup in the backup version for the given session and room, then it will
     * keep the "better" one. To determine which one is "better", key backups are compared first by the is_verified
     * flag (true is better than false), then by the first_message_index (a lower number is better), and finally by
     * forwarded_count (a lower number is better).
     */
    @PUT("room_keys/keys/{roomId}/{sessionId}")
    fun storeRoomSessionData(@Path("roomId") roomId: String,
                             @Path("sessionId") sessionId: String,
                             @Query("version") version: String,
                             @Body keyBackupData: KeyBackupData): Call<BackupKeysResult>


    /**
     * Store several keys for the given room, using the given backup version.
     */
    @PUT("room_keys/keys/{roomId}")
    fun storeRoomSessionsData(@Path("roomId") roomId: String,
                              @Query("version") version: String,
                              @Body roomKeysBackupData: RoomKeysBackupData): Call<BackupKeysResult>

    /**
     * Store several keys, using the given backup version.
     */
    @PUT("room_keys/keys")
    fun storeSessionsData(@Query("version") version: String,
                          @Body keysBackupData: KeysBackupData): Call<BackupKeysResult>

    /* ==========================================================================================
     * Retrieving keys
     * ========================================================================================== */

    /**
     * Retrieve the key for the given session in the given room from the backup.
     */
    @GET("room_keys/keys/{roomId}/{sessionId}")
    fun getRoomSessionData(@Path("roomId") roomId: String,
                           @Path("sessionId") sessionId: String,
                           @Query("version") version: String): Call<KeyBackupData>

    /**
     * Retrieve all the keys for the given room from the backup.
     */
    @GET("room_keys/keys/{roomId}")
    fun getRoomSessionsData(@Path("roomId") roomId: String,
                            @Query("version") version: String): Call<RoomKeysBackupData>

    /**
     * Retrieve all the keys from the backup.
     */
    @GET("room_keys/keys")
    fun getSessionsData(@Query("version") version: String): Call<KeysBackupData>


    /* ==========================================================================================
     * Deleting keys
     * ========================================================================================== */

    /**
     * Deletes keys from the backup.
     */
    @DELETE("room_keys/keys/{roomId}/{sessionId}")
    fun deleteRoomSessionData(@Path("roomId") roomId: String,
                              @Path("sessionId") sessionId: String,
                              @Query("version") version: String): Call<Void>

    /**
     * Deletes keys from the backup.
     */
    @DELETE("room_keys/keys/{roomId}")
    fun deleteRoomSessionsData(@Path("roomId") roomId: String,
                               @Query("version") version: String): Call<Void>

    /**
     * Deletes keys from the backup.
     */
    @DELETE("room_keys/keys")
    fun deleteSessionsData(@Query("version") version: String): Call<Void>

    /* ==========================================================================================
     * Deleting backup
     * ========================================================================================== */

    /**
     * Deletes a backup.
     */
    @DELETE("room_keys/version/{version}")
    fun deleteBackup(@Path("version") version: String): Call<Void>
}
