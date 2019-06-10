/**
 * ownCloud Android client application
 *
 * @author Bartek Przybylski
 * Copyright (C) 2011  Bartek Przybylski
 * Copyright (C) 2016 ownCloud GmbH.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2,
 * as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.owncloud.android.ui;

import android.graphics.drawable.Drawable;
import android.view.View.OnClickListener;

/**
 * Represents an Item on the ActionBar.
 */
public class ActionItem {
    private Drawable mIcon;
    private String mTitle;
    private OnClickListener mClickListener;

    public ActionItem() {
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setIcon(Drawable icon) {
        mIcon = icon;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public void setOnClickListener(OnClickListener listener) {
        mClickListener = listener;
    }

    public OnClickListener getOnClickListerner() {
        return mClickListener;
    }

}
