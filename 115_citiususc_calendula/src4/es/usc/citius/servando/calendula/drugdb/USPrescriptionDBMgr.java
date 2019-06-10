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

package es.usc.citius.servando.calendula.drugdb;

import android.content.Context;
import android.text.TextUtils;

import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Prescription;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.util.LogUtil;

public class USPrescriptionDBMgr extends PrescriptionDBMgr {

    private static final String TAG = "USPrescriptionDBMgr";


    @Override
    public String getProspectURL(Prescription p) {
        return "http://www.accessdata.fda.gov/spl/data/#ID#/#ID#.xml".replaceAll("#ID#", p.getPID());
    }


    public Prescription fromCsv(String csvLine, String separator) {

        String[] values = csvLine.split(separator);

        if (values.length != 3) {
            throw new RuntimeException("Invalid CSV. Input string must contain exactly 3 members. " + csvLine);
        }

        if (TextUtils.isEmpty(values[1])) {
            return null;
        }

        Prescription p = new Prescription();
        p.setCode(values[0]);
        p.setPID(values[0]);
        p.setName(values[1]);
        p.setDose("0");
        p.setContent(values[2]);
        p.setGeneric(false);
        p.setAffectsDriving(false);
        p.setPackagingUnits(0f);

        return p;
    }

    @Override
    public Presentation expectedPresentation(Prescription p) {
        String name = p.getName();
        String content = p.getContent();
        return expectedPresentation(name, content);
    }

    @Override
    public Presentation expectedPresentation(String name, String content) {

        String n = name.toLowerCase() + " " + content.toLowerCase();
        if (n.contains("tablet")) {
            return Presentation.PILLS;
        } else if (n.contains("capsule")) {
            return Presentation.CAPSULES;
        } else if (n.contains("inhale")) {
            return Presentation.INHALER;
        } else if (n.contains("injection")) {
            return Presentation.INJECTIONS;
        } else if (n.contains("drops")) {
            return Presentation.DROPS;
        } else if (n.contains("suspension")) {
            return Presentation.EFFERVESCENT;
        } else if (n.contains("cream") || n.contains("gel") || n.contains("powder") || n.contains("paste")) {
            return Presentation.POMADE;
        } else if (n.contains("spray")) {
            return Presentation.SPRAY;
        } else if (!n.contains("liquid")) {
            return Presentation.SYRUP;
        }

        return null;
    }

    @Override
    public String shortName(Prescription p) {
        if (p.getName().length() < 20)
            return p.getName();
        return p.getName().substring(0, 20) + "…";

    }

    @Override
    public void setup(final Context ctx, final String downloadPath, final SetupProgressListener l) throws Exception {

        final ConnectionSource connection = DB.helper().getConnectionSource();

        TransactionManager.callInTransaction(connection, new Callable<Object>() {
            @Override
            public Object call() throws Exception {

                BufferedReader br = null;

                try {

                    DBRegistry.instance().clear();

                    String line;
                    int progressUpdateBy;
                    int lines = 0;
                    int i = 0;

                    br = new BufferedReader(new InputStreamReader(new FileInputStream(downloadPath)));
                    // count file lines (for progress updating)
                    while (br.readLine() != null) {
                        lines++;
                    }
                    br.close();

                    if (lines > 0) {
                        progressUpdateBy = lines / 20;
                        updateProgress(l, 0);

                        br = new BufferedReader(new InputStreamReader(new FileInputStream(downloadPath)));

                        while ((line = br.readLine()) != null) {
                            if (l != null && progressUpdateBy!=0 && i % progressUpdateBy == 0) {
                                int progress = (int) (((float) i / lines) * 100);
                                l.onProgressUpdate(progress);
                            }
                            // exec line content as raw sql
                            Prescription prescription = fromCsv(line, "\\|");
                            DB.drugDB().prescriptions().save(prescription);
                            i++;
                        }
                    } else {
                        LogUtil.e(TAG, "setup:  database file is empty");
                        throw new IllegalArgumentException("Database file is empty");
                    }
                } catch (Exception e) {
                    throw e;
                } finally {
                    if (br != null) {
                        br.close();
                    }
                }
                return null;

            }
        });

        EventBus.getDefault().post(new PersistenceEvents.DatabaseInstalledEvent());

        LogUtil.d(TAG, "setup: cleaning up...");
        try {
            boolean delete = new File(downloadPath).delete();
            if (!delete) {
                LogUtil.i(TAG, "setup: couldn't delete file " + downloadPath);
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "setup: couldn't finish cleanup: ", e);
        }
    }

    private void updateProgress(SetupProgressListener l, int progress) {
        if (l != null) l.onProgressUpdate(progress);
    }


}
