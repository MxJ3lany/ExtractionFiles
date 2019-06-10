/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.mcxiaoke.next.ui.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ScrollView;

import java.util.ArrayList;

/**
 * A {@link android.widget.ScrollView} that will never lock scrolling in a particular
 * direction.
 * <p/>
 * Usually ScrollView will capture all touch events once a drag has begun. In
 * some cases, we want to delegate those touches to children as normal, even in
 * the middle of a drag. This is useful when there are childviews like a WebView
 * tha handles scrolling in the horizontal direction even while the ScrollView
 * drags vertically.
 * <p/>
 * This is only tested to work for ScrollViews where the content scrolls in one
 * direction.
 */

//from android\source\packages\apps\Email
public class NonLockingScrollView extends ScrollView {
    public NonLockingScrollView(Context context) {
        super(context);
    }

    public NonLockingScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NonLockingScrollView(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Whether or not the contents of this view is being dragged by one of the
     * children in {@link #mChildrenNeedingAllTouches}.
     */
    private boolean mInCustomDrag = false;

    /**
     * The list of children who should always receive touch events, and not have
     * them intercepted.
     */
    private final ArrayList<View> mChildrenNeedingAllTouches = new ArrayList<View>();

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();
        final boolean isUp = action == MotionEvent.ACTION_UP;

        if (isUp && mInCustomDrag) {
            // An up event after a drag should be intercepted so that child
            // views don't handle
            // click events falsely after a drag.
            mInCustomDrag = false;
            onTouchEvent(ev);
            return true;
        }

        if (!mInCustomDrag && !isEventOverChild(ev, mChildrenNeedingAllTouches)) {
            return super.onInterceptTouchEvent(ev);
        }

        // Note the normal scrollview implementation is to intercept all touch
        // events after it has
        // detected a drag starting. We will handle this ourselves.
        mInCustomDrag = super.onInterceptTouchEvent(ev);
        if (mInCustomDrag) {
            onTouchEvent(ev);
        }

        // Don't intercept events - pass them on to children as normal.
        return false;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        excludeChildrenFromInterceptions(this);
    }

    /**
     * Traverses the view tree for {@link android.webkit.WebView}s so they can be excluded from
     * touch interceptions and receive all events.
     */
    private void excludeChildrenFromInterceptions(View node) {
        // If additional types of children should be excluded (e.g. horizontal
        // scrolling banners),
        // this needs to be modified accordingly.
        if (node instanceof WebView) {
            mChildrenNeedingAllTouches.add(node);
        } else if (node instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) node;
            final int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = viewGroup.getChildAt(i);
                excludeChildrenFromInterceptions(child);
            }
        }
    }

    private static final Rect sHitFrame = new Rect();

    private static boolean isEventOverChild(MotionEvent ev,
                                            ArrayList<View> children) {
        final int actionIndex = ev.getActionIndex();
        final float x = ev.getX(actionIndex);
        final float y = ev.getY(actionIndex);

        for (View child : children) {
            if (!canViewReceivePointerEvents(child)) {
                continue;
            }
            child.getHitRect(sHitFrame);

            // child can receive the motion event.
            if (sHitFrame.contains((int) x, (int) y)) {
                return true;
            }
        }
        return false;
    }

    private static boolean canViewReceivePointerEvents(View child) {
        return child.getVisibility() == VISIBLE
                || (child.getAnimation() != null);
    }
}
