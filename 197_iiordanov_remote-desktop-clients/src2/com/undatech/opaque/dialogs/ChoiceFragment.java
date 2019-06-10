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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

import org.xmlpull.v1.XmlPullParser;

import com.undatech.opaque.R;

import android.R.bool;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class ChoiceFragment extends DialogFragment {
    public static String TAG = "GetTextFragment";
    
    public interface OnFragmentDismissedListener {
        public void onResponseObtained(boolean result);
    }
    
    private Button noButton;
    private Button yesButton;
    private TextView message;
    private boolean result;
    private OnFragmentDismissedListener dismissalListener;
    private String title;
    private String messageText;
    private String positiveButtonText;
    private String negativeButtonText;
    
    public ChoiceFragment () {}
    
    /**
     * We make sure that if this dialog is produced automatically as part of an activity rebuild
     * and its dismissalListener is null, it closes itself upon being attached to the window.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (dismissalListener == null) {
            this.dismiss();
        }
    }
    
    public void setOnFragmentDismissedListener (OnFragmentDismissedListener dismissalListener) {
        this.dismissalListener = dismissalListener;
    }
    
    public static ChoiceFragment newInstance(String title, String messageText, String positiveButtonText,
                                             String negativeButtonText, OnFragmentDismissedListener dismissalListener) {
        android.util.Log.e(TAG, "newInstance called");
        ChoiceFragment f = new ChoiceFragment();
        f.setOnFragmentDismissedListener(dismissalListener);

        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("messageText", messageText);
        args.putString("positiveButtonText", positiveButtonText);
        args.putString("negativeButtonText", negativeButtonText);
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
        positiveButtonText = getArguments().getString("positiveButtonText");
        negativeButtonText = getArguments().getString("negativeButtonText");
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        android.util.Log.e(TAG, "onCreateView called");

        // Set title for this dialog
        getDialog().setTitle(title);

        View v = inflater.inflate(R.layout.choice, container, false);
        message = (TextView) v.findViewById(R.id.message);
        message.setText(messageText);
        
        yesButton = (Button) v.findViewById(R.id.yesButton);
        yesButton.setText(positiveButtonText);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChoiceFragment.this.result = true;
                ChoiceFragment.this.dismiss();
            }
        });

        noButton = (Button) v.findViewById(R.id.noButton);
        noButton.setText(negativeButtonText);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChoiceFragment.this.result = false;
                ChoiceFragment.this.dismiss();
            }
        });
        return v;
    }
    
    @Override
    public void onDismiss (DialogInterface dialog) {
        android.util.Log.e(TAG, "dismiss: sending back data to Activity");
        if (dismissalListener != null) {
            dismissalListener.onResponseObtained(result);
        }
    }

    @Override
    public void onDestroyView() {
      if (getDialog() != null && getRetainInstance())
        getDialog().setOnDismissListener(null);
      super.onDestroyView();
    }
}
