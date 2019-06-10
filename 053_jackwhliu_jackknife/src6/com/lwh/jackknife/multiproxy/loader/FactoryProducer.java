/*
 * Copyright (C) 2019 The JackKnife Open Source Project
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

package com.lwh.jackknife.multiproxy.loader;

import com.lwh.jackknife.multiproxy.DefaultDecoratorFactory;
import com.lwh.jackknife.multiproxy.interfaces.DecoratorFactory;

import java.util.HashMap;
import java.util.Map;

public class FactoryProducer {

    private static Map<String, DecoratorFactory> sInjectCache = new HashMap<>();

    public static DecoratorFactory getFactory(Class<?> targetClazz) {
        String $className = targetClazz.getName() + "$Factory";
        DecoratorFactory factory = sInjectCache.get($className);
        if (factory != null) {
            return factory;
        }
        try {
            Class<?> factoryClazz = Class.forName($className);
            factory = (DecoratorFactory) factoryClazz.newInstance();
            sInjectCache.put($className, factory);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return factory == null ? new DefaultDecoratorFactory() : factory;
    }
}
