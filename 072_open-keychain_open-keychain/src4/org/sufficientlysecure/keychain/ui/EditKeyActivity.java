/*
 * Copyright (C) 2017 Schürmann & Breitmoser GbR
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.sufficientlysecure.keychain.ui;


import android.os.Bundle;

import org.sufficientlysecure.keychain.R;
import org.sufficientlysecure.keychain.service.SaveKeyringParcel;
import org.sufficientlysecure.keychain.ui.base.BaseActivity;
import timber.log.Timber;


public class EditKeyActivity extends BaseActivity {
    public static final String EXTRA_SAVE_KEYRING_PARCEL = "save_keyring_parcel";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SaveKeyringParcel saveKeyringParcel = getIntent().getParcelableExtra(EXTRA_SAVE_KEYRING_PARCEL);
        if (saveKeyringParcel == null) {
            Timber.e("Either a key Uri or EXTRA_SAVE_KEYRING_PARCEL is required!");
            finish();
            return;
        }

        loadFragment(savedInstanceState, saveKeyringParcel);
    }

    @Override
    protected void initLayout() {
        setContentView(R.layout.edit_key_activity);
    }

    private void loadFragment(Bundle savedInstanceState, SaveKeyringParcel saveKeyringParcel) {
        // However, if we're being restored from a previous state,
        // then we don't need to do anything and should return or else
        // we could end up with overlapping fragments.
        if (savedInstanceState != null) {
            return;
        }

        // Create an instance of the fragment
        EditKeyFragment editKeyFragment = EditKeyFragment.newInstance(saveKeyringParcel);

        // Add the fragment to the 'fragment_container' FrameLayout
        // NOTE: We use commitAllowingStateLoss() to prevent weird crashes!
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.edit_key_fragment_container, editKeyFragment)
                .commitAllowingStateLoss();
        // do it immediately!
        getSupportFragmentManager().executePendingTransactions();
    }

}
