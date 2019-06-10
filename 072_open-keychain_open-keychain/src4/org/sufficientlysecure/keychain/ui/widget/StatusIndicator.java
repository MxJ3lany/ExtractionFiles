/*
 * Copyright (C) 2017 Schürmann & Breitmoser GbR
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.sufficientlysecure.keychain.ui.widget;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.AnimationUtils;

import org.sufficientlysecure.keychain.R;


public class StatusIndicator extends ToolableViewAnimator {

    public enum Status {
        IDLE, PROGRESS, OK, ERROR
    }

    public StatusIndicator(Context context) {
        super(context);

        LayoutInflater.from(context).inflate(R.layout.status_indicator, this, true);
        setInAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in));
        setOutAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out));
    }

    public StatusIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.status_indicator, this, true);
        setInAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in));
        setOutAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out));
    }

    @Override
    public void setDisplayedChild(int whichChild) {
        if (whichChild != getDisplayedChild()) {
            super.setDisplayedChild(whichChild);
        }
    }

    public void setDisplayedChild(Status status) {
        setDisplayedChild(status.ordinal());
    }

}
