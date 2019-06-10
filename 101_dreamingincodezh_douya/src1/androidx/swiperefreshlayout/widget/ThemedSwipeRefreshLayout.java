/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package androidx.swiperefreshlayout.widget;

import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import me.zhanghai.android.douya.R;
import me.zhanghai.android.douya.util.ViewUtils;

public class ThemedSwipeRefreshLayout extends SwipeRefreshLayout {

    public ThemedSwipeRefreshLayout(@NonNull Context context) {
        super(context);

        init();
    }

    public ThemedSwipeRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        Context context = getContext();
        ((ShapeDrawable) mCircleView.getBackground()).getPaint().setColor(
                ViewUtils.getColorFromAttrRes(R.attr.colorBackgroundFloating, 0, context));
        setColorSchemeColors(ViewUtils.getColorFromAttrRes(R.attr.colorAccent, 0, context));
    }
}
