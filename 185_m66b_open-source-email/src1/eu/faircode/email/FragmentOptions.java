package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

public class FragmentOptions extends FragmentBase {
    private ViewPager pager;
    private PagerAdapter adapter;

    static String[] OPTIONS_RESTART = new String[]{
            "startup", "date", "threading", "avatars", "identicons", "circular", "name_email", "subject_italic", "flags", "preview",
            "addresses", "attachments_alt", "monospaced", "autohtml", "autoimages", "actionbar",
            "pull", "autoscroll", "swipenav", "autoexpand", "autoclose", "autonext",
            "subscriptions",
            "authentication", "debug"
    };

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.title_advanced);

        View view = inflater.inflate(R.layout.fragment_options, container, false);

        pager = view.findViewById(R.id.pager);
        adapter = new PagerAdapter(getChildFragmentManager());
        pager.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(pager);
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 7;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new FragmentOptionsSynchronize();
                case 1:
                    return new FragmentOptionsSend();
                case 2:
                    return new FragmentOptionsConnection();
                case 3:
                    return new FragmentOptionsDisplay();
                case 4:
                    return new FragmentOptionsBehavior();
                case 5:
                    return new FragmentOptionsNotifications();
                case 6:
                    return new FragmentOptionsMisc();
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_advanced_section_synchronize);
                case 1:
                    return getString(R.string.title_advanced_section_send);
                case 2:
                    return getString(R.string.title_advanced_section_connection);
                case 3:
                    return getString(R.string.title_advanced_section_display);
                case 4:
                    return getString(R.string.title_advanced_section_behavior);
                case 5:
                    return getString(R.string.title_advanced_section_notifications);
                case 6:
                    return getString(R.string.title_advanced_section_misc);
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE; // always recreate fragment
        }
    }
}
