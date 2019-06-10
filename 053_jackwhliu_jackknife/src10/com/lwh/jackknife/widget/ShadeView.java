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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.RadioButton;

public class ShadeView extends RadioButton {

    private Paint mTextPaint;

    private Paint mIconPaint;

    private Rect mTextRect;

    private Rect mIconRect;

    private Bitmap mCacheBitmap;

    private Bitmap mIconBitmap;

    private String mText;

    private int mTextColor;

    private float mTextSize;

    private int mHoverColor;

    private float mAlpha;

    private final int DEFAULT_TEXT_SIZE = 12;

    private final int DEFAULT_TEXT_COLOR = 0xFF2B2B2B;

    private final int DEFAULT_HOVER_COLOR = 0xFFFFA500;

    private final String STATE_INSTANCE = "state_instance";

    private final String STATE_ALPHA = "state_alpha";

    public ShadeView(Context context) {
        this(context, null);
    }

    public ShadeView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.shadeViewStyle);
    }

    public ShadeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs, defStyleAttr);
        initRects();
        initPaints();
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ShadeView, defStyleAttr, 0);
        BitmapDrawable iconDrawable = (BitmapDrawable) a.getDrawable(
                R.styleable.ShadeView_shadeview_icon);
        if (iconDrawable != null) {
            setIconBitmap(iconDrawable.getBitmap());
        }
        setTextColor(a.getColor(R.styleable.ShadeView_shadeview_textColor, DEFAULT_TEXT_COLOR));
        setHoverColor(a.getColor(R.styleable.ShadeView_shadeview_hoverColor, DEFAULT_HOVER_COLOR));
        setText(a.getString(R.styleable.ShadeView_shadeview_text));
        setTextSize(a.getDimension(R.styleable.ShadeView_shadeview_textSize,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE,
                        getResources().getDisplayMetrics())));
        a.recycle();
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        setShade(checked ? 1.0f : 0.0f);
    }

    private void initPaints() {
        mTextPaint = new Paint();
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setDither(true);
        mTextPaint.getTextBounds(mText, 0, mText.length(), mTextRect);
        mIconPaint = new Paint();
    }

    private void initRects() {
        mTextRect = new Rect();
        mIconRect = new Rect();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int iconSide = Math.min(getMeasuredWidth() - getPaddingLeft()
                - getPaddingRight(), getMeasuredHeight() - getPaddingTop()
                - getPaddingBottom() - mTextRect.height());
        int left = getMeasuredWidth() / 2 - iconSide / 2;
        int top = (getMeasuredHeight() - mTextRect.height()) / 2 - iconSide / 2;
        int right = left + iconSide;
        int bottom = top + iconSide;
        mIconRect.set(left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int alpha = (int) Math.ceil((255 * mAlpha));
        resetIcon(canvas);
        drawIcon(alpha);
        drawCacheText(canvas, alpha);
        drawHoverText(canvas, alpha);
        clearIconCache(canvas);
    }

    private void resetIcon(Canvas canvas) {
        canvas.drawBitmap(mIconBitmap, null, mIconRect, null);
    }

    private void drawIcon(int alpha) {
        mCacheBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mCacheBitmap);
        mIconPaint.reset();
        mIconPaint.setColor(mHoverColor);
        mIconPaint.setAntiAlias(true);
        mIconPaint.setDither(true);
        mIconPaint.setAlpha(alpha);
        canvas.drawRect(mIconRect, mIconPaint);
        mIconPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mIconPaint.setAlpha(255);
        canvas.drawBitmap(mIconBitmap, null, mIconRect, mIconPaint);
    }

    private void clearIconCache(Canvas canvas) {
        canvas.drawBitmap(mCacheBitmap, 0, 0, null);
    }

    private void drawCacheText(Canvas canvas, int alpha) {
        mTextPaint.setColor(mTextColor);
        mTextPaint.setAlpha(255 - alpha);
        canvas.drawText(mText, mIconRect.left + mIconRect.width() / 2 - mTextRect.width() / 2,
                mIconRect.bottom + mTextRect.height(), mTextPaint);
    }

    private void drawHoverText(Canvas canvas, int alpha) {
        mTextPaint.setColor(mHoverColor);
        mTextPaint.setAlpha(alpha);
        canvas.drawText(mText, mIconRect.left + mIconRect.width() / 2 - mTextRect.width() / 2,
                mIconRect.bottom + mTextRect.height(), mTextPaint);
    }

    public void setShade(float alpha) {
        if (mAlpha != alpha) {
            this.mAlpha = alpha;
            invalidateView();
        }
    }

    public void setIconResource(int resId) {
        BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(resId);
        Bitmap bitmap = drawable.getBitmap();
        setIconBitmap(bitmap);
    }

    public void setIconBitmap(Bitmap bitmap) {
        if (!bitmap.equals(mIconBitmap)) {
            mIconBitmap = bitmap;
            invalidateView();
        }
    }

    private void invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        if (!text.equals(mText)) {
            this.mText = text;
            invalidateView();
        }
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int color) {
        if (color != mTextColor) {
            this.mTextColor = color;
            invalidateView();
        }
    }

    public float getTextSize() {
        return mTextSize;
    }

    public void setTextSize(float size) {
        if (size != mTextSize) {
            this.mTextSize = size;
            invalidateView();
        }
    }

    public int getHoverColor() {
        return mHoverColor;
    }

    public void setHoverColor(int color) {
        if (color != mHoverColor) {
            this.mHoverColor = color;
            invalidateView();
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(STATE_INSTANCE, super.onSaveInstanceState());
        bundle.putFloat(STATE_ALPHA, mAlpha);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable parcelable) {
        if (parcelable instanceof Bundle) {
            Bundle bundle = (Bundle) parcelable;
            mAlpha = bundle.getFloat(STATE_ALPHA);
            super.onRestoreInstanceState(bundle.getParcelable(STATE_INSTANCE));
        } else {
            super.onRestoreInstanceState(parcelable);
        }
    }
}
