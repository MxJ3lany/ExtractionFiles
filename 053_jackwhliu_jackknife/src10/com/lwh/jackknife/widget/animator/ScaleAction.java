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

package com.lwh.jackknife.widget.animator;

import android.view.View;

public class ScaleAction implements Action<ScaleAction> {

    private float mScaleX;
    private float mScaleY;

    public ScaleAction(float scaleX, float scaleY) {
        this.mScaleX = scaleX;
        this.mScaleY = scaleY;
    }

    public float getScaleX() {
        return mScaleX;
    }

    public float getScaleY() {
        return mScaleY;
    }

    @Override
    public final Action add(ScaleAction action) {
        throw new UnsupportedOperationException("ScaleAction added leaf node does not support at present.");
    }

    @Override
    public final void startAnimation(View view, int duration) {
        throw new UnsupportedOperationException("ScaleAction does not hold the animation at present.");
    }
}
