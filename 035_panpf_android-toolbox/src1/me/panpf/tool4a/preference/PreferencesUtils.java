/*
 * Copyright (C) 2017 Peng fei Pan <sky@panpf.me>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.panpf.tool4a.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Set;

import me.panpf.tool4j.lang.StringUtils;

/**
 * Preference工具箱
 */
public class PreferencesUtils {
    /**
     * 保存一个boolean型的值到指定的Preference中
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param mode            模式、类型
     * @param key             键
     * @param value           值
     */
    public static void putBoolean(Context context, String preferencesName, int mode, String key, boolean value) {
        context.getSharedPreferences(preferencesName, mode).edit().putBoolean(key, value).commit();
    }

    /**
     * 保存一个boolean型的值到指定的Preference中
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param key             键
     * @param value           值
     */
    public static void putBoolean(Context context, String preferencesName, String key, boolean value) {
        context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE).edit().putBoolean(key, value).commit();
    }

    /**
     * 保存一个boolean型的值到默认的Preference中
     *
     * @param context 上下文
     * @param key     键
     * @param value   值
     */
    public static void putBoolean(Context context, String key, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key, value).commit();
    }

    /**
     * 从指定的Preference中取出一个boolean型的值
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param mode            模式、类型
     * @param key             键
     * @param defaultValue    默认值
     * @return 值
     */
    public static boolean getBoolean(Context context, String preferencesName, int mode, String key, boolean defaultValue) {
        return context.getSharedPreferences(preferencesName, mode).getBoolean(key, defaultValue);
    }

    /**
     * 从指定的Preference中取出一个boolean型的值
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param key             键
     * @param defaultValue    默认值
     * @return 值
     */
    public static boolean getBoolean(Context context, String preferencesName, String key, boolean defaultValue) {
        return context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE).getBoolean(key, defaultValue);
    }

    /**
     * 从指定的Preference中取出一个boolean型的值，默认值为false
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param key             键
     * @return 值
     */
    public static boolean getBoolean(Context context, String preferencesName, String key) {
        return context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE).getBoolean(key, false);
    }

    /**
     * 从默认的Preference中取出一个boolean型的值
     *
     * @param context      上下文
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defaultValue);
    }

    /**
     * 从默认的Preference中取出一个boolean型的值，默认值为false
     *
     * @param context 上下文
     * @param key     键
     * @return 值
     */
    public static boolean getBoolean(Context context, String key) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, false);
    }

    /**
     * 保存一个float型的值到指定的Preference中
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param mode            模式、类型
     * @param key             键
     * @param value           值
     */
    public static void putFloat(Context context, String preferencesName, int mode, String key, float value) {
        context.getSharedPreferences(preferencesName, mode).edit().putFloat(key, value).commit();
    }

    /**
     * 保存一个float型的值到指定的Preference中
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param key             键
     * @param value           值
     */
    public static void putFloat(Context context, String preferencesName, String key, float value) {
        context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE).edit().putFloat(key, value).commit();
    }

    /**
     * 保存一个float型的值到默认的Preference中
     *
     * @param context 上下文
     * @param key     键
     * @param value   值
     */
    public static void putFloat(Context context, String key, float value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putFloat(key, value).commit();
    }

    /**
     * 从指定的Preference中取出一个float型的值
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param mode            模式、类型
     * @param key             键
     * @param defaultValue    默认值
     * @return 值
     */
    public static float getFloat(Context context, String preferencesName, int mode, String key, float defaultValue) {
        return context.getSharedPreferences(preferencesName, mode).getFloat(key, defaultValue);
    }

    /**
     * 从指定的Preference中取出一个float型的值
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param key             键
     * @param defaultValue    默认值
     * @return 值
     */
    public static float getFloat(Context context, String preferencesName, String key, float defaultValue) {
        return context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE).getFloat(key, defaultValue);
    }

    /**
     * 从指定的Preference中取出一个float型的值，默认值为0
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param key             键
     * @return 值
     */
    public static float getFloat(Context context, String preferencesName, String key) {
        return context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE).getFloat(key, 0);
    }

    /**
     * 从默认的Preference中取出一个float型的值
     *
     * @param context      上下文
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public static float getFloat(Context context, String key, float defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getFloat(key, defaultValue);
    }

    /**
     * 从默认的Preference中取出一个float型的值，默认值为0
     *
     * @param context 上下文
     * @param key     键
     * @return 值
     */
    public static float getFloat(Context context, String key) {
        return PreferenceManager.getDefaultSharedPreferences(context).getFloat(key, 0);
    }


    /**
     * 保存一个int型的值到指定的Preference中
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param mode            模式、类型
     * @param key             键
     * @param value           值
     */
    public static void putInt(Context context, String preferencesName, int mode, String key, int value) {
        context.getSharedPreferences(preferencesName, mode).edit().putInt(key, value).commit();
    }

    /**
     * 保存一个int型的值到指定的Preference中
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param key             键
     * @param value           值
     */
    public static void putInt(Context context, String preferencesName, String key, int value) {
        context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE).edit().putInt(key, value).commit();
    }

    /**
     * 保存一个int型的值到默认的Preference中
     *
     * @param context 上下文
     * @param key     键
     * @param value   值
     */
    public static void putInt(Context context, String key, int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(key, value).commit();
    }

    /**
     * 从指定的Preference中取出一个int型的值
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param mode            模式、类型
     * @param key             键
     * @param defaultValue    默认值
     * @return 值
     */
    public static int getInt(Context context, String preferencesName, int mode, String key, int defaultValue) {
        return context.getSharedPreferences(preferencesName, mode).getInt(key, defaultValue);
    }

    /**
     * 从指定的Preference中取出一个int型的值
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param key             键
     * @param defaultValue    默认值
     * @return 值
     */
    public static int getInt(Context context, String preferencesName, String key, int defaultValue) {
        return context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE).getInt(key, defaultValue);
    }

    /**
     * 从指定的Preference中取出一个int型的值，默认值为0
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param key             键
     * @return 值
     */
    public static int getInt(Context context, String preferencesName, String key) {
        return context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE).getInt(key, 0);
    }

    /**
     * 从默认的Preference中取出一个int型的值
     *
     * @param context      上下文
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public static int getInt(Context context, String key, int defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(key, defaultValue);
    }

    /**
     * 从默认的Preference中取出一个int型的值，默认值为0
     *
     * @param context 上下文
     * @param key     键
     * @return 值
     */
    public static int getInt(Context context, String key) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(key, 0);
    }

    /**
     * 保存一个long型的值到指定的Preference中
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param mode            模式、类型
     * @param key             键
     * @param value           值
     */
    public static void putLong(Context context, String preferencesName, int mode, String key, long value) {
        context.getSharedPreferences(preferencesName, mode).edit().putLong(key, value).commit();
    }

    /**
     * 保存一个long型的值到指定的Preference中
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param key             键
     * @param value           值
     */
    public static void putLong(Context context, String preferencesName, String key, long value) {
        context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE).edit().putLong(key, value).commit();
    }

    /**
     * 保存一个long型的值到默认的Preference中
     *
     * @param context 上下文
     * @param key     键
     * @param value   值
     */
    public static void putLong(Context context, String key, long value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(key, value).commit();
    }

    /**
     * 从指定的Preference中取出一个long型的值
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param mode            模式、类型
     * @param key             键
     * @param defaultValue    默认值
     * @return 值
     */
    public static long getLong(Context context, String preferencesName, int mode, String key, long defaultValue) {
        return context.getSharedPreferences(preferencesName, mode).getLong(key, defaultValue);
    }

    /**
     * 从指定的Preference中取出一个long型的值
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param key             键
     * @param defaultValue    默认值
     * @return 值
     */
    public static long getLong(Context context, String preferencesName, String key, long defaultValue) {
        return context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE).getLong(key, defaultValue);
    }

    /**
     * 从指定的Preference中取出一个long型的值，默认值为0
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param key             键
     * @return 值
     */
    public static long getLong(Context context, String preferencesName, String key) {
        return context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE).getLong(key, 0);
    }

    /**
     * 从默认的Preference中取出一个long型的值
     *
     * @param context      上下文
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public static long getLong(Context context, String key, long defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(key, defaultValue);
    }

    /**
     * 从默认的Preference中取出一个long型的值，默认值为0
     *
     * @param context 上下文
     * @param key     键
     * @return 值
     */
    public static long getLong(Context context, String key) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(key, 0);
    }

    /**
     * 保存一个String型的值到指定的Preference中
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param mode            模式、类型
     * @param key             键
     * @param value           值
     */
    public static void putString(Context context, String preferencesName, int mode, String key, String value) {
        context.getSharedPreferences(preferencesName, mode).edit().putString(key, value).commit();
    }

    /**
     * 保存一个String型的值到指定的Preference中
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param key             键
     * @param value           值
     */
    public static void putString(Context context, String preferencesName, String key, String value) {
        context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE).edit().putString(key, value).commit();
    }

    /**
     * 保存一个String型的值到默认的Preference中
     *
     * @param context 上下文
     * @param key     键
     * @param value   值
     */
    public static void putString(Context context, String key, String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value).commit();
    }

    /**
     * 从指定的Preference中取出一个String型的值
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param mode            模式、类型
     * @param key             键
     * @param defaultValue    默认值
     * @return 值
     */
    public static String getString(Context context, String preferencesName, int mode, String key, String defaultValue) {
        return context.getSharedPreferences(preferencesName, mode).getString(key, defaultValue);
    }

    /**
     * 从指定的Preference中取出一个String型的值
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param key             键
     * @param defaultValue    默认值
     * @return 值
     */
    public static String getString(Context context, String preferencesName, String key, String defaultValue) {
        return context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE).getString(key, defaultValue);
    }

    /**
     * 从默认的Preference中取出一个String型的值
     *
     * @param context      上下文
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public static String getString(Context context, String key, String defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defaultValue);
    }

    /**
     * 从默认的Preference中取出一个String型的值，默认值为0
     *
     * @param context 上下文
     * @param key     键
     * @return 值
     */
    public static String getString(Context context, String key) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, null);
    }

    /**
     * 保存一个Set<String>到指定的Preference中，如果当前系统的SDK版本小于11，则会将Set<String>转换成JSON字符串保存
     *
     * @param preferences Preferences
     * @param key         键
     * @param value       值
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void putStringSet(SharedPreferences preferences, String key, Set<String> value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            preferences.edit().putStringSet(key, value).commit();
        } else {
            putObject(preferences, key, value);
        }
    }

    /**
     * 保存一个Set<String>到指定的Preference中，如果当前系统的SDK版本小于11，则会将Set<String>转换成JSON字符串保存
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param mode            模式、类型
     * @param key             键
     * @param value           值
     */
    public static void putStringSet(Context context, String preferencesName, int mode, String key, Set<String> value) {
        SharedPreferences preferences = context.getSharedPreferences(preferencesName, mode);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            preferences.edit().putStringSet(key, value).commit();
        } else {
            putObject(preferences, key, value);
        }
    }

    /**
     * 保存一个Set<String>到指定的Preference中，如果当前系统的SDK版本小于11，则会将Set<String>转换成JSON字符串保存
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param key             键
     * @param value           值
     */
    public static void putStringSet(Context context, String preferencesName, String key, Set<String> value) {
        SharedPreferences preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            preferences.edit().putStringSet(key, value).commit();
        } else {
            putObject(preferences, key, value);
        }
    }

    /**
     * 保存一个Set<String>到默认的Preference中，如果当前系统的SDK版本小于11，则会将Set<String>转换成JSON字符串保存
     *
     * @param context 上下文
     * @param key     键
     * @param value   值
     */
    public static void putStringSet(Context context, String key, Set<String> value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            preferences.edit().putStringSet(key, value).commit();
        } else {
            putObject(preferences, key, value);
        }
    }

    /**
     * 从指定的Preference中取出一个Set<String>，如果当前系统的SDK版本小于11，则会先取出JSON字符串然后再转换成Set<String>
     *
     * @param preferences  Preferences
     * @param key          键
     * @param defaultValue 默认值
     * @return 字符串集合
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static Set<String> getStringSet(SharedPreferences preferences, String key, Set<String> defaultValue) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return preferences.getStringSet(key, defaultValue);
        } else {
            Set<String> strings = getObject(preferences, key, new TypeToken<Set<String>>() {
            }.getType());
            if (strings == null) {
                strings = defaultValue;
            }
            return strings;
        }
    }

    /**
     * 从指定的Preference中取出一个Set<String>，如果当前系统的SDK版本小于11，则会先取出JSON字符串然后再转换成Set<String>
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param mode            模式、类型
     * @param key             键
     * @param defaultValue    默认值
     * @return 字符串集合
     */
    public static Set<String> getStringSet(Context context, String preferencesName, int mode, String key, Set<String> defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(preferencesName, mode);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return preferences.getStringSet(key, defaultValue);
        } else {
            Set<String> strings = getObject(preferences, key, new TypeToken<Set<String>>() {
            }.getType());
            if (strings == null) {
                strings = defaultValue;
            }
            return strings;
        }
    }

    /**
     * 从指定的Preference中取出一个Set<String>，如果当前系统的SDK版本小于11，则会先取出JSON字符串然后再转换成Set<String>
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param key             键
     * @param defaultValue    默认值
     * @return 字符串集合
     */
    public static Set<String> getStringSet(Context context, String preferencesName, String key, Set<String> defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return preferences.getStringSet(key, defaultValue);
        } else {
            Set<String> strings = getObject(preferences, key, new TypeToken<Set<String>>() {
            }.getType());
            if (strings == null) {
                strings = defaultValue;
            }
            return strings;
        }
    }

    /**
     * 从指定的Preference中取出一个Set<String>，如果当前系统的SDK版本小于11，则会先取出JSON字符串然后再转换成Set<String>
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param key             键
     * @return 字符串集合
     */
    public static Set<String> getStringSet(Context context, String preferencesName, String key) {
        SharedPreferences preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return preferences.getStringSet(key, null);
        } else {
            return getObject(preferences, key, new TypeToken<Set<String>>() {
            }.getType());
        }
    }

    /**
     * 从默认的Preference中取出一个Set<String>，如果当前系统的SDK版本小于11，则会先取出JSON字符串然后再转换成Set<String>
     *
     * @param context      上下文
     * @param key          键
     * @param defaultValue 默认值
     * @return 字符串集合
     */
    public static Set<String> getStringSet(Context context, String key, Set<String> defaultValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return preferences.getStringSet(key, defaultValue);
        } else {
            Set<String> strings = getObject(preferences, key, new TypeToken<Set<String>>() {
            }.getType());
            if (strings == null) {
                strings = defaultValue;
            }
            return strings;
        }
    }

    /**
     * 从默认的Preference中取出一个Set<String>，如果当前系统的SDK版本小于11，则会先取出JSON字符串然后再转换成Set<String>
     *
     * @param context 上下文
     * @param key     键
     * @return 字符串集合
     */
    public static Set<String> getStringSet(Context context, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return preferences.getStringSet(key, null);
        } else {
            return getObject(preferences, key, new TypeToken<Set<String>>() {
            }.getType());
        }
    }

    /**
     * 保存一个对象到指定的Preference中，此对象会被格式化为JSON格式再存
     *
     * @param preferences Preferences
     * @param key         键
     * @param object      对象
     */
    public static void putObject(SharedPreferences preferences, String key, Object object) {
        preferences.edit().putString(key, new Gson().toJson(object)).commit();
    }

    /**
     * 保存一个对象到指定的Preference中，此对象会被格式化为JSON格式再存
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param mode            模式、类型
     * @param key             键
     * @param object          对象
     */
    public static void putObject(Context context, String preferencesName, int mode, String key, Object object) {
        context.getSharedPreferences(preferencesName, mode).edit().putString(key, new Gson().toJson(object)).commit();
    }

    /**
     * 保存一个对象到指定的Preference中，此对象会被格式化为JSON格式再存
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param key             键
     * @param object          对象
     */
    public static void putObject(Context context, String preferencesName, String key, Object object) {
        context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE).edit().putString(key, new Gson().toJson(object)).commit();
    }

    /**
     * 保存一个对象到默认的Preference中，此对象会被格式化为JSON格式再存
     *
     * @param context 上下文
     * @param key     键
     * @param object  对象
     */
    public static void putObject(Context context, String key, Object object) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, new Gson().toJson(object)).commit();
    }

    /**
     * 从指定的Preference中取出一个对象
     *
     * @param preferences Preferences
     * @param key         键
     * @param clas        对象Class
     * @return T
     */
    public static <T> T getObject(SharedPreferences preferences, String key, Class<T> clas) {
        String configJson = preferences.getString(key, null);
        if (StringUtils.isNotEmpty(configJson)) {
            return (T) new Gson().fromJson(configJson, clas);
        } else {
            return null;
        }
    }

    /**
     * 从指定的Preference中取出一个对象
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param mode            模式、类型
     * @param key             键
     * @param clas            对象Class
     * @return T
     */
    public static <T> T getObject(Context context, String preferencesName, int mode, String key, Class<T> clas) {
        String configJson = context.getSharedPreferences(preferencesName, mode).getString(key, null);
        if (StringUtils.isNotEmpty(configJson)) {
            return (T) new Gson().fromJson(configJson, clas);
        } else {
            return null;
        }
    }

    /**
     * 从指定的Preference中取出一个对象
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param key             键
     * @param clas            对象Class
     * @return T
     */
    public static <T> T getObject(Context context, String preferencesName, String key, Class<T> clas) {
        String configJson = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE).getString(key, null);
        if (StringUtils.isNotEmpty(configJson)) {
            return (T) new Gson().fromJson(configJson, clas);
        } else {
            return null;
        }
    }

    /**
     * 从默认的Preference中取出一个对象
     *
     * @param context 上下文
     * @param key     键
     * @param clas    对象Class
     * @return T
     */
    public static <T> T getObject(Context context, String key, Class<T> clas) {
        String configJson = PreferenceManager.getDefaultSharedPreferences(context).getString(key, null);
        if (StringUtils.isNotEmpty(configJson)) {
            return (T) new Gson().fromJson(configJson, clas);
        } else {
            return null;
        }
    }

    /**
     * 从指定的Preference中取出一个对象
     *
     * @param preferences Preferences
     * @param key         键
     * @param typeOfT     集合类型
     * @return T
     */
    public static <T> T getObject(SharedPreferences preferences, String key, Type typeOfT) {
        String configJson = preferences.getString(key, null);
        if (StringUtils.isNotEmpty(configJson)) {
            return new Gson().fromJson(configJson, typeOfT);
        } else {
            return null;
        }
    }

    /**
     * 从指定的Preference中取出一个对象
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param mode            模式、类型
     * @param key             键
     * @param typeOfT         集合类型
     * @return T
     */
    public static <T> T getObject(Context context, String preferencesName, int mode, String key, Type typeOfT) {
        String configJson = context.getSharedPreferences(preferencesName, mode).getString(key, null);
        if (StringUtils.isNotEmpty(configJson)) {
            return new Gson().fromJson(configJson, typeOfT);
        } else {
            return null;
        }
    }

    /**
     * 从指定的Preference中取出一个对象
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param key             键
     * @param typeOfT         集合类型
     * @return T
     */
    public static <T> T getObject(Context context, String preferencesName, String key, Type typeOfT) {
        String configJson = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE).getString(key, null);
        if (StringUtils.isNotEmpty(configJson)) {
            return new Gson().fromJson(configJson, typeOfT);
        } else {
            return null;
        }
    }

    /**
     * 从默认的Preference中取出一个对象
     *
     * @param context 上下文
     * @param key     键
     * @param typeOfT
     * @return T
     */
    public static <T> T getObject(Context context, String key, Type typeOfT) {
        String configJson = PreferenceManager.getDefaultSharedPreferences(context).getString(key, null);
        if (StringUtils.isNotEmpty(configJson)) {
            return new Gson().fromJson(configJson, typeOfT);
        } else {
            return null;
        }
    }

    /**
     * 删除
     *
     * @param preferences Preferences
     * @param keys        键集合
     */
    public static void remove(SharedPreferences preferences, String... keys) {
        if (keys == null) {
            return;
        }
        Editor editor = preferences.edit();
        for (String key : keys) {
            editor.remove(key);
        }
        editor.commit();
    }

    /**
     * 删除
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param mode            模式、类型
     * @param keys            键集合
     */
    public static void remove(Context context, String preferencesName, int mode, String... keys) {
        if (keys == null) {
            return;
        }
        Editor editor = context.getSharedPreferences(preferencesName, mode).edit();
        for (String key : keys) {
            editor.remove(key);
        }
        editor.commit();
    }

    /**
     * 删除
     *
     * @param context         上下文
     * @param preferencesName Preferences名称
     * @param keys            键集合
     */
    public static void remove(Context context, String preferencesName, String... keys) {
        if (keys == null) {
            return;
        }
        Editor editor = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE).edit();
        for (String key : keys) {
            editor.remove(key);
        }
        editor.commit();
    }

    /**
     * 删除
     *
     * @param context 上下文
     * @param keys    键集合
     */
    public static void remove(Context context, String... keys) {
        if (keys == null) {
            return;
        }
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        for (String key : keys) {
            editor.remove(key);
        }
        editor.commit();
    }
}