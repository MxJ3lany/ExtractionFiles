/*
 * Copyright (C) 2019 The JackKnife Open Source Project
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

package com.lwh.jackknife.widget.wheelview.timer;

import com.lwh.jackknife.widget.wheelview.WheelView;

import java.util.TimerTask;

public final class InertiaTimerTask extends TimerTask {

    private float mCurrentVelocityY;
    private final float mFirstVelocityY;
    private final WheelView mWheelView;

    public InertiaTimerTask(WheelView wheelView, float velocityY) {
        super();
        this.mWheelView = wheelView;
        this.mFirstVelocityY = velocityY;
        mCurrentVelocityY = Integer.MAX_VALUE;
    }

    @Override
    public final void run() {
        //防止闪动，对速度做一个限制
        if (mCurrentVelocityY == Integer.MAX_VALUE) {
            if (Math.abs(mFirstVelocityY) > 2000f) {
                mCurrentVelocityY = mFirstVelocityY > 0 ? 2000f : -2000f;
            } else {
                mCurrentVelocityY = mFirstVelocityY;
            }
        }

        //发送handler消息 处理平顺停止滚动逻辑
        if (Math.abs(mCurrentVelocityY) >= 0.0f && Math.abs(mCurrentVelocityY) <= 20f) {
            mWheelView.cancelFuture();
            mWheelView.getHandler().sendEmptyMessage(MessageHandler.WHAT_SMOOTH_SCROLL);
            return;
        }

        int dy = (int) (mCurrentVelocityY / 100f);
        mWheelView.setTotalScrollY(mWheelView.getTotalScrollY() - dy);
        if (!mWheelView.isLoop()) {
            float itemHeight = mWheelView.getItemHeight();
            float top = (-mWheelView.getPosition()) * itemHeight;
            float bottom = (mWheelView.getItemsCount() - 1 - mWheelView.getPosition()) * itemHeight;
            if (mWheelView.getTotalScrollY() - itemHeight * 0.25 < top) {
                top = mWheelView.getTotalScrollY() + dy;
            } else if (mWheelView.getTotalScrollY() + itemHeight * 0.25 > bottom) {
                bottom = mWheelView.getTotalScrollY() + dy;
            }

            if (mWheelView.getTotalScrollY() <= top) {
                mCurrentVelocityY = 40f;
                mWheelView.setTotalScrollY((int) top);
            } else if (mWheelView.getTotalScrollY() >= bottom) {
                mWheelView.setTotalScrollY((int) bottom);
                mCurrentVelocityY = -40f;
            }
        }

        if (mCurrentVelocityY < 0.0f) {
            mCurrentVelocityY = mCurrentVelocityY + 20f;
        } else {
            mCurrentVelocityY = mCurrentVelocityY - 20f;
        }

        mWheelView.getHandler().sendEmptyMessage(MessageHandler.WHAT_INVALIDATE_LOOP_VIEW);
    }
}
