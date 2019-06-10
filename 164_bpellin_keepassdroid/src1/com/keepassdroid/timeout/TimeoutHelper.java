/*
 * Copyright 2012-2018 Brian Pellin.
 *     
 * This file is part of KeePassDroid.
 *
 *  KeePassDroid is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  KeePassDroid is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with KeePassDroid.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.keepassdroid.timeout;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.android.keepass.KeePass;
import com.android.keepass.R;
import com.keepassdroid.app.App;
import com.keepassdroid.timers.Timeout;

public class TimeoutHelper {
	
	private static final long DEFAULT_TIMEOUT = 5 * 60 * 1000;  // 5 minutes
	
	public static void pause(Activity act) {
		// Record timeout time in case timeout service is killed
		long time = System.currentTimeMillis();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act);
		SharedPreferences.Editor edit = prefs.edit();
		edit.putLong(act.getString(R.string.timeout_key), time);

		edit.apply();

		if ( App.getDB().Loaded() ) {
	        Timeout.start(act);
		}

	}

	public static long getTimeoutLength(Context ctx) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		String sTimeout = prefs.getString(ctx.getString(R.string.app_timeout_key), ctx.getString(R.string.clipboard_timeout_default));
		long timeout;
		try {
			timeout = Long.parseLong(sTimeout);
		} catch (NumberFormatException e) {
			timeout = DEFAULT_TIMEOUT;
		}

		return timeout;
	}

	public static void resume(Activity act) {
		if ( App.getDB().Loaded() ) {
	        Timeout.cancel(act);
		}

		
		// Check whether the timeout has expired
		long cur_time = System.currentTimeMillis();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act);
		long timeout_start = prefs.getLong(act.getString(R.string.timeout_key), -1);
		// The timeout never started
		if (timeout_start == -1) {
			return;
		}
		
		
		long timeout = getTimeoutLength(act);
		// We are set to never timeout
		if (timeout == -1) {
			return;
		}
		
		long diff = cur_time - timeout_start;
		if (diff >= timeout) {
			// We have timed out
			App.setShutdown();
		}
	}

	public static void checkShutdown(Activity act) {
		if ( App.isShutdown() && App.getDB().Loaded() ) {
			act.setResult(KeePass.EXIT_LOCK);
			act.finish();
		}
	}
}
