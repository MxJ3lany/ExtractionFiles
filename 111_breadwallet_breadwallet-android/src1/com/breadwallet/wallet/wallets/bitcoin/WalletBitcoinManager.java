package com.breadwallet.wallet.wallets.bitcoin;

import android.content.Context;
import android.util.Log;

import com.breadwallet.BuildConfig;
import com.breadwallet.core.BRCoreAddress;
import com.breadwallet.core.BRCoreChainParams;
import com.breadwallet.core.BRCoreMasterPubKey;
import com.breadwallet.presenter.entities.BRSettingsItem;
import com.breadwallet.tools.util.EventUtils;
import com.breadwallet.tools.manager.BRSharedPrefs;
import com.breadwallet.tools.security.BRKeyStore;
import com.breadwallet.tools.threads.executor.BRExecutor;
import com.breadwallet.tools.util.BRConstants;
import com.breadwallet.tools.util.SettingsUtil;
import com.breadwallet.tools.util.Utils;
import com.breadwallet.wallet.WalletsMaster;
import com.breadwallet.wallet.configs.WalletSettingsConfiguration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * BreadWallet
 * <p/>
 * Created by Mihail Gutan on <mihail@breadwallet.com> 1/22/18.
 * Copyright (c) 2018 breadwallet LLC
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
public final class WalletBitcoinManager extends BaseBitcoinWalletManager {

    private static final String TAG = WalletBitcoinManager.class.getName();

    private static final String CURRENCY_CODE = BITCOIN_CURRENCY_CODE;
    public static final String NAME = "Bitcoin";
    private static final String SCHEME = "bitcoin";
    private static final String COLOR = "#f29500";

    private static WalletBitcoinManager mInstance;

    public static synchronized WalletBitcoinManager getInstance(Context context) {
        if (mInstance == null) {
            byte[] rawPubKey = BRKeyStore.getMasterPublicKey(context);
            if (Utils.isNullOrEmpty(rawPubKey)) {
                Log.e(TAG, "getInstance: rawPubKey is null");
                return null;
            }
            BRCoreMasterPubKey pubKey = new BRCoreMasterPubKey(rawPubKey, false);
            long time = BRKeyStore.getWalletCreationTime(context);
            mInstance = new WalletBitcoinManager(context, pubKey, BuildConfig.BITCOIN_TESTNET ? BRCoreChainParams.testnetChainParams : BRCoreChainParams.mainnetChainParams, time);
        }
        return mInstance;
    }

    private WalletBitcoinManager(final Context context, BRCoreMasterPubKey masterPubKey,
                                 BRCoreChainParams chainParams,
                                 double earliestPeerTime) {
        super(context, masterPubKey, chainParams, earliestPeerTime);
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                if (BRSharedPrefs.getStartHeight(context, getCurrencyCode()) == 0)
                    BRSharedPrefs.putStartHeight(context, getCurrencyCode(), getPeerManager().getLastBlockHeight());

                BigDecimal fee = BRSharedPrefs.getFeeRate(context, getCurrencyCode());
                BigDecimal economyFee = BRSharedPrefs.getEconomyFeeRate(context, getCurrencyCode());
                if (fee.compareTo(BigDecimal.ZERO) == 0) {
                    fee = new BigDecimal(getWallet().getDefaultFeePerKb());
                    EventUtils.pushEvent(EventUtils.EVENT_WALLET_DID_USE_DEFAULT_FEE_PER_KB);
                }
                getWallet().setFeePerKb(BRSharedPrefs.getFavorStandardFee(context, getCurrencyCode()) ? fee.longValue() : economyFee.longValue());
                WalletsMaster.getInstance().updateFixedPeer(context, WalletBitcoinManager.this);
            }
        });
        WalletsMaster.getInstance().setSpendingLimitIfNotSet(context, this);
        updateSettings(context);
    }

    public void updateSettings(Context context) {
        setSettingsConfig(new WalletSettingsConfiguration(context, getCurrencyCode(), SettingsUtil.getBitcoinSettings(context), getFingerprintLimits(context)));
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    protected String getColor() {
        return COLOR;
    }

    protected List<BigDecimal> getFingerprintLimits(Context app) {
        List<BigDecimal> result = new ArrayList<>();
        result.add(new BigDecimal(ONE_BITCOIN_IN_SATOSHIS).divide(new BigDecimal(1000), getMaxDecimalPlaces(app), BRConstants.ROUNDING_MODE));
        result.add(new BigDecimal(ONE_BITCOIN_IN_SATOSHIS).divide(new BigDecimal(100), getMaxDecimalPlaces(app), BRConstants.ROUNDING_MODE));
        result.add(new BigDecimal(ONE_BITCOIN_IN_SATOSHIS).divide(new BigDecimal(10), getMaxDecimalPlaces(app), BRConstants.ROUNDING_MODE));
        result.add(new BigDecimal(ONE_BITCOIN_IN_SATOSHIS));
        result.add(new BigDecimal(ONE_BITCOIN_IN_SATOSHIS).multiply(new BigDecimal(10)));
        return result;
    }

    @Override
    public String getCurrencyCode() {
        return CURRENCY_CODE;
    }

    @Override
    public String getScheme() {
        return SCHEME;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String decorateAddress(String addr) {
        return addr; // no need to decorate
    }

    @Override
    public String undecorateAddress(String addr) {
        return addr; //no need to undecorate
    }

    protected void syncStopped(Context context) {
    }

    @Override
    public void refreshAddress(Context context) {
        BRCoreAddress address = BRSharedPrefs.getIsSegwitEnabled(context) ? getWallet().getReceiveAddress() : getWallet().getLegacyAddress();
        updateCachedAddress(context, address.stringify());
    }

    @Override
    public List<BRSettingsItem> getSettingsList(Context context) {
        return SettingsUtil.getBitcoinSettings(context);
    }
}
