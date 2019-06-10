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

package com.lwh.jackknife.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

public class GradientTextView extends TextView {

    private LinearGradient linearGradient;
    private Paint mPaint;
    private int viewWidth;
    private Matrix mMatrix;
    private int bottomColor;
    private int topColor;
    private boolean forward = true;
    private int animationTime = 700;

    public GradientTextView(Context context) {
        super(context);
        init();
    }

    public GradientTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GradientTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = getPaint();
        mMatrix = new Matrix();
        bottomColor = getResources().getColor(R.color.pink);
        topColor = getResources().getColor(R.color.yellow);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = getMeasuredWidth();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = getMeasuredWidth();
        linearGradient = new LinearGradient(-viewWidth, 0, 0, 0, topColor, bottomColor, Shader.TileMode.CLAMP);
        if (mPaint.getShader() == null) {
            mPaint.setShader(linearGradient);
        }
    }

    public void setAnimationTime(int animationTime) {
        this.animationTime = animationTime;
    }

    public void setTopColorRes(int topColorId, int bottomColorId) {
        topColor = getResources().getColor(topColorId);
        bottomColor = getResources().getColor(bottomColorId);

        linearGradient = new LinearGradient(-viewWidth, 0, 0, 0, topColor, bottomColor, Shader.TileMode.CLAMP);
        mPaint.setShader(linearGradient);

        postInvalidate();
    }

    public void setInit() {
        mMatrix = new Matrix();
        mPaint.setShader(null);
        linearGradient = new LinearGradient(-viewWidth, 0, 0, 0, topColor, bottomColor, Shader.TileMode.CLAMP);
        mPaint.setShader(linearGradient);
        postInvalidate();
    }

    public void setDone() {
        mMatrix = new Matrix();
        mMatrix.setTranslate(2 * viewWidth, 0);
        mPaint.setShader(null);
        linearGradient = new LinearGradient(-viewWidth, 0, 0, 0, topColor, bottomColor, Shader.TileMode.CLAMP);
        mPaint.setShader(linearGradient);
        postInvalidate();
    }

    public void startAnimation() {
        ValueAnimator valueAnimator;
        if (forward) {
            valueAnimator = ValueAnimator.ofFloat(0, 1);
            forward = false;
        } else {
            valueAnimator = ValueAnimator.ofFloat(1, 0);
            forward = true;
        }
        valueAnimator.setDuration(animationTime);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mMatrix.setTranslate(2 * value * viewWidth, 0);
                postInvalidate();
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        linearGradient.setLocalMatrix(mMatrix);
    }
}
