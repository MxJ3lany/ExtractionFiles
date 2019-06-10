package com.github.omadahealth.lollipin.lib.interfaces;

import android.app.Activity;

/**
 * Created by stoyan on 1/12/15.
 * Allows to follow the LifeCycle of the {@link com.github.omadahealth.lollipin.lib.PinActivity}
 * Implemented by {@link com.github.omadahealth.lollipin.lib.managers.AppLockImpl} in order to
 * determine when the app was launched for the last time and when to launch the
 * {@link com.github.omadahealth.lollipin.lib.managers.AppLockActivity}
 */
public interface LifeCycleInterface {

    /**
     * Called in {@link android.app.Activity#onResume()}
     */
    public void onActivityResumed(Activity activity);

    /**
     * Called in {@link Activity#onUserInteraction()}
     */
    public void onActivityUserInteraction(Activity activity);

    /**
     * Called in {@link android.app.Activity#onPause()}
     */
    public void onActivityPaused(Activity activity);
}
