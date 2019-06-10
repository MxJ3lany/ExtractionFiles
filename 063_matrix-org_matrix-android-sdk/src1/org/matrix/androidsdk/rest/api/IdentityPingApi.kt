/*
 * Copyright 2019 Vector Creations Ltd
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
package org.matrix.androidsdk.rest.api

import org.json.JSONObject
import org.matrix.androidsdk.RestClient

import retrofit2.Call
import retrofit2.http.GET

interface IdentityPingApi {

    /**
     * https://matrix.org/docs/spec/client_server/r0.4.0.html#server-discovery
     * Simple ping call to check if server alive
     * @return
     */
    @GET(RestClient.URI_IDENTITY_PATH)
    fun ping(): Call<JSONObject>
}
