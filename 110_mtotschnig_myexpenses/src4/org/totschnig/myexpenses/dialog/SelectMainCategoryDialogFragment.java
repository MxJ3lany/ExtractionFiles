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
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.SimpleCursorAdapter;

import org.apache.commons.lang3.ArrayUtils;
import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.provider.TransactionProvider;

import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_LABEL;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_PARENTID;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_ROWID;

public class SelectMainCategoryDialogFragment extends CommitSafeDialogFragment implements OnClickListener,
    LoaderManager.LoaderCallbacks<Cursor> {
  public static final String KEY_RESULT = "result";
  public static final String KEY_WITH_ROOT = "with_root";
  public static final String KEY_EXCLUDED_ID = "excluded_id";
  protected SimpleCursorAdapter mAdapter;
  protected Cursor mCursor;
  private String[] projection = new String[]{
      KEY_ROWID,
      KEY_LABEL
  };

  public interface CategorySelectedListener {
    void onCategorySelected(Bundle args);
  }

  public static SelectMainCategoryDialogFragment newInstance(Bundle args) {
    final SelectMainCategoryDialogFragment fragment = new SelectMainCategoryDialogFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    mAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.select_dialog_item, null,
        new String[]{KEY_LABEL}, new int[]{android.R.id.text1}, 0);
    getLoaderManager().initLoader(0, null, this);
    final AlertDialog dialog = new AlertDialog.Builder(getActivity())
        .setTitle(R.string.dialog_title_select_target)
        .setAdapter(mAdapter, this)
        .create();
    //dialog.getListView().setItemsCanFocus(false);
    return dialog;
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {
    if (getActivity() == null || mCursor == null) {
      return;
    }
    Bundle args = getArguments();
    args.putLong(KEY_RESULT, ((AlertDialog) dialog).getListView().getItemIdAtPosition(which));
    ((CategorySelectedListener) getActivity()).onCategorySelected(args);
    dismiss();
  }

  @Override
  public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
    if (getActivity() == null) {
      return null;
    }
    String selection = KEY_PARENTID + " is null AND "+ KEY_ROWID + " NOT IN (" +
        TextUtils.join(",", ArrayUtils.toObject(getArguments().getLongArray(KEY_EXCLUDED_ID)))+ ")";

    CursorLoader cursorLoader = new CursorLoader(
        getActivity(),
        TransactionProvider.CATEGORIES_URI,
        projection,
        selection,
        null,
        null);
    return cursorLoader;

  }

  @Override
  public void onLoadFinished(Loader<Cursor> arg0, Cursor data) {
    if (getArguments().getBoolean(KEY_WITH_ROOT)) {
      MatrixCursor extras = new MatrixCursor(projection);
      extras.addRow(new String[]{
          "0",
          getString(R.string.transform_subcategory_to_main),
      });
      mCursor = new MergeCursor(new Cursor[]{extras, data});
    } else {
      mCursor = data;
    }
    mAdapter.swapCursor(mCursor);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> arg0) {
    mCursor = null;
    mAdapter.swapCursor(null);
  }
}