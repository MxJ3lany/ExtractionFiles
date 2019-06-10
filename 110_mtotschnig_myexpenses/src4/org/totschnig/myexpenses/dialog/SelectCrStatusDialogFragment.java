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

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.SparseBooleanArray;
import android.widget.ListView;

import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.activity.MyExpenses;
import org.totschnig.myexpenses.model.Transaction;
import org.totschnig.myexpenses.provider.filter.CrStatusCriteria;

import java.util.ArrayList;

/**
 * uses {@link MessageDialogFragment.MessageDialogListener} to dispatch result back to activity
 */
public class SelectCrStatusDialogFragment extends CommitSafeDialogFragment implements OnClickListener {

  public static SelectCrStatusDialogFragment newInstance() {
    return new SelectCrStatusDialogFragment();
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    String[] items = new String[Transaction.CrStatus.values().length];
    for (int i = 0; i < Transaction.CrStatus.values().length; i++) {
      items[i] = getString(Transaction.CrStatus.values()[i].toStringRes());
    }
    return new AlertDialog.Builder(getActivity())
        .setTitle(R.string.search_status)
        .setMultiChoiceItems(items, null, null)
        .setPositiveButton(android.R.string.ok, this)
        .setNegativeButton(android.R.string.cancel, null)
        .create();
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {
    if (getActivity() == null) {
      return;
    }

    ListView listView = ((AlertDialog) getDialog()).getListView();

    SparseBooleanArray positions = listView.getCheckedItemPositions();

    ArrayList<String> statusList = new ArrayList<>();
    for (int i = 0; i < positions.size(); i++) {
      if (positions.valueAt(i)) {
        statusList.add(Transaction.CrStatus.values()[positions.keyAt(i)].name());
      }
    }
    if (!statusList.isEmpty() && statusList.size() < Transaction.CrStatus.values().length) {
      ((MyExpenses) getActivity()).addFilterCriteria(
          R.id.FILTER_STATUS_COMMAND,
          new CrStatusCriteria(statusList.toArray(new String[statusList.size()])));
    }
    dismiss();
  }
}