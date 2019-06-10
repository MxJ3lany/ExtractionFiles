package com.breadwallet.wallet.wallets.ethereum;

import android.content.Context;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.breadwallet.BuildConfig;
import com.breadwallet.core.ethereum.BREthereumAmount;
import com.breadwallet.core.ethereum.BREthereumToken;
import com.breadwallet.core.ethereum.BREthereumTransaction;
import com.breadwallet.core.ethereum.BREthereumWallet;
import com.breadwallet.presenter.entities.CurrencyEntity;
import com.breadwallet.presenter.entities.TokenItem;
import com.breadwallet.tools.manager.BRReportsManager;
import com.breadwallet.tools.manager.BRSharedPrefs;
import com.breadwallet.tools.sqlite.RatesDataSource;
import com.breadwallet.tools.util.BRConstants;
import com.breadwallet.tools.util.TokenUtil;
import com.breadwallet.tools.util.Utils;
import com.breadwallet.wallet.configs.WalletSettingsConfiguration;
import com.breadwallet.wallet.configs.WalletUiConfiguration;
import com.breadwallet.wallet.wallets.CryptoAddress;
import com.breadwallet.wallet.wallets.CryptoTransaction;
import com.breadwallet.wallet.wallets.WalletManagerHelper;
import com.breadwallet.wallet.wallets.bitcoin.WalletBitcoinManager;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static com.breadwallet.tools.util.BRConstants.ROUNDING_MODE;

/**
 * BreadWallet
 * <p/>
 * Created by Mihail Gutan on <mihail@breadwallet.com> 4/13/18.
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
public class WalletTokenManager extends BaseEthereumWalletManager {

    private static final String TAG = WalletTokenManager.class.getSimpleName();

    private WalletEthManager mWalletEthManager;
    private static Map<String, String> mTokenIsos = new HashMap<>();
    private static Map<String, WalletTokenManager> mTokenWallets = new HashMap<>();
    private BREthereumWallet mWalletToken;
    private WalletUiConfiguration mUiConfig;

    private static final String DEFAULT_COLOR_LEFT = "#ff5193"; // tokenWallet.getToken().getColorLeft()
    private static final String DEFAULT_COLOR_RIGHT = "#f9a43a"; // tokenWallet.getToken().getColorRight();
    public static final String BRD_CONTRACT_ADDRESS = BuildConfig.BITCOIN_TESTNET ? "0x7108ca7c4718efa810457f228305c9c71390931a" : "0x558ec3152e2eb2174905cd19aea4e34a23de9ad6";
    public static final String BRD_CURRENCY_CODE = "BRD";

    private WalletTokenManager(WalletEthManager walletEthManager, BREthereumWallet tokenWallet) {
        mWalletEthManager = walletEthManager;
        mWalletToken = tokenWallet;
        String currencyCode = tokenWallet.getSymbol();
        mUiConfig = new WalletUiConfiguration(TokenUtil.getTokenStartColor(currencyCode),
                TokenUtil.getTokenEndColor(currencyCode), false, WalletManagerHelper.MAX_DECIMAL_PLACES_FOR_UI);
        mAddress = mWalletEthManager.getAddress(null);
    }

    private synchronized static WalletTokenManager getTokenWallet(WalletEthManager walletEthManager, BREthereumToken token) {
        if (mTokenWallets.containsKey(token.getAddress().toLowerCase())) {
            return mTokenWallets.get(token.getAddress().toLowerCase());
        } else {
            BREthereumWallet w = walletEthManager.node.getWallet(token);

            if (w != null) {
                w.setDefaultUnit(BREthereumAmount.Unit.TOKEN_DECIMAL);
                w.estimateGasPrice();
                if (w.getToken() == null) {
                    BRReportsManager.reportBug(new NullPointerException("getToken is null:" + token.getAddress()));
                    return null;
                }
                WalletTokenManager wm = new WalletTokenManager(walletEthManager, w);
                mTokenWallets.put(w.getToken().getAddress().toLowerCase(), wm);

                return wm;
            } else {
                BRReportsManager.reportBug(new NullPointerException("Failed to find token by address: " + token.getAddress()), true);
            }

        }
        return null;
    }

    //for testing only
    public static WalletTokenManager getBrdWallet(WalletEthManager walletEthManager) {
        BREthereumWallet brdWallet = walletEthManager.node.getWallet(walletEthManager.node.getBRDToken());
        if (brdWallet.getToken() == null) {
            BRReportsManager.reportBug(new NullPointerException("getBrd failed"));
            return null;
        }
        return new WalletTokenManager(walletEthManager, brdWallet);
    }

    public String getContractAddress() {
        return mWalletToken.getToken().getAddress();
    }

    public synchronized static WalletTokenManager getTokenWalletByIso(Context context, String iso) {

        WalletEthManager walletEthManager = WalletEthManager.getInstance(context);

        long start = System.currentTimeMillis();
        if (mTokenIsos.size() <= 0) mapTokenIsos(context);

        String address = mTokenIsos.get(iso.toLowerCase());
        address = address == null ? null : address.toLowerCase();
        if (address != null) {
            if (mTokenWallets.containsKey(address)) {
                return mTokenWallets.get(address);
            }
            BREthereumToken token = walletEthManager.node.lookupToken(address);
            if (token != null) {
                return getTokenWallet(walletEthManager, token);
            } else
                BRReportsManager.reportBug(new NullPointerException("Failed to getTokenWalletByIso: " + iso + ":" + address));
        }
        return null;
    }


    public static synchronized void mapTokenIsos(Context context) {
        for (TokenItem tokenItem : TokenUtil.getTokenItems(context)) {
            if (!mTokenIsos.containsKey(tokenItem.symbol.toLowerCase())) {
                mTokenIsos.put(tokenItem.symbol.toLowerCase(), tokenItem.address.toLowerCase());
            }
        }
    }

    @Override
    public BREthereumAmount.Unit getUnit() {
        return BREthereumAmount.Unit.TOKEN_INTEGER;
    }

    @Override
    public byte[] signAndPublishTransaction(CryptoTransaction tx, byte[] seed) {
        mWalletToken.sign(tx.getEtherTx(), new String(seed));
        mWalletToken.submit(tx.getEtherTx());
        String hash = tx.getEtherTx().getHash();
        return hash == null ? new byte[0] : hash.getBytes();
    }

    @Override
    public void watchTransactionForHash(CryptoTransaction tx, OnHashUpdated listener) {
        mWalletEthManager.watchTransactionForHash(tx, listener);
    }

    @Override
    public long getRelayCount(byte[] txHash) {
        return 3; // ready to go
    }

    @Override
    public double getSyncProgress(long startHeight) {
        return mWalletEthManager.getSyncProgress(startHeight);
    }

    @Override
    public double getConnectStatus() {
        return mWalletEthManager.getConnectStatus();
    }

    @Override
    public void connect(Context context) {
        //no need for Tokens
    }

    @Override
    public void disconnect(Context context) {
        //no need for Tokens
    }

    @Override
    public boolean useFixedNode(String node, int port) {
        //no need for tokens
        return false;
    }

    @Override
    public void rescan(Context context) {
        //no need for tokens
    }

    @Override
    public CryptoTransaction[] getTxs(Context context) {
        long start = System.currentTimeMillis();
        BREthereumTransaction[] txs = mWalletToken.getTransactions();
        CryptoTransaction[] arr = new CryptoTransaction[txs.length];
        for (int i = 0; i < txs.length; i++) {
            arr[i] = new CryptoTransaction(txs[i]);
        }
        return arr;
    }

    @Override
    public BigDecimal getTxFee(CryptoTransaction tx) {
        return new BigDecimal(tx.getEtherTx().getFee(BREthereumAmount.Unit.ETHER_WEI));
    }

    @Override
    public BigDecimal getEstimatedFee(BigDecimal amount, String address) {
        BigDecimal fee;
        if (amount == null) {
            return null;
        }
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            fee = BigDecimal.ZERO;
        } else {
            String feeString = mWalletToken.transactionEstimatedFee(amount.toPlainString(),
                    BREthereumAmount.Unit.TOKEN_INTEGER, BREthereumAmount.Unit.ETHER_WEI);
            fee = Utils.isNullOrEmpty(feeString) ? BigDecimal.ZERO : new BigDecimal(feeString);
        }
        return fee;
    }

    @Override
    public BigDecimal getFeeForTransactionSize(BigDecimal size) {
        return null;
    }

    @Override
    public String getTxAddress(CryptoTransaction tx) {
        return mWalletEthManager.getTxAddress(tx);
    }

    @Override
    public BigDecimal getMaxOutputAmount(Context context) {
        return mWalletEthManager.getMaxOutputAmount(context);
    }

    @Override
    public BigDecimal getMinOutputAmount(Context context) {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTransactionAmount(CryptoTransaction tx) {
        return mWalletEthManager.getTransactionAmount(tx);
    }

    @Override
    public BigDecimal getMinOutputAmountPossible() {
        return mWalletEthManager.getMinOutputAmountPossible();
    }

    @Override
    public void updateFee(Context context) {
        //no need
    }

    @Override
    public boolean containsAddress(String address) {
        return mWalletEthManager.containsAddress(address);
    }

    @Override
    public boolean addressIsUsed(String address) {
        return mWalletEthManager.addressIsUsed(address);
    }

    @Override
    public boolean generateWallet(Context context) {
        return false;
    }

    @Override
    public String getSymbol(Context context) {
        return mWalletToken.getToken().getSymbol();
    }

    @Override
    public String getCurrencyCode() {
        return mWalletToken.getToken().getSymbol();
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public String getName() {
        return mWalletToken.getToken().getName();
    }

    @Override
    public String getDenominator() {
        return BigDecimal.TEN.pow(mWalletToken.getToken().getDecimals()).toPlainString();
    }

    @Override
    public CryptoAddress getReceiveAddress(Context context) {
        return mWalletEthManager.getReceiveAddress(context);
    }

    @Override
    public CryptoTransaction createTransaction(BigDecimal amount, String address) {
        BREthereumTransaction tx = mWalletToken.createTransaction(address, amount.toPlainString(), BREthereumAmount.Unit.TOKEN_INTEGER);
        return new CryptoTransaction(tx);
    }

    @Override
    public String decorateAddress(String addr) {
        return addr;
    }

    @Override
    public String undecorateAddress(String addr) {
        return addr;
    }

    @Override
    public int getMaxDecimalPlaces(Context context) {
        int tokenDecimals = mWalletToken.getToken().getDecimals();
        boolean isMaxDecimalLargerThanTokenDecimals = WalletManagerHelper.MAX_DECIMAL_PLACES > tokenDecimals;

        return isMaxDecimalLargerThanTokenDecimals ? tokenDecimals : WalletManagerHelper.MAX_DECIMAL_PLACES;
    }

    @Override
    public BigDecimal getBalance() {
        return new BigDecimal(mWalletToken.getBalance(BREthereumAmount.Unit.TOKEN_INTEGER));
    }

    @Override
    public BigDecimal getTotalSent(Context context) {
        return mWalletEthManager.getTotalSent(context);
    }

    @Override
    public void wipeData(Context context) {
        //Not needed for Tokens
    }

    @Override
    public void syncStarted() {
        //Not needed for Tokens
    }

    @Override
    public void syncStopped(String error) {
        //Not needed for Tokens
    }

    @Override
    public boolean networkIsReachable() {
        return mWalletEthManager.networkIsReachable();
    }

    @Override
    public BigDecimal getMaxAmount(Context context) {
        return mWalletEthManager.getMaxAmount(context);
    }

    @Override
    public WalletUiConfiguration getUiConfiguration() {
        return mUiConfig;
    }

    @Override
    public WalletSettingsConfiguration getSettingsConfiguration() {
        //no settings for tokens, so empty
        return new WalletSettingsConfiguration();
    }

    @Override
    public BigDecimal getFiatExchangeRate(Context context) {
        BigDecimal fiatData = getFiatForToken(context, BigDecimal.ONE, BRSharedPrefs.getPreferredFiatIso(context));
        if (fiatData == null) {
            return BigDecimal.ZERO;
        }
        return fiatData; //fiat, e.g. dollars
    }

    @Override
    public BigDecimal getFiatBalance(Context context) {
        if (context == null) {
            return null;
        }
        return getFiatForSmallestCrypto(context, getBalance(), null);
    }

    @Override
    public BigDecimal getFiatForSmallestCrypto(Context context, BigDecimal
            amount, CurrencyEntity ent) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return amount;
        }
        String iso = BRSharedPrefs.getPreferredFiatIso(context);
        if (ent != null) {
            //passed in a custom CurrencyEntity
            //get crypto amount
            //multiply by fiat rate
            return getCryptoForSmallestCrypto(context, amount).multiply(new BigDecimal(ent.rate));
        }
        BigDecimal cryptoAmount = getCryptoForSmallestCrypto(context, amount);
        BigDecimal fiatData = getFiatForToken(context, cryptoAmount, iso);
        if (fiatData == null) {
            return null;
        }
        return fiatData;
    }

    @Override
    public BigDecimal getCryptoForFiat(Context context, BigDecimal fiatAmount) {
        if (fiatAmount == null || fiatAmount.compareTo(BigDecimal.ZERO) == 0) {
            return fiatAmount;
        }
        String iso = BRSharedPrefs.getPreferredFiatIso(context);
        return getTokensForFiat(context, fiatAmount, iso);
    }

    @Override
    public BigDecimal getCryptoForSmallestCrypto(Context context, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return amount;
        }
        return amount.divide(new BigDecimal(getDenominator()), mWalletToken.getToken().getDecimals(), ROUNDING_MODE);
    }

    @Override
    public BigDecimal getSmallestCryptoForCrypto(Context context, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return amount;
        }
        return amount.multiply(new BigDecimal(getDenominator())).stripTrailingZeros();
    }

    @Override
    public BigDecimal getSmallestCryptoForFiat(Context context, BigDecimal amount) {
        BigDecimal convertedCryptoAmount = getCryptoForFiat(context, amount);
        //Round the amount up for situations when the decimals of a token is smaller than the precision we're using.
        if (convertedCryptoAmount != null) {
            convertedCryptoAmount = convertedCryptoAmount.setScale(getMaxDecimalPlaces(context), BRConstants.ROUNDING_MODE);
        }
        return getSmallestCryptoForCrypto(context, convertedCryptoAmount);
    }

    //pass in a token amount and return the specified amount in fiat
    //erc20 rates are in BTC (thus this math)
    private BigDecimal getFiatForToken(Context context, BigDecimal tokenAmount, String
            code) {
        //fiat rate for btc
        CurrencyEntity rate = RatesDataSource.getInstance(context).getCurrencyByCode(context, WalletBitcoinManager.BITCOIN_CURRENCY_CODE, code);
        //Btc rate for the token
        CurrencyEntity tokenBtcRate = RatesDataSource.getInstance(context).getCurrencyByCode(context, getCurrencyCode(), WalletBitcoinManager.BITCOIN_CURRENCY_CODE);

        if (rate == null) {
            Log.e(TAG, "getUsdFromBtc: No USD rates for BTC or ETH");
            return null;
        }
        if (tokenBtcRate == null) {
            Log.e(TAG, "getUsdFromBtc: No BTC or ETH rates for token");
            return null;
        }
        if (tokenBtcRate.rate == 0 || rate.rate == 0) {
            return BigDecimal.ZERO;
        }

        return tokenAmount.multiply(new BigDecimal(tokenBtcRate.rate)).multiply(new BigDecimal(rate.rate));
    }

    //pass in a fiat amount and return the specified amount in tokens
    //Token rates are in BTC (thus this math)
    private BigDecimal getTokensForFiat(Context context, BigDecimal fiatAmount, String
            code) {
        //fiat rate for btc
        CurrencyEntity btcRate = RatesDataSource.getInstance(context).getCurrencyByCode(context, WalletBitcoinManager.BITCOIN_CURRENCY_CODE, code);
        //Btc rate for token
        CurrencyEntity tokenBtcRate = RatesDataSource.getInstance(context).getCurrencyByCode(context, getCurrencyCode(), WalletBitcoinManager.BITCOIN_CURRENCY_CODE);
        if (btcRate == null) {
            Log.e(TAG, "getUsdFromBtc: No USD rates for BTC");
            return null;
        }
        if (tokenBtcRate == null) {
            Log.e(TAG, "getUsdFromBtc: No BTC rates for ETH");
            return null;
        }

        return fiatAmount.divide(new BigDecimal(tokenBtcRate.rate).multiply(new BigDecimal(btcRate.rate)), SCALE, BRConstants.ROUNDING_MODE);
    }

    public static void printTxInfo(BREthereumTransaction tx) {
        StringBuilder builder = new StringBuilder();
        builder.append("|Tx:");
        builder.append("|Amount:");
        builder.append(tx.getAmount());
        builder.append("|Fee:");
        builder.append(tx.getFee());
        builder.append("|Source:");
        builder.append(tx.getSourceAddress());
        builder.append("|Target:");
        builder.append(tx.getTargetAddress());
        Log.e(TAG, "printTxInfo: " + builder.toString());
    }

    @Override
    public BREthereumWallet getWallet() {
        return mWalletToken;
    }

    @Override
    protected WalletEthManager getEthereumWallet() {
        return mWalletEthManager;
    }
}
