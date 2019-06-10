/*
 * Copyright (C) 2018 The JackKnife Open Source Project
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

package com.lwh.jackknife.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public abstract class AutoEditTextGroup<E extends AutoEditText> extends LinearLayout
        implements TextWatcher {

    protected List<E> mSections;
    protected float mSectionTextSize;
    protected float mSemicolonTextSize;
    protected int mSectionPadding;
    protected int mSemicolonPadding;

    public AutoEditTextGroup(Context context) {
        this(context, null);
    }

    public AutoEditTextGroup(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.autoEditTextGroupStyle);
    }

    public AutoEditTextGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    protected void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mSections = new ArrayList<>();
        initAttrs(context, attrs, defStyleAttr);
        initViews();
        initListeners();
    }

    protected void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AutoEditTextGroup, defStyleAttr, 0);
        mSectionTextSize = a.getDimension(R.styleable.AutoEditTextGroup_autoedittextgroup_sectionTextSize,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 5, getResources().getDisplayMetrics()));
        mSemicolonTextSize = a.getDimension(R.styleable.AutoEditTextGroup_autoedittextgroup_semicolonTextSize,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 5, getResources().getDisplayMetrics()));
        mSemicolonPadding = a.getDimensionPixelOffset(R.styleable.AutoEditTextGroup_autoedittextgroup_sectionPadding,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()));
        mSectionPadding = a.getDimensionPixelOffset(R.styleable.AutoEditTextGroup_autoedittextgroup_semicolonPadding,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
        a.recycle();
    }

    public boolean checkInputValue(E... editTexts) {
        boolean result = true;
        for (int i = 0; i < editTexts.length - 1; i++) {
            if (!editTexts[i].checkInputValue()) {
                result = false;
                break;
            }
        }
        return result;
    }

    public String getText() {
        String result = "";
        for (int i = 0; i < mSections.size(); i++) {
            result += mSections.get(i).getText().toString();
            if (i != mSections.size() - 1) {
                result += getSemicolonText();
            }
        }
        return result;
    }

    public E getSectionAt(int position) {
        int size = mSections.size();
        if (position >= 0 && position < size) {
            return mSections.get(position);
        }
        return null;
    }

    public void clearText() {
        for (int i = 0; i < mSections.size(); i++) {
            E section = getSectionAt(i);
            if (section != null) {
                section.setText("");
                section.setSelection(0);
            }
        }
    }

    public void setText(String[] sectionTexts) {
        if (sectionTexts == null || sectionTexts.length == 0) {
            return;
        }
        clearText();
        int size = mSections.size();
        for (int i = 0; i < sectionTexts.length; i++) {
            if (i < size) {
                E section = getSectionAt(i);
                if (section != null) {
                    if (!hasFocus()) {
                        section.requestFocus();
                    }
                    section.setText(sectionTexts[i]);
                    section.setSelection(sectionTexts[i].length());
                }
            }
        }
    }

    protected abstract E createEditText();

    private class OnDelKeyListener implements OnKeyListener {

        private E mRequestEditText;
        private E mClearEditText;

        public OnDelKeyListener(E request, E clear) {
            this.mRequestEditText = request;
            this.mClearEditText = clear;
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_UP &&
                    mClearEditText.getText().toString().trim().equals("")) {
                if (mClearEditText.hasFocus()) {
                    mClearEditText.clearFocus();
                    mRequestEditText.requestFocus();
                }
                return true;
            }
            return false;
        }
    }

    public abstract int getChildCount();

    public abstract String getSemicolonText();

    public abstract int getMaxLength();

    public abstract void applySemicolonTextViewTheme(TextView semicolonTextView);

    public abstract void applyEditTextTheme(AutoEditText absEditText);

    private void initListeners() {
        for (int i = 0; i < mSections.size(); i++) {
            mSections.get(i).addTextChangedListener(this);
            if (i != 0) {
                mSections.get(i).setOnKeyListener(new OnDelKeyListener(mSections.get(i - 1), mSections.get(i)));
            }
        }
    }

    protected void initViews() {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            if (i % 2 == 0) {
                E section = createEditText();
                addView(section);
                mSections.add(section);
            } else {
                addView(createSemicolonTextView());
            }
        }
    }

    private View createSemicolonTextView() {
        TextView textView = new TextView(getContext());
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(lp);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mSemicolonTextSize);
        textView.setTextColor(getResources().getColor(R.color.gray));
        textView.setText(getSemicolonText());
        applySemicolonTextViewTheme(textView);
        return textView;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        int maxLength = getMaxLength();
        int length = s.toString().length();
        if (maxLength == length) {
            for (int i = 0; i < mSections.size() - 1; i++) {
                if (mSections.get(i).hasFocus()) {//hasFocus √ & isFocus ×
                    mSections.get(i).clearFocus();
                    mSections.get(i + 1).requestFocus();
                    break;
                }
            }
        }
    }
}
