package com.battlelancer.seriesguide.billing.amazon;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.Product;
import com.amazon.device.iap.model.RequestId;
import com.battlelancer.seriesguide.R;
import com.battlelancer.seriesguide.ui.BaseActivity;
import com.battlelancer.seriesguide.util.Utils;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import timber.log.Timber;

public class AmazonBillingActivity extends BaseActivity {

    @BindView(R.id.progressBarAmazonBilling) View progressBar;
    @BindView(R.id.buttonAmazonBillingSubscribe) Button buttonSubscribe;
    @BindView(R.id.textViewAmazonBillingSubPrice) TextView textViewPriceSub;
    @BindView(R.id.buttonAmazonBillingGetPass) Button buttonGetPass;
    @BindView(R.id.textViewAmazonBillingPricePass) TextView textViewPricePass;
    @BindView(R.id.textViewAmazonBillingExisting) TextView textViewIsSupporter;
    @BindView(R.id.buttonPositive) Button dismissButton;
    @BindView(R.id.buttonNegative) Button hiddenButton;
    @BindView(R.id.textViewAmazonBillingMoreInfo) View buttonMoreInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amazon_billing);
        setupActionBar();

        setupViews();

        AmazonIapManager.setup(this);
    }

    @Override
    protected void setupActionBar() {
        super.setupActionBar();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupViews() {
        ButterKnife.bind(this);

        buttonSubscribe.setEnabled(false);
        buttonSubscribe.setOnClickListener(v -> subscribe());

        buttonGetPass.setEnabled(false);
        buttonGetPass.setOnClickListener(v -> purchasePass());

        dismissButton.setText(R.string.dismiss);
        dismissButton.setOnClickListener(v -> dismiss());
        hiddenButton.setVisibility(View.GONE);

        buttonMoreInfo.setOnClickListener(
                v -> Utils.launchWebsite(v.getContext(), getString(R.string.url_whypay)));

        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // no need to get product data every time we were hidden, so do it in onStart
        AmazonIapManager.get().requestProductData();
    }

    @Override
    protected void onResume() {
        super.onResume();

        AmazonIapManager.get().activate();
        AmazonIapManager.get().requestUserDataAndPurchaseUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();

        AmazonIapManager.get().deactivate();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return false;
    }

    private void subscribe() {
        final RequestId requestId = PurchasingService.purchase(
                AmazonSku.SERIESGUIDE_SUB_YEARLY.getSku());
        Timber.d("subscribe: requestId (%s)", requestId);
    }

    private void purchasePass() {
        final RequestId requestId = PurchasingService.purchase(
                AmazonSku.SERIESGUIDE_PASS.getSku());
        Timber.d("purchasePass: requestId (%s)", requestId);
    }

    private void dismiss() {
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(AmazonIapManager.AmazonIapMessageEvent event) {
        Toast.makeText(this, event.messageResId, Toast.LENGTH_LONG).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(AmazonIapManager.AmazonIapAvailabilityEvent event) {
        progressBar.setVisibility(View.GONE);

        // enable or disable purchase buttons based on what can be purchased
        buttonSubscribe.setEnabled(event.subscriptionAvailable && !event.userHasActivePurchase);
        buttonGetPass.setEnabled(event.passAvailable && !event.userHasActivePurchase);

        // status text
        if (!event.subscriptionAvailable && !event.passAvailable) {
            // neither purchase available, probably not signed in
            textViewIsSupporter.setText(R.string.subscription_not_signed_in);
        } else {
            // subscription or pass available
            // show message if either one is active
            textViewIsSupporter.setText(
                    event.userHasActivePurchase ? getString(R.string.upgrade_success) : null);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(AmazonIapManager.AmazonIapProductEvent event) {
        Product product = event.product;
        // display the actual price like "1.23 C"
        String price = product.getPrice();
        if (price == null) {
            price = "--";
        }
        if (AmazonSku.SERIESGUIDE_SUB_YEARLY.getSku().equals(product.getSku())) {
            textViewPriceSub.setText(
                    getString(R.string.billing_price_subscribe,
                            price, getString(R.string.amazon))
            );
        } else if (AmazonSku.SERIESGUIDE_PASS.getSku().equals(product.getSku())) {
            textViewPricePass.setText(
                    String.format("%s\n%s", price, getString(R.string.billing_price_pass)));
        }
    }
}
