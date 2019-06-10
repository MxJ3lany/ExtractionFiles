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
import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import org.sufficientlysecure.keychain.R;
import org.sufficientlysecure.keychain.ui.widget.EmailEditText;
import timber.log.Timber;


public class AddEmailDialogFragment extends DialogFragment implements OnEditorActionListener {
    private static final String ARG_MESSENGER = "messenger";

    public static final int MESSAGE_OKAY = 1;
    public static final int MESSAGE_CANCEL = 2;

    public static final String MESSAGE_DATA_EMAIL = "email";

    private Messenger mMessenger;
    private EmailEditText mEmail;

    public static AddEmailDialogFragment newInstance(Messenger messenger) {

        AddEmailDialogFragment frag = new AddEmailDialogFragment();
        Bundle args = new Bundle();
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
        mMessenger = getArguments().getParcelable(ARG_MESSENGER);

        CustomAlertDialogBuilder alert = new CustomAlertDialogBuilder(activity);

        alert.setTitle(R.string.create_key_add_email);

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.add_email_dialog, null);
        alert.setView(view);

        mEmail = view.findViewById(R.id.add_email_address);

        alert.setPositiveButton(android.R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dismiss();

                // return new user id back to activity
                Bundle data = new Bundle();
                String email = mEmail.getText().toString();
                data.putString(MESSAGE_DATA_EMAIL, email);
                sendMessageToHandler(MESSAGE_OKAY, data);
            }
        });

        alert.setNegativeButton(android.R.string.cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        // Hack to open keyboard.
        // This is the only method that I found to work across all Android versions
        // http://turbomanage.wordpress.com/2012/05/02/show-soft-keyboard-automatically-when-edittext-receives-focus/
        // Notes: * onCreateView can't be used because we want to add buttons to the dialog
        //        * opening in onActivityCreated does not work on Android 4.4
        mEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mEmail.post(new Runnable() {
                    @Override
                    public void run() {
                        if(getActivity() != null) {
                            InputMethodManager imm = (InputMethodManager) getActivity()
                                    .getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(mEmail, InputMethodManager.SHOW_IMPLICIT);
                        }
                    }
                });
            }
        });
        mEmail.requestFocus();

        mEmail.setImeActionLabel(getString(android.R.string.ok), EditorInfo.IME_ACTION_DONE);
        mEmail.setOnEditorActionListener(this);

        return alert.show();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        dismiss();
        sendMessageToHandler(MESSAGE_CANCEL);
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

        // check if no view has focus:
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
    private void sendMessageToHandler(Integer what) {
        Message msg = Message.obtain();
        msg.what = what;

        try {
            mMessenger.send(msg);
        } catch (RemoteException e) {
            Timber.w(e, "Exception sending message, Is handler present?");
        } catch (NullPointerException e) {
            Timber.w(e, "Messenger is null!");
        }
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
