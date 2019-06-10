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

import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;

import io.plactal.eoscommander.R;
import io.plactal.eoscommander.crypto.ec.EosPrivateKey;
import io.plactal.eoscommander.data.EoscDataManager;
import io.plactal.eoscommander.data.remote.model.api.PushTxnResponse;
import io.plactal.eoscommander.data.remote.model.chain.Action;
import io.plactal.eoscommander.data.remote.model.types.EosNewAccount;
import io.plactal.eoscommander.data.remote.model.types.TypePublicKey;
import io.plactal.eoscommander.data.wallet.EosWallet;
import io.plactal.eoscommander.ui.base.BasePresenter;
import io.plactal.eoscommander.ui.base.RxCallbackWrapper;
import io.plactal.eoscommander.util.StringUtils;
import io.plactal.eoscommander.util.Utils;
import io.reactivex.Observable;

/**
 * Created by swapnibble on 2017-11-06.
 */

public class CreateEosAccountPresenter extends BasePresenter<CreateEosAccountMvpView> {
    @Inject
    EoscDataManager mDataManager;

    private EosPrivateKey mOwnerKey;
    private EosPrivateKey mActiveKey;

    @Inject
    public CreateEosAccountPresenter(){
    }

    private boolean loadUnlockedWallets() {
        ArrayList<EosWallet.Status> wallets = mDataManager.getWalletManager().listWallets( false );
        if ( wallets.size() <= 0 ) {
            getMvpView().showUnlockedWallets( new ArrayList<>());
            getMvpView().showToast( R.string.no_wallet_unlocked_or_selected);
            return false;
        }

        ArrayList<String> unlockedWalletNames = new ArrayList<>( wallets.size());
        for ( EosWallet.Status status : wallets ){
            unlockedWalletNames.add( status.walletName );
        }

        getMvpView().showUnlockedWallets( unlockedWalletNames);

        return true;
    }

    public void onStart(){
        if ( ! loadUnlockedWallets() ){
            getMvpView().exitWithResult( false );
            return;
        }

        String defaultAccountCreator = mDataManager.getPreferenceHelper().getDefaultAccountCreator();
        if ( !StringUtils.isEmpty(defaultAccountCreator)) {
            getMvpView().showCreator(defaultAccountCreator);
        }

        getMvpView().showLoading( true);

        // generate keys..
        addDisposable(
                mDataManager
                        .createKey(2)
                        .subscribeOn(getSchedulerProvider().computation())
                        .observeOn( getSchedulerProvider().ui())
                        .subscribeWith( new RxCallbackWrapper<EosPrivateKey[]>(this) {
                                            @Override
                                            public void onNext(EosPrivateKey[] keys) {
                                                if ( !isViewAttached() ) return;

                                                getMvpView().showLoading( false);

                                                mOwnerKey = keys[0];
                                                mActiveKey= keys[1];

                                                getMvpView().showPubKeys( mOwnerKey.getPublicKey().toString(), mActiveKey.getPublicKey().toString());
                                            }
                                        }
                        )
        );
    }



    public void createAccount( String creator, String newAccount) {

        if( ( null == mOwnerKey) || ( null== mActiveKey ) ) {
            getMvpView().showToast(R.string.key_empty_maybe_no_wallet_unlocked);
            return;
        }

        String walletName = getMvpView().getSelectedWalletName();
        if (StringUtils.isEmpty(walletName)) {
            getMvpView().showToast(R.string.no_wallet_unlocked_or_selected);
            return;
        }

        getMvpView().showLoading( true );

        // create account and save keys if successful.
        addDisposable( mDataManager
                .createAccount( new EosNewAccount(creator, newAccount
                        , TypePublicKey.from( mOwnerKey.getPublicKey()) , TypePublicKey.from(mActiveKey.getPublicKey()) ))
                .doOnNext( jsonObject -> mDataManager.addAccountHistory( creator, newAccount ))
                .subscribeOn(getSchedulerProvider().io())
                .doOnNext( pushTxnResult -> {
                    mDataManager.getWalletManager().importKeys( walletName, new EosPrivateKey[]{mOwnerKey, mActiveKey});
                    mDataManager.getWalletManager().saveFile( walletName );
                })
                .observeOn( getSchedulerProvider().ui())
                .subscribeWith( new RxCallbackWrapper<PushTxnResponse>( this) {
                    @Override
                    public void onNext(PushTxnResponse result) {

                        if ( ! isViewAttached() ) return;

                        getMvpView().showLoading( false );

                        if ( ( null != result) && ! StringUtils.isEmpty( result.getTransactionId()) ) {
                            getMvpView().exitWithResult( true );
                            getMvpView().showResult(Utils.prettyPrintJson(result ), result.toString() );
                        }
                        else {
                            getMvpView().showToast( R.string.failed );
                        }
                    }
                })

        );
    }

    public void createAccountVerbose( String creator, String newAccount, String stake4net, String stake4cpu, String eosToBuyRam) {

        if( ( null == mOwnerKey) || ( null== mActiveKey ) ) {
            getMvpView().showToast(R.string.key_empty_maybe_no_wallet_unlocked);
            return;
        }

        String walletName = getMvpView().getSelectedWalletName();
        if (StringUtils.isEmpty(walletName)) {
            getMvpView().showToast(R.string.no_wallet_unlocked_or_selected);
            return;
        }

        getMvpView().showLoading( true );

        // create account and save keys if successful.

        addDisposable( Observable
                    .zip ( mDataManager.createAccountAction( creator, newAccount, mOwnerKey.getPublicKey(), mActiveKey.getPublicKey())
                            , mDataManager.buyRamInAssetAction( creator, newAccount, eosToBuyRam)
                            , mDataManager.delegateAction( creator, newAccount, stake4net, stake4cpu, false)
                            ,  ( createAccount, buyRam, delegate) -> Arrays.asList( createAccount,buyRam,delegate) )

                        .flatMap( actionList -> mDataManager.pushActions( actionList))
                        .doOnNext( jsonObject -> mDataManager.addAccountHistory( creator, newAccount ))
                        .subscribeOn(getSchedulerProvider().io())
                        .doOnNext( pushTxnResult -> {
                            mDataManager.getWalletManager().importKeys( walletName, new EosPrivateKey[]{mOwnerKey, mActiveKey});
                            mDataManager.getWalletManager().saveFile( walletName );
                        })
                        .observeOn( getSchedulerProvider().ui())
                        .subscribeWith( new RxCallbackWrapper<PushTxnResponse>( this) {
                            @Override
                            public void onNext(PushTxnResponse result) {

                                if ( ! isViewAttached() ) return;

                                getMvpView().showLoading( false );

                                if ( ( null != result) && ! StringUtils.isEmpty( result.getTransactionId()) ) {
                                    getMvpView().exitWithResult( true );
                                    getMvpView().showResult(Utils.prettyPrintJson(result ), result.toString() );
                                }
                                else {
                                    getMvpView().showToast( R.string.failed );
                                }
                            }
                        })

        );
    }
}
