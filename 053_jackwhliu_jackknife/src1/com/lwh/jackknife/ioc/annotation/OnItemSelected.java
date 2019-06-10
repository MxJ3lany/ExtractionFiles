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
import android.widget.AdapterView;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static android.widget.AdapterView.OnItemSelectedListener;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Bind a method to an {@link OnItemSelectedListener OnItemSelectListener} on the view for each
 * ID specified.
 * <pre><code>
 * {@literal @}OnItemSelected(R.id.example_list) void onItemSelected(int position) {
 *   Toast.makeText(this, "Selected position " + position + "!", Toast.LENGTH_SHORT).show();
 * }
 * </code></pre>
 * Any number of parameters from
 * {@link OnItemSelectedListener#onItemSelected(android.widget.AdapterView, android.view.View, int,
 * long) onItemSelected} may be used on the method.
 * <p>
 * To bind to methods other than {@code onItemSelected}, specify a different {@code callback}.
 * <pre><code>
 * {@literal @}OnItemSelected(value = R.id.example_list, callback = NOTHING_SELECTED)
 * void onNothingSelected() {
 *   Toast.makeText(this, "Nothing selected!", Toast.LENGTH_SHORT).show();
 * }
 * </code></pre>
 *
 * @see OnItemSelectedListener
 */
@Target(METHOD)
@Retention(RUNTIME)
@EventBase(
        listenerSetter = "setmOnItemSelectListener",
        listenerType = AdapterView.OnItemSelectedListener.class,
        callbackMethod = "onItemSelected",
        parameters = {
                AdapterView.class, View.class, int.class, long.class
        },
        parameterNames = {
                "parent", "view", "position", "id"
        },
        returns = void.class
)
public @interface OnItemSelected {
    int[] value() default {View.NO_ID};
}