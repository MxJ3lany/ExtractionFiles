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

package com.lwh.jackknife.ioc.annotation;

import android.view.View;
import android.widget.CompoundButton;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static android.widget.CompoundButton.OnCheckedChangeListener;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Bind a method to an {@link OnCheckedChangeListener OnCheckedChangeListener} on the view for
 * each ID specified.
 * <pre><code>
 * {@literal @}OnCheckedChanged2(R.id.example) void onChecked(boolean checked) {
 *   Toast.makeText(this, checked ? "Checked!" : "Unchecked!", Toast.LENGTH_SHORT).show();
 * }
 * </code></pre>
 * Any number of parameters from
 * {@link OnCheckedChangeListener#onCheckedChanged(android.widget.CompoundButton, boolean)
 * onCheckedChanged} may be used on the method.
 *
 * @see OnCheckedChangeListener
 */
@Target(METHOD)
@Retention(RUNTIME)
@EventBase(
        listenerSetter = "setOnCheckedChangeListener",
        listenerType = CompoundButton.OnCheckedChangeListener.class,
        callbackMethod = "onCheckedChanged",
        parameters = {
                CompoundButton.class, boolean.class
        },
        parameterNames = {
                "buttonView", "isChecked"
        },
        returns = void.class
)
public @interface OnCheckedChanged2 {
    int[] value() default {View.NO_ID};
}