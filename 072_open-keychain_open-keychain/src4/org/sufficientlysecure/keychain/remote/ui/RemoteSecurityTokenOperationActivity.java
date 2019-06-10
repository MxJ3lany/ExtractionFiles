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

package org.sufficientlysecure.keychain.remote.ui;

import android.content.Intent;
import android.os.Bundle;

import org.sufficientlysecure.keychain.remote.CryptoInputParcelCacheService;
import org.sufficientlysecure.keychain.service.input.CryptoInputParcel;
import org.sufficientlysecure.keychain.ui.SecurityTokenOperationActivity;

public class RemoteSecurityTokenOperationActivity extends SecurityTokenOperationActivity {

    public static final String EXTRA_DATA = "data";

    private Intent mPendingIntentData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        mPendingIntentData = data.getParcelable(EXTRA_DATA);
    }

    @Override
    protected void handleResult(CryptoInputParcel inputParcel) {
        // save updated cryptoInputParcel in cache
        CryptoInputParcelCacheService.addCryptoInputParcel(this, mPendingIntentData, inputParcel);
        setResult(RESULT_OK, mPendingIntentData);
    }

}
