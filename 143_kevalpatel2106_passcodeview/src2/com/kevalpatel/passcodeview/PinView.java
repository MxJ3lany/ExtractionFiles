/*
 * Copyright 2018 Keval Patel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kevalpatel.passcodeview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.kevalpatel.passcodeview.authenticator.PinAuthenticator;
import com.kevalpatel.passcodeview.indicators.Indicator;
import com.kevalpatel.passcodeview.interfaces.AuthenticationListener;
import com.kevalpatel.passcodeview.internal.BasePasscodeView;
import com.kevalpatel.passcodeview.internal.BoxKeypad;
import com.kevalpatel.passcodeview.internal.BoxTitleIndicator;
import com.kevalpatel.passcodeview.keys.Key;
import com.kevalpatel.passcodeview.keys.KeyNamesBuilder;

import java.util.ArrayList;

/**
 * Created by Keval on 06-Apr-17.
 * <p>
 * This view will perform the PIN based authentication. This view also support fingerprint authentication.
 * To set this view application has to
 * <li>1. Set key shape using {@link #setKey(Key.Builder)}.</li>
 * <li>2. Set the callback listener. {@link #setAuthenticationListener(AuthenticationListener)}</li>
 * <br/>
 * This view is made up of three different views.
 * <li>Title with the PIN indicators. {@link BoxTitleIndicator}</li>
 * <li>Keyboard. {@link BoxKeypad}</li>
 * <li>Fingerprint authentication view.</li>
 *
 *@author <a href="https://github.com/kevalpatel2106">kevalpatel2106</a>
 * @see AuthenticationListener
 */

public final class PinView extends BasePasscodeView implements InteractiveArrayList.ChangeListener {
    public static final int DYNAMIC_PIN_LENGTH = 0;

    /**
     * X coordinate for the touch down event.
     */
    private float mDownKeyX;
    /**
     * Y coordinate for the touch down event.
     */
    private float mDownKeyY;

    /**
     * {@link java.util.ArrayList} of the currently typed pin. At any given time this array will
     * hold the digits of the PIN that user typed.
     *
     * @see InteractiveArrayList
     */
    private InteractiveArrayList<Integer> mPinTyped;                //PIN typed.

    /**
     * {@link BoxKeypad} that displays the numeric keyboard to display the keys.
     */
    private BoxKeypad mBoxKeypad;

    /**
     * Box that will contain title and pin indicators.
     */
    private BoxTitleIndicator mBoxIndicator;

    /**
     * {@link KeyNamesBuilder} that holds the title name of all the keys for keyboard digits.
     */
    @NonNull
    private KeyNamesBuilder mKeyNamesBuilder = new KeyNamesBuilder();

    /**
     * {@link PinAuthenticator} that will perform the authentication for the user pin.
     *
     * @see PinAuthenticator
     */
    private volatile PinAuthenticator mAuthenticator;

    /**
     * Instance of the currently running {@link PinAuthenticatorTask}. If the value is null that indicates,
     * no {@link PinAuthenticatorTask} is running currently.
     *
     * @see PinAuthenticatorTask
     */
    @Nullable
    private PinAuthenticatorTask mPinAuthenticatorTask;

    ///////////////////////////////////////////////////////////////
    //                  CONSTRUCTORS
    ///////////////////////////////////////////////////////////////

    public PinView(Context context) {
        super(context);
    }

    public PinView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PinView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    ///////////////////////////////////////////////////////////////
    //                  SET THEME PARAMS INITIALIZE
    ///////////////////////////////////////////////////////////////

    /**
     * Initialize view. This will initialize the view boxes.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void init() {
        //Set the typed pin numbers holder.
        mPinTyped = new InteractiveArrayList<>();
        mPinTyped.setChangeListener(this);

        //Set the keyboard box.
        mBoxKeypad = new BoxKeypad(this);
        mBoxKeypad.init();

        //Set the title/PIN indicator box.
        mBoxIndicator = new BoxTitleIndicator(this);
        mBoxIndicator.init();
    }

    /**
     * Set default parameters if the theme is not set.
     */
    @Override
    public void setDefaults() {
        mBoxIndicator.setDefaults();
        mBoxKeypad.setDefaults();
    }

    /**
     * Prepare all the required pain objects.
     */
    @Override
    public void preparePaint() {
        //Prepare paints.
        mBoxKeypad.preparePaint();
        mBoxIndicator.preparePaint();
    }

    /**
     * Parse the theme attribute using the parse array.
     *
     * @param typedArray {@link AttributeSet} received from the XML.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void parseTypeArr(@NonNull AttributeSet typedArray) {
        //Do nothing
    }

    @Override
    public void measureView(@NonNull Rect rootViewBounds) {
        mBoxKeypad.measureView(mRootViewBound);
        mBoxIndicator.measureView(mRootViewBound);
    }

    @Override
    public void onAuthenticationFail() {
        //Notify all the boxes for authentication success.
        mBoxKeypad.onAuthenticationFail();
        mBoxIndicator.onAuthenticationFail();
        super.onAuthenticationFail();
    }

    @Override
    public void onAuthenticationSuccess() {
        //Notify all the boxes for authentication success.
        mBoxKeypad.onAuthenticationSuccess();
        mBoxIndicator.onAuthenticationSuccess();
        super.onAuthenticationSuccess();
    }

    /**
     * Reset the pin code and view state.
     */
    @Override
    public void reset() {
        super.reset();
        mPinTyped.clear();
        mBoxKeypad.reset();
        mBoxIndicator.reset();
        invalidate();
    }

    /**
     * Draw method of the view called every time frame refreshes.
     *
     * @param canvas view canvas
     */
    @Override
    public void drawView(@NonNull Canvas canvas) {
        mBoxKeypad.drawView(canvas);
        mBoxIndicator.drawView(canvas);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mPinAuthenticatorTask != null) mPinAuthenticatorTask.cancel(true);
    }

    ///////////////////////////////////////////////////////////////
    //                  TOUCH HANDLER
    ///////////////////////////////////////////////////////////////

    /**
     * Handle touch event.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDownKeyX = event.getX();
                mDownKeyY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                onKeyPressed(mBoxKeypad.findKeyPressed(mDownKeyX,
                        mDownKeyY,
                        event.getX(),
                        event.getY()));
                break;
            default:
                return false;
        }
        return true;
    }

    /**
     * Handle the newly added key digit. Append the digit to {@link #mPinTyped}.
     * If the new digit is {@link KeyNamesBuilder#BACKSPACE_TITLE}, remove the last digit of the {@link #mPinTyped}.
     *
     * @param newDigit newly pressed digit
     */
    private void onKeyPressed(@Nullable String newDigit) {
        if (newDigit == null) return;

        //Check for the state
        if (mAuthenticationListener == null) {
            throw new IllegalStateException("Set AuthenticationListener to receive callbacks.");
        }

        if (newDigit.equals(KeyNamesBuilder.BACKSPACE_TITLE)) { //Back space key is pressed.
            if (mPinTyped.size() > 0) mPinTyped.remove(mPinTyped.size() - 1);   //Remove last digit.
        } else {

            //Add new digit
            mPinTyped.add(mKeyNamesBuilder.getValueOfKey(newDigit));
        }

        invalidate();

        if (isDynamicPinEnabled() || mPinTyped.size() == mBoxIndicator.getPinLength()) {
            if (mPinAuthenticatorTask != null && mPinAuthenticatorTask.getStatus() == AsyncTask.Status.RUNNING)
                mPinAuthenticatorTask.cancel(true);

            mPinAuthenticatorTask = new PinAuthenticatorTask(mAuthenticator);
            //noinspection unchecked
            mPinAuthenticatorTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mPinTyped);
        } else {
            giveTactileFeedbackForKeyPress();
        }

    }

    /**
     * This method will be called when there is any change in {@link #mPinTyped}.
     *
     * @param size this is the new size of {@link #mPinTyped}.
     * @see InteractiveArrayList
     */
    @Override
    public void onArrayValueChange(int size) {
        mBoxIndicator.onPinDigitEntered(size);
        if (isDynamicPinEnabled()) mBoxIndicator.measureView(mRootViewBound);
    }

    ///////////////////////////////////////////////////////////////
    //                  GETTERS/SETTERS
    ///////////////////////////////////////////////////////////////

    @Nullable
    public PinAuthenticator getPinAuthenticator() {
        return mAuthenticator;
    }

    public void setPinAuthenticator(final PinAuthenticator authenticator) {
        mAuthenticator = authenticator;
    }

    /**
     * Get the currently typed PIN numbers.
     *
     * @return Array of PIN digits.
     */
    public int[] getCurrentTypedPin() {
        int[] arr = new int[mPinTyped.size()];
        for (int i = 0; i < mPinTyped.size(); i++) arr[i] = mPinTyped.get(i);
        return arr;
    }

    /**
     * Set the currently typed PIN numbers.
     *
     * @param currentTypedPin Array of PIN digits.
     */
    public void setCurrentTypedPin(final int[] currentTypedPin) {
        //Add the pin to pin typed
        mPinTyped.clear();
        for (int i : currentTypedPin) mPinTyped.add(i);

        requestLayout();
        invalidate();
    }

    public void setPinLength(final int pinLength) {
        mBoxIndicator.setPinLength(pinLength);
    }

    public int getPinLength() {
        return mBoxIndicator.getPinLength();
    }

    public boolean isDynamicPinEnabled() {
        return mBoxIndicator.getPinLength() == PinView.DYNAMIC_PIN_LENGTH;
    }

    //********************** For keyboard box

    /**
     * @return true if the one hand operation is enabled.
     */
    public boolean isOneHandOperationEnabled() {
        return mBoxKeypad.isOneHandOperation();
    }

    /**
     * Enable/Disable the one hand operation. One hand operation  will shrink the keypad to 70% of the
     * original width and stick it to the right edge of the screen. This will allow user to press the
     * key without using both hands on large screen devices.(e.g. Phablets)
     *
     * @param isEnable true to enable one hand mode.
     */
    public void enableOneHandOperation(final boolean isEnable) {
        mBoxKeypad.setOneHandOperation(isEnable);
        requestLayout();
        invalidate();
    }

    /**
     * Set the key shape and theme properties by using {@link com.kevalpatel.passcodeview.keys.Key.Builder}.
     * https://github.com/kevalpatel2106/PasscodeView/wiki/Diffrent-Key-Shapes
     *
     * @param keyBuilder {@link com.kevalpatel.passcodeview.keys.Key.Builder}
     */
    public void setKey(@NonNull final Key.Builder keyBuilder) {
        mBoxKeypad.setKeyBuilder(keyBuilder);
        requestLayout();
        invalidate();
    }

    /**
     * Set the name of the keys. So that you can support different locale.
     * https://github.com/kevalpatel2106/PasscodeView/wiki/Add-localized-key-names
     *
     * @param keyNamesBuilder {@link KeyNamesBuilder}
     */
    public void setKeyNames(@NonNull final KeyNamesBuilder keyNamesBuilder) {
        mKeyNamesBuilder = keyNamesBuilder;
        mBoxKeypad.setKeyNames(keyNamesBuilder);

        mPinTyped.clear(); //Need to clear the typed pin, so that change in localization don't affect the pin matching process.

        requestLayout();
        invalidate();
    }

    //********************** For title and indicator box

    /**
     * @return Title color of the view.
     */
    @ColorInt
    public int getTitleColor() {
        return mBoxIndicator.getTitleColor();
    }

    /**
     * @return {@link com.kevalpatel.passcodeview.keys.Key.Builder}
     */
    @Nullable
    public Key.Builder getKeyBuilder() {
        return mBoxKeypad.getKeyBuilder();
    }

    /**
     * Set the color of the view title.
     *
     * @param titleColor Color of the title.
     */
    public void setTitleColor(@ColorInt final int titleColor) {
        mBoxIndicator.setTitleColor(titleColor);
        invalidate();
    }

    /**
     * Set the color of the view title.
     *
     * @param titleColor Color of the title.
     */
    public void setTitleColorResource(@ColorRes final int titleColor) {
        mBoxIndicator.setTitleColor(getResources().getColor(titleColor));
        invalidate();
    }

    /**
     * @return Current title of the view.
     */
    public String getTitle() {
        return mBoxIndicator.getTitle();
    }

    /**
     * Set the title at the top of view.
     *
     * @param title title string
     */
    public void setTitle(@NonNull final String title) {
        mBoxIndicator.setTitle(title);
        invalidate();
    }

    /**
     * Set the PIN change indicator. Use {@link com.kevalpatel.passcodeview.indicators.Indicator.Builder}
     * to use different indicators.
     *
     * @param indicatorBuilder {@link com.kevalpatel.passcodeview.indicators.Indicator.Builder}
     */
    public void setIndicator(@NonNull final Indicator.Builder indicatorBuilder) {
        mBoxIndicator.setIndicatorBuilder(indicatorBuilder);
        requestLayout();
        invalidate();
    }

    /**
     * @return {@link com.kevalpatel.passcodeview.indicators.Indicator.Builder}
     */
    public Indicator.Builder getIndicatorBuilder() {
        return mBoxIndicator.getIndicatorBuilder();
    }

    @SuppressLint("StaticFieldLeak")
    private final class PinAuthenticatorTask extends AsyncTask<ArrayList<Integer>, Void, PinAuthenticator.PinAuthenticationState> {

        @NonNull
        private final PinAuthenticator mAuthenticator;

        @NonNull
        private final Handler mHandler;

        @NonNull
        private final Runnable mResetRunnable;

        private PinAuthenticatorTask(@NonNull final PinAuthenticator authenticator) {
            mAuthenticator = authenticator;
            mHandler = new Handler(Looper.getMainLooper());
            mResetRunnable = new Runnable() {
                @Override
                public void run() {
                    reset();
                }
            };
        }

        @SafeVarargs
        @Override
        protected final PinAuthenticator.PinAuthenticationState doInBackground(final ArrayList<Integer>... pinTyped) {
            return this.mAuthenticator.isValidPin(pinTyped[0]);
        }

        @Override
        protected void onPostExecute(final PinAuthenticator.PinAuthenticationState authenticationState) {
            super.onPostExecute(authenticationState);

            if (authenticationState == PinAuthenticator.PinAuthenticationState.NEED_MORE_DIGIT) {
                //This is just a key press.
                giveTactileFeedbackForKeyPress();
                return;
            } else if (authenticationState == PinAuthenticator.PinAuthenticationState.SUCCESS) {
                //Hurray!!! Authentication is successful.
                onAuthenticationSuccess();
            } else if (authenticationState == PinAuthenticator.PinAuthenticationState.FAIL) {
                //:-( Authentication failed.
                onAuthenticationFail();
            }

            //Reset the view.
            mHandler.postDelayed(mResetRunnable, 350);
            mPinAuthenticatorTask = null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mHandler.removeCallbacks(mResetRunnable);
            mPinAuthenticatorTask = null;
        }
    }
}
