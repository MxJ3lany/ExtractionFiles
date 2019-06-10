/***
  Copyright (c) 2012-16 CommonsWare, LLC
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

package com.commonsware.android.rvp;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import com.commonsware.cwac.crossport.design.widget.TabLayout;

public class MainActivity extends Activity {
  private static final String STATE_ADAPTER="adapter";
  private final SnapHelper snapperCarr=new PagerSnapHelper();
  private PageAdapter adapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    final RecyclerView pager=(RecyclerView)findViewById(R.id.pager);
    final LinearLayoutManager layoutManager=new LinearLayoutManager(this,
      LinearLayoutManager.HORIZONTAL, false);

    pager.setLayoutManager(layoutManager);
    snapperCarr.attachToRecyclerView(pager);
    adapter=new PageAdapter(pager, getLayoutInflater());
    pager.setAdapter(adapter);

    final TabLayout tabs=(TabLayout)findViewById(R.id.tabs);

    for (int i=0;i<adapter.getItemCount();i++) {
      tabs.addTab(tabs.newTab().setText(adapter.getTabText(this, i)));
    }

    tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
      @Override
      public void onTabSelected(TabLayout.Tab tab) {
        pager.smoothScrollToPosition(tab.getPosition());
      }

      @Override
      public void onTabUnselected(TabLayout.Tab tab) {
        // unused
      }

      @Override
      public void onTabReselected(TabLayout.Tab tab) {
        // unused
      }
    });

    pager.setOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        int tab=layoutManager.findFirstCompletelyVisibleItemPosition();

        if (tab>=0 && tab<tabs.getTabCount()) {
          tabs.getTabAt(tab).select();
        }
      }
    });
  }

  @Override
  protected void onSaveInstanceState(Bundle state) {
    super.onSaveInstanceState(state);

    Bundle adapterState=new Bundle();

    adapter.onSaveInstanceState(adapterState);
    state.putBundle(STATE_ADAPTER, adapterState);
  }

  @Override
  protected void onRestoreInstanceState(Bundle state) {
    super.onRestoreInstanceState(state);

    adapter.onRestoreInstanceState(state.getBundle(STATE_ADAPTER));
  }
}