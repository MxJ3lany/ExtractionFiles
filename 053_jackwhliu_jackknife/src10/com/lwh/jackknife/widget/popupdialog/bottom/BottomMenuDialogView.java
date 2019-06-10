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

package com.lwh.jackknife.widget.popupdialog.bottom;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.lwh.jackknife.widget.NestedScrollingListView;
import com.lwh.jackknife.widget.R;
import com.lwh.jackknife.widget.popupdialog.DialogView;

import java.util.Arrays;
import java.util.List;

public class BottomMenuDialogView extends DialogView {

    private BottomMenuAdapter mMenuAdapter;

    /**
     * 是否显示取消菜单。
     */
    private boolean mShowCancelMenu = true;
    private String mCancelText;
    private float mMenuTextSize = INVALID;
    private int mMenuTextColor = INVALID_COLOR;
    private int mMenuBackground = INVALID_COLOR;
    private int mCancelBackground = INVALID_COLOR;
    private OnMenuClickListener mOnMenuClickListener;

    public BottomMenuDialogView(Context context, String... menuItemNames) {
        this(context, Arrays.asList(menuItemNames));
    }

    public BottomMenuDialogView(final Context context, final List<String> menuItemNames) {
        mMenuAdapter = new BottomMenuAdapter(context);
        mMenuAdapter.addItems(menuItemNames);
        mMenuAdapter.setMenuTextSize(mMenuTextSize);
        mMenuAdapter.setMenuTextColor(mMenuTextColor);
        mMenuAdapter.setMenuBackground(mMenuBackground);
        mContentView = LayoutInflater.from(context).inflate(R.layout.jknf_item_dialog_menu, null);
        NestedScrollingListView listView = (NestedScrollingListView) mContentView.findViewById(R.id.lv_dialogmenu_menu);
        listView.setAdapter(mMenuAdapter);
        listView.requestFocus();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mOnMenuClickListener != null) {
                    mOnMenuClickListener.onClick(position, menuItemNames.get(position));
                    if (mOnCancelListener != null) {
                        mOnCancelListener.onCancel();
                    }
                }
            }
        });
        final TextView cancelTextView = (TextView) mContentView.findViewById(R.id.tv_dialogmenu_cancel);
        if (mMenuTextSize != INVALID) {
            cancelTextView.setTextSize(mMenuTextSize);
        }
        if (mMenuTextColor != INVALID_COLOR) {
            cancelTextView.setTextColor(mMenuTextColor);
        }
        if (mMenuBackground != INVALID_COLOR) {
            GradientDrawable drawable = (GradientDrawable) cancelTextView.getBackground();
            drawable.setColor(mMenuBackground);
        }
        if (mCancelBackground != INVALID_COLOR) {
            GradientDrawable drawable = (GradientDrawable) cancelTextView.getBackground();
            drawable.setColor(mCancelBackground);
        }

        if (!TextUtils.isEmpty(mCancelText)) {
            cancelTextView.setText(mCancelText);
        }
        if (mShowCancelMenu) {
            cancelTextView.setVisibility(View.VISIBLE);
        } else {
            cancelTextView.setVisibility(View.GONE);
        }
        cancelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnMenuClickListener != null) {
                    mOnMenuClickListener.onClick(-1, cancelTextView.getText().toString());
                    if (mOnCancelListener != null) {
                        mOnCancelListener.onCancel();
                    }
                }
            }
        });
        initShadow(DEFAULT_SHADOW_COLOR);
    }

    public void setOnMenuClickListener(OnMenuClickListener l) {
        this.mOnMenuClickListener = l;
    }

    public void setShowCancel(boolean isShowCancel) {
        this.mShowCancelMenu = isShowCancel;
    }

    public void setMenuTextSize(float menuTextSize) {
        this.mMenuTextSize = menuTextSize;
    }

    public void setMenuTextColor(int menuTextColor) {
        this.mMenuTextColor = menuTextColor;
    }

    public void setCancelText(String mCancelText) {
        this.mCancelText = mCancelText;
    }

    public void setMenuBackground(int menuBackground) {
        this.mMenuBackground = menuBackground;
    }

    public void setCancelBackground(int cancelBackground) {
        this.mCancelBackground = cancelBackground;
    }

    /**
     * 底部弹出的菜单对话框的回调接口。
     */
    public interface OnMenuClickListener {

        /**
         * @param position 点击的是第几个菜单
         * @param menu     菜单的文字
         */
        void onClick(int position, String menu);
    }
}
