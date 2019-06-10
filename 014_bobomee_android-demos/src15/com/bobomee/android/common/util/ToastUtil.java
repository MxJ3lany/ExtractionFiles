/*
 * Copyright (C) 2016.  BoBoMEe(wbwjx115@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.bobomee.android.common.util;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

/**
 * Toast工具类，避免了在activity使用toast可能导致的泄露，并保证即使在后台线程调用也会执行在UI线程
 *
 * @author markzhai on 16/3/5
 * @version 1.0.0
 */
public final class ToastUtil {

  public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;
  public static final int LENGTH_LONG = Toast.LENGTH_LONG;

  private final static int DEFAULT_GRAVITY = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;

  private final static Singleton<Toast, Context> sToast = new Singleton<Toast, Context>() {
    @Override protected Toast create(Context context) {
      if (context == null || context.getApplicationContext() == null) {
        return null;
      }
      try {
        return Toast.makeText(context.getApplicationContext(), null, LENGTH_SHORT);
      } catch (Throwable e) {
        return null;
      }
    }
  };

  private ToastUtil() {
    // static usage.
  }

  /**
   * Get the singleton instance of toast.
   *
   * @param context Application or activity context.
   * @return Singleton instance of toast.
   */
  public static Toast get(Context context) {
    return sToast.get(context);
  }

  public static void show(Context _context, int resId, Object... args) {
    String s = resId == 0 ? null : getString(_context, resId);
    if (s != null) {
      show(_context, String.format(s, args));
    }
  }

  public static void show(Context _context, String format, Object... args) {
      show(_context, String.format(format, args));
  }

  /**
   * Show application or activity level toast.
   *
   * @param context Application or activity context.
   * @param resId The resource text to show.  Can be formatted text.
   */
  public static void show(Context context, int resId) {
    show(context, resId, LENGTH_SHORT);
  }

  /**
   * Show application or activity level toast.
   *
   * @param context Application or activity context.
   * @param msg The text to show.  Can be formatted text.
   */
  public static void show(Context context, CharSequence msg) {
    show(context, msg, LENGTH_SHORT);
  }

  /**
   * Show application or activity level toast.
   *
   * @param context Application or activity context.
   * @param resId The resource text to show.  Can be formatted text.
   * @param duration How long to display the message.  Either {@link Toast#LENGTH_SHORT} or
   * {@link Toast#LENGTH_LONG}
   */
  public static void show(Context context, int resId, int duration) {
    show(context, resId, duration, DEFAULT_GRAVITY);
  }

  /**
   * Show application or activity level toast.
   *
   * @param context Application or activity context.
   * @param msg The text to show.  Can be formatted text.
   * @param duration How long to display the message.  Either {@link Toast#LENGTH_SHORT} or
   * {@link Toast#LENGTH_LONG}
   */
  public static void show(Context context, CharSequence msg, int duration) {
    show(context, msg, duration, DEFAULT_GRAVITY);
  }

  /**
   * Show application or activity level toast.
   *
   * @param context Application or activity context.
   * @param resId The resource text to show.  Can be formatted text.
   * @param duration How long to display the message.  Either {@link Toast#LENGTH_SHORT} or
   * {@link Toast#LENGTH_LONG}
   * @param gravity The gravity when display the message. See constant defined in {@link Gravity}.
   */
  public static void show(Context context, int resId, int duration, int gravity) {
    show(context, resId == 0 ? null : getString(context, resId), duration, gravity);
  }

  /**
   * Show application or activity level toast.
   *
   * @param context Application or activity context.
   * @param msg The text to show.  Can be formatted text.
   * @param duration How long to display the message.  Either {@link Toast#LENGTH_SHORT} or
   * {@link Toast#LENGTH_LONG}
   * @param gravity The gravity when display the message. See constant defined in {@link Gravity}.
   */
  public static void show(Context context, final CharSequence msg, final int duration,
      final int gravity) {
    if (msg == null || msg.length() == 0) {
      return;
    }
    if (!shouldShow(context)) {
      return;
    }
    final Context appContext = context.getApplicationContext();
    if (UIUtil.isUIThread()) {
      showImmediately(appContext, msg, duration, gravity);
    } else {
      UIUtil.post(new Runnable() {
        @Override public void run() {
          showImmediately(appContext, msg, duration, gravity);
        }
      });
    }
  }

  public static void show(Activity _activity, int resId, Object... args) {
    String s = resId == 0 ? null : getString(_activity, resId);
    if (s != null) {
      show(_activity, String.format(s, args));
    }
  }

  public static void show(Activity _activity, String format, Object... args) {
    show(_activity, String.format(format, args));
  }

  /**
   * Show activity level toast.
   *
   * @param activity Activity.
   * @param resId The resource text to show.  Can be formatted text.
   */
  public static void show(Activity activity, int resId) {
    show(activity, resId, LENGTH_SHORT);
  }

  /**
   * Show activity level toast.
   *
   * @param activity Activity, can be null.
   * @param msg The text to show.  Can be formatted text.
   */
  public static void show(@Nullable Activity activity, CharSequence msg) {
    show(activity, msg, LENGTH_SHORT);
  }

  /**
   * Show activity level toast.
   *
   * @param activity Activity.
   * @param resId The resource text to show.  Can be formatted text.
   * @param duration How long to display the message.  Either {@link Toast#LENGTH_SHORT} or
   * {@link Toast#LENGTH_LONG}
   */
  public static void show(@Nullable Activity activity, int resId, int duration) {
    show(activity, resId == 0 ? null : getString(activity, resId), duration, DEFAULT_GRAVITY);
  }

  /**
   * Show activity level toast.
   *
   * @param activity Activity.
   * @param msg The text to show.  Can be formatted text.
   * @param duration How long to display the message.  Either {@link Toast#LENGTH_SHORT} or
   * {@link Toast#LENGTH_LONG}
   */
  public static void show(@Nullable Activity activity, CharSequence msg, int duration) {
    show(activity, msg, duration, DEFAULT_GRAVITY);
  }

  /**
   * Show activity level toast.
   *
   * @param activity Activity.
   * @param resId The resource text to show.  Can be formatted text.
   * @param duration How long to display the message.  Either {@link Toast#LENGTH_SHORT} or
   * {@link Toast#LENGTH_LONG}
   * @param gravity The gravity when display the message. See constant defined in {@link Gravity}.
   */
  public static void show(@Nullable Activity activity, int resId, int duration, int gravity) {
    show(activity, resId == 0 ? null : getString(activity, resId), duration, gravity);
  }

  /**
   * Show activity level toast.
   *
   * @param activity Activity.
   * @param msg The text to show.  Can be formatted text.
   * @param duration How long to display the message.  Either {@link Toast#LENGTH_SHORT} or
   * {@link Toast#LENGTH_LONG}
   * @param gravity The gravity when display the message. See constant defined in {@link Gravity}.
   */
  public static void show(@Nullable Activity activity, final CharSequence msg, final int duration,
      final int gravity) {
    if (msg == null || msg.length() == 0) {
      return;
    }
    if (!shouldShow(activity)) {
      return;
    }
    final Context appContext = activity.getApplicationContext();
    if (UIUtil.isUIThread()) {
      showImmediately(appContext, msg, duration, gravity);
    } else {
      UIUtil.post(new Runnable() {
        @Override public void run() {
          showImmediately(appContext, msg, duration, gravity);
        }
      });
    }
  }

  private static void showImmediately(Context context, CharSequence msg, int duration,
      int gravity) {
    Toast toast = sToast.get(context);
    if (toast != null) {
      try {
        toast.setText(msg);
        toast.setDuration(duration);
        toast.setGravity(gravity, toast.getXOffset(), toast.getYOffset());
        toast.show();
      } catch (Throwable e) {
        // internal INotificationManager may be null, just catch it and do nothing.
      }
    }
  }

  private static boolean shouldShow(Context context) {
    if (context == null) {
      // special situation.
      return true;
    }
    if (context != context.getApplicationContext()) {
      // fast way: application context.
      return true;
    }
    if (context instanceof Activity) {
      return shouldShow((Activity) context);
    }
    // default.
    return true;
  }

  private static boolean shouldShow(Activity activity) {
    if (activity == null) {
      // null activity.
      return false;
    }
    if (activity.isFinishing()) {
      // activity finished.
      return false;
    }
    Window window = activity.getWindow();
    if (window == null) {
      // contains no window.
      return false;
    }
    View decorView = window.getDecorView();
    if (decorView == null || decorView.getVisibility() != View.VISIBLE) {
      // contains no decor view or decor view is not visible.
      return false;
    }
    return true;
  }

  private static String getString(Context context, int resId) {
    return context.getString(resId);
  }
}
