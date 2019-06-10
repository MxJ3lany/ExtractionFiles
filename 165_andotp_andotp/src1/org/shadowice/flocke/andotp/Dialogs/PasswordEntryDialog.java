package org.shadowice.flocke.andotp.Dialogs;

import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.Tools;

public class PasswordEntryDialog extends AppCompatDialog
    implements View.OnClickListener, TextWatcher {

    public enum Mode { ENTER, UPDATE }

    public interface PasswordEnteredCallback {
        void onPasswordEntered(String newPassword);
    }

    private Mode dialogMode;
    private PasswordEnteredCallback callback;

    private TextInputEditText passwordInput;
    private EditText passwordConfirm;
    private Button okButton;

    public PasswordEntryDialog(Context context, Mode newMode, PasswordEnteredCallback newCallback) {
        super(context, Tools.getThemeResource(context, R.attr.dialogTheme));

        setTitle(R.string.dialog_title_enter_password);
        setContentView(R.layout.dialog_password_entry);

        passwordInput = findViewById(R.id.passwordInput);
        passwordConfirm = findViewById(R.id.passwordConfirm);

        okButton = findViewById(R.id.buttonOk);
        Button cancelButton = findViewById(R.id.buttonCancel);

        okButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        this.callback = newCallback;

        this.dialogMode = newMode;

        if (this.dialogMode == Mode.UPDATE) {
            passwordConfirm.setVisibility(View.VISIBLE);
            passwordInput.addTextChangedListener(this);
            passwordConfirm.addTextChangedListener(this);
        } else if (this.dialogMode == Mode.ENTER) {
            passwordConfirm.setVisibility(View.GONE);
        }
    }

    // TextWatcher
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (TextUtils.equals(passwordInput.getEditableText(), passwordConfirm.getEditableText()))
            okButton.setEnabled(true);
        else
            okButton.setEnabled(false);
    }

    public void afterTextChanged(Editable s) {}
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    // View.OnClickListener
    public void onClick(View view)  {
        if (view.getId() == R.id.buttonOk) {
            if (callback != null)
                callback.onPasswordEntered(passwordInput.getText().toString());
        }

        dismiss();
    }
}
