package com.applozic.mobicomkit.uiwidgets.uikit;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.widget.LinearLayout;


/**
 * Created by ashish on 14/05/18.
 */

public class ApplozicComponents extends LinearLayout {

    public ApplozicComponents(Context context) {
        super(context);
    }

    public ApplozicComponents(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ApplozicComponents(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    public AppCompatActivity getAppCompatActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof AppCompatActivity) {
                return (AppCompatActivity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}
