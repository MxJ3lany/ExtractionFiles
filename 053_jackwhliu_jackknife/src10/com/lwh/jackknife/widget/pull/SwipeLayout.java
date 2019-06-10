/*
 * Copyright (C) 2018 The JackKnife Open Source Project
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

package com.lwh.jackknife.widget.pull;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lwh.jackknife.widget.R;

import java.util.Timer;
import java.util.TimerTask;

public class SwipeLayout extends RelativeLayout {

    public static final int INIT = 0;

    public static final int RELEASE_TO_REFRESH = 1;

    public static final int REFRESHING = 2;

    public static final int RELEASE_TO_LOAD = 3;

    public static final int LOADING = 4;

    public static final int DONE = 5;

    private int mState = INIT;

    private OnRefreshListener mOnRefreshListener;

    public static final int SUCCEED = 0;

    public static final int FAIL = 1;

    private float mDownY, mLastY;

    public float mPullDownY = 0;

    private float mPullUpY = 0;

    private float mRefreshDist = 200;

    private float mLoadmoreDist = 200;

    private RefreshTimer mTimer;

    public float MOVE_SPEED = 8;

    private boolean mLayout = false;

    private boolean mTouch = false;

    private float mRatio = 2;

    private RotateAnimation mRotateAnimation;

    private RotateAnimation mRefreshingAnimation;

    private View mRefreshView;

    private ImageView mRefreshStateImageView;

    private TextView mRefreshStateTextView;

    private View mLoadmoreView;

    private ImageView mLoadStateImageView;

    private TextView mLoadStateTextView;

    private View mPullableView;

    private int mEvents;

    private boolean mCanPullDown = true;

    private boolean mCanPullUp = true;

    Handler mUpdateHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            MOVE_SPEED = (float) (8 + 5 * Math.tan(Math.PI / 2
                    / getMeasuredHeight() * (mPullDownY + Math.abs(mPullUpY))));
            if (!mTouch) {
                if (mState == REFRESHING && mPullDownY <= mRefreshDist) {
                    mPullDownY = mRefreshDist;
                    mTimer.cancel();
                } else if (mState == LOADING && -mPullUpY <= mLoadmoreDist) {
                    mPullUpY = -mLoadmoreDist;
                    mTimer.cancel();
                }
            }
            if (mPullDownY > 0) {
                mPullDownY -= MOVE_SPEED;
            } else if (mPullUpY < 0) {
                mPullUpY += MOVE_SPEED;
            }
            if (mPullDownY < 0) {
                mPullDownY = 0;
                if (mState != REFRESHING && mState != LOADING)
                    changeState(INIT);
                mTimer.cancel();
                requestLayout();
            }
            if (mPullUpY > 0) {
                mPullUpY = 0;
                if (mState != REFRESHING && mState != LOADING)
                    changeState(INIT);
                mTimer.cancel();
                requestLayout();
            }
            requestLayout();
            if (mPullDownY + Math.abs(mPullUpY) == 0)
                mTimer.cancel();
        }
    };

    public void setOnRefreshListener(OnRefreshListener l) {
        mOnRefreshListener = l;
    }

    public SwipeLayout(Context context) {
        super(context);
        initView(context);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(Context context) {
        if (isInEditMode()) {
            return;
        }
        mTimer = new RefreshTimer(mUpdateHandler);//循环UI检查
        mRotateAnimation = (RotateAnimation) AnimationUtils.loadAnimation(
                context, R.anim.jknf_reverse);
        mRefreshingAnimation = (RotateAnimation) AnimationUtils.loadAnimation(
                context, R.anim.jknf_rotating);
        LinearInterpolator lir = new LinearInterpolator();
        mRotateAnimation.setInterpolator(lir);
        mRefreshingAnimation.setInterpolator(lir);
    }

    private void hide() {
        mTimer.schedule(5);
    }

    public void refreshFinish(int refreshResult) {
        mRefreshStateImageView.clearAnimation();
        switch (refreshResult) {
            case SUCCEED:
                mRefreshStateTextView.setText(R.string.refresh_succeed);
                mRefreshStateImageView.setImageResource(R.drawable.jknf_swipe_layout_refresh_succeed);
                break;
            case FAIL:
            default:
                mRefreshStateTextView.setText(R.string.refresh_fail);
                mRefreshStateImageView.setImageResource(R.drawable.jknf_swipe_layout_refresh_failed);
                break;
        }
        if (mPullDownY > 0) {
            new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    changeState(DONE);
                    hide();
                }
            }.sendEmptyMessageDelayed(0, 1000);
        } else {
            changeState(DONE);
            hide();
        }
    }

    public void loadmoreFinish(int refreshResult) {
        mLoadStateImageView.clearAnimation();
        switch (refreshResult) {
            case SUCCEED:
                mLoadStateTextView.setText(R.string.load_succeed);
                mLoadStateImageView.setImageResource(R.drawable.jknf_swipe_layout_load_succeed);
                break;
            case FAIL:
            default:
                mLoadStateTextView.setText(R.string.load_fail);
                mLoadStateImageView.setImageResource(R.drawable.jknf_swipe_layout_load_failed);
                break;
        }
        if (mPullUpY < 0) {
            new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    changeState(DONE);
                    hide();
                }
            }.sendEmptyMessageDelayed(0, 1000);
        } else {
            changeState(DONE);
            hide();
        }
    }

    private void changeState(int to) {
        mState = to;
        switch (mState) {
            case INIT:
                mRefreshStateImageView.setImageResource(R.drawable.jknf_swipe_layout_logo);
                mRefreshStateTextView.setText(R.string.pull_to_refresh);
                mLoadStateImageView.setImageResource(R.drawable.jknf_swipe_layout_logo);
                mLoadStateTextView.setText(R.string.pullup_to_load);
                break;
            case RELEASE_TO_REFRESH:
                mRefreshStateTextView.setText(R.string.release_to_refresh);
                break;
            case REFRESHING:
                mRefreshStateTextView.setText(R.string.refreshing);
                mRefreshStateImageView.setImageResource(R.drawable.jknf_swipe_layout_refreshing);
                mRefreshStateImageView.startAnimation(mRefreshingAnimation);
                break;
            case RELEASE_TO_LOAD:
                mLoadStateTextView.setText(R.string.release_to_load);
                break;
            case LOADING:
                mLoadStateTextView.setText(R.string.loading);
                mLoadStateImageView.setImageResource(R.drawable.jknf_swipe_layout_loading);
                mLoadStateImageView.startAnimation(mRefreshingAnimation);
                break;
            case DONE:
                break;
        }
    }

    private void releasePull() {
        mCanPullDown = true;
        mCanPullUp = true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDownY = ev.getY();
                mLastY = mDownY;
                mTimer.cancel();
                mEvents = 0;
                releasePull();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
                mEvents = -1;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mEvents == 0) {
                    if (mPullDownY > 0
                            || (((Pullable) mPullableView).canPullDown()
                            && mCanPullDown && mState != LOADING)) {
                        mPullDownY = mPullDownY + (ev.getY() - mLastY) / mRatio;
                        if (mPullDownY < 0) {
                            mPullDownY = 0;
                            mCanPullDown = false;
                            mCanPullUp = true;
                        }
                        if (mPullDownY > getMeasuredHeight())
                            mPullDownY = getMeasuredHeight();
                        if (mState == REFRESHING) {
                            mTouch = true;
                        }
                    } else if (mPullUpY < 0
                            || (((Pullable) mPullableView).canPullUp() && mCanPullUp && mState != REFRESHING)) {
                        mPullUpY = mPullUpY + (ev.getY() - mLastY) / mRatio;
                        mPullUpY = mPullUpY + ev.getY() - mLastY;
                        if (mPullUpY > 0) {
                            mPullUpY = 0;
                            mCanPullDown = true;
                            mCanPullUp = false;
                        }
                        if (mPullUpY < -getMeasuredHeight())
                            mPullUpY = -getMeasuredHeight();
                        if (mState == LOADING) {
                            mTouch = true;
                        }
                    } else {
                        releasePull();
                    }
                } else {
                    mEvents = 0;
                }
                mLastY = ev.getY();
                mRatio = (float) (2 + 2 * Math.tan(Math.PI / 2 / getMeasuredHeight()
                        * (mPullDownY + Math.abs(mPullUpY))));
                if (mPullDownY > 0 || mPullUpY < 0) {
                    requestLayout();
                }
                if (mPullDownY > 0) {
                    if (mPullDownY <= mRefreshDist
                            && (mState == RELEASE_TO_REFRESH || mState == DONE)) {
                        changeState(INIT);
                    }
                    if (mPullDownY >= mRefreshDist && mState == INIT) {
                        changeState(RELEASE_TO_REFRESH);
                    }
                } else if (mPullUpY < 0) {
                    if (-mPullUpY <= mLoadmoreDist
                            && (mState == RELEASE_TO_LOAD || mState == DONE)) {
                        changeState(INIT);
                    }
                    if (-mPullUpY >= mLoadmoreDist && mState == INIT) {
                        changeState(RELEASE_TO_LOAD);
                    }
                }
                if ((mPullDownY + Math.abs(mPullUpY)) > 8) {
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mPullDownY > mRefreshDist || -mPullUpY > mLoadmoreDist) {
                    mTouch = false;
                }
                if (mState == RELEASE_TO_REFRESH) {
                    changeState(REFRESHING);
                    if (mOnRefreshListener != null)
                        mOnRefreshListener.onRefresh(this);
                } else if (mState == RELEASE_TO_LOAD) {
                    changeState(LOADING);
                    if (mOnRefreshListener != null)
                        mOnRefreshListener.onLoadMore(this);
                }
                hide();
            default:
                break;
        }
        super.dispatchTouchEvent(ev);
        return true;
    }

    private void initView() {
        mRefreshStateTextView = (TextView) mRefreshView
                .findViewById(R.id.state_tv);
        mRefreshStateImageView = (ImageView) mRefreshView.findViewById(R.id.state_iv);
        mLoadStateTextView = (TextView) mLoadmoreView
                .findViewById(R.id.loadstate_tv);
        mLoadStateImageView = (ImageView) mLoadmoreView.findViewById(R.id.loadstate_iv);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (!mLayout) {
            mRefreshView = getChildAt(0);
            mPullableView = getChildAt(1);
            mLoadmoreView = getChildAt(2);
            mLayout = true;
            initView();
            mRefreshDist = ((ViewGroup) mRefreshView).getChildAt(0)
                    .getMeasuredHeight();
            mLoadmoreDist = ((ViewGroup) mLoadmoreView).getChildAt(0)
                    .getMeasuredHeight();
        }
        mRefreshView.layout(0,
                (int) (mPullDownY + mPullUpY) - mRefreshView.getMeasuredHeight(),
                mRefreshView.getMeasuredWidth(), (int) (mPullDownY + mPullUpY));
        mPullableView.layout(0, (int) (mPullDownY + mPullUpY),
                mPullableView.getMeasuredWidth(), (int) (mPullDownY + mPullUpY)
                        + mPullableView.getMeasuredHeight());
        mLoadmoreView.layout(0,
                (int) (mPullDownY + mPullUpY) + mPullableView.getMeasuredHeight(),
                mLoadmoreView.getMeasuredWidth(),
                (int) (mPullDownY + mPullUpY) + mPullableView.getMeasuredHeight()
                        + mLoadmoreView.getMeasuredHeight());
    }

    class RefreshTimer {

        private Handler handler;
        private Timer timer;
        private RefreshTask mTask;

        public RefreshTimer(Handler handler) {
            this.handler = handler;
            timer = new Timer();
        }

        public void schedule(long period) {
            if (mTask != null) {
                mTask.cancel();
                mTask = null;
            }
            mTask = new RefreshTask(handler);
            timer.schedule(mTask, 0, period);
        }

        public void cancel() {
            if (mTask != null) {
                mTask.cancel();
                mTask = null;
            }
        }

        private class RefreshTask extends TimerTask {

            private Handler handler;

            public RefreshTask(Handler handler) {
                this.handler = handler;
            }

            @Override
            public void run() {
                handler.obtainMessage().sendToTarget();
            }
        }
    }

    public interface OnRefreshListener {

        void onRefresh(SwipeLayout swipeLayout);

        void onLoadMore(SwipeLayout swipeLayout);
    }
}