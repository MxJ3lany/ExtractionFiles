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

package es.usc.citius.servando.calendula.drugdb.download;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import es.usc.citius.servando.calendula.BuildConfig;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DatabaseHelper;
import es.usc.citius.servando.calendula.util.HttpDownloadUtil;
import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;


public class DBVersionManager {

    private static final String VERSION_FILE = "versions.json";
    private static final String TAG = "DBVersionManager";

    /**
     * Connects to the database server, retrieves version info and determines the newest working
     * version for a given database.
     *
     * @param databaseID the database ID
     * @return the newest working version
     */
    public static String getLastDBVersion(String databaseID) {
        final String downloadUrl = BuildConfig.DB_DOWNLOAD_URL;
        final String url = downloadUrl + VERSION_FILE;

        try {

            final String result = HttpDownloadUtil.downloadFileToText(url).trim();
            LogUtil.d(TAG, "getLastDBVersion: json is: " + result);

            Type type = new TypeToken<Map<String, Map<Integer, String>>>() {
            }.getType();
            Map<String, Map<Integer, String>> versions = new Gson().fromJson(result, type);
            LogUtil.d(TAG, "getLastDBVersion: map is: " + versions);
            Map<Integer, String> dbVersions = versions.get(databaseID);
            if (dbVersions == null) {
                LogUtil.e(TAG, "getLastDBVersion: Invalid database ID \"" + databaseID + "\"");
                return null;
            }
            List<Integer> appVersionThresholds = new ArrayList<>(dbVersions.keySet());
            Collections.sort(appVersionThresholds);
            LogUtil.d(TAG, "getLastDBVersion: version thresholds are: " + appVersionThresholds);

            //check last valid version threshold
            int lastValid = -1;
            for (Integer version : appVersionThresholds) {
                if (version <= DatabaseHelper.DATABASE_VERSION) {
                    lastValid = version;
                } else {
                    break;
                }
            }

            if (lastValid != -1) {
                String dbVersion = dbVersions.get(lastValid);
                LogUtil.d(TAG, "getLastDBVersion: Last valid threshold was: " + lastValid + ", corresponding db version is " + dbVersion);
                return dbVersion;
            } else {
                LogUtil.e(TAG, "getLastDBVersion: No valid threshold! This probably means the version file is wrong.");
                return null;
            }

        } catch (Exception e) {
            LogUtil.e(TAG, "getLastDBVersion: ", e);
            return null;
        }
    }


    /**
     * Checks if there is any available update for the current medicine database.
     *
     * @param ctx the context
     * @return the version code of the update if there is one available, <code>null</code> otherwise
     */
    public static String checkForUpdate(Context ctx) {
        final SharedPreferences prefs = PreferenceUtils.instance().preferences();
        final String noneId = ctx.getString(R.string.database_none_id);
        final String database = prefs.getString(PreferenceKeys.DRUGDB_CURRENT_DB.key(), noneId);
        final String currentVersion = prefs.getString(PreferenceKeys.DRUGDB_VERSION.key(), null);

        if (!database.equals(noneId) && !database.equals(ctx.getString(R.string.database_setting_up))) {
            if (currentVersion != null) {
                final String lastDBVersion = DBVersionManager.getLastDBVersion(database);
                final DateTime lastDBDate = DateTime.parse(lastDBVersion, ISODateTimeFormat.basicDate());
                final DateTime currentDBDate = DateTime.parse(currentVersion, ISODateTimeFormat.basicDate());

                if (lastDBDate.isAfter(currentDBDate)) {
                    LogUtil.d(TAG, "checkForUpdate: Update found for database " + database + " (" + lastDBVersion + ")");
                    return lastDBVersion;
                } else {
                    LogUtil.d(TAG, "checkForUpdate: Database is updated. ID is '" + database + "', version is '" + currentVersion + "'");
                    return null;
                }
            } else {
                LogUtil.w(TAG, "checkForUpdate: Database is " + database + " but no version is set!");
                return null;
            }
        } else {
            LogUtil.d(TAG, "checkForUpdate: No database. No version check needed.");
            return null;
        }

    }
}
