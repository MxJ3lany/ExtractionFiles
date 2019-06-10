/***
  Copyright (c) 2013 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.
  
  Covered in detail in the book _The Busy Coder's Guide to Android Development_
    https://commonsware.com/Android
 */

package com.commonsware.android.eventbus.otto;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import java.util.Random;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.squareup.otto.Bus;
import com.squareup.otto.DeadEvent;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

public class ScheduledService extends WakefulIntentService {
  static final Bus bus=new Bus(ThreadEnforcer.ANY);
  private static int NOTIFY_ID=1337;
  private Random rng=new Random();

  public ScheduledService() {
    super("ScheduledService");
  }
  
  @Override
  public void onCreate() {
    super.onCreate();
    
    bus.register(this);
  }

  @Override
  protected void doWakefulWork(Intent intent) {
    bus.post(new RandomEvent(rng.nextInt()));
  }

  @Override
  public void onDestroy() {
    bus.unregister(this);

    super.onDestroy();
  }

  @Subscribe
  public void onDeadEvent(DeadEvent braiiiiiiinz) {
    RandomEvent original=(RandomEvent)braiiiiiiinz.event;
    NotificationCompat.Builder b=new NotificationCompat.Builder(this);
    Intent ui=new Intent(this, EventDemoActivity.class);

    b.setAutoCancel(true).setDefaults(Notification.DEFAULT_SOUND)
     .setContentTitle(getString(R.string.notif_title))
     .setContentText(Integer.toHexString(original.value))
     .setSmallIcon(android.R.drawable.stat_notify_more)
     .setTicker(getString(R.string.notif_title))
     .setContentIntent(PendingIntent.getActivity(this, 0, ui, 0));

    NotificationManager mgr=
        (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

    mgr.notify(NOTIFY_ID, b.build());
  }
}
