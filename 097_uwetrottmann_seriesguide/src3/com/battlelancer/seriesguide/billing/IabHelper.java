/* Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.battlelancer.seriesguide.billing;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.android.vending.billing.IInAppBillingService;
import com.battlelancer.seriesguide.BuildConfig;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import timber.log.Timber;

/**
 * Provides convenience methods for in-app billing. You can create one instance of this class for
 * your application and use it to process in-app billing operations. It provides synchronous
 * (blocking) and asynchronous (non-blocking) methods for many common in-app billing operations, as
 * well as automatic signature verification.
 *
 * <p>After instantiating, you must perform setup in order to start using the object. To perform
 * setup, call the {@link #startSetup} method and provide a listener; that listener will be notified
 * when setup is complete, after which (and not before) you may call other methods.
 *
 * <p>After setup is complete, you will typically want to request an inventory of owned items and
 * subscriptions. See {@link #queryInventory}, {@link #queryInventoryAsync} and related methods.
 *
 * <p>When you are done with this object, don't forget to call {@link #dispose} to ensure proper
 * cleanup. This object holds a binding to the in-app billing service, which will leak unless you
 * dispose of it correctly. If you created the object on an Activity's onCreate method, then the
 * recommended place to dispose of it is the Activity's onDestroy method.
 *
 * <p>A note about threading: When using this object from a background thread, you may call the
 * blocking versions of methods; when using from a UI thread, call only the asynchronous versions
 * and handle the results via callbacks. Also, notice that you can only call one asynchronous
 * operation at a time; attempting to start a second asynchronous operation while the first one has
 * not yet completed will result in an exception being thrown.
 *
 * <p>http://developer.android.com/google/play/billing/billing_reference.html
 */
public class IabHelper {

    // Billing response codes
    /** Success. */
    public static final int BILLING_RESPONSE_RESULT_OK = 0;
    /** User pressed back or canceled a dialog. */
    public static final int BILLING_RESPONSE_RESULT_USER_CANCELED = 1;
    /** Network connection is down. */
    public static final int BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE = 2;
    /** Billing API version is not supported for the type requested. */
    public static final int BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3;
    /** Requested product is not available for purchase. */
    public static final int BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4;
    /**
     * Invalid arguments provided to the API. This error can also indicate that the application was
     * not correctly signed or properly set up for In-app Billing in Google Play, or does not have
     * the necessary permissions in its manifest.
     */
    public static final int BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5;
    /** Fatal error during the API action. */
    public static final int BILLING_RESPONSE_RESULT_ERROR = 6;
    /** Failure to purchase since item is already owned. */
    public static final int BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7;
    /** Failure to consume since item is not owned. */
    public static final int BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = 8;

    // IAB Helper error codes
    public static final int IABHELPER_ERROR_BASE = -1000;
    public static final int IABHELPER_REMOTE_EXCEPTION = -1001;
    public static final int IABHELPER_BAD_RESPONSE = -1002;
    public static final int IABHELPER_VERIFICATION_FAILED = -1003;
    public static final int IABHELPER_SEND_INTENT_FAILED = -1004;
    public static final int IABHELPER_USER_CANCELLED = -1005;
    public static final int IABHELPER_UNKNOWN_PURCHASE_RESPONSE = -1006;
    // used with consumables
//    public static final int IABHELPER_MISSING_TOKEN = -1007;
    public static final int IABHELPER_UNKNOWN_ERROR = -1008;
    public static final int IABHELPER_SUBSCRIPTIONS_NOT_AVAILABLE = -1009;
    // used with consumables
//    public static final int IABHELPER_INVALID_CONSUMPTION = -1010;

    // Keys for the responses from InAppBillingService
    public static final String RESPONSE_CODE = "RESPONSE_CODE";
    public static final String RESPONSE_GET_SKU_DETAILS_LIST = "DETAILS_LIST";
    public static final String RESPONSE_BUY_INTENT = "BUY_INTENT";
    public static final String RESPONSE_INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA";
    public static final String RESPONSE_INAPP_SIGNATURE = "INAPP_DATA_SIGNATURE";
    public static final String RESPONSE_INAPP_ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST";
    public static final String RESPONSE_INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST";
    public static final String RESPONSE_INAPP_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST";
    public static final String INAPP_CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN";

    // Item types
    public static final String ITEM_TYPE_INAPP = "inapp";
    public static final String ITEM_TYPE_SUBS = "subs";

    // some fields on the getSkuDetails response bundle
    public static final String GET_SKU_DETAILS_ITEM_LIST = "ITEM_ID_LIST";
//    public static final String GET_SKU_DETAILS_ITEM_TYPE_LIST = "ITEM_TYPE_LIST";

    @Nullable
    private Context context;
    @Nullable
    private IInAppBillingService billingService;
    @Nullable
    private ServiceConnection billingServiceConnection;

    /** Public key for verifying signature, in base64 encoding. */
    private final String signatureBase64;
    private boolean subscriptionsSupported = false;

    // Is an asynchronous operation in progress?
    // (only one at a time can be in progress)
    private boolean asyncInProgress = false;
    // (for logging/debugging)
    // if asyncInProgress == true, what asynchronous operation is in progress?
    private String asyncOperation = "";

    /** The request code used to launch the current purchase flow. */
    private int requestCode;
    /** The item type of the current purchase flow. */
    private String purchasingItemType;

    /**
     * Creates an instance. After creation, it will not yet be ready to use. You must perform setup
     * by calling {@link #startSetup} and wait for setup to complete. This constructor does not
     * block and is safe to call from a UI thread.
     */
    public IabHelper(Context context) {
        this.context = context.getApplicationContext();
        signatureBase64 = BuildConfig.IAP_KEY_A + BuildConfig.IAP_KEY_B
                + BuildConfig.IAP_KEY_C + BuildConfig.IAP_KEY_D;
        logDebug("IAB helper created.");
    }

    /**
     * Callback for setup process. This listener's {@link #onIabSetupFinished} method is called when
     * the setup process is complete.
     */
    public interface OnIabSetupFinishedListener {
        /**
         * Called to notify that setup is complete.
         *
         * @param result The result of the setup process.
         */
        void onIabSetupFinished(IabResult result);
    }

    /**
     * Tries to bind to the billing service and update the inventory. Disposes itself on completion
     * or any error.
     */
    public void startSetupAndQueryInventory() {
        startSetup(setupResult -> {
            if (!setupResult.isSuccess()) {
                // do not care about failure, will try again next time
                dispose();
                return;
            }

            // only query for owned items, we do not care about sku details
            Timber.d("onIabSetupFinished: Successful. Querying inventory.");
            queryInventoryAsync(false, null, (queryResult, inv) -> {
                if (queryResult.isFailure()) {
                    // do not care about failure, will try again next time
                    dispose();
                    return;
                }

                if (context == null) {
                    return;
                }
                Timber.d("onQueryInventoryFinished: Successful. Updating inventory.");
                BillingActivity.checkForSubscription(context, inv);
                dispose();
            });
        });
    }

    /**
     * Starts the setup process. This will start up the setup process asynchronously. You will be
     * notified through the listener when the setup process is complete. This method is safe to call
     * from a UI thread.
     *
     * @param listener The listener to notify when the setup process is complete.
     */
    public void startSetup(@NonNull final OnIabSetupFinishedListener listener) {
        if (context == null) {
            // helper should not have been disposed of already
            throwDisposed();
        }
        if (billingService != null || billingServiceConnection != null) {
            throw new IllegalStateException("IAB helper is already set up.");
        }

        // check if Google IAB service is available
        logDebug("Starting in-app billing setup.");
        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        List<ResolveInfo> resolveInfos = context.getPackageManager()
                .queryIntentServices(serviceIntent, 0);
        if (resolveInfos == null || resolveInfos.isEmpty()) {
            // billing service not available
            listener.onIabSetupFinished(new IabResult(BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE,
                    "Billing service unavailable on device."));
            return;
        }

        billingServiceConnection = buildServiceConnection(listener);
        context.bindService(serviceIntent, billingServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @NonNull
    private ServiceConnection buildServiceConnection(
            @NonNull final OnIabSetupFinishedListener listener) {
        return new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                logDebug("Billing service disconnected.");
                billingService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if (context == null) {
                    // the helper was disposed of already, no need to continue setup
                    return;
                }
                logDebug("Billing service connected.");
                billingService = IInAppBillingService.Stub.asInterface(service);

                logDebug("Checking for in-app billing 3 support.");
                subscriptionsSupported = false;
                String packageName = context.getPackageName();
                try {
                    //noinspection ConstantConditions
                    int response = billingService.isBillingSupported(3, packageName,
                            ITEM_TYPE_INAPP);
                    if (response != BILLING_RESPONSE_RESULT_OK) {
                        listener.onIabSetupFinished(new IabResult(response,
                                "Error checking for billing v3 support."));
                        logDebug("In-app billing v3 NOT AVAILABLE for " + packageName);
                        return;
                    }
                    logDebug("In-app billing v3 AVAILABLE for " + packageName);

                    // check if v3 subscriptions are supported
                    response = billingService.isBillingSupported(3, packageName, ITEM_TYPE_SUBS);
                    if (response == BILLING_RESPONSE_RESULT_OK) {
                        logDebug("Subscriptions AVAILABLE.");
                        subscriptionsSupported = true;
                    } else {
                        logDebug("Subscriptions NOT AVAILABLE. Response: " + response);
                    }
                } catch (RemoteException e) {
                    // the service has crashed before we could even do anything with it;
                    // we can count on soon being disconnected (and then reconnected if it can be
                    // restarted)
                    Timber.e(e, "onServiceConnected: checking billing support failed.");
                    listener.onIabSetupFinished(new IabResult(IABHELPER_REMOTE_EXCEPTION,
                            "RemoteException while setting up in-app billing."));
                    return;
                }

                // success, in-app billing is possible
                listener.onIabSetupFinished(new IabResult(BILLING_RESPONSE_RESULT_OK,
                        "Setup successful."));
            }
        };
    }

    /**
     * Dispose of object, releasing resources. It's very important to call this method when you are
     * done with this object. It will release any resources used by it such as service connections.
     * Naturally, once the object is disposed of, it can't be used again.
     */
    public void dispose() {
        logDebug("Disposing.");
        if (billingServiceConnection != null) {
            logDebug("Unbinding from service.");
            if (context != null) {
                context.unbindService(billingServiceConnection);
            }
            billingServiceConnection = null;
        }
        context = null;
        billingService = null;
        purchaseListener = null;
    }

    private void throwDisposed() {
        throw new IllegalStateException("IabHelper was disposed of, so it cannot be used.");
    }

    /**
     * Callback that notifies when a purchase is finished.
     */
    public interface OnIabPurchaseFinishedListener {
        /**
         * Called to notify that an in-app purchase finished. If the purchase was successful, then
         * the sku parameter specifies which item was purchased. If the purchase failed, the sku and
         * extraData parameters may or may not be null, depending on how far the purchase process
         * went.
         *
         * @param result The result of the purchase.
         * @param info The purchase information (null if purchase failed)
         */
        void onIabPurchaseFinished(IabResult result, Purchase info);
    }

    // The listener registered on launchPurchaseFlow, which we have to call back when
    // the purchase finishes
    @Nullable
    OnIabPurchaseFinishedListener purchaseListener;

// CURRENTLY NOT ALLOWING NEW IN-APP PURCHASES
//    public void launchPurchaseFlow(Activity act, String sku, int requestCode,
//            OnIabPurchaseFinishedListener listener, String extraData) {
//        launchPurchaseFlow(act, sku, ITEM_TYPE_INAPP, requestCode, listener, extraData);
//    }

    public void launchSubscriptionPurchaseFlow(Activity act, String sku, int requestCode,
            OnIabPurchaseFinishedListener listener, String extraData) {
        launchPurchaseFlow(act, sku, ITEM_TYPE_SUBS, requestCode, listener, extraData);
    }

    /**
     * Initiate the UI flow for an in-app purchase. Call this method to initiate an in-app purchase,
     * which will involve bringing up the Google Play screen. The calling activity will be paused
     * while the user interacts with Google Play, and the result will be delivered via the
     * activity's {@link android.app.Activity#onActivityResult} method, at which point you must call
     * this object's {@link #handleActivityResult} method to continue the purchase flow. This method
     * MUST be called from the UI thread of the Activity.
     *
     * @param activity The calling activity.
     * @param sku The sku of the item to purchase.
     * @param itemType indicates if it's a product or a subscription (ITEM_TYPE_INAPP or
     * ITEM_TYPE_SUBS)
     * @param requestCode A request code (to differentiate from other responses -- as in {@link
     * android.app.Activity#startActivityForResult}).
     * @param listener The listener to notify when the purchase process finishes
     * @param extraData Extra data (developer payload), which will be returned with the purchase
     * data when the purchase completes. This extra data will be permanently bound to that purchase
     * and will always be returned when the purchase is queried.
     */
    public void launchPurchaseFlow(@NonNull Activity activity, @NonNull String sku,
            @NonNull String itemType, int requestCode, OnIabPurchaseFinishedListener listener,
            @NonNull String extraData) {
        if (context == null || billingService == null) {
            warnNotSetup();
            if (listener != null) {
                listener.onIabPurchaseFinished(
                        new IabResult(BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE,
                                "Billing service not set up."), null);
            }
            return;
        }
        flagStartAsync("launchPurchaseFlow");
        IabResult result;

        if (itemType.equals(ITEM_TYPE_SUBS) && !subscriptionsSupported) {
            IabResult r = new IabResult(IABHELPER_SUBSCRIPTIONS_NOT_AVAILABLE,
                    "Subscriptions are not available.");
            flagEndAsync();
            if (listener != null) {
                listener.onIabPurchaseFinished(r, null);
            }
            return;
        }

        try {
            logDebug("Constructing buy intent for " + sku + ", item type: " + itemType);
            Bundle buyIntentBundle = billingService.getBuyIntent(3, context.getPackageName(), sku,
                    itemType, extraData);
            int response = getResponseCodeFromBundle(buyIntentBundle);
            if (response != BILLING_RESPONSE_RESULT_OK) {
                logError("Unable to buy item, Error response: " + getResponseDesc(response));
                flagEndAsync();
                result = new IabResult(response, "Unable to buy item");
                if (listener != null) {
                    listener.onIabPurchaseFinished(result, null);
                }
                return;
            }

            PendingIntent pendingIntent = buyIntentBundle.getParcelable(RESPONSE_BUY_INTENT);
            if (pendingIntent == null) {
                logError("Unable to buy item, buy intent is null.");
                flagEndAsync();
                if (listener != null) {
                    listener.onIabPurchaseFinished(
                            new IabResult(IABHELPER_SEND_INTENT_FAILED, "Buy intent is null."),
                            null);
                }
                return;
            }
            logDebug("Launching buy intent for " + sku + ". Request code: " + requestCode);
            this.requestCode = requestCode;
            purchaseListener = listener;
            purchasingItemType = itemType;
            activity.startIntentSenderForResult(pendingIntent.getIntentSender(), requestCode,
                    new Intent(), 0, 0, 0);
        } catch (SendIntentException e) {
            logError(e, "launchPurchaseFlow: failed for sku " + sku);
            flagEndAsync();

            result = new IabResult(IABHELPER_SEND_INTENT_FAILED, "Failed to send intent.");
            if (listener != null) {
                listener.onIabPurchaseFinished(result, null);
            }
        } catch (RemoteException e) {
            logError(e, "launchPurchaseFlow: failed for sku " + sku);
            flagEndAsync();

            result = new IabResult(IABHELPER_REMOTE_EXCEPTION,
                    "Remote exception while starting purchase flow");
            if (listener != null) {
                listener.onIabPurchaseFinished(result, null);
            }
        }
    }

    /**
     * Handles an activity result that's part of the purchase flow in in-app billing. If you are
     * calling {@link #launchPurchaseFlow}, then you must call this method from your Activity's
     * {@link android.app.Activity@onActivityResult} method. This method MUST be called from the UI
     * thread of the Activity.
     *
     * @param requestCode The requestCode as you received it.
     * @param resultCode The resultCode as you received it.
     * @param data The data (Intent) as you received it.
     * @return Returns true if the result was related to a purchase flow and was handled; false if
     * the result was not related to a purchase, in which case you should handle it normally.
     */
    public boolean handleActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IabResult result;
        if (requestCode != this.requestCode) {
            return false;
        }

        if (purchaseListener == null) {
            // not set up or helper was disposed of, so listener is gone
            return false;
        }

        // end of async purchase operation that started on launchPurchaseFlow
        flagEndAsync();

        if (data == null) {
            logError("Null data in IAB activity result.");
            result = new IabResult(IABHELPER_BAD_RESPONSE, "Null data in IAB result");
            if (purchaseListener != null) {
                purchaseListener.onIabPurchaseFinished(result, null);
            }
            return true;
        }

        int responseCode = getResponseCodeFromIntent(data);
        if (resultCode == Activity.RESULT_OK && responseCode == BILLING_RESPONSE_RESULT_OK) {
            logDebug("Successful resultcode from purchase activity.");

            String purchaseData = data.getStringExtra(RESPONSE_INAPP_PURCHASE_DATA);
            String dataSignature = data.getStringExtra(RESPONSE_INAPP_SIGNATURE);
            logDebug("Purchase data: " + purchaseData);
            logDebug("Data signature: " + dataSignature);
            logDebug("Extras: " + data.getExtras());
            logDebug("Expected item type: " + purchasingItemType);

            if (purchaseData == null || dataSignature == null) {
                logError("BUG: either purchaseData or dataSignature is null.");
                logDebug("Extras: " + data.getExtras().toString());
                result = new IabResult(IABHELPER_UNKNOWN_ERROR,
                        "IAB returned null purchaseData or dataSignature");
                if (purchaseListener != null) {
                    purchaseListener.onIabPurchaseFinished(result, null);
                }
                return true;
            }

            Purchase purchase;
            try {
                purchase = new Purchase(purchasingItemType, purchaseData, dataSignature);
                String sku = purchase.getSku();

                // Verify signature
                if (Security.verifyPurchase(signatureBase64, purchaseData, dataSignature)) {
                    logDebug("Purchase signature successfully verified.");
                    result = new IabResult(BILLING_RESPONSE_RESULT_OK, "Success");
                } else {
                    logError("Purchase signature verification FAILED for sku " + sku);
                    result = new IabResult(IABHELPER_VERIFICATION_FAILED,
                            "Signature verification failed for sku " + sku);
                }
            } catch (JSONException e) {
                logError(e, "Failed to parse purchase data.");
                result = new IabResult(IABHELPER_BAD_RESPONSE, "Failed to parse purchase data.");
                purchase = null;
            }

            if (purchaseListener != null) {
                purchaseListener.onIabPurchaseFinished(result, purchase);
            }
        } else {
            if (resultCode == Activity.RESULT_OK) {
                // result code was OK, but in-app billing response was not OK.
                logDebug(
                        "Result code was OK but in-app billing response was not OK: "
                                + getResponseDesc(
                                responseCode));
                result = new IabResult(responseCode, "Problem purchasing item.");
            } else if (resultCode == Activity.RESULT_CANCELED) {
                logDebug("Purchase canceled - Response: " + getResponseDesc(responseCode));
                result = new IabResult(IABHELPER_USER_CANCELLED, "User canceled.");
            } else {
                logError("Purchase failed. Result code: " + Integer.toString(resultCode)
                        + ". Response: " + getResponseDesc(responseCode));
                result = new IabResult(IABHELPER_UNKNOWN_PURCHASE_RESPONSE,
                        "Unknown purchase response.");
            }
            if (purchaseListener != null) {
                purchaseListener.onIabPurchaseFinished(result, null);
            }
        }
        return true;
    }

    /**
     * Queries the inventory. This will query all owned items from the server, as well as
     * information on additional skus, if specified. This method may block or take long to execute.
     * Do not call from a UI thread. For that, use the non-blocking version {@link
     * #queryInventoryAsync}.
     *
     * @param querySkuDetails if true, SKU details (price, description, etc) will be queried as well
     * as purchase information.
     * @param moreItemSkus additional PRODUCT skus to query information on, regardless of ownership.
     * Ignored if null or if querySkuDetails is false.
     * @throws IabException if a problem occurs while refreshing the inventory.
     */
    private static Inventory queryInventory(@NonNull IInAppBillingService service,
            String packageName, String signatureBase64, boolean subscriptionsSupported,
            boolean querySkuDetails, List<String> moreItemSkus)
            throws IabException {
        try {
            Inventory inv = new Inventory();
            int r = queryPurchases(service, packageName, signatureBase64, inv, ITEM_TYPE_INAPP);
            if (r != BILLING_RESPONSE_RESULT_OK) {
                throw new IabException(r, "Error refreshing inventory (querying owned items).");
            }

            if (querySkuDetails) {
                r = querySkuDetails(service, packageName, ITEM_TYPE_INAPP, inv, moreItemSkus);
                if (r != BILLING_RESPONSE_RESULT_OK) {
                    throw new IabException(r,
                            "Error refreshing inventory (querying prices of items).");
                }
            }

            // if subscriptions are supported, then also query for subscriptions
            if (subscriptionsSupported) {
                r = queryPurchases(service, packageName, signatureBase64, inv, ITEM_TYPE_SUBS);
                if (r != BILLING_RESPONSE_RESULT_OK) {
                    throw new IabException(r,
                            "Error refreshing inventory (querying owned subscriptions).");
                }

                if (querySkuDetails) {
                    r = querySkuDetails(service, packageName, ITEM_TYPE_SUBS, inv, moreItemSkus);
                    if (r != BILLING_RESPONSE_RESULT_OK) {
                        throw new IabException(r,
                                "Error refreshing inventory (querying prices of subscriptions).");
                    }
                }
            }

            return inv;
        } catch (RemoteException e) {
            throw new IabException(IABHELPER_REMOTE_EXCEPTION,
                    "Remote exception while refreshing inventory.", e);
        } catch (JSONException e) {
            throw new IabException(IABHELPER_BAD_RESPONSE,
                    "Error parsing JSON response while refreshing inventory.", e);
        }
    }

    /**
     * Listener that notifies when an inventory query operation completes.
     */
    public interface QueryInventoryFinishedListener {
        /**
         * Called to notify that an inventory query operation completed.
         *
         * @param result The result of the operation.
         * @param inv The inventory. Never null if the result is success.
         */
        void onQueryInventoryFinished(IabResult result, Inventory inv);
    }

    /**
     * Asynchronous wrapper for inventory query. This will perform an inventory query as described
     * in {@link #queryInventory}, but will do so asynchronously and call back the specified
     * listener upon completion. This method is safe to call from a UI thread.
     *
     * <p> Ensure helper is not disposed and setup, or will do nothing.
     *
     * @param querySkuDetails as in {@link #queryInventory}
     * @param moreSkus as in {@link #queryInventory}
     * @param listener The listener to notify when the refresh operation completes.
     */
    public void queryInventoryAsync(final boolean querySkuDetails, final List<String> moreSkus,
            final QueryInventoryFinishedListener listener) {
        if (context == null || billingService == null) {
            // avoid creating a thread by doing checks here as well
            warnQueryInventorySkipped(listener);
            return;
        }

        final Handler handler = new Handler();
        flagStartAsync("refresh inventory");
        (new Thread(() -> {
            // keep own references as IabHelper might be disposed in different thread
            Context context = IabHelper.this.context;
            IInAppBillingService billingService = IabHelper.this.billingService;
            if (context == null || billingService == null) {
                flagEndAsync();
                warnQueryInventorySkipped(listener);
                return;
            }

            IabResult result = new IabResult(BILLING_RESPONSE_RESULT_OK,
                    "Inventory refresh successful.");
            Inventory inv = null;
            try {
                inv = queryInventory(billingService, context.getPackageName(), signatureBase64,
                        subscriptionsSupported, querySkuDetails, moreSkus);
            } catch (IabException e) {
                Timber.e(e, "queryInventoryAsync: failed.");
                result = e.getResult();
            }

            flagEndAsync();

            final IabResult result_f = result;
            final Inventory inv_f = inv;
            if (listener != null) {
                handler.post(() -> listener.onQueryInventoryFinished(result_f, inv_f));
            }
        })).start();
    }

    /**
     * Returns a human-readable description for the given response code.
     *
     * @param code The response code
     * @return A human-readable string explaining the result code. It also includes the result code
     * numerically.
     */
    public static String getResponseDesc(int code) {
        String[] iab_msgs = ("0:OK/1:User Canceled/2:Unknown/" +
                "3:Billing Unavailable/4:Item unavailable/" +
                "5:Developer Error/6:Error/7:Item Already Owned/" +
                "8:Item not owned").split("/");
        String[] iabhelper_msgs = ("0:OK/-1001:Remote exception during initialization/" +
                "-1002:Bad response received/" +
                "-1003:Purchase signature verification failed/" +
                "-1004:Send intent failed/" +
                "-1005:User cancelled/" +
                "-1006:Unknown purchase response/" +
                "-1007:Missing token/" +
                "-1008:Unknown error/" +
                "-1009:Subscriptions not available/" +
                "-1010:Invalid consumption attempt").split("/");

        if (code <= IABHELPER_ERROR_BASE) {
            int index = IABHELPER_ERROR_BASE - code;
            if (index >= 0 && index < iabhelper_msgs.length) {
                return iabhelper_msgs[index];
            } else {
                return String.valueOf(code) + ":Unknown IAB Helper Error";
            }
        } else if (code < 0 || code >= iab_msgs.length) {
            return String.valueOf(code) + ":Unknown";
        } else {
            return iab_msgs[code];
        }
    }

    // Workaround to bug where sometimes response codes come as Long instead of Integer
    private static int getResponseCodeFromBundle(Bundle b) {
        Object o = b.get(RESPONSE_CODE);
        if (o == null) {
            logDebug("Bundle with null response code, assuming OK (known issue)");
            return BILLING_RESPONSE_RESULT_OK;
        } else if (o instanceof Integer) {
            return (Integer) o;
        } else if (o instanceof Long) {
            return (int) ((Long) o).longValue();
        } else {
            logError("Unexpected type for bundle response code.");
            logError(o.getClass().getName());
            throw new RuntimeException(
                    "Unexpected type for bundle response code: " + o.getClass().getName());
        }
    }

    // Workaround to bug where sometimes response codes come as Long instead of Integer
    int getResponseCodeFromIntent(Intent i) {
        Object o = i.getExtras().get(RESPONSE_CODE);
        if (o == null) {
            logError("Intent with no response code, assuming OK (known issue)");
            return BILLING_RESPONSE_RESULT_OK;
        } else if (o instanceof Integer) {
            return (Integer) o;
        } else if (o instanceof Long) {
            return (int) ((Long) o).longValue();
        } else {
            logError("Unexpected type for intent response code.");
            logError(o.getClass().getName());
            throw new RuntimeException(
                    "Unexpected type for intent response code: " + o.getClass().getName());
        }
    }

    private static int queryPurchases(@NonNull IInAppBillingService service,
            @NonNull String packageName, @NonNull String signatureBase64, @NonNull Inventory inv,
            @NonNull String itemType)
            throws JSONException, RemoteException {
        logDebug("Querying owned items, item type: " + itemType);
        logDebug("Package name: " + packageName);
        boolean verificationFailed = false;
        String continueToken = null;

        do {
            logDebug("Calling getPurchases with continuation token: " + continueToken);
            Bundle ownedItems = service.getPurchases(3, packageName, itemType, continueToken);

            int response = getResponseCodeFromBundle(ownedItems);
            logDebug("Owned items response: " + String.valueOf(response));
            if (response != BILLING_RESPONSE_RESULT_OK) {
                logDebug("getPurchases() failed: " + getResponseDesc(response));
                return response;
            }
            if (!ownedItems.containsKey(RESPONSE_INAPP_ITEM_LIST)
                    || !ownedItems.containsKey(RESPONSE_INAPP_PURCHASE_DATA_LIST)
                    || !ownedItems.containsKey(RESPONSE_INAPP_SIGNATURE_LIST)) {
                logError("Bundle returned from getPurchases() doesn't contain required fields.");
                return IABHELPER_BAD_RESPONSE;
            }

            ArrayList<String> ownedSkus = ownedItems.getStringArrayList(
                    RESPONSE_INAPP_ITEM_LIST);
            ArrayList<String> purchaseDataList = ownedItems.getStringArrayList(
                    RESPONSE_INAPP_PURCHASE_DATA_LIST);
            ArrayList<String> signatureList = ownedItems.getStringArrayList(
                    RESPONSE_INAPP_SIGNATURE_LIST);
            if (ownedSkus == null || purchaseDataList == null || signatureList == null) {
                logError("Fields returned in bundle from getPurchases() not in expected format.");
                return IABHELPER_BAD_RESPONSE;
            }

            for (int i = 0; i < purchaseDataList.size(); ++i) {
                String purchaseData = purchaseDataList.get(i);
                String signature = signatureList.get(i);
                String sku = ownedSkus.get(i);
                if (Security.verifyPurchase(signatureBase64, purchaseData, signature)) {
                    logDebug("Sku is owned: " + sku);
                    Purchase purchase = new Purchase(itemType, purchaseData, signature);

                    if (TextUtils.isEmpty(purchase.getToken())) {
                        logWarn("BUG: empty/null token!");
                        logDebug("Purchase data: " + purchaseData);
                    }

                    // Record ownership and token
                    inv.addPurchase(purchase);
                } else {
                    logWarn("Purchase signature verification **FAILED**. Not adding item.");
                    logDebug("   Purchase data: " + purchaseData);
                    logDebug("   Signature: " + signature);
                    verificationFailed = true;
                }
            }

            continueToken = ownedItems.getString(INAPP_CONTINUATION_TOKEN);
            logDebug("Continuation token: " + continueToken);
        } while (!TextUtils.isEmpty(continueToken));

        return verificationFailed ? IABHELPER_VERIFICATION_FAILED : BILLING_RESPONSE_RESULT_OK;
    }

    private static int querySkuDetails(@NonNull IInAppBillingService service,
            @NonNull String packageName, @NonNull String itemType, @NonNull Inventory inv,
            @Nullable List<String> moreSkus)
            throws RemoteException, JSONException {
        logDebug("Querying SKU details.");
        ArrayList<String> skuList = new ArrayList<>();
        skuList.addAll(inv.getAllOwnedSkus(itemType));
        if (moreSkus != null) {
            for (String sku : moreSkus) {
                if (!skuList.contains(sku)) {
                    skuList.add(sku);
                }
            }
        }

        if (skuList.size() == 0) {
            logDebug("queryPrices: nothing to do because there are no SKUs.");
            return BILLING_RESPONSE_RESULT_OK;
        }

        Bundle querySkus = new Bundle();
        querySkus.putStringArrayList(GET_SKU_DETAILS_ITEM_LIST, skuList);
        Bundle skuDetails = service.getSkuDetails(3, packageName, itemType, querySkus);

        if (!skuDetails.containsKey(RESPONSE_GET_SKU_DETAILS_LIST)) {
            int response = getResponseCodeFromBundle(skuDetails);
            if (response != BILLING_RESPONSE_RESULT_OK) {
                logDebug("getSkuDetails() failed: " + getResponseDesc(response));
                return response;
            } else {
                logError(
                        "getSkuDetails() returned a bundle with neither an error nor a detail list.");
                return IABHELPER_BAD_RESPONSE;
            }
        }

        ArrayList<String> responseList = skuDetails.getStringArrayList(
                RESPONSE_GET_SKU_DETAILS_LIST);
        if (responseList != null) {
            for (String thisResponse : responseList) {
                SkuDetails d = new SkuDetails(itemType, thisResponse);
                logDebug("Got sku details: " + d);
                inv.addSkuDetails(d);
            }
        }

        return BILLING_RESPONSE_RESULT_OK;
    }

    private void flagStartAsync(String operation) {
        if (asyncInProgress) {
            throw new IllegalStateException("Can't start async operation (" +
                    operation + ") because another async operation(" + asyncOperation
                    + ") is in progress.");
        }
        asyncOperation = operation;
        asyncInProgress = true;
        logDebug("Starting async operation: " + operation);
    }

    private void flagEndAsync() {
        logDebug("Ending async operation: " + asyncOperation);
        asyncOperation = "";
        asyncInProgress = false;
    }

    private void warnNotSetup() {
        if (context == null) {
            logWarn("queryInventoryAsync: failed, helper was disposed of.");
        }
        if (billingService == null) {
            logWarn("queryInventoryAsync: failed, service not set up or disconnected.");
        }
    }

    private void warnQueryInventorySkipped(QueryInventoryFinishedListener listener) {
        warnNotSetup();
        if (listener != null) {
            listener.onQueryInventoryFinished(new IabResult(BILLING_RESPONSE_RESULT_ERROR,
                    "Inventory refresh skipped, not set up."), null);
        }
    }

    static void logDebug(String msg) {
        Timber.d(msg);
    }

    static void logError(String msg) {
        Timber.e("In-app billing error: %s", msg);
    }

    static void logError(Throwable e, String msg) {
        Timber.e(e, "In-app billing error: %s", msg);
    }

    static void logWarn(String msg) {
        Timber.w("In-app billing warning: %s", msg);
    }
}
