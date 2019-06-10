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

package es.usc.citius.servando.calendula.scheduling;

import android.content.Context;
import android.content.SharedPreferences;

import com.j256.ormlite.misc.TransactionManager;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.sql.SQLException;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.util.LogUtil;

public class DailyAgenda {

    private static final String TAG = "DailyAgenda";

    private static final String PREFERENCES_NAME = "DailyAgendaPreferences";
    private static final String PREF_LAST_DATE = "LastDate";

    private static final int NEXT_DAYS_TO_SHOW = 1; // show tomorrow
    private static final DailyAgenda instance = new DailyAgenda();


    private DailyAgenda() {
    }

    public static final DailyAgenda instance() {
        return instance;
    }

    public void setupForToday(Context ctx, final boolean force) {

        final SharedPreferences settings = ctx.getSharedPreferences(PREFERENCES_NAME, 0);
        final Long lastDate = settings.getLong(PREF_LAST_DATE, 0);
        final DateTime now = DateTime.now();

        LogUtil.d(TAG, "Setup daily agenda. Last updated: " + new DateTime(lastDate).toString("dd/MM - HH:mm"));

        Interval today = new Interval(now.withTimeAtStartOfDay(), now.withTimeAtStartOfDay().plusDays(1));

        // we need to update daily agenda
        if (!today.contains(lastDate) || force) {
            // Start transaction
            try {
                TransactionManager.callInTransaction(DB.helper().getConnectionSource(), new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        if (!force) {
                            LocalDate yesterday = now.minusDays(1).toLocalDate();
                            LocalDate tomorrow = now.plusDays(1).toLocalDate();

                            // delete items older than yesterday
                            DB.dailyScheduleItems().removeOlderThan(yesterday);
                            // delete items beyond tomorrow (only possible when changing date)
                            DB.dailyScheduleItems().removeBeyond(tomorrow);
                        } else {
                            DB.dailyScheduleItems().removeAll();
                        }
                        // and add new ones
                        createDailySchedule(now);
                        // Save last date to prefs
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putLong(PREF_LAST_DATE, now.getMillis());
                        editor.apply();
                        return null;
                    }
                });
            } catch (SQLException e) {
                if (!force) {
                    LogUtil.e(TAG, "Error setting up daily agenda. Retrying with force = true", e);
                    // setup with force, destroy current daily agenda but continues working
                    setupForToday(ctx, true);
                } else {
                    LogUtil.e(TAG, "Error setting up daily agenda", e);
                }
            }
            // Update alarms
            AlarmScheduler.instance().updateAllAlarms(ctx);
            CalendulaApp.eventBus().post(new AgendaUpdatedEvent());
        } else {
            LogUtil.d(TAG, "No need to update daily schedule (" + DailyScheduleItem.findAll().size() + " items found for today)");
        }
    }

    public void createScheduleForDate(LocalDate date) {

        LogUtil.d(TAG, "Adding DailyScheduleItem to daily schedule for date: " + date.toString("dd/MM"));
        int items = 0;
        // create a list with all day doses for schedules bound to routines
        for (Routine r : Routine.findAll()) {
            for (ScheduleItem s : r.getScheduleItems()) {
                if (s.getSchedule().enabledForDate(date)) {
                    // create a dailyScheduleItem and save it
                    DailyScheduleItem dsi = new DailyScheduleItem(s);
                    dsi.setPatient(s.getSchedule().patient());
                    dsi.setDate(date);
                    dsi.save();
                    items++;
                }
            }
        }
        // Do the same for hourly schedules
        for (Schedule s : DB.schedules().findHourly()) {
            // create an schedule item for each repetition today
            for (DateTime time : s.hourlyItemsAt(date.toDateTimeAtStartOfDay())) {
                LocalTime timeToday = time.toLocalTime();
                DailyScheduleItem dsi = new DailyScheduleItem(s, timeToday);
                dsi.setPatient(s.patient());
                dsi.setDate(date);
                dsi.save();
            }
        }
        LogUtil.d(TAG, items + " items added to daily schedule");
    }

    // SINGLETON

    public void createDailySchedule(DateTime d) {

        boolean todayCreated = DB.dailyScheduleItems().isDatePresent(d.toLocalDate());

        if (!todayCreated) {
            createScheduleForDate(d.toLocalDate());
        }

        for (int i = 1; i <= NEXT_DAYS_TO_SHOW; i++) {
            LocalDate date = d.plusDays(i).toLocalDate();
            if (!DB.dailyScheduleItems().isDatePresent(date)) {
                createScheduleForDate(date);
            }
        }
    }

    /**
     * Generates item for a ROUTINE SCHEDULE
     */
    public void addItem(Patient p, ScheduleItem item, boolean taken) {
        // add to daily schedule
        DailyScheduleItem dsi;
        if (item.getSchedule().enabledForDate(LocalDate.now())) {
            dsi = new DailyScheduleItem(item);
            dsi.setPatient(p);
            dsi.setTakenToday(taken);
            dsi.save();
        }

        for (int i = 1; i <= NEXT_DAYS_TO_SHOW; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            if (item.getSchedule().enabledForDate(date)) {
                dsi = new DailyScheduleItem(item);
                dsi.setDate(LocalDate.now().plusDays(i));
                dsi.setTakenToday(false);
                dsi.setPatient(p);
                dsi.save();
            }
        }
    }


    /**
     * Generates item for a HOURLY REP SCHEDULE
     */
    private void addItem(Patient p, Schedule s, LocalDateTime time) {
        // add to daily schedule
        DailyScheduleItem dsi;
        if (s.enabledForDate(time.toLocalDate())) {
            dsi = new DailyScheduleItem(s, time.toLocalTime());
            dsi.setDate(time.toLocalDate());
            dsi.setPatient(p);
            dsi.save();
        }
    }


    /**
     * Generates DailyScheduleItems for today and tomorrow for the given HOURLY schedule
     *
     * @param patient the patient
     * @param s the HOURLY schedule
     */
    public void generateItemsForHourlySchedule(Patient patient, Schedule s) {
        for (DateTime time : s.hourlyItemsToday()) {
            DailyAgenda.instance().addItem(patient, s, time.toLocalDateTime());
        }
        for (int i = 1; i <= DailyAgenda.NEXT_DAYS_TO_SHOW; i++) {
            for (DateTime time : s.hourlyItemsAt(DateTime.now().plusDays(i))) {
                DailyAgenda.instance().addItem(patient, s, time.toLocalDateTime());
            }
        }
    }

    public class AgendaUpdatedEvent {
    }
}
