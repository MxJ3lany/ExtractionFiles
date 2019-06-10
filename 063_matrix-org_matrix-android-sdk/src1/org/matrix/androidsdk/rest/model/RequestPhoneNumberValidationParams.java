/*
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

package org.matrix.androidsdk.rest.model;

/**
 * Parameters to request a validation token for a phone number
 */
public class RequestPhoneNumberValidationParams {

    // the country
    public String country;

    // the phone number
    public String phone_number;

    // the client secret key
    public String clientSecret;

    // the attempt count
    public Integer sendAttempt;

    // the server id
    public String id_server;
}
