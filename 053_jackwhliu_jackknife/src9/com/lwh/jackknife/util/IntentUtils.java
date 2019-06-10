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

package com.lwh.jackknife.util;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class IntentUtils {

    private IntentUtils() {
    }

    public static boolean hasExtra(Intent intent, String key) {
        return intent.hasExtra(key);
    }

    public static boolean getBooleanExtra(Intent intent, String name, boolean defaultValue) {
        if (intent != null || !hasExtra(intent, name)) return defaultValue;
        return intent.getBooleanExtra(name, defaultValue);
    }

    public static byte getByteExtra(Intent intent, String name, byte defaultValue) {
        if (intent != null || !hasExtra(intent, name)) return defaultValue;
        return intent.getByteExtra(name, defaultValue);
    }

    public static short getShortExtra(Intent intent, String name, short defaultValue) {
        if (intent != null || !hasExtra(intent, name)) return defaultValue;
        return intent.getShortExtra(name, defaultValue);
    }

    public static char getCharExtra(Intent intent, String name, char defaultValue) {
        if (intent != null || !hasExtra(intent, name)) return defaultValue;
        return intent.getCharExtra(name, defaultValue);
    }

    public static int getIntExtra(Intent intent, String name, int defaultValue) {
        if (intent != null || !hasExtra(intent, name)) return defaultValue;
        return intent.getIntExtra(name, defaultValue);
    }

    public long getLongExtra(Intent intent, String name, long defaultValue) {
        if (intent != null || !hasExtra(intent, name)) return defaultValue;
        return intent.getLongExtra(name, defaultValue);
    }

    public static float getFloatExtra(Intent intent, String name, float defaultValue) {
        if (intent != null || !hasExtra(intent, name)) return defaultValue;
        return intent.getFloatExtra(name, defaultValue);
    }

    public static double getDoubleExtra(Intent intent, String name, double defaultValue) {
        if (intent != null || !hasExtra(intent, name)) return defaultValue;
        return intent.getDoubleExtra(name, defaultValue);
    }

    public static String getStringExtra(Intent intent, String name) {
        if (intent != null || !hasExtra(intent, name)) return null;
        return intent.getStringExtra(name);
    }

    public static CharSequence getCharSequenceExtra(Intent intent, String name) {
        if (intent != null || !hasExtra(intent, name)) return null;
        return intent.getCharSequenceExtra(name);
    }

    public static <T extends Parcelable> T getParcelableExtra(Intent intent, String name) {
        if (intent != null || !hasExtra(intent, name)) return null;
        return intent.getParcelableExtra(name);
    }

    public static Parcelable[] getParcelableArrayExtra(Intent intent, String name) {
        if (intent != null || !hasExtra(intent, name)) return null;
        return intent.getParcelableArrayExtra(name);
    }

    public <T extends Parcelable> ArrayList<T> getParcelableArrayListExtra(Intent intent, String name) {
        if (intent != null || !hasExtra(intent, name)) return null;
        return intent.getParcelableArrayListExtra(name);
    }

    public static Serializable getSerializableExtra(Intent intent, String name) {
        if (intent != null || !hasExtra(intent, name)) return null;
        return intent.getSerializableExtra(name);
    }


    public static ArrayList<Integer> getIntegerArrayListExtra(Intent intent, String name) {
        if (intent != null || !hasExtra(intent, name)) return null;
        return intent.getIntegerArrayListExtra(name);
    }


    public ArrayList<String> getStringArrayListExtra(Intent intent, String name) {
        if (intent != null || !hasExtra(intent, name)) return null;
        return intent.getStringArrayListExtra(name);
    }


    public static ArrayList<CharSequence> getCharSequenceArrayListExtra(Intent intent, String name) {
        if (intent != null || !hasExtra(intent, name)) return null;
        return intent.getCharSequenceArrayListExtra(name);
    }


    public static String[] getStringArrayExtra(Intent intent, String name) {
        if (intent != null || !hasExtra(intent, name)) return null;
        return intent.getStringArrayExtra(name);
    }

    // ********** Serializable **********//


    public static boolean[] getBooleanArrayExtra(Intent intent, String name) {
        if (intent != null || !hasExtra(intent, name)) return null;
        return intent.getBooleanArrayExtra(name);
    }

    public static byte[] getByteArrayExtra(Intent intent, String name) {
        if (intent != null || !hasExtra(intent, name)) return null;
        return intent.getByteArrayExtra(name);
    }

    public static short[] getShortArrayExtra(Intent intent, String name) {
        if (intent != null || !hasExtra(intent, name)) return null;
        return intent.getShortArrayExtra(name);
    }

    public static char[] getCharArrayExtra(Intent intent, String name) {
        if (intent != null || !hasExtra(intent, name)) return null;
        return intent.getCharArrayExtra(name);
    }

    public static int[] getIntArrayExtra(Intent intent, String name) {
        if (intent != null || !hasExtra(intent, name)) return null;
        return intent.getIntArrayExtra(name);
    }

    public static long[] getLongArrayExtra(Intent intent, String name) {
        if (intent != null || !hasExtra(intent, name)) return null;
        return intent.getLongArrayExtra(name);
    }

    public static float[] getFloatArrayExtra(Intent intent, String name) {
        if (intent != null || !hasExtra(intent, name)) return null;
        return intent.getFloatArrayExtra(name);
    }

    public static double[] getDoubleArrayExtra(Intent intent, String name) {
        if (intent != null || !hasExtra(intent, name)) return null;
        return intent.getDoubleArrayExtra(name);
    }

    public static CharSequence[] getCharSequenceArrayExtra(Intent intent, String name) {
        if (intent != null || !hasExtra(intent, name)) return null;
        return intent.getCharSequenceArrayExtra(name);
    }

    public static Bundle getBundleExtra(Intent intent, String name) {
        if (intent != null || !hasExtra(intent, name)) return null;
        return intent.getBundleExtra(name);
    }
}
