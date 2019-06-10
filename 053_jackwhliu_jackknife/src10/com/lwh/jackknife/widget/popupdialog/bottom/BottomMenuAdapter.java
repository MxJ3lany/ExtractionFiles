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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lwh.jackknife.widget.R;

import java.util.ArrayList;
import java.util.List;

public class BottomMenuAdapter extends BaseAdapter {

    private static final int INVALID = -1;
    private static final int INVALID_COLOR = 0;
    private float mMenuTextSize = INVALID;
    private int mMenuTextColor = INVALID_COLOR;
    private int mMenuBackground = INVALID_COLOR;
    private LayoutInflater mInflater;
    private volatile List<String> mMenus;

    public BottomMenuAdapter(Context context) {
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mMenus = new ArrayList<>();
    }

    public float getMenuTextSize() {
        return mMenuTextSize;
    }

    public void setMenuTextSize(float menuTextSize) {
        this.mMenuTextSize = menuTextSize;
    }

    public void setMenuTextColor(int menuTextColor) {
        this.mMenuTextColor = menuTextColor;
    }

    public void setMenuBackground(int menuBackground) {
        this.mMenuBackground = menuBackground;
    }

    @Override
    public int getCount() {
        return mMenus == null ? 0 : mMenus.size();
    }

    @Override
    public Object getItem(int position) {
        return mMenus == null ? null : mMenus.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItems(List<String> menus) {
        this.mMenus = menus;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (holder == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.jknf_item_popup_menu, null);
            holder.tv_popupmenu_name = (TextView) convertView.findViewById(R.id.tv_popupmenu_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String menu = (String) getItem(position);
        holder.tv_popupmenu_name.setText(menu);
        if (mMenuTextSize != INVALID) {
            holder.tv_popupmenu_name.setTextSize(mMenuTextSize);
        }
        if (mMenuTextColor != INVALID_COLOR) {
            holder.tv_popupmenu_name.setTextColor(mMenuTextColor);
        }
        if (mMenuBackground != INVALID_COLOR) {
            GradientDrawable drawable = (GradientDrawable) holder.tv_popupmenu_name.getBackground();
            drawable.setColor(mMenuBackground);
        }
        if (getCount() == 1) {
            holder.tv_popupmenu_name.setBackgroundResource(R.drawable.jknf_bottom_menu_bg);
        } else if (getCount() == 2) {
            if (position == 0) {
                holder.tv_popupmenu_name.setBackgroundResource(R.drawable.jknf_bottom_menu_top);
            }
            if (position == 1) {
                holder.tv_popupmenu_name.setBackgroundResource(R.drawable.jknf_bottom_menu_bottom);
            }
        } else {
            if (position == 0) {
                holder.tv_popupmenu_name.setBackgroundResource(R.drawable.jknf_bottom_menu_top);
            } else if (position == (getCount() - 1)) {
                holder.tv_popupmenu_name.setBackgroundResource(R.drawable.jknf_bottom_menu_bottom);
            }
        }
        return convertView;
    }

    private static class ViewHolder {

        TextView tv_popupmenu_name;
    }
}
