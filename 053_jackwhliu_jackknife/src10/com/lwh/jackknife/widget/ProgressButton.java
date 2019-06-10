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

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.Button;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ProgressButton extends Button {

    /**
     * The paint to draw background.
     */
    private Paint mBackgroundPaint;

    /**
     * The paint to draw the text in the middle of the button.
     */
    private TextPaint mTextPaint;

    /**
     * The size of the rounded corners of the button.
     */
    private int mCornerRadius;

    /**
     * The gap between the animated balls at the time of loading.
     */
    private float mBallSpacing;

    /**
     * The radius of the animated balls.
     */
    private float mBallRadius;

    /**
     * To make a decision to display the border of the button.
     */
    private boolean mShowBorder;

    /**
     * The width of the button border.
     */
    private int mBorderWidth;

    /**
     * The text in the middle of the button.
     */
    private String mText;

    /**
     * The text size in the middle of the button.
     */
    private float mTextSize;

    /**
     * The color of the text in the middle of the button.
     */
    private int mTextColor;

    /**
     * The hover color of the text in the middle of the button.
     */
    private int mHoverTextColor;

    /**
     * The color of the button's default state，and when it s filled.
     */
    private int mBackgroundColor;

    /**
     * The bounds of the button's background.
     */
    private RectF mBackgroundRect;

    /**
     * The background color in the bottom layer.
     */
    private int mSecondaryBackgroundColor;

    /**
     * The progress of the button when performing the task.
     */
    private float mProgress;

    /**
     * The progress percent of the button when performing the task.
     */
    private float mProgressPercent;

    /**
     * The target progress of the button when performing the task.
     */
    private float mTargetProgress;

    /**
     * The maximum progress of the button.
     */
    private int mMaxProgress;

    /**
     * The minimum progress of the button.
     */
    private int mMinProgress;

    /**
     * The right border of the text in the middle of the button.
     */
    private float mTextRightBorder;

    /**
     * The bottom border of the text in the middle of the button.
     */
    private float mTextBottomBorder;

    /**
     * It is used to intersect the background and the color of the text.
     */
    private LinearGradient mTextGradient;

    /**
     * The state when the button defaults.
     */
    public static final int STATE_NORMAL = 0;

    /**
     * The state when the button performs the task.
     */
    public static final int STATE_PENDING = 1;

    /**
     * The state when the button suspends execution of the task.
     */
    public static final int STATE_PAUSE = 2;

    /**
     * The state when the button's task is completed.
     */
    public static final int STATE_FINISH = 3;

    /**
     * The state of the button.
     *
     * @see #STATE_NORMAL
     * @see #STATE_PENDING
     * @see #STATE_PAUSE
     * @see #STATE_FINISH
     */
    private int mState;

    private ArrayList<ValueAnimator> mAnimators;

    /**
     * For pulse animation.
     */
    private ValueAnimator mProgressAnimator;

    public static final float SCALE = 1.0f;

    private float[] mScales = new float[]{SCALE, SCALE, SCALE};

    private float[] mTranslateYs = new float[3];

    public ProgressButton(Context context) {
        this(context, null);
    }

    public ProgressButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.progressButtonStyle);
    }

    public ProgressButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            initAttrs(context, attrs, defStyleAttr);
            initPaints();
            initAnimations();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                setLayerType(LAYER_TYPE_SOFTWARE, mTextPaint);
            }
            reset();
        }
    }

    public void reset() {
        mMinProgress = 0;
        mMaxProgress = 100;
        mProgress = 0;
        mState = STATE_NORMAL;
        invalidate();
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressButton,
                defStyleAttr, 0);
        mCornerRadius = a.getDimensionPixelOffset(R.styleable
                .ProgressButton_progressbutton_cornerRadius, 0);
        mShowBorder = a.getBoolean(R.styleable.ProgressButton_progressbutton_showBorder, false);
        mBorderWidth = a.getDimensionPixelOffset(R.styleable
                .ProgressButton_progressbutton_borderWidth, 2);
        mTextSize = a.getDimensionPixelSize(R.styleable.ProgressButton_progressbutton_textSize, 50);
        mTextColor = a.getColor(R.styleable.ProgressButton_progressbutton_textColor, Color.BLACK);
        mHoverTextColor = a.getColor(R.styleable.ProgressButton_progressbutton_hoverTextColor,
                Color.WHITE);
        mBackgroundColor = a.getColor(R.styleable.ProgressButton_progressbutton_backgroundColor,
                Color.BLACK);
        mSecondaryBackgroundColor = a.getColor(R.styleable
                .ProgressButton_progressbutton_secondaryBackgroundColor, Color.GRAY);
        mBallRadius = a.getDimensionPixelOffset(R.styleable
                .ProgressButton_progressbutton_ballRadius, 6);
        mBallSpacing = a.getDimensionPixelOffset(R.styleable
                .ProgressButton_progressbutton_ballSpacing, 4);
        a.recycle();
    }

    private void initPaints() {
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setDither(true);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setDither(true);
        mTextPaint.setTextSize(mTextSize);
    }

    private void initAnimations() {
        mProgressAnimator = ValueAnimator.ofFloat(0, 1).setDuration(500);
        mProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float ratio = (float) animation.getAnimatedValue();
                mProgress += ((mTargetProgress - mProgress) * ratio);
                invalidate();
            }
        });
        mAnimators = createBallPulseAnimators();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isInEditMode()) {
            drawBackground(canvas);
            drawText(canvas);
        }
    }

    private void drawBackground(Canvas canvas) {
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        mBackgroundRect = new RectF();
        int left = mShowBorder ? mBorderWidth : 0;
        int top = mShowBorder ? mBorderWidth : 0;
        int right = measuredWidth - (mShowBorder ? mBorderWidth : 0);
        int bottom = measuredHeight - (mShowBorder ? mBorderWidth : 0);
        mBackgroundRect.set(left, top, right, bottom);
        if (mShowBorder) {
            mBackgroundPaint.setStyle(Paint.Style.STROKE);
            mBackgroundPaint.setColor(mBackgroundColor);
            mBackgroundPaint.setStrokeWidth(mBorderWidth);
            canvas.drawRoundRect(mBackgroundRect, mCornerRadius, mCornerRadius, mBackgroundPaint);
        }
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        switch (mState) {
            case STATE_NORMAL:
                mBackgroundPaint.setColor(mBackgroundColor);
                canvas.drawRoundRect(mBackgroundRect, mCornerRadius, mCornerRadius, mBackgroundPaint);
                break;
            case STATE_PAUSE:
            case STATE_PENDING:
                mProgressPercent = mProgress / mMaxProgress;
                mBackgroundPaint.setColor(mSecondaryBackgroundColor);
                canvas.save();
                canvas.drawRoundRect(mBackgroundRect, mCornerRadius, mCornerRadius, mBackgroundPaint);
                mBackgroundPaint.setColor(mBackgroundColor);
                mBackgroundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
                float rightGap = mBackgroundRect.right * mProgressPercent;
                canvas.drawRect(mBackgroundRect.left, mBackgroundRect.top, rightGap,
                        mBackgroundRect.bottom, mBackgroundPaint);
                canvas.restore();
                mBackgroundPaint.setXfermode(null);
                break;
            case STATE_FINISH:
                mBackgroundPaint.setColor(mBackgroundColor);
                canvas.drawRoundRect(mBackgroundRect, mCornerRadius, mCornerRadius, mBackgroundPaint);
                break;
        }
    }

    private void drawText(Canvas canvas) {
        float y = canvas.getHeight() / 2 - ((mTextPaint.descent()) / 2 + mTextPaint.ascent() / 2);
        int measuredWidth = getMeasuredWidth();
        if (mText == null) {
            mText = "";
        }
        float textWidth = mTextPaint.measureText(mText);
        mTextBottomBorder = y;
        mTextRightBorder = (measuredWidth + textWidth) / 2;
        switch (mState) {
            case STATE_NORMAL:
                mTextPaint.setShader(null);
                mTextPaint.setColor(mHoverTextColor);
                canvas.drawText(mText, (measuredWidth - textWidth) / 2, y, mTextPaint);
                break;
            case STATE_PAUSE:
            case STATE_PENDING:
                float hoverWidth = measuredWidth * mProgressPercent;
                float startIndicator = (measuredWidth - textWidth) / 2;
                float endIndicator = (measuredWidth + textWidth) / 2;
                float hoverTextWidth = (textWidth - measuredWidth) / 2 + hoverWidth;
                float textProgress = hoverTextWidth / textWidth;
                if (hoverWidth <= startIndicator) {
                    mTextPaint.setShader(null);
                    mTextPaint.setColor(mTextColor);
                } else if (startIndicator < hoverWidth && hoverWidth <= endIndicator) {
                    mTextGradient = new LinearGradient((measuredWidth - textWidth) / 2, 0,
                            (measuredWidth + textWidth) / 2, 0, new int[]{mHoverTextColor,
                            mTextColor}, new float[]{textProgress, textProgress + 0.001f},
                            Shader.TileMode.CLAMP);
                    mTextPaint.setColor(mTextColor);
                    mTextPaint.setShader(mTextGradient);
                } else {
                    mTextPaint.setShader(null);
                    mTextPaint.setColor(mHoverTextColor);
                }
                canvas.drawText(mText, (measuredWidth - textWidth) / 2, y, mTextPaint);
                break;
            case STATE_FINISH:
                mTextPaint.setColor(mHoverTextColor);
                canvas.drawText(mText, (measuredWidth - textWidth) / 2, y, mTextPaint);
                drawLoadingBall(canvas);
                break;
        }
    }

    private void drawLoadingBall(Canvas canvas) {
        for (int i = 0; i < 3; i++) {
            canvas.save();
            float translateX = mTextRightBorder + 10 + (mBallRadius * 2) * i + mBallSpacing * i;
            canvas.translate(translateX, mTextBottomBorder);
            canvas.drawCircle(0, mTranslateYs[i], mBallRadius * mScales[i], mTextPaint);
            canvas.restore();
        }
    }

    private void startAnimators() {
        for (int i = 0; i < mAnimators.size(); i++) {
            ValueAnimator animator = mAnimators.get(i);
            animator.start();
        }
    }

    private void stopAnimators() {
        if (mAnimators != null) {
            for (ValueAnimator animator : mAnimators) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    if (animator != null && animator.isStarted()) {
                        animator.end();
                    }
                } else {
                    if (animator != null && animator.isRunning()) {
                        animator.end();
                    }
                }
            }
        }
    }

    public ArrayList<ValueAnimator> createBallPulseAnimators() {
        ArrayList<ValueAnimator> animators = new ArrayList<>();
        int[] delays = new int[]{120, 240, 360};
        for (int i = 0; i < 3; i++) {
            final int index = i;
            ValueAnimator scaleAnim = ValueAnimator.ofFloat(1, 0.3f, 1);
            scaleAnim.setDuration(750);
            scaleAnim.setRepeatCount(-1);
            scaleAnim.setStartDelay(delays[i]);
            scaleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mScales[index] = (float) animation.getAnimatedValue();
                    postInvalidate();
                }
            });
            animators.add(scaleAnim);
        }
        return animators;
    }

    public int getState() {
        return mState;
    }

    public void setState(int state) {
        if (mState != state) {
            this.mState = state;
            invalidate();
            if (state == STATE_FINISH) {
                startAnimators();
            } else {
                stopAnimators();
            }
        }
    }

    public void setText(String text) {
        mText = text;
        invalidate();
    }

    public String getText() {
        return mText;
    }

    public int getMaxProgress() {
        return mMaxProgress;
    }

    public void setMaxProgress(int progress) {
        this.mMaxProgress = progress;
    }

    public int getMinProgress() {
        return mMinProgress;
    }

    public void setMinProgress(int progress) {
        this.mMinProgress = progress;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int color) {
        this.mTextColor = color;
    }

    public int getHoverTextColor() {
        return mHoverTextColor;
    }

    public void setHoverTextColor(int color) {
        this.mHoverTextColor = color;
    }

    public int getCornerRadius() {
        return mCornerRadius;
    }

    public void setCornerRadius(int radius) {
        this.mCornerRadius = radius;
    }

    public boolean isShowBorder() {
        return mShowBorder;
    }

    public void setShowBorder(boolean isShowBorder) {
        this.mShowBorder = isShowBorder;
    }

    public int getBorderWidth() {
        return mBorderWidth;
    }

    public void setBorderWidth(int width) {
        this.mBorderWidth = width;
    }

    public float getProgress() {
        return mProgress;
    }

    public void setProgress(float progress) {
        this.mProgress = progress;
    }

    public void setProgressText(String text, float progress) {
        if (progress >= mMinProgress && progress <= mMaxProgress) {
            DecimalFormat format = new DecimalFormat("##0.0");
            mText = text + format.format(progress) + "%";
            mTargetProgress = progress;
            if (mProgressAnimator.isRunning()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mProgressAnimator.resume();
                }
                mProgressAnimator.start();
            } else {
                mProgressAnimator.start();
            }
        } else if (progress < mMinProgress) {
            mProgress = 0;
        } else if (progress > mMaxProgress) {
            mProgress = 100;
            mText = text + progress + "%";
            invalidate();
        }
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mState = savedState.state;
        mProgress = savedState.progress;
        mText = savedState.text;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, (int) mProgress, mState, mText);
    }

    public static class SavedState extends BaseSavedState {

        private int progress;
        private int state;
        private String text;

        public SavedState(Parcelable parcel, int progress, int state, String text) {
            super(parcel);
            this.progress = progress;
            this.state = state;
            this.text = text;
        }

        private SavedState(Parcel in) {
            super(in);
            progress = in.readInt();
            state = in.readInt();
            text = in.readString();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(progress);
            out.writeInt(state);
            out.writeString(text);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
