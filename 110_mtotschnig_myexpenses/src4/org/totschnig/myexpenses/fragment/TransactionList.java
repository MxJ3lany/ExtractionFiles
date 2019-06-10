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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.util.LongSparseArray;
import android.text.InputType;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;

import org.apache.commons.lang3.ArrayUtils;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;
import org.totschnig.myexpenses.MyApplication;
import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.activity.ExpenseEdit;
import org.totschnig.myexpenses.activity.ManageCategories;
import org.totschnig.myexpenses.activity.MyExpenses;
import org.totschnig.myexpenses.activity.ProtectedFragmentActivity;
import org.totschnig.myexpenses.adapter.TransactionAdapter;
import org.totschnig.myexpenses.dialog.AmountFilterDialog;
import org.totschnig.myexpenses.dialog.ConfirmationDialogFragment;
import org.totschnig.myexpenses.dialog.DateFilterDialog;
import org.totschnig.myexpenses.dialog.SelectCrStatusDialogFragment;
import org.totschnig.myexpenses.dialog.SelectMethodDialogFragment;
import org.totschnig.myexpenses.dialog.SelectPayerDialogFragment;
import org.totschnig.myexpenses.dialog.SelectTransferAccountDialogFragment;
import org.totschnig.myexpenses.dialog.TransactionDetailFragment;
import org.totschnig.myexpenses.model.Account;
import org.totschnig.myexpenses.model.AccountType;
import org.totschnig.myexpenses.model.ContribFeature;
import org.totschnig.myexpenses.model.CurrencyContext;
import org.totschnig.myexpenses.model.Grouping;
import org.totschnig.myexpenses.model.Transaction.CrStatus;
import org.totschnig.myexpenses.model.Transfer;
import org.totschnig.myexpenses.preference.PrefHandler;
import org.totschnig.myexpenses.preference.PrefKey;
import org.totschnig.myexpenses.provider.DatabaseConstants;
import org.totschnig.myexpenses.provider.DbUtils;
import org.totschnig.myexpenses.provider.TransactionProvider;
import org.totschnig.myexpenses.provider.filter.AmountCriteria;
import org.totschnig.myexpenses.provider.filter.CategoryCriteria;
import org.totschnig.myexpenses.provider.filter.CommentCriteria;
import org.totschnig.myexpenses.provider.filter.CrStatusCriteria;
import org.totschnig.myexpenses.provider.filter.Criteria;
import org.totschnig.myexpenses.provider.filter.DateCriteria;
import org.totschnig.myexpenses.provider.filter.MethodCriteria;
import org.totschnig.myexpenses.provider.filter.NullCriteria;
import org.totschnig.myexpenses.provider.filter.PayeeCriteria;
import org.totschnig.myexpenses.provider.filter.TransferCriteria;
import org.totschnig.myexpenses.provider.filter.WhereFilter;
import org.totschnig.myexpenses.task.TaskExecutionFragment;
import org.totschnig.myexpenses.util.AppDirHelper;
import org.totschnig.myexpenses.util.CurrencyFormatter;
import org.totschnig.myexpenses.util.Result;
import org.totschnig.myexpenses.util.UiUtils;
import org.totschnig.myexpenses.util.Utils;
import org.totschnig.myexpenses.util.crashreporting.CrashHandler;
import org.totschnig.myexpenses.viewmodel.TransactionListViewModel;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import eltos.simpledialogfragment.input.SimpleInputDialog;
import se.emilsjolander.stickylistheaders.ExpandableStickyListHeadersListView;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView.OnHeaderClickListener;

import static org.totschnig.myexpenses.preference.PrefKey.NEW_SPLIT_TEMPLATE_ENABLED;
import static org.totschnig.myexpenses.provider.DatabaseConstants.HAS_TRANSFERS;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_ACCOUNTID;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_AMOUNT;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_CATID;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_COMMENT;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_CR_STATUS;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_CURRENCY;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_DATE;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_DAY;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_EXCLUDE_FROM_TOTALS;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_HAS_TRANSFERS;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_LABEL;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_LABEL_MAIN;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_LABEL_SUB;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_MAPPED_CATEGORIES;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_MAPPED_METHODS;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_MAPPED_PAYEES;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_METHODID;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_MONTH;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_PARENTID;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_PAYEEID;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_PAYEE_NAME;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_ROWID;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_SECOND_GROUP;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_SUM_EXPENSES;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_SUM_INCOME;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_SUM_TRANSFERS;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_TRANSFER_ACCOUNT;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_TRANSFER_PEER_PARENT;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_WEEK;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_YEAR;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_YEAR_OF_MONTH_START;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_YEAR_OF_WEEK_START;
import static org.totschnig.myexpenses.provider.DatabaseConstants.MAPPED_CATEGORIES;
import static org.totschnig.myexpenses.provider.DatabaseConstants.MAPPED_METHODS;
import static org.totschnig.myexpenses.provider.DatabaseConstants.MAPPED_PAYEES;
import static org.totschnig.myexpenses.provider.DatabaseConstants.SPLIT_CATID;
import static org.totschnig.myexpenses.provider.DatabaseConstants.TABLE_ACCOUNTS;
import static org.totschnig.myexpenses.task.TaskExecutionFragment.KEY_LONG_IDS;
import static org.totschnig.myexpenses.util.ColorUtils.getContrastColor;

public class TransactionList extends ContextualActionBarFragment implements
    LoaderManager.LoaderCallbacks<Cursor>, OnHeaderClickListener {

  public static final String NEW_TEMPLATE_DIALOG = "dialogNewTempl";
  public static final String FILTER_COMMENT_DIALOG = "dialogFilterCom";

  protected int getMenuResource() {
    return R.menu.transactionlist_context;
  }

  private WhereFilter mFilter = WhereFilter.empty();

  private static final int TRANSACTION_CURSOR = 0;
  private static final int SUM_CURSOR = 1;
  private static final int GROUPING_CURSOR = 2;

  public static final String KEY_FILTER = "filter";
  public static final String CATEGORY_SEPARATOR = " : ",
      COMMENT_SEPARATOR = " / ";
  private MyGroupedAdapter mAdapter;
  private boolean hasItems;
  private boolean mappedCategories;
  private boolean mappedPayees;
  private boolean mappedMethods;
  private boolean hasTransfers;
  private boolean firstLoadCompleted;
  private Cursor mTransactionsCursor;
  private Parcelable listState;

  @BindView(R.id.list)
  ExpandableStickyListHeadersListView mListView;
  @BindView(R.id.filter)
  TextView filterView;
  @BindView(R.id.filterCard)
  ViewGroup filterCard;
  private LoaderManager mManager;

  /**
   * maps header to an array that holds an array of following sums:
   * [0] incomeSum
   * [1] expenseSum
   * [2] transferSum
   * [3] previousBalance
   * [4] delta (incomSum - expenseSum + transferSum)
   * [5] interimBalance
   * [6] mappedCategories
   */
  private LongSparseArray<Long[]> headerData = new LongSparseArray<>();

  /**
   * used to restore list selection when drawer is reopened
   */
  private SparseBooleanArray mCheckedListItems;

  private int columnIndexYear, columnIndexYearOfWeekStart, columnIndexMonth,
      columnIndexWeek, columnIndexDay, columnIndexLabelSub,
      columnIndexPayee, columnIndexCrStatus, columnIndexYearOfMonthStart,
      columnIndexLabelMain;
  private boolean indexesCalculated = false;
  //the following values are cached from the account object, so that we can react to changes in the observer
  private Account mAccount;
  private TransactionListViewModel viewModel;

  @Inject
  CurrencyFormatter currencyFormatter;
  @Inject
  PrefHandler prefHandler;
  @Inject
  CurrencyContext currencyContext;

  public static Fragment newInstance(long accountId) {
    TransactionList pageFragment = new TransactionList();
    Bundle bundle = new Bundle();
    bundle.putLong(KEY_ACCOUNTID, accountId);
    pageFragment.setArguments(bundle);
    return pageFragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
    viewModel = ViewModelProviders.of(this).get(TransactionListViewModel.class);
    viewModel.getAccount().observe(this, account -> {
      mAccount = account;
      if (mAccount.isSealed()) {
        mListView.getWrappedList().setChoiceMode(AbsListView.CHOICE_MODE_NONE);
      } else {
        registerForContextualActionBar(mListView.getWrappedList());
      }
      setAdapter();
      setGrouping();
      Utils.requireLoader(mManager, TRANSACTION_CURSOR, null, TransactionList.this);
      Utils.requireLoader(mManager, SUM_CURSOR, null, TransactionList.this);
    });
    viewModel.loadAccount(getArguments().getLong(KEY_ACCOUNTID));
    MyApplication.getInstance().getAppComponent().inject(this);
    firstLoadCompleted = (savedInstanceState != null);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
  }

  private void setAdapter() {
    if (mAccount != null) {
      Context ctx = getActivity();
      if (mAdapter == null) {
        mAdapter = new MyGroupedAdapter(ctx, R.layout.expense_row, null, 0);
      } else {
        mAdapter.setAccount(mAccount);
      }
      if (mListView.getAdapter() == null) {
        mListView.addFooterView(LayoutInflater.from(getActivity()).inflate(R.layout.group_divider, mListView.getWrappedList(), false), null, false);
        mListView.setAdapter(mAdapter);
      }
    }
  }

  private void setGrouping() {
    mAdapter.refreshDateFormat();
    restartGroupingLoader();
  }

  private void restartGroupingLoader() {
    if (mManager == null) {
      //can happen after an orientation change in ExportDialogFragment, when resetting multiple accounts
      mManager = getLoaderManager();
    }
    Utils.requireLoader(mManager, GROUPING_CURSOR, null, this);
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    final MyExpenses ctx = (MyExpenses) getActivity();
    mManager = getLoaderManager();
    //setGrouping();
    if (savedInstanceState != null) {
      mFilter = new WhereFilter(savedInstanceState.getSparseParcelableArray(KEY_FILTER));
    } else {
      restoreFilterFromPreferences();
    }
    View v = inflater.inflate(R.layout.expenses_list, container, false);
    ButterKnife.bind(this, v);
    setAdapter();
    mListView.setOnHeaderClickListener(this);
    mListView.setDrawingListUnderStickyHeader(false);

    mListView.setEmptyView(v.findViewById(R.id.empty));
    mListView.setOnItemClickListener((a, v1, position, id) -> {
      FragmentManager fm = ctx.getSupportFragmentManager();
      DialogFragment f = (DialogFragment) fm.findFragmentByTag(TransactionDetailFragment.class.getName());
      if (f == null) {
        FragmentTransaction ft = fm.beginTransaction();
        TransactionDetailFragment.newInstance(id).show(ft, TransactionDetailFragment.class.getName());
      }
    });
    return v;
  }

  protected void refresh(boolean invalidateMenu) {
    if (mAccount != null) { //if we are refreshed from onActivityResult, it might happen, that mAccount is not yet set (report 5c1754c8f8b88c29631ef140)
      mManager.restartLoader(TRANSACTION_CURSOR, null, this);
      mManager.restartLoader(GROUPING_CURSOR, null, this);
    }
    if (invalidateMenu) {
      getActivity().invalidateOptionsMenu();
    }
  }

  @Override
  public void onDestroyView() {
    if (mListView != null) {
      listState = mListView.getWrappedList().onSaveInstanceState();
    }
    super.onDestroyView();
  }

  @Override
  public boolean dispatchCommandMultiple(int command,
                                         SparseBooleanArray positions, Long[] itemIds) {
    MyExpenses ctx = (MyExpenses) getActivity();
    if (ctx == null) return false;
    FragmentManager fm = getFragmentManager();
    switch (command) {
      case R.id.DELETE_COMMAND: {
        boolean hasReconciled = false, hasNotVoid = false;
        for (int i = 0; i < positions.size(); i++) {
          if (positions.valueAt(i)) {
            mTransactionsCursor.moveToPosition(positions.keyAt(i));
            CrStatus status;
            try {
              status = CrStatus.valueOf(mTransactionsCursor.getString(columnIndexCrStatus));
            } catch (IllegalArgumentException ex) {
              status = CrStatus.UNRECONCILED;
            }
            if (status == CrStatus.RECONCILED) {
              hasReconciled = true;
            }
            if (status != CrStatus.VOID) {
              hasNotVoid = true;
            }
            if (hasNotVoid && hasReconciled) break;
          }
        }
        String message = getResources().getQuantityString(R.plurals.warning_delete_transaction, itemIds.length, itemIds.length);
        if (hasReconciled) {
          message += " " + getString(R.string.warning_delete_reconciled);
        }
        Bundle b = new Bundle();
        b.putInt(ConfirmationDialogFragment.KEY_TITLE,
            R.string.dialog_title_warning_delete_transaction);
        b.putString(
            ConfirmationDialogFragment.KEY_MESSAGE, message);
        b.putInt(ConfirmationDialogFragment.KEY_COMMAND_POSITIVE,
            R.id.DELETE_COMMAND_DO);
        b.putInt(ConfirmationDialogFragment.KEY_COMMAND_NEGATIVE,
            R.id.CANCEL_CALLBACK_COMMAND);
        b.putInt(ConfirmationDialogFragment.KEY_POSITIVE_BUTTON_LABEL, R.string.menu_delete);
        if (hasNotVoid) {
          b.putInt(ConfirmationDialogFragment.KEY_CHECKBOX_LABEL,
              R.string.mark_void_instead_of_delete);
        }
        b.putLongArray(TaskExecutionFragment.KEY_OBJECT_IDS, ArrayUtils.toPrimitive(itemIds));
        ConfirmationDialogFragment.newInstance(b).show(fm, "DELETE_TRANSACTION");
        return true;
      }
      case R.id.SPLIT_TRANSACTION_COMMAND:
        ctx.contribFeatureRequested(ContribFeature.SPLIT_TRANSACTION, ArrayUtils.toPrimitive(itemIds));
        break;
      case R.id.UNGROUP_SPLIT_COMMAND: {
        Bundle b = new Bundle();
        b.putString(ConfirmationDialogFragment.KEY_MESSAGE, getString(R.string.warning_ungroup_split_transactions));
        b.putInt(ConfirmationDialogFragment.KEY_COMMAND_POSITIVE, R.id.UNGROUP_SPLIT_COMMAND);
        b.putInt(ConfirmationDialogFragment.KEY_COMMAND_NEGATIVE, R.id.CANCEL_CALLBACK_COMMAND);
        b.putInt(ConfirmationDialogFragment.KEY_POSITIVE_BUTTON_LABEL, R.string.menu_ungroup_split_transaction);
        b.putLongArray(KEY_LONG_IDS, ArrayUtils.toPrimitive(itemIds));
        ConfirmationDialogFragment.newInstance(b).show(fm, "UNSPLIT_TRANSACTION");
        return true;
      }
      case R.id.UNDELETE_COMMAND:
        ctx.startTaskExecution(
            TaskExecutionFragment.TASK_UNDELETE_TRANSACTION,
            itemIds,
            null,
            0);
        break;
      //super is handling deactivation of mActionMode
    }
    return super.dispatchCommandMultiple(command, positions, itemIds);
  }

  @Override
  public boolean dispatchCommandSingle(int command, ContextMenu.ContextMenuInfo info) {
    AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) info;
    MyExpenses ctx = (MyExpenses) getActivity();
    switch (command) {
      case R.id.EDIT_COMMAND:
      case R.id.CLONE_TRANSACTION_COMMAND:
        mTransactionsCursor.moveToPosition(acmi.position);
        if (DbUtils.getLongOrNull(mTransactionsCursor, KEY_TRANSFER_PEER_PARENT) != null) {
          ctx.showSnackbar(R.string.warning_splitpartcategory_context, Snackbar.LENGTH_LONG);
        } else {
          Intent i = new Intent(ctx, ExpenseEdit.class);
          i.putExtra(KEY_ROWID, acmi.id);
          if (command == R.id.CLONE_TRANSACTION_COMMAND) {
            i.putExtra(ExpenseEdit.KEY_CLONE, true);
          }
          ctx.startActivityForResult(i, MyExpenses.EDIT_TRANSACTION_REQUEST);
        }
        //super is handling deactivation of mActionMode
        break;
      case R.id.CREATE_TEMPLATE_COMMAND:
        if (isSplitAtPosition(acmi.position) && !prefHandler.getBoolean(NEW_SPLIT_TEMPLATE_ENABLED, true)) {
          ctx.showContribDialog(ContribFeature.SPLIT_TEMPLATE, null);
          return true;
        }
        mTransactionsCursor.moveToPosition(acmi.position);
        String label = mTransactionsCursor.getString(columnIndexPayee);
        if (TextUtils.isEmpty(label))
          label = mTransactionsCursor.getString(columnIndexLabelSub);
        if (TextUtils.isEmpty(label))
          label = mTransactionsCursor.getString(columnIndexLabelMain);
        Bundle args = new Bundle();
        args.putLong(KEY_ROWID, acmi.id);
        SimpleInputDialog.build()
            .title(R.string.dialog_title_template_title)
            .cancelable(false)
            .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
            .hint(R.string.label)
            .text(label)
            .extra(args)
            .pos(R.string.dialog_button_add)
            .neut()
            .show(this, NEW_TEMPLATE_DIALOG);
        return true;
    }
    return super.dispatchCommandSingle(command, info);
  }

  @NonNull
  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
    CursorLoader cursorLoader = null;
    String selection;
    String[] selectionArgs;
    if (mAccount.isHomeAggregate()) {
      selection = "";
      selectionArgs = null;
    } else if (mAccount.isAggregate()) {
      selection = KEY_ACCOUNTID + " IN " +
          "(SELECT " + KEY_ROWID + " from " + TABLE_ACCOUNTS + " WHERE " + KEY_CURRENCY + " = ? AND " +
          KEY_EXCLUDE_FROM_TOTALS + " = 0)";
      selectionArgs = new String[]{mAccount.getCurrencyUnit().code()};
    } else {
      selection = KEY_ACCOUNTID + " = ?";
      selectionArgs = new String[]{String.valueOf(mAccount.getId())};
    }
    switch (id) {
      case TRANSACTION_CURSOR:
        if (!mFilter.isEmpty()) {
          String selectionForParents = mFilter.getSelectionForParents(DatabaseConstants.VIEW_EXTENDED);
          if (!selectionForParents.equals("")) {
            if (!TextUtils.isEmpty(selection)) {
              selection += " AND ";
            }
            selection += selectionForParents;
            selectionArgs = Utils.joinArrays(selectionArgs, mFilter.getSelectionArgs(false));
          }
        }
        if (!TextUtils.isEmpty(selection)) {
          selection += " AND ";
        }
        selection += KEY_PARENTID + " is null";
        cursorLoader = new CursorLoader(getActivity(),
            mAccount.getExtendedUriForTransactionList(false),
            mAccount.getExtendedProjectionForTransactionList(),
            selection,
            selectionArgs, KEY_DATE + " " + mAccount.getSortDirection().name());
        break;
      //TODO: probably we can get rid of SUM_CURSOR, if we also aggregate unmapped transactions
      case SUM_CURSOR:
        cursorLoader = new CursorLoader(getActivity(),
            TransactionProvider.TRANSACTIONS_URI,
            new String[]{MAPPED_CATEGORIES, MAPPED_METHODS, MAPPED_PAYEES, HAS_TRANSFERS},
            selection,
            selectionArgs, null);
        break;
      case GROUPING_CURSOR:
        selection = null;
        selectionArgs = null;
        Builder builder = TransactionProvider.TRANSACTIONS_URI.buildUpon();
        if (!mFilter.isEmpty()) {
          selection = mFilter.getSelectionForParts(DatabaseConstants.VIEW_EXTENDED);//GROUP query uses extended view
          if (!selection.equals("")) {
            selectionArgs = mFilter.getSelectionArgs(true);
          }
        }
        builder.appendPath(TransactionProvider.URI_SEGMENT_GROUPS)
            .appendPath(mAccount.getGrouping().name());
        if (!mAccount.isHomeAggregate()) {
          if (mAccount.isAggregate()) {
            builder.appendQueryParameter(KEY_CURRENCY, mAccount.getCurrencyUnit().code());
          } else {
            builder.appendQueryParameter(KEY_ACCOUNTID, String.valueOf(mAccount.getId()));
          }
        }
        cursorLoader = new CursorLoader(getActivity(),
            builder.build(),
            null, selection, selectionArgs, null);
        break;
    }
    return cursorLoader;
  }

  @Override
  public void onLoadFinished(@NonNull Loader<Cursor> arg0, Cursor c) {
    switch (arg0.getId()) {
      case TRANSACTION_CURSOR:
        mTransactionsCursor = c;
        hasItems = c.getCount() > 0;
        if (!indexesCalculated) {
          columnIndexYear = c.getColumnIndex(KEY_YEAR);
          columnIndexYearOfWeekStart = c.getColumnIndex(KEY_YEAR_OF_WEEK_START);
          columnIndexYearOfMonthStart = c.getColumnIndex(KEY_YEAR_OF_MONTH_START);
          columnIndexMonth = c.getColumnIndex(KEY_MONTH);
          columnIndexWeek = c.getColumnIndex(KEY_WEEK);
          columnIndexDay = c.getColumnIndex(KEY_DAY);
          columnIndexLabelSub = c.getColumnIndex(KEY_LABEL_SUB);
          columnIndexLabelMain = c.getColumnIndex(KEY_LABEL_MAIN);
          columnIndexPayee = c.getColumnIndex(KEY_PAYEE_NAME);
          columnIndexCrStatus = c.getColumnIndex(KEY_CR_STATUS);
          indexesCalculated = true;
        }
        mAdapter.swapCursor(c);
        if (c.getCount() > 0) {
          if (firstLoadCompleted) {
            mListView.post(() -> {
              if (listState != null) {
                mListView.getWrappedList().onRestoreInstanceState(listState);
                listState = null;
              }
            });
          } else {
            firstLoadCompleted = true;
            if (prefHandler.getBoolean(PrefKey.SCROLL_TO_CURRENT_DATE, false)) {
              final int currentPosition = findCurrentPosition(c);
              mListView.post(() -> {
                mListView.setSelection(currentPosition);
              });
            }
          }
        }
        invalidateCAB();
        break;
      case SUM_CURSOR:
        c.moveToFirst();
        mappedCategories = c.getInt(c.getColumnIndex(KEY_MAPPED_CATEGORIES)) > 0;
        mappedPayees = c.getInt(c.getColumnIndex(KEY_MAPPED_PAYEES)) > 0;
        mappedMethods = c.getInt(c.getColumnIndex(KEY_MAPPED_METHODS)) > 0;
        hasTransfers = c.getInt(c.getColumnIndex(KEY_HAS_TRANSFERS)) > 0;
        getActivity().invalidateOptionsMenu();
        break;
      case GROUPING_CURSOR:
        int columnIndexGroupYear = c.getColumnIndex(KEY_YEAR);
        int columnIndexGroupSecond = c.getColumnIndex(KEY_SECOND_GROUP);
        int columnIndexGroupSumIncome = c.getColumnIndex(KEY_SUM_INCOME);
        int columnIndexGroupSumExpense = c.getColumnIndex(KEY_SUM_EXPENSES);
        int columnIndexGroupSumTransfer = c.getColumnIndex(KEY_SUM_TRANSFERS);
        int columnIndexGroupMappedCategories = c.getColumnIndex(KEY_MAPPED_CATEGORIES);
        headerData.clear();
        if (c.moveToFirst()) {
          long previousBalance = mAccount.openingBalance.getAmountMinor();
          do {
            long sumIncome = c.getLong(columnIndexGroupSumIncome);
            long sumExpense = c.getLong(columnIndexGroupSumExpense);
            long sumTransfer = c.getLong(columnIndexGroupSumTransfer);
            long delta = sumIncome + sumExpense + sumTransfer;
            long interimBalance = previousBalance + delta;
            long mappedCategories = c.getLong(columnIndexGroupMappedCategories);
            headerData.put(calculateHeaderId(c.getInt(columnIndexGroupYear),
                c.getInt(columnIndexGroupSecond)),
                new Long[]{sumIncome, sumExpense, sumTransfer, previousBalance, delta, interimBalance, mappedCategories});
            previousBalance = interimBalance;
          } while (c.moveToNext());
        }
        //if the transactionscursor has been loaded before the grouping cursor, we need to refresh
        //in order to have accurate grouping values
        if (mTransactionsCursor != null)
          mAdapter.notifyDataSetChanged();
    }
  }

  private int findCurrentPosition(Cursor c) {
    int dateColumn = c.getColumnIndex(KEY_DATE);
    switch (mAccount.getSortDirection()) {
      case ASC:
        long startOfToday = ZonedDateTime.of(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS), ZoneId.systemDefault()).toEpochSecond();
        if (c.moveToLast()) {
          do {
            if (c.getLong(dateColumn) <= startOfToday) {
              return c.isLast() ? c.getPosition() : c.getPosition() + 1;
            }
          } while (c.moveToPrevious());
        }
        break;
      case DESC:
        long endOfDay = ZonedDateTime.of(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusDays(1), ZoneId.systemDefault()).toEpochSecond();
        if (c.moveToFirst()) {
          do {
            if (c.getLong(dateColumn) < endOfDay) {
              return c.getPosition();
            }
          } while (c.moveToNext());
        }
    }
    return 0;
  }

  private long calculateHeaderId(int year, int second) {
    if (mAccount.getGrouping().equals(Grouping.NONE)) {
      return 1;
    }
    return year * 1000 + second;
  }

  @Override
  public void onLoaderReset(@NonNull Loader<Cursor> arg0) {
    switch (arg0.getId()) {
      case TRANSACTION_CURSOR:
        mTransactionsCursor = null;
        mAdapter.swapCursor(null);
        hasItems = false;
        break;
      case SUM_CURSOR:
        mappedCategories = false;
        mappedPayees = false;
        mappedMethods = false;
        break;
    }
  }

  public boolean isFiltered() {
    return !mFilter.isEmpty();
  }

  public boolean hasItems() {
    return hasItems;
  }

  public boolean hasMappedCategories() {
    return mappedCategories;
  }

  private class MyGroupedAdapter extends TransactionAdapter implements StickyListHeadersAdapter {
    private LayoutInflater inflater;

    private MyGroupedAdapter(Context context, int layout, Cursor c, int flags) {
      super(mAccount, context, layout, c, flags, currencyFormatter, prefHandler, currencyContext);
      inflater = LayoutInflater.from(getActivity());
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
      HeaderViewHolder holder;
      if (convertView == null) {
        final int headerLayout = !mFilter.isEmpty() || mAccount.getBudget() == null
            || !ContribFeature.BUDGET.isAvailable(prefHandler)
            ? R.layout.header : R.layout.header_with_budget;
        convertView = inflater.inflate(headerLayout, parent, false);
        holder = new HeaderViewHolder(convertView);
        convertView.setTag(holder);
      } else {
        holder = (HeaderViewHolder) convertView.getTag();
      }

      Cursor c = getCursor();
      c.moveToPosition(position);
      fillSums(holder, getHeaderId(position));
      holder.text.setText(mAccount.getGrouping().getDisplayTitle(getActivity(), c.getInt(getColumnIndexForYear()), getSecond(c), c));
      return convertView;
    }

    @SuppressLint("SetTextI18n")
    private void fillSums(HeaderViewHolder holder, long headerId) {
      Long[] data = headerData != null ? headerData.get(headerId) : null;
      if (data != null) {
        holder.sumIncome.setText("+ " + currencyFormatter.convAmount(data[0], mAccount.getCurrencyUnit()));
        final Long expensesSum = -data[1];
        holder.sumExpense.setText("- " + currencyFormatter.convAmount(expensesSum, mAccount.getCurrencyUnit()));
        holder.sumTransfer.setText(Transfer.BI_ARROW + " " + currencyFormatter.convAmount(
            data[2], mAccount.getCurrencyUnit()));
        String formattedDelta = String.format("%s %s", Long.signum(data[4]) > -1 ? "+" : "-",
            currencyFormatter.convAmount(Math.abs(data[4]), mAccount.getCurrencyUnit()));
        currencyFormatter.convAmount(Math.abs(data[4]), mAccount.getCurrencyUnit());
        holder.interimBalance.setText(
            mFilter.isEmpty() && !mAccount.isHomeAggregate() ? String.format("%s %s = %s",
                currencyFormatter.convAmount(data[3], mAccount.getCurrencyUnit()), formattedDelta,
                currencyFormatter.convAmount(data[5], mAccount.getCurrencyUnit())) :
                formattedDelta);
        if (holder.budgetProgress != null && mAccount.getBudget() != null) {
          long budget = mAccount.getBudget().getAmountMinor();
          int progress = budget == 0 ? 100 : Math.round(expensesSum * 100F / budget);
          UiUtils.configureProgress(holder.budgetProgress, progress);
          holder.budgetProgress.setFinishedStrokeColor(mAccount.color);
          holder.budgetProgress.setUnfinishedStrokeColor(getContrastColor(mAccount.color));
        }
      }
    }

    @Override
    public long getHeaderId(int position) {
      Cursor c = getCursor();
      c.moveToPosition(position);
      return calculateHeaderId(c.getInt(getColumnIndexForYear()), getSecond(c));
    }

    private int getSecond(Cursor c) {
      switch (mAccount.getGrouping()) {
        case DAY:
          return c.getInt(columnIndexDay);
        case WEEK:
          return c.getInt(columnIndexWeek);
        case MONTH:
          return c.getInt(columnIndexMonth);
        default:
          return 0;
      }
    }

    private int getColumnIndexForYear() {
      switch (mAccount.getGrouping()) {
        case WEEK:
          return columnIndexYearOfWeekStart;
        case MONTH:
          return columnIndexYearOfMonthStart;
        default:
          return columnIndexYear;
      }
    }
  }

  class HeaderViewHolder {
    @BindView(R.id.interim_balance) TextView interimBalance;
    @BindView(R.id.text) TextView text;
    @BindView(R.id.sum_income) TextView sumIncome;
    @BindView(R.id.sum_expense) TextView sumExpense;
    @BindView(R.id.sum_transfer) TextView sumTransfer;
    @Nullable @BindView(R.id.budgetProgress) DonutProgress budgetProgress;
    @BindView(R.id.divider_bottom) View dividerBottom;

    HeaderViewHolder(View convertView) {
      ButterKnife.bind(this, convertView);
    }
  }

  @Override
  public void onHeaderClick(StickyListHeadersListView l, View header,
                            int itemPosition, long headerId, boolean currentlySticky) {
    final HeaderViewHolder viewHolder = (HeaderViewHolder) header.getTag();
    if (mListView.isHeaderCollapsed(headerId)) {
      mListView.expand(headerId);
      viewHolder.dividerBottom.setVisibility(View.VISIBLE);
    } else {
      mListView.collapse(headerId);
      viewHolder.dividerBottom.setVisibility(View.GONE);
    }
  }

  @Override
  public boolean onHeaderLongClick(StickyListHeadersListView l, View header,
                                   int itemPosition, long headerId, boolean currentlySticky) {
    MyExpenses ctx = (MyExpenses) getActivity();
    if (headerData != null && headerData.get(headerId)[6] > 0) {
      ctx.contribFeatureRequested(ContribFeature.DISTRIBUTION, headerId);
    } else {
      ctx.showSnackbar(R.string.no_mapped_transactions, Snackbar.LENGTH_LONG);
    }
    return true;
  }

  @Override
  protected void configureMenuLegacy(Menu menu, ContextMenuInfo menuInfo, int listId) {
    super.configureMenuLegacy(menu, menuInfo, listId);
    AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
    final boolean hasSplit = isSplitAtPosition(info.position);
    configureMenuInternal(menu, hasSplit, isVoidAtPosition(info.position), !hasSplit, 1);
  }

  @Override
  protected void configureMenu11(Menu menu, int count, AbsListView lv) {
    super.configureMenu11(menu, count, lv);
    SparseBooleanArray checkedItemPositions = lv.getCheckedItemPositions();
    boolean hasSplit = false, hasVoid = false, hasNotSplit = false;
    for (int i = 0; i < checkedItemPositions.size(); i++) {
      if (checkedItemPositions.valueAt(i))
        if (isSplitAtPosition(checkedItemPositions.keyAt(i))) {
          hasSplit = true;
        } else {
          hasNotSplit = true;
        }
      if (isVoidAtPosition(checkedItemPositions.keyAt(i))) {
        hasVoid = true;
        break;
      }
    }
    for (int i = 0; i < checkedItemPositions.size(); i++) {
      if (checkedItemPositions.valueAt(i))
        if (isVoidAtPosition(checkedItemPositions.keyAt(i))) {
          hasVoid = true;
          break;
        }
    }
    configureMenuInternal(menu, hasSplit, hasVoid, hasNotSplit, count);
  }

  private boolean isSplitAtPosition(int position) {
    if (mTransactionsCursor != null) {
      if (mTransactionsCursor.moveToPosition(position) &&
          SPLIT_CATID.equals(DbUtils.getLongOrNull(mTransactionsCursor, KEY_CATID))) {
        return true;
      }
    }
    return false;
  }

  private boolean isVoidAtPosition(int position) {
    if (mTransactionsCursor != null) {
      if (mTransactionsCursor.moveToPosition(position)) {
        CrStatus status;
        try {
          status = CrStatus.valueOf(mTransactionsCursor.getString(columnIndexCrStatus));
        } catch (IllegalArgumentException ex) {
          status = CrStatus.UNRECONCILED;
        }
        if (status.equals(CrStatus.VOID)) {
          return true;
        }
      }
    }
    return false;
  }

  private void configureMenuInternal(Menu menu, boolean hasSplit, boolean hasVoid, boolean hasNotSplit, int count) {
    menu.findItem(R.id.CREATE_TEMPLATE_COMMAND).setVisible(count == 1);
    menu.findItem(R.id.SPLIT_TRANSACTION_COMMAND).setVisible(!hasSplit && !hasVoid);
    menu.findItem(R.id.UNGROUP_SPLIT_COMMAND).setVisible(!hasNotSplit && !hasVoid);
    menu.findItem(R.id.UNDELETE_COMMAND).setVisible(hasVoid);
    menu.findItem(R.id.EDIT_COMMAND).setVisible(count == 1 && !hasVoid);
  }

  @SuppressLint("NewApi")
  public void onDrawerOpened() {
    if (mActionMode != null) {
      mCheckedListItems = mListView.getWrappedList().getCheckedItemPositions().clone();
      mActionMode.finish();
    }
  }

  public void onDrawerClosed() {
    if (mCheckedListItems != null) {
      for (int i = 0; i < mCheckedListItems.size(); i++) {
        if (mCheckedListItems.valueAt(i)) {
          mListView.getWrappedList().setItemChecked(mCheckedListItems.keyAt(i), true);
        }
      }
    }
    mCheckedListItems = null;
  }

  public void addFilterCriteria(Integer id, Criteria c) {
    mFilter.put(id, c);
    prefHandler.putString(prefNameForCriteria(c.columnName), c.toStringExtra());
    refreshAfterFilterChange();
  }

  protected void refreshAfterFilterChange() {
    refresh(true);
    setAdapter();
  }

  /**
   * Removes a given filter
   *
   * @param id
   * @return true if the filter was set and succesfully removed, false otherwise
   */
  public boolean removeFilter(Integer id) {
    Criteria c = mFilter.get(id);
    boolean isFiltered = c != null;
    if (isFiltered) {
      prefHandler.remove(prefNameForCriteria(c.columnName));
      mFilter.remove(id);
      refreshAfterFilterChange();
    }
    return isFiltered;
  }

  private String prefNameForCriteria(String criteriaColumn) {
    return String.format(Locale.ROOT, "%s_%s_%d", KEY_FILTER, criteriaColumn,
        getArguments().getLong(KEY_ACCOUNTID));
  }


  public void clearFilter() {
    for (int i = 0, size = getFilterCriteria().size(); i < size; i++) {
      prefHandler.remove(prefNameForCriteria(getFilterCriteria().valueAt(i).columnName));
    }
    mFilter.clear();
    refreshAfterFilterChange();
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    if (mAccount == null || getActivity() == null) {
      //mAccount seen in report 3331195c529454ca6b25a4c5d403beda
      //getActivity seen in report 68a501c984bdfcc95b40050af4f815bf
      return;
    }
    MenuItem searchMenu = menu.findItem(R.id.SEARCH_COMMAND);
    if (searchMenu != null) {
      Drawable searchMenuIcon = searchMenu.getIcon();
      if (searchMenuIcon == null) {
        CrashHandler.report("Search menu icon not found");
      }
      filterCard.setVisibility(mFilter.isEmpty() ? View.GONE : View.VISIBLE);
      searchMenu.setChecked(!mFilter.isEmpty());
      if (searchMenuIcon != null) {
        DrawableCompat.setTintList(searchMenuIcon, mFilter.isEmpty() ? null : ColorStateList.valueOf(Color.GREEN));
      }
      if (!mFilter.isEmpty()) {
        filterView.setText(mFilter.prettyPrint(getContext()));
      }
      getActivity().setTitle(mAccount.getLabelForScreenTitle(getContext()));
      SubMenu filterMenu = searchMenu.getSubMenu();
      for (int i = 0; i < filterMenu.size(); i++) {
        MenuItem filterItem = filterMenu.getItem(i);
        boolean enabled = true;
        switch (filterItem.getItemId()) {
          case R.id.FILTER_CATEGORY_COMMAND:
            enabled = mappedCategories;
            break;
          case R.id.FILTER_STATUS_COMMAND:
            enabled = !mAccount.getType().equals(AccountType.CASH);
            break;
          case R.id.FILTER_PAYEE_COMMAND:
            enabled = mappedPayees;
            break;
          case R.id.FILTER_METHOD_COMMAND:
            enabled = mappedMethods;
            break;
          case R.id.FILTER_TRANSFER_COMMAND:
            enabled = hasTransfers;
            break;
        }
        Criteria c = mFilter.get(filterItem.getItemId());
        Utils.menuItemSetEnabledAndVisible(filterItem, enabled || c != null);
        if (c != null) {
          filterItem.setChecked(true);
          filterItem.setTitle(c.prettyPrint(getContext()));
        }
      }
    } else {
      CrashHandler.report("Search menu not found");
    }

    MenuItem groupingItem = menu.findItem(R.id.GROUPING_COMMAND);
    if (groupingItem != null) {
      SubMenu groupingMenu = groupingItem.getSubMenu();
      Utils.configureGroupingMenu(groupingMenu, mAccount.getGrouping());
    }

    MenuItem sortDirectionItem = menu.findItem(R.id.SORT_DIRECTION_COMMAND);
    if (sortDirectionItem != null) {
      SubMenu sortDirectionMenu = sortDirectionItem.getSubMenu();
      Utils.configureSortDirectionMenu(sortDirectionMenu, mAccount.getSortDirection());
    }

    MenuItem balanceItem = menu.findItem(R.id.BALANCE_COMMAND);
    if (balanceItem != null) {
      Utils.menuItemSetEnabledAndVisible(balanceItem, mAccount.getType() != AccountType.CASH && !mAccount.isSealed());
    }
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putSparseParcelableArray(KEY_FILTER, mFilter.getCriteria());
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int command = item.getItemId();
    switch (command) {
      case R.id.FILTER_CATEGORY_COMMAND:
        if (!removeFilter(command)) {
          Intent i = new Intent(getActivity(), ManageCategories.class);
          i.setAction(ManageCategories.ACTION_SELECT_FILTER);
          startActivityForResult(i, ProtectedFragmentActivity.FILTER_CATEGORY_REQUEST);
        }
        return true;
      case R.id.FILTER_AMOUNT_COMMAND:
        if (!removeFilter(command)) {
          AmountFilterDialog.newInstance(mAccount.getCurrencyUnit())
              .show(getActivity().getSupportFragmentManager(), "AMOUNT_FILTER");
        }
        return true;
      case R.id.FILTER_DATE_COMMAND:
        if (!removeFilter(command)) {
          DateFilterDialog.newInstance()
              .show(getActivity().getSupportFragmentManager(), "AMOUNT_FILTER");
        }
        return true;
      case R.id.FILTER_COMMENT_COMMAND:
        if (!removeFilter(command)) {
          SimpleInputDialog.build()
              .title(R.string.search_comment)
              .pos(R.string.menu_search)
              .neut()
              .show(this, FILTER_COMMENT_DIALOG);
        }
        return true;
      case R.id.FILTER_STATUS_COMMAND:
        if (!removeFilter(command)) {
          SelectCrStatusDialogFragment.newInstance()
              .show(getActivity().getSupportFragmentManager(), "STATUS_FILTER");
        }
        return true;
      case R.id.FILTER_PAYEE_COMMAND:
        if (!removeFilter(command)) {
          SelectPayerDialogFragment.newInstance(mAccount.getId())
              .show(getActivity().getSupportFragmentManager(), "PAYER_FILTER");
        }
        return true;
      case R.id.FILTER_METHOD_COMMAND:
        if (!removeFilter(command)) {
          SelectMethodDialogFragment.newInstance(mAccount.getId())
              .show(getActivity().getSupportFragmentManager(), "METHOD_FILTER");
        }
        return true;
      case R.id.FILTER_TRANSFER_COMMAND:
        if (!removeFilter(command)) {
          SelectTransferAccountDialogFragment.newInstance(mAccount.getId())
              .show(getActivity().getSupportFragmentManager(), "TRANSFER_FILTER");
        }
        return true;
      case R.id.PRINT_COMMAND:
        MyExpenses ctx = (MyExpenses) getActivity();
        Result appDirStatus = AppDirHelper.checkAppDir(ctx);
        if (hasItems) {
          if (appDirStatus.isSuccess()) {
            ctx.contribFeatureRequested(ContribFeature.PRINT, null);
          } else {
            ctx.showSnackbar(appDirStatus.print(ctx), Snackbar.LENGTH_LONG);
          }
        } else {
          ctx.showExportDisabledCommand();
        }
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  public SparseArray<Criteria> getFilterCriteria() {
    return mFilter.getCriteria();
  }

  private void restoreFilterFromPreferences() {
    String filter = prefHandler.getString(prefNameForCriteria(KEY_CATID), null);
    if (filter != null) {
      mFilter.put(R.id.FILTER_CATEGORY_COMMAND, CategoryCriteria.fromStringExtra(filter));
    }
    filter = prefHandler.getString(prefNameForCriteria(KEY_AMOUNT), null);
    if (filter != null) {
      mFilter.put(R.id.FILTER_AMOUNT_COMMAND, AmountCriteria.fromStringExtra(filter));
    }
    filter = prefHandler.getString(prefNameForCriteria(KEY_COMMENT), null);
    if (filter != null) {
      mFilter.put(R.id.FILTER_COMMENT_COMMAND, CommentCriteria.fromStringExtra(filter));
    }
    filter = prefHandler.getString(prefNameForCriteria(KEY_CR_STATUS), null);
    if (filter != null) {
      mFilter.put(R.id.FILTER_STATUS_COMMAND, CrStatusCriteria.fromStringExtra(filter));
    }
    filter = prefHandler.getString(prefNameForCriteria(KEY_PAYEEID), null);
    if (filter != null) {
      mFilter.put(R.id.FILTER_PAYEE_COMMAND, PayeeCriteria.fromStringExtra(filter));
    }
    filter = prefHandler.getString(prefNameForCriteria(KEY_METHODID), null);
    if (filter != null) {
      mFilter.put(R.id.FILTER_METHOD_COMMAND, MethodCriteria.fromStringExtra(filter));
    }
    filter = prefHandler.getString(prefNameForCriteria(KEY_DATE), null);
    if (filter != null) {
      mFilter.put(R.id.FILTER_DATE_COMMAND, DateCriteria.fromStringExtra(filter));
    }
    filter = prefHandler.getString(prefNameForCriteria(KEY_TRANSFER_ACCOUNT), null);
    if (filter != null) {
      mFilter.put(R.id.FILTER_TRANSFER_COMMAND, TransferCriteria.fromStringExtra(filter));
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    if (requestCode == ProtectedFragmentActivity.FILTER_CATEGORY_REQUEST &&
        resultCode != Activity.RESULT_CANCELED) {
      String label = intent.getStringExtra(KEY_LABEL);
      if (resultCode == Activity.RESULT_OK) {
        long catId = intent.getLongExtra(KEY_CATID, 0);
        addCategoryFilter(label, catId);
      }
      if (resultCode == Activity.RESULT_FIRST_USER) {
        long[] catIds = intent.getLongArrayExtra(KEY_CATID);
        addCategoryFilter(label, catIds);
      }
    }
  }

  private void addCategoryFilter(String label, long... catIds) {
    addFilterCriteria(R.id.FILTER_CATEGORY_COMMAND, catIds.length == 1 && catIds[0] == -1 ?
        new NullCriteria(KEY_CATID) :
        new CategoryCriteria(label, catIds));
  }
}
