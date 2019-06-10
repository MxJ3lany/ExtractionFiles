package com.lwh.jackknife.ioc2.match;

import com.lwh.jackknife.ioc2.handler.ActivityHandler;
import com.lwh.jackknife.ioc2.Context;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;

public class ActivityMatcher extends Matcher {

    private final String CLASS_ACTIVITY = "android.app.Activity";
    private final String CLASS_APPCOMPAT_ACTIVITY = "android.support.v7.app.AppCompatActivity";

    public ActivityMatcher(ProcessingEnvironment env, Filer filer) {
        super(env, filer);
    }

    @Override
    public boolean match(String className) {
        return className != null && className.equals(CLASS_ACTIVITY) || className.equals(CLASS_APPCOMPAT_ACTIVITY);
    }

    @Override
    public Context getContext() {
        return new ActivityHandler(mEnv, mFiler);
    }
}
