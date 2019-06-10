/*   This file is part of My Expenses.
 *   My Expenses is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   My Expenses is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with My Expenses.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.totschnig.myexpenses.util;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.util.Pair;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.IconMarginSpan;
import android.util.Xml;
import android.view.MenuItem;
import android.view.SubMenu;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.squareup.phrase.Phrase;

import org.totschnig.myexpenses.MyApplication;
import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.di.AppComponent;
import org.totschnig.myexpenses.model.AggregateAccount;
import org.totschnig.myexpenses.model.Category;
import org.totschnig.myexpenses.model.ContribFeature;
import org.totschnig.myexpenses.model.CurrencyContext;
import org.totschnig.myexpenses.model.CurrencyUnit;
import org.totschnig.myexpenses.model.Grouping;
import org.totschnig.myexpenses.model.Payee;
import org.totschnig.myexpenses.model.Sort;
import org.totschnig.myexpenses.model.SortDirection;
import org.totschnig.myexpenses.preference.PrefKey;
import org.totschnig.myexpenses.provider.TransactionDatabase;
import org.totschnig.myexpenses.provider.filter.WhereFilter;
import org.totschnig.myexpenses.task.GrisbiImportTask;
import org.totschnig.myexpenses.util.crashreporting.CrashHandler;
import org.totschnig.myexpenses.util.licence.LicenceStatus;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Currency;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

import static android.text.format.DateUtils.DAY_IN_MILLIS;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_AMOUNT;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_LAST_USED;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_SORT_KEY;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_USAGES;

/**
 * Util class with helper methods
 *
 * @author Michael Totschnig
 */
public class Utils {

  public static final String PLACEHOLDER_APP_NAME = "app_name";

  private Utils() {
  }

  public static String getCountryFromTelephonyManager() {
    TelephonyManager telephonyManager = (TelephonyManager) MyApplication.getInstance()
        .getSystemService(Context.TELEPHONY_SERVICE);
    if (telephonyManager != null) {
      try {
        String userCountry = telephonyManager.getNetworkCountryIso();
        if (TextUtils.isEmpty(userCountry)) {
          userCountry = telephonyManager.getSimCountryIso();
        }
        return userCountry;
      } catch (Exception ignore) {}
    }
    return null;
  }

  public static CurrencyUnit getHomeCurrency() {
    //TODO provide home currency in a cleaner way
    AppComponent appComponent = MyApplication.getInstance().getAppComponent();
    String home = appComponent.prefHandler().getString(PrefKey.HOME_CURRENCY,null);
    final CurrencyContext currencyContext = appComponent.currencyContext();
    return currencyContext.get(home != null ? home : getLocalCurrency().getCurrencyCode());
  }

  private static Currency getLocalCurrency() {
    Currency result = null;
    String userCountry = getCountryFromTelephonyManager();
    if (!TextUtils.isEmpty(userCountry)) {
      try {
        result = Currency.getInstance(new Locale("", userCountry));
      } catch (Exception ignore) {}
    }
    if (result == null) {
      result = getSaveDefault();
    }
    return result;
  }

  public static List<Map<String,String>> getProjectDependencies(Context context) {
    List<Map<String, String>> result = new ArrayList<>();
    XmlPullParser xpp= context.getResources().getXml(R.xml.project_dependencies);
    int eventType = 0;
    try {
      eventType = xpp.getEventType();
      Map<String,String> project = null;
      while (eventType != XmlPullParser.END_DOCUMENT) {
        if(eventType == XmlPullParser.START_TAG) {
          if (xpp.getName().equals("project")) {
            project = new HashMap<>();
          } else if (project != null) {
            String key = xpp.getName();
            xpp.next();
            project.put(key, xpp.getText());
          }
        } else if(eventType == XmlPullParser.END_TAG) {
          if(xpp.getName().equals("project")){
            result.add(project);
          }
        }
        eventType = xpp.next();
      }
    } catch (Exception e) {
      Timber.e(e);
    }
    return result;
  }

  public enum Feature {
    ;

    public boolean isEnabled() {
      return true;
    }
  }

  public static boolean hasApiLevel(int checkVersion) {
    return Build.VERSION.SDK_INT >= checkVersion;
  }

  public static char getDefaultDecimalSeparator() {
    char sep = '.';
    NumberFormat nfDLocal = NumberFormat.getNumberInstance();
    if (nfDLocal instanceof DecimalFormat) {
      DecimalFormatSymbols symbols = ((DecimalFormat) nfDLocal)
          .getDecimalFormatSymbols();
      sep = symbols.getDecimalSeparator();
    }
    return sep;
  }

  public static String defaultOrderBy(String textColumn, PrefKey prefKey) {
    Sort sort;
    try {
      sort = Sort.valueOf(prefKey.getString("USAGES"));
    } catch (IllegalArgumentException e) {
      sort = Sort.USAGES;
    }

    String sortOrder = textColumn + " COLLATE LOCALIZED";
    switch (sort) {
      case USAGES:
        sortOrder = KEY_USAGES + " DESC, " + sortOrder;
        break;
      case LAST_USED:
        sortOrder = KEY_LAST_USED + " DESC, " + sortOrder;
        break;
      case CUSTOM:
        sortOrder = KEY_SORT_KEY + " ASC, " + sortOrder;
        break;
      case AMOUNT:
        sortOrder = "abs(" + KEY_AMOUNT + ") DESC, " + sortOrder;
        break;
      case NEXT_INSTANCE:
        sortOrder = null; //handled by PlanInfoCursorWrapper
        //default is textColumn
    }
    return sortOrder;
  }

  /**
   * <a href="http://www.ibm.com/developerworks/java/library/j-numberformat/">
   * http://www.ibm.com/developerworks/java/library/j-numberformat/</a>
   *
   * @param strFloat parsed as float with the number format defined in the locale
   * @return the float retrieved from the string or null if parse did not
   * succeed
   */
  public static BigDecimal validateNumber(DecimalFormat df, String strFloat) {
    ParsePosition pp;
    pp = new ParsePosition(0);
    pp.setIndex(0);
    df.setParseBigDecimal(true);
    BigDecimal n = (BigDecimal) df.parse(strFloat, pp);
    if (strFloat.length() != pp.getIndex() || n == null) {
      return null;
    } else {
      return n;
    }
  }

  public static Date dateFromSQL(String dateString) {
    try {
      return TransactionDatabase.dateFormat.parse(dateString);
    } catch (ParseException e) {
      return null;
    }
  }

  /**
   * @param currency
   * @param separator
   * @return a Decimalformat with the number of fraction digits appropriate for
   * currency, and with the given separator, but without the currency
   * symbol appropriate for CSV and QIF export
   */
  public static DecimalFormat getDecimalFormat(CurrencyUnit currency, char separator) {
    DecimalFormat nf = new DecimalFormat();
    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
    symbols.setDecimalSeparator(separator);
    nf.setDecimalFormatSymbols(symbols);
    int fractionDigits = currency.fractionDigits();
    nf.setMinimumFractionDigits(fractionDigits);
    nf.setMaximumFractionDigits(fractionDigits);
    nf.setGroupingUsed(false);
    return nf;
  }

  /**
   * utility method that calls formatters for date
   *
   * @param text
   * @return formated string
   */
  public static String convDate(String text, DateFormat format) {
    Date date = dateFromSQL(text);
    if (date == null)
      return text;
    else
      return format.format(date);
  }

  /**
   * utility method that calls formatters for date
   *
   * @param date unixEpoch
   * @return formated string
   */
  public static String convDateTime(long date, DateFormat format) {
    return format.format(new Date(date * 1000L));
  }

  @NonNull
  private static Currency getSaveDefault() {
    try {
      return Currency.getInstance(MyApplication.getSystemLocale());
    } catch (NullPointerException | IllegalArgumentException ex) {
      return Currency.getInstance(new Locale("en", "US"));
    }
  }

  @Nullable
  public static Currency getInstance(@Nullable String strCurrency) {
    if (strCurrency != null) {
      if (strCurrency.equals(AggregateAccount.AGGREGATE_HOME_CURRENCY_CODE)) {
        strCurrency = PrefKey.HOME_CURRENCY.getString("EUR");
      }
      try {
        return Currency.getInstance(strCurrency);
      } catch (IllegalArgumentException ignored) {
      }
    }
    return null;
  }

  /**
   * @param context The application's environment.
   * @param intent  The Intent action to check for availability.
   * @return True if an Intent with the specified action can be sent and
   * responded to, false otherwise.
   */
  public static boolean isIntentAvailable(Context context, Intent intent) {
    return intent.resolveActivity(context.getPackageManager()) != null;
  }

  public static boolean isIntentReceiverAvailable(Context context, Intent intent) {
    final PackageManager packageManager = context.getPackageManager();
    List<ResolveInfo> list = packageManager.queryBroadcastReceivers(intent, 0);
    return !list.isEmpty();
  }

  public static boolean isComAndroidVendingInstalled(Context context) {
    PackageManager pm = context.getPackageManager();
    try
    {
      pm.getPackageInfo("com.android.vending", PackageManager.GET_ACTIVITIES);
    }
    catch (PackageManager.NameNotFoundException e)
    {
      return false;
    }
    return true;
  }

  public static long getDaysSinceInstall(Context context) {
    try {
      return (System.currentTimeMillis() -
          context.getPackageManager().getPackageInfo(context.getPackageName(), 0)
              .firstInstallTime) / DAY_IN_MILLIS;
    } catch (NameNotFoundException e) {
      return 0;
    }
  }


  public static long getDaysSinceUpdate(Context context) {
    try {
      return (System.currentTimeMillis() -
          context.getPackageManager().getPackageInfo(context.getPackageName(), 0)
              .lastUpdateTime) / DAY_IN_MILLIS;
    } catch (NameNotFoundException e) {
      return 0;
    }
  }

  /**
   * get a value from extras that could be either passed as String or a long extra
   * we need this method, to pass values from monkeyrunner, which is not able to pass long extras
   * if extras is null, defaultValue is returned
   *
   * @param extras
   * @param key
   * @param defaultValue
   * @return
   */
  public static long getFromExtra(Bundle extras, String key, long defaultValue) {
    if (extras == null) return defaultValue;
    String stringValue = extras.getString(key);
    if (TextUtils.isEmpty(stringValue)) {
      return extras.getLong(key, defaultValue);
    } else {
      return Long.parseLong(stringValue);
    }
  }

  @SuppressLint("DefaultLocale")
  public static String toLocalizedString(int i) {
    return String.format("%d", i);
  }

  public static List<CharSequence> getContribFeatureLabelsAsList(Context ctx, LicenceStatus type) {
    Stream<ContribFeature> features = Stream.of(EnumSet.allOf(ContribFeature.class));
    if (type != null) {
      features =  features.filter(feature -> feature.getLicenceStatus() == type);
    }
    return features
        .map(feature -> {
          String resName = "contrib_feature_" + feature.toString() + "_label";
          int resId = ctx.getResources().getIdentifier(
              resName, "string",
              ctx.getPackageName());
          return ctx.getText(resId);
        })
        .collect(Collectors.toList());
  }

  public static String md5(String s) {
    try {
      // Create MD5 Hash
      MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
      digest.update(s.getBytes());
      byte messageDigest[] = digest.digest();

      // Create Hex String
      StringBuffer hexString = new StringBuffer();
      for (int i = 0; i < messageDigest.length; i++)
        hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
      return hexString.toString();

    } catch (NoSuchAlgorithmException e) {
      Timber.e(e);
    }
    return "";
  }

  /**
   * Credit:
   * https://groups.google.com/forum/?fromgroups#!topic/actionbarsherlock
   * /Z8Ic8djq-3o
   *
   * @param item
   * @param enabled
   */
  public static void menuItemSetEnabledAndVisible(@NonNull MenuItem item, boolean enabled) {
    item.setEnabled(enabled).setVisible(enabled);
  }

  public static boolean doesPackageExist(Context context, String targetPackage) {
    try {
      context.getPackageManager().getPackageInfo(targetPackage,
          PackageManager.GET_META_DATA);
    } catch (NameNotFoundException e) {
      return false;
    }
    return true;
  }

  public static DateFormat getDateFormatSafe(Context context) {
    try {
      return android.text.format.DateFormat.getDateFormat(context);
    } catch (Exception e) {
      CrashHandler.report(e);
      //java.lang.SecurityException: Requires READ_PHONE_STATE observed on HUAWEI Y625-U13
      return java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT);
    }
  }

  public static DateFormat localizedYearlessDateFormat() {
    Locale l = Locale.getDefault();
    DateFormat dateFormat = getDateFormatSafe(MyApplication.getInstance());
    if (dateFormat instanceof SimpleDateFormat) {
      final String contextPattern = ((SimpleDateFormat) dateFormat).toPattern();
      String yearlessPattern = contextPattern.replaceAll("\\W?[Yy]+\\W?", "");
      return new SimpleDateFormat(yearlessPattern, l);
    } else {
      return dateFormat;
    }
  }

  public static Result<Pair<CategoryTree, ArrayList<String>>> analyzeGrisbiFileWithSAX(InputStream is) {
    GrisbiHandler handler = new GrisbiHandler();
    try {
      Xml.parse(is, Xml.Encoding.UTF_8, handler);
    } catch (IOException e) {
      return Result.ofFailure(R.string.parse_error_other_exception, e.getMessage());
    } catch (GrisbiHandler.FileVersionNotSupportedException e) {
      return  Result.ofFailure(R.string.parse_error_grisbi_version_not_supported, e.getMessage());
    } catch (SAXException e) {
      return  Result.ofFailure(R.string.parse_error_parse_exception);
    }
    return handler.getResult();
  }

  public static int importParties(ArrayList<String> partiesList,
                                  GrisbiImportTask task) {
    int total = 0;
    for (int i = 0; i < partiesList.size(); i++) {
      if (Payee.maybeWrite(partiesList.get(i)) != -1) {
        total++;
      }
      if (task != null && i % 10 == 0) {
        task.publishProgress(i);
      }
    }
    return total;
  }

  public static int importCats(CategoryTree catTree, GrisbiImportTask task) {
    int count = 0, total = 0;
    String label;
    long main_id, sub_id;

    int size = catTree.children().size();
    for (int i = 0; i < size; i++) {
      CategoryTree mainCat = catTree.children().valueAt(i);
      label = mainCat.getLabel();
      count++;
      main_id = Category.find(label, null);
      if (main_id != -1) {
        Timber.i("category with label %s already defined", label);
      } else {
        main_id = Category.write(0L, label, null);
        if (main_id != -1) {
          total++;
          if (task != null && count % 10 == 0) {
            task.publishProgress(count);
          }
        } else {
          // this should not happen
          Timber.w("could neither retrieve nor store main category %s", label);
          continue;
        }
      }
      int subSize = mainCat.children().size();
      for (int j = 0; j < subSize; j++) {
        label = mainCat.children().valueAt(j).getLabel();
        count++;
        sub_id = Category.write(0L, label, main_id);
        if (sub_id != -1) {
          total++;
        } else {
          Timber.i("could not store sub category %s", label);
        }
        if (task != null && count % 10 == 0) {
          task.publishProgress(count);
        }
      }
    }
    return total;
  }


  public static CharSequence getTextWithAppName(Context ctx, int resId) {
    return Phrase.from(ctx, resId).put(PLACEHOLDER_APP_NAME, ctx.getString(R.string.app_name)).format();
  }

  public static CharSequence getTellAFriendMessage(Context ctx) {
    return Phrase.from(ctx, R.string.tell_a_friend_message)
        .put(PLACEHOLDER_APP_NAME, ctx.getString(R.string.app_name))
        .put("platform",  DistribHelper.getPlatform())
        .put("website", ctx.getString(R.string.website)).format();
  }

  // From Financisto
  public static String[] joinArrays(String[] a1, String[] a2) {
    if (a1 == null || a1.length == 0) {
      return a2;
    }
    if (a2 == null || a2.length == 0) {
      return a1;
    }
    String[] a = new String[a1.length + a2.length];
    System.arraycopy(a1, 0, a, 0, a1.length);
    System.arraycopy(a2, 0, a, a1.length, a2.length);
    return a;
  }

  /**
   * @param str
   * @return a representation of str converted to lower case, Unicode
   * normalization applied and markers removed this allows
   * case-insensitive comparison for non-ascii and non-latin strings
   */
  public static String normalize(String str) {
    //noinspection DefaultLocale
    str = str.toLowerCase();
    // Credits: http://stackoverflow.com/a/3322174/1199911
    return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("\\p{M}",
        "");
  }

  public static String esacapeSqlLikeExpression(String str) {
    return str
        .replace(WhereFilter.LIKE_ESCAPE_CHAR,
            WhereFilter.LIKE_ESCAPE_CHAR + WhereFilter.LIKE_ESCAPE_CHAR)
        .replace("%", WhereFilter.LIKE_ESCAPE_CHAR + "%")
        .replace("_", WhereFilter.LIKE_ESCAPE_CHAR + "_");
  }

  public static String printDebug(Object[] objects) {
    if (objects == null) {
      return "null";
    }
    StringBuilder result = new StringBuilder();
    for (Object object : objects) {
      if (!result.toString().equals(""))
        result.append(",");
      result.append(object == null ? "null" : object.toString());
    }
    return result.toString();
  }

  /**
   * filters out the '/' character and characters of type {@link java.lang.Character#SURROGATE} or
   * {@link java.lang.Character#OTHER_SYMBOL}, meant primarily to skip emojs
   *
   * @param in
   * @return
   */
  public static String escapeForFileName(String in) {
    return in.replace("/", "").replaceAll("\\p{Cs}", "").replaceAll("\\p{So}", "");
  }

  public static int getFirstDayOfWeek(Locale locale) {
    return new GregorianCalendar(locale).getFirstDayOfWeek();
  }

  public static int getFirstDayOfWeekFromPreferenceWithFallbackToLocale(Locale locale) {
    String weekStartsOn = PrefKey.GROUP_WEEK_STARTS.getString("-1");
    return weekStartsOn.equals("-1") ? Utils.getFirstDayOfWeek(locale) :
        Integer.parseInt(weekStartsOn);
  }

  public static void configureGroupingMenu(SubMenu groupingMenu, Grouping currentGrouping) {
    MenuItem activeItem;
    switch (currentGrouping) {
      case DAY:
        activeItem = groupingMenu.findItem(R.id.GROUPING_DAY_COMMAND);
        break;
      case WEEK:
        activeItem = groupingMenu.findItem(R.id.GROUPING_WEEK_COMMAND);
        break;
      case MONTH:
        activeItem = groupingMenu.findItem(R.id.GROUPING_MONTH_COMMAND);
        break;
      case YEAR:
        activeItem = groupingMenu.findItem(R.id.GROUPING_YEAR_COMMAND);
        break;
      default:
        activeItem = groupingMenu.findItem(R.id.GROUPING_NONE_COMMAND);
        break;
    }
    activeItem.setChecked(true);
  }

  public static void configureSortDirectionMenu(SubMenu subMenu, SortDirection currentSortDirection) {
    MenuItem activeItem;
    switch (currentSortDirection) {
      case ASC:
        activeItem = subMenu.findItem(R.id.SORT_DIRECTION_ASCENDING_COMMAND);
        break;
      default:
        activeItem = subMenu.findItem(R.id.SORT_DIRECTION_DESCENDING_COMMAND);
        break;
    }
    activeItem.setChecked(true);
  }

  @Nullable
  public static Grouping getGroupingFromMenuItemId(int id) {
    switch (id) {
      case R.id.GROUPING_NONE_COMMAND:
        return Grouping.NONE;
      case R.id.GROUPING_DAY_COMMAND:
        return Grouping.DAY;
      case R.id.GROUPING_WEEK_COMMAND:
        return Grouping.WEEK;
      case R.id.GROUPING_MONTH_COMMAND:
        return Grouping.MONTH;
      case R.id.GROUPING_YEAR_COMMAND:
        return Grouping.YEAR;
    }
    return null;
  }

  @Nullable
  public static SortDirection getSortDirectionFromMenuItemId(int id) {
    switch (id) {
      case R.id.SORT_DIRECTION_DESCENDING_COMMAND:
        return SortDirection.DESC;
      case R.id.SORT_DIRECTION_ASCENDING_COMMAND:
        return SortDirection.ASC;
    }
    return null;
  }

  public static void requireLoader(LoaderManager manager, int loaderId, Bundle args,
                                   LoaderManager.LoaderCallbacks callback) {
    if (manager.getLoader(loaderId) != null && !manager.getLoader(loaderId).isReset()) {
      manager.restartLoader(loaderId, args, callback);
    } else {
      manager.initLoader(loaderId, args, callback);
    }
  }

  /**
   * backport of {@link Integer#compare(int, int)} which is API 19
   * returns -1, 0 or 1
   */
  public static int compare(int lhs, int rhs) {
    return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
  }

  /**
   * backport of {@link Long#compare(long, long)} which is API 19
   * returns -1, 0 or 1
   */
  public static int compare(long x, long y) {
    return (x < y) ? -1 : ((x == y) ? 0 : 1);
  }

  /**
   * backport of {@link java.util.Objects#compare(Object, Object, Comparator)} which is API 19
   */
  public static <T> int compare(T a, T b, Comparator<? super T> c) {
    return (a == b) ? 0 :  c.compare(a, b);
  }

  // From Guava
  public static int indexOf(int[] array, int target) {
    return indexOf(array, target, 0, array.length);
  }

  private static int indexOf(
      int[] array, int target, int start, int end) {
    for (int i = start; i < end; i++) {
      if (array[i] == target) {
        return i;
      }
    }
    return -1;
  }

  public static int pow(int b, int k) {
    switch (b) {
      case 0:
        return (k == 0) ? 1 : 0;
      case 1:
        return 1;
      case (-1):
        return ((k & 1) == 0) ? 1 : -1;
      case 2:
        return (k < Integer.SIZE) ? (1 << k) : 0;
      case (-2):
        if (k < Integer.SIZE) {
          return ((k & 1) == 0) ? (1 << k) : -(1 << k);
        } else {
          return 0;
        }
      default:
        // continue below to handle the general case
    }
    for (int accum = 1; ; k >>= 1) {
      switch (k) {
        case 0:
          return accum;
        case 1:
          return b * accum;
        default:
          accum *= ((k & 1) == 0) ? 1 : b;
          b *= b;
      }
    }
  }

  public static CharSequence makeBulletList(Context ctx, List<CharSequence> lines, int icon) {
    Bitmap scaledBitmap = scaledBitmap(ctx, icon);
    SpannableStringBuilder sb = new SpannableStringBuilder();
    for (int i = 0; i < lines.size(); i++) {
      sb.append(withIconMargin(scaledBitmap, lines.get(i), i < lines.size() - 1));
    }
    return sb;
  }

  private static CharSequence withIconMargin(Bitmap bitmap, CharSequence text, boolean withNewLine) {
    Spannable spannable = new SpannableString(text + (withNewLine ? "\n" : ""));
    spannable.setSpan(new IconMarginSpan(bitmap, 25), 0, text.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    return spannable;
  }

  private static Bitmap scaledBitmap(Context ctx, int icon) {
    InsetDrawable drawable = new InsetDrawable(
        UiUtils.getTintedDrawableForContext(ctx, icon), 0, 20, 0, 0);
    Bitmap bitmap = UiUtils.drawableToBitmap(drawable);
    return Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 0.5),
        (int) (bitmap.getHeight() * 0.5), true);
  }

  public static String getSimpleClassNameFromComponentName(@NonNull ComponentName componentName) {
    String className = componentName.getShortClassName();
    return className.substring(className.lastIndexOf(".") + 1);
  }

  //http://stackoverflow.com/a/28565320/1199911
  public static Throwable getCause(Throwable e) {
    Throwable cause;
    Throwable result = e;

    while(null != (cause = result.getCause())  && (result != cause) ) {
      result = cause;
    }
    return result;
  }

  public static boolean isFrameworkCurrency(String currencyCode) {
    try {
      final java.util.Currency instance = java.util.Currency.getInstance(currencyCode);
      return Build.VERSION.SDK_INT < Build.VERSION_CODES.N || instance.getNumericCode() != 0;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
