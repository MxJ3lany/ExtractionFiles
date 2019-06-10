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

package org.dizitart.no2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a key and a value pair.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
@Data
@EqualsAndHashCode
@AllArgsConstructor
public class KeyValuePair {

    /**
     * The key of the pair.
     *
     * @param key the key to set.
     * @returns the key.
     * */
    private String key;

    /**
     * The value of the pair.
     *
     * @param value the value to set.
     * @returns the value.
     * */
    private Object value;
}
