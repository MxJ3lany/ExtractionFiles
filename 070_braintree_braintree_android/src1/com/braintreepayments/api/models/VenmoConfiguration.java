package com.braintreepayments.api.models;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.braintreepayments.api.Json;
import com.braintreepayments.api.Venmo;

import org.json.JSONObject;

/**
 * Contains the remote Venmo configuration for the Braintree SDK.
 */
public class VenmoConfiguration {

    private static final String ACCESS_TOKEN_KEY = "accessToken";
    private static final String ENVIRONMENT_KEY = "environment";
    private static final String MERCHANT_ID_KEY = "merchantId";

    private String mAccessToken;
    private String mEnvironment;
    private String mMerchantId;

    /**
     * Parses the Venmo configuration from json.
     *
     * @param json The json to parse.
     * @return A {@link VenmoConfiguration} instance with data that was able to be parsed from the
     * {@link JSONObject}.
     */
    static VenmoConfiguration fromJson(JSONObject json) {
        if (json == null) {
            json = new JSONObject();
        }

        VenmoConfiguration venmoConfiguration = new VenmoConfiguration();
        venmoConfiguration.mAccessToken = Json.optString(json, ACCESS_TOKEN_KEY, "");
        venmoConfiguration.mEnvironment = Json.optString(json, ENVIRONMENT_KEY, "");
        venmoConfiguration.mMerchantId = Json.optString(json, MERCHANT_ID_KEY, "");

        return venmoConfiguration;
    }

    /**
     * @return The access token to use with Venmo.
     */
    public String getAccessToken() {
        return mAccessToken;
    }

    /**
     * @return The merchant Id associated with this merchant's Venmo integration.
     */
    public String getMerchantId() {
        return mMerchantId;
    }

    /**
     * @return The Venmo environment the merchant is running in.
     */
    public String getEnvironment() {
        return mEnvironment;
    }

    /**
     * Determines if the Venmo flow is available to be used. This can be used to determine
     * if UI components should be shown or hidden.
     *
     * @param context A context to access the {@link PackageManager}
     * @return boolean if Venmo is enabled, and available to be used
     */
    public boolean isEnabled(Context context) {
        return isAccessTokenValid() && Venmo.isVenmoInstalled(context);
    }

    public boolean isAccessTokenValid() {
        return !TextUtils.isEmpty(mAccessToken);
    }
}
