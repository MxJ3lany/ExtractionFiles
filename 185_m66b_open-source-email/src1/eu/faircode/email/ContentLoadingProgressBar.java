package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ContentLoadingProgressBar extends ProgressBar {
    private int visibility;

    private static final int VISIBILITY_DELAY = 500; // milliseconds

    public ContentLoadingProgressBar(@NonNull Context context) {
        this(context, null);
    }

    public ContentLoadingProgressBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
    }

    @Override
    public void setVisibility(int visibility) {
        this.visibility = visibility;
        if (false && BuildConfig.DEBUG) {
            super.setVisibility(visibility);
            return;
        }
        removeCallbacks(delayedShow);
        if (visibility == VISIBLE) {
            super.setVisibility(INVISIBLE);
            postDelayed(delayedShow, VISIBILITY_DELAY);
        } else
            super.setVisibility(visibility);
    }

    private final Runnable delayedShow = new Runnable() {
        @Override
        public void run() {
            if (visibility == VISIBLE)
                ContentLoadingProgressBar.super.setVisibility(VISIBLE);
        }
    };
}
