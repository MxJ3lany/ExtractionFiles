package co.infinum.example;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

import co.infinum.goldfinger.Error;
import co.infinum.goldfinger.Goldfinger;

public class ExampleActivity extends AppCompatActivity {

    private static final String KEY_NAME = "Example";

    private View authenticateButton;
    private View decryptButton;
    private View encryptButton;

    private String encryptedValue;
    private Goldfinger goldfinger;
    private OnTextChangedListener onTextChangedListener = new OnTextChangedListener() {
        @Override
        void onTextChanged(String text) {
            encryptButton.setEnabled(!text.isEmpty() && goldfinger.hasEnrolledFingerprint());
        }
    };
    private EditText secretInputView;
    private TextView statusView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);

        encryptButton = findViewById(R.id.encryptButton);
        decryptButton = findViewById(R.id.decryptButton);
        authenticateButton = findViewById(R.id.authenticateButton);
        secretInputView = findViewById(R.id.secretInputView);
        statusView = findViewById(R.id.statusView);

        goldfinger = new Goldfinger.Builder(this).setLogEnabled(BuildConfig.DEBUG).build();

        secretInputView.addTextChangedListener(onTextChangedListener);
        initListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();

        authenticateButton.setEnabled(goldfinger.hasEnrolledFingerprint());

        if (goldfinger.hasFingerprintHardware()
            && goldfinger.hasEnrolledFingerprint()) {
            authenticateButton.setEnabled(true);
        } else {
            authenticateButton.setEnabled(false);
            statusView.setText(getString(R.string.fingerprint_not_available));
            statusView.setTextColor(ContextCompat.getColor(this, R.color.error));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        goldfinger.cancel();
    }

    private void authenticateUserFingerprint() {
        goldfinger.authenticate(new Goldfinger.Callback() {
            @Override
            public void onError(Error error) {
                onErrorResult(error);
            }

            @Override
            public void onSuccess(String value) {
                onSuccessResult(value);
            }
        });
    }

    private void decryptEncryptedValue() {
        goldfinger.decrypt(KEY_NAME, encryptedValue, new Goldfinger.Callback() {
            @Override
            public void onError(Error error) {
                onErrorResult(error);
            }

            @Override
            public void onSuccess(String value) {
                onSuccessResult(value);
            }
        });
    }

    private void encryptSecretValue() {
        goldfinger.encrypt(KEY_NAME, secretInputView.getText().toString(), new Goldfinger.Callback() {
            @Override
            public void onError(Error error) {
                onErrorResult(error);
            }

            @Override
            public void onSuccess(String value) {
                encryptedValue = value;
                decryptButton.setEnabled(true);
                onSuccessResult(value);
            }
        });
    }

    private void initListeners() {
        encryptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetStatusText();
                encryptSecretValue();
            }
        });

        decryptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetStatusText();
                decryptEncryptedValue();
            }
        });

        authenticateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetStatusText();
                authenticateUserFingerprint();
            }
        });
    }

    private void onErrorResult(Error error) {
        onResult("onError", error.toString());
        if (error.isCritical()) {
            statusView.setTextColor(ContextCompat.getColor(this, R.color.error));
        } else {
            statusView.setTextColor(ContextCompat.getColor(this, R.color.warning));
        }
    }

    private void onResult(String methodName, String value) {
        statusView.setText(String.format(Locale.US, "%s - [%s]", methodName, value));
    }

    private void onSuccessResult(String value) {
        onResult("onSuccess", value);
        statusView.setTextColor(ContextCompat.getColor(this, R.color.ok));
    }

    private void resetStatusText() {
        statusView.setTextColor(ContextCompat.getColor(this, R.color.textRegular));
        statusView.setText(getString(R.string.authenticate_user));
    }
}
