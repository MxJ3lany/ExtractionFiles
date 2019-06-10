package me.panpf.tool4a.app;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

public class FragmentUtils {
    public static Fragment getFragmentPager(FragmentManager fragmentManager, ViewPager viewPager, int position) {
        return fragmentManager.findFragmentByTag("android:switcher:" + viewPager.getId() + ":" + position);
    }
}
