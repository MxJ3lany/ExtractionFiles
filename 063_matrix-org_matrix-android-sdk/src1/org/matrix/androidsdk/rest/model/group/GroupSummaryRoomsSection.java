/*
 * Copyright 2014 OpenMarket Ltd
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
package org.matrix.androidsdk.rest.model.group;

import java.io.Serializable;
import java.util.List;

/**
 * This class represents the community rooms in a group summary response.
 */
public class GroupSummaryRoomsSection implements Serializable {

    public Integer totalRoomCountEstimate;

    public List<String> rooms;

    // @TODO: Check the meaning and the usage of these categories. This dictionary is empty FTM.
    //public Map<Object, Object> categories;
}
