/*
 * Copyright (C) 2017 The JackKnife Open Source Project
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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ApkUtils {

    private static final String CLASS_PACKAGE_PARSER = "android.content.pm.PackageParser";
    private static final String METHOD_PARSE_PACKAGE = "parsePackage";
    private static final String METHOD_COLLECT_CERTIFICATES = "collectCertificates";
    private static final String FIELD_SIGNATURES = "mSignatures";
    private static final String URI_INSTALL_PACKAGE = "application/vnd.android.package-archive";

    private ApkUtils() {
    }

    public static Signature[] getUninstalledApkSignatures(String apkPath) {
        try {
            Class<?> packageParserClass = Class.forName(CLASS_PACKAGE_PARSER);
            Class[] typeArgs = new Class[1];
            typeArgs[0] = String.class;
            Constructor packageParserConstructor = packageParserClass.getConstructor(typeArgs);
            Object[] valueArgs = new Object[1];
            valueArgs[0] = apkPath;
            Object packageParser = packageParserConstructor.newInstance(valueArgs);
            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();
            typeArgs = new Class[4];
            typeArgs[0] = File.class;
            typeArgs[1] = String.class;
            typeArgs[2] = DisplayMetrics.class;
            typeArgs[3] = Integer.TYPE;
            Method parsePackageMethod = packageParserClass.getDeclaredMethod(METHOD_PARSE_PACKAGE,
                    typeArgs);
            valueArgs = new Object[4];
            valueArgs[0] = new File(apkPath);
            valueArgs[1] = apkPath;
            valueArgs[2] = metrics;
            valueArgs[3] = PackageManager.GET_SIGNATURES;
            Object packageParserPackage = parsePackageMethod.invoke(packageParser, valueArgs);
            typeArgs = new Class[2];
            typeArgs[0] = packageParserPackage.getClass();
            typeArgs[1] = Integer.TYPE;
            Method collectCertificatesMethod = packageParserClass
                    .getDeclaredMethod(METHOD_COLLECT_CERTIFICATES, typeArgs);
            valueArgs = new Object[2];
            valueArgs[0] = packageParserPackage;
            valueArgs[1] = PackageManager.GET_SIGNATURES;
            collectCertificatesMethod.invoke(packageParser, valueArgs);
            Field packageInfoField = packageParserPackage.getClass()
                    .getDeclaredField(FIELD_SIGNATURES);
            Signature[] signatures = (Signature[]) packageInfoField.get(packageParserPackage);
            return signatures;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Signature[] getSignatures(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_SIGNATURES);
            return packageInfo.signatures;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PackageInfo getUninstalledApkPackageInfo(Context context, String apkPath) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageArchiveInfo(
                apkPath, 0);
        return packageInfo;
    }

    public static PackageInfo getPackageInfo(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            return packageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Drawable getUninstalledApkIcon(Context context, String apkPath) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = getUninstalledApkPackageInfo(context, apkPath);
        if (packageInfo == null) {
            return null;
        }
        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            applicationInfo.sourceDir = apkPath;
            applicationInfo.publicSourceDir = apkPath;
        }
        return packageManager.getApplicationIcon(applicationInfo);
    }

    public static CharSequence getUninstalledApkLabel(Context context, String apkPath) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = getUninstalledApkPackageInfo(context, apkPath);
        if (packageInfo == null) {
            return null;
        }
        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            applicationInfo.sourceDir = apkPath;
            applicationInfo.publicSourceDir = apkPath;
        }
        return packageManager.getApplicationLabel(applicationInfo);
    }

    public static String getVersionName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getVersionCode(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static File extractApk(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationContext().getApplicationInfo();
        String apkPath = applicationInfo.sourceDir;
        File apkFile = new File(apkPath);
        return apkFile;
    }

    public static void launch(Context context, String packageName, String className) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(new ComponentName(packageName, className));
        context.startActivity(intent);
    }

    public static void install(Context context, String apkPath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(apkPath)),
                URI_INSTALL_PACKAGE);
        context.startActivity(intent);
    }

    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> getAllPackageName(Context context) {
        PackageManager packManager = context.getPackageManager();
        List<PackageInfo> packInfos = packManager
                .getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        if (packInfos == null || packInfos.size() == 0) {
            return null;
        }
        List<String> pkList = new ArrayList<>();
        for (PackageInfo packInfo : packInfos) {
            String packageName = packInfo.packageName;
            pkList.add(packageName);
        }
        return pkList;
    }
}
