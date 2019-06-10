/*
 *
 *  * Copyright (C) 2017 The JackKnife Open Source Project
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.lwh.jackknife.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TitleBar extends RelativeLayout {

    private String mTitle;

    private int mTitleTextColor;

    private float mTitleTextSize = 12.0f;

    private Drawable mLeftDrawable;

    private Drawable mRightDrawable;

    private int mLeftDrawableSize = 30;

    private int mRightDrawableSize = 30;

    private TextView mTitleView;

    private ImageView mLeftView;

    private ImageView mRightView;

    private OnLeftClickListener mOnLeftClickListener;

    private OnRightClickListener mOnRightClickListener;

    public TitleBar(Context context) {
        this(context, null);
    }

    public TitleBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.titleBarStyle);
    }

    public TitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs, defStyleAttr);
        initViews();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (mLeftView != null) {
            mLeftView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnLeftClickListener != null) {
                        mOnLeftClickListener.onClick(v);
                    }
                }
            });
        }
        if (mRightView != null) {
            mRightView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnRightClickListener != null) {
                        mOnRightClickListener.onClick(v);
                    }
                }
            });
        }
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TitleBar, 0, defStyleAttr);
        mTitle = a.getString(R.styleable.TitleBar_titlebar_title);
        mTitleTextColor = a.getColor(R.styleable.TitleBar_titlebar_titleTextColor,
                getResources().getColor(R.color.gray));
        mTitleTextSize = a.getDimension(R.styleable.TitleBar_titlebar_titleTextSize, mTitleTextSize);
        mLeftDrawable = a.getDrawable(R.styleable.TitleBar_titlebar_leftDrawable);
        mRightDrawable = a.getDrawable(R.styleable.TitleBar_titlebar_rightDrawable);
        mLeftDrawableSize = a.getDimensionPixelOffset(R.styleable.TitleBar_titlebar_leftDrawableSize,
                mLeftDrawableSize);
        mRightDrawableSize = a.getDimensionPixelOffset(R.styleable.TitleBar_titlebar_rightDrawableSize,
                mRightDrawableSize);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = measureWidth(widthMeasureSpec);
        int measuredHeight = measureHeight(heightMeasureSpec);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    private int measureWidth(int widthMeasureSpec) {
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
            return MeasureSpec.getSize(widthMeasureSpec);
        } else {
            return getMeasuredWidth();
        }
    }

    private int measureHeight(int heightMeasureSpec) {
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            return MeasureSpec.getSize(heightMeasureSpec);
        } else {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
        }
    }

    private void initViews() {
        if (mLeftDrawable != null) {
            mLeftView = new ImageView(getContext());
            LayoutParams leftLp = new LayoutParams(mLeftDrawableSize, mLeftDrawableSize);
            leftLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            leftLp.addRule(RelativeLayout.CENTER_VERTICAL);
            mLeftView.setImageBitmap(((BitmapDrawable) mLeftDrawable).getBitmap());
            addView(mLeftView, leftLp);
        }
        if (mTitle != null) {
            mTitleView = new TextView(getContext());
            mTitleView.setText(mTitle);
            mTitleView.setTextColor(mTitleTextColor);
            mTitleView.setTextSize(mTitleTextSize);
            LayoutParams centerLp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            centerLp.addRule(RelativeLayout.CENTER_IN_PARENT);
            addView(mTitleView, centerLp);
        }
        if (mRightDrawable != null) {
            mRightView = new ImageView(getContext());
            LayoutParams rightLp = new LayoutParams(mRightDrawableSize, mRightDrawableSize);
            rightLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            rightLp.addRule(RelativeLayout.CENTER_VERTICAL);
            mRightView.setImageBitmap(((BitmapDrawable) mRightDrawable).getBitmap());
            addView(mRightView, rightLp);
        }
    }

    public String getTitle() {
        return mTitle;
    }

    public int getTitleTextColor() {
        return mTitleTextColor;
    }

    public float getTitleTextSize() {
        return mTitleTextSize;
    }

    public Drawable getLeftDrawable() {
        return mLeftDrawable;
    }

    public Drawable getRightDrawable() {
        return mRightDrawable;
    }

    public TextView getTitleView() {
        return mTitleView;
    }

    public ImageView getLeftView() {
        return mLeftView;
    }

    public ImageView getRightView() {
        return mRightView;
    }

    public void setOnLeftClickListener(OnLeftClickListener l) {
        if (mLeftView == null) {
            throw new RuntimeException("The left-hand-side view is not specified.");
        }
        this.mOnLeftClickListener = l;
    }

    public void setOnRightClickListener(OnRightClickListener l) {
        if (mRightView == null) {
            throw new RuntimeException("The right-hand-side view is not specified.");
        }
        this.mOnRightClickListener = l;
    }

    public interface OnLeftClickListener {
        void onClick(View view);
    }

    public interface OnRightClickListener {
        void onClick(View view);
    }
}
