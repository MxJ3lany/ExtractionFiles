/*
 *
 *  * Copyright (C) 2017 The JackKnife Open Source Project
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.lwh.jackknife.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

public class VerifyCodeEditTextGroup extends AutoEditTextGroup<VerifyCodeEditText> {

    private int mLength;

    public VerifyCodeEditTextGroup(Context context) {
        this(context, null);
    }

    public VerifyCodeEditTextGroup(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.verifyCodeEditTextGroupStyle);
    }

    public VerifyCodeEditTextGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        super.initAttrs(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VerifyCodeEditTextGroup,
                defStyleAttr, 0);
        int type = a.getInt(R.styleable.VerifyCodeEditTextGroup_verifycodeedittextgroup_type, 0);
        switch (type) {
            case 0:
                mLength = 4;
                break;
            case 1:
                mLength = 6;
                break;
        }
        a.recycle();
    }

    @Override
    protected VerifyCodeEditText createEditText() {
        VerifyCodeEditText section = new VerifyCodeEditText(getContext());
        LayoutParams lp = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
        section.setLayoutParams(lp);
        section.setTextSize(TypedValue.COMPLEX_UNIT_PX, mSectionTextSize);
        section.setTextColor(Color.GRAY);
        section.setGravity(Gravity.CENTER);
        section.setPadding(mSectionPadding, mSectionPadding, mSectionPadding, mSectionPadding);
        section.setSingleLine();
        section.addInputType(InputType.TYPE_CLASS_NUMBER);
        section.setFocusableInTouchMode(true);
        applyEditTextTheme(section);
        return section;
    }

    @Override
    public String getText() {
        String result = "";
        for (int i = 0; i < mSections.size(); i++) {
            result += mSections.get(i).getText().toString();
        }
        return result;
    }

    @Override
    public int getChildCount() {
        return mLength * 2 - 1;
    }

    @Override
    public String getSemicolonText() {
        return " ";
    }

    @Override
    public int getMaxLength() {
        return 1;
    }

    @Override
    public void applySemicolonTextViewTheme(TextView semicolonTextView) {
        semicolonTextView.setGravity(Gravity.CENTER);
        semicolonTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        semicolonTextView.setPadding(mSemicolonPadding, mSemicolonPadding, mSemicolonPadding,
                mSemicolonPadding);
    }

    @Override
    public void applyEditTextTheme(AutoEditText autoEditText) {
        autoEditText.setBackgroundResource(R.drawable.jknf_edit_text_border);
    }
}
