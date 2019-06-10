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

import java.util.List;

public class SimpleFragmentPagerAdapter<T> extends FragmentPagerAdapter {
    private List<T> datas;
    private Class<? extends Fragment> fragmentClass;
    private ArgumentsFactory<? super T> argumentsFactory;
    private GetPageTitleListener getPageTitleListener;

    public SimpleFragmentPagerAdapter(FragmentManager fm, Class<? extends Fragment> fragmentClass, List<T> datas, ArgumentsFactory<T> argumentsFactory) {
        super(fm);
        this.datas = datas;
        this.fragmentClass = fragmentClass;
        this.argumentsFactory = argumentsFactory;
    }

    @Override
    public Fragment getItem(int i) {
        try {
            Fragment fragment = fragmentClass.newInstance();
            if (argumentsFactory != null) {
                fragment.setArguments(argumentsFactory.onCreateArguments(datas.get(i)));
            }
            return fragment;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getCount() {
        return datas != null ? datas.size() : 0;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (getPageTitleListener != null) {
            return getPageTitleListener.onGetPageTitle(position);
        } else {
            return super.getPageTitle(position);
        }
    }

    public List<T> getDatas() {
        return datas;
    }

    public void setDatas(List<T> datas) {
        this.datas = datas;
    }

    public void setFragmentClass(Class<? extends Fragment> fragmentClass) {
        this.fragmentClass = fragmentClass;
    }

    public void setArgumentsFactory(ArgumentsFactory<? super T> argumentsFactory) {
        this.argumentsFactory = argumentsFactory;
    }

    public void setGetPageTitleListener(GetPageTitleListener getPageTitleListener) {
        this.getPageTitleListener = getPageTitleListener;
    }
}
