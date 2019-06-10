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

package org.totschnig.myexpenses.fragment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.totschnig.myexpenses.MyApplication;
import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.activity.ExpenseEdit;
import org.totschnig.myexpenses.activity.ManageTemplates;
import org.totschnig.myexpenses.activity.ProtectedFragmentActivity;
import org.totschnig.myexpenses.dialog.ConfirmationDialogFragment;
import org.totschnig.myexpenses.dialog.MessageDialogFragment;
import org.totschnig.myexpenses.model.Account;
import org.totschnig.myexpenses.model.Category;
import org.totschnig.myexpenses.model.ContribFeature;
import org.totschnig.myexpenses.model.CurrencyContext;
import org.totschnig.myexpenses.model.Transfer;
import org.totschnig.myexpenses.preference.PrefKey;
import org.totschnig.myexpenses.provider.DatabaseConstants;
import org.totschnig.myexpenses.provider.DbUtils;
import org.totschnig.myexpenses.provider.TransactionProvider;
import org.totschnig.myexpenses.task.TaskExecutionFragment;
import org.totschnig.myexpenses.util.CurrencyFormatter;
import org.totschnig.myexpenses.util.Utils;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import javax.inject.Inject;

import icepick.Icepick;
import icepick.State;

import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_AMOUNT;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_CATID;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_COLOR;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_COMMENT;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_CURRENCY;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_INSTANCEID;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_LABEL_MAIN;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_LABEL_SUB;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_PARENTID;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_PAYEE_NAME;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_PLANID;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_PLAN_INFO;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_ROWID;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_SEALED;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_TEMPLATEID;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_TITLE;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_TRANSFER_ACCOUNT;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_UUID;
import static org.totschnig.myexpenses.provider.DatabaseConstants.SPLIT_CATID;
import static org.totschnig.myexpenses.util.PermissionHelper.PermissionGroup.CALENDAR;

public class TemplatesList extends SortableListFragment
    implements LoaderManager.LoaderCallbacks<Cursor> {
  protected static final int SORTABLE_CURSOR = -1;
  public static final String CALDROID_DIALOG_FRAGMENT_TAG = "CALDROID_DIALOG_FRAGMENT";
  public static final String KEY_IS_SPLIT = "isSplit";
  private ListView mListView;
  private PlanMonthFragment planMonthFragment;

  protected int getMenuResource() {
    return R.menu.templateslist_context;
  }

  private Cursor mTemplatesCursor;
  private SimpleCursorAdapter mAdapter;
  private LoaderManager mManager;

  private int columnIndexAmount, columnIndexLabelSub, columnIndexComment,
      columnIndexPayee, columnIndexColor,
      columnIndexCurrency, columnIndexTransferAccount, columnIndexPlanId,
      columnIndexTitle, columnIndexRowId, columnIndexPlanInfo, columnIndexIsSealed;
  private boolean indexesCalculated = false;
  /**
   * if we are called from the calendar app, we only need to handle display of plan once
   */
  @State
  boolean expandedHandled = false;

  @State
  boolean repairTriggered = false;

  @Inject
  CurrencyFormatter currencyFormatter;
  @Inject
  CurrencyContext currencyContext;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
    Icepick.restoreInstanceState(this, savedInstanceState);
    MyApplication.getInstance().getAppComponent().inject(this);
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    Icepick.saveInstanceState(this, outState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    ProtectedFragmentActivity ctx = (ProtectedFragmentActivity) getActivity();
    View v = inflater.inflate(R.layout.templates_list, container, false);
    mListView = v.findViewById(R.id.list);

    mManager = getLoaderManager();
    mManager.initLoader(SORTABLE_CURSOR, null, this);
    // Create an array to specify the fields we want to display in the list
    String[] from = new String[]{KEY_TITLE, KEY_LABEL_MAIN, KEY_AMOUNT};
    // and an array of the fields we want to bind those fields to
    int[] to = new int[]{R.id.title, R.id.category, R.id.amount};
    mAdapter = new MyAdapter(
       ctx,
        R.layout.template_row,
        null,
        from,
        to,
        0);
    mListView.setAdapter(mAdapter);
    mListView.setEmptyView(v.findViewById(R.id.empty));
    mListView.setOnItemClickListener((parent, view, position, id) -> {
      if (mTemplatesCursor == null || !mTemplatesCursor.moveToPosition(position)) return;
      if (!mTemplatesCursor.isNull(columnIndexPlanId)) {
        if (isCalendarPermissionGranted()) {
          planMonthFragment = PlanMonthFragment.newInstance(
              mTemplatesCursor.getString(columnIndexTitle),
              id,
              mTemplatesCursor.getLong(columnIndexPlanId),
              mTemplatesCursor.getInt(columnIndexColor), false, ctx.getThemeType());
          planMonthFragment.show(getChildFragmentManager(), CALDROID_DIALOG_FRAGMENT_TAG);
        } else {
          ctx.requestCalendarPermission();
        }
      } else if (isForeignExchangeTransfer(position)) {
        dispatchCreateInstanceEditDo(id);
      } else {
        boolean splitAtPosition = isSplitAtPosition(position);
        if (PrefKey.TEMPLATE_CLICK_HINT_SHOWN.getBoolean(false)) {
          if (PrefKey.TEMPLATE_CLICK_DEFAULT.getString("SAVE").equals("SAVE")) {
            if (splitAtPosition) {
              requestSplitTransaction(new Long[]{id});
            } else {
              dispatchCreateInstanceSaveDo(new Long[]{id});
            }
          } else {
            if (splitAtPosition) {
              requestSplitTransaction(id);
            } else {
              dispatchCreateInstanceEditDo(id);
            }
          }
        } else {
          Bundle b = new Bundle();
          b.putLong(KEY_ROWID, id);
          b.putBoolean(KEY_IS_SPLIT, splitAtPosition);
          b.putInt(ConfirmationDialogFragment.KEY_TITLE, R.string.dialog_title_information);
          b.putString(ConfirmationDialogFragment.KEY_MESSAGE, getString(R.string
              .hint_template_click));
          b.putInt(ConfirmationDialogFragment.KEY_COMMAND_POSITIVE, R.id
              .CREATE_INSTANCE_SAVE_COMMAND);
          b.putInt(ConfirmationDialogFragment.KEY_COMMAND_NEGATIVE, R.id
              .CREATE_INSTANCE_EDIT_COMMAND);
          b.putString(ConfirmationDialogFragment.KEY_PREFKEY, PrefKey
              .TEMPLATE_CLICK_HINT_SHOWN.getKey());
          b.putInt(ConfirmationDialogFragment.KEY_POSITIVE_BUTTON_LABEL, R.string
              .menu_create_instance_save);
          b.putInt(ConfirmationDialogFragment.KEY_NEGATIVE_BUTTON_LABEL, R.string
              .menu_create_instance_edit);
          ConfirmationDialogFragment.newInstance(b).show(getFragmentManager(),
              "TEMPLATE_CLICK_HINT");
        }
      }
    });
    registerForContextualActionBar(mListView);
    return v;
  }

  private boolean isCalendarPermissionGranted() {
    return CALENDAR.hasPermission(getContext());
  }

  @Override
  public boolean dispatchCommandMultiple(int command,
                                         SparseBooleanArray positions, Long[] itemIds) {
    switch (command) {
      case R.id.DELETE_COMMAND:
        MessageDialogFragment.newInstance(
            R.string.dialog_title_warning_delete_template,//TODO check if template
            getResources().getQuantityString(R.plurals.warning_delete_template, itemIds.length, itemIds.length),
            new MessageDialogFragment.Button(
                R.string.menu_delete,
                R.id.DELETE_COMMAND_DO,
                itemIds),
            null,
            new MessageDialogFragment.Button(android.R.string.no, R.id.CANCEL_CALLBACK_COMMAND, null))
            .show(getActivity().getSupportFragmentManager(), "DELETE_TEMPLATE");
        return true;
      case R.id.CREATE_INSTANCE_SAVE_COMMAND:
        if (hasSplitAtPositions(positions)) {
          requestSplitTransaction(itemIds);
        } else {
          dispatchCreateInstanceSaveDo(itemIds);
        }
        finishActionMode();
        return true;
      case R.id.CREATE_PLAN_INSTANCE_SAVE_COMMAND:
      case R.id.CANCEL_PLAN_INSTANCE_COMMAND:
      case R.id.RESET_PLAN_INSTANCE_COMMAND:
        requirePlanMonthFragment().dispatchCommandMultiple(command, positions);
        finishActionMode();
        return true;
    }
    return super.dispatchCommandMultiple(command, positions, itemIds);
  }

  @Override
  public boolean dispatchCommandSingle(int command, ContextMenu.ContextMenuInfo info) {
    AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) info;
    Intent i;
    switch (command) {
      case R.id.CREATE_INSTANCE_EDIT_COMMAND:
        if (isSplitAtPosition(menuInfo.position)) {
          requestSplitTransaction(menuInfo.id);
        } else {
          dispatchCreateInstanceEditDo(menuInfo.id);
        }
        finishActionMode();
        return true;
      case R.id.EDIT_COMMAND:
        finishActionMode();
        i = new Intent(getActivity(), ExpenseEdit.class);
        i.putExtra(DatabaseConstants.KEY_TEMPLATEID, menuInfo.id);
        //TODO check what to do on Result
        startActivityForResult(i, ProtectedFragmentActivity.EDIT_TRANSACTION_REQUEST);
        return true;
      case R.id.EDIT_PLAN_INSTANCE_COMMAND:
      case R.id.CREATE_PLAN_INSTANCE_EDIT_COMMAND:
        requirePlanMonthFragment().dispatchCommandSingle(command, menuInfo.position);
        finishActionMode();
        return true;
    }
    return super.dispatchCommandSingle(command, info);
  }

  private boolean isSplitAtPosition(int position) {
    if (mTemplatesCursor != null) {
      if (mTemplatesCursor.moveToPosition(position) &&
          SPLIT_CATID.equals(DbUtils.getLongOrNull(mTemplatesCursor, KEY_CATID))) {
        return true;
      }
    }
    return false;
  }

  private boolean hasSplitAtPositions(SparseBooleanArray positions) {
    for (int i = 0; i < positions.size(); i++) {
      if (positions.valueAt(i) && isSplitAtPosition(positions.keyAt(i))) {
        return true;
      }
    }
    return false;
  }

  /**
   * calls {@link ProtectedFragmentActivity#contribFeatureRequested(ContribFeature, Serializable)}
   * for feature {@link ContribFeature#SPLIT_TRANSACTION}
   * @param tag if tag holds a single long the new instance will be edited, if tag holds an array of longs
   *            new instances will be immediately saved for each
   */
  public void requestSplitTransaction(Serializable tag) {
    ((ProtectedFragmentActivity) getActivity()).contribFeatureRequested(ContribFeature.SPLIT_TRANSACTION, tag);
  }

  public void dispatchCreateInstanceSaveDo(Long[] itemIds) {
    ((ProtectedFragmentActivity) getActivity()).startTaskExecution(
        TaskExecutionFragment.TASK_NEW_FROM_TEMPLATE,
        itemIds,
        null,
        0);
  }

  public void dispatchCreateInstanceEditDo(long itemId) {
    Intent intent = new Intent(getActivity(), ExpenseEdit.class);
    intent.putExtra(KEY_TEMPLATEID, itemId);
    intent.putExtra(KEY_INSTANCEID, -1L);
    startActivity(intent);
  }

  @NonNull
  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
    switch (id) {
      case SORTABLE_CURSOR:
        return new CursorLoader(getActivity(),
            TransactionProvider.TEMPLATES_URI.buildUpon()
                .appendQueryParameter(TransactionProvider.QUERY_PARAMETER_WITH_PLAN_INFO, "1").build(),
            null,
            KEY_PARENTID + " is null",
            null,
            null);
    }
    return null;
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
    final ManageTemplates ctx = (ManageTemplates) getActivity();
    switch (loader.getId()) {
      case SORTABLE_CURSOR:
        mTemplatesCursor = c;
        if (c != null && !indexesCalculated) {
          columnIndexRowId = c.getColumnIndex(KEY_ROWID);
          columnIndexAmount = c.getColumnIndex(KEY_AMOUNT);
          columnIndexLabelSub = c.getColumnIndex(KEY_LABEL_SUB);
          columnIndexComment = c.getColumnIndex(KEY_COMMENT);
          columnIndexPayee = c.getColumnIndex(KEY_PAYEE_NAME);
          columnIndexColor = c.getColumnIndex(KEY_COLOR);
          columnIndexCurrency = c.getColumnIndex(KEY_CURRENCY);
          columnIndexTransferAccount = c.getColumnIndex(KEY_TRANSFER_ACCOUNT);
          columnIndexPlanId = c.getColumnIndex(KEY_PLANID);
          columnIndexTitle = c.getColumnIndex(KEY_TITLE);
          columnIndexPlanInfo = c.getColumnIndex(KEY_PLAN_INFO);
          columnIndexIsSealed = c.getColumnIndex(KEY_SEALED);
          indexesCalculated = true;
        }
        mAdapter.swapCursor(mTemplatesCursor);
        invalidateCAB();
        if (isCalendarPermissionGranted() &&
            mTemplatesCursor != null && mTemplatesCursor.moveToFirst()) {
          long needToExpand = expandedHandled ? ManageTemplates.NOT_CALLED :
              ctx.getCalledFromCalendarWithId();
          boolean foundToExpand = false;
          while (!mTemplatesCursor.isAfterLast()) {
            long templateId = mTemplatesCursor.getLong(columnIndexRowId);
            if (needToExpand == templateId) {
              planMonthFragment = PlanMonthFragment.newInstance(
                  mTemplatesCursor.getString(columnIndexTitle),
                  templateId,
                  mTemplatesCursor.getLong(columnIndexPlanId),
                  mTemplatesCursor.getInt(columnIndexColor), false, ctx.getThemeType());
              foundToExpand = true;
            }
            mTemplatesCursor.moveToNext();
          }
          if (needToExpand != ManageTemplates.NOT_CALLED) {
            expandedHandled = true;
            if (foundToExpand) {
              planMonthFragment.show(getChildFragmentManager(), CALDROID_DIALOG_FRAGMENT_TAG);
            } else {
              ctx.showSnackbar(R.string.save_transaction_template_deleted, Snackbar.LENGTH_LONG);
            }
          }
          //look for plans that we could possible relink
          if (!repairTriggered && mTemplatesCursor.moveToFirst()) {
            final ArrayList<String> missingUuids = new ArrayList<>();
            while (!mTemplatesCursor.isAfterLast()) {
              if (!mTemplatesCursor.isNull(columnIndexPlanId) && mTemplatesCursor.isNull(columnIndexPlanInfo)) {
                missingUuids.add(mTemplatesCursor.getString(mTemplatesCursor.getColumnIndex(KEY_UUID)));
              }
              mTemplatesCursor.moveToNext();
            }
            if (missingUuids.size() > 0) {
              new RepairHandler(this).obtainMessage(
                  0, missingUuids.toArray(new String[missingUuids.size()]))
                  .sendToTarget();
            }
          }
        }
        break;
    }
  }

  public void showSnackbar(String msg, int length) {
    if (planMonthFragment != null) {
      planMonthFragment.showSnackbar(msg, length);
    } else {
      ((ProtectedFragmentActivity) getActivity()).showSnackbar(msg, length);
    }
  }

  private static class RepairHandler extends Handler {
    private final WeakReference<TemplatesList> mFragment;

    public RepairHandler(TemplatesList fragment) {
      mFragment = new WeakReference<>(fragment);
    }

    @Override
    public void handleMessage(Message msg) {
      String[] missingUuids = (String[]) msg.obj;
      TemplatesList fragment = mFragment.get();
      if (fragment != null && fragment.getActivity() != null) {
        fragment.repairTriggered = true;
        ((ProtectedFragmentActivity) fragment.getActivity()).startTaskExecution(
            TaskExecutionFragment.TASK_REPAIR_PLAN,
            missingUuids,
            null,
            0);
      }
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    switch (loader.getId()) {
      case SORTABLE_CURSOR:
        mTemplatesCursor = null;
        mAdapter.swapCursor(null);
        break;
    }
  }

  @Override
  protected PrefKey getSortOrderPrefKey() {
    return PrefKey.SORT_ORDER_TEMPLATES;
  }


  //after orientation change, we need to restore the reference
  public PlanMonthFragment requirePlanMonthFragment() {
    return planMonthFragment != null ? planMonthFragment : ((PlanMonthFragment)
        getChildFragmentManager().findFragmentByTag(CALDROID_DIALOG_FRAGMENT_TAG));
  }

  private class MyAdapter extends SimpleCursorAdapter {
    private int colorExpense;
    private int colorIncome;
    private String categorySeparator = " : ",
        commentSeparator = " / ";

    public MyAdapter(Context context, int layout, Cursor c, String[] from,
                     int[] to, int flags) {
      super(context, layout, c, from, to, flags);
      colorIncome = ((ProtectedFragmentActivity) context).getColorIncome();
      colorExpense = ((ProtectedFragmentActivity) context).getColorExpense();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      convertView = super.getView(position, convertView, parent);
      Cursor c = getCursor();
      c.moveToPosition(position);
      boolean isSealed = c.getInt(columnIndexIsSealed) != 0;
      boolean doesHavePlan = !c.isNull(columnIndexPlanId);
      TextView tv1 = convertView.findViewById(R.id.amount);
      long amount = c.getLong(columnIndexAmount);
      tv1.setTextColor(amount < 0 ? colorExpense : colorIncome);
      tv1.setText(currencyFormatter.convAmount(amount,
          currencyContext.get(c.getString(columnIndexCurrency))));
      int color = c.getInt(columnIndexColor);
      convertView.findViewById(R.id.colorAccount).setBackgroundColor(color);
      TextView tv2 = convertView.findViewById(R.id.category);
      CharSequence catText = tv2.getText();
      if (!c.isNull(columnIndexTransferAccount)) {
        catText = Transfer.getIndicatorPrefixForLabel(amount) + catText;
      } else {
        Long catId = DbUtils.getLongOrNull(c, KEY_CATID);
        if (catId == null) {
          catText = Category.NO_CATEGORY_ASSIGNED_LABEL;
        } else {
          String label_sub = c.getString(columnIndexLabelSub);
          if (label_sub != null && label_sub.length() > 0) {
            catText = catText + categorySeparator + label_sub;
          }
        }
      }
      //TODO: simplify confer TemplateWidget
      SpannableStringBuilder ssb;
      String comment = c.getString(columnIndexComment);
      if (comment != null && comment.length() > 0) {
        ssb = new SpannableStringBuilder(comment);
        ssb.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 0, comment.length(), 0);
        catText = TextUtils.concat(catText, commentSeparator, ssb);
      }
      String payee = c.getString(columnIndexPayee);
      if (payee != null && payee.length() > 0) {
        ssb = new SpannableStringBuilder(payee);
        ssb.setSpan(new UnderlineSpan(), 0, payee.length(), 0);
        catText = TextUtils.concat(catText, commentSeparator, ssb);
      }
      tv2.setText(catText);

      if (doesHavePlan) {
        CharSequence planInfo = c.getString(columnIndexPlanInfo);
        if (planInfo == null) {
          if (isCalendarPermissionGranted()) {
            planInfo = getString(R.string.plan_event_deleted);
          } else {
            planInfo = Utils.getTextWithAppName(getContext(), R.string.calendar_permission_required);
          }
        }
        ((TextView) convertView.findViewById(R.id.title)).setText(
            //noinspection SetTextI18n
            c.getString(columnIndexTitle) + " (" + planInfo + ")");
      }
      ImageView planImage = convertView.findViewById(R.id.Plan);
      planImage.setImageResource(
          isSealed ? R.drawable.ic_lock : (doesHavePlan ? R.drawable.ic_event : R.drawable.ic_menu_template));
      planImage.setContentDescription(getString(doesHavePlan ?
          R.string.plan : R.string.template));
      return convertView;
    }

    @Override
    public boolean areAllItemsEnabled() {
      return false;
    }

    @Override
    public boolean isEnabled(int position) {
      Cursor c = getCursor();
      c.moveToPosition(position);
      return c.getInt(c.getColumnIndex(KEY_SEALED)) == 0;
    }
  }

  @Override
  protected void configureMenuLegacy(Menu menu, ContextMenu.ContextMenuInfo menuInfo, int listId) {
    super.configureMenuLegacy(menu, menuInfo, listId);
    switch (listId) {
      case R.id.list:
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        configureMenuInternal(menu, 1, isForeignExchangeTransfer(info.position), isPlan(info.position));
        break;
      case R.id.calendar_gridview:
        requirePlanMonthFragment().configureMenuLegacy(menu, menuInfo);
    }
  }

  @Override
  protected void configureMenu11(Menu menu, int count, AbsListView lv) {
    super.configureMenu11(menu, count, lv);
    switch (lv.getId()) {
      case R.id.list:
        SparseBooleanArray checkedItemPositions = mListView.getCheckedItemPositions();
        boolean hasForeignExchangeTransfer = false, hasPlan = false;
        for (int i = 0; i < checkedItemPositions.size(); i++) {
          if (checkedItemPositions.valueAt(i) && isForeignExchangeTransfer(checkedItemPositions.keyAt
              (i))) {
            hasForeignExchangeTransfer = true;
            break;
          }
        }
        for (int i = 0; i < checkedItemPositions.size(); i++) {
          if (checkedItemPositions.valueAt(i) && isPlan(checkedItemPositions.keyAt
              (i))) {
            hasPlan = true;
            break;
          }
        }
        configureMenuInternal(menu, count, hasForeignExchangeTransfer, hasPlan);
        break;
      case R.id.calendar_gridview:
        requirePlanMonthFragment().configureMenu11(menu, count, lv);
    }
  }

  private void configureMenuInternal(Menu menu, int count, boolean foreignExchangeTransfer, boolean hasPlan) {
    menu.findItem(R.id.CREATE_INSTANCE_SAVE_COMMAND).setVisible(!foreignExchangeTransfer && !hasPlan);
    menu.findItem(R.id.CREATE_INSTANCE_EDIT_COMMAND).setVisible(count == 1 && !hasPlan);
  }

  private boolean isForeignExchangeTransfer(int position) {
    if (mTemplatesCursor != null && mTemplatesCursor.moveToPosition(position)) {
      if (!mTemplatesCursor.isNull(columnIndexTransferAccount)) {
        Account transferAccount = Account.getInstanceFromDb(
            mTemplatesCursor.getLong(columnIndexTransferAccount));
        return !mTemplatesCursor.getString(columnIndexCurrency).equals(
            transferAccount.getCurrencyUnit().code());
      }
    }
    return false;
  }

  private boolean isPlan(int position) {
    if (mTemplatesCursor != null && mTemplatesCursor.moveToPosition(position)) {
      return !mTemplatesCursor.isNull(columnIndexPlanId);
    }
    return false;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.sort, menu);
    SubMenu subMenu = menu.findItem(R.id.SORT_COMMAND).getSubMenu();
    subMenu.findItem(R.id.SORT_AMOUNT_COMMAND).setVisible(true);
    subMenu.findItem(R.id.SORT_NEXT_INSTANCE_COMMAND).setVisible(true);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    return handleSortOption(item);
  }

  @Override
  protected void inflateHelper(Menu menu, int listId) {
    switch (listId) {
      case R.id.list:
        super.inflateHelper(menu, listId);
        break;
      case R.id.calendar_gridview:
        getActivity().getMenuInflater().inflate(R.menu.planlist_context, menu);
    }
  }
  public void loadData() {
    Utils.requireLoader(mManager, SORTABLE_CURSOR, null, this);
  }
}
