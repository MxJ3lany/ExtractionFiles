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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.lwh.jackknife.widget.model.AwardModel;

import java.util.ArrayList;

public class LuckyWheelView extends View {

    private ArrayList<AwardModel> mAwards = new ArrayList<>();

    private Paint mItemTitleTextPaint;
    private Paint mItemContentTextPaint;
    private Paint mCircleLinePaint;
    private Paint mIntervalLinePaint;
    private Paint mUnSelectPiecePaint;
    private Paint mSelectedPiecePaint;

    private int mViewWidth;
    private int mViewHeight;

    private int mLastAngle = 0;
    private int mLastPos = 0;

    private int mRotationSpeed = 300;
    private boolean mRotationing = false;

    private int mItemTitleColor = Color.BLACK;
    private int mItemContentColor = Color.BLUE;
    private int mLineColor = Color.BLUE;
    private int mPieceUnSelectColor = Color.YELLOW;
    private int mPieceSelectedColor = Color.WHITE;

    private int mItemTitleTextSize = 70;
    private int mItemContentTextSize = 40;

    private int mIntervalStrokeWidth = 10;
    private int mCircleStrokeWidth = 20;

    public void setAwards(ArrayList<AwardModel> awards) {
        this.mAwards = awards;
    }

    public void setItemTitleColor(int itemTitleColor) {
        this.mItemTitleColor = itemTitleColor;
    }

    public void setItemContentColor(int itemContentColor) {
        this.mItemContentColor = itemContentColor;
    }

    public void setLineColor(int lineColor) {
        this.mLineColor = lineColor;
    }

    public void setPieceUnSelectColor(int pieceUnSelectColor) {
        this.mPieceUnSelectColor = pieceUnSelectColor;
    }

    public void setPieceSelectedColor(int pieceSelectedColor) {
        this.mPieceSelectedColor = pieceSelectedColor;
    }

    public void setItemTitleTextSize(int itemTitleTextSize) {
        this.mItemTitleTextSize = itemTitleTextSize;
    }

    public void setItemContentTextSize(int itemContentTextSize) {
        this.mItemContentTextSize = itemContentTextSize;
    }

    public void setIntervalStrokeWidth(int intervalStrokeWidth) {
        this.mIntervalStrokeWidth = intervalStrokeWidth;
    }

    public void setCircleStrokeWidth(int circleStrokeWidth) {
        this.mCircleStrokeWidth = circleStrokeWidth;
    }

    public void setRotationSpeed(int rotationSpeed) {
        this.mRotationSpeed = rotationSpeed;
    }

    public LuckyWheelView(Context context) {
        super(context);
        init();
    }

    public LuckyWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
        init();
    }

    public LuckyWheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
        init();
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.LuckyWheelView);
        mIntervalStrokeWidth = (int) array.getDimension(R.styleable.LuckyWheelView_interval_line_width, dp2px(getContext(), 5));
        mCircleStrokeWidth = (int) array.getDimension(R.styleable.LuckyWheelView_circle_line_width, dp2px(getContext(), 10));
        mItemTitleTextSize = (int) array.getDimension(R.styleable.LuckyWheelView_item_title_text_size, sp2px(getContext(), 15));
        mItemContentTextSize = (int) array.getDimension(R.styleable.LuckyWheelView_item_content_text_size, sp2px(getContext(), 15));
        mItemTitleColor = array.getColor(R.styleable.LuckyWheelView_item_title_color, Color.BLACK);
        mItemContentColor = array.getColor(R.styleable.LuckyWheelView_item_content_color, Color.BLACK);
        mLineColor = array.getColor(R.styleable.LuckyWheelView_line_color, Color.BLACK);
        mPieceUnSelectColor = array.getColor(R.styleable.LuckyWheelView_un_select_color, Color.YELLOW);
        mPieceSelectedColor = array.getColor(R.styleable.LuckyWheelView_selected_color, Color.WHITE);
        array.recycle();
    }

    private void init() {
        mItemTitleTextPaint = new Paint();
        mItemTitleTextPaint.setAntiAlias(true);
        mItemTitleTextPaint.setDither(true);
        mItemTitleTextPaint.setTextSize(mItemTitleTextSize);
        mItemTitleTextPaint.setColor(mItemTitleColor);

        mItemContentTextPaint = new Paint();
        mItemContentTextPaint.setAntiAlias(true);
        mItemContentTextPaint.setDither(true);
        mItemContentTextPaint.setTextSize(mItemContentTextSize);
        mItemContentTextPaint.setColor(mItemContentColor);

        mUnSelectPiecePaint = new Paint();
        mUnSelectPiecePaint.setAntiAlias(true);
        mUnSelectPiecePaint.setDither(true);
        mUnSelectPiecePaint.setColor(mPieceUnSelectColor);

        mSelectedPiecePaint = new Paint();
        mSelectedPiecePaint.setAntiAlias(true);
        mSelectedPiecePaint.setDither(true);
        mSelectedPiecePaint.setColor(mPieceSelectedColor);

        mIntervalLinePaint = new Paint();
        mIntervalLinePaint.setAntiAlias(true);
        mIntervalLinePaint.setDither(true);
        mIntervalLinePaint.setColor(mLineColor);
        mIntervalLinePaint.setStrokeWidth(mIntervalStrokeWidth);
        mIntervalLinePaint.setStyle(Paint.Style.STROKE);

        mCircleLinePaint = new Paint();
        mCircleLinePaint.setAntiAlias(true);
        mCircleLinePaint.setDither(true);
        mCircleLinePaint.setColor(mLineColor);
        mCircleLinePaint.setStrokeWidth(mCircleStrokeWidth);
        mCircleLinePaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mViewWidth = getMeasuredWidth();
        mViewHeight = getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (null != mAwards && mAwards.size() > 0) {
            RectF rectF = new RectF(0, 0, mViewWidth, mViewHeight);
            float outRadius = Math.min(mViewWidth / 2, mViewHeight / 2) - mCircleStrokeWidth / 2;
            float intRadius = Math.min(mViewWidth / 4, mViewHeight / 4);
            float ringWidth = outRadius - intRadius;
            RectF outRect = new RectF(mViewWidth / 2 - outRadius, mViewHeight / 2 - outRadius, outRadius + mViewWidth / 2, outRadius + mViewHeight / 2);
            canvas.drawCircle(mViewWidth / 2, mViewHeight / 2, outRadius - 2, mUnSelectPiecePaint);
            if (!mRotationing) {
                canvas.drawArc(outRect, 252, 36, true, mSelectedPiecePaint);
            }
            canvas.drawCircle(mViewWidth / 2, mViewHeight / 2, outRadius, mCircleLinePaint);
            int awardsSize = mAwards.size();
            int percentAngle = 360 / awardsSize;
            canvas.rotate(mLastAngle, mViewWidth / 2, mViewHeight / 2);
            canvas.save();
            for (int i = 0; i < awardsSize; i++) {
                if (i != 0) {
                    canvas.rotate(percentAngle, mViewWidth / 2, mViewHeight / 2);
                }
                canvas.drawLine((mViewWidth / 2 - outRadius), mViewHeight / 2, (mViewWidth / 2), mViewHeight / 2, mIntervalLinePaint);
                String awardTitleStr = mAwards.get(i).getTitle();
                String awardContentStr = mAwards.get(i).getContent();
                float titleWidth = mItemTitleTextPaint.measureText(awardTitleStr);
                float contentWidth = mItemTitleTextPaint.measureText(awardContentStr);
                canvas.drawText(awardTitleStr, mViewWidth / 2 - titleWidth / 2, mViewHeight / 2 - outRadius + ringWidth / 2, mItemTitleTextPaint);
                canvas.drawText(awardContentStr, mViewWidth / 2 - contentWidth / 2, mViewHeight / 2 - outRadius + ringWidth - 30, mItemTitleTextPaint);
            }
            canvas.restore();
        }
    }

    public void startPreSetRotation() {
        for (int i = 0; i < mAwards.size(); i++) {
            double randomResult = Math.random();
            if (randomResult < mAwards.get(i).getPercent()) {
                startRotation(mAwards.get(i).getIndex());
                return;
            } else {
                continue;
            }
        }
        startPreSetRotation();
    }

    public void startRotation(int pos) {
        if (mRotationing) {
            return;
        }
        if (pos < 0 || pos > mAwards.size()) {
            int newPos = (int) (Math.random() * 10);
            startRotation(newPos);
            return;
        }
        mRotationing = true;
        int lap = (int) (Math.random() * 12) + 4;
        int angle;
        final int currentPos = pos;
        if (currentPos > mLastPos) {
            angle = ((mAwards.size() - currentPos) + mLastPos) * 36;
        } else if (currentPos < mLastPos) {
            angle = (mLastPos - currentPos) * 36;
        } else {
            angle = 0;
        }
        int increaseDegree = lap * 360 + angle;
        long time = (lap + angle / 360) * mRotationSpeed;
        int DesRotate = increaseDegree + mLastAngle;
        ValueAnimator valueAnimator = ValueAnimator.ofInt(mLastAngle, DesRotate);
        valueAnimator.setDuration(time);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                mLastAngle = (value % 360 + 360) % 360;
                postInvalidate();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mRotationing = false;
                mLastPos = currentPos;
            }
        });
    }

    private int queryPosition() {
        mLastAngle = (mLastAngle % 360 + 360) % 360;
        int pos = mLastAngle / 36;
        return calcumAngle(pos);
    }

    private int calcumAngle(int pos) {
        if (pos >= 0 && pos <= 5) {
            pos = 5 - pos;
        } else {
            pos = (10 - pos) + 5;
        }
        return pos;
    }

    public static int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int px2dp(Context context, int px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, context.getResources().getDisplayMetrics());
    }

    public static int sp2px(Context context, int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    public static int px2sp(Context context, int px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, context.getResources().getDisplayMetrics());
    }
}
