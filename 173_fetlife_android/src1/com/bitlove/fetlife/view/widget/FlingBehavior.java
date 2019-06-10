package com.bitlove.fetlife.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.material.appbar.AppBarLayout;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ScrollingView;

public final class FlingBehavior extends AppBarLayout.Behavior {

    public FlingBehavior() {
    }

    public FlingBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onNestedFling(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, float velocityX, float velocityY, boolean consumed) {
        if (target instanceof ScrollingView) {
            final ScrollingView scrollingView = (ScrollingView) target;
            consumed = velocityY > 0 || scrollingView.computeVerticalScrollOffset() > 0;
        }
        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
    }
}
