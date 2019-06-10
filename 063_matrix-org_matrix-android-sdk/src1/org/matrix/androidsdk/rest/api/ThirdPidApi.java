/*
 * Copyright 2014 OpenMarket Ltd
 * Copyright 2017 Vector Creations Ltd
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
package org.matrix.androidsdk.rest.api;

import org.matrix.androidsdk.rest.model.BulkLookupParams;
import org.matrix.androidsdk.rest.model.BulkLookupResponse;
import org.matrix.androidsdk.rest.model.RequestEmailValidationParams;
import org.matrix.androidsdk.rest.model.pid.PidResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ThirdPidApi {

    /**
     * Get the 3rd party id from a medium
     *
     * @param address the address.
     * @param medium  the medium.
     */
    @GET("lookup")
    Call<PidResponse> lookup3Pid(@Query("address") String address,
                                 @Query("medium") String medium);

    /**
     * Request a bunch of 3PIDs
     *
     * @param body the body request
     */
    @POST("bulk_lookup")
    Call<BulkLookupResponse> bulkLookup(@Body BulkLookupParams body);


    /**
     * Request the ownership validation of an email address or a phone number previously set
     * by {@link ProfileApi#requestEmailValidation(RequestEmailValidationParams)}
     *
     * @param medium       the medium of the 3pid
     * @param token        the token generated by the requestToken call
     * @param clientSecret the client secret which was supplied in the requestToken call
     * @param sid          the sid for the session
     */
    @POST("validate/{medium}/submitToken")
    Call<Map<String, Object>> requestOwnershipValidation(@Path("medium") String medium,
                                                         @Query("token") String token,
                                                         @Query("client_secret") String clientSecret,
                                                         @Query("sid") String sid);
}