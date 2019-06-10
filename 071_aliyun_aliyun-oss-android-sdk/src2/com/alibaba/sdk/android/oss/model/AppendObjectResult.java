/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.alibaba.sdk.android.oss.model;


/**
 * Successful response of append object operation.
 */
public class AppendObjectResult extends OSSResult {

    /* Indicates that which position to append at next time. */
    private long nextPosition;

    /* Returned value of the appended object crc64 */
    private String objectCRC64;

    /**
     * Gets the next position for appending
     *
     * @return
     */
    public long getNextPosition() {
        return nextPosition;
    }

    public void setNextPosition(Long nextPosition) {
        this.nextPosition = nextPosition;
    }

    /**
     * Gets the CRC64 checksum
     *
     * @return
     */
    public String getObjectCRC64() {
        return objectCRC64;
    }

    public void setObjectCRC64(String objectCRC64) {
        this.objectCRC64 = objectCRC64;
    }
}
