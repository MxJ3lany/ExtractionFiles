/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2014-2018 CiTIUS - University of Santiago de Compostela
 *
 *    Calendula is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.pinlock;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.usc.citius.servando.calendula.CalendulaActivity;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.StartActivity;
import es.usc.citius.servando.calendula.pinlock.fingerprint.FingerprintHelper;
import es.usc.citius.servando.calendula.pinlock.view.IndicatorDotView;
import es.usc.citius.servando.calendula.pinlock.view.NumberPadView;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;

public class PinLockActivity extends CalendulaActivity {

    public static final String EXTRA_NEW_PIN = "PinLockActivity.newpin.result";
    public static final String EXTRA_VERIFY_PIN_RESULT = "PinLockActivity.verify.result";
    public static final String ACTION_NEW_PIN = "PinLockActivity.action.new_pin";
    public static final String ACTION_VERIFY_PIN = "PinLockActivity.action.verify_pin";
    public static final int REQUEST_PIN = 15765;
    public static final int REQUEST_VERIFY = 15766;
    private static final int PIN_SIZE = 4;

    private static final String TAG = "PinLockActivity";


    @BindView(R.id.number_pad)
    NumberPadView numberPad;
    @BindView(R.id.indicator_dots)
    IndicatorDotView indicatorDotView;
    @BindView(R.id.pin_prompt_message)
    TextView promptMessage;
    @BindView(R.id.error_message)
    TextView errorMessage;
    @BindView(R.id.main_progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.footer)
    View footer;
    @BindView(R.id.use_fingerprint_btn)
    Button useFingerprintButton;
    @BindView(R.id.use_fingerprint_separator)
    View useFingerprintSeparator;

    private PinInputStateManager pinInputStateManager;
    private FingerprintHelper fpHelper;
    private MaterialDialog fingerprintDialog;

    @OnClick(R.id.forgot_pin_btn)
    void showResetPinDialog() {
        new MaterialStyledDialog.Builder(this)
                .autoDismiss(false)
                .setTitle(R.string.pin_recovery_title)
                .setDescription(R.string.pin_recovery_description)
                .setHeaderColor(R.color.android_blue)
                .setStyle(Style.HEADER_WITH_ICON)
                .withDialogAnimation(true)
                .setIcon(IconUtils.icon(this, GoogleMaterial.Icon.gmd_key, R.color.white, 100))
                .setPositiveText(R.string.ok)
                .setNegativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        PreferenceUtils.edit()
                                .remove(PreferenceKeys.UNLOCK_PIN_HASH.key())
                                .remove(PreferenceKeys.UNLOCK_PIN_SALT.key())
                                .apply();
                        startActivity(new Intent(PinLockActivity.this, StartActivity.class));
                        finish();
                        dialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @OnClick(R.id.use_fingerprint_btn)
    @RequiresApi(Build.VERSION_CODES.M)
    void launchFingerprintAuth() {
        showFingerprintDialog();
        fpHelper.startAuthentication(new LoginFPCallbackAdapter(this, fingerprintDialog));
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private void showFingerprintDialog() {
        fingerprintDialog = new MaterialDialog.Builder(this)
                .icon(IconUtils.icon(this, CommunityMaterial.Icon.cmd_fingerprint, R.color.android_blue_dark, 48))
                .title(R.string.fingerprint_unlock_dialog_title)
                .cancelable(false)
                .negativeText(R.string.fingerprint_unlock_dialog_use_pin)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        fpHelper.stop();
                    }
                })
                .build();
        fingerprintDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_lock);
        ButterKnife.bind(this);

        if (!isCalledForResult()) {
            throw new IllegalStateException("This activity can only be called for result");
        }

        final String action = getIntent().getAction();
        if (action == null) {
            throw new IllegalArgumentException("Action required!");
        }

        pinInputStateManager = new PinInputStateManager(PIN_SIZE);
        indicatorDotView.setSize(PIN_SIZE);

        numberPad.setListener(new NumberPadView.NumberPadListener() {
            @Override
            public void onNumberClicked(int numberValue) {
                pinInputStateManager.putNumber(numberValue);
            }

            @Override
            public void onDeleteClicked() {
                pinInputStateManager.delete();
            }
        });

        pinInputStateManager.setPinChangeListener(new PinInputStateManager.PinInputChangeListener() {

            @Override
            public void onPinChange(String currentPin, int pinLength) {
                LogUtil.v(TAG, "onPinChange() called with: currentPin = [" + currentPin + "], pinLength = [" + pinLength + "]");
                indicatorDotView.setMarked(pinLength);
            }
        });

        progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE,
                android.graphics.PorterDuff.Mode.MULTIPLY);

        setupStatusBar(ContextCompat.getColor(this, R.color.android_blue_dark));

        switch (action) {
            case ACTION_NEW_PIN:
                pinInputStateManager.setPinCompleteListener(new NewPinListener());
                promptMessage.setText(R.string.text_pinlock_new_prompt);
                setupToolbar(null, ContextCompat.getColor(this, R.color.android_blue_dark));
                break;
            case ACTION_VERIFY_PIN:
                pinInputStateManager.setPinCompleteListener(new AuthorizeAccessListener());
                errorMessage.setVisibility(View.GONE); // indicator shows error
                promptMessage.setText(R.string.text_pinlock_auth_prompt);
                footer.setVisibility(View.VISIBLE);
                toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
                toolbar.setVisibility(View.GONE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setupFingerprintAuth();
                }
                break;
        }
    }

    @Override
    protected void onPause() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (fpHelper != null) {
                fpHelper.stop();
            }
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (fpHelper != null && fingerprintDialog != null && fingerprintDialog.isShowing()) {
                fpHelper.startAuthentication(new LoginFPCallbackAdapter(this, fingerprintDialog));
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private void setupFingerprintAuth() {
        fpHelper = new FingerprintHelper(this);
        if (fpHelper.fingerPrintEnabled() && fpHelper.canUseFingerPrint()) {
            useFingerprintButton.setVisibility(View.VISIBLE);
            showFingerprintDialog();
        }

    }

    private boolean isCalledForResult() {
        // if not called for result, calling activity is null
        boolean calledForResult = getCallingActivity() != null;
        LogUtil.d(TAG, "isCalledForResult() returned: " + calledForResult);
        return calledForResult;
    }

    @Override
    public void onBackPressed() {
        // delete a number if possible, else go back
        if (!pinInputStateManager.delete()) {
            super.onBackPressed();
        }
    }

    private class NewPinListener implements PinInputStateManager.PinInputCompleteListener {

        private String firstPin;

        @Override
        public void onComplete(final String pin) {
            numberPad.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            errorMessage.setText("");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (firstPin == null) {
                        //reset the views and ask for the same pin again
                        firstPin = pin;
                        pinInputStateManager.clear();
                        progressBar.setVisibility(View.INVISIBLE);
                        promptMessage.setText(R.string.text_pinlock_repeat_prompt);
                        numberPad.setEnabled(true);
                    } else {
                        if (pin.equals(firstPin)) {
                            // everything's fine, return the PIN
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra(EXTRA_NEW_PIN, pin);
                            setResult(Activity.RESULT_OK, returnIntent);
                            LogUtil.d(TAG, "PIN input correct");
                            finish();
                        } else {
                            // PINs don't match: display error and repeat
                            promptMessage.setText(R.string.text_pinlock_new_prompt);
                            errorMessage.setText(R.string.text_pinlock_match_error);
                            pinInputStateManager.clear();
                            indicatorDotView.error();
                            progressBar.setVisibility(View.INVISIBLE);
                            firstPin = null;
                            numberPad.setEnabled(true);
                            LogUtil.e(TAG, "Input PINs don't match");
                        }
                    }
                }
            }, 200);

        }

    }

    private class AuthorizeAccessListener implements PinInputStateManager.PinInputCompleteListener {

        private int failures = 0;

        @Override
        public void onComplete(String pin) {
            numberPad.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            errorMessage.setText("");
            boolean checkPIN = PINManager.checkPIN(pin);
            if (checkPIN) {
                //PIN is correct, forward to main activity
                LogUtil.d(TAG, "PIN is correct");
                returnVerifiedResult();
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pinInputStateManager.clear();
                        numberPad.setEnabled(true);
                        indicatorDotView.error();
                        progressBar.setVisibility(View.INVISIBLE);
//                        errorMessage.setText(R.string.text_pinlock_auth_error);
                        failures++;
                        LogUtil.e(TAG, "PIN input failed, failures=" + failures);
                    }
                }, 200);
            }
        }

    }

    private void returnVerifiedResult() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(EXTRA_VERIFY_PIN_RESULT, true);
        setResult(Activity.RESULT_OK, returnIntent);
        UnlockStateManager.getInstance().unlock();
        finish();
    }


    private static class LoginFPCallbackAdapter implements FingerprintHelper.FingerprintCallbackAdapter {

        private PinLockActivity activity;
        private MaterialDialog fingerprintDialog;

        LoginFPCallbackAdapter(PinLockActivity activity, MaterialDialog fingerprintDialog) {
            this.activity = activity;
            this.fingerprintDialog = fingerprintDialog;
        }

        @Override
        public void onAuthenticationFailed() {
            if (fingerprintDialog != null) {
                fingerprintDialog.setTitle(R.string.fingerprint_unlock_dialog_failed_title);
                fingerprintDialog.setContent(R.string.fingerprint_unlock_dialog_failed_message);
                fingerprintDialog.setIcon(IconUtils.icon(activity, GoogleMaterial.Icon.gmd_alert_circle, R.color.android_red, 48));
            } else {
                Toast.makeText(activity, R.string.fingerprint_unlock_dialog_failed_title, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            if (fingerprintDialog != null) {
                fingerprintDialog.setActionButton(DialogAction.NEGATIVE, null);
                fingerprintDialog.setTitle(R.string.fingerprint_unlock_dialog_successful_title);
                fingerprintDialog.setContent(R.string.fingerprint_unlock_dialog_successful_message);
                fingerprintDialog.setIcon(IconUtils.icon(activity, GoogleMaterial.Icon.gmd_check_circle, R.color.android_green, 48));
            }

            switch (activity.getIntent().getAction()) {
                case ACTION_NEW_PIN:
                    throw new IllegalArgumentException("No fingerprint allowed for new pin recording");
                case ACTION_VERIFY_PIN:
                    activity.returnVerifiedResult();
                    break;
            }

        }
    }
}
