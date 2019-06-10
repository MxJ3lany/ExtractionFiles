/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.datagate;

/**
 * DataGate constants.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
public class Constants {
    public static final String AUTH_USER = "USER";
    public static final String AUTH_CLIENT = "CLIENT";
    public static final String AUTH_ADMIN = "ADMIN";

    public static final String USER_REPO = "no2.datagate.users";
    public static final String SYNC_LOG = "no2.datagate.synclog";
    public static final String ATTRIBUTE_REPO = "no2.datagate.attributes";

    public static final String VENDOR = "Nitrite DataGate Server";
    public static final String VERSION = "1.0";
    public static final String STORAGE_VENDOR = "Mongo Database";
}
