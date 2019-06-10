/*
 * Copyright (C) 2017 Schürmann & Breitmoser GbR
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.sufficientlysecure.keychain.ui.dialog;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import org.sufficientlysecure.keychain.R;
import org.sufficientlysecure.keychain.util.Passphrase;
import timber.log.Timber;


public class SetPassphraseDialogFragment extends DialogFragment implements OnEditorActionListener {
    private static final String ARG_MESSENGER = "messenger";
    private static final String ARG_TITLE = "title";

    public static final int MESSAGE_OKAY = 1;

    public static final String MESSAGE_NEW_PASSPHRASE = "new_passphrase";

    private Messenger mMessenger;
    private EditText mPassphraseEditText;
    private EditText mPassphraseAgainEditText;
    private CheckBox mNoPassphraseCheckBox;

    /**
     * Creates new instance of this dialog fragment
     *
     * @param title     title of dialog
     * @param messenger to communicate back after setting the passphrase
     * @return
     */
    public static SetPassphraseDialogFragment newInstance(Messenger messenger, int title) {
        SetPassphraseDialogFragment frag = new SetPassphraseDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TITLE, title);
        args.putParcelable(ARG_MESSENGER, messenger);

        frag.setArguments(args);

        return frag;
    }

    /**
     * Creates dialog
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = getActivity();

        int title = getArguments().getInt(ARG_TITLE);
        mMessenger = getArguments().getParcelable(ARG_MESSENGER);

        CustomAlertDialogBuilder alert = new CustomAlertDialogBuilder(activity);

        alert.setTitle(title);

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.passphrase_repeat_dialog, null);
        alert.setView(view);

        mPassphraseEditText = view.findViewById(R.id.passphrase_passphrase);
        mPassphraseAgainEditText = view.findViewById(R.id.passphrase_passphrase_again);
        mNoPassphraseCheckBox = view.findViewById(R.id.passphrase_no_passphrase);

        mNoPassphraseCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPassphraseEditText.setEnabled(!isChecked);
                mPassphraseAgainEditText.setEnabled(!isChecked);
            }
        });

        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {
                dismiss();

                Passphrase passphrase1 = new Passphrase();
                if (mNoPassphraseCheckBox.isChecked()) {
                    passphrase1.setEmpty();
                } else {
                    passphrase1 = new Passphrase(mPassphraseEditText);
                    Passphrase passphrase2 = new Passphrase(mPassphraseAgainEditText);
                    if (!passphrase1.equals(passphrase2)) {
                        Toast.makeText(
                                activity,
                                getString(R.string.error_message,
                                        getString(R.string.passphrases_do_not_match)), Toast.LENGTH_SHORT
                        )
                                .show();
                        return;
                    }

                    if (passphrase1.isEmpty()) {
                        Toast.makeText(
                                activity,
                                getString(R.string.error_message,
                                        getString(R.string.passphrase_must_not_be_empty)),
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }
                }

                // return resulting data back to activity
                Bundle data = new Bundle();
                data.putParcelable(MESSAGE_NEW_PASSPHRASE, passphrase1);

                sendMessageToHandler(MESSAGE_OKAY, data);
            }
        });

        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {
                dismiss();
            }
        });

        // Hack to open keyboard.
        // This is the only method that I found to work across all Android versions
        // http://turbomanage.wordpress.com/2012/05/02/show-soft-keyboard-automatically-when-edittext-receives-focus/
        // Notes: * onCreateView can't be used because we want to add buttons to the dialog
        //        * opening in onActivityCreated does not work on Android 4.4
        mPassphraseEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mPassphraseEditText.post(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager imm = (InputMethodManager) getActivity()
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(mPassphraseEditText, InputMethodManager.SHOW_IMPLICIT);
                    }
                });
            }
        });
        mPassphraseEditText.requestFocus();

        mPassphraseAgainEditText.setImeActionLabel(getString(android.R.string.ok), EditorInfo.IME_ACTION_DONE);
        mPassphraseAgainEditText.setOnEditorActionListener(this);

        return alert.show();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        // hide keyboard on dismiss
        hideKeyboard();
    }

    private void hideKeyboard() {
        if (getActivity() == null) {
            return;
        }
        InputMethodManager inputManager = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        //check if no view has focus:
        View v = getActivity().getCurrentFocus();
        if (v == null)
            return;

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    /**
     * Associate the "done" button on the soft keyboard with the okay button in the view
     */
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            AlertDialog dialog = ((AlertDialog) getDialog());
            Button bt = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            bt.performClick();
            return true;
        }
        return false;
    }

    /**
     * Send message back to handler which is initialized in a activity
     *
     * @param what Message integer you want to send
     */
    private void sendMessageToHandler(Integer what, Bundle data) {
        Message msg = Message.obtain();
        msg.what = what;
        if (data != null) {
            msg.setData(data);
        }

        try {
            mMessenger.send(msg);
        } catch (RemoteException e) {
            Timber.w(e, "Exception sending message, Is handler present?");
        } catch (NullPointerException e) {
            Timber.w(e, "Messenger is null!");
        }
    }
}
