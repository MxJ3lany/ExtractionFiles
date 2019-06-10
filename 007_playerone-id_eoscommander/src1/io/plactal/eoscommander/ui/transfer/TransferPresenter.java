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
package io.plactal.eoscommander.ui.transfer;

import com.google.gson.JsonObject;

import javax.inject.Inject;

import io.plactal.eoscommander.R;
import io.plactal.eoscommander.data.EoscDataManager;
import io.plactal.eoscommander.ui.base.BasePresenter;
import io.plactal.eoscommander.ui.base.RxCallbackWrapper;
import io.plactal.eoscommander.util.Utils;

/**
 * Created by swapnibble on 2017-11-07.
 */

public class TransferPresenter extends BasePresenter<TransferMvpView> {

    @Inject
    EoscDataManager mDataManager;

    @Inject
    public TransferPresenter(){
    }

    public void transfer( String from, String to, String amount, String memo){
        long amountAsLong = Utils.parseLongSafely( amount, 0);
        if ( amountAsLong < 0 ) {
            getMvpView().onError( R.string.invalid_amount);
            return;
        }

        getMvpView().showLoading( true );

        addDisposable( mDataManager
                .transfer( from, to, amountAsLong, memo)
                .doOnNext( jsonObject -> mDataManager.addAccountHistory( from, to ))
                .subscribeOn( getSchedulerProvider().io())
                .observeOn( getSchedulerProvider().ui())
                .subscribeWith( new RxCallbackWrapper<JsonObject>( this){
                    @Override
                    public void onNext(JsonObject result) {

                        if ( ! isViewAttached() ) return;

                        getMvpView().showLoading( false );

                        getMvpView().showResult( Utils.prettyPrintJson(result), null);
                    }
                })

        );

    }
}
