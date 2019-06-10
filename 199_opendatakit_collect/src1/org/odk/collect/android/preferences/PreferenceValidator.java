/*
 * Copyright (C) 2018 Callum Stott
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.preferences;

import java.util.Map;

class PreferenceValidator {

    private final Map<String, Object> defaults;

    PreferenceValidator(Map<String, Object> defaults) {
        this.defaults = defaults;
    }

    boolean isValid(Map<String, Object> preferences) {
        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            Object defaultValue = entry.getValue();
            Object newValue = preferences.get(entry.getKey());

            if (newValue != null) {
                if (!defaultValue.getClass().isAssignableFrom(newValue.getClass())) {
                    return false;
                }
            }

        }

        return true;
    }
}
