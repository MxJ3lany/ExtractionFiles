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

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

public class SkinLoader implements ISkinLoader {

    private static final String DEF_TYPE_DRAWABLE = "drawable";
    private static final String DEF_TYPE_COLOR = "color";
    private static SkinLoader sLoader;
    private ResourceManager mResourceManager;
    private Resources mSkinResources;
    private String mCurPluginPkgName;

    private SkinLoader() {
        mResourceManager = SkinManager.getInstance().getResourceManager();
        mSkinResources = mResourceManager.getResources();
        mCurPluginPkgName = mResourceManager.getPluginPackageName();
    }

    public static SkinLoader getInstance() {
        if (sLoader == null) {
            synchronized (SkinLoader.class) {
                if (sLoader == null) {
                    sLoader = new SkinLoader();
                }
            }
        }
        return sLoader;
    }

    public Resources getSkinResources() {
        return mSkinResources;
    }

    @Override
    public int getDrawableRes(String resName) {
        return mSkinResources.getIdentifier(resName, DEF_TYPE_DRAWABLE, mCurPluginPkgName);
    }

    @Override
    public int getColorRes(String resName) {
        return mSkinResources.getIdentifier(resName, DEF_TYPE_COLOR, mCurPluginPkgName);
    }

    @Override
    public Drawable getDrawable(String resName) {
        return mResourceManager.getDrawableByName(resName);
    }

    @Override
    public Drawable getDrawableWithRect(String resName, Rect rect) {
        Drawable drawable = getDrawable(resName);
        if (drawable != null) {
            drawable.setBounds(rect);
        }
        return drawable;
    }

    @Override
    public Drawable getDrawableWithPixels(String resName, int width, int height) {
        Rect rect = new Rect(0, 0, width, height);
        return getDrawableWithRect(resName, rect);
    }

    @Override
    public Drawable getDrawableWithSize(String resName, int size) {
        return getDrawableWithPixels(resName, size, size);
    }

    @Override
    public Bitmap getBitmap(String resName) {
        return BitmapFactory.decodeResource(mSkinResources, getDrawableRes(resName));
    }

    @Override
    public void setImageDrawable(ImageView imageView, String resName) {
        Drawable drawable = getDrawable(resName);
        if (drawable == null) {
            return;
        }
        imageView.setImageDrawable(drawable);
    }

    @Override
    public void setBackgroundDrawable(View view, String resName) {
        Drawable drawable = getDrawable(resName);
        if (drawable == null) {
            return;
        }
        view.setBackgroundDrawable(drawable);
    }
}

