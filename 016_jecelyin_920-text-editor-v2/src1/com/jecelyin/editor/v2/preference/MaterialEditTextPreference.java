/*
 * Copyright (C) 2016 Jecelyin Peng <jecelyin@gmail.com>
 *
 * This file is part of 920 Text Editor.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jecelyin.editor.v2.preference;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDTintHelper;
import com.afollestad.materialdialogs.util.DialogUtils;

import java.lang.reflect.Method;

/**
 * @author Jecelyin Peng <jecelyin@gmail.com>
 */

public class MaterialEditTextPreference extends EditTextPreference {

    private int mColor = 0;
    private MaterialDialog mDialog;
    private EditText mEditText;

    public MaterialEditTextPreference(Context context) {
        super(context);
        init(context, null);
    }

    public MaterialEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MaterialEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MaterialEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setLayoutResource(context, this, attrs);
        int fallback;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            fallback = DialogUtils.resolveColor(context, android.R.attr.colorAccent);
        else fallback = 0;
        fallback = DialogUtils.resolveColor(context, com.afollestad.materialdialogs.commons.R.attr.colorAccent, fallback);
        mColor = DialogUtils.resolveColor(context, com.afollestad.materialdialogs.commons.R.attr.md_widget_color, fallback);

        mEditText = new AppCompatEditText(context, attrs);
        // Give it an ID so it can be saved/restored
        mEditText.setId(android.R.id.edit);
        mEditText.setEnabled(true);
    }

    @Override
    protected void onAddEditTextToDialogView(@NonNull View dialogView, @NonNull EditText editText) {
        ((ViewGroup) dialogView).addView(editText, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onBindDialogView(@NonNull View view) {
        EditText editText = mEditText;
        editText.setText(getText());
        // Initialize cursor to end of text
        if (editText.getText().length() > 0)
            editText.setSelection(editText.length());
        ViewParent oldParent = editText.getParent();
        if (oldParent != view) {
            if (oldParent != null)
                ((ViewGroup) oldParent).removeView(editText);
            onAddEditTextToDialogView(view, editText);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String value = mEditText.getText().toString();
            if (callChangeListener(value))
                setText(value);
        }
    }

    @Override
    public EditText getEditText() {
        return mEditText;
    }

    @Override
    public Dialog getDialog() {
        return mDialog;
    }

    @Override
    protected void showDialog(Bundle state) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(getContext())
                .title(getDialogTitle())
                .icon(getDialogIcon())
                .positiveText(getPositiveButtonText())
                .negativeText(getNegativeButtonText())
                .dismissListener(this)
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        switch (which) {
                            default:
                                MaterialEditTextPreference.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                                break;
                            case NEUTRAL:
                                MaterialEditTextPreference.this.onClick(dialog, DialogInterface.BUTTON_NEUTRAL);
                                break;
                            case NEGATIVE:
                                MaterialEditTextPreference.this.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
                                break;
                        }
                    }
                })
                .dismissListener(this);

        onPrepareDialogBuilder(mBuilder);

        @SuppressLint("InflateParams")
        View layout = LayoutInflater.from(getContext()).inflate(com.afollestad.materialdialogs.commons.R.layout.md_stub_inputpref, null);
        onBindDialogView(layout);

        MDTintHelper.setTint(mEditText, mColor);

        TextView message = (TextView) layout.findViewById(android.R.id.message);
        if (getDialogMessage() != null && getDialogMessage().toString().length() > 0) {
            message.setVisibility(View.VISIBLE);
            message.setText(getDialogMessage());
        } else {
            message.setVisibility(View.GONE);
        }
        mBuilder.customView(layout, false);

        registerOnActivityDestroyListener(this, this);

        mDialog = mBuilder.build();
        if (state != null)
            mDialog.onRestoreInstanceState(state);
        requestInputMethod(mDialog);

        mDialog.show();
    }

    protected void onPrepareDialogBuilder(MaterialDialog.Builder builder) {

    }

    /**
     * Copied from DialogPreference.java
     */
    private void requestInputMethod(Dialog dialog) {
        Window window = dialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        Dialog dialog = getDialog();
        if (dialog == null || !dialog.isShowing()) {
            return superState;
        }

        final MaterialEditTextPreference.SavedState myState = new MaterialEditTextPreference.SavedState(superState);
        myState.isDialogShowing = true;
        myState.dialogBundle = dialog.onSaveInstanceState();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(MaterialEditTextPreference.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        MaterialEditTextPreference.SavedState myState = (MaterialEditTextPreference.SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        if (myState.isDialogShowing) {
            showDialog(myState.dialogBundle);
        }
    }

    // From DialogPreference
    private static class SavedState extends BaseSavedState {
        boolean isDialogShowing;
        Bundle dialogBundle;

        public SavedState(Parcel source) {
            super(source);
            isDialogShowing = source.readInt() == 1;
            dialogBundle = source.readBundle();
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(isDialogShowing ? 1 : 0);
            dest.writeBundle(dialogBundle);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<MaterialEditTextPreference.SavedState> CREATOR =
                new Parcelable.Creator<MaterialEditTextPreference.SavedState>() {
                    public MaterialEditTextPreference.SavedState createFromParcel(Parcel in) {
                        return new MaterialEditTextPreference.SavedState(in);
                    }

                    public MaterialEditTextPreference.SavedState[] newArray(int size) {
                        return new MaterialEditTextPreference.SavedState[size];
                    }
                };
    }

    public static void setLayoutResource(@NonNull Context context, @NonNull Preference preference, @Nullable AttributeSet attrs) {
        boolean foundLayout = false;
        if (attrs != null) {
            for (int i = 0; i < attrs.getAttributeCount(); i++) {
                final String namespace = ((XmlResourceParser) attrs).getAttributeNamespace(0);
                if (namespace.equals("http://schemas.android.com/apk/res/android") &&
                        attrs.getAttributeName(i).equals("layout")) {
                    foundLayout = true;
                    break;
                }
            }
        }

        boolean useStockLayout = false;
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, com.afollestad.materialdialogs.commons.R.styleable.Preference, 0, 0);
            try {
                useStockLayout = a.getBoolean(com.afollestad.materialdialogs.commons.R.styleable.Preference_useStockLayout, false);
            } finally {
                a.recycle();
            }
        }

        if (!foundLayout && !useStockLayout)
            preference.setLayoutResource(com.afollestad.materialdialogs.commons.R.layout.md_preference_custom);
    }

    public static void registerOnActivityDestroyListener(@NonNull Preference preference, @NonNull PreferenceManager.OnActivityDestroyListener listener) {
        try {
            PreferenceManager pm = preference.getPreferenceManager();
            Method method = pm.getClass().getDeclaredMethod(
                    "registerOnActivityDestroyListener",
                    PreferenceManager.OnActivityDestroyListener.class);
            method.setAccessible(true);
            method.invoke(pm, listener);
        } catch (Exception ignored) {
        }
    }
}
