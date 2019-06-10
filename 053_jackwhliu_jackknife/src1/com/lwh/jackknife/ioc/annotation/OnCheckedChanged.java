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
import android.widget.RadioGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static android.widget.RadioGroup.OnCheckedChangeListener;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Bind a method to an {@link OnCheckedChangeListener OnCheckedChangeListener} on the view for
 * each ID specified.
 * Any number of parameters from
 * {@link OnCheckedChangeListener#onCheckedChanged(RadioGroup, int)
 * onCheckedChanged} may be used on the method.
 *
 * @see OnCheckedChangeListener
 */
@Target(METHOD)
@Retention(RUNTIME)
@EventBase(
        listenerSetter = "setOnCheckedChangeListener",
        listenerType = RadioGroup.OnCheckedChangeListener.class,
        callbackMethod = "onCheckedChanged",
        parameters = {
                RadioGroup.class, int.class
        },
        parameterNames = {
                "group", "checkedId"
        },
        returns = void.class
)
public @interface OnCheckedChanged {
    int[] value() default {View.NO_ID};
}