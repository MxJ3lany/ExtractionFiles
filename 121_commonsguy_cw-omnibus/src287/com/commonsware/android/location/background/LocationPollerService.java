/***
 Copyright (c) 2017 CommonsWare, LLC
 Licensed under the Apache License, Version 2.0 (the "License"); you may not
 use this file except in compliance with the License. You may obtain	a copy
 of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS,	WITHOUT	WARRANTIES OR CONDITIONS
 OF ANY KIND, either express or implied. See the License for the specific
 language governing permissions and limitations under the License.

 Covered in detail in the book _The Busy Coder's Guide to Android Development_
 https://commonsware.com/Android
 */

package com.commonsware.android.location.background;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import com.elvishew.xlog.XLog;

public class LocationPollerService extends Service implements LocationListener {
  static int NOTIFY_ID=1337;
  private static final String PREF_DELAY="delay";
  private static final String PREF_FOREGROUND="foreground";
  private LocationManager mgr;
  private SharedPreferences prefs;

  static Notification buildNotification(Context ctxt) {
    NotificationCompat.Builder b=new NotificationCompat.Builder(ctxt)
      .setOngoing(true)
      .setContentTitle(ctxt.getString(R.string.notif_title))
      .setContentText(ctxt.getString(R.string.notif_text))
      .setSmallIcon(android.R.drawable.stat_sys_warning);

    return(b.build());
  }

  @SuppressWarnings("MissingPermission")
  @Override
  public void onCreate() {
    super.onCreate();

    prefs=PreferenceManager.getDefaultSharedPreferences(this);
    mgr=(LocationManager)getSystemService(LOCATION_SERVICE);

    String delay=prefs.getString(PREF_DELAY, "60000");

    mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,
      Integer.parseInt(delay), 0, this);

    if (Build.VERSION.SDK_INT<=Build.VERSION_CODES.N_MR1
        && prefs.getBoolean(PREF_FOREGROUND, false)) {
      startForeground(NOTIFY_ID, buildNotification(this));
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  @Override
  public IBinder onBind(Intent intent) {
    throw new UnsupportedOperationException("Wait, wut?");
  }

  @Override
  public void onLocationChanged(Location location) {
    XLog.d("Location: %f/%f", location.getLatitude(), location.getLongitude());
  }

  @Override
  public void onStatusChanged(String provider, int status, Bundle extras) {
    // unused
  }

  @Override
  public void onProviderEnabled(String provider) {
    // unused
  }

  @Override
  public void onProviderDisabled(String provider) {
    // unused
  }
}
