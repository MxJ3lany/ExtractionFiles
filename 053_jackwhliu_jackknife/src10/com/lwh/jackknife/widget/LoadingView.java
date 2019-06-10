package com.lwh.jackknife.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import android.widget.ImageView;

public class LoadingView extends ImageView {

    private float mRotateDegrees;
    private int mFrameTime;
    private boolean mNeedToUpdateView;
    private Runnable mUpdateViewRunnable;

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setImageResource(R.drawable.jknf_loading_view_rotate);
        mFrameTime = 1000 / 12;
        mUpdateViewRunnable = new Runnable() {
            @Override
            public void run() {
                mRotateDegrees += 30;
                mRotateDegrees = mRotateDegrees < 360 ? mRotateDegrees : mRotateDegrees - 360;
                invalidate();
                if (mNeedToUpdateView) {
                    postDelayed(this, mFrameTime);
                }
            }
        };
    }

    public void setAnimationSpeed(float scale) {
        mFrameTime = (int) (1000 / 12 / scale);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.rotate(mRotateDegrees, getWidth() / 2, getHeight() / 2);
        super.onDraw(canvas);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mNeedToUpdateView = true;
        post(mUpdateViewRunnable);
    }

    @Override
    protected void onDetachedFromWindow() {
        mNeedToUpdateView = false;
        super.onDetachedFromWindow();
    }
}
