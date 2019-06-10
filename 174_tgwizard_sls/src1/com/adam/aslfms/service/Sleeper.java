/**
 * This file is part of Simple Last.fm Scrobbler.
 * <p>
 * https://github.com/tgwizard/sls
 * <p>
 * Copyright 2011 Simple Last.fm Scrobbler Team
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.adam.aslfms.service;

import android.content.Context;
import android.util.Log;

public class Sleeper extends NetRunnable {

    private static final String TAG = "Sleeper";

    // TODO: correct value
    private static final long START_TIME = 60 * 1000; // 1 min
    private static final long MAX_TIME = 32 * 60 * 1000; // 32 min. every half hour seems ok.

    private long mSleepTime;

    public Sleeper(NetApp napp, Context ctx, Networker net) {
        super(napp, ctx, net);
        reset();
    }

    public void reset() {
        synchronized (this) {
            mSleepTime = START_TIME;
            this.notifyAll(); // if we were waiting, which we probably wasn't
        }
    }

    private void incSleepTime() {
        synchronized (this) {
            mSleepTime *= 2;
            if (mSleepTime > MAX_TIME) {
                mSleepTime = MAX_TIME;
            }
        }
    }

    @Override
    public void run() {
        synchronized (this) {
            try {
                Log.d(TAG, "start sleeping: " + mSleepTime + ": "
                        + getNetApp().getName());
                this.wait(mSleepTime);
                Log.d(TAG, "woke up sleeping: " + getNetApp().getName());
            } catch (InterruptedException e) {
                Log.i(TAG, "Got interrupted: " + getNetApp().getName());
                Log.i(TAG, e.getMessage());
            }
            incSleepTime();
        }
    }

}
