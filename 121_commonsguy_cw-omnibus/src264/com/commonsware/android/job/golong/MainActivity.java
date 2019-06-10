/***
 Copyright (c) 2017 CommonsWare, LLC
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

package com.commonsware.android.job.golong;

import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity {
  private static final int JOB_ID=1337;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    JobScheduler jobs=
      (JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);
    ComponentName cn=new ComponentName(this, LongJobService.class);
    JobInfo.Builder b=new JobInfo.Builder(JOB_ID, cn)
      .setMinimumLatency(30000)
      .setOverrideDeadline(60000)
      .setPersisted(false)
      .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
      .setRequiresCharging(false)
      .setRequiresDeviceIdle(false);

    jobs.schedule(b.build());

    Toast.makeText(this, R.string.msg_start, Toast.LENGTH_LONG).show();
    finish();
  }
}
