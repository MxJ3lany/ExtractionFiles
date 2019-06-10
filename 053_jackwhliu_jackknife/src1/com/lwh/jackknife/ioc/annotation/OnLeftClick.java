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

import com.lwh.jackknife.widget.TitleBar;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(METHOD)
@Retention(RUNTIME)
@EventBase(
        listenerSetter = "setOnLeftClickListener",
        listenerType = TitleBar.OnLeftClickListener.class,
        callbackMethod = "onClick",
        parameters = View.class,
        parameterNames = "view",
        returns = void.class
)
public @interface OnLeftClick {
    int[] value() default {View.NO_ID};
}