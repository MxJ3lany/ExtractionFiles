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

package com.lwh.jackknife.xskin;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.view.View;
import android.widget.ImageView;

/**
 * 这个接口提供了一组从皮肤资源包中加载和显示图片和颜色资源的API。
 */
public interface ISkinLoader {

    /**
     * 无效值。
     */
    int INVALID = 0;

    /**
     * 通过插件资源包的资源名resName获取插件的资源ID。
     *
     * @param resName 比如skin_logo
     * @return 资源ID，如R.drawable.skin_logo
     */
    @DrawableRes
    int getDrawableRes(String resName);

    /**
     * 通过插件资源包的资源名resName获取插件的资源ID。
     *
     * @param resName 比如skin_theme_item_outside_color
     * @return 资源ID，如R.color.skin_theme_item_outside_color
     */
    @ColorRes
    int getColorRes(String resName);

    /**
     * 从插件资源包中获取Drawable，返回后，必须setBounds才可以使用。
     *
     * @param resName 比如skin_logo
     * @return 可绘制的对象
     */
    @Deprecated
    Drawable getDrawable(String resName);

    Drawable getDrawableWithRect(String resName, Rect rect);

    Drawable getDrawableWithPixels(String resName, int width, int height);

    Drawable getDrawableWithSize(String resName, int size);

    /**
     * 从插件资源包中获取位图。
     *
     * @param resName 比如skin_logo
     * @return 位图
     */
    Bitmap getBitmap(String resName);

    /**
     * @see ImageView#setImageDrawable(Drawable)
     */
    void setImageDrawable(ImageView imageView, String resName);

    /**
     * @see View#setBackgroundDrawable(Drawable)
     */
    void setBackgroundDrawable(View view, String resName);
}
