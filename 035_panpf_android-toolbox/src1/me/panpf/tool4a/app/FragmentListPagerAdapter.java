/*
 * Copyright (C) 2017 Peng fei Pan <sky@panpf.me>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.panpf.tool4a.app;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;

public class FragmentListPagerAdapter extends FragmentPagerAdapter {
    private FragmentManager fragmentManager;
    private List<Fragment> fragments;
    private GetPageTitleListener getPageTitleListener;

    public FragmentListPagerAdapter(FragmentManager fragmentManager, List<Fragment> fragments) {
        super(fragmentManager);
        this.fragmentManager = fragmentManager;
        this.fragments = fragments;
    }

    public FragmentListPagerAdapter(FragmentManager fragmentManager, Fragment... fragments) {
        super(fragmentManager);
        this.fragmentManager = fragmentManager;
        this.fragments = new ArrayList<Fragment>(fragments.length);
        for (Fragment fragment : fragments) {
            this.fragments.add(fragment);
        }
    }

    @Override
    public Fragment getItem(int arg0) {
        return fragments.get(arg0);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public List<Fragment> getFragments() {
        return fragments;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (getPageTitleListener != null) {
            return getPageTitleListener.onGetPageTitle(position);
        } else {
            Fragment fragment = fragments.get(position);
            if (fragment instanceof TitleFragment) {
                return ((TitleFragment) fragment).pageTitle();
            } else {
                return super.getPageTitle(position);
            }
        }
    }

    public void setFragments(List<Fragment> fragmentsList) {
        if (fragments != null && fragments.size() > 0) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            for (Fragment f : this.fragments) {
                ft.remove(f);
            }
            ft.commit();
            ft = null;
            fragmentManager.executePendingTransactions();
        }

        this.fragments = fragmentsList;
    }

    public void setGetPageTitleListener(GetPageTitleListener getPageTitleListener) {
        this.getPageTitleListener = getPageTitleListener;
    }

    public interface TitleFragment {
        public CharSequence pageTitle();
    }
}