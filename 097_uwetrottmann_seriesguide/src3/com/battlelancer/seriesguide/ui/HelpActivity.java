
package com.battlelancer.seriesguide.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.ActionBar;
import com.battlelancer.seriesguide.R;
import com.battlelancer.seriesguide.util.Utils;
import java.util.Locale;

/**
 * Displays the SeriesGuide online help page.
 */
public class HelpActivity extends BaseActivity {

    private static final String SUPPORT_MAIL = "support@seriesgui.de";

    public static Intent getFeedbackEmailIntent(Context context) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {SUPPORT_MAIL});
        // include app version in subject
        intent.putExtra(Intent.EXTRA_SUBJECT,
                "SeriesGuide " + Utils.getVersion(context) + " Feedback");
        // and hardware and Android info in body
        intent.putExtra(Intent.EXTRA_TEXT,
                Build.MANUFACTURER.toUpperCase(Locale.US) + " " + Build.MODEL + ", Android "
                        + Build.VERSION.RELEASE + "\n\n");

        return Intent.createChooser(intent, context.getString(R.string.feedback));
    }

    private WebView webview;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_webview);
        setupActionBar();

        webview = findViewById(R.id.webView);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebViewClient(webViewClient);
        webview.loadUrl(getString(R.string.help_url));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /*
          Force the text-to-speech accessibility Javascript plug-in service on Android 4.2.2 to
          get shutdown, to avoid leaking its context.

          http://stackoverflow.com/a/18798305/1000543
         */
        if (webview != null) {
            webview.getSettings().setJavaScriptEnabled(false);
            webview = null;
        }
    }

    private WebViewClient webViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url != null && !url.startsWith(getString(R.string.help_url))) {
                // launch browser when leaving help page
                Utils.launchWebsite(view.getContext(), url);
                return true;
            }
            return false;
        }
    };

    @Override
    protected void setupActionBar() {
        super.setupActionBar();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.help_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (itemId == R.id.menu_action_help_open_browser) {
            openInBrowser();
            return true;
        }
        if (itemId == R.id.menu_action_help_send_feedback) {
            createFeedbackEmail();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openInBrowser() {
        Utils.launchWebsite(this, getString(R.string.help_url));
    }

    private void createFeedbackEmail() {
        Utils.tryStartActivity(this, getFeedbackEmailIntent(this), true);
    }
}
