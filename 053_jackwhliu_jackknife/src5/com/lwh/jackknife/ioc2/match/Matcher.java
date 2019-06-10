package com.lwh.jackknife.ioc2.match;

import com.lwh.jackknife.ioc2.Context;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;

public abstract class Matcher {

    protected ProcessingEnvironment mEnv;
    protected Filer mFiler;

    public Matcher(ProcessingEnvironment env, Filer filer) {
        this.mEnv = env;
        this.mFiler = filer;
    }

    public abstract boolean match(String className);

    public abstract Context getContext();
}
