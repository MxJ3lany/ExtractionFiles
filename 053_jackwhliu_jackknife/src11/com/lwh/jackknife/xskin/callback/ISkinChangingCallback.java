/*
 * Copyright (C) 2018 The JackKnife Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lwh.jackknife.xskin.callback;

public interface ISkinChangingCallback {

    void onStart();

    void onError(Exception e);

    void onComplete();

    DefaultSkinChangingCallback DEFAULT_SKIN_CHANGING_CALLBACK = new DefaultSkinChangingCallback();

    class DefaultSkinChangingCallback implements ISkinChangingCallback {

        @Override
        public void onStart() {
        }

        @Override
        public void onError(Exception e) {
        }

        @Override
        public void onComplete() {
        }
    }
}
