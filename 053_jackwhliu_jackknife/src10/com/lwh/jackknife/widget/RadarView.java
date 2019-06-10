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
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

public class RadarView extends View {

    private float mCircleRadius;
    private float mSpaceRadius;
    private int mAnnulusColor;
    private int mRadarColor;
    private int mAnnulusNum = 10;
    private float mAnnulusWidth;
    private Paint mAnnulusPaint;
    private Paint mRadarPaint;
    private DisplayMetrics mMetrics;
    private float mCircleCenterX;
    private float mCircleCenterY;
    private Matrix mMatrix;
    private float mDegree;
    private boolean mRunning;
    private RadarRunnable mRadarRunnable;
    private RotateRate mRotateRate;
    private boolean mReverse;
    private int mRange = 1;
    private Drawable mBackgroundDrawable;

    public enum RotateRate {
        SLOW, FAST
    }

    public RadarView(Context context) {
        this(context, null);
    }

    public RadarView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.radarViewStyle);
    }

    public RadarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBackgroundDrawable = getBackground();
        mMetrics = getResources().getDisplayMetrics();
        mMatrix = new Matrix();
        mRadarRunnable = new RadarRunnable();
        initAttrs(context, attrs, defStyleAttr);
        initPaints();
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        mCircleRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, mMetrics);
        mSpaceRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, mMetrics);
        mAnnulusWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, mMetrics);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RadarView, defStyleAttr, 0);
        mRadarColor = a.getColor(R.styleable.RadarView_radarview_radarColor, Color.GRAY);
        mCircleRadius = a.getDimension(R.styleable.RadarView_radarview_circleRadius, mCircleRadius);
        mSpaceRadius = a.getDimension(R.styleable.RadarView_radarview_spaceRadius, mSpaceRadius);
        mAnnulusNum = a.getInt(R.styleable.RadarView_radarview_annulusNum, mAnnulusNum);
        mAnnulusWidth = a.getDimension(R.styleable.RadarView_radarview_annulusWidth, mAnnulusWidth);
        mAnnulusColor = a.getColor(R.styleable.RadarView_radarview_annulusColor, Color.GRAY);
        int rotateRateIndex = a.getInt(R.styleable.RadarView_radarview_rotateRate, 1);
        switch (rotateRateIndex) {
            case 0:
                mRotateRate = RotateRate.SLOW;
                break;
            case 1:
                mRotateRate = RotateRate.FAST;
                break;
        }
        mReverse = a.getBoolean(R.styleable.RadarView_radarview_reverse, false);
        a.recycle();
    }

    private void initPaints() {
        mAnnulusPaint = new Paint();
        mAnnulusPaint.setAntiAlias(true);
        mAnnulusPaint.setDither(true);
        mAnnulusPaint.setStyle(Paint.Style.STROKE);
        mAnnulusPaint.setStrokeWidth(mAnnulusWidth);
        mAnnulusPaint.setColor(mAnnulusColor);
        mRadarPaint = new Paint();
        mRadarPaint.setColor(mRadarColor);
        mRadarPaint.setAntiAlias(true);
        mRadarPaint.setDither(true);
        mRadarPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        mCircleCenterX = width / 2;
        mCircleCenterY = height / 2;
        for (int i = 0; i < mAnnulusNum; i++) {
            canvas.drawCircle(mCircleCenterX, mCircleCenterY, mCircleRadius + i * mSpaceRadius,
                    mAnnulusPaint);
        }
        if (mRunning) {
            int a = Color.alpha(mRadarColor) / 2;
            int r = Color.red(mRadarColor);
            int g = Color.green(mRadarColor);
            int b = Color.blue(mRadarColor);
            Shader shader;
            if (!mReverse) {
                shader = new SweepGradient(mCircleCenterX, mCircleCenterY, Color.argb(a, r, g, b), mRadarColor);
            } else {
                shader = new SweepGradient(mCircleCenterX, mCircleCenterY, mRadarColor, Color.argb(a, r, g, b));
            }
            mRadarPaint.setShader(shader);
            canvas.concat(mMatrix);
            canvas.drawCircle(mCircleCenterX, mCircleCenterY, mCircleRadius + mAnnulusNum * mSpaceRadius,
                    mRadarPaint);
        } else {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            if (mBackgroundDrawable != null) {
                mBackgroundDrawable.draw(canvas);
            } else {
                canvas.drawColor(Color.WHITE);
            }
            for (int i = 0; i < mAnnulusNum; i++) {
                canvas.drawCircle(mCircleCenterX, mCircleCenterY, mCircleRadius + i * mSpaceRadius,
                        mAnnulusPaint);
            }
        }
    }

    public void start() {
        if (mRunning) {
            return;
        }
        mRunning = true;
        new Thread(mRadarRunnable).start();
    }

    public void stop() {
        mRunning = false;
        mDegree = 0;
        mMatrix.reset();
        invalidateView();
    }

    public float getCircleRadius() {
        return mCircleRadius;
    }

    public void setCircleRadius(float radius) {
        if (radius != mCircleRadius) {
            this.mCircleRadius = radius;
            invalidateView();
        }
    }

    public float getSpaceRadius() {
        return mSpaceRadius;
    }

    public void setSpaceRadius(float radius) {
        if (radius != mSpaceRadius) {
            this.mSpaceRadius = radius;
            invalidateView();
        }
    }

    public int getAnnulusColor() {
        return mAnnulusColor;
    }

    public void setAnnulusColor(int color) {
        if (color != mAnnulusColor) {
            this.mAnnulusColor = color;
            invalidateView();
        }
    }

    public void setAnnulusColorResource(int resId) {
        setAnnulusColor(getResources().getColor(resId));
    }

    public int getRadarColor() {
        return mRadarColor;
    }

    public void setRadarColorResource(int resId) {
        setRadarColor(getResources().getColor(resId));
    }

    public void setRadarColor(int color) {
        if (color != mRadarColor) {
            this.mRadarColor = color;
            invalidateView();
        }
    }

    public int getAnnulusNum() {
        return mAnnulusNum;
    }

    public void setAnnulusNum(int num) {
        if (num != mAnnulusNum) {
            this.mAnnulusNum = num;
            invalidateView();
        }
    }

    public float getAnnulusWidth() {
        return mAnnulusWidth;
    }

    public void setAnnulusWidth(float width) {
        if (width != mAnnulusWidth) {
            this.mAnnulusWidth = width;
            invalidateView();
        }
    }

    public RotateRate getRotateRate() {
        return mRotateRate;
    }

    public void setRotateRate(RotateRate rate) {
        if (rate != mRotateRate) {
            this.mRotateRate = rate;
            invalidateView();
        }
    }

    public boolean isReverse() {
        return mReverse;
    }

    public void setReverse(boolean reverse) {
        if (reverse != mReverse) {
            this.mReverse = reverse;
            invalidateView();
        }
    }

    public int getRange() {
        return mRange;
    }

    public void setRange(int range) {
        if (range != mRange) {
            this.mRange = range;
            invalidateView();
        }
    }

    public boolean isRotating() {
        return mRunning;
    }

    private class RadarRunnable implements Runnable {

        @Override
        public void run() {
            while (mRunning) {
                mMatrix.setRotate(mDegree, mCircleCenterX, mCircleCenterY);
                if (mRotateRate == RotateRate.SLOW) {
                    mRange = 1;
                }
                if (mRotateRate == RotateRate.FAST) {
                    mRange = 30;
                }
                if (!mReverse) {
                    mDegree += mRange;
                } else {
                    mDegree -= mRange;
                }
                invalidateView();
                SystemClock.sleep(100);
            }
        }
    }

    /**
     * The refresh view operation of the automatic processing thread.
     */
    private void invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }
}
