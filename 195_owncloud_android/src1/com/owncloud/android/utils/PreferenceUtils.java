/**
 * ownCloud Android client application
 *
 * @author David González Verdugo
 * Copyright (C) 2019 ownCloud GmbH.
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

package com.owncloud.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.owncloud.android.ui.activity.Preferences;

public class PreferenceUtils {
    public static boolean shouldDisallowTouchesWithOtherVisibleWindows(Context context) {
        SharedPreferences appPrefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return !appPrefs.getBoolean(Preferences.PREFERENCE_TOUCHES_WITH_OTHER_VISIBLE_WINDOWS, false);
    }
}
