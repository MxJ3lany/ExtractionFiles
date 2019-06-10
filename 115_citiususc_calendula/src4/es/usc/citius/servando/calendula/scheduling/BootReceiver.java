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

package es.usc.citius.servando.calendula.scheduling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import es.usc.citius.servando.calendula.util.LogUtil;

/**
 * This class receives our routine alarms
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.d(TAG, "onReceive: " + intent.getAction());

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            LogUtil.d(TAG, "Boot completed intent received");
            // Update alarms
            AlarmScheduler.instance().updateAllAlarms(context);
            LogUtil.d(TAG, "Alarms updated!");
        } else if ("android.intent.action.MY_PACKAGE_REPLACED".equals(intent.getAction())) {
            LogUtil.d(TAG, "Package received intent received");
            AlarmScheduler.instance().updateAllAlarms(context);
        }

    }
}