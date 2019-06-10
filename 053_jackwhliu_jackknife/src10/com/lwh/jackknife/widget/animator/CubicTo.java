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

public class CubicTo extends PathAction<CubicTo> {

    private float mInflectionX1;
    private float mInflectionY1;
    private float mInflectionX2;
    private float mInflectionY2;

    public CubicTo(float inflectionX1, float inflectionX2, float inflectionY1, float inflectionY2,
                   float x, float y) {
        super(x, y);
        this.mInflectionX1 = inflectionX1;
        this.mInflectionY1 = inflectionY1;
        this.mInflectionX2 = inflectionX2;
        this.mInflectionY2 = inflectionY2;
    }

    public float getInflectionX1() {
        return mInflectionX1;
    }

    public float getInflectionY1() {
        return mInflectionY1;
    }

    public float getInflectionX2() {
        return mInflectionX2;
    }

    public float getInflectionY2() {
        return mInflectionY2;
    }
}
