package com.paypal.android.sdk.onetouch.core.config;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.braintreepayments.api.internal.AppHelper;
import com.braintreepayments.browserswitch.ChromeCustomTabs;
import com.paypal.android.sdk.onetouch.core.enums.Protocol;
import com.paypal.android.sdk.onetouch.core.enums.RequestTarget;
import com.paypal.android.sdk.onetouch.core.sdk.AppSwitchHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public abstract class Recipe<T extends Recipe<T>> {

    private List<String> mTargetPackagesInReversePriorityOrder;
    private RequestTarget mTarget;
    private Protocol mProtocol;
    private String mTargetIntentAction;
    private Collection<String> mSupportedLocales;

    public Recipe() {
        mTargetPackagesInReversePriorityOrder = new ArrayList<>();
        mSupportedLocales = new HashSet<>();
    }

    public T target(RequestTarget target) {
        mTarget = target;
        return getThis();
    }

    public T protocol(String protocol) {
        mProtocol = Protocol.getProtocol(protocol);
        return getThis();
    }

    public T targetPackage(String singleTargetPackage) {
        mTargetPackagesInReversePriorityOrder.add(singleTargetPackage);
        return getThis();
    }

    public List<String> getTargetPackagesInReversePriorityOrder() {
        return new ArrayList<>(mTargetPackagesInReversePriorityOrder);
    }

    public T supportedLocale(String supportedLocale) {
        mSupportedLocales.add(supportedLocale);
        return getThis();
    }

    public T targetIntentAction(String targetIntentAction) {
        mTargetIntentAction = targetIntentAction;
        return getThis();
    }

    public String getTargetIntentAction() {
        return mTargetIntentAction;
    }

    public RequestTarget getTarget() {
        return mTarget;
    }

    protected abstract T getThis();

    public boolean isValidAppTarget(Context context) {
        // Only support wallet app switch if package name is a match
        String packageName = context.getApplicationContext().getPackageName();
        if (!packageName.equals(packageName.toLowerCase(Locale.ROOT).replace("_", ""))) {
            return false;
        }

        for (String allowedWalletTarget : getTargetPackagesInReversePriorityOrder()) {
            boolean isIntentAvailable = AppHelper.isIntentAvailable(context,
                    AppSwitchHelper.createBaseIntent(getTargetIntentAction(), allowedWalletTarget));

            String locale = Locale.getDefault().toString();
            // if no locales are specified, then presumed to be allowed for all
            boolean isLocaleAllowed =
                    mSupportedLocales.isEmpty() || mSupportedLocales.contains(locale);

            boolean isSignatureValid = AppSwitchHelper.isSignatureValid(context, allowedWalletTarget);

            if (isIntentAvailable && isLocaleAllowed && isSignatureValid) {
                return true;
            }
        }

        return false;
    }

    public boolean isValidBrowserTarget(Context context, String browserSwitchUrl) {
        for (String allowedBrowserPackage : getTargetPackagesInReversePriorityOrder()) {
            boolean canBeResolved =
                    isValidBrowserTarget(context, browserSwitchUrl, allowedBrowserPackage);
            if (canBeResolved) {
                return true;
            }
        }

        return false;
    }

    public static boolean isValidBrowserTarget(Context context, String browserSwitchUrl, String allowedBrowserPackage) {
        Intent intent = getBrowserIntent(context, browserSwitchUrl, allowedBrowserPackage);
        return (intent.resolveActivity(context.getPackageManager()) != null);
    }

    public static Intent getBrowserIntent(Context context, String browserSwitchUrl, String allowedBrowserPackage) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(browserSwitchUrl))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (!"*".equals(allowedBrowserPackage)) {
            intent.setPackage(allowedBrowserPackage);
        }

        return ChromeCustomTabs.addChromeCustomTabsExtras(context, intent);
    }

    public Protocol getProtocol() {
        return mProtocol;
    }
}
