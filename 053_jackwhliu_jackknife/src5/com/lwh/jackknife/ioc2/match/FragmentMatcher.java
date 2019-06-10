package com.lwh.jackknife.ioc2.match;

import com.lwh.jackknife.ioc2.Context;
import com.lwh.jackknife.ioc2.handler.FragmentHandler;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;

public class FragmentMatcher extends Matcher {

    private final String CLASS_FRAGMENT = "android.app.Fragment";
    private final String CLASS_V4_FRAGMENT = "android.support.v4.app.Fragment";

    public FragmentMatcher(ProcessingEnvironment env, Filer filer) {
        super(env, filer);
    }

    @Override
    public boolean match(String className) {
        return className != null && className.equals(CLASS_FRAGMENT) || className.equals(CLASS_V4_FRAGMENT);
    }

    @Override
    public Context getContext() {
        return new FragmentHandler(mEnv, mFiler);
    }
}
