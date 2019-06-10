/*
 * Copyright (C) 2017 Peter Gregus for GravityBox Project (C3C076@xda)
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

package com.ceco.pie.gravitybox.quicksettings;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;

public class DoNotDisturbTile extends AospTile {
    public static final String AOSP_KEY = "dnd";

    protected DoNotDisturbTile(Object host, String key, Object tile, XSharedPreferences prefs,
            QsTileEventDistributor eventDistributor) throws Throwable {
        super(host, key, tile, prefs, eventDistributor);
    }

    @Override
    public void handleUpdateState(Object state, Object arg) {
        super.handleUpdateState(state, arg);
        XposedHelpers.setBooleanField(state, "dualTarget", false);
    }

    @Override
    public String getSettingsKey() {
        return null;
    }
}
