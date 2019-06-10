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

public class AlphaAction implements Action<AlphaAction> {

    private float mAlpha;

    public AlphaAction(float alpha) {
        if (alpha < 0.0f || alpha > 1.0f) {
            throw new RuntimeException("The alpha value is illegal.");
        }
        this.mAlpha = alpha;
    }

    public float getAlpha() {
        return mAlpha;
    }

    @Override
    public final Action add(AlphaAction action) {
        throw new UnsupportedOperationException("AlphaAction added leaf node does not support at present.");
    }

    @Override
    public final void startAnimation(View view, int duration) {
        throw new UnsupportedOperationException("AlphaAction does not hold the animation at present.");
    }
}
