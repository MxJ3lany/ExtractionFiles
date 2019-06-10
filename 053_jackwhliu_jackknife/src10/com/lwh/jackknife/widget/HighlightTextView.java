/*
 * Copyright (C) 2017 The JackKnife Open Source Project
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

package com.lwh.jackknife.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Shader;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

public class HighlightTextView extends TextView {

    private TextPaint mTextPaint;
    private LinearGradient mGradient;
    private Matrix mMatrix;
    private float mTransX;
    private float mDeltaX = 20;
    private int mDefaultColor = 0x00000000;
    private int mHighlightColor = 0x22000000;

    public HighlightTextView(Context context) {
        this(context, null);
    }

    public HighlightTextView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.highlightTextViewStyle);
    }

    public HighlightTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        initAttrs(context, attrs, defStyleAttr);
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HighlightTextView, defStyleAttr, 0);
        mDefaultColor = a.getColor(R.styleable.HighlightTextView_highlighttextview_defaultColor, mDefaultColor);
        mHighlightColor = a.getColor(R.styleable.HighlightTextView_highlighttextview_highlightColor, mHighlightColor);
        a.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTextPaint = getPaint();
        String text = getText().toString();
        float textWidth = mTextPaint.measureText(text);
        int GradientSize = (int) (3 * textWidth / text.length());
        mGradient = new LinearGradient(-GradientSize, 0, 0, 0,
                new int[]{mDefaultColor, mHighlightColor, mDefaultColor}, new float[]{0, 0.5f, 1},
                Shader.TileMode.CLAMP);
        mTextPaint.setShader(mGradient);
        mMatrix = new Matrix();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float textWidth = getPaint().measureText(getText().toString());
        mTransX += mDeltaX;
        if (mTransX > textWidth + 1 || mTransX < 1) {
            mDeltaX = -mDeltaX;
        }
        mMatrix.setTranslate(mTransX, 0);
        mGradient.setLocalMatrix(mMatrix);
        postInvalidateDelayed(50);
    }
}
