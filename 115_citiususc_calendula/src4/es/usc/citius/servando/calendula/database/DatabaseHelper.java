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

package es.usc.citius.servando.calendula.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.joda.time.LocalDate;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.migrationHelpers.DrugModelMigrationHelper;
import es.usc.citius.servando.calendula.database.migrationHelpers.LocalDateMigrationHelper;
import es.usc.citius.servando.calendula.drugdb.model.persistence.ActiveIngredient;
import es.usc.citius.servando.calendula.drugdb.model.persistence.ContentUnit;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Excipient;
import es.usc.citius.servando.calendula.drugdb.model.persistence.HomogeneousGroup;
import es.usc.citius.servando.calendula.drugdb.model.persistence.PackageType;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Prescription;
import es.usc.citius.servando.calendula.drugdb.model.persistence.PrescriptionActiveIngredient;
import es.usc.citius.servando.calendula.drugdb.model.persistence.PrescriptionExcipient;
import es.usc.citius.servando.calendula.drugdb.model.persistence.PresentationForm;
import es.usc.citius.servando.calendula.persistence.AllergyGroup;
import es.usc.citius.servando.calendula.persistence.DailyScheduleItem;
import es.usc.citius.servando.calendula.persistence.HtmlCacheEntry;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.PatientAlert;
import es.usc.citius.servando.calendula.persistence.PatientAllergen;
import es.usc.citius.servando.calendula.persistence.PickupInfo;
import es.usc.citius.servando.calendula.persistence.RepetitionRule;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.util.LogUtil;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    // any time you make changes to your database objects, you may have to increase the database version
    public static final int DATABASE_VERSION = 12;
    private static final String TAG = "DatabaseHelper";
    // name of the database file for our application
    private static final String DATABASE_NAME = DB.DB_NAME;
    // List of persisted classes to simplify table creation
    public Class<?>[] persistedClasses = new Class<?>[]{
            Routine.class,
            Medicine.class,
            Schedule.class,
            ScheduleItem.class,
            DailyScheduleItem.class,
            // Prescription.class, //deprecated in v12
            // v8
            // HomogeneousGroup.class, //deprecated in v12
            PickupInfo.class,
            // v9
            Patient.class,
            // v10
            HtmlCacheEntry.class,
            // v12
            ActiveIngredient.class,
            ContentUnit.class,
            Excipient.class,
            HomogeneousGroup.class,
            PackageType.class,
            Prescription.class,
            PresentationForm.class,
            PrescriptionActiveIngredient.class,
            PrescriptionExcipient.class,
            PatientAllergen.class,
            PatientAlert.class,
            AllergyGroup.class,
    };
    // the DAO object we use to access the Medicines table
    private Dao<Medicine, Long> medicinesDao = null;
    // the DAO object we use to access the Routines table
    private Dao<Routine, Long> routinesDao = null;
    // the DAO object we use to access the Schedules table
    private Dao<Schedule, Long> schedulesDao = null;
    // the DAO object we use to access the ScheduleItems table
    private Dao<ScheduleItem, Long> scheduleItemsDao = null;
    // the DAO object we use to access the DailyScheduleItems table
    private Dao<DailyScheduleItem, Long> dailyScheduleItemsDao = null;
    // the DAO object we use to access the DailyScheduleItems table
    private Dao<Prescription, Long> prescriptionsDao = null;
    // the DAO object we use to access the HomogeneousGroups table
    private Dao<HomogeneousGroup, Long> homogeneousGroupsDao = null;
    // the DAO object we use to access the pcikupInfo table
    private Dao<PickupInfo, Long> pickupInfoDao = null;
    // the DAO object we use to access the patients table
    private Dao<Patient, Long> patientDao = null;

    private final Context ctx;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        ctx = context;
    }

    /**
     * This is called when the database is first created. Usually you should call createTable statements here to create
     * the tables that will store your data.
     */
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            LogUtil.i(TAG, "onCreate");

            for (Class<?> c : persistedClasses) {
                LogUtil.d(TAG, "Creating table for " + c.getSimpleName());
                TableUtils.createTable(connectionSource, c);
            }

        } catch (SQLException e) {
            LogUtil.e(TAG, "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
     * the various data to match the new version number.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            LogUtil.i(TAG, "onUpgrade");
            LogUtil.d(TAG, "OldVersion: " + oldVersion + ", newVersion: " + newVersion);

            if (oldVersion < 6) {
                oldVersion = 6;
            }

            switch (oldVersion + 1) {
                case 7:
                    // migrate to iCal
                    migrateToICal();
                case 8:
                    getMedicinesDao().executeRaw("ALTER TABLE Medicines ADD COLUMN hg INTEGER;");
                    // Add column scanned (boolean, INTEGER in SQLite) to schedules table
                    getSchedulesDao().executeRaw("ALTER TABLE Schedules ADD COLUMN Scanned INTEGER;");
                    // update schedules scanned value
                    TransactionManager.callInTransaction(getConnectionSource(), new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            // iterate over schedules and set Scanned to false
                            List<Schedule> schedules = getSchedulesDao().queryForAll();
                            for (Schedule s : schedules) {
                                s.setScanned(false);
                                s.save();
                            }
                            return null;
                        }
                    });
                    // v8: Create HomogeneousGroup and PickupInfo tables:
                    // v12: HomogeneousGroup becomes deprecated, so we don't need to create it
                    TableUtils.createTable(connectionSource, HomogeneousGroup.class);
                    TableUtils.createTable(connectionSource, PickupInfo.class);
                case 9:
                    TableUtils.createTable(connectionSource, Patient.class);
                    migrateToMultiPatient();
                case 10:
                    TableUtils.createTable(connectionSource, HtmlCacheEntry.class);
                case 11:
                    //delete all html cache entries and change data types (bugfix)
                    TableUtils.dropTable(connectionSource, HtmlCacheEntry.class, true);
                    TableUtils.createTable(connectionSource, HtmlCacheEntry.class);
                    LocalDateMigrationHelper.migrateLocalDates(this);
                case 12:
                    DrugModelMigrationHelper.migrateDrugModel(db, connectionSource);
                    getMedicinesDao().executeRaw("ALTER TABLE Medicines ADD COLUMN Database TEXT;");
                    getMedicinesDao().executeRaw("ALTER TABLE Medicines ADD COLUMN Stock REAL DEFAULT -1;");
                    TableUtils.createTable(connectionSource, PatientAllergen.class);
                    TableUtils.createTable(connectionSource, AllergyGroup.class);
                    getSchedulesDao().executeRaw("ALTER TABLE Schedules ADD COLUMN " + Schedule.COLUMN_STATE + " TEXT;");
                    TableUtils.createTable(connectionSource, PatientAlert.class);
                    // add COLUMN_HG:text to meds table
                    getSchedulesDao().executeRaw("ALTER TABLE Medicines ADD COLUMN " + Medicine.COLUMN_HG + " TEXT;");
            }

        } catch (Exception e) {
            LogUtil.e(TAG, "Can't upgrade databases", e);
            try {
                LogUtil.d(TAG, "Will try to recreate db...");
                dropAndCreateAllTables();
            } catch (Exception ex) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Returns the Database Access Object (DAO) for our Medicines class. It will create it or just give the cached
     * value.
     */
    public Dao<Medicine, Long> getMedicinesDao() throws SQLException {
        if (medicinesDao == null) {
            medicinesDao = getDao(Medicine.class);
        }
        return medicinesDao;
    }

    /**
     * Returns the Database Access Object (DAO) for our Routines class. It will create it or just give the cached
     * value.
     */
    public Dao<Routine, Long> getRoutinesDao() throws SQLException {
        if (routinesDao == null) {
            routinesDao = getDao(Routine.class);
        }
        return routinesDao;
    }

    /**
     * Returns the Database Access Object (DAO) for our Schedules class. It will create it or just give the cached
     * value.
     */
    public Dao<Schedule, Long> getSchedulesDao() throws SQLException {
        if (schedulesDao == null) {
            schedulesDao = getDao(Schedule.class);
        }
        return schedulesDao;
    }

    /**
     * Returns the Database Access Object (DAO) for our ScheduleItem class. It will create it or just give the cached
     * value.
     */
    public Dao<ScheduleItem, Long> getScheduleItemsDao() throws SQLException {
        if (scheduleItemsDao == null) {
            scheduleItemsDao = getDao(ScheduleItem.class);
        }
        return scheduleItemsDao;
    }

    /**
     * Returns the Database Access Object (DAO) for our DailyScheduleItem class. It will create it or just give the cached
     * value.
     */
    public Dao<DailyScheduleItem, Long> getDailyScheduleItemsDao() throws SQLException {
        if (dailyScheduleItemsDao == null) {
            dailyScheduleItemsDao = getDao(DailyScheduleItem.class);
        }
        return dailyScheduleItemsDao;
    }

    /**
     * Returns the Database Access Object (DAO) for our DailyScheduleItem class. It will create it or just give the cached
     * value.
     */
    public Dao<Prescription, Long> getPrescriptionsDao() throws SQLException {
        if (prescriptionsDao == null) {
            prescriptionsDao = getDao(Prescription.class);
        }
        return prescriptionsDao;
    }

    /**
     * Returns the Database Access Object (DAO) for our HomogeneousGroup class. It will create it or just give the cached
     * value.
     */
    public Dao<HomogeneousGroup, Long> getHomogeneousGroupsDao() throws SQLException {
        if (homogeneousGroupsDao == null) {
            homogeneousGroupsDao = getDao(HomogeneousGroup.class);
        }
        return homogeneousGroupsDao;
    }

    /**
     * Returns the Database Access Object (DAO) for our PickupInfo class. It will create it or just give the cached
     * value.
     */
    public Dao<PickupInfo, Long> getPickupInfosDao() throws SQLException {
        if (pickupInfoDao == null) {
            pickupInfoDao = getDao(PickupInfo.class);
        }
        return pickupInfoDao;
    }

    /**
     * Returns the Database Access Object (DAO) for our User class. It will create it or just give the cached
     * value.
     */
    public Dao<Patient, Long> getPatientDao() throws SQLException {
        if (patientDao == null) {
            patientDao = getDao(Patient.class);
        }
        return patientDao;
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void dropAndCreateAllTables() {

        LogUtil.i(TAG, "Dropping all tables...");
        for (Class<?> c : persistedClasses) {
            LogUtil.d(TAG, "Dropping table " + c.getSimpleName());
            try {
                TableUtils.dropTable(connectionSource, c, true);
            } catch (SQLException e) {
                // ignore
                LogUtil.e(TAG, "Erro dropping table " + c.getSimpleName());
            }

        }

        try {

            LogUtil.i(TAG, "Creating tables...");
            for (Class<?> c : persistedClasses) {
                LogUtil.d(TAG, "Creating table " + c.getSimpleName());
                TableUtils.createTable(connectionSource, c);
            }
            createDefaultPatient();

        } catch (SQLException e) {
            LogUtil.e(TAG, "Can't recreate database", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Close the database connections and clear any cached DAOs.
     */
    @Override
    public void close() {
        super.close();
        medicinesDao = null;
        routinesDao = null;
        schedulesDao = null;
        scheduleItemsDao = null;
        dailyScheduleItemsDao = null;
        prescriptionsDao = null;
        homogeneousGroupsDao = null;
        pickupInfoDao = null;
        patientDao = null;
    }

    /**
     * Creates a default patient.
     *
     * @return the created Patient
     * @throws SQLException if anything goes wrong
     */
    public Patient createDefaultPatient() throws SQLException {
        // Create a default patient
        Patient p = new Patient();
        p.setName(ctx.getString(R.string.default_user_name));
        p.setDefault(true);
        getPatientDao().create(p);
        return p;
    }

    /**
     * Method that migrate models to multi-user
     */
    private void migrateToMultiPatient() throws SQLException {

        // add patient column to routines, schedules and medicines
        getRoutinesDao().executeRaw("ALTER TABLE Routines ADD COLUMN Patient INTEGER;");
        getRoutinesDao().executeRaw("ALTER TABLE Medicines ADD COLUMN Patient INTEGER;");
        getRoutinesDao().executeRaw("ALTER TABLE Schedules ADD COLUMN Patient INTEGER;");
        getRoutinesDao().executeRaw("ALTER TABLE DailyScheduleItems ADD COLUMN Patient INTEGER;");
        getRoutinesDao().executeRaw("ALTER TABLE DailyScheduleItems ADD COLUMN Date TEXT;");

        Patient p = createDefaultPatient();
        // PreferenceUtils.edit().putLong(PatientDao.PREFERENCE_ACTIVE_PATIENT, p.id()).commit();

        // Assign all routines to the default patient
        UpdateBuilder<Routine, Long> rUpdateBuilder = getRoutinesDao().updateBuilder();
        rUpdateBuilder.updateColumnValue(Routine.COLUMN_PATIENT, p.getId());
        rUpdateBuilder.update();

        // Assign all schedules to the default patient
        UpdateBuilder<Schedule, Long> sUpdateBuilder = getSchedulesDao().updateBuilder();
        sUpdateBuilder.updateColumnValue(Schedule.COLUMN_PATIENT, p.getId());
        sUpdateBuilder.update();

        // Assign all medicines to the default patient
        UpdateBuilder<Medicine, Long> mUpdateBuilder = getMedicinesDao().updateBuilder();
        mUpdateBuilder.updateColumnValue(Medicine.COLUMN_PATIENT, p.getId());
        mUpdateBuilder.update();

        // Assign all DailyScheduleItems to the default patient, for today
        UpdateBuilder<DailyScheduleItem, Long> siUpdateBuilder = getDailyScheduleItemsDao().updateBuilder();
        siUpdateBuilder.updateColumnValue(DailyScheduleItem.COLUMN_PATIENT, p.getId());
        siUpdateBuilder.update();

        // date formatter changes on v11, so we can no use LocalDatePersister here
        String now = LocalDate.now().toString("ddMMYYYY");
        String updateDateSql = "UPDATE DailyScheduleItems SET " + DailyScheduleItem.COLUMN_DATE + " = '" + now + "'";
        getDailyScheduleItemsDao().executeRaw(updateDateSql);

    }

    /**
     * Method that migrate schedules to the iCal format
     */
    private void migrateToICal() throws SQLException {

        getSchedulesDao().executeRaw("ALTER TABLE Schedules ADD COLUMN Rrule TEXT;");
        getSchedulesDao().executeRaw("ALTER TABLE Schedules ADD COLUMN Start TEXT;");
        getSchedulesDao().executeRaw("ALTER TABLE Schedules ADD COLUMN Starttime TEXT;");
        getSchedulesDao().executeRaw("ALTER TABLE Schedules ADD COLUMN Dose REAL;");
        getSchedulesDao().executeRaw("ALTER TABLE Schedules ADD COLUMN Type INTEGER;");
        getSchedulesDao().executeRaw("ALTER TABLE Schedules ADD COLUMN Cycle TEXT;");

        getDailyScheduleItemsDao().executeRaw(
                "ALTER TABLE DailyScheduleItems ADD COLUMN Schedule INTEGER;");
        getDailyScheduleItemsDao().executeRaw(
                "ALTER TABLE DailyScheduleItems ADD COLUMN Time TEXT;");

        // update schedules
        TransactionManager.callInTransaction(getConnectionSource(), new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // iterate over schedules and replace days[] with rrule
                List<Schedule> schedules = getSchedulesDao().queryForAll();
                LogUtil.d(TAG, "Upgrade " + schedules.size() + " schedules");
                for (Schedule s : schedules) {
                    if (s.rule() == null) {
                        s.setRepetition(new RepetitionRule(RepetitionRule.DEFAULT_ICAL_VALUE));
                    }
                    s.setDays(s.getLegacyDays());

                    if (s.allDaysSelected()) {
                        s.setType(Schedule.SCHEDULE_TYPE_EVERYDAY);
                    } else {
                        s.setType(Schedule.SCHEDULE_TYPE_SOMEDAYS);
                    }
                    s.setStart(LocalDate.now());
                    s.save();
                }

                return null;
            }
        });


    }

}
