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

/**
 * An interface when implemented makes an object be
 * able to listen to any changes in a {@link org.dizitart.no2.NitriteCollection}
 * or {@link org.dizitart.no2.objects.ObjectRepository}.
 *
 * [[app-listing]]
 * [source,java]
 * .Example
 * --
 *
 *  // observe any change to the collection
 *  collection.register(new ChangeListener() {
 *
 *      @Override
 *      public void onChange(ChangeInfo changeInfo) {
 *          System.out.println("Action - " + changeInfo.getChangeType());
 *
 *          System.out.println("List of affected ids:");
 *          for (NitriteId id : changeInfo.getChangedItems()) {
 *              System.out.println("Id - " + id);
 *          }
 *      }
 *  });
 *
 * --
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
public interface ChangeListener {

    /**
     * Listener routine to be invoked for each change event.
     *
     * @param changeInfo the change information
     */
    void onChange(ChangeInfo changeInfo);
}
