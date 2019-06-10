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

package com.lwh.jackknife.ioc2;

import com.lwh.jackknife.ioc2.adapter.InjectAdapter;
import com.lwh.jackknife.ioc2.adapter.NullAdapter;

import java.util.HashMap;
import java.util.Map;

public class ViewInjector {

    static Map<Class<?>, InjectAdapter> mInjectCache = new HashMap<>();

    public static String SUFFIX = "$InjectAdapter";

    public static Object inject(Object target) {
        return getViewAdapter(target.getClass()).inject(target);
    }

    private static InjectAdapter getViewAdapter(Class<?> viewClass) {
        InjectAdapter adapter = mInjectCache.get(viewClass);
        if (adapter != null) {
            return adapter;
        }
        String $className = viewClass.getName() + SUFFIX;
        try {
            Class<?> adapterClass = Class.forName($className);
            adapter = (InjectAdapter) adapterClass.newInstance();
            mInjectCache.put(viewClass, adapter);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return adapter == null ? new NullAdapter() : adapter;
    }
}
