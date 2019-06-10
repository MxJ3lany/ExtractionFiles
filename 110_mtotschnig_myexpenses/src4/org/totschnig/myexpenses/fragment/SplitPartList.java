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

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;

import org.totschnig.myexpenses.MyApplication;
import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.activity.ExpenseEdit;
import org.totschnig.myexpenses.activity.MyExpenses;
import org.totschnig.myexpenses.activity.ProtectedFragmentActivity;
import org.totschnig.myexpenses.adapter.SplitPartAdapter;
import org.totschnig.myexpenses.model.Account;
import org.totschnig.myexpenses.model.Money;
import org.totschnig.myexpenses.model.Template;
import org.totschnig.myexpenses.model.Transaction;
import org.totschnig.myexpenses.provider.TransactionProvider;
import org.totschnig.myexpenses.task.TaskExecutionFragment;
import org.totschnig.myexpenses.util.CurrencyFormatter;
import org.totschnig.myexpenses.util.TextUtils;
import org.totschnig.myexpenses.util.UiUtils;
import org.totschnig.myexpenses.util.Utils;

import javax.inject.Inject;

import icepick.Icepick;
import icepick.State;

import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_ACCOUNTID;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_AMOUNT;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_LABEL_MAIN;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_PARENTID;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_ROWID;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_TEMPLATEID;

//TODO: consider moving to ListFragment
public class SplitPartList extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
  private static final String KEY_PARENT_IS_TEMPLATE = "parentIsTemplate";
  //
  SplitPartAdapter mAdapter;
  private TextView balanceTv;
  private long transactionSum = 0;
  private Money unsplitAmount;
  private FloatingActionButton fab;

  @State
  long parentId;

  @State
  long accountId;

  @Inject
  CurrencyFormatter currencyFormatter;

  public static SplitPartList newInstance(Transaction transaction) {
    SplitPartList f = new SplitPartList(); 
    Bundle bundle = new Bundle();
    bundle.putLong(KEY_PARENTID, transaction.getId());
    bundle.putLong(KEY_ACCOUNTID, transaction.getAccountId());
    bundle.putBoolean(KEY_PARENT_IS_TEMPLATE, transaction instanceof Template);
    f.setArguments(bundle);
    return f;
  }
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
    setRetainInstance(true);
    if (savedInstanceState == null) {
      parentId = getArguments().getLong(KEY_PARENTID);
      accountId = getArguments().getLong(KEY_ACCOUNTID);
    } else {
      Icepick.restoreInstanceState(this, savedInstanceState);
    }
    MyApplication.getInstance().getAppComponent().inject(this);
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    Icepick.saveInstanceState(this, outState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    final FragmentActivity ctx = getActivity();
    View v = inflater.inflate(R.layout.split_parts_list, container, false);
    View emptyView = v.findViewById(R.id.empty);
    balanceTv = (TextView) v.findViewById(R.id.end);
    
    ListView lv = (ListView) v.findViewById(R.id.list);
    // Create an array to specify the fields we want to display in the list
    String[] from = new String[]{KEY_LABEL_MAIN,KEY_AMOUNT};

    // and an array of the fields we want to bind those fields to 
    int[] to = new int[]{R.id.category,R.id.amount};

    requireLoaders();
    // Now create a simple cursor adapter and set it to display
    final Account account = Account.getInstanceFromDb(accountId);
    mAdapter = new SplitPartAdapter(ctx, R.layout.split_part_row, null, from, to, 0,
        account.getCurrencyUnit(), currencyFormatter);
    lv.setAdapter(mAdapter);
    lv.setEmptyView(emptyView);
    lv.setOnItemClickListener((a, v1, position, id) -> {
      Intent i = new Intent(ctx, ExpenseEdit.class);
      i.putExtra(parentIsTemplate() ? KEY_TEMPLATEID : KEY_ROWID, id);
      startActivityForResult(i, MyExpenses.EDIT_TRANSACTION_REQUEST);
    });
    registerForContextMenu(lv);
    fab = v.findViewById(R.id.CREATE_COMMAND);
    fab.setContentDescription(TextUtils.concatResStrings(getActivity(), ". ",
        R.string.menu_create_split_part_category, R.string.menu_create_split_part_transfer));
    updateFabColor(account.color);
    return v;
  }

  public void updateFabColor(int color) {
    UiUtils.setBackgroundTintListOnFab(fab,color);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v,
      ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    menu.add(0, R.id.DELETE_COMMAND, 0, R.string.menu_delete);
  }
  @Override
  public boolean onContextItemSelected(android.view.MenuItem item) {
    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    switch(item.getItemId()) {
    case R.id.DELETE_COMMAND:
      ((ProtectedFragmentActivity) getActivity()).startTaskExecution(
          parentIsTemplate() ? TaskExecutionFragment.TASK_DELETE_TEMPLATES : TaskExecutionFragment.TASK_DELETE_TRANSACTION,
          new Long[] {info.id},
          Boolean.FALSE,
          0);
      return true;
    }
    return super.onContextItemSelected(item);
  }
  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    String[] selectionArgs = new String[] {String.valueOf(parentId)};
    CursorLoader cursorLoader = null;
    Uri uri = parentIsTemplate() ?
        TransactionProvider.TEMPLATES_UNCOMMITTED_URI : TransactionProvider.UNCOMMITTED_URI;
    switch(id) {
    case ExpenseEdit.TRANSACTION_CURSOR:
      cursorLoader = new CursorLoader(getActivity(), uri, null, "parent_id = ?",
          selectionArgs, null);
      return cursorLoader;
    case ExpenseEdit.SUM_CURSOR:
      cursorLoader = new CursorLoader(getActivity(),uri,
          new String[] {"sum(" + KEY_AMOUNT + ")"}, "parent_id = ?",
          selectionArgs, null);
    }
    return cursorLoader;
  }

  private boolean parentIsTemplate() {
    return getArguments().getBoolean(KEY_PARENT_IS_TEMPLATE);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
    switch(arg0.getId()) {
    case ExpenseEdit.TRANSACTION_CURSOR:
      mAdapter.swapCursor(c);
      break;
    case ExpenseEdit.SUM_CURSOR:
      c.moveToFirst();
      transactionSum = c.getLong(0);
      updateBalance();
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> arg0) {
    switch(arg0.getId()) {
    case ExpenseEdit.TRANSACTION_CURSOR:
      mAdapter.swapCursor(null);
      break;
    }
  }
  public void updateBalance() {
    ExpenseEdit ctx = (ExpenseEdit) getActivity();
    if (ctx == null)
      return;
    unsplitAmount = ctx.getAmount();
    //when we are called before transaction is loaded in parent activity
    if (unsplitAmount == null)
      return;
    unsplitAmount = new Money(unsplitAmount.getCurrencyUnit(), unsplitAmount.getAmountMinor()-transactionSum);
    if (balanceTv != null)
      balanceTv.setText(currencyFormatter.formatCurrency(unsplitAmount));
  }

  public boolean splitComplete() {
    return unsplitAmount != null && unsplitAmount.getAmountMinor() == 0L;
  }
  public int getSplitCount() {
    return mAdapter.getCount();
  }

  public void updateAccount(Account account) {
    accountId = account.getId();
    mAdapter.setCurrency(account.getCurrencyUnit());
    mAdapter.notifyDataSetChanged();
    updateBalance();
    updateFabColor(account.color);
  }

  public void updateParent(long parentId) {
    this.parentId = parentId;
    requireLoaders();
  }

  private void requireLoaders() {
    Utils.requireLoader(getActivity().getSupportLoaderManager(), ExpenseEdit.TRANSACTION_CURSOR, null, this);
    Utils.requireLoader(getActivity().getSupportLoaderManager(), ExpenseEdit.SUM_CURSOR, null, this);
  }
}
