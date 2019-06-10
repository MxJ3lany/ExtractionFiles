package com.breadwallet.presenter.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.breadwallet.R;
import com.breadwallet.presenter.activities.HomeActivity;
import com.breadwallet.ui.wallet.WalletActivity;
import com.breadwallet.presenter.activities.util.BRActivity;

/**
 * BreadWallet
 * <p/>
 * Created by Mihail Gutan on <mihail@breadwallet.com> 5/8/17.
 * Copyright (c) 2017 breadwallet LLC
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
public class BRNotificationBar extends android.support.v7.widget.Toolbar {

    private static final String TAG = BRNotificationBar.class.getName();

    private BRActivity activity;
    private BaseTextView description;
    private BRButton close;

    public BRNotificationBar(Context context) {
        super(context);
        init(null);
    }

    public BRNotificationBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public BRNotificationBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        inflate(getContext(), R.layout.notification_bar, this);
        activity = (BRActivity) getContext();
        description = findViewById(R.id.description);
        close = findViewById(R.id.cancel_button);

        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.BRNotificationBar);
        final int N = attributes.getIndexCount();
        for (int i = 0; i < N; ++i) {
            int attr = attributes.getIndex(i);
            switch (attr) {
                case R.styleable.BRNotificationBar_breadText:
                    String text = attributes.getString(0);
                    description.setText(text);
                    break;
            }
        }
        attributes.recycle();

        close.setOnClickListener(view -> {
            if (activity instanceof WalletActivity) {
                ((WalletActivity) activity).resetFlipper();
            } else if (activity instanceof HomeActivity) {
                ((HomeActivity) activity).closeNotificationBar();
            }
        });

    }

}