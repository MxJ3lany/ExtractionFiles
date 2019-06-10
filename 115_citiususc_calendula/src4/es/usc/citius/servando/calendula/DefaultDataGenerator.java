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

package es.usc.citius.servando.calendula;

import android.content.Context;
import android.content.res.Resources;

import org.joda.time.LocalTime;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.util.LogUtil;

public class DefaultDataGenerator {

    private static final String TAG = "DefaultDataGenerator";

    public static void fillDBWithDummyData(Context ctx) {
        Resources r = ctx.getResources();
        if (Routine.findAll().size() == 0 && Schedule.findAll().size() == 0 && Medicine.findAll().size() == 0) {
            try {
                LogUtil.d(TAG, "Creating dummy data...");
                Patient p = DB.patients().getActive(ctx);
                new Routine(p, new LocalTime(9, 0), r.getString(R.string.routine_breakfast)).save();
                new Routine(p, new LocalTime(13, 0), r.getString(R.string.routine_lunch)).save();
                new Routine(p, new LocalTime(21, 0), r.getString(R.string.routine_dinner)).save();
                LogUtil.d(TAG, "Dummy data saved successfully!");
            } catch (Exception e) {
                LogUtil.e(TAG, "Error filling db with dummy data!", e);
            }
        }
    }

    public static void generateDefaultRoutines(Patient p, Context ctx) {
        Resources r = ctx.getResources();
        new Routine(p, new LocalTime(9, 0), r.getString(R.string.routine_breakfast)).save();
        new Routine(p, new LocalTime(13, 0), r.getString(R.string.routine_lunch)).save();
        new Routine(p, new LocalTime(21, 0), r.getString(R.string.routine_dinner)).save();
    }

}
