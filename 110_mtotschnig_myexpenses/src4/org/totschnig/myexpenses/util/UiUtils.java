package org.totschnig.myexpenses.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatDrawableManager;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;

import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.activity.ProtectedFragmentActivity;
import org.totschnig.myexpenses.model.Account;
import org.totschnig.myexpenses.model.AccountType;
import org.totschnig.myexpenses.preference.PrefHandler;

import static org.totschnig.myexpenses.preference.PrefKey.TRANSACTION_WITH_TIME;
import static org.totschnig.myexpenses.preference.PrefKey.TRANSACTION_WITH_VALUE_DATE;

public class UiUtils {

  private UiUtils() {}

  public static void configureSnackbarForDarkTheme(Snackbar snackbar, ProtectedFragmentActivity.ThemeType themeType) {
    if (themeType.equals(ProtectedFragmentActivity.ThemeType.dark)) {
      //Workaround for https://issuetracker.google.com/issues/37120757
      View snackbarView = snackbar.getView();
      snackbarView.setBackgroundColor(Color.WHITE);
      TextView textView = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
      textView.setTextColor(Color.BLACK);
    }
  }

  public static Bitmap getTintedBitmapForTheme(Context context, int drawableResId, int themeResId) {
    Drawable d = getTintedDrawableForContext(new ContextThemeWrapper(context, themeResId), drawableResId);
    return drawableToBitmap(d);
  }

  static Drawable getTintedDrawableForContext(Context context, int drawableResId) {
    //noinspection RestrictedApi
    return AppCompatDrawableManager.get().getDrawable(context, drawableResId);
  }

  public static Bitmap drawableToBitmap(Drawable d) {
    Bitmap b = Bitmap.createBitmap(d.getIntrinsicWidth(),
        d.getIntrinsicHeight(),
        Bitmap.Config.ARGB_8888);
    Canvas c = new Canvas(b);
    d.setBounds(0, 0, c.getWidth(), c.getHeight());
    d.draw(c);
    return b;
  }

  //http://stackoverflow.com/a/11072627/1199911
  public static void selectSpinnerItemByValue(Spinner spnr, long value) {
    SimpleCursorAdapter adapter = (SimpleCursorAdapter) spnr.getAdapter();
    for (int position = 0; position < adapter.getCount(); position++) {
      if (adapter.getItemId(position) == value) {
        spnr.setSelection(position);
        return;
      }
    }
  }

  public static void setBackgroundTintListOnFab(FloatingActionButton fab, int color) {
    fab.setBackgroundTintList(ColorStateList.valueOf(color));
    DrawableCompat.setTint(fab.getDrawable(), ColorUtils.isBrightColor(color) ? Color.BLACK : Color.WHITE);
    fab.invalidate();
  }

  public static void setBackgroundOnButton(AppCompatButton button, int color) {
    //noinspection RestrictedApi
    button.setSupportBackgroundTintList(new ColorStateList(new int[][] {{0}}, new int[] {color}));
  }

  public static void configureAmountTextViewForHebrew(TextView amount) {
    int layoutDirection = amount.getContext().getResources().getInteger(R.integer.amount_layout_direction);
    if (layoutDirection == 0) { // hebrew
      ViewCompat.setLayoutDirection(amount, layoutDirection);
      amount.setEms(5);
      amount.setEllipsize(android.text.TextUtils.TruncateAt.MARQUEE);
      amount.setSingleLine(true);
      amount.setMarqueeRepeatLimit(-1);
      amount.setHorizontallyScrolling(true);
      amount.setSelected(true);
    }
  }

  public enum DateMode {
    DATE, DATE_TIME, BOOKING_VALUE;
  }

  public static DateMode getDateMode(Account account, PrefHandler prefHandler) {
    if (!(account.getType() == AccountType.CASH)) {
      if (prefHandler.getBoolean(TRANSACTION_WITH_VALUE_DATE, false)) {
        return DateMode.BOOKING_VALUE;
      }
    }
    return prefHandler.getBoolean(TRANSACTION_WITH_TIME, true) ?
        DateMode.DATE_TIME : DateMode.DATE;
  }

  public static void configureProgress(DonutProgress donutProgress, int progress) {
    donutProgress.setProgress(Math.min(progress, 100));
    donutProgress.setText(progress < 1000 ? String.valueOf(progress) : ">1k");
  }

  public static int dp2Px(float dp, Resources resources) {
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
  }

  public static int resolveIcon(Context context, String resourceName) {
    return context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
  }

  public static void setCompoundDrawablesCompatWithIntrinsicBounds(TextView textView, int start, int top, int end, int bottom) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      textView.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end,bottom);
    } else {
      textView.setCompoundDrawablesWithIntrinsicBounds(start, top, end,bottom);
    }
  }
}
