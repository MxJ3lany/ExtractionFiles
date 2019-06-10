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

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class SelectTextElementFragment extends DialogFragment {
    public static String TAG = "SelectVmFragment";
    
    public interface OnFragmentDismissedListener {
        public void onTextSelected(String selectedString);
    }
    
    private OnFragmentDismissedListener dismissalListener;
    private ArrayList<String> strings;
    private String title;
    
    LinearLayout verticalLayout;
    
    String selected = "";

    public SelectTextElementFragment () {}

    public void setOnFragmentDismissedListener (OnFragmentDismissedListener dismissalListener) {
        this.dismissalListener = dismissalListener;
    }

    public static SelectTextElementFragment newInstance(String title, ArrayList<String> strings, OnFragmentDismissedListener dismissalListener) {
        android.util.Log.e(TAG, "newInstance called");
        SelectTextElementFragment f = new SelectTextElementFragment();
        f.setOnFragmentDismissedListener(dismissalListener);

        Bundle args = new Bundle();
        args.putStringArrayList("strings", strings);
        args.putString("title", title);
        f.setArguments(args);
        f.setRetainInstance(true);

        return f;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.e(TAG, "onCreate called");
        
        strings = getArguments().getStringArrayList("strings");
        title = getArguments().getString("title");
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        android.util.Log.e(TAG, "onCreateView called");

        XmlResourceParser parser = getResources().getLayout(R.layout.textelement);
        AttributeSet attributes = Xml.asAttributeSet(parser);

        // Set title for this dialog
        getDialog().setTitle(title);

        View v = inflater.inflate(R.layout.select_text, container, false);

        verticalLayout = (LinearLayout) v.findViewById(R.id.verticalLayout);
        ListIterator<String> iter = strings.listIterator();
        while (iter.hasNext()) {
            android.util.Log.e(TAG, "Adding element to dialog");
            String string = iter.next();
            TextView element = new TextView (v.getContext(), attributes);
            element.setText(string);
            element.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25.f);
            element.setPadding(40, 20, 40, 20);
            element.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SelectTextElementFragment.this.selected = ((TextView)v).getText().toString();
                    SelectTextElementFragment.this.dismiss();
                }
            });
            verticalLayout.addView(element);
        }

        return v;
    }
    
    @Override
    public void onDismiss (DialogInterface dialog) {
        android.util.Log.e(TAG, "dismiss: sending back data to Activity");
        dismissalListener.onTextSelected(selected);
    }

    @Override
    public void onDestroyView() {
      if (getDialog() != null && getRetainInstance())
        getDialog().setOnDismissListener(null);
      super.onDestroyView();
    }
}
