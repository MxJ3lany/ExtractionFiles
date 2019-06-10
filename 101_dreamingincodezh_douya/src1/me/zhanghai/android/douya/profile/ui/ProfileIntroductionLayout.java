/*
 * Copyright (c) 2016 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.douya.profile.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.core.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.douya.R;
import me.zhanghai.android.douya.network.api.info.apiv2.User;
import me.zhanghai.android.douya.ui.FriendlyCardView;
import me.zhanghai.android.douya.util.ViewUtils;

public class ProfileIntroductionLayout extends FriendlyCardView {

    @BindView(R.id.title)
    TextView mTitleText;
    @BindView(R.id.content)
    TextView mContentText;

    private Listener mListener;

    public ProfileIntroductionLayout(Context context) {
        super(context);

        init();
    }

    public ProfileIntroductionLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public ProfileIntroductionLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        ViewUtils.inflateInto(R.layout.profile_introduction_layout, this);
        ButterKnife.bind(this);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void bind(String introduction) {
        introduction = introduction
                // \h requires Java 8.
                //.replaceFirst("^(\\h*\\n)*", "")
                //.replaceFirst("(\\n\\h*)*\\n?$", "");
                .replaceFirst(
                        "^([ \\t\\xA0\\u1680\\u180e\\u2000-\\u200a\\u202f\\u205f\\u3000]*\\n)*",
                        "")
                .replaceFirst(
                        "(\\n[ \\t\\xA0\\u1680\\u180e\\u2000-\\u200a\\u202f\\u205f\\u3000]*)*\\n?$",
                        "");
        if (!TextUtils.isEmpty(introduction)) {
            final String finalIntroduction = introduction;
            mTitleText.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    onCopyText(finalIntroduction);
                }
            });
            Drawable selectableItemBackground = ViewUtils.getDrawableFromAttrRes(
                    R.attr.selectableItemBackground, getContext());
            // ?selectableItemBackground is a nine-patch drawable which reports its padding (of 0).
            boolean shouldSavePadding = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
            if (shouldSavePadding) {
                ViewUtils.setBackgroundPreservingPadding(mTitleText, selectableItemBackground);
            } else {
                ViewCompat.setBackground(mTitleText, selectableItemBackground);
            }
            mContentText.setText(introduction);
        } else {
            mTitleText.setOnClickListener(null);
            mTitleText.setClickable(false);
            ViewCompat.setBackground(mTitleText, null);
            mContentText.setText(R.string.profile_introduction_empty);
        }
    }

    public void bind(User user) {
        bind(user.introduction);
    }

    private void onCopyText(String text) {
        if (mListener != null) {
            mListener.onCopyText(text);
        }
    }

    public interface Listener {
        void onCopyText(String text);
    }
}
