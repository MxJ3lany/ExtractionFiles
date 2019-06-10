/**
 * Copyright (C) 2013- Iordan Iordanov
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
 * USA.
 */


package com.undatech.opaque.dialogs;

import com.undatech.opaque.R;
import com.undatech.opaque.dialogs.GetTextFragment.OnFragmentDismissedListener;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class MessageFragment extends DialogFragment {
    public static String TAG = "MessageFragment";
    
    public interface OnFragmentDismissedListener {
        public void onDialogDismissed();
    }
    
    private TextView message;
    private Button okButton;
    private OnFragmentDismissedListener dismissalListener;
    private String title;
    private String messageText;
    private String okButtonText;
    
    public MessageFragment () {}
    
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        TextView message = (TextView)dialog.findViewById(android.R.id.message);
        if (message != null)
            message.setMovementMethod(LinkMovementMethod.getInstance());
    }
    
    /**
     * We make sure that if this dialog is produced automatically as part of an activity rebuild
     * and its dismissalListener is null, it closes itself upon being attached to the window.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
    
    public void setOnFragmentDismissedListener (OnFragmentDismissedListener dismissalListener) {
        this.dismissalListener = dismissalListener;
    }
    
    public static MessageFragment newInstance(String title, String messageText, String okButtonText,
                                              OnFragmentDismissedListener dismissalListener) {
        android.util.Log.e(TAG, "newInstance called");
        MessageFragment f = new MessageFragment();
        f.setOnFragmentDismissedListener(dismissalListener);

        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("messageText", messageText);
        args.putString("okButtonText", okButtonText);
        f.setArguments(args);
        f.setRetainInstance(true);
        return f;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.e(TAG, "onCreate called");
        title = getArguments().getString("title");
        messageText = getArguments().getString("messageText");
        okButtonText = getArguments().getString("okButtonText");
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        android.util.Log.e(TAG, "onCreateView called");
        
        // Set title for this dialog
        getDialog().setTitle(title);
        
        View v = inflater.inflate(R.layout.message, container, false);
        message = (TextView) v.findViewById(R.id.message);
        Spanned text = Html.fromHtml(messageText);
        message.setText(text);
        message.setMovementMethod(LinkMovementMethod.getInstance());
        
        okButton = (Button) v.findViewById(R.id.okButton);
        okButton.setText(okButtonText);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageFragment.this.dismiss();
            }
        });
        return v;
    }
    
    @Override
    public void onDismiss (DialogInterface dialog) {
        android.util.Log.i(TAG, "dismiss: sending back data to listener");
        if (dismissalListener != null) {
            dismissalListener.onDialogDismissed();
        }
    }
    
    @Override
    public void onDestroyView() {
      if (getDialog() != null && getRetainInstance())
        getDialog().setOnDismissListener(null);
      super.onDestroyView();
    }
}
