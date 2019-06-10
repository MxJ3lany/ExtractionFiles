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


import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.ReminderNotification;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.scheduling.AlarmScheduler;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.LogUtil;

public class RoutinesListFragment extends Fragment {


    private static final String TAG = "RoutinesListFragment";
    List<Routine> mRoutines;
    OnRoutineSelectedListener mRoutineSelectedCallback;
    ArrayAdapter adapter;
    ListView listview;

    Drawable ic;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_routines_list, container, false);
        listview = (ListView) rootView.findViewById(R.id.routines_list);

        View empty = rootView.findViewById(android.R.id.empty);
        listview.setEmptyView(empty);
        mRoutines = new ArrayList<>();
        ic = new IconicsDrawable(getContext())
                .icon(CommunityMaterial.Icon.cmd_clock)
                .colorRes(R.color.agenda_item_title)
                .paddingDp(8)
                .sizeDp(40);

        adapter = new RoutinesListAdapter(getActivity(), R.layout.routines_list_item, mRoutines);
        listview.setAdapter(adapter);
        new ReloadItemsTask().execute();
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        LogUtil.d(TAG, "Activity " + activity.getClass().getName() + ", " + (activity instanceof OnRoutineSelectedListener));
        // If the container activity has implemented
        // the callback interface, set it as listener
        if (activity instanceof OnRoutineSelectedListener) {
            mRoutineSelectedCallback = (OnRoutineSelectedListener) activity;
        }
    }

    public void notifyDataChange() {
        LogUtil.d(TAG, "Routines - Notify data change");
        new ReloadItemsTask().execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        CalendulaApp.eventBus().register(this);
    }

    @Override
    public void onStop() {
        CalendulaApp.eventBus().unregister(this);
        super.onStop();
    }

    // Method called from the event bus
    @SuppressWarnings("unused")
    @Subscribe
    public void handleActiveUserChange(final PersistenceEvents.ActiveUserChangeEvent event) {
        notifyDataChange();
    }

    void showDeleteConfirmationDialog(final Routine r) {

        String message;
        if (r.getScheduleItems().size() > 0) {
            message = String.format(getString(R.string.remove_routine_message_long), r.getName());
        } else {
            message = String.format(getString(R.string.remove_routine_message_short), r.getName());
        }

        new MaterialStyledDialog.Builder(getActivity())
                .setTitle("")
                .setStyle(Style.HEADER_WITH_ICON)
                .setIcon(IconUtils.icon(getActivity(), CommunityMaterial.Icon.cmd_clock, R.color.white, 100))
                .setHeaderColor(R.color.android_red)
                .withDialogAnimation(true)
                .setTitle(getString(R.string.remove_routine_dialog_title))
                .setDescription(message)
                .setCancelable(true)
                .setNeutralText(getString(R.string.dialog_no_option))
                .setPositiveText(getString(R.string.dialog_yes_option))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        AlarmScheduler.instance().onDeleteRoutine(r, getActivity());
                        DB.routines().deleteCascade(r, true);
                        ReminderNotification.cancel(getContext(), ReminderNotification.routineNotificationId(r.getId().intValue()));
                        notifyDataChange();
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.cancel();
                    }
                })
                .show();

    }

    private View createRoutineListItem(LayoutInflater inflater, final Routine routine) {

        int hour = routine.getTime().getHourOfDay();
        int minute = routine.getTime().getMinuteOfHour();

        String strHour = String.valueOf(hour >= 10 ? hour : "0" + hour);
        String strMinute = ":" + String.valueOf(minute >= 10 ? minute : "0" + minute);

        View item = inflater.inflate(R.layout.routines_list_item, null);

        ((TextView) item.findViewById(R.id.routines_list_item_hour)).setText(strHour);
        ((TextView) item.findViewById(R.id.routines_list_item_minute)).setText(strMinute);
        ((TextView) item.findViewById(R.id.routines_list_item_name)).setText(routine.getName());
        ((ImageButton) item.findViewById(R.id.imageButton2)).setImageDrawable(ic);

        int items = routine.getScheduleItems().size();

        String schedules = items > 0 ? getString(R.string.schedules_for_med, items) : getString(R.string.schedules_for_med_none);
        ((TextView) item.findViewById(R.id.routines_list_item_subtitle)).setText(schedules);
        View overlay = item.findViewById(R.id.routine_list_item_container);
        overlay.setTag(routine);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Routine r = (Routine) view.getTag();
                if (mRoutineSelectedCallback != null && r != null) {
                    LogUtil.d(TAG, "Click at " + r.getName());
                    mRoutineSelectedCallback.onRoutineSelected(r);
                } else {
                    LogUtil.d(TAG, "No callback set");
                }

            }
        };

        overlay.setOnClickListener(clickListener);
        overlay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (view.getTag() != null)
                    showDeleteConfirmationDialog((Routine) view.getTag());
                return true;
            }
        });
        return item;
    }


    // Container Activity must implement this interface
    public interface OnRoutineSelectedListener {
        void onRoutineSelected(Routine r);

        void onCreateRoutine();
    }

    private class RoutinesListAdapter extends ArrayAdapter<Routine> {

        public RoutinesListAdapter(Context context, int layoutResourceId, List<Routine> items) {
            super(context, layoutResourceId, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final LayoutInflater layoutInflater = getActivity().getLayoutInflater();
            return createRoutineListItem(layoutInflater, mRoutines.get(position));
        }

    }

    private class ReloadItemsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mRoutines.clear();
            mRoutines.addAll(DB.routines().findAllForActivePatient(getContext()));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter.notifyDataSetChanged();
        }
    }


}