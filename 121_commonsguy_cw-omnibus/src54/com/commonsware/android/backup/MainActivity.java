/***
  Copyright (c) 2012-2015 CommonsWare, LLC
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

package com.commonsware.android.backup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends Activity  {
  private static final String PREF_LAST_VISITED="lastVisited";
  private SharedPreferences prefs;
  private ViewPager pager;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    pager=findViewById(R.id.pager);
    pager.setAdapter(
      new SampleAdapter(this, getFragmentManager()));
  }

  @Override
  protected void onStart() {
    super.onStart();

    EventBus.getDefault().register(this);

    if (prefs==null) {
      new PrefsLoadThread(this).start();
    }
  }

  @Override
  protected void onStop() {
    EventBus.getDefault().unregister(this);

    if (prefs!=null) {
      prefs
        .edit()
        .putInt(PREF_LAST_VISITED, pager.getCurrentItem())
        .apply();
    }

    super.onStop();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.actions, menu);

    return(super.onCreateOptionsMenu(menu));
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId()==R.id.backup) {
      BackupService.enqueueWork(this);

      return(true);
    }
    else if (item.getItemId()==R.id.restore) {
      startActivity(new Intent(this, RestoreRosterActivity.class));

      return(true);
    }

    return(super.onOptionsItemSelected(item));
  }

  @Subscribe(threadMode =ThreadMode.MAIN)
  public void onCompleted(BackupService.BackupCompletedEvent event) {
    Toast
      .makeText(this, R.string.msg_backup_completed, Toast.LENGTH_LONG)
      .show();
  }

  @Subscribe(threadMode =ThreadMode.MAIN)
  public void onFailed(BackupService.BackupFailedEvent event) {
    Toast
      .makeText(this, R.string.msg_backup_failed, Toast.LENGTH_LONG)
      .show();
  }

  @Subscribe(threadMode =ThreadMode.MAIN)
  public void onPrefsLoaded(PrefsLoadedEvent event) {
    this.prefs=event.prefs;

    int lastVisited=prefs.getInt(PREF_LAST_VISITED, -1);

    if (lastVisited>-1) {
      pager.setCurrentItem(lastVisited);
    }
  }

  private static class PrefsLoadThread extends Thread {
    private final Context ctxt;

    PrefsLoadThread(Context ctxt) {
      this.ctxt=ctxt.getApplicationContext();
    }

    @Override
    public void run() {
      SharedPreferences prefs=
        PreferenceManager.getDefaultSharedPreferences(ctxt);
      PrefsLoadedEvent event=new PrefsLoadedEvent(prefs);

      EventBus.getDefault().post(event);
    }
  }

  private static class PrefsLoadedEvent {
    private final SharedPreferences prefs;

    PrefsLoadedEvent(SharedPreferences prefs) {
      this.prefs=prefs;
    }
  }
}