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

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static android.view.View.OnLongClickListener;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Bind a method to an {@link OnLongClickListener OnLongClickListener} on the view for each ID
 * specified.
 * <pre><code>
 * {@literal @}OnLongClick(R.id.example) boolean onLongClick() {
 *   Toast.makeText(this, "Long clicked!", Toast.LENGTH_SHORT).show();
 *   return true;
 * }
 * </code></pre>
 * Any number of parameters from {@link OnLongClickListener#onLongClick(android.view.View)} may be
 * used on the method.
 *
 * @see OnLongClickListener
 */
@Target(METHOD)
@Retention(RUNTIME)
@EventBase(
        listenerSetter = "setOnLongClickListener",
        listenerType = View.OnLongClickListener.class,
        callbackMethod = "onLongClick",
        parameters = View.class,
        parameterNames = "v",
        returns = boolean.class
)
public @interface OnLongClick {
    int[] value() default {View.NO_ID};
}