package com.lwh.jackknife.widget.wheelview.timer;

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

import com.lwh.jackknife.widget.wheelview.WheelView;

import java.util.TimerTask;

public final class SmoothScrollTimerTask extends TimerTask {

    private int mRealTotalOffset;
    private int mRealOffset;
    private int mOffset;
    private final WheelView mWheelView;

    public SmoothScrollTimerTask(WheelView wheelView, int offset) {
        this.mWheelView = wheelView;
        this.mOffset = offset;
        mRealTotalOffset = Integer.MAX_VALUE;
        mRealOffset = 0;
    }

    @Override
    public final void run() {
        if (mRealTotalOffset == Integer.MAX_VALUE) {
            mRealTotalOffset = mOffset;
        }
        //把要滚动的范围细分成10小份，按10小份单位来重绘
        mRealOffset = (int) ((float) mRealTotalOffset * 0.1F);

        if (mRealOffset == 0) {
            if (mRealTotalOffset < 0) {
                mRealOffset = -1;
            } else {
                mRealOffset = 1;
            }
        }

        if (Math.abs(mRealTotalOffset) <= 1) {
            mWheelView.cancelFuture();
            mWheelView.getHandler().sendEmptyMessage(MessageHandler.WHAT_ITEM_SELECTED);
        } else {
            mWheelView.setTotalScrollY(mWheelView.getTotalScrollY() + mRealOffset);

            //这里如果不是循环模式，则点击空白位置需要回滚，不然就会出现选到－1 item的 情况
            if (!mWheelView.isLoop()) {
                float itemHeight = mWheelView.getItemHeight();
                float top = (float) (-mWheelView.getPosition()) * itemHeight;
                float bottom = (float) (mWheelView.getItemsCount() - 1 - mWheelView.getPosition()) * itemHeight;
                if (mWheelView.getTotalScrollY() <= top || mWheelView.getTotalScrollY() >= bottom) {
                    mWheelView.setTotalScrollY(mWheelView.getTotalScrollY() - mRealOffset);
                    mWheelView.cancelFuture();
                    mWheelView.getHandler().sendEmptyMessage(MessageHandler.WHAT_ITEM_SELECTED);
                    return;
                }
            }
            mWheelView.getHandler().sendEmptyMessage(MessageHandler.WHAT_INVALIDATE_LOOP_VIEW);
            mRealTotalOffset = mRealTotalOffset - mRealOffset;
        }
    }
}
