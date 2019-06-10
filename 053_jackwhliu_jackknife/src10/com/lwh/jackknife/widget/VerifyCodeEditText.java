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

package com.lwh.jackknife.widget;

import android.content.Context;

public class VerifyCodeEditText extends AutoEditText {

    public VerifyCodeEditText(Context context) {
        super(context);
    }

    @Override
    public int getMaxLength() {
        return 1;
    }

    @Override
    public char[] getInputFilterAcceptedChars() {
        return new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    }

    @Override
    public boolean checkInputValue() {
        return getText().length() == 1 ? true : false;
    }
}
