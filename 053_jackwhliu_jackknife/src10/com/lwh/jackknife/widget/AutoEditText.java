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
import android.text.InputFilter;
import android.text.method.NumberKeyListener;
import android.util.AttributeSet;
import android.widget.EditText;

public abstract class AutoEditText extends EditText {

    public AutoEditText(Context context) {
        this(context, null);
    }

    public AutoEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setMaxLength();
    }

    protected void setMaxLength() {
        setFilters(new InputFilter[]{new InputFilter.LengthFilter(getMaxLength())});
    }

    public void addInputType(final int inputType) {
        setKeyListener(new NumberKeyListener() {
            @Override
            protected char[] getAcceptedChars() {
                return getInputFilterAcceptedChars();
            }

            @Override
            public int getInputType() {
                return inputType;
            }
        });
    }

    public abstract int getMaxLength();

    public abstract char[] getInputFilterAcceptedChars();

    public abstract boolean checkInputValue();
}
