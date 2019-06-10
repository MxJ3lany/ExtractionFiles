/*
 * Copyright (C) 2019 Peter Gregus for GravityBox Project (C3C076@xda)
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
package com.ceco.pie.gravitybox;

import com.ceco.pie.gravitybox.ledcontrol.QuietHours;
import com.ceco.pie.gravitybox.managers.SysUiManagers;
import com.ceco.pie.gravitybox.managers.StatusbarQuietHoursManager.QuietHoursListener;

public class StatusbarQuietHoursIcon implements  QuietHoursListener {

    private static final String SLOT = "gravitybox.quiet_hours";

    private QuietHours mQuietHours;
    private int mCurrentDrawableId;
    private SystemIconController mSysIconCtrl;

    StatusbarQuietHoursIcon(SystemIconController sysIconCtrl) {
        mSysIconCtrl = sysIconCtrl;
        mQuietHours = SysUiManagers.QuietHoursManager.getQuietHours();
        SysUiManagers.QuietHoursManager.registerListener(this);
        updateIcon();
    }

    @Override
    public void onQuietHoursChanged() {
        mQuietHours = SysUiManagers.QuietHoursManager.getQuietHours();
        updateIcon();
    }

    @Override
    public void onTimeTick() {
        updateIcon();
    }

    private void updateIcon() {
        if (mQuietHours.showStatusbarIcon && mQuietHours.quietHoursActive()) {
            final int oldDrawableId = mCurrentDrawableId;
            if (mQuietHours.mode == QuietHours.Mode.WEAR) {
                mCurrentDrawableId = R.drawable.stat_sys_quiet_hours_wear;
            } else {
                mCurrentDrawableId = R.drawable.stat_sys_quiet_hours;
            }
            if (oldDrawableId != mCurrentDrawableId) {
                mSysIconCtrl.setIcon(SLOT, mCurrentDrawableId);
            }
        } else {
            mCurrentDrawableId = 0;
            mSysIconCtrl.removeIcon(SLOT);
        }
    }

    public void destroy() {
        SysUiManagers.QuietHoursManager.unregisterListener(this);
        mSysIconCtrl.removeIcon(SLOT);
        mSysIconCtrl = null;
        mQuietHours = null;
    }
}
