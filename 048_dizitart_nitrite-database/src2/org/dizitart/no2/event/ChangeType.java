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

package org.dizitart.no2.event;

import org.dizitart.no2.*;
import org.dizitart.no2.objects.ObjectFilter;
import org.dizitart.no2.objects.ObjectRepository;

/**
 * Represents different types of collection modification
 * actions.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
public enum ChangeType {
    /**
     * Insert action.
     *
     * @see org.dizitart.no2.NitriteCollection#insert(Object[])
     * @see org.dizitart.no2.objects.ObjectRepository#insert(Object, Object[])
     * @see org.dizitart.no2.objects.ObjectRepository#insert(Object[])
     */
    INSERT,

    /**
     * Update action.
     *
     * @see org.dizitart.no2.NitriteCollection#update(Filter, Document)
     * @see org.dizitart.no2.NitriteCollection#update(Filter, Document, UpdateOptions)
     * @see org.dizitart.no2.objects.ObjectRepository#update(ObjectFilter, Object)
     */
    UPDATE,

    /**
     * Remove action.
     *
     * @see org.dizitart.no2.NitriteCollection#remove(Filter)
     * @see org.dizitart.no2.NitriteCollection#remove(Filter, RemoveOptions)
     * @see org.dizitart.no2.objects.ObjectRepository#remove(ObjectFilter, RemoveOptions)
     */
    REMOVE,

    /**
     * Collection Drop action.
     *
     * @see NitriteCollection#drop()
     * @see ObjectRepository#drop()
     */
    DROP,

    /**
     * Collection Close action.
     *
     * @see NitriteCollection#close()
     * @see ObjectRepository#close()
     */
    CLOSE
}
