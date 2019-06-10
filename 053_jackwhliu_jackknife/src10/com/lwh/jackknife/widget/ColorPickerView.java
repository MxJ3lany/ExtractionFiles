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
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ColorPickerView extends View {

    private Paint mRightPaint;

    private int mHeight;

    private int mWidth;

    private int[] mRightColors;

    private int mLeftWidth;

    private int mRightWidth;

    private Bitmap mLeftNormalBitmap;

    private Bitmap mLeftPressedBitmap;

    private Bitmap mRightNormalBitmap;

    private Bitmap mRightPressedBitmap;

    private Paint mBitmapPaint;

    private final int mSplitWidth = 24;

    private boolean mDownInLeft;

    private boolean mDownInRight;

    private PointF mLeftSelectPoint;

    private PointF mRightSelectPoint;

    private OnColorChangedListener mOnColorChangedListener;

    private boolean mLeftMove;

    private boolean mRightMove;

    private float mLeftBitmapRadius;

    private Bitmap mGradualChangeBitmap;

    private float mRightBitmapHalfHeight;

    private float mRightBitmapQuarterWidth;

    private int mCallbackColor = Integer.MAX_VALUE;

    public ColorPickerView(Context context) {
        this(context, null);
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setOnColorChangedListenner(OnColorChangedListener l) {
        mOnColorChangedListener = l;
    }

    private void init() {
        mRightPaint = new Paint();
        mRightPaint.setStyle(Paint.Style.FILL);
        mRightPaint.setStrokeWidth(1);
        mRightColors = new int[3];
        mRightColors[0] = Color.WHITE;
        mRightColors[2] = Color.BLACK;
        mBitmapPaint = new Paint();
        mLeftNormalBitmap = BitmapFactory.decodeResource(getContext().getResources(),
                R.drawable.jknf_plus);
        mLeftPressedBitmap = BitmapFactory.decodeResource(getContext().getResources(),
                R.drawable.jknf_plus_pressed);
        mLeftBitmapRadius = mLeftNormalBitmap.getWidth() / 2;
        mLeftSelectPoint = new PointF(mSplitWidth, mSplitWidth);
        mRightNormalBitmap = BitmapFactory.decodeResource(getContext().getResources(),
                R.drawable.jknf_minus);
        mRightPressedBitmap = BitmapFactory.decodeResource(getContext().getResources(),
                R.drawable.jknf_minus_pressed);
        mRightSelectPoint = new PointF(mSplitWidth, mSplitWidth);
        mRightBitmapHalfHeight = mRightNormalBitmap.getHeight() / 2;
        mRightBitmapQuarterWidth = mRightNormalBitmap.getWidth() / 4;
        mRightWidth = mRightNormalBitmap.getWidth() / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(getGradualBitmap(), null, new Rect(mSplitWidth, mSplitWidth, mLeftWidth
                + mSplitWidth, mHeight - mSplitWidth), mBitmapPaint);
        mRightColors[1] = mRightPaint.getColor();
        Shader rightShader = new LinearGradient(mWidth - mSplitWidth - mRightWidth / 2, mSplitWidth,
                mWidth - mSplitWidth - mRightWidth / 2, mHeight - mSplitWidth, mRightColors, null,
                TileMode.MIRROR);
        mRightPaint.setShader(rightShader);
        canvas.drawRect(new Rect(mWidth - mSplitWidth - mRightWidth, mSplitWidth, mWidth
                - mSplitWidth, mHeight - mSplitWidth), mRightPaint);
        if (mLeftMove) {
            canvas.drawBitmap(mLeftNormalBitmap, mLeftSelectPoint.x - mLeftBitmapRadius,
                    mLeftSelectPoint.y - mLeftBitmapRadius, mBitmapPaint);
        } else {
            canvas.drawBitmap(mLeftPressedBitmap, mLeftSelectPoint.x - mLeftBitmapRadius,
                    mLeftSelectPoint.y - mLeftBitmapRadius, mBitmapPaint);
        }
        if (mRightMove) {
            canvas.drawBitmap(mRightNormalBitmap, mWidth - mSplitWidth - mRightWidth -
                            mRightBitmapQuarterWidth, mRightSelectPoint.y - mRightBitmapHalfHeight,
                    mBitmapPaint);
        } else {
            canvas.drawBitmap(mRightPressedBitmap, mWidth - mSplitWidth - mRightWidth -
                            mRightBitmapQuarterWidth, mRightSelectPoint.y - mRightBitmapHalfHeight,
                    mBitmapPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            mWidth = width;
        } else {
            mWidth = 480;
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            mHeight = height;
        } else {
            mHeight = 350;
        }
        mLeftWidth = mWidth - mSplitWidth * 3 - mRightWidth;
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                mDownInLeft = isInLeftPanel(x, y);
                mDownInRight = isInRightPanel(x, y);
                if (mDownInLeft) {
                    mLeftMove = true;
                    reviseLeft(x, y);
                    mRightPaint.setColor(getLeftColor(mLeftSelectPoint.x - mSplitWidth,
                            mLeftSelectPoint.y - mSplitWidth));
                } else if (mDownInRight) {
                    mRightMove = true;
                    reviseRight(x, y);
                }
                invalidate();
                int rightColor = getRightColor(mRightSelectPoint.y - mSplitWidth);
                if (mCallbackColor == Integer.MAX_VALUE || mCallbackColor != rightColor) {
                    mCallbackColor = rightColor;
                } else {
                    break;
                }
                if (mOnColorChangedListener != null) {
                    mOnColorChangedListener.onColorChanged(mCallbackColor,
                            mRightPaint.getColor(),
                            (mRightSelectPoint.y - mSplitWidth) / (mHeight - 2 * mSplitWidth));
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mDownInLeft) {
                    mDownInLeft = false;
                } else if (mDownInRight) {
                    mDownInRight = false;
                }
                mLeftMove = false;
                mRightMove = false;
                invalidate();
                if (mOnColorChangedListener != null) {
                    mOnColorChangedListener.onColorChanged(getRightColor(mRightSelectPoint.y
                                    - mSplitWidth),
                            mRightPaint.getColor(),
                            (mRightSelectPoint.y - mSplitWidth) / (mHeight - 2 * mSplitWidth));
                }
        }
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mGradualChangeBitmap != null && mGradualChangeBitmap.isRecycled() == false) {
            mGradualChangeBitmap.recycle();
        }
        if (mLeftNormalBitmap != null && mLeftNormalBitmap.isRecycled() == false) {
            mLeftNormalBitmap.recycle();
        }
        if (mLeftPressedBitmap != null && mLeftPressedBitmap.isRecycled() == false) {
            mLeftPressedBitmap.recycle();
        }
        if (mRightNormalBitmap != null && mRightNormalBitmap.isRecycled() == false) {
            mRightNormalBitmap.recycle();
        }
        if (mRightPressedBitmap != null && mRightPressedBitmap.isRecycled() == false) {
            mRightPressedBitmap.recycle();
        }
        super.onDetachedFromWindow();
    }

    private Bitmap getGradualBitmap() {
        if (mGradualChangeBitmap == null) {
            Paint leftPaint = new Paint();
            leftPaint.setStrokeWidth(1);
            mGradualChangeBitmap = Bitmap.createBitmap(mLeftWidth, mHeight - 2 * mSplitWidth,
                    Config.RGB_565);
            Canvas canvas = new Canvas(mGradualChangeBitmap);
            int bitmapWidth = mGradualChangeBitmap.getWidth();
            mLeftWidth = bitmapWidth;
            int bitmapHeight = mGradualChangeBitmap.getHeight();
            int[] leftColors = new int[]{Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN,
                    Color.BLUE, Color.MAGENTA};
            Shader leftShader = new LinearGradient(0, bitmapHeight / 2, bitmapWidth, bitmapHeight
                    / 2, leftColors, null, TileMode.REPEAT);
            LinearGradient shadowShader = new LinearGradient(bitmapWidth / 2, 0, bitmapWidth / 2,
                    bitmapHeight,
                    Color.WHITE, Color.BLACK, TileMode.CLAMP);
            ComposeShader shader = new ComposeShader(leftShader, shadowShader,
                    PorterDuff.Mode.SCREEN);
            leftPaint.setShader(shader);
            canvas.drawRect(0, 0, bitmapWidth, bitmapHeight, leftPaint);
        }
        return mGradualChangeBitmap;
    }

    private boolean isInLeftPanel(float x, float y) {
        if (0 < x && x < mSplitWidth + mLeftWidth + mSplitWidth / 2 && 0 < y && y < mWidth) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isInRightPanel(float x, float y) {
        if (mWidth - mSplitWidth - mRightWidth - mSplitWidth / 2 < x && x < mWidth && 0 < y && y
                < mHeight) {
            return true;
        } else {
            return false;
        }
    }

    private void reviseLeft(float x, float y) {
        if (x < mSplitWidth) {
            mLeftSelectPoint.x = mSplitWidth;
        } else if (x > (mSplitWidth + mLeftWidth)) {
            mLeftSelectPoint.x = mSplitWidth + mLeftWidth;
        } else {
            mLeftSelectPoint.x = x;
        }
        if (y < mSplitWidth) {
            mLeftSelectPoint.y = mSplitWidth;
        } else if (y > (mHeight - mSplitWidth)) {
            mLeftSelectPoint.y = mHeight - mSplitWidth;
        } else {
            mLeftSelectPoint.y = y;
        }
    }

    private void reviseRight(float x, float y) {
        if (x < mSplitWidth) {
            mRightSelectPoint.x = mSplitWidth;
        } else if (x > (mSplitWidth + mLeftWidth)) {
            mRightSelectPoint.x = mSplitWidth + mLeftWidth;
        } else {
            mRightSelectPoint.x = x;
        }
        if (y < mSplitWidth) {
            mRightSelectPoint.y = mSplitWidth;
        } else if (y > (mHeight - mSplitWidth)) {
            mRightSelectPoint.y = mHeight - mSplitWidth;
        } else {
            mRightSelectPoint.y = y;
        }
    }

    private int getLeftColor(float x, float y) {
        Bitmap temp = getGradualBitmap();
        int intX = (int) x;
        int intY = (int) y;
        if (intX >= temp.getWidth()) {
            intX = temp.getWidth() - 1;
        }
        if (intY >= temp.getHeight()) {
            intY = temp.getHeight() - 1;
        }
        return temp.getPixel(intX, intY);
    }

    private int getRightColor(float y) {
        int a, r, g, b, so, dst;
        float p;
        float rightHalfHeight = (mHeight - (float) mSplitWidth * 2) / 2;
        if (y < rightHalfHeight) {
            so = mRightColors[0];
            dst = mRightColors[1];
            p = y / rightHalfHeight;
        } else {
            so = mRightColors[1];
            dst = mRightColors[2];
            p = (y - rightHalfHeight) / rightHalfHeight;
        }
        a = average(Color.alpha(so), Color.alpha(dst), p);
        r = average(Color.red(so), Color.red(dst), p);
        g = average(Color.green(so), Color.green(dst), p);
        b = average(Color.blue(so), Color.blue(dst), p);
        return Color.argb(a, r, g, b);
    }

    private int average(int s, int d, float p) {
        return s + Math.round(p * (d - s));
    }

    public interface OnColorChangedListener {

        void onColorChanged(int color, int originalColor, float saturation);
    }
}
