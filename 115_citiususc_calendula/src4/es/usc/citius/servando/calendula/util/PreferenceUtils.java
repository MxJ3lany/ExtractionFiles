/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2014-2018 CiTIUS - University of Santiago de Compostela
 *
 *    Calendula is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Set;

import es.usc.citius.servando.calendula.R;

/**
 * Utility to access default shared preferences
 */
public class PreferenceUtils {

    private static PreferenceUtils instance;
    private final Context context;

    private PreferenceUtils(Context context) {
        this.context = context;
    }

    /**
     * Initializes this object and the default preferences.
     * MUST be called on app startup, preferably before launching any activity.
     *
     * @param context a {@link Context} for the appplication
     */
    public synchronized static void init(Context context) {
        if (instance == null) {
            instance = new PreferenceUtils(context);
            if (!getBoolean(PreferenceKeys.SETTINGS_DEFAULTS_LOADED, false)) {
                PreferenceManager.setDefaultValues(context, R.xml.pref_main, true);
                PreferenceManager.setDefaultValues(context, R.xml.pref_privacy, true);
                PreferenceManager.setDefaultValues(context, R.xml.pref_database, true);
                PreferenceManager.setDefaultValues(context, R.xml.pref_notifications, true);
                edit().putBoolean(PreferenceKeys.SETTINGS_DEFAULTS_LOADED.key(), true).apply();
            }
        }
    }

    /**
     * @return a singleton instance of this class
     * @throws IllegalStateException if {@link #init(Context)} hasn't been called yet
     */
    public static PreferenceUtils instance() throws IllegalStateException {
        if (instance == null)
            throw new IllegalStateException("PreferenceUtils must be initialized before use!");
        return instance;
    }

    public static boolean getBoolean(PreferenceKeys key, boolean defVal) {
        return instance().preferences().getBoolean(key.key(), defVal);
    }

    public static int getInt(PreferenceKeys key, int defVal) {
        return instance().preferences().getInt(key.key(), defVal);
    }

    public static long getLong(PreferenceKeys key, long defVal) {
        return instance().preferences().getLong(key.key(), defVal);
    }

    public static float getFloat(PreferenceKeys key, float defVal) {
        return instance().preferences().getFloat(key.key(), defVal);
    }

    public static String getString(PreferenceKeys key, String defVal) {
        return instance().preferences().getString(key.key(), defVal);
    }

    public static Set<String> getStringSet(PreferenceKeys key, Set<String> defVal) {
        return instance().preferences().getStringSet(key.key(), defVal);
    }

    public static SharedPreferences.Editor edit() {
        return instance().preferences().edit();
    }

    public SharedPreferences preferences() {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

}
