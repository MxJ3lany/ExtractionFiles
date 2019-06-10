package org.totschnig.myexpenses.fragment;

import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.calendar.CalendarContractCompat;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidGridAdapter;
import com.roomorama.caldroid.CaldroidListener;
import com.roomorama.caldroid.CalendarHelper;
import com.roomorama.caldroid.CellView;

import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.activity.ExpenseEdit;
import org.totschnig.myexpenses.activity.ManageTemplates;
import org.totschnig.myexpenses.activity.ProtectedFragmentActivity;
import org.totschnig.myexpenses.provider.CalendarProviderProxy;
import org.totschnig.myexpenses.provider.DatabaseConstants;
import org.totschnig.myexpenses.provider.TransactionProvider;
import org.totschnig.myexpenses.task.TaskExecutionFragment;
import org.totschnig.myexpenses.util.ColorUtils;
import org.totschnig.myexpenses.util.UiUtils;
import org.totschnig.myexpenses.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;
import icepick.Icepick;
import icepick.State;

import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_DATE;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_INSTANCEID;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_ROWID;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_TEMPLATEID;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_TRANSACTIONID;

public class PlanMonthFragment extends CaldroidFragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

  private static final String TOOLBAR_TITLE = "toolbarTitle";
  private static final String KEY_READ_ONLY = "readoOnly";
  private LoaderManager mManager;
  public static final int INSTANCES_CURSOR = 1;
  public static final int INSTANCE_STATUS_CURSOR = 2;
  private boolean readOnly;

  private boolean isDarkThemeSelected() {
    return getThemeType().equals(ProtectedFragmentActivity.ThemeType.dark);
  }

  private ProtectedFragmentActivity.ThemeType getThemeType() {
    return ((ProtectedFragmentActivity) getContext()).getThemeType();
  }

  public void showSnackbar(String msg, int length) {
    final Dialog dialog = getDialog();
    if (dialog != null) {
      final Window window = dialog.getWindow();
      if (window != null) {
        View view = window.getDecorView();
        Snackbar snackbar = Snackbar.make(view, msg, length);
        UiUtils.configureSnackbarForDarkTheme(snackbar, getThemeType());
        snackbar.show();
        return;
      }
      Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    }

  }

  private enum PlanInstanceState {
    OPEN, APPLIED, CANCELLED
  }

  @State
  protected HashMap<Long, Long> instance2TransactionMap = new HashMap<>();

  //caldroid fragment operates on Dates set to Midnight. We want to store the exact timestamp in order
  //create the transactions with the exact date provided by the caldendar
  @State
  protected HashMap<DateTime, Long> dateTime2TimeStampMap = new HashMap<>();

  public static PlanMonthFragment newInstance(String title, long templateId, long planId, int color,
                                              boolean readOnly, ProtectedFragmentActivity.ThemeType themeType) {
    PlanMonthFragment f = new PlanMonthFragment();
    Bundle args = new Bundle();
    args.putString(TOOLBAR_TITLE, title);
    args.putInt(CaldroidFragment.THEME_RESOURCE,
        themeType.equals(ProtectedFragmentActivity.ThemeType.dark) ?
            R.style.CaldroidCustomDark : R.style.CaldroidCustom);
    args.putLong(DatabaseConstants.KEY_PLANID, planId);
    args.putInt(DatabaseConstants.KEY_COLOR, color);
    args.putLong(DatabaseConstants.KEY_ROWID, templateId);
    args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, false);
    args.putBoolean(KEY_READ_ONLY, readOnly);
    args.putInt(CaldroidFragment.START_DAY_OF_WEEK,
        Utils.getFirstDayOfWeekFromPreferenceWithFallbackToLocale(Locale.getDefault()));
    f.setArguments(args);
    return f;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    readOnly = getArguments().getBoolean(KEY_READ_ONLY);
    Icepick.restoreInstanceState(this, savedInstanceState);
    setCaldroidListener(new CaldroidListener() {
      @Override
      public void onSelectDate(Date date, View view) {
        //not our concern
      }

      @Override
      public void onChangeMonth(int month, int year) {
        if (!readOnly && isVisible()) {
          ((ContextualActionBarFragment) getParentFragment()).finishActionMode();
        }
        requireLoader(INSTANCES_CURSOR);
      }

      @Override
      public void onGridCreated(GridView gridView) {
        if (!readOnly)
          ((TemplatesList) getParentFragment()).registerForContextualActionBar(gridView);
      }
    });
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    return new Dialog(getActivity(), getTheme()) {
      @Override
      public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == Window.FEATURE_CONTEXT_MENU) {
          return getParentFragment().onContextItemSelected(item);
        } else {
          return super.onMenuItemSelected(featureId, item);
        }
      }
    };
  }

  private void requireLoader(int loaderId) {
    Utils.requireLoader(mManager, loaderId, null, this);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mManager = getLoaderManager();
    View view = super.onCreateView(inflater, container, savedInstanceState);
    Toolbar toolbar = view.findViewById(R.id.calendar_toolbar);
    toolbar.setOnMenuItemClickListener(item -> {
      ((ProtectedFragmentActivity) getActivity()).dispatchCommand(item.getItemId(),
          ManageTemplates.HelpVariant.plans.name());
      return true;
    });
    toolbar.inflateMenu(R.menu.help_with_icon);
    toolbar.setTitle(getArguments().getString(TOOLBAR_TITLE));

    requireLoader(INSTANCE_STATUS_CURSOR);
    return view;
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    Icepick.saveInstanceState(this, outState);
  }


  @Override
  public CaldroidGridAdapter getNewDatesGridAdapter(int month, int year) {
    return new CaldroidCustomAdapter(getActivity(), month, year, getCaldroidData(), extraData);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    switch (id) {
      case INSTANCES_CURSOR:
        // Construct the query with the desired date range.
        Uri.Builder builder = CalendarProviderProxy.INSTANCES_URI.buildUpon();
        DateTime startOfMonth = new DateTime(year, month, 1, 0, 0, 0, 0);
        long start = startOfMonth.minusDays(7)
            .getMilliseconds(TimeZone.getDefault());
        long end = startOfMonth.getEndOfMonth().plusDays(7)
            .getMilliseconds(TimeZone.getDefault());
        ContentUris.appendId(builder, start);
        ContentUris.appendId(builder, end);
        return new CursorLoader(
            getActivity(),
            builder.build(),
            null,
            String.format(Locale.US, CalendarContractCompat.Instances.EVENT_ID + " = %d",
                getArguments().getLong(DatabaseConstants.KEY_PLANID)),
            null,
            null);
      case INSTANCE_STATUS_CURSOR:
        return new CursorLoader(
            getActivity(),
            TransactionProvider.PLAN_INSTANCE_STATUS_URI,
            new String[]{
                KEY_TEMPLATEID,
                KEY_INSTANCEID,
                KEY_TRANSACTIONID
            },
            KEY_TEMPLATEID + " = ?",
            new String[]{String.valueOf(getArguments().getLong(KEY_ROWID))},
            null);
    }
    return null;
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    switch (loader.getId()) {
      case INSTANCES_CURSOR:
        Calendar calendar = Calendar.getInstance();
        data.moveToFirst();
        while (!data.isAfterLast()) {
          long timeInMillis = data.getLong(
              data.getColumnIndex(CalendarContractCompat.Instances.BEGIN));
          calendar.setTimeInMillis(timeInMillis);
          DateTime dateTime = CalendarHelper.convertDateToDateTime(calendar.getTime());
          selectedDates.add(dateTime);
          dateTime2TimeStampMap.put(dateTime, timeInMillis);
          data.moveToNext();
        }
        refreshView();
        break;
      case INSTANCE_STATUS_CURSOR:
        data.moveToFirst();
        instance2TransactionMap.clear();
        while (!data.isAfterLast()) {
          instance2TransactionMap.put(
              data.getLong(data.getColumnIndex(KEY_INSTANCEID)),
              data.getLong(data.getColumnIndex(KEY_TRANSACTIONID)));
          data.moveToNext();
        }
        refreshView();
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {

  }

  public void dispatchCommandSingle(int command, int position) {
    Intent i;
    long instanceId = getPlanInstanceForPosition(position);
    if (instanceId != -1) {
      switch (command) {
        case R.id.CREATE_PLAN_INSTANCE_EDIT_COMMAND:
          i = new Intent(getActivity(), ExpenseEdit.class);
          i.putExtra(KEY_TEMPLATEID, getArguments().getLong(KEY_ROWID));
          i.putExtra(KEY_INSTANCEID, instanceId);
          i.putExtra(KEY_DATE, getDateForPosition(position));
          startActivityForResult(i, 0);
          break;
        case R.id.EDIT_PLAN_INSTANCE_COMMAND:
          i = new Intent(getActivity(), ExpenseEdit.class);
          i.putExtra(KEY_ROWID, instance2TransactionMap.get(instanceId));
          startActivity(i);
          break;
      }
    }
  }

  public void dispatchCommandMultiple(int command, SparseBooleanArray positions) {
    ArrayList<Long[]> extra2dAL = new ArrayList<Long[]>();
    ArrayList<Long> objectIdsAL = new ArrayList<Long>();
    final ProtectedFragmentActivity activity = (ProtectedFragmentActivity) getActivity();
    final Bundle arguments = getArguments();
    if (activity == null || arguments == null) return;
    switch (command) {
      case R.id.CREATE_PLAN_INSTANCE_SAVE_COMMAND:
        for (int i = 0; i < positions.size(); i++) {
          if (positions.valueAt(i)) {
            int position = positions.keyAt(i);
            long instanceId = getPlanInstanceForPosition(position);
            //ignore instances that are not open
            if (instanceId == -1 || instance2TransactionMap.get(instanceId) != null)
              continue;
            //pass event instance id and date as extra
            extra2dAL.add(new Long[]{instanceId, getDateForPosition(position)});
            objectIdsAL.add(arguments.getLong(KEY_ROWID));
          }
        }
        activity.startTaskExecution(
            TaskExecutionFragment.TASK_NEW_FROM_TEMPLATE,
            objectIdsAL.toArray(new Long[0]),
            extra2dAL.toArray(new Long[extra2dAL.size()][2]),
            0);
        break;
      case R.id.CANCEL_PLAN_INSTANCE_COMMAND:
        for (int i = 0; i < positions.size(); i++) {
          if (positions.valueAt(i)) {
            int position = positions.keyAt(i);
            long instanceId = getPlanInstanceForPosition(position);
            if (instanceId == -1)
              continue;
            objectIdsAL.add(instanceId);
            extra2dAL.add(new Long[]{arguments.getLong(KEY_ROWID),
                instance2TransactionMap.get(instanceId)});
          }
        }
        activity.startTaskExecution(
            TaskExecutionFragment.TASK_CANCEL_PLAN_INSTANCE,
            objectIdsAL.toArray(new Long[0]),
            extra2dAL.toArray(new Long[extra2dAL.size()][2]),
            0);
        break;
      case R.id.RESET_PLAN_INSTANCE_COMMAND:
        for (int i = 0; i < positions.size(); i++) {
          if (positions.valueAt(i)) {
            int position = positions.keyAt(i);
            long instanceId = getPlanInstanceForPosition(position);
            if (instanceId == -1)
              continue;
            objectIdsAL.add(instanceId);
            //pass transactionId in extra
            extra2dAL.add(new Long[]{arguments.getLong(KEY_ROWID),
                instance2TransactionMap.get(instanceId)});
          }
        }
        activity.startTaskExecution(
            TaskExecutionFragment.TASK_RESET_PLAN_INSTANCE,
            objectIdsAL.toArray(new Long[0]),
            extra2dAL.toArray(new Long[extra2dAL.size()][2]),
            0);
        break;
    }
  }

  private long getPlanInstanceForPosition(int position) {
    final Long date = dateTime2TimeStampMap.get(dateInMonthsList.get(position));
    return date != null ? CalendarProviderProxy.calculateId(date) : -1;
  }

  private long getDateForPosition(int position) {
    return dateTime2TimeStampMap.get(dateInMonthsList.get(position));
  }

  public void configureMenu11(Menu menu, int count, AbsListView lv) {
    boolean withOpen = false, withApplied = false, withCancelled = false;
    SparseBooleanArray checkedItemPositions = lv.getCheckedItemPositions();
    for (int i = 0; i < checkedItemPositions.size(); i++) {
      if (checkedItemPositions.valueAt(i)) {
        long instanceId = getPlanInstanceForPosition(checkedItemPositions.keyAt(i));
        if (instanceId == -1)
          continue;
        switch (getState(instanceId)) {
          case APPLIED:
            withApplied = true;
            break;
          case CANCELLED:
            withCancelled = true;
            break;
          case OPEN:
            withOpen = true;
            break;
        }
        configureMenuInternalPlanInstances(menu, count, withOpen, withApplied, withCancelled);
      }
    }
  }

  public void configureMenuLegacy(Menu menu, ContextMenu.ContextMenuInfo menuInfo) {
    boolean withOpen = false, withApplied = false, withCancelled = false;
    long instanceId = getPlanInstanceForPosition(((AdapterView.AdapterContextMenuInfo) menuInfo).position);
    if (instanceId != -1) {
      switch (getState(instanceId)) {
        case APPLIED:
          withApplied = true;
          break;
        case CANCELLED:
          withCancelled = true;
          break;
        case OPEN:
          withOpen = true;
          break;
      }
      configureMenuInternalPlanInstances(menu, 1, withOpen, withApplied, withCancelled);
    }
  }

  private PlanInstanceState getState(Long id) {
    Long transactionId = instance2TransactionMap.get(id);
    if (transactionId == null) {
      return PlanInstanceState.OPEN;
    } else if (transactionId != 0L) {
      return PlanInstanceState.APPLIED;
    } else {
      return PlanInstanceState.CANCELLED;
    }
  }

  private void configureMenuInternalPlanInstances(Menu menu, int count, boolean withOpen,
                                                  boolean withApplied, boolean withCancelled) {
    //state open
    menu.findItem(R.id.CREATE_PLAN_INSTANCE_SAVE_COMMAND).setVisible(withOpen);
    menu.findItem(R.id.CREATE_PLAN_INSTANCE_EDIT_COMMAND).setVisible(count == 1 && withOpen);
    //state open or applied
    menu.findItem(R.id.CANCEL_PLAN_INSTANCE_COMMAND).setVisible(withOpen || withApplied);
    //state cancelled or applied
    menu.findItem(R.id.RESET_PLAN_INSTANCE_COMMAND).setVisible(withApplied || withCancelled);
    //state applied
    menu.findItem(R.id.EDIT_PLAN_INSTANCE_COMMAND).setVisible(count == 1 && withApplied);
  }

  private class CaldroidCustomAdapter extends CaldroidGridAdapter {

    public CaldroidCustomAdapter(Context context, int month, int year,
                                 Map<String, Object> caldroidData,
                                 Map<String, Object> extraData) {
      super(context, month, year, caldroidData,
          extraData);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View framelayout;

      // For reuse
      if (convertView == null) {
        //TODO investigate why passing parent to inflate leads to corrupted display
        //noinspection InflateParams
        framelayout = localInflater.inflate(R.layout.plan_calendar_cell, null);
      } else {
        framelayout = convertView;
      }

      CellView cell = (CellView) framelayout.findViewById(R.id.cell);
      ImageView state = (ImageView) framelayout.findViewById(R.id.state);

      customizeTextView(position, cell);

      DateTime dateTime = this.datetimeList.get(position);

      if (selectedDates.contains(dateTime)) {
        state.setVisibility(View.VISIBLE);
        Long transactionId = instance2TransactionMap.get(CalendarProviderProxy.calculateId(dateTime));
        boolean brightColor = ColorUtils.isBrightColor(getArguments().getInt(DatabaseConstants.KEY_COLOR));
        int themeResId = brightColor ? R.style.ThemeLight : R.style.ThemeDark;
        if (transactionId == null) {
          state.setImageBitmap(UiUtils.getTintedBitmapForTheme(getContext(), R.drawable.ic_stat_open, themeResId));
          framelayout.setContentDescription(getString(R.string.plan_instance_state_open));
        } else if (transactionId == 0L) {
          state.setImageBitmap(UiUtils.getTintedBitmapForTheme(getContext(), R.drawable.ic_stat_cancelled, themeResId));
          framelayout.setContentDescription(getString(R.string.plan_instance_state_cancelled));
        } else {
          state.setImageBitmap(UiUtils.getTintedBitmapForTheme(getContext(), R.drawable.ic_stat_applied, themeResId));
          framelayout.setContentDescription(getString(R.string.plan_instance_state_applied));
        }

        cell.setTextColor(getContext().getResources().getColor(
            brightColor ? R.color.cell_text_color : R.color.cell_text_color_dark));
      } else {
        state.setVisibility(View.GONE);
      }

      return framelayout;
    }

    @Override
    protected void resetCustomResources(CellView cellView) {
      int accountColor = getArguments().getInt(DatabaseConstants.KEY_COLOR);
      StateListDrawable stateListDrawable = new StateListDrawable();
      int todayDrawable = isDarkThemeSelected() ? R.drawable.red_border_dark : R.drawable.red_border;
      GradientDrawable todaySelected =
          (GradientDrawable) getResources().getDrawable(todayDrawable).mutate();
      todaySelected.setColor(accountColor);
      stateListDrawable.addState(new int[]{android.R.attr.state_activated},
          new ColorDrawable(getContext().getResources().getColor(R.color.appDefault)));
      stateListDrawable.addState(
          new int[]{R.attr.state_date_selected, R.attr.state_date_today},
          todaySelected);
      stateListDrawable.addState(
          new int[]{R.attr.state_date_selected},
          new ColorDrawable(accountColor));
      stateListDrawable.addState(
          new int[]{R.attr.state_date_today},
          getResources().getDrawable(todayDrawable));
      stateListDrawable.addState(
          new int[]{R.attr.state_date_prev_next_month},
          new ColorDrawable(getContext().getResources().getColor(
              isDarkThemeSelected() ? R.color.caldroid_333 : R.color.caldroid_white)));
      stateListDrawable.addState(
          new int[]{},
          new ColorDrawable(getContext().getResources().getColor(
              isDarkThemeSelected() ? R.color.caldroid_black : R.color.caldroid_white)));
      cellView.setBackgroundDrawable(stateListDrawable);

      cellView.setTextColor(defaultTextColorRes);
    }

    @Override
    public boolean isEnabled(int position) {
      return selectedDates.contains(this.datetimeList.get(position));
    }
  }
}
