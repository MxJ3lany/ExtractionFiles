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

package com.lwh.jackknife.xskin.attr;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lwh.jackknife.xskin.ResourceManager;
import com.lwh.jackknife.xskin.SkinManager;

public enum SkinAttrType {

    /**
     * 背景属性。
     */
    BACKGROUD("background") {
        @Override
        public void apply(View view, String resName) {
            Drawable drawable = getResourceManager().getDrawableByName(resName);
            if (drawable == null) return;
            view.setBackgroundDrawable(drawable);
        }
    },
    /**
     * 字体颜色。
     */
    COLOR("textColor") {
        @Override
        public void apply(View view, String resName) {
            ColorStateList colorlist = getResourceManager().getColorStateList(resName);
            if (colorlist == null) return;
            ((TextView) view).setTextColor(colorlist);
        }
    },
    /**
     * 图片资源。
     */
    SRC("src") {
        @Override
        public void apply(View view, String resName) {
            if (view instanceof ImageView) {
                Drawable drawable = getResourceManager().getDrawableByName(resName);
                if (drawable == null) return;
                ((ImageView) view).setImageDrawable(drawable);
            }

        }
    };

    String attrType;

    SkinAttrType(String attrType) {
        this.attrType = attrType;
    }

    public String getAttrType() {
        return attrType;
    }

    public abstract void apply(View view, String resName);

    /**
     * 获取资源管理器。
     */
    public ResourceManager getResourceManager() {
        return SkinManager.getInstance().getResourceManager();
    }
}
