/*
 * Copyright (C) 2017 The JackKnife Open Source Project
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

package com.lwh.jackknife.widget;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

public class ViewHolder<VIEW extends View> {

    private SparseArray<VIEW> mItemViews;
    private int mPosition;
    private View mConvertView;
    private Context mContext;
    private ViewGroup mParent;
    protected int mLayoutId;

    public ViewHolder(Context context, View itemView, int[] itemViewIds, ViewGroup parent, int position) {
        this.mContext = context;
        this.mConvertView = itemView;
        this.mPosition = position;
        this.mItemViews = new SparseArray<>();
        this.mParent = parent;
        for (int id : itemViewIds) {
            findViewById(id);
        }
        this.mConvertView.setTag(this);
    }

    public SparseArray<VIEW> getItemViews() {
        return mItemViews;
    }

    public void setPosition(int position) {
        this.mPosition = position;
    }

    public int getPosition() {
        return mPosition;
    }

    public View getConvertView() {
        return mConvertView;
    }

    public ViewGroup getViewParent() {
        return mParent;
    }

    public Context getContext() {
        return mContext;
    }

    public int getLayoutId() {
        return mLayoutId;
    }

    public VIEW findViewById(int id) {
        View view = mItemViews.get(id);
        if (view == null) {
            view = mConvertView.findViewById(id);
            mItemViews.put(id, (VIEW) view);
        }
        return (VIEW) view;
    }
}
