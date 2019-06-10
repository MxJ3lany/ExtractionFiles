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


import java.io.IOException;
import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;

import edu.cmu.cylab.starslinger.exchange.ExchangeActivity;
import edu.cmu.cylab.starslinger.exchange.ExchangeConfig;
import org.sufficientlysecure.keychain.Constants;
import org.sufficientlysecure.keychain.R;
import org.sufficientlysecure.keychain.keyimport.HkpKeyserverAddress;
import org.sufficientlysecure.keychain.keyimport.ParcelableKeyRing;
import org.sufficientlysecure.keychain.operations.ImportOperation;
import org.sufficientlysecure.keychain.operations.results.ImportKeyResult;
import org.sufficientlysecure.keychain.operations.results.OperationResult;
import org.sufficientlysecure.keychain.daos.KeyRepository;
import org.sufficientlysecure.keychain.daos.KeyRepository.NotFoundException;
import org.sufficientlysecure.keychain.service.ImportKeyringParcel;
import org.sufficientlysecure.keychain.ui.base.BaseActivity;
import org.sufficientlysecure.keychain.ui.base.CryptoOperationHelper;
import org.sufficientlysecure.keychain.ui.util.FormattingUtils;
import org.sufficientlysecure.keychain.ui.util.Notify;
import org.sufficientlysecure.keychain.util.ParcelableFileCache;
import timber.log.Timber;


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SafeSlingerActivity extends BaseActivity
        implements CryptoOperationHelper.Callback<ImportKeyringParcel, ImportKeyResult> {

    private static final int REQUEST_CODE_SAFE_SLINGER = 211;

    public static final String EXTRA_MASTER_KEY_ID = "master_key_id";

    private long mMasterKeyId;
    private int mSelectedNumber = 2;

    // for CryptoOperationHelper
    private ArrayList<ParcelableKeyRing> mKeyList;
    private HkpKeyserverAddress mKeyserver;
    private CryptoOperationHelper<ImportKeyringParcel, ImportKeyResult> mOperationHelper;
    private KeyRepository keyRepository;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        keyRepository = KeyRepository.create(this);
        mMasterKeyId = getIntent().getLongExtra(EXTRA_MASTER_KEY_ID, 0);

        NumberPicker picker = findViewById(R.id.safe_slinger_picker);
        picker.setMinValue(2);
        picker.setMaxValue(10);
        picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mSelectedNumber = newVal;
            }
        });

        ImageView buttonIcon = findViewById(R.id.safe_slinger_button_image);
        buttonIcon.setColorFilter(FormattingUtils.getColorFromAttr(this, R.attr.colorTertiaryText),
                PorterDuff.Mode.SRC_IN);

        View button = findViewById(R.id.safe_slinger_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startExchange(mMasterKeyId, mSelectedNumber);
            }
        });
    }

    @Override
    protected void initLayout() {
        setContentView(R.layout.safe_slinger_activity);
    }

    private void startExchange(long masterKeyId, int number) {
        try {
            byte[] keyBlob = keyRepository.loadPublicKeyRingData(masterKeyId);

            Intent slingerIntent = new Intent(this, ExchangeActivity.class);

            slingerIntent.putExtra(ExchangeConfig.extra.NUM_USERS, number);
            slingerIntent.putExtra(ExchangeConfig.extra.USER_DATA, keyBlob);
            slingerIntent.putExtra(ExchangeConfig.extra.HOST_NAME, Constants.SAFESLINGER_SERVER);
            startActivityForResult(slingerIntent, REQUEST_CODE_SAFE_SLINGER);
        } catch (NotFoundException e) {
            Timber.e(e, "key for transfer not found");
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mOperationHelper != null) {
            mOperationHelper.handleActivityResult(requestCode, resultCode, data);
        }

        if (requestCode == REQUEST_CODE_SAFE_SLINGER) {
            if (resultCode == ExchangeActivity.RESULT_EXCHANGE_CANCELED) {
                return;
            }

            Timber.d("importKeys started");

            // instead of giving the entries by Intent extra, cache them into a
            // file to prevent Java Binder problems on heavy imports
            // read FileImportCache for more info.
            try {
                // import exchanged keys
                ArrayList<ParcelableKeyRing> it = getSlingedKeys(data.getExtras());

                // We parcel this iteratively into a file - anything we can
                // display here, we should be able to import.
                ParcelableFileCache<ParcelableKeyRing> cache =
                        new ParcelableFileCache<>(this, ImportOperation.CACHE_FILE_NAME);
                cache.writeCache(it.size(), it.iterator());

                mOperationHelper =
                        new CryptoOperationHelper(1, this, this, R.string.progress_importing);

                mKeyList = null;
                mKeyserver = null;
                mOperationHelper.cryptoOperation();
            } catch (IOException e) {
                Timber.e(e, "Problem writing cache file");
                Notify.create(this, "Problem writing cache file!", Notify.Style.ERROR).show();
            }
        } else {
            // give everything else down to KeyListActivity!
            setResult(resultCode, data);
            finish();
        }
    }

    private static ArrayList<ParcelableKeyRing> getSlingedKeys(Bundle extras) {
        ArrayList<ParcelableKeyRing> list = new ArrayList<>();

        if (extras != null) {
            byte[] d;
            int i = 0;
            do {
                d = extras.getByteArray(ExchangeConfig.extra.MEMBER_DATA + i);
                if (d != null) {
                    list.add(ParcelableKeyRing.createFromEncodedBytes(d));
                    i++;
                }
            } while (d != null);
        }

        return list;
    }

    // CryptoOperationHelper.Callback functions

    @Override
    public ImportKeyringParcel createOperationInput() {
        return ImportKeyringParcel.createImportKeyringParcel(mKeyList, mKeyserver);
    }

    @Override
    public void onCryptoOperationSuccess(ImportKeyResult result) {
        Intent certifyIntent = new Intent(this, CertifyKeyActivity.class);
        certifyIntent.putExtra(CertifyKeyActivity.EXTRA_RESULT, result);
        certifyIntent.putExtra(CertifyKeyActivity.EXTRA_KEY_IDS, result.getImportedMasterKeyIds());
        certifyIntent.putExtra(CertifyKeyActivity.EXTRA_CERTIFY_KEY_ID, mMasterKeyId);
        startActivityForResult(certifyIntent, 0);
    }

    @Override
    public void onCryptoOperationCancelled() {

    }

    @Override
    public void onCryptoOperationError(ImportKeyResult result) {
        Bundle returnData = new Bundle();
        returnData.putParcelable(OperationResult.EXTRA_RESULT, result);
        Intent data = new Intent();
        data.putExtras(returnData);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public boolean onCryptoSetProgress(String msg, int progress, int max) {
        return false;
    }
}
