package com.platform.tools;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;

import com.breadwallet.core.BRCoreKey;
import com.breadwallet.presenter.entities.CurrencyEntity;
import com.breadwallet.protocols.messageexchange.entities.PairingMetaData;
import com.breadwallet.tools.crypto.CryptoHelper;
import com.breadwallet.tools.manager.BRReportsManager;
import com.breadwallet.tools.manager.BRSharedPrefs;
import com.breadwallet.tools.sqlite.RatesDataSource;
import com.breadwallet.tools.util.BRCompressor;
import com.breadwallet.tools.util.BRConstants;
import com.breadwallet.tools.util.Utils;
import com.breadwallet.wallet.abstracts.BaseWalletManager;
import com.breadwallet.wallet.wallets.CryptoTransaction;
import com.platform.APIClient;
import com.platform.entities.TokenListMetaData;
import com.platform.entities.TxMetaData;
import com.platform.entities.WalletInfoData;
import com.platform.kvstore.CompletionObject;
import com.platform.kvstore.RemoteKVStore;
import com.platform.kvstore.ReplicatedKVStore;
import com.platform.sqlite.KVItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BreadWallet
 * <p/>
 * Created by Mihail Gutan on <mihail@breadwallet.com> 6/22/17.
 * Copyright (c) 2017 breadwallet LLC
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
public class KVStoreManager {
    private static final String TAG = KVStoreManager.class.getName();

    private static final String KEY_WALLET_INFO = "wallet-info";
    private static final String KEY_SEGWIT_META_DATA = "segwit-metadata";
    private static final String KEY_TOKEN_LIST_META_DATA = "token-list-metadata";
    private static final String KEY_PAIRING_META_DATA = "pairing-metadata";
    private static final String KEY_CURSOR = "lastCursor";
    private static final String CLASS_VERSION = "classVersion";
    private static final String CREATION_DATE = "creationDate";
    private static final String ENABLED_AT_BLOCK_HEIGHT = "enabledAtBlockHeight";
    private static final String NAME = "name";
    private static final String MY_BREAD = "My Bread";
    private static final String ENABLED_CURRENCIES = "enabledCurrencies";
    private static final String HIDDEN_CURRENCIES = "hiddenCurrencies";
    private static final String BLOCK_HEIGHT = "bh";
    private static final String EXCHANGE_RATE = "er";
    private static final String EXCHANGE_CURRENCY = "erc";
    private static final String FEE_RATE = "fr";
    private static final String TX_SIZE = "s";
    private static final String CREATION_TIME = "c";
    private static final String DEVICE_ID = "dId";
    private static final String COMMENT = "comment";
    private static final String IDENTIFIER = "identifier";
    private static final String SERVICE = "service";
    private static final String REMOTE_PUBKEY = "remotePubKey";
    private static final String CREATED = "created";
    private static final String RETURN_URL = "return-to";
    private static final String CURSOR = "cursor";
    private static final String TX_META_DATA_KEY_PREFIX = "txn2-";
    private static final String PAIRING_META_DATA_KEY_PREFIX = "pwd-";


    private KVStoreManager() {
    }

    public static WalletInfoData getWalletInfo(Context context) {
        WalletInfoData result = new WalletInfoData();
        byte[] data = getData(context, KEY_WALLET_INFO);

        JSONObject json;

        try {
            if (data == null) {
                Log.e(TAG, "getWalletInfo: data value is null");
                return null;
            }
            json = new JSONObject(new String(data));
        } catch (JSONException e) {
            Log.e(TAG, "getWalletInfo: ", e);
            return null;
        }

        try {
            result.classVersion = json.getInt(CLASS_VERSION);
            result.creationDate = json.getInt(CREATION_DATE);
            result.name = json.getString(NAME);
            Log.d(TAG, "getWalletInfo: " + result.creationDate + ", name: " + result.name);
        } catch (JSONException e) {
            Log.e(TAG, "getWalletInfo: ", e);
        }

        Log.e(TAG, "getWalletInfo: " + json);
        return result;
    }

    public static void putWalletInfo(Context app, WalletInfoData info) {
        WalletInfoData old = getWalletInfo(app);
        if (old == null) {
            old = new WalletInfoData(); //create new one if it's null
        }

        //add all the params that we want to change
        if (info.classVersion != 0) {
            old.classVersion = info.classVersion;
        }
        if (info.creationDate != 0) {
            old.creationDate = info.creationDate;
        }
        if (info.name != null) {
            old.name = info.name;
        }

        //sanity check
        if (old.classVersion == 0) {
            old.classVersion = 1;
        }
        if (old.name != null) {
            old.name = MY_BREAD;
        }

        JSONObject obj = new JSONObject();
        byte[] result;
        try {
            obj.put(CLASS_VERSION, old.classVersion);
            obj.put(CREATION_DATE, old.creationDate);
            obj.put(NAME, old.name);
            result = obj.toString().getBytes();

        } catch (JSONException e) {
            Log.e(TAG, "putWalletInfo: ", e);
            return;
        }

        if (result.length == 0) {
            Log.e(TAG, "putWalletInfo: FAILED: result is empty");
            return;
        }
        CompletionObject completionObject = setData(app, result, KEY_WALLET_INFO);
        if (completionObject != null && completionObject.err != null) {
            Log.e(TAG, "setData: Error setting value for key: " + KEY_WALLET_INFO + ", err: " + completionObject.err);
        }

    }

    /**
     * Synchronize wallet info with KV store.
     *
     * @param context Caller context.
     */
    public static void syncWalletInfo(Context context) {
        ReplicatedKVStore replicatedKVStore = getReplicatedKvStore(context);
        replicatedKVStore.syncKey(KEY_WALLET_INFO);
    }

    public static PairingMetaData getPairingMetadata(Context context, byte[] pubKey) {
        Log.e(TAG, "putPairingMetadata: hexPubkey: " + BRCoreKey.encodeHex(pubKey));
        String key = pairingKey(pubKey);
        byte[] data = getData(context, key);

        JSONObject json;

        try {
            if (data == null) {
                Log.e(TAG, "getPairingMetadata: data value is null");
                return null;
            }
            json = new JSONObject(new String(data));
        } catch (JSONException e) {
            Log.e(TAG, "getPairingMetadata: ", e);
            return null;
        }

        int classVersion = 0;
        String identifier = null;
        String service = null;
        String remotePubKey = null;
        long created = 0;
        String returnURl = null;

        try {
            classVersion = json.getInt(CLASS_VERSION);
            identifier = json.getString(IDENTIFIER);
            service = json.getString(SERVICE);
            remotePubKey = json.getString(REMOTE_PUBKEY);
            created = json.getLong(CREATED);
            returnURl = json.getString(RETURN_URL);
        } catch (JSONException e) {
            Log.e(TAG, "getPairingMetadata: ", e);
        }
        Log.e(TAG, "getPairingMetadata: " + key);
        String hexPubKey = BRCoreKey.encodeHex(Base64.decode(remotePubKey, Base64.NO_WRAP));
        return new PairingMetaData(identifier, hexPubKey, service, returnURl);
    }

    public static void putPairingMetadata(Context app, PairingMetaData pairingData) {
        Log.d(TAG, "putPairingMetadata: hexPubkey: " + pairingData.getPublicKeyHex());
        if (pairingData.getPublicKeyHex() != null) {
            byte[] rawPubKey = BRCoreKey.decodeHex(pairingData.getPublicKeyHex());
            String base64PubKey = Base64.encodeToString(rawPubKey, Base64.NO_WRAP);

            JSONObject obj = new JSONObject();
            byte[] result;
            try {
                obj.put(CLASS_VERSION, 1);
                obj.put(IDENTIFIER, pairingData.getId());
                obj.put(SERVICE, pairingData.getService());
                obj.put(REMOTE_PUBKEY, base64PubKey);
                obj.put(CREATED, System.currentTimeMillis());
                obj.put(RETURN_URL, pairingData.getReturnUrl());
                result = obj.toString().getBytes();

            } catch (JSONException e) {
                Log.e(TAG, "putPairingMetadata: ", e);
                return;
            }

            if (result.length == 0) {
                Log.e(TAG, "putPairingMetadata: FAILED: result is empty");
                return;
            }
            String key = pairingKey(rawPubKey);
            Log.e(TAG, "putPairingMetadata: " + key);
            CompletionObject completionObject = setData(app, result, key);
            if (completionObject != null && completionObject.err != null) {
                Log.e(TAG, "putPairingMetadata: Error setting value for key: " + KEY_PAIRING_META_DATA + ", err: " + completionObject.err);
            }
        } else {
            Log.e(TAG, "putPairingMetadata: pairingData.getPublicKeyHex() is null!");
        }
    }

    public static String getLastCursor(Context context) {

        JSONObject json;
        byte[] data = getData(context, KEY_CURSOR);
        try {
            if (data == null) {
                Log.e(TAG, "getLastCursor: data value is null");
                return null;
            }
            json = new JSONObject(new String(data));
        } catch (JSONException e) {
            Log.e(TAG, "getLastCursor: ", e);
            return null;
        }

        String cursor = null;

        try {
            cursor = json.getString(CURSOR);
        } catch (JSONException e) {
            Log.e(TAG, "getLastCursor: ", e);
        }
        Log.e(TAG, "getLastCursor: " + KEY_CURSOR);
        return cursor;
    }

    public static void putLastCursor(Context app, String lastCursor) {
        JSONObject obj = new JSONObject();
        byte[] result;
        try {
            obj.put(CURSOR, lastCursor);
            result = obj.toString().getBytes();

        } catch (JSONException e) {
            Log.e(TAG, "putLastCursor: ", e);
            return;
        }

        if (result.length == 0) {
            Log.e(TAG, "putLastCursor: FAILED: result is empty");
            return;
        }
        Log.e(TAG, "putLastCursor: " + lastCursor);
        CompletionObject completionObject = setData(app, result, KEY_CURSOR);
        if (completionObject != null && completionObject.err != null) {
            Log.e(TAG, "putLastCursor: Error setting value for key: " + KEY_CURSOR + ", err: " + completionObject.err);
        }

    }

    /* unused code
    public static SegWitMetaData getSegwit(Context context) {
        JSONObject json;
        byte[] data = getData(context, KEY_SEGWIT_META_DATA);
        try {
            if (data == null) {
                Log.e(TAG, "getSegwit: data value is null");
                return null;
            }
            json = new JSONObject(new String(data));
        } catch (JSONException e) {
            Log.e(TAG, "getSegwit: ", e);
            return null;
        }

        int classVersion = 0;
        int enabledAtBlockHeight = 0;

        try {
            classVersion = json.getInt(CLASS_VERSION);
            enabledAtBlockHeight = json.getInt(ENABLED_AT_BLOCK_HEIGHT);
        } catch (JSONException e) {
            Log.e(TAG, "getSegwit: ", e);
        }
        Log.d(TAG, "getSegwit: " + KEY_SEGWIT_META_DATA);
        return new SegWitMetaData(classVersion, enabledAtBlockHeight);
    }

    public static void putSegwit(Context app, SegWitMetaData segwitData) {
        JSONObject obj = new JSONObject();
        byte[] result;
        try {
            obj.put(CLASS_VERSION, segwitData.getClassVersion());
            obj.put(ENABLED_AT_BLOCK_HEIGHT, segwitData.getEnabledAtBlockHeight());
            result = obj.toString().getBytes();

        } catch (JSONException e) {
            Log.e(TAG, "putSegwit: ", e);
            return;
        }

        if (result.length == 0) {
            Log.e(TAG, "putSegwit: FAILED: result is empty");
            return;
        }
        CompletionObject completionObject = setData(app, result, KEY_SEGWIT_META_DATA);
        if (completionObject != null && completionObject.err != null) {
            Log.e(TAG, "putSegwit: Error setting value for key: " + KEY_SEGWIT_META_DATA + ", err: " + completionObject.err);
        }
    }
*/
    public static synchronized void putTokenListMetaData(Context app, TokenListMetaData md) {
        TokenListMetaData old = getTokenListMetaData(app);
        if (old == null) {
            old = new TokenListMetaData(null, null); //create new one if it's null
        }

        //add all the params that we want to change
        if (md.enabledCurrencies.size() > 0) {
            old.enabledCurrencies = md.enabledCurrencies;
        }
        old.hiddenCurrencies = md.hiddenCurrencies;

        JSONObject obj = new JSONObject();
        byte[] result;
        try {
            obj.put(CLASS_VERSION, TokenListMetaData.CLASS_VERSION);
            JSONArray enabledArr = new JSONArray();
            JSONArray hiddenArr = new JSONArray();
            for (TokenListMetaData.TokenInfo item : old.enabledCurrencies) {
                enabledArr.put(item.erc20 ? item.symbol + ":" + item.contractAddress : item.symbol);
            }
            for (TokenListMetaData.TokenInfo item : old.hiddenCurrencies) {
                hiddenArr.put(item.erc20 ? item.symbol + ":" + item.contractAddress : item.symbol);
            }

            obj.put(ENABLED_CURRENCIES, enabledArr);
            obj.put(HIDDEN_CURRENCIES, hiddenArr);
            result = obj.toString().getBytes();

        } catch (JSONException e) {
            Log.e(TAG, "putTokenListMetaData: ", e);
            return;
        }

        if (result.length == 0) {
            Log.e(TAG, "putTokenListMetaData: FAILED: result is empty");
            return;
        }
        CompletionObject completionObject = setData(app, result, KEY_TOKEN_LIST_META_DATA);
        if (completionObject != null && completionObject.err != null) {
            Log.e(TAG, "putTokenListMetaData: Error setting value for key: " + KEY_TOKEN_LIST_META_DATA + ", err: " + completionObject.err);
        }

    }

    //expensive, takes ~ 20 milliseconds
    public static synchronized TokenListMetaData getTokenListMetaData(Context context) {

        byte[] data = getData(context, KEY_TOKEN_LIST_META_DATA);

        JSONObject json;

        try {
            if (data == null) {
                Log.e(TAG, "getTokenListMetaData: data value is null");
                return null;
            }
            json = new JSONObject(new String(data));
        } catch (JSONException e) {
            Log.e(TAG, "getTokenListMetaData: ", e);
            return null;
        }


        TokenListMetaData result = null;
        try {
            int classVersion = json.getInt(CLASS_VERSION); //not using yet
            List<TokenListMetaData.TokenInfo> enabledCurrencies = jsonToMetaData(json.getJSONArray(ENABLED_CURRENCIES));
            List<TokenListMetaData.TokenInfo> hiddenCurrencies = jsonToMetaData(json.getJSONArray(HIDDEN_CURRENCIES));
            result = new TokenListMetaData(enabledCurrencies, hiddenCurrencies);
        } catch (JSONException e) {
            Log.e(TAG, "getTokenListMetaData: ", e);
        }

        return result;
    }

    private static List<TokenListMetaData.TokenInfo> jsonToMetaData(JSONArray json) throws JSONException {
        List<TokenListMetaData.TokenInfo> result = new ArrayList<>();
        if (json == null) {
            Log.e(TAG, "jsonToMetaData: JSONArray is null");
            return result;
        }
        for (int i = 0; i < json.length(); i++) {
            String s = json.getString(i);
            boolean isErc20 = s.contains(":");
            String symbol = s;
            String address = null;
            if (isErc20) {
                symbol = s.split(":")[0];
                address = s.split(":")[1];
            }
            result.add(new TokenListMetaData.TokenInfo(symbol, isErc20, address));
        }

        return result;
    }

    public static TxMetaData getTxMetaData(Context context, byte[] txHash) {
        String key = txKey(txHash);

        ReplicatedKVStore kvStore = getReplicatedKvStore(context);
        long ver = kvStore.localVersion(key).version;

        CompletionObject obj = kvStore.get(key, ver);

        if (obj.kv == null) {
//            Log.e(TAG, "getTxMetaData: kv is null for key: " + key);
            return null;
        }

        return valueToMetaData(obj.kv.value);
    }

    public static Map<String, TxMetaData> getAllTxMD(Context context) {
        Map<String, TxMetaData> mds = new HashMap<>();
        ReplicatedKVStore kvStore = getReplicatedKvStore(context);
        List<KVItem> list = kvStore.getAllTxMdKv();
        for (int i = 0; i < list.size(); i++) {
            TxMetaData md = valueToMetaData(list.get(i).value);
            if (md != null) mds.put(list.get(i).key, md);
        }

        return mds;
    }

    private static TxMetaData valueToMetaData(byte[] value) {
        TxMetaData result = new TxMetaData();
        JSONObject json;
        if (value == null) {
            Log.e(TAG, "valueToMetaData: value is null!");
            return null;
        }
        try {
            byte[] decompressed = BRCompressor.bz2Extract(value);
            if (decompressed == null) {
                Log.e(TAG, "getTxMetaData: decompressed value is null");
                return null;
            }
            json = new JSONObject(new String(decompressed));
        } catch (JSONException e) {

            Log.e(TAG, "valueToMetaData: " + new String(value) + ":", e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "valueToMetaData: ", e);
            return null;
        }

        try {
            if (json.has(CLASS_VERSION)) {
                result.classVersion = json.getInt(CLASS_VERSION);
            }
            if (json.has(BLOCK_HEIGHT)) {
                result.blockHeight = json.getInt(BLOCK_HEIGHT);
            }
            if (json.has(EXCHANGE_RATE)) {
                result.exchangeRate = json.getDouble(EXCHANGE_RATE);
            }
            if (json.has(EXCHANGE_CURRENCY)) {
                result.exchangeCurrency = json.getString(EXCHANGE_CURRENCY);
            }
            if (json.has(COMMENT)) {
                result.comment = json.getString(COMMENT);
            }
            if (json.has(FEE_RATE)) {
                result.fee = json.getString(FEE_RATE);
            }
            if (json.has(TX_SIZE)) {
                result.txSize = json.getInt(TX_SIZE);
            }
            if (json.has(CREATION_TIME)) {
                result.creationTime = json.getInt(CREATION_TIME);
            }
            if (json.has(DEVICE_ID)) {
                result.deviceId = json.getString(DEVICE_ID);
            }
        } catch (JSONException e) {
            Log.e(TAG, "valueToMetaData: ", e);
        }
        return result;
    }

    /**
     * In this method we ONLY update the data if the comment was changed.
     * Populate other fields only if they're empty.
     *
     * @param context       The context in which we are operating.
     * @param newTxMetaData The transaction data to be used for insert or update.
     * @param txHash        The transaction hash for this transaction data.
     */
    public static void putTxMetaData(Context context, TxMetaData newTxMetaData, byte[] txHash) {
        String key = txKey(txHash);
        TxMetaData oldTxMetaData = getTxMetaData(context, txHash);

        boolean needsUpdate = false;
        if (oldTxMetaData == null) {
            needsUpdate = true;
            oldTxMetaData = newTxMetaData;
        } else if (newTxMetaData != null) {
            String finalComment = getFinalValue(newTxMetaData.comment, oldTxMetaData.comment);
            //We might need to update this if it changed
            if (finalComment != null) {
                oldTxMetaData.comment = finalComment;
                needsUpdate = true;
            }
            //We might need to update this if we didn't have a rate at the time of creating the tx.
            if (oldTxMetaData.exchangeRate == 0 && newTxMetaData.exchangeRate != 0) {
                oldTxMetaData.exchangeRate = newTxMetaData.exchangeRate;
                needsUpdate = true;
            }
        }

        if (needsUpdate) {
            Log.d(TAG, "putTxMetaData: updating txMetadata for : " + key);

            JSONObject txMetadataJsonObject = new JSONObject();
            byte[] result;
            try {
                txMetadataJsonObject.put(CLASS_VERSION, oldTxMetaData.classVersion);
                txMetadataJsonObject.put(BLOCK_HEIGHT, oldTxMetaData.blockHeight);
                txMetadataJsonObject.put(EXCHANGE_RATE, oldTxMetaData.exchangeRate);
                txMetadataJsonObject.put(EXCHANGE_CURRENCY, oldTxMetaData.exchangeCurrency == null ? "" : oldTxMetaData.exchangeCurrency);
                txMetadataJsonObject.put(FEE_RATE, oldTxMetaData.fee);
                txMetadataJsonObject.put(TX_SIZE, oldTxMetaData.txSize);
                txMetadataJsonObject.put(CREATION_TIME, oldTxMetaData.creationTime);
                txMetadataJsonObject.put(DEVICE_ID, oldTxMetaData.deviceId == null ? "" : oldTxMetaData.deviceId);
                txMetadataJsonObject.put(COMMENT, oldTxMetaData.comment == null ? "" : oldTxMetaData.comment);
                result = txMetadataJsonObject.toString().getBytes();
                if (result.length == 0) {
                    Log.e(TAG, "putTxMetaData: FAILED: result is empty");
                }
                CompletionObject completionObject = setData(context, result, key);
                if (completionObject != null && completionObject.err != null) {
                    Log.e(TAG, "putTxMetaData: Error setting value for key: " + key + ", err: " + completionObject.err);
                }
            } catch (JSONException e) {
                Log.e(TAG, "putTxMetaData: ", e);
            }
        }

    }

    private static CompletionObject setData(Context context, byte[] data, String key) {
        byte[] compressed;
        try {
            compressed = BRCompressor.bz2Compress(data);
        } catch (IOException e) {
            BRReportsManager.reportBug(e);
            return null;
        }
        ReplicatedKVStore kvStore = getReplicatedKvStore(context);
        long localVer = kvStore.localVersion(key).version;
        long removeVer = kvStore.remoteVersion(key);
        return kvStore.set(localVer, removeVer, key, compressed, System.currentTimeMillis(), 0);

    }

    private static byte[] getData(Context context, String key) {
        ReplicatedKVStore kvStore = getReplicatedKvStore(context);
        long ver = kvStore.localVersion(key).version;
        CompletionObject obj = kvStore.get(key, ver);
        if (obj.kv == null) {
            Log.w(TAG, "getData: value is null for key: " + key);
            return null;
        }

        byte[] decompressed = BRCompressor.bz2Extract(obj.kv.value);
        if (decompressed == null) {
            Log.e(TAG, "getData: decompressed value is null");
            return null;
        }
        return decompressed;

    }

    //null means no change
    private static String getFinalValue(String newVal, String oldVal) {
        if (newVal == null) {
            return null;
        }
        if (oldVal == null) {
            return newVal;
        }
        if (newVal.equals(oldVal)) {
            return null;
        } else {
            return newVal;
        }
    }

    // -1 means no change
    private static int getFinalValue(int newVal, int oldVal) {
        if (newVal <= 0) {
            return -1;
        }
        if (oldVal <= 0) {
            return newVal;
        }
        if (newVal == oldVal) {
            return -1;
        } else {
            return newVal;
        }
    }

    public static TxMetaData createMetadata(Context app, BaseWalletManager wm, CryptoTransaction tx) {
        TxMetaData txMetaData = new TxMetaData();
        txMetaData.exchangeCurrency = BRSharedPrefs.getPreferredFiatIso(app);
        CurrencyEntity ent = RatesDataSource.getInstance(app).getCurrencyByCode(app, wm.getCurrencyCode(), txMetaData.exchangeCurrency);
        txMetaData.exchangeRate = ent == null ? 0 : new BigDecimal(ent.rate).setScale(8, BRConstants.ROUNDING_MODE).stripTrailingZeros().doubleValue();
        txMetaData.fee = wm.getTxFee(tx).toPlainString();
        txMetaData.txSize = tx.getTxSize().intValue();
        txMetaData.blockHeight = BRSharedPrefs.getLastBlockHeight(app, wm.getCurrencyCode());
        //seconds
        txMetaData.creationTime = (int) (System.currentTimeMillis() / DateUtils.SECOND_IN_MILLIS);
        txMetaData.deviceId = BRSharedPrefs.getDeviceId(app);
        txMetaData.classVersion = 1;

        return txMetaData;

    }


    // -1 means no change
    private static double getFinalValue(double newVal, double oldVal) {
        if (newVal <= 0) {
            return -1;
        }
        if (oldVal <= 0) {
            return newVal;
        }
        if (newVal == oldVal) {
            return -1;
        } else {
            return newVal;
        }
    }


    private static String txKey(byte[] txHash) {
        if (Utils.isNullOrEmpty(txHash)) {
            return null;
        }
        String hex = Utils.bytesToHex(CryptoHelper.sha256(txHash));
        if (Utils.isNullOrEmpty(hex)) {
            return null;
        }
        return TX_META_DATA_KEY_PREFIX + hex;
    }

    private static String pairingKey(byte[] pubKey) {
        String suffix = BRCoreKey.encodeHex(CryptoHelper.sha256(pubKey));
        return PAIRING_META_DATA_KEY_PREFIX + suffix;
    }

    private static ReplicatedKVStore getReplicatedKvStore(Context context) {
        RemoteKVStore remoteKVStore = RemoteKVStore.getInstance(APIClient.getInstance(context));
        return ReplicatedKVStore.getInstance(context, remoteKVStore);
    }
}
