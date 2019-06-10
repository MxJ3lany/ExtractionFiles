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

import android.animation.Animator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class InstrumentView extends View {

    private RectF mValidRect;
    private RectF mAnnulusRect;
    private TextPaint mTextPaint;
    private Paint mBackgroundPaint;
    private Paint mAnnulusPaint;
    private Paint mHoverAnnulusPaint;
    private String mTitle;
    private String mBody;
    private int mTitleTextColor;
    private int mBodyTextColor;
    private float mTitleTextSize;
    private float mBodyTextSize;
    private float mAnnulusWidth;
    private int mBackgroundColor;
    private int mAnnulusColor;
    private int mAnnulusHoverColor;
    private float mRatio;
    private int mAngle;
    private int mAnimationTime;

    public InstrumentView(Context context) {
        this(context, null);
    }

    public InstrumentView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.instrumentViewStyle);
    }

    public InstrumentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs, defStyleAttr);
        initPaints();
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.InstrumentView, defStyleAttr, 0);
        mTitle = a.getString(R.styleable.InstrumentView_instrumentview_title);
        mBody = a.getString(R.styleable.InstrumentView_instrumentview_body);
        mTitleTextColor = a.getColor(R.styleable.InstrumentView_instrumentview_titleTextColor, Color.BLACK);
        mBodyTextColor = a.getColor(R.styleable.InstrumentView_instrumentview_bodyTextColor, Color.BLACK);
        mTitleTextSize = a.getDimension(R.styleable.InstrumentView_instrumentview_titleTextSize,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15, getResources().getDisplayMetrics()));
        mBodyTextSize = a.getDimension(R.styleable.InstrumentView_instrumentview_bodyTextSize,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
        mAnnulusWidth = a.getDimension(R.styleable.InstrumentView_instrumentview_annulusWidth,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()));
        mBackgroundColor = a.getColor(R.styleable.InstrumentView_instrumentview_backgroundColor, Color.GRAY);
        mAnnulusColor = a.getColor(R.styleable.InstrumentView_instrumentview_annulusColor, Color.WHITE);
        mAnnulusHoverColor = a.getColor(R.styleable.InstrumentView_instrumentview_annulusHoverColor, Color.BLACK);
        mAnimationTime = a.getInt(R.styleable.InstrumentView_instrumentview_animationTime, 1000);
        a.recycle();
    }

    private void initPaints() {
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setDither(true);
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setDither(true);
        mBackgroundPaint.setColor(mBackgroundColor);
        mAnnulusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAnnulusPaint.setDither(true);
        mAnnulusPaint.setStyle(Paint.Style.STROKE);
        mAnnulusPaint.setStrokeCap(Paint.Cap.ROUND);
        mAnnulusPaint.setColor(mAnnulusColor);
        mAnnulusPaint.setStrokeWidth(mAnnulusWidth);
        mHoverAnnulusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHoverAnnulusPaint.setDither(true);
        mHoverAnnulusPaint.setStyle(Paint.Style.STROKE);
        mHoverAnnulusPaint.setStrokeCap(Paint.Cap.ROUND);
        mHoverAnnulusPaint.setColor(mAnnulusHoverColor);
        mHoverAnnulusPaint.setStrokeWidth(mAnnulusWidth);
    }

    private void initRects() {
        mValidRect = new RectF();
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        int min = Math.min(measuredWidth, measuredHeight);
        mValidRect.set(0, 0, measuredWidth, measuredHeight);
        if (measuredWidth > min) {
            mValidRect.left = (measuredWidth - min) / 2;
            mValidRect.right = mValidRect.left + min;
        }
        if (measuredHeight > min) {
            mValidRect.top = (measuredHeight - min) / 2;
            mValidRect.bottom = mValidRect.top + min;
        }
        mAnnulusRect = new RectF();
        mAnnulusRect.set(mValidRect.left + mAnnulusWidth / 2, mValidRect.top + mAnnulusWidth / 2,
                mValidRect.right - mAnnulusWidth / 2, mValidRect.bottom - mAnnulusWidth / 2);
    }

    public void setRatio(final float ratio) {
        ValueAnimator animator = ValueAnimator.ofObject(
                new AnimationEvaluator(),
                mRatio,
                ratio);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float ratio = (float) animation.getAnimatedValue();
                mAngle = (int) (ratio * 359);
                invalidate();
            }
        });
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(mAnimationTime).start();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mRatio = ratio;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    public void setTitle(String title) {
        this.mTitle = title;
        invalidate();
    }

    public void setBody(String body) {
        this.mBody = body;
        invalidate();
    }

    private class AnimationEvaluator implements TypeEvaluator<Float> {

        @Override
        public Float evaluate(float fraction, Float startValue, Float endValue) {
            if (endValue > startValue) {
                return startValue + fraction * (endValue - startValue);
            } else {
                return startValue - fraction * (startValue - endValue);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        initRects();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float r = mAnnulusRect.width() / 2;
        canvas.drawCircle(mAnnulusRect.left + r, mAnnulusRect.top + r, r, mBackgroundPaint);
        mTextPaint.setTextSize(mTitleTextSize);
        mTextPaint.setColor(mTitleTextColor);
        Paint.FontMetrics titleMetrics = mTextPaint.getFontMetrics();
        float titleBaselineY = mAnnulusRect.top + r / 2 + (titleMetrics.bottom - titleMetrics.top) / 2 - titleMetrics.bottom;
        canvas.drawText(mTitle, mAnnulusRect.left + r - mTextPaint.measureText(mTitle) / 2, titleBaselineY + mAnnulusWidth, mTextPaint);
        mTextPaint.setTextSize(mBodyTextSize);
        mTextPaint.setColor(mBodyTextColor);
        Paint.FontMetrics bodyMetrics = mTextPaint.getFontMetrics();
        float bodyBaselineY = mAnnulusRect.bottom - r / 2 + (bodyMetrics.bottom - bodyMetrics.top) / 2 - bodyMetrics.bottom;
        canvas.drawText(mBody, mAnnulusRect.left + r - mTextPaint.measureText(mBody) / 2, bodyBaselineY - mAnnulusWidth, mTextPaint);
        canvas.drawArc(mAnnulusRect, 0, 359, false, mAnnulusPaint);
        canvas.drawArc(mAnnulusRect, 90, mAngle, false, mHoverAnnulusPaint);
    }
}
