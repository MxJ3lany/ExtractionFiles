/***
  Copyright (c) 2012 CommonsWare, LLC
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

package com.commonsware.android.tilemode;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity implements TabListener {
  private static final int TABS[]= { R.string._default, R.string.clamp,
      R.string.repeat, R.string.mirror };
  private static final int DRAWABLES[]= { R.drawable._default,
      R.drawable.clamp, R.drawable.repeat, R.drawable.mirror };
  private View widget=null;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    widget=findViewById(R.id.widget);

    ActionBar bar=getActionBar();
    bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

    for (int i=0; i < TABS.length; i++) {
      bar.addTab(bar.newTab().setText(getString(TABS[i]))
                    .setTabListener(this));
    }
  }

  @Override
  public void onTabSelected(Tab tab, FragmentTransaction ft) {
    widget.setBackgroundResource(DRAWABLES[tab.getPosition()]);
  }

  @Override
  public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    // no-op
  }

  @Override
  public void onTabReselected(Tab tab, FragmentTransaction ft) {
    // no-op
  }
}
