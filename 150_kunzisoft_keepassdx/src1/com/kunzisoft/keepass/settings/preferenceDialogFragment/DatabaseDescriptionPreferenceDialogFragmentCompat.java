/*
 * Copyright 2018 Jeremy Jamet / Kunzisoft.
 *
 * This file is part of KeePass DX.
 *
 *  KeePass DX is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  KeePass DX is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with KeePass DX.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.kunzisoft.keepass.settings.preferenceDialogFragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.kunzisoft.keepass.database.action.OnFinishRunnable;
import com.kunzisoft.keepass.tasks.SaveDatabaseProgressTaskDialogFragment;

public class DatabaseDescriptionPreferenceDialogFragmentCompat extends InputDatabaseSavePreferenceDialogFragmentCompat {

    public static DatabaseDescriptionPreferenceDialogFragmentCompat newInstance(
            String key) {
        final DatabaseDescriptionPreferenceDialogFragmentCompat
                fragment = new DatabaseDescriptionPreferenceDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        setInputText(database.getDescription());
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if ( positiveResult ) {
            assert getContext() != null;

            String newDescription = getInputText();
            String oldDescription = database.getDescription();
            database.assignDescription(newDescription);

            Handler handler = new Handler();
            setAfterSaveDatabase(new AfterDescriptionSave((AppCompatActivity) getActivity(), handler, newDescription, oldDescription));
        }

        super.onDialogClosed(positiveResult);
    }

    private class AfterDescriptionSave extends OnFinishRunnable {

        private AppCompatActivity mActivity;
        private String mNewDescription;
        private String mOldDescription;

        AfterDescriptionSave(AppCompatActivity ctx, Handler handler, String newDescription, String oldDescription) {
            super(handler);

            mActivity = ctx;
            mNewDescription = newDescription;
            mOldDescription = oldDescription;
        }

        @Override
        public void run() {
            if (mActivity != null) {
                mActivity.runOnUiThread(() -> {
                    String descriptionToShow = mNewDescription;

                    if (!mSuccess) {
                        displayMessage(mActivity);
                        database.assignDescription(mOldDescription);
                    }

                    getPreference().setSummary(descriptionToShow);
                    SaveDatabaseProgressTaskDialogFragment.stop(mActivity);
                });
            }

            super.run();
        }
    }
}
