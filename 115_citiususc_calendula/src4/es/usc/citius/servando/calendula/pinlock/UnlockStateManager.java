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

package es.usc.citius.servando.calendula.pinlock;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;

/**
 * Utility class to handle unlock state (for PIN lock)
 */
public class UnlockStateManager {

    private final static String DEFAULT_UNLOCK_DURATION_SECONDS = "120";
    private final static String TAG = "UnlockStateManager";
    private static UnlockStateManager theInstance;
    private DateTime unlockTimestamp;

    private UnlockStateManager() {

    }

    public static UnlockStateManager getInstance() {
        if (theInstance == null) {
            theInstance = new UnlockStateManager();
        }
        return theInstance;
    }

    /**
     * Stores unlock status
     */
    public void unlock() {
        LogUtil.v(TAG, "unlock() called");
        if (unlockTimestamp != null)
            LogUtil.d(TAG, "unlock: timestamp was already set. Overwriting.");
        unlockTimestamp = DateTime.now();
    }


    /**
     * Stores lock status
     */
    public void lock() {
        LogUtil.v(TAG, "lock() called");
        if (unlockTimestamp != null) {
            unlockTimestamp = null;
        } else {
            LogUtil.d(TAG, "lock: timestamp wasn't set. Doing nothing.");
        }
    }

    /**
     * Checks if unlock is set and not expired
     *
     * @return <code>true</code> if unlocked, false otherwise.
     */
    public boolean isUnlocked() {
        LogUtil.v(TAG, "isUnlocked() called");
        if (unlockTimestamp != null) {
            Duration diff = new Duration(unlockTimestamp, DateTime.now());
            // need to be a string to use a ListPreference
            final String maxDurationStr = PreferenceUtils.getString(PreferenceKeys.UNLOCK_PIN_TIMEOUT, DEFAULT_UNLOCK_DURATION_SECONDS);
            Duration maxDuration = Duration.standardSeconds(Integer.parseInt(maxDurationStr));
            LogUtil.d(TAG, "Max unlock duration is " + maxDuration.toString());
            if (diff.compareTo(maxDuration) <= 0) {
                LogUtil.d(TAG, "isUnlocked() returned: " + true);
                return true;
            } else {
                LogUtil.d(TAG, "isUnlocked: unlock has expired, unsetting and returning null");
                unlockTimestamp = null;
                return false;
            }
        } else {
            LogUtil.d(TAG, "isUnlocked: timestamp is null");
        }
        return false;
    }


}
