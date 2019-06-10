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

/**
 * This class represents the summary of a community in the server response.
 */
public class GroupSummary implements Serializable {
    /**
     * The group profile.
     */
    public GroupProfile profile;

    /**
     * The group users.
     */
    public GroupSummaryUsersSection usersSection;

    /**
     * The current user status.
     */
    public GroupSummaryUser user;

    /**
     * The rooms linked to the community.
     */
    public GroupSummaryRoomsSection roomsSection;
}
