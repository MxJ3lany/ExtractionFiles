package com.lwh.jackknife.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SlidingDrawer;

public class SlidingView extends SlidingDrawer {

    //抽屉行为控件ID
    private int mHandleId = 0;
    //Handle 部分其他控件ID
    private int[] mTouchableIds = null;
    private OnSlidingViewClickListener mOnSlidingViewClickListener;

    public SlidingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlidingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public int[] getTouchableIds() {
        return mTouchableIds;
    }

    public void setTouchableIds(int[] ids) {
        this.mTouchableIds = ids;
    }

    public int getHandleId() {
        return mHandleId;
    }

    public void setHandleId(int id) {
        this.mHandleId = id;
    }

    public void setOnSlidingViewClickListener(OnSlidingViewClickListener listener) {
        mOnSlidingViewClickListener = listener;
    }

    public interface OnSlidingViewClickListener {
        void onClick(View view);
    }

    /*
     * 获取控件的屏幕区域。
     */
    public Rect getRectOnScreen(View view) {
        Rect rect = new Rect();
        int[] location = new int[2];
        View parent = view;
        if (view.getParent() instanceof View) {
            parent = (View) view.getParent();
        }
        parent.getLocationOnScreen(location);
        view.getHitRect(rect);
        rect.offset(location[0], location[1]);
        return rect;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // 触摸位置转换为屏幕坐标
        int[] location = new int[2];
        int x = (int) event.getX();
        int y = (int) event.getY();
        this.getLocationOnScreen(location);
        x += location[0];
        y += location[1];
        if (mTouchableIds != null) {
            for (int id : mTouchableIds) {
                View view = findViewById(id);
                if (view.isShown()) {
                    Rect rect = getRectOnScreen(view);
                    if (rect.contains(x, y)) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            if (mOnSlidingViewClickListener != null) {
                                mOnSlidingViewClickListener.onClick(view);
                            }
                        }
                        return true;
                    }
                }
            }
        }
        //抽屉行为控件
        if (event.getAction() == MotionEvent.ACTION_DOWN && mHandleId != 0) {
            View view = findViewById(mHandleId);
            Rect rect = getRectOnScreen(view);
            if (rect.contains(x, y)) {//点击抽屉控件时交由系统处理
                return super.onInterceptTouchEvent(event);
            } else {
                return false;
            }
        }
        return super.onInterceptTouchEvent(event);
    }
}
