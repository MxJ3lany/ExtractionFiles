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

package es.usc.citius.servando.calendula.settings.privacy

import android.content.Intent
import android.support.annotation.StringRes
import es.usc.citius.servando.calendula.mvp.IPresenter
import es.usc.citius.servando.calendula.mvp.IView

interface PrivacyPrefsContract {

    interface View : IView {

        fun recordPIN()
        fun verifyPIN(requestCode: Int)
        fun showPINOptions()
        fun showConfirmDeletePinChoice()
        fun setPINPrefText(@StringRes pinPrefText: Int)
        fun setFingerprintPrefEnabled(enabled: Boolean)
        fun setPINDependentPrefsEnabled(enabled: Boolean)
        fun showEnableFingerprintDialog()
    }

    interface Presenter : IPresenter<View> {

        fun onResult(request: Int, result: Int, data: Intent?)
        fun onClickPINPref()
        fun onClickDeletePIN()
        fun onClickModifyPIN()
        fun confirmDeletePIN()
    }
}