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

package com.lwh.jackknife.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lwh.jackknife.widget.R;

public abstract class BaseDialog extends Dialog {

    protected Context mContext;
    protected OnClickListener mOnConfirmListener;
    protected OnClickListener mOnCancelListener;
    protected OnDismissListener mOnDismissListener;
    protected View mView;
    protected Button mPositiveButton;
    protected Button mNegativeButton;
    private boolean mFullScreen;
    private boolean mHasTitle = true;
    Toast mToast;
    private int mWidth;
    private int mHeight;
    private int mX;
    private int mY;
    private int mTitleIcon = 0;
    private String mTitle;
    private String mMessage;
    private String mPositiveLabel;
    private String mNegativeLabel;
    private boolean mCancel;

    public BaseDialog(Context context) {
        super(context, R.style.JackKnife_Theme_Widget_BaseDialog);
        this.mContext = context;
    }

    public boolean isCancel() {
        return mCancel;
    }

    public void setCancel(boolean isCancel) {
        this.mCancel = isCancel;
    }

    public void toast(String text) {
        if (mToast == null) {
            mToast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
        }
        mToast.show();
    }

    public int dp2px(float value) {
        final float scale = getContext().getResources().getDisplayMetrics().densityDpi;
        return (int) (value * (scale / 160) + 0.5f);
    }

    public int sp2px(float value) {
        float scale = mContext.getResources().getDisplayMetrics().scaledDensity;
        return (int) (value * scale + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        View view = onCreateView(this);
        setView(view);
        if (title() != null && !title().equals("")) {
            setTitle(title());
        }
        if (positiveLabel() != null && !positiveLabel().equals("")) {
            setPositiveLabel(positiveLabel());
        }
        if (negativeLabel() != null && !negativeLabel().equals("")) {
            setNegativeLabel(negativeLabel());
        }
    }

    protected abstract String title();

    protected abstract String positiveLabel();

    protected abstract String negativeLabel();

    protected abstract View onCreateView(BaseDialog dialog);

    private void init() {
        Context context = getContext();
        LinearLayout dialogLayout = new LinearLayout(context);
        dialogLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setMinimumWidth(dp2px(200));
        dialogLayout.setBackgroundColor(Color.WHITE);
        LinearLayout topLayout = new LinearLayout(context);
        topLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, dp2px(45)));
        topLayout.setGravity(Gravity.CENTER_VERTICAL);
        TextView titleTextView = new TextView(context);
        titleTextView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        titleTextView.setText(getTitle());
        titleTextView.setTextColor(0xFF00CB7E);
        titleTextView.setTextSize(18);
        titleTextView.setEllipsize(TruncateAt.END);
        titleTextView.setPadding(dp2px(10), 0, dp2px(10), 0);
        topLayout.addView(titleTextView);
        dialogLayout.addView(topLayout);
        View view = new View(context);
        view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, dp2px(0.2f)));
        view.setBackgroundColor(0xFFCACACA);
        if (mHasTitle) {
            topLayout.setVisibility(View.VISIBLE);
            view.setVisibility(View.VISIBLE);
        } else {
            topLayout.setVisibility(View.GONE);
            view.setVisibility(View.GONE);
        }
        RelativeLayout layoutContainer = new RelativeLayout(context);
        layoutContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        TextView messageTextView = new TextView(context);
        messageTextView.setText(getMessage());
        messageTextView.setTextColor(Color.GRAY);
        if (mView != null) {
            layoutContainer.addView(mView);
        } else {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.topMargin = dp2px(40);
            params.bottomMargin = dp2px(40);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            layoutContainer.addView(messageTextView, params);
        }
        LinearLayout bottomLayout = new LinearLayout(context);
        bottomLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, dp2px(40)));
        bottomLayout.setOrientation(LinearLayout.HORIZONTAL);
        mPositiveButton = new Button(context);
        mPositiveButton.setLayoutParams(new LayoutParams(0, LayoutParams.MATCH_PARENT, 1));
        mPositiveButton.setBackgroundResource(R.drawable.jknf_base_dialog_bottom_button);
        mNegativeButton = new Button(context);
        mNegativeButton.setLayoutParams(new LayoutParams(0, LayoutParams.MATCH_PARENT, 1));
        mNegativeButton.setBackgroundResource(R.drawable.jknf_base_dialog_bottom_button);
        if (mPositiveLabel != null && mPositiveLabel.length() > 0) {
            mPositiveButton.setText(mPositiveLabel);
            mPositiveButton.setTextColor(0xFF00CB7E);
        } else {
            mPositiveButton.setVisibility(View.GONE);
        }
        if (mNegativeLabel != null && mNegativeLabel.length() > 0) {
            mNegativeButton.setText(mNegativeLabel);
        } else {
            mNegativeButton.setVisibility(View.GONE);
        }
        View view2 = new View(context);
        view2.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, dp2px(0.5f)));
        view2.setBackgroundColor(0xFFCACACA);
        dialogLayout.addView(view);
        View view3 = new View(context);
        view3.setLayoutParams(new LayoutParams(dp2px(0.5f), LayoutParams.MATCH_PARENT));
        view3.setBackgroundColor(0xFFCACACA);
        dialogLayout.addView(layoutContainer);
        dialogLayout.addView(view2);
        bottomLayout.addView(mNegativeButton);
        bottomLayout.addView(view3);
        bottomLayout.addView(mPositiveButton);
        dialogLayout.addView(bottomLayout);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        if (getWidth() > 0)
            params.width = getWidth();
        if (getHeight() > 0)
            params.height = getHeight();
        if (getX() > 0)
            params.width = getX();
        if (getY() > 0)
            params.height = getY();
        if (mFullScreen) {
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
        }
        if (mCancel) {
            setCanceledOnTouchOutside(true);
            setCancelable(true);
        } else {
            setCanceledOnTouchOutside(false);
            setCancelable(false);
        }
        getWindow().setAttributes(params);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContentView(dialogLayout);
    }

    public void setOnConfirmListener(OnClickListener listener) {
        mOnConfirmListener = listener;
    }

    public void setOnDismissListener(OnDismissListener listener) {
        mOnDismissListener = listener;
    }

    public void setOnCancelListener(OnClickListener listener) {
        mOnCancelListener = listener;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public int getTitleIcon() {
        return mTitleIcon;
    }

    public void setTitleIcon(int titleIcon) {
        this.mTitleIcon = titleIcon;
    }

    public int getIconTitle() {
        return mTitleIcon;
    }

    protected String getMessage() {
        return mMessage;
    }

    protected void setMessage(String message) {
        this.mMessage = message;
    }

    protected View getView() {
        return mView;
    }

    protected void setView(View view) {
        this.mView = view;
    }

    public boolean isFullScreen() {
        return mFullScreen;
    }

    public void setFullScreen(boolean isFullScreen) {
        this.mFullScreen = isFullScreen;
    }

    public boolean isHasTitle() {
        return mHasTitle;
    }

    public void setHasTitle(boolean hasTitle) {
        this.mHasTitle = hasTitle;
    }

    protected int getWidth() {
        return mWidth;
    }

    protected void setWidth(int width) {
        this.mWidth = width;
    }

    protected int getHeight() {
        return mHeight;
    }

    protected void setHeight(int height) {
        this.mHeight = height;
    }

    public int getX() {
        return mX;
    }

    public void setX(int x) {
        this.mX = x;
    }

    public int getY() {
        return mY;
    }

    public void setY(int y) {
        this.mY = y;
    }

    public String getPositiveLabel() {
        return mPositiveLabel;
    }

    public void setPositiveLabel(String positiveLabel) {
        this.mPositiveLabel = positiveLabel;
    }

    public String getNegativeLabel() {
        return mNegativeLabel;
    }

    public void setNegativeLabel(String negativeLabel) {
        this.mNegativeLabel = negativeLabel;
    }
}