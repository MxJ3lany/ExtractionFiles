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

import com.lwh.jackknife.widget.HorizontalTabBar;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(METHOD)
@Retention(RUNTIME)
@EventBase(
        listenerSetter = "setOnTabClickListener",
        listenerType = HorizontalTabBar.OnTabClickListener.class,
        callbackMethod = "onTabClick",
        parameters = {
                View.class, int.class
        },
        parameterNames = {
                "view", "position"
        },
        returns = boolean.class
)
public @interface OnTabClick {
    int[] value() default {View.NO_ID};
}