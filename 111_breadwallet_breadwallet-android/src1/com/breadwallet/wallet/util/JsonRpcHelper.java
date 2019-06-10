package com.breadwallet.wallet.util;

import android.content.Context;
import android.support.annotation.WorkerThread;

import com.breadwallet.BuildConfig;
import com.breadwallet.tools.util.BRConstants;
import com.platform.APIClient;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * BreadWallet
 * <p/>
 * Created by Mihail Gutan on <mihail@breadwallet.com> 6/5/18.
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
public class JsonRpcHelper {
    private static final String BRD_ETH_RPC_ENDPOINT = BuildConfig.BITCOIN_TESTNET ? "/ethq/ropsten/proxy" : "/ethq/mainnet/proxy";
    private static final String BRD_ETH_TX_ENDPOINT = BuildConfig.BITCOIN_TESTNET ? "/ethq/ropsten/" : "/ethq/mainnet/";

    private JsonRpcHelper() {
    }

    public interface JsonRpcRequestListener {

        void onRpcRequestCompleted(String jsonResult);
    }

    public static String getEthereumRpcUrl() {
        return APIClient.getBaseURL() + JsonRpcHelper.BRD_ETH_RPC_ENDPOINT;
    }

    public static String createTokenTransactionsUrl(String address, String contractAddress) {
        return APIClient.getBaseURL() + BRD_ETH_TX_ENDPOINT + "query?" + "module=account&action=tokenbalance"
                + "&address=" + address + "&contractaddress=" + contractAddress;
    }

    public static String createEthereumTransactionsUrl(String address) {
        return APIClient.getBaseURL() + BRD_ETH_TX_ENDPOINT
                + "query?module=account&action=txlist&address=" + address;
    }

    public static String createLogsUrl(String address, String contract, String event) {

        return APIClient.getBaseURL() + BRD_ETH_TX_ENDPOINT + "query?"
                + "module=logs&action=getLogs"
                + "&fromBlock=0&toBlock=latest"
                + (null == contract ? "" : ("&address=" + contract))
                + "&topic0=" + event
                + "&topic1=" + address
                + "&topic1_2_opr=or"
                + "&topic2=" + address;
    }

    @WorkerThread
    public static void makeRpcRequest(Context app, String url, JSONObject payload, JsonRpcRequestListener listener) {
        final MediaType JSON = MediaType.parse(BRConstants.CONTENT_TYPE_JSON_CHARSET_UTF8);

        RequestBody requestBody = RequestBody.create(JSON, payload.toString());

        Request request = new Request.Builder()
                .url(url)
                .header(BRConstants.HEADER_CONTENT_TYPE, BRConstants.CONTENT_TYPE_JSON_CHARSET_UTF8)
                .header(BRConstants.HEADER_ACCEPT, BRConstants.CONTENT_TYPE_JSON_CHARSET_UTF8)
                .post(requestBody).build();

        APIClient.BRResponse resp = APIClient.getInstance(app).sendRequest(request, true);
        String responseString = resp.getBodyText();

        if (listener != null) {
            listener.onRpcRequestCompleted(responseString);
        }

    }
}
