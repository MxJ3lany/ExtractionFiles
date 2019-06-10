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

package com.lwh.jackknife.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class AOPHandler implements InvocationHandler {

    private Action mAction;
    private AOPInterceptor mInterceptor;

    public AOPHandler() {
        this.mInterceptor = new DefaultAOPInterceptor();
    }

    public AOPHandler(AOPInterceptor interceptor) {
        this.mInterceptor = interceptor;
    }

    public Action bind(Action action) {
        this.mAction = action;
        Class<?> callbackType = action.getClass();
        return (Action) Proxy.newProxyInstance(callbackType.getClassLoader(),
                new Class<?>[]{callbackType}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        mInterceptor.beforeAction();
        Object returnValue = method.invoke(proxy, args);
        mInterceptor.afterAction();
        return returnValue;
    }
}
