/*
 * Copyright 2009-2018 Brian Pellin.
 *
 * This file is part of KeePassDroid.
 *
 *  KeePassDroid is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  KeePassDroid is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with KeePassDroid.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.keepassdroid;

import android.Manifest;
import android.app.Activity;
import android.app.backup.BackupManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.keepass.KeePass;
import com.android.keepass.R;
import com.keepassdroid.app.App;
import com.keepassdroid.compat.ClipDataCompat;
import com.keepassdroid.compat.StorageAF;
import com.keepassdroid.database.edit.LoadDB;
import com.keepassdroid.database.edit.OnFinish;
import com.keepassdroid.dialog.PasswordEncodingDialogHelper;
import com.keepassdroid.fileselect.BrowserDialog;
import com.keepassdroid.fingerprint.FingerPrintHelper;
import com.keepassdroid.intents.Intents;
import com.keepassdroid.settings.AppSettingsActivity;
import com.keepassdroid.utils.EmptyUtils;
import com.keepassdroid.utils.Interaction;
import com.keepassdroid.utils.PermissionUtil;
import com.keepassdroid.utils.UriUtil;
import com.keepassdroid.utils.Util;

import java.io.File;
import java.io.FileNotFoundException;

import javax.crypto.Cipher;

public class PasswordActivity extends LockingActivity implements FingerPrintHelper.FingerPrintCallback {

    public static final String KEY_DEFAULT_FILENAME = "defaultFileName";
    private static final String KEY_FILENAME = "fileName";
    private static final String KEY_KEYFILE = "keyFile";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_LAUNCH_IMMEDIATELY = "launchImmediately";
    private static final String VIEW_INTENT = "android.intent.action.VIEW";

    private static final int FILE_BROWSE = 256;
    public static final int GET_CONTENT = 257;
    private static final int OPEN_DOC = 258;

    private static final String[] READ_WRITE_PERMISSIONS =
            {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int PERMISSION_REQUEST_ID = 1;

    private Uri mDbUri = null;
    private Uri mKeyUri = null;
    private boolean mRememberKeyfile;
    SharedPreferences prefs;
    SharedPreferences prefsNoBackup;

    private Uri storedKeyUri = null;
    private String storedPassword = null;


    private FingerPrintHelper fingerPrintHelper;
    private int mode;
    private static final String PREF_KEY_VALUE_PREFIX = "valueFor_"; // key is a combination of db file name and this prefix
    private static final String PREF_KEY_IV_PREFIX = "ivFor_"; // key is a combination of db file name and this prefix
    private View fingerprintView;
    private TextView confirmationView;
    private EditText passwordView;
    private Button confirmButton;

    public static void Launch(
            Activity act,
            String fileName) throws FileNotFoundException {
        Launch(act, fileName, "");
    }

    public static void Launch(
            Activity act,
            String fileName,
            String keyFile) throws FileNotFoundException {
        if (EmptyUtils.isNullOrEmpty(fileName)) {
            throw new FileNotFoundException();
        }

        Uri uri = UriUtil.parseDefaultFile(fileName);
        String scheme = uri.getScheme();

        if (!EmptyUtils.isNullOrEmpty(scheme) && scheme.equalsIgnoreCase("file")) {
            File dbFile = new File(uri.getPath());
            if (!dbFile.exists()) {
                throw new FileNotFoundException();
            }
        }

        Intent i = new Intent(act, PasswordActivity.class);
        i.putExtra(KEY_FILENAME, fileName);
        i.putExtra(KEY_KEYFILE, keyFile);

        act.startActivityForResult(i, 0);

    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case KeePass.EXIT_NORMAL:
                setEditText(R.id.password, "");
                App.getDB().clear(getApplicationContext());
                break;

            case KeePass.EXIT_LOCK:
                setResult(KeePass.EXIT_LOCK);
                setEditText(R.id.password, "");
                finish();
                App.getDB().clear(getApplicationContext());
                break;
            case FILE_BROWSE:
                if (resultCode == RESULT_OK) {
                    String filename = data.getDataString();
                    if (filename != null) {
                        EditText fn = (EditText) findViewById(R.id.pass_keyfile);
                        fn.setText(filename);
                        mKeyUri = UriUtil.parseDefaultFile(filename);
                    }
                }
                break;
            case GET_CONTENT:
            case OPEN_DOC:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        Uri uri = data.getData();
                        if (uri != null) {
                            if (requestCode == GET_CONTENT) {
                                uri = UriUtil.translate(this, uri);
                            }
                            String path = uri.toString();
                            if (path != null) {
                                EditText fn = (EditText) findViewById(R.id.pass_keyfile);
                                fn.setText(path);

                            }
                            mKeyUri = uri;
                        }
                    }
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefsNoBackup = getSharedPreferences("nobackup", Context.MODE_PRIVATE);

        mRememberKeyfile = prefs.getBoolean(getString(R.string.keyfile_key), getResources().getBoolean(R.bool.keyfile_default));
        setContentView(R.layout.password);

        confirmButton = (Button) findViewById(R.id.pass_ok);
        fingerprintView = findViewById(R.id.fingerprint);
        confirmationView = (TextView) findViewById(R.id.fingerprint_label);
        passwordView = (EditText) findViewById(R.id.password);

        new InitTask().execute(i);

        initForFingerprint();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // If the application was shutdown make sure to clear the password field, if it
        // was saved in the instance state
        if (App.isShutdown()) {
            TextView password = (TextView) findViewById(R.id.password);
            password.setText("");
        }

        // Clear the shutdown flag
        App.clearShutdown();

        // checks if fingerprint is available, only starts listening for fingerprints when available & auto start configured
        checkFingerprintAvailability(false);
    }

    private void retrieveSettings() {
        String defaultFilename = prefs.getString(KEY_DEFAULT_FILENAME, "");
        if (!EmptyUtils.isNullOrEmpty(mDbUri.getPath()) && UriUtil.equalsDefaultfile(mDbUri, defaultFilename)) {
            CheckBox checkbox = (CheckBox) findViewById(R.id.default_database);
            checkbox.setChecked(true);
        }
    }

    private Uri getKeyFile(Uri dbUri) {
        if (mRememberKeyfile) {

            return App.getFileHistory().getFileByName(dbUri);
        } else {
            return null;
        }
    }

    private void populateView() {
        String db = (mDbUri == null) ? "" : mDbUri.toString();
        setEditText(R.id.filename, db);

        String key = (mKeyUri == null) ? "" : mKeyUri.toString();
        setEditText(R.id.pass_keyfile, key);
    }

    private void errorMessage(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_LONG).show();
    }

    // fingerprint related code here
    private void initForFingerprint() {
        // when fingerprint is enabled but auto scanning is not you can start scanning by clicking the fingerprint view
        fingerprintView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkFingerprintAvailability(true);
            }
        });
        fingerPrintHelper = new FingerPrintHelper(this, this);
        if (fingerPrintHelper.hasEnrolledFingerprints()) {

            // when text entered we can enable the logon/purchase button and if required update encryption/decryption mode
            passwordView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(
                        final CharSequence s,
                        final int start,
                        final int count,
                        final int after) {

                }

                @Override
                public void onTextChanged(
                        final CharSequence s,
                        final int start,
                        final int before,
                        final int count) {

                }

                @Override
                public void afterTextChanged(final Editable s) {
                    final boolean validInput = s.length() > 0;
                    // encrypt or decrypt mode based on how much input or not
                    int messageId;
                    if (validInput) {
                        messageId = R.string.store_with_fingerprint;
                    }
                    else if (EmptyUtils.isNullOrEmpty(prefsNoBackup.getString(getPreferenceKeyValue(), null))) {
                        messageId = R.string.no_password_stored;
                    }
                    else {
                        messageId = R.string.scanning_fingerprint;
                    }
                    confirmationView.setText(messageId);
                    boolean autoStartScanning = prefs.getBoolean(getString(R.string.fingerprint_autoscan_key), true);
                    mode = validInput ? toggleMode(Cipher.ENCRYPT_MODE, autoStartScanning) : toggleMode(Cipher.DECRYPT_MODE, autoStartScanning);

                }
            });

            // callback for fingerprint findings
            fingerPrintHelper.setAuthenticationCallback(new FingerprintManagerCompat.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(
                        final int errorCode,
                        final CharSequence errString) {

                    if (errorCode != 5) { // FINGERPRINT_ERROR_CANCELLED (not defined in support library)
                        onException(errString);
                    }
                }

                @Override
                public void onAuthenticationHelp(
                        final int helpCode,
                        final CharSequence helpString) {

                    onException();
                    confirmationView.setText(helpString);
                }

                @Override
                public void onAuthenticationSucceeded(final FingerprintManagerCompat.AuthenticationResult result) {

                    if (mode == Cipher.ENCRYPT_MODE) {

                        // newly store the entered password in encrypted way
                        final String password = passwordView.getText().toString();
                        fingerPrintHelper.encryptData(password);

                    } else if (mode == Cipher.DECRYPT_MODE) {

                        // retrieve the encrypted value from preferences
                        final String encryptedValue = prefsNoBackup.getString(getPreferenceKeyValue(), null);
                        if (encryptedValue != null) {
                            fingerPrintHelper.decryptData(encryptedValue);
                        }
                    }
                }

                @Override
                public void onAuthenticationFailed() {
                    onException(R.string.fingerprint_notrecognized);
                }
            });
        }
    }

    private String getPreferenceKeyValue() {
        // makes it possible to store passwords uniqly per database
        return PREF_KEY_VALUE_PREFIX + (mDbUri != null ? mDbUri.getPath() : "");
    }

    private String getPreferenceKeyIvSpec() {
        return PREF_KEY_IV_PREFIX + (mDbUri != null ? mDbUri.getPath() : "");
    }

    private int toggleMode(final int newMode, final boolean startListening) {
        if (mode != newMode) {
            mode = newMode;
            switch (mode) {
                case Cipher.ENCRYPT_MODE:
                    fingerPrintHelper.initEncryptData(startListening);
                    break;
                case Cipher.DECRYPT_MODE:
                    final String ivSpecValue = prefsNoBackup.getString(getPreferenceKeyIvSpec(), null);
                    if (ivSpecValue != null) {
                        fingerPrintHelper.initDecryptData(ivSpecValue, startListening);
                    }
                    break;
            }
        }
        else {
            fingerPrintHelper.stopListening();
            if( startListening ) fingerPrintHelper.startListening();
        }

        return mode;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // stop listening when we go in background
        if (fingerPrintHelper != null) {
            fingerPrintHelper.stopListening();
        }
    }

    private void setFingerPrintVisibilty(int vis) {
        ImageButton browse = (ImageButton) findViewById(R.id.browse_button);
        EditText fn = (EditText) findViewById(R.id.pass_keyfile);

        RelativeLayout.LayoutParams browseParams = (RelativeLayout.LayoutParams) browse.getLayoutParams();
        RelativeLayout.LayoutParams fnParams = (RelativeLayout.LayoutParams) fn.getLayoutParams();
        int layoutBelow;
        if (vis == View.GONE) {
            layoutBelow = R.id.password;
        } else {
            layoutBelow = R.id.fingerprint_label;
        }
        browseParams.addRule(RelativeLayout.BELOW, layoutBelow);
        fnParams.addRule(RelativeLayout.BELOW, layoutBelow);
        fingerprintView.setVisibility(vis);
        confirmationView.setVisibility(vis);
    }

    private void checkFingerprintAvailability(final boolean forceAutoStart) {
        // fingerprint not supported (by API level or hardware) so keep option hidden
        if (!fingerPrintHelper.isHardwareDetected()) {
            setFingerPrintVisibilty(View.GONE);
        }
        // fingerprint is available but not configured show icon but in disabled state with some information
        else if (!fingerPrintHelper.hasEnrolledFingerprints()) {

            setFingerPrintVisibilty(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                fingerprintView.setAlpha(0.3f);
            }
            // This happens when no fingerprints are registered. Listening won't start
            confirmationView.setText(R.string.configure_fingerprint);
        }
        // finally fingerprint available and configured so we can use it
        else {

            setFingerPrintVisibilty(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                fingerprintView.setAlpha(1f);
            }
            // fingerprint available but no stored password found yet for this DB so show info don't listen
            if (prefsNoBackup.getString(getPreferenceKeyValue(), null) == null) {

                confirmationView.setText(R.string.no_password_stored);
            }
            // all is set here so we can confirm to user and start listening for fingerprints
            if( forceAutoStart || prefs.getBoolean(getString(R.string.fingerprint_autoscan_key), true) ){

                confirmationView.setText(R.string.scanning_fingerprint);
                // listen for decryption by default
                toggleMode(Cipher.DECRYPT_MODE, true);
            }
            // config OK but no auto scanning enabled
            else {

                confirmationView.setText(R.string.fingerprint_autoscan_title);
            }
        }
    }

    @Override
    public void handleEncryptedResult(
            final String value,
            final String ivSpec) {

        prefsNoBackup.edit()
                .putString(getPreferenceKeyValue(), value)
                .putString(getPreferenceKeyIvSpec(), ivSpec)
                .commit();
        // and remove visual input to reset UI
        confirmButton.performClick();
        confirmationView.setText(R.string.encrypted_value_stored);
    }

    @Override
    public void handleDecryptedResult(final String value) {
        // on decrypt enter it for the purchase/login action
        passwordView.setText(value);
        confirmButton.performClick();
    }

    @Override
    public void onInvalidKeyException() {
        Toast.makeText(this, R.string.fingerprint_invalid_key, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onException() {
        onException(true);
    }

    @Override
    public void onException(boolean showMessage) {
        if (showMessage) {
            onException(R.string.fingerprint_error);
        }
    }

    @Override
    public void onException(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onException(CharSequence message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onKeyInvalidated() {
        prefsNoBackup.edit()
                .remove(getPreferenceKeyValue())
                .remove(getPreferenceKeyIvSpec())
                .commit();

        confirmationView.setText(R.string.fingerprint_key_invalidated);
    }

    private class DefaultCheckChange implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(
                CompoundButton buttonView,
                boolean isChecked) {

            String newDefaultFileName;

            if (isChecked) {
                newDefaultFileName = mDbUri.toString();
            } else {
                newDefaultFileName = "";
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_DEFAULT_FILENAME, newDefaultFileName);
            editor.apply();

            BackupManager backupManager = new BackupManager(PasswordActivity.this);
            backupManager.dataChanged();

        }

    }

    private class OkClickHandler implements View.OnClickListener {

        public void onClick(View view) {
            String pass = getEditText(R.id.password);
            String key = getEditText(R.id.pass_keyfile);
            loadDatabase(pass, key);
        }
    }

    private void loadDatabase(
            String pass,
            String keyfile) {

        loadDatabase(pass, UriUtil.parseDefaultFile(keyfile));
    }

    private boolean hasFileUri(Uri uri) {
        try {
            if (uri == null) { return false; }

            return uri.getScheme().equalsIgnoreCase("file");
        } catch (Exception e) {
            return false;
        }

    }

    private boolean checkFilePermissions(Uri db, Uri keyfile) {
        boolean hasFileUri = hasFileUri(db) ||
                hasFileUri(keyfile);

        if (!hasFileUri) return true;

        return PermissionUtil.checkAndRequest(this, PERMISSION_REQUEST_ID);
    }

    public void onRequestPermissionsResult (int requestCode,
                String[] permissions,
                int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_ID &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED){

            loadDatabaseWithPermission();
        } else {
            errorMessage(R.string.no_external_permissions);
        }
    }

    private void loadDatabase(
            String pass,
            Uri keyfile) {
        if (pass.length() == 0 && (keyfile == null || keyfile.toString().length() == 0)) {
            errorMessage(R.string.error_nopass);
            return;
        }

        storedPassword = pass;
        storedKeyUri = keyfile;

        if (checkFilePermissions(mDbUri, keyfile)) {
            loadDatabaseWithPermission();
        }
    }

    private void loadDatabaseWithPermission() {
        String pass = storedPassword;
        storedPassword = null;
        Uri keyfile = storedKeyUri;
        storedKeyUri = null;


        // Clear before we load
        Database db = App.getDB();
        db.clear(getApplicationContext());

        // Clear the shutdown flag
        App.clearShutdown();

        Handler handler = new Handler();
        LoadDB task = new LoadDB(db, PasswordActivity.this, mDbUri, pass, keyfile, new AfterLoad(handler, db));
        ProgressTask pt = new ProgressTask(PasswordActivity.this, task, R.string.loading_database);
        pt.run();
    }

    private String getEditText(int resId) {
        return Util.getEditText(this, resId);
    }

    private void setEditText(
            int resId,
            String str) {
        TextView te = (TextView) findViewById(resId);
        assert (te == null);

        if (te != null) {
            te.setText(str);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflate = getMenuInflater();
        inflate.inflate(R.menu.password, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about:
                AboutDialog dialog = new AboutDialog(this);
                dialog.show();
                return true;

            case R.id.menu_app_settings:
                AppSettingsActivity.Launch(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private final class AfterLoad extends OnFinish {

        private Database db;

        public AfterLoad(
                Handler handler,
                Database db) {
            super(handler);

            this.db = db;
        }

        @Override
        public void run() {
            if (db.passwordEncodingError) {
                PasswordEncodingDialogHelper dialog = new PasswordEncodingDialogHelper();
                dialog.show(PasswordActivity.this, new OnClickListener() {

                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int which) {
                        GroupActivity.Launch(PasswordActivity.this);
                    }

                });
            } else if (mSuccess) {
                GroupActivity.Launch(PasswordActivity.this);
            } else {
                displayMessage(PasswordActivity.this);
            }
        }
    }

    private class InitTask extends AsyncTask<Intent, Void, Integer> {

        String password = "";
        boolean launch_immediately = false;

        @Override
        protected Integer doInBackground(Intent... args) {
            Intent i = args[0];
            String action = i.getAction();
            ;
            if (action != null && action.equals(VIEW_INTENT)) {
                Uri incoming = i.getData();
                mDbUri = incoming;

                mKeyUri = ClipDataCompat.getUriFromIntent(i, KEY_KEYFILE);

                if (incoming == null) {
                    return R.string.error_can_not_handle_uri;
                } else if (incoming.getScheme().equals("file")) {
                    String fileName = incoming.getPath();

                    if (fileName.length() == 0) {
                        // No file name
                        return R.string.FileNotFound;
                    }

                    File dbFile = new File(fileName);
                    if (!dbFile.exists()) {
                        // File does not exist
                        return R.string.FileNotFound;
                    }

                    if (mKeyUri == null) {
                        mKeyUri = getKeyFile(mDbUri);
                    }
                } else if (incoming.getScheme().equals("content")) {
                    if (mKeyUri == null) {
                        mKeyUri = getKeyFile(mDbUri);
                    }
                } else {
                    return R.string.error_can_not_handle_uri;
                }
                password = i.getStringExtra(KEY_PASSWORD);
                launch_immediately = i.getBooleanExtra(KEY_LAUNCH_IMMEDIATELY, false);

            } else {
                mDbUri = UriUtil.parseDefaultFile(i.getStringExtra(KEY_FILENAME));
                mKeyUri = UriUtil.parseDefaultFile(i.getStringExtra(KEY_KEYFILE));
                password = i.getStringExtra(KEY_PASSWORD);
                launch_immediately = i.getBooleanExtra(KEY_LAUNCH_IMMEDIATELY, false);

                if (mKeyUri == null || mKeyUri.toString().length() == 0) {
                    mKeyUri = getKeyFile(mDbUri);
                }
            }
            return null;
        }

        public void onPostExecute(Integer result) {
            if (result != null) {
                Toast.makeText(PasswordActivity.this, result, Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            populateView();

            confirmButton.setOnClickListener(new OkClickHandler());

            CheckBox checkBox = (CheckBox) findViewById(R.id.show_password);
            // Show or hide password
            checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                public void onCheckedChanged(
                        CompoundButton buttonView,
                        boolean isChecked) {
                    TextView password = (TextView) findViewById(R.id.password);

                    if (isChecked) {
                        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    } else {
                        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }
                }

            });

            if (password != null) {
                TextView tv_password = (TextView) findViewById(R.id.password);
                tv_password.setText(password);
            }

            CheckBox defaultCheck = (CheckBox) findViewById(R.id.default_database);
            defaultCheck.setOnCheckedChangeListener(new DefaultCheckChange());

            ImageButton browse = (ImageButton) findViewById(R.id.browse_button);
            browse.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    if (StorageAF.useStorageFramework(PasswordActivity.this)) {
                        Intent i = new Intent(StorageAF.ACTION_OPEN_DOCUMENT);
                        i.addCategory(Intent.CATEGORY_OPENABLE);
                        i.setType("*/*");
                        startActivityForResult(i, OPEN_DOC);
                    } else {
                        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                        i.addCategory(Intent.CATEGORY_OPENABLE);
                        i.setType("*/*");

                        try {
                            startActivityForResult(i, GET_CONTENT);
                        } catch (ActivityNotFoundException e) {
                            lookForOpenIntentsFilePicker();
                        }
                    }
                }

                private void lookForOpenIntentsFilePicker() {
                    if (Interaction.isIntentAvailable(PasswordActivity.this, Intents.OPEN_INTENTS_FILE_BROWSE)) {
                        Intent i = new Intent(Intents.OPEN_INTENTS_FILE_BROWSE);

                        // Get file path parent if possible
                        try {
                            if (mDbUri != null && mDbUri.toString().length() > 0) {
                                if (mDbUri.getScheme().equals("file")) {
                                    File keyfile = new File(mDbUri.getPath());
                                    File parent = keyfile.getParentFile();
                                    if (parent != null) {
                                        i.setData(Uri.parse("file://" + parent.getAbsolutePath()));
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // Ignore
                        }

                        try {
                            startActivityForResult(i, FILE_BROWSE);
                        } catch (ActivityNotFoundException e) {
                            showBrowserDialog();
                        }
                    } else {
                        showBrowserDialog();
                    }
                }

                private void showBrowserDialog() {
                    BrowserDialog diag = new BrowserDialog(PasswordActivity.this);
                    diag.show();
                }
            });

            retrieveSettings();

            if (launch_immediately) {
                loadDatabase(password, mKeyUri);
            }
        }
    }
}
