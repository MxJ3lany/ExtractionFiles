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
package org.matrix.androidsdk.rest.model.sync;

import org.jetbrains.annotations.NotNull;
import org.matrix.androidsdk.crypto.interfaces.CryptoDeviceListResponse;

import java.util.List;

/**
 * This class describes the device list response from a sync request
 */
public class DeviceListResponse implements CryptoDeviceListResponse {
    // user ids list which have new crypto devices
    public List<String> changed;

    //  List of user ids who are no more tracked.
    public List<String> left;

    @NotNull
    @Override
    public List<String> getChanged() {
        return changed;
    }

    @NotNull
    @Override
    public List<String> getLeft() {
        return left;
    }
}
