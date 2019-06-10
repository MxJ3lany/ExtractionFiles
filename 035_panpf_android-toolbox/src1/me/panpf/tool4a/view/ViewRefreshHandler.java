/*
 * Copyright (C) 2017 Peng fei Pan <sky@panpf.me>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.panpf.tool4a.view;

import android.os.Handler;
import android.view.View;

/**
 * 视图刷新处理器
 */
public class ViewRefreshHandler implements Runnable {
    private View view;
    private Handler handler;
    private int intervalMillis;

    public ViewRefreshHandler(View view, Handler handler, int intervalMillis) {
        this.view = view;
        this.handler = handler;
        this.intervalMillis = intervalMillis;
    }

    public ViewRefreshHandler(View view, Handler handler) {
        this(view, handler, 30);
    }

    public ViewRefreshHandler(View view, int intervalMillis) {
        this(view, new Handler(), intervalMillis);
    }

    public ViewRefreshHandler(View view) {
        this(view, new Handler(), 30);
    }

    @Override
    public void run() {
        view.invalidate();
        handler.postDelayed(this, intervalMillis);
    }

    public void start() {
        handler.removeCallbacks(this);
        handler.post(this);
    }

    public void stop() {
        handler.removeCallbacks(this);
        view.invalidate();
    }
}