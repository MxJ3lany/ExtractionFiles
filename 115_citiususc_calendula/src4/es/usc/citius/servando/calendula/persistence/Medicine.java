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

package es.usc.citius.servando.calendula.persistence;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.drugdb.DBRegistry;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Prescription;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;

import static java.util.Collections.sort;


@DatabaseTable(tableName = "Medicines")
public class Medicine implements Comparable<Medicine> {

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_PRESENTATION = "Presentation";
    public static final String COLUMN_CN = "cn";
    public static final String COLUMN_HG = "homogeneousGroup";
    public static final String COLUMN_STOCK = "Stock";
    public static final String COLUMN_PATIENT = "Patient";
    public static final String COLUMN_DATABASE = "Database";

    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private Long id;

    @DatabaseField(columnName = COLUMN_NAME)
    private String name;

    @DatabaseField(columnName = COLUMN_PRESENTATION)
    private Presentation presentation;

    @DatabaseField(columnName = COLUMN_CN)
    private String cn;

    @DatabaseField(columnName = COLUMN_STOCK)
    private Float stock;

    @DatabaseField(columnName = COLUMN_HG)
    private String homogeneousGroup;

    @DatabaseField(columnName = COLUMN_PATIENT, foreign = true, foreignAutoRefresh = true)
    private Patient patient;

    @DatabaseField(columnName = COLUMN_DATABASE)
    private String database;

    public Medicine() {
    }

    public Medicine(String name) {
        this.name = name;
    }

    public Medicine(String name, Presentation presentation) {
        this.name = name;
        this.presentation = presentation;
    }

    public static List<Medicine> findAll() {
        return DB.medicines().findAll();
    }

    public static Medicine findById(long id) {
        return DB.medicines().findById(id);
    }

    public static Medicine findByName(String name) {
        return DB.medicines().findOneBy(COLUMN_NAME, name);
    }

    public static Medicine fromPrescription(Prescription p) {
        Medicine m = new Medicine();
        m.setCn(String.valueOf(p.getCode()));
        m.setName(p.shortName());
        Presentation pre = DBRegistry.instance().current().expectedPresentation(p);
        m.setPresentation(pre != null ? pre : Presentation.PILLS);
        m.setDatabase(DBRegistry.instance().current().id());
        return m;
    }

    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public String getHomogeneousGroup() {
        return homogeneousGroup;
    }

    public void setHomogeneousGroup(String homogeneousGroup) {
        this.homogeneousGroup = homogeneousGroup;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Presentation getPresentation() {
        return presentation;
    }

    public void setPresentation(Presentation presentation) {
        this.presentation = presentation;
    }

    public Collection<PickupInfo> getPickups() {
        return DB.pickups().findByMedicine(this);
    }

    public Patient getPatient() {
        return patient;
    }

    // *************************************
    // DB queries
    // *************************************

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    @Override
    public int compareTo(Medicine another) {
        return name.compareTo(another.name);
    }

    public void deleteCascade() {
        DB.medicines().deleteCascade(this, false);
    }

    public void save() {
        DB.medicines().save(this);
    }

    public LocalDate nextPickupDate() {

        List<PickupInfo> pickupList = new ArrayList<>();
        for (PickupInfo pickupInfo : getPickups()) {
            if (!pickupInfo.isTaken())
                pickupList.add(pickupInfo);
        }

        if (!pickupList.isEmpty()) {
            sort(pickupList, new PickupInfo.PickupComparator());
            return pickupList.get(0).getFrom();
        }

        return null;
    }

    public String nextPickup() {
        LocalDate np = nextPickupDate();
        return np != null ? np.toString("dd MMMM") : null;
    }

    public boolean isBoundToPrescription() {
        return cn != null && database != null && database.equals(PreferenceUtils.getString(PreferenceKeys.DRUGDB_CURRENT_DB, null));
    }

    public Float getStock() {
        return stock;
    }

    public void setStock(Float stock) {
        this.stock = stock;
    }

    public boolean stockManagementEnabled() {
        return stock != null && stock != -1;
    }

    @Override
    public String toString() {
        return "Medicine{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", presentation=" + presentation +
                ", cn='" + cn + '\'' +
                ", homogeneousGroup=" + homogeneousGroup +
                ", patient=" + patient +
                ", database='" + database + '\'' +
                '}';
    }
}
