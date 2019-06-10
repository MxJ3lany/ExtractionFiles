/*
 * Copyright (c) 2017-2018 PlayerOne.
 *
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.plactal.eoscommander.ui.account.create;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.plactal.eoscommander.R;
import io.plactal.eoscommander.di.component.ActivityComponent;
import io.plactal.eoscommander.ui.base.BaseDialog;
import io.plactal.eoscommander.util.UiUtils;

/**
 * Created by swapnibble on 2017-11-06.
 */

public class CreateEosAccountDialog extends BaseDialog implements CreateEosAccountMvpView{
    private static final String TAG = CreateEosAccountDialog.class.getSimpleName();

    private static final int WALLET_SPINNER_SELECT_NOTICE_INDEX = 0;
    private static final int WALLET_SPINNER_SELECT_NOTICE_STR_RSC = R.string.select_wallet_to_save_keys;


    @Inject
    CreateEosAccountPresenter mPresenter;

    private AutoCompleteTextView mEtCreator;
    private AutoCompleteTextView mEtNewAccount;
    private TextView mTvOwner;
    private TextView mTvActive;

    private EditText mStake4Net;
    private EditText mStake4Cpu;
    private EditText mBuyRamInEOS;

    private TextView mTvNoWalletWarn;
    private AppCompatSpinner mWalletSpinner;

    public static CreateEosAccountDialog newInstance() {
        CreateEosAccountDialog fragment = new CreateEosAccountDialog();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_account_create, container, false);

        ActivityComponent component = getActivityComponent();
        if (component != null) {

            component.inject(this);

            mPresenter.attachView(this);
        }

        return view;
    }

    @Override
    protected void setUpView(View view) {

        mEtCreator      = view.findViewById( R.id.et_creator_name);
        mEtNewAccount   = view.findViewById( R.id.et_new_account);
        mEtNewAccount.setOnEditorActionListener( (textView, actionId, keyEvent) -> {
                    if (EditorInfo.IME_ACTION_DONE == actionId) {
                        onClickCreate();
                        return true;
                    }

                    return false;
                });

        mTvOwner        = view.findViewById( R.id.tv_owner_key );
        mTvActive       = view.findViewById( R.id.tv_active_key );

        // 2018.06.25. added.
        mStake4Net      = view.findViewById( R.id.et_stake_net );
        mStake4Cpu      = view.findViewById( R.id.et_stake_cpu );
        mBuyRamInEOS    = view.findViewById( R.id.et_buy_ram_eos );


        view.findViewById( R.id.btn_create).setOnClickListener( v -> onClickCreate() );

        view.findViewById( R.id.btn_cancel).setOnClickListener( v -> dismissDialog( TAG ));

        mTvNoWalletWarn= view.findViewById( R.id.tv_no_wallet_warn );
        mWalletSpinner = view.findViewById( R.id.sp_wallets_unlocked);

        mWalletSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                hideKeyboard();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });


        UiUtils.setupAccountHistory( mEtCreator );
    }

    private void onClickCreate() {

        mPresenter.createAccountVerbose(mEtCreator.getText().toString()
                , mEtNewAccount.getText().toString()
                , mStake4Net.getText().toString()
                , mStake4Cpu.getText().toString()
                , mBuyRamInEOS.getText().toString()
        );

    }

    @Override
    public void onStart() {
        super.onStart();

        mPresenter.onStart();
    }


    @Override
    public void showCreator( String creator ) {
        UiUtils.setTextAndMoveCursorToEnd( mEtCreator, creator );
    }

    @Override
    public void showPubKeys( String ownerKey, String activeKey) {

        mTvOwner.setText( ownerKey );
        mTvActive.setText( activeKey );
    }

    @Override
    public void showUnlockedWallets(ArrayList<String> walletNames) {
        if ( walletNames.size() <= 0 ){
            mWalletSpinner.setVisibility( View.GONE );
            mTvNoWalletWarn.setVisibility( View.VISIBLE );
            return;
        }

        mWalletSpinner.setVisibility( View.VISIBLE );
        mTvNoWalletWarn.setVisibility( View.GONE );

        int autoSelectPosition = ( walletNames.size() == 1 ) ? ( WALLET_SPINNER_SELECT_NOTICE_INDEX + 1) : -1;

        // select wallet notice..
        walletNames.add(WALLET_SPINNER_SELECT_NOTICE_INDEX, getString( WALLET_SPINNER_SELECT_NOTICE_STR_RSC ));


        ArrayAdapter<String> adapter = new ArrayAdapter<>( getContext(), android.R.layout.simple_spinner_item, walletNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mWalletSpinner.setAdapter( adapter);

        // select
        if ( autoSelectPosition >= 0 ) {
            mWalletSpinner.setSelection( autoSelectPosition );
        }
    }

    @Override
    public String getSelectedWalletName() {
        if ( mWalletSpinner.getVisibility() != View.VISIBLE ) {
            return null;
        }

        if ( mWalletSpinner.getSelectedItemPosition() == WALLET_SPINNER_SELECT_NOTICE_INDEX) { // select wallet notice
            return null;
        }

        return mWalletSpinner.getSelectedItem().toString();
    }


    @Override
    public void onDestroyView() {
        mPresenter.detachView();
        super.onDestroyView();
    }

    public void show(FragmentManager fragmentManager) {
        super.show(fragmentManager, TAG);
    }

    @Override
    public void dismissDialog(String tag) {
        dismiss();
    }

    @Override
    public void exitWithResult( boolean success ){
        dismiss();
    }

}
