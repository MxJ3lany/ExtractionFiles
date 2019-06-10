package com.paypal.android.sdk.onetouch.core.sdk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.braintreepayments.api.internal.SignatureVerification;
import com.paypal.android.sdk.data.collector.InstallationIdentifier;
import com.paypal.android.sdk.onetouch.core.CheckoutRequest;
import com.paypal.android.sdk.onetouch.core.Request;
import com.paypal.android.sdk.onetouch.core.Result;
import com.paypal.android.sdk.onetouch.core.base.ContextInspector;
import com.paypal.android.sdk.onetouch.core.base.DeviceInspector;
import com.paypal.android.sdk.onetouch.core.config.ConfigManager;
import com.paypal.android.sdk.onetouch.core.config.Recipe;
import com.paypal.android.sdk.onetouch.core.enums.ResponseType;
import com.paypal.android.sdk.onetouch.core.exception.ResponseParsingException;
import com.paypal.android.sdk.onetouch.core.exception.WalletSwitchException;
import com.paypal.android.sdk.onetouch.core.fpti.TrackingPoint;
import com.paypal.android.sdk.onetouch.core.network.EnvironmentManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class AppSwitchHelper {

    private static final String WALLET_APP_PACKAGE = "com.paypal.android.p2pmobile";
    private static final String WALLET_APP_CERT_SUBJECT = "O=Paypal";
    private static final String WALLET_APP_CERT_ISSUER = "O=Paypal";
    private static final int WALLET_APP_PUBLIC_KEY_HASH_CODE = 34172764;

    public static boolean isSignatureValid(Context context, String packageName) {
        return SignatureVerification.isSignatureValid(context, packageName, WALLET_APP_CERT_SUBJECT,
                WALLET_APP_CERT_ISSUER, WALLET_APP_PUBLIC_KEY_HASH_CODE);
    }

    public static Intent createBaseIntent(String action, String packageName) {
        return new Intent(action).setPackage(packageName);
    }

    public static Intent getAppSwitchIntent(ContextInspector contextInspector, ConfigManager configManager,
            Request request, Recipe recipe) {
        Intent intent = createBaseIntent(recipe.getTargetIntentAction(), WALLET_APP_PACKAGE)
                .putExtra("version", recipe.getProtocol().getVersion())
                .putExtra("app_guid", InstallationIdentifier.getInstallationGUID(contextInspector.getContext()))
                .putExtra("client_metadata_id", request.getClientMetadataId())
                .putExtra("client_id", request.getClientId())
                .putExtra("app_name", DeviceInspector.getApplicationInfoName(contextInspector.getContext()))
                .putExtra("environment", request.getEnvironment())
                .putExtra("environment_url", EnvironmentManager.getEnvironmentUrl(request.getEnvironment()));

        CheckoutRequest checkoutRequest = (CheckoutRequest) request;
        String webURL = checkoutRequest.getBrowserSwitchUrl();
        intent.putExtra("response_type", "web")
            .putExtra("webURL", webURL);

        return intent;
    }

    public static Result parseAppSwitchResponse(ContextInspector contextInspector, Request request, Intent data) {
        Bundle bundle = data.getExtras();
        if (request.validateV1V2Response(bundle)) {
            request.trackFpti(contextInspector.getContext(), TrackingPoint.Return, null);
            return processResponseIntent(bundle);
        } else {
            if (bundle.containsKey("error")) {
                request.trackFpti(contextInspector.getContext(), TrackingPoint.Error, null);
                return new Result(new WalletSwitchException(bundle.getString("error")));
            } else {
                request.trackFpti(contextInspector.getContext(), TrackingPoint.Error, null);
                return new Result(new ResponseParsingException("invalid wallet response"));
            }
        }
    }

    private static Result processResponseIntent(Bundle bundle) {
        String error = bundle.getString("error");
        if (!TextUtils.isEmpty(error)) {
            return new Result(new WalletSwitchException(error));
        } else {
            String environment = bundle.getString("environment");
            String bundleResponseType = bundle.getString("response_type").toLowerCase(Locale.US);
            ResponseType response_type;
            if ("code".equals(bundleResponseType)) {
                response_type = ResponseType.authorization_code;
            } else {
                response_type = ResponseType.web;
            }

            try {
                if (ResponseType.web == response_type) {
                    String webURL = bundle.getString("webURL");
                    return new Result(environment, response_type,
                            new JSONObject().put("webURL", webURL), null); // email not sent back in checkout requests since Hermes doesn't return that info
                } else {
                    String authorization_code = bundle.getString("authorization_code");
                    String email = bundle.getString("email");
                    return new Result(environment, response_type,
                            new JSONObject().put("code", authorization_code), email);
                }
            } catch (JSONException e) {
                return new Result(new ResponseParsingException(e));
            }
        }
    }
}
