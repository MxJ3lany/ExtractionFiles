/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.douya.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowInsets;

import androidx.annotation.RequiresApi;
import me.zhanghai.android.douya.R;
import me.zhanghai.android.douya.util.ViewUtils;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class StatusBarBackgroundView extends View {

    @NonNull
    private Drawable mStatusBarBackground;

    @Nullable
    private WindowInsets mInsets;

    public StatusBarBackgroundView(@NonNull Context context) {
        super(context);

        init();
    }

    public StatusBarBackgroundView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public StatusBarBackgroundView(@NonNull Context context, @Nullable AttributeSet attrs,
                                   int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public StatusBarBackgroundView(@NonNull Context context, @Nullable AttributeSet attrs,
                                   int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {

        int darkPrimaryColor = ViewUtils.getColorFromAttrRes(R.attr.colorPrimaryDark, 0,
                getContext());
        mStatusBarBackground = new ColorDrawable(darkPrimaryColor);

        @SuppressLint("PrivateResource")
        int appBarElevation = getResources().getDimensionPixelSize(R.dimen.design_appbar_elevation);
        setElevation(appBarElevation);
        setFitsSystemWindows(true);
        setWillNotDraw(true);
    }

    @NonNull
    @Override
    public WindowInsets onApplyWindowInsets(@NonNull WindowInsets insets) {
        mInsets = insets;
        setWillNotDraw(mInsets.getSystemWindowInsetTop() == 0);
        return insets;
    }

    @Override
    @SuppressLint("MissingSuperCall")
    public void draw(@NonNull Canvas canvas) {
        if (mInsets == null) {
            return;
        }
        int left = mInsets.getSystemWindowInsetLeft();
        int right = getWidth() - left - mInsets.getSystemWindowInsetRight();
        mStatusBarBackground.setBounds(left, 0, right, mInsets.getSystemWindowInsetTop());
        mStatusBarBackground.draw(canvas);
    }
}
