/***
 Copyright (c) 2017 CommonsWare, LLC
 Licensed under the Apache License, Version 2.0 (the "License"); you may not
 use this file except in compliance with the License. You may obtain a copy
 of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 OF ANY KIND, either express or implied. See the License for the specific
 language governing permissions and limitations under the License.

 Covered in detail in the book _Android's Architecture Components_
 https://commonsware.com/AndroidArch
 */

package com.commonsware.android.todo;

import android.app.Application;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import com.squareup.leakcanary.LeakCanary;

public class App extends Application {
  @Override
  public void onCreate() {
    super.onCreate();

    if (LeakCanary.isInAnalyzerProcess(this)) {
      return;
    }

    LeakCanary.install(this);

    new Handler().postAtFrontOfQueue(this::enableStrictMode);
  }

  private void enableStrictMode() {
    if (BuildConfig.DEBUG && !"samsung".equals(Build.MANUFACTURER)) {
      StrictMode.ThreadPolicy.Builder b=new StrictMode.ThreadPolicy.Builder()
        .detectAll();

      if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
        b.penaltyDeath();
      }
      else {
        b.penaltyLog().penaltyFlashScreen();
      }

      StrictMode.setThreadPolicy(b.build());
    }
    else {
      StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        .detectAll()
        .penaltyLog()
        .build());
    }
  }
}
