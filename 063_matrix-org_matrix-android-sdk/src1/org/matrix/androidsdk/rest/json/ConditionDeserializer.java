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
package org.matrix.androidsdk.rest.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.matrix.androidsdk.core.Log;
import org.matrix.androidsdk.rest.model.bingrules.Condition;
import org.matrix.androidsdk.rest.model.bingrules.ContainsDisplayNameCondition;
import org.matrix.androidsdk.rest.model.bingrules.DeviceCondition;
import org.matrix.androidsdk.rest.model.bingrules.EventMatchCondition;
import org.matrix.androidsdk.rest.model.bingrules.RoomMemberCountCondition;
import org.matrix.androidsdk.rest.model.bingrules.SenderNotificationPermissionCondition;
import org.matrix.androidsdk.rest.model.bingrules.UnknownCondition;

import java.lang.reflect.Type;

public class ConditionDeserializer implements JsonDeserializer<Condition> {
    private static final String LOG_TAG = ConditionDeserializer.class.getSimpleName();

    @Override
    public Condition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Condition condition = null;

        JsonObject jsonObject = json.getAsJsonObject();
        JsonElement kindElement = jsonObject.get("kind");

        if (null != kindElement) {
            String kind = kindElement.getAsString();

            if (null != kind) {
                switch (kind) {
                    case Condition.KIND_EVENT_MATCH:
                        condition = context.deserialize(json, EventMatchCondition.class);
                        break;
                    case Condition.KIND_DEVICE:
                        condition = context.deserialize(json, DeviceCondition.class);
                        break;
                    case Condition.KIND_CONTAINS_DISPLAY_NAME:
                        condition = context.deserialize(json, ContainsDisplayNameCondition.class);
                        break;
                    case Condition.KIND_ROOM_MEMBER_COUNT:
                        condition = context.deserialize(json, RoomMemberCountCondition.class);
                        break;
                    case Condition.KIND_SENDER_NOTIFICATION_PERMISSION:
                        condition = context.deserialize(json, SenderNotificationPermissionCondition.class);
                        break;
                    default:
                        Log.e(LOG_TAG, "## deserialize() : unsupported kind " + kind + " with value " + json);
                        condition = context.deserialize(json, UnknownCondition.class);
                        break;
                }
            }
        }
        return condition;
    }
}
