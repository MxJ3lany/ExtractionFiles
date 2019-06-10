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

package es.usc.citius.servando.calendula.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import butterknife.ButterKnife;
import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.fragments.RoutineCreateOrEditFragment;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.scheduling.AlarmScheduler;
import es.usc.citius.servando.calendula.util.FragmentUtils;

public class RoutinesActivity extends CalendulaActivity implements RoutineCreateOrEditFragment.OnRoutineEditListener {

    RoutineCreateOrEditFragment routineFragment;

    MenuItem removeItem;

    long mRoutineId;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.telt
        getMenuInflater().inflate(R.menu.routines, menu);
        removeItem = menu.findItem(R.id.action_remove);
        removeItem.setVisible(mRoutineId != -1);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_remove:
                routineFragment.showDeleteConfirmationDialog(Routine.findById(mRoutineId));
                break;
            default:
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onRoutineEdited(Routine r) {
        AlarmScheduler.instance().onCreateOrUpdateRoutine(r, this);
        //Snack.show(getString(R.string.routine_edited_message),this);
        finish();
    }

    @Override
    public void onRoutineDeleted(Routine r) {
        AlarmScheduler.instance().onDeleteRoutine(r, this);
        DB.routines().deleteCascade(r, true);
        ReminderNotification.cancel(this, ReminderNotification.routineNotificationId(r.getId().intValue()));
        finish();
    }

    @Override
    public void onRoutineCreated(Routine r) {
        AlarmScheduler.instance().onCreateOrUpdateRoutine(r, this);
        //Snack.show(getString(R.string.routine_created_message),this);
        // send result to caller activity
        finish();
    }

    Fragment getViewPagerFragment(int position) {
        return getSupportFragmentManager().findFragmentByTag(FragmentUtils.makeViewPagerFragmentName(R.id.pager, position));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routines);
        ButterKnife.bind(this);

        int color = DB.patients().getActive(this).getColor();
        setupToolbar(null, color);
        setupStatusBar(color);

        processIntent();

        routineFragment = (RoutineCreateOrEditFragment) getSupportFragmentManager().findFragmentById(R.id.routine_fragment);
        routineFragment.setRoutine(mRoutineId);

        TextView title = ((TextView) findViewById(R.id.textView2));
        title.setBackgroundColor(color);
        title.setText(getString(mRoutineId != -1 ? R.string.title_edit_routine_activity : R.string.create_routine_button_text));


        findViewById(R.id.add_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routineFragment.onEdit();
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    private void processIntent() {
        mRoutineId = getIntent().getLongExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, -1);
    }

}
