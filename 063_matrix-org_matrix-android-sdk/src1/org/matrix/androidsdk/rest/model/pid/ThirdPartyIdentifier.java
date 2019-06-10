/*
 * Copyright 2016 OpenMarket Ltd
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

package org.matrix.androidsdk.rest.model.pid;

import java.io.Serializable;

public class ThirdPartyIdentifier implements Serializable {
    /**
     * The medium of the third party identifier (ThreePid.MEDIUM_XXX)
     */
    public String medium;

    /**
     * The third party identifier address.
     */
    public String address;

    /**
     * The timestamp in milliseconds when this 3PID has been validated.
     * Define as Object because it should be Long and it is a Double.
     * So, it might change.
     */
    public Object validatedAt;

    /**
     * The timestamp in milliseconds when this 3PID has been added to the user account.
     * Define as Object because it should be Long and it is a Double.
     * So, it might change.
     */
    public Object addedAt;
}
