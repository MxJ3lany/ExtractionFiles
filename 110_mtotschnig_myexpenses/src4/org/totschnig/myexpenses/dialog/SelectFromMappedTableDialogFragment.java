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

package org.totschnig.myexpenses.dialog;

import android.os.Bundle;
import android.text.TextUtils;

import org.apache.commons.lang3.ArrayUtils;
import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.activity.MyExpenses;
import org.totschnig.myexpenses.model.AggregateAccount;
import org.totschnig.myexpenses.provider.filter.Criteria;

import java.util.Arrays;
import java.util.List;

import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_ACCOUNTID;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_CODE;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_CURRENCY;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_LABEL;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_ROWID;
import static org.totschnig.myexpenses.provider.DatabaseConstants.TABLE_ACCOUNTS;
import static org.totschnig.myexpenses.provider.DatabaseConstants.TABLE_CURRENCIES;

public abstract class SelectFromMappedTableDialogFragment extends SelectFromTableDialogFragment {

  protected SelectFromMappedTableDialogFragment(boolean withNullItem) {
    super(withNullItem);
  }

  abstract Criteria makeCriteria(String label, long... id);

  abstract int getCommand();

  @Override
  String getColumn() {
    return KEY_LABEL;
  }

  @Override
  boolean onResult(List<String> labelList, long[] itemIds, int which) {
    if (itemIds.length == 1 || Arrays.asList(ArrayUtils.toObject(itemIds)).indexOf(-1L) == -1) {
      ((MyExpenses) getActivity()).addFilterCriteria(
          getCommand(),
          makeCriteria(TextUtils.join(",", labelList), itemIds));
      return true;
    } else {
      showSnackbar(R.string.unmapped_filter_only_single);
      return false;
    }
  }

  @Override
  String getSelection() {
    final long rowId = getArguments().getLong(KEY_ROWID);
    if (rowId > 0) {
      return KEY_ACCOUNTID + " = ?";
    } else if (rowId != AggregateAccount.HOME_AGGREGATE_ID) {
      return KEY_ACCOUNTID + " IN " +
          "(SELECT " + KEY_ROWID + " FROM " + TABLE_ACCOUNTS + " WHERE " + KEY_CURRENCY +
          " = (SELECT " + KEY_CODE + " FROM " + TABLE_CURRENCIES + " WHERE " + KEY_ROWID + " = ?))";
    }
    return null;
  }

  @Override
  String[] getSelectionArgs() {
    final long rowId = getArguments().getLong(KEY_ROWID);
    return rowId == AggregateAccount.HOME_AGGREGATE_ID ? null : new String[]{String.valueOf(Math.abs(rowId))};
  }

  protected static void setArguments(SelectFromMappedTableDialogFragment dialogFragment, long rowId) {
    Bundle args = new Bundle(1);
    args.putLong(KEY_ROWID, rowId);
    dialogFragment.setArguments(args);
  }
}