/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.douya.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.appcompat.widget.TintTypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

public class ForegroundHelper {

    //com.android.internal.R.attr.foregroundInsidePadding
    private static final int COM_ANDROID_INTERNAL_R_ATTR_FOREGROUND_INSIDE_PADDING = -300063;
    private static final int[] STYLEABLE = {
            android.R.attr.foreground,
            android.R.attr.foregroundGravity,
            COM_ANDROID_INTERNAL_R_ATTR_FOREGROUND_INSIDE_PADDING
    };
    private static final int STYLEABLE_ANDROID_FOREGROUND = 0;
    private static final int STYLEABLE_ANDROID_FOREGROUND_GRAVITY = 1;
    private static final int STYLEABLE_ANDROID_FOREGROUND_INSIDE_PADDING = 2;

    private Delegate mDelegate;

    private boolean mHasFrameworkForeground;

    private Drawable mForeground;

    private final Rect mSelfBounds = new Rect();
    private final Rect mOverlayBounds = new Rect();
    private int mForegroundGravity = Gravity.FILL;
    private boolean mForegroundInPadding = true;

    private boolean mForegroundBoundsChanged = false;

    public ForegroundHelper(Delegate delegate) {
        mDelegate = delegate;
    }

    @SuppressLint("RestrictedApi")
    public void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {

        // @see View#View(android.content.Context, android.util.AttributeSet, int, int)
        mHasFrameworkForeground = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && context.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.M)
                || mDelegate.getOwner() instanceof FrameLayout;

        if (mHasFrameworkForeground) {
            return;
        }

        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs, STYLEABLE,
                defStyleAttr, defStyleRes);

        mForegroundGravity = a.getInt(STYLEABLE_ANDROID_FOREGROUND_GRAVITY, mForegroundGravity);

        Drawable foreground = a.getDrawable(STYLEABLE_ANDROID_FOREGROUND);
        if (foreground != null) {
            setForeground(foreground);
        }

        mForegroundInPadding = a.getBoolean(STYLEABLE_ANDROID_FOREGROUND_INSIDE_PADDING, true);

        a.recycle();
    }

    public int getForegroundGravity() {

        if (mHasFrameworkForeground) {
            return mDelegate.superGetForegroundGravity();
        }

        return mForegroundGravity;
    }

    public void setForegroundGravity(int foregroundGravity) {

        if (mHasFrameworkForeground) {
            mDelegate.superSetForegroundGravity(foregroundGravity);
            return;
        }

        if (mForegroundGravity != foregroundGravity) {

            if ((foregroundGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) == 0) {
                foregroundGravity |= Gravity.START;
            }

            if ((foregroundGravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
                foregroundGravity |= Gravity.TOP;
            }

            mForegroundGravity = foregroundGravity;

            mDelegate.getOwner().requestLayout();
        }
    }

    public void setVisibility(int visibility) {
        mDelegate.superSetVisibility(visibility);

        if (mHasFrameworkForeground) {
            return;
        }

        if (mForeground != null) {
            mForeground.setVisible(visibility == View.VISIBLE, false);
        }
    }

    public boolean verifyDrawable(Drawable who) {

        if (mHasFrameworkForeground) {
            return mDelegate.superVerifyDrawable(who);
        }

        return mDelegate.superVerifyDrawable(who) || (who == mForeground);
    }

    public void jumpDrawablesToCurrentState() {
        mDelegate.superJumpDrawablesToCurrentState();

        if (mHasFrameworkForeground) {
            return;
        }

        if (mForeground != null) {
            mForeground.jumpToCurrentState();
        }
    }

    public void drawableStateChanged() {
        mDelegate.superDrawableStateChanged();

        if (mHasFrameworkForeground) {
            return;
        }

        if (mForeground != null && mForeground.isStateful()) {
            mForeground.setState(mDelegate.getOwner().getDrawableState());
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void drawableHotspotChanged(float x, float y) {
        mDelegate.superDrawableHotspotChanged(x, y);

        if (mHasFrameworkForeground) {
            return;
        }

        if (mForeground != null) {
            mForeground.setHotspot(x, y);
        }
    }

    public void setForeground(Drawable foreground) {

        if (mHasFrameworkForeground) {
            mDelegate.superSetForeground(foreground);
            return;
        }

        if (mForeground != foreground) {

            View owner = mDelegate.getOwner();

            if (mForeground != null) {
                mForeground.setCallback(null);
                owner.unscheduleDrawable(mForeground);
            }

            mForeground = foreground;

            if (foreground != null) {
                owner.setWillNotDraw(false);
                foreground.setCallback(owner);
                DrawableCompat.setLayoutDirection(foreground, ViewCompat.getLayoutDirection(owner));
                if (foreground.isStateful()) {
                    foreground.setState(owner.getDrawableState());
                }
            } else {
                owner.setWillNotDraw(true);
            }
            owner.requestLayout();
            owner.invalidate();
        }
    }

    public Drawable getForeground() {

        if (mHasFrameworkForeground) {
            return mDelegate.superGetForeground();
        }

        return mForeground;
    }

    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mDelegate.superOnLayout(changed, left, top, right, bottom);

        if (mHasFrameworkForeground) {
            return;
        }

        mForegroundBoundsChanged = true;
    }

    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        mDelegate.superOnSizeChanged(w, h, oldw, oldh);

        if (mHasFrameworkForeground) {
            return;
        }

        mForegroundBoundsChanged = true;
    }

    public void draw(@NonNull Canvas canvas) {
        mDelegate.superDraw(canvas);

        if (mHasFrameworkForeground) {
            return;
        }

        if (mForeground != null) {

            final Drawable foreground = mForeground;

            if (mForegroundBoundsChanged) {

                View owner = mDelegate.getOwner();

                mForegroundBoundsChanged = false;
                Rect selfBounds = mSelfBounds;
                Rect overlayBounds = mOverlayBounds;

                int w = owner.getRight() - owner.getLeft();
                int h = owner.getBottom() - owner.getTop();

                if (mForegroundInPadding) {
                    selfBounds.set(0, 0, w, h);
                } else {
                    selfBounds.set(owner.getPaddingLeft(), owner.getPaddingTop(),
                            w - owner.getPaddingRight(), h - owner.getPaddingBottom());
                }

                int layoutDirection = ViewCompat.getLayoutDirection(owner);
                GravityCompat.apply(mForegroundGravity, foreground.getIntrinsicWidth(),
                        foreground.getIntrinsicHeight(), selfBounds, overlayBounds,
                        layoutDirection);
                foreground.setBounds(overlayBounds);
            }

            foreground.draw(canvas);
        }
    }

    public interface Delegate {
        View getOwner();
        int superGetForegroundGravity();
        void superSetForegroundGravity(int foregroundGravity);
        void superSetVisibility(int visibility);
        boolean superVerifyDrawable(Drawable who);
        void superJumpDrawablesToCurrentState();
        void superDrawableStateChanged();
        void superDrawableHotspotChanged(float x, float y);
        void superSetForeground(Drawable foreground);
        Drawable superGetForeground();
        void superOnLayout(boolean changed, int left, int top, int right, int bottom);
        void superOnSizeChanged(int w, int h, int oldw, int oldh);
        void superDraw(@NonNull Canvas canvas);
    }
}
