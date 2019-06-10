/*
 * Copyright (C) 2018 Peter Gregus for GravityBox Project (C3C076@xda)
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
package com.ceco.pie.gravitybox.quicksettings;

import com.ceco.pie.gravitybox.GravityBox;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XC_MethodHook;

public class QsDetailItems {
    private static final String TAG = "GB:QsDetailItems";
    private static final String CLASS_QS_DETAIL_ITEMS = "com.android.systemui.qs.QSDetailItems";

    private ClassLoader mClassLoader;

    public QsDetailItems(ClassLoader cl) {
        mClassLoader = cl;
        createHooks();
    }

    private void createHooks() {
        hook_getView();
        hook_onFinishInflate();
    }

    private void hook_getView() {
        try {
            XposedHelpers.findAndHookMethod(CLASS_QS_DETAIL_ITEMS+".Adapter", mClassLoader,
                    "getView", int.class, View.class, ViewGroup.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    View v = (View) param.getResult();
                    if (v == null)
                        return;

                    final Context ctx = v.getContext();

                    ImageView icon = v.findViewById(android.R.id.icon);
                    if (icon != null) {
                        icon.setImageTintList(ColorStateList.valueOf(
                                OOSThemeColorUtils.getColorAccent(ctx)));
                    }

                    TextView title = v.findViewById(android.R.id.title);
                    if (title != null) {
                        title.setTextColor(OOSThemeColorUtils.getColorTextPrimary(ctx));
                    }

                    TextView summary = v.findViewById(android.R.id.summary);
                    if (summary != null) {
                        summary.setTextColor(OOSThemeColorUtils.getColorTextSecondary(ctx));
                    }

                    ImageView icon2 = v.findViewById(android.R.id.icon2);
                    if (icon2 != null) {
                        icon2.setImageTintList(ColorStateList.valueOf(
                                OOSThemeColorUtils.getColorAccent(ctx)));
                    }
                }
            });
        } catch (Throwable t) {
            GravityBox.log(TAG, "Error hooking getView()", t);
        }
    }

    private void hook_onFinishInflate() {
        try {
            XposedHelpers.findAndHookMethod(CLASS_QS_DETAIL_ITEMS, mClassLoader,
                    "onFinishInflate", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    ImageView emptyIcon = (ImageView) XposedHelpers.getObjectField(
                            param.thisObject, "mEmptyIcon");
                    if (emptyIcon != null) {
                        emptyIcon.setImageTintList(ColorStateList.valueOf(
                                OOSThemeColorUtils.getColorTextSecondary(
                                        emptyIcon.getContext())));
                    }

                    TextView emptyText = (TextView) XposedHelpers.getObjectField(
                            param.thisObject, "mEmptyText");
                    if (emptyText != null) {
                        emptyText.setTextColor(OOSThemeColorUtils.getColorTextSecondary(
                                emptyText.getContext()));
                    }
                }
            });
        } catch (Throwable t) {
            GravityBox.log(TAG, "Error hooking onFinishInflate()", t);
        }
    }
}
