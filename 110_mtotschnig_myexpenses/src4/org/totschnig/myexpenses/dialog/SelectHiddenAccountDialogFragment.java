package org.totschnig.myexpenses.dialog;

import android.content.DialogInterface;
import android.net.Uri;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.activity.ProtectedFragmentActivity;
import org.totschnig.myexpenses.provider.DatabaseConstants;
import org.totschnig.myexpenses.provider.TransactionProvider;

import java.util.List;

import static org.totschnig.myexpenses.task.TaskExecutionFragment.TASK_SET_ACCOUNT_HIDDEN;

public class SelectHiddenAccountDialogFragment extends SelectFromTableDialogFragment {

  public SelectHiddenAccountDialogFragment() {
    super(false);
  }

  public static SelectHiddenAccountDialogFragment newInstance() {
    return new SelectHiddenAccountDialogFragment();
  }

  @Override
  int getDialogTitle() {
    return R.string.menu_hidden_accounts;
  }

  @Override
  Uri getUri() {
    return TransactionProvider.ACCOUNTS_URI;
  }

  @Override
  String getColumn() {
    return DatabaseConstants.KEY_LABEL;

  }

  @Override
  boolean onResult(List<String> labelList, long[] itemIds, int which) {
    switch (which) {
      case DialogInterface.BUTTON_POSITIVE:
        if (itemIds.length > 0) {
          ((ProtectedFragmentActivity) getActivity()).startTaskExecution(TASK_SET_ACCOUNT_HIDDEN,
              ArrayUtils.toObject(itemIds), false, 0);
        }
        return true;
      case DialogInterface.BUTTON_NEUTRAL:
        if (itemIds.length > 0) {
          final String message = Stream.of(labelList).map(label -> getString(R.string.warning_delete_account, label))
              .collect(Collectors.joining(" ")) + " " + getString(R.string.continue_confirmation);
          MessageDialogFragment.newInstance(
              getResources().getQuantityString(R.plurals.dialog_title_warning_delete_account, itemIds.length, itemIds.length),
              message,
              new MessageDialogFragment.Button(R.string.menu_delete, R.id.DELETE_ACCOUNT_COMMAND_DO,
                  ArrayUtils.toObject(itemIds)),
              null,
              MessageDialogFragment.Button.noButton(), 0)
              .show(getChildFragmentManager(), "DELETE_ACCOUNTS");
          return false;
        }
        return true;
    }
    return false;
  }

  @Override
  String[] getSelectionArgs() {
    return null;
  }

  @Override
  String getSelection() {
    return DatabaseConstants.KEY_HIDDEN + " = 1";
  }

  @Override
  protected int getPositiveButton() {
    return R.string.button_label_show;
  }

  @Override
  protected int getNeutralButton() {
    return R.string.menu_delete;
  }
}
