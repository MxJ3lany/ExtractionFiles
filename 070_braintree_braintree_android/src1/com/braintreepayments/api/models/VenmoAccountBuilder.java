package com.braintreepayments.api.models;

import android.content.Context;

import com.braintreepayments.api.exceptions.BraintreeException;

import org.json.JSONException;
import org.json.JSONObject;

public class VenmoAccountBuilder extends PaymentMethodBuilder<VenmoAccountBuilder> {

    private final String VENMO_ACCOUNT_KEY = "venmoAccount";
    private final String NONCE_KEY = "nonce";

    private String mNonce;

    public VenmoAccountBuilder() {}

    public VenmoAccountBuilder nonce(String nonce) {
        mNonce = nonce;
        return this;
    }

    @Override
    protected void build(JSONObject base, JSONObject paymentMethodNonceJson) throws JSONException {
        paymentMethodNonceJson.put(NONCE_KEY, mNonce);
        base.put(VENMO_ACCOUNT_KEY, paymentMethodNonceJson);
    }

    @Override
    protected void buildGraphQL(Context context, JSONObject base, JSONObject input)
            throws BraintreeException, JSONException {}

    @Override
    public String getApiPath() {
        return "venmo_accounts";
    }

    @Override
    public String getResponsePaymentMethodType() {
        return VenmoAccountNonce.TYPE;
    }
}
