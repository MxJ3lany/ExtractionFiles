/*
 * Copyright (C) 2019 Peter Gregus for GravityBox Project (C3C076@xda)
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

package com.ceco.pie.gravitybox;

import de.robv.android.xposed.XposedHelpers;

import android.graphics.drawable.Drawable;
import android.view.View;

public class StatusbarBattery {
    private static final String TAG = "GB:StatusbarBattery";
    private static final boolean DEBUG = false;

    private View mBattery;
    private Drawable mDrawable;

    public StatusbarBattery(View batteryView) {
        mBattery = batteryView;
    }

    public void destroy() {
        mDrawable = null;
        mBattery = null;
    }

    private Drawable getDrawable() {
        if (mDrawable == null) {
            try {
                mDrawable = (Drawable) XposedHelpers.getObjectField(mBattery, "mDrawable");
            } catch (Throwable t) {
                GravityBox.log(TAG, t);
            }
        }
        return mDrawable;
    }

    public void setVisibility(int visibility) {
        mBattery.setVisibility(visibility);
    }

    public void setShowPercentage(boolean showPercentage) {
        if (mBattery != null && getDrawable() != null) {
            try {
                XposedHelpers.setBooleanField(getDrawable(), "mShowPercent", showPercentage);
                try {
                    XposedHelpers.callMethod(getDrawable(), "postInvalidate");
                } catch (Throwable t) {
                    mBattery.postInvalidate();
                }
            } catch (Throwable t) {
                GravityBox.log(TAG, "Error setting percentage: ", t);
            }
        }
    }
}
