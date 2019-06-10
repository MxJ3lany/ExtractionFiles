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

package es.usc.citius.servando.calendula.settings.database

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.app.ActivityCompat
import android.support.v7.preference.ListPreference
import android.support.v7.preference.Preference
import android.widget.Toast
import es.usc.citius.servando.calendula.CalendulaActivity
import es.usc.citius.servando.calendula.R
import es.usc.citius.servando.calendula.drugdb.download.DownloadDatabaseHelper
import es.usc.citius.servando.calendula.settings.CalendulaPrefsFragment
import es.usc.citius.servando.calendula.util.LogUtil
import es.usc.citius.servando.calendula.util.PermissionUtils
import es.usc.citius.servando.calendula.util.PreferenceKeys
import es.usc.citius.servando.calendula.util.PreferenceUtils


/**
 * Instantiated via reflection, don't delete!
 */
class DatabasePrefsFragment :
    CalendulaPrefsFragment<DatabasePrefsContract.View, DatabasePrefsContract.Presenter>(),
    DatabasePrefsContract.View {

    companion object {
        private const val TAG = "DatabasePrefsFragment"
        private const val REQUEST_DL_PERMISSION = 938
    }

    override val fragmentTitle: Int = R.string.pref_header_prescriptions
    override val presenter: DatabasePrefsContract.Presenter by lazy {
        DatabasePrefsPresenter(
            PreferenceUtils.getString(
                PreferenceKeys.DRUGDB_CURRENT_DB,
                getString(R.string.database_none_id)
            )
        )
    }

    private val dbPref: ListPreference by lazy {
        findPreference(getString(R.string.prefkey_drugdb_current_db)) as ListPreference
    }
    private val updateDBPref: Preference by lazy {
        findPreference(getString(R.string.prefkey_settings_database_update))
    }

    private val noneId by lazy { getString(R.string.database_none_id) }
    private val settingUpId by lazy { getString(R.string.database_setting_up_id) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        LogUtil.d(TAG, "onCreatePreferences() called")
        addPreferencesFromResource(R.xml.pref_database)

        // set listeners for our prefs
        dbPref.setOnPreferenceChangeListener { _, newValue ->
            presenter.selectNewDb(newValue as String)
        }
        updateDBPref.setOnPreferenceClickListener {
            context?.let(presenter::checkDatabaseUpdate)
            true
        }
    }


    /**
     * From [SharedPreferences.OnSharedPreferenceChangeListener]
     *
     * @see [CalendulaPrefsFragment]
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        LogUtil.d(TAG, "onSharedPreferenceChanged: preference $key changed")
        if (key == dbPref.key) {
            presenter.currentDbUpdated(
                PreferenceUtils.getString(
                    PreferenceKeys.DRUGDB_CURRENT_DB,
                    noneId
                )
            )
        }
    }


    override fun setDbList(dbIds: Array<String>, dbDisplayNames: Array<String>) {
        dbPref.entryValues = dbIds
        dbPref.entries = dbDisplayNames
        refreshUi()
    }

    override fun resolveString(@StringRes stringRes: Int): String {
        return context!!.getString(stringRes)
    }

    override fun showSelectedDb(dbId: String) {
        dbPref.value = dbId
        refreshUi()
    }

    override fun showDatabaseDownloadChoice(dbId: String) {
        DownloadDatabaseHelper.instance()
            .showDownloadDialog(activity, dbId) { accepted ->
                presenter.onDbDownloadChoiceResult(accepted)
            }
    }

    override fun showDatabaseUpdateNotAvailable() {
        Toast.makeText(context, R.string.database_update_not_available, Toast.LENGTH_SHORT).show()
    }

    override fun getIntent(): Intent = activity!!.intent

    override fun openDatabaseSelection() {
        preferenceManager.showDialog(dbPref)
    }

    override fun askForDownloadPermission(dbId: String) {
        if (!hasDownloadPermission()) {
            (activity as CalendulaActivity).requestPermission(object :
                PermissionUtils.PermissionRequest {
                override fun reqCode(): Int = REQUEST_DL_PERMISSION

                override fun permissions(): Array<String> =
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

                override fun onPermissionGranted() {
                    LogUtil.d(TAG, "onPermissionGranted() called")
                    presenter.onDownloadPermissionGranted(dbId)
                }

                override fun onPermissionDenied() {
                    LogUtil.d(TAG, "onPermissionDenied: permission denied")
                }
            })
        } else {
            throw IllegalStateException("Permissions already granted!")
        }
    }

    override fun hasDownloadPermission(): Boolean {
        val hasPermission = ActivityCompat.checkSelfPermission(
            context!!,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        LogUtil.d(TAG, "hasDownloadPermission: result is $hasPermission")
        return hasPermission
    }


    private fun refreshUi() {
        LogUtil.d(TAG, "refreshUi() called")
        if (dbPref.value == settingUpId) {
            dbPref.summary = getString(R.string.database_setting_up)
            dbPref.isEnabled = false
        } else {
            dbPref.summary = dbPref.entry
            dbPref.isEnabled = true
        }

        updateDBPref.isEnabled = dbPref.isEnabled && dbPref.value !=
                noneId
    }


}