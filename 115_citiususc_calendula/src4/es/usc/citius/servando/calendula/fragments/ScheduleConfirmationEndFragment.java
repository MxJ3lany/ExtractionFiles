/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2014-2018 CiTIUS - University of Santiago de Compostela
 *
 *    Calendula is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.ConfirmSchedulesActivity;
import es.usc.citius.servando.calendula.activities.qrWrappers.PrescriptionWrapper;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Prescription;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.util.Strings;


public class ScheduleConfirmationEndFragment extends Fragment {

    private static final String TAG = "SchedulConfEndFragment";

    List<PrescriptionWrapper> prescriptions;

    LinearLayout createdSchedulesList;
    LinearLayout updatedSchedulesList;

    TextView createdSchedulesTitle;
    TextView updatedSchedulesTitle;

    public static ScheduleConfirmationEndFragment newInstance() {
        ScheduleConfirmationEndFragment fragment = new ScheduleConfirmationEndFragment();
        Bundle args = new Bundle();
        //args.putSerializable(ARG_PRESCRIPTION, pw);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prescriptions = ((ConfirmSchedulesActivity) getActivity()).getPrescriptions();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_schedule_confirmation_end, container, false);

        createdSchedulesList = (LinearLayout) rootView.findViewById(R.id.new_schedules);
        updatedSchedulesList = (LinearLayout) rootView.findViewById(R.id.updated_schedules);
        createdSchedulesTitle = (TextView) rootView.findViewById(R.id.new_schedules_title);
        updatedSchedulesTitle = (TextView) rootView.findViewById(R.id.updated_schedules_title);

        updateCounts();

        return rootView;
    }

    public String getMedicineName(PrescriptionWrapper p) {
        String name = "";
        if (p.cn != null) {
            Prescription prescription = DB.drugDB().prescriptions().findByCn(p.cn);
            name = prescription.shortName();
        } else if (p.isGroup) {
            name = Strings.firstPart(p.group.getName());
        }
        return name;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && getView() != null) {
            updateCounts();
        }
    }

    private void updateCounts() {
        Map<Schedule, PrescriptionWrapper> schedules = ((ConfirmSchedulesActivity) getActivity()).getScheduleInfo();
        int totalSchedules = schedules.size();
        int newSchedules = getNewSchedules(new ArrayList<>(schedules.keySet()));
        int updated = totalSchedules - newSchedules;

        createdSchedulesList.removeAllViews();
        updatedSchedulesList.removeAllViews();

        createdSchedulesTitle.setText(getString(R.string.scan_schedules_to_create, newSchedules));
        updatedSchedulesTitle.setText(getString(R.string.scan_schedules_to_update, updated));

        LayoutInflater li = getActivity().getLayoutInflater();

        for (Schedule s : schedules.keySet()) {
            if (s.getId() == null) {
                View v = getScheduleView(s, schedules.get(s), li);
                createdSchedulesList.addView(v);
            }
        }

        for (Schedule s : schedules.keySet()) {
            if (s.getId() != null) {
                View v = getScheduleView(s, schedules.get(s), li);
                updatedSchedulesList.addView(v);
            }
        }
    }

    private View getScheduleView(Schedule s, PrescriptionWrapper p, LayoutInflater li) {
        View v = li.inflate(R.layout.schedule_confirmation_end_list_item, null);
        ((TextView) v.findViewById(R.id.med_name)).setText(getMedicineName(p));
        ((TextView) v.findViewById(R.id.med_schedule)).setText(s.toReadableString(getActivity()));
        return v;
    }

    private int getNewSchedules(List<Schedule> schedules) {
        int count = 0;
        for (Schedule s : schedules) {
            if (s.getId() == null) {
                count++;
            }
        }
        return count;
    }
}