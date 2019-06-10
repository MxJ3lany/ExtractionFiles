/*
 * Copyright © Yan Zhenjie. All Rights Reserved
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
package com.yanzhenjie.nohttp;

import android.os.Handler;
import android.os.Looper;

/**
 * <p>Poster.</p>
 * Created on 2016/6/7.
 *
 * @author Yan Zhenjie.
 */
public final class HandlerDelivery {

    private static HandlerDelivery instance;

    public static HandlerDelivery getInstance() {
        if (instance == null)
            synchronized (HandlerDelivery.class) {
                if (instance == null)
                    instance = new HandlerDelivery(new Handler(Looper.getMainLooper()));
            }
        return instance;
    }

    private Handler mHandler;

    private HandlerDelivery(Handler handler) {
        this.mHandler = handler;
    }

    public boolean post(Runnable r) {
        return mHandler.post(r);
    }

    public boolean postDelayed(Runnable r, long delayMillis) {
        return mHandler.postDelayed(r, delayMillis);
    }

    public boolean postAtFrontOfQueue(Runnable r) {
        return mHandler.postAtFrontOfQueue(r);
    }

    public boolean postAtTime(Runnable r, long uptimeMillis) {
        return mHandler.postAtTime(r, uptimeMillis);
    }

    public boolean postAtTime(Runnable r, Object token, long uptimeMillis) {
        return mHandler.postAtTime(r, token, uptimeMillis);
    }

    public void removeCallbacks(Runnable r) {
        mHandler.removeCallbacks(r);
    }

    public void removeCallbacks(Runnable r, Object token) {
        mHandler.removeCallbacks(r, token);
    }
}
