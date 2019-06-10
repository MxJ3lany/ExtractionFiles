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

import android.view.MotionEvent;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static android.view.View.OnTouchListener;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Bind a method to an {@link OnTouchListener OnTouchListener} on the view for each ID specified.
 * <pre><code>
 * {@literal @}OnTouch(R.id.example) boolean onTouch() {
 *   Toast.makeText(this, "Touched!", Toast.LENGTH_SHORT).show();
 *   return false;
 * }
 * </code></pre>
 * Any number of parameters from
 * {@link OnTouchListener#onTouch(android.view.View, android.view.MotionEvent) onTouch} may be used
 * on the method.
 *
 * @see OnTouchListener
 */
@Target(METHOD)
@Retention(RUNTIME)
@EventBase(
        listenerSetter = "setOnTouchListener",
        listenerType = View.OnTouchListener.class,
        callbackMethod = "onTouch",
        parameters = {
                View.class, MotionEvent.class
        },
        parameterNames = {
                "v", "event"
        },
        returns = boolean.class)
public @interface OnTouch {
    int[] value() default {View.NO_ID};
}