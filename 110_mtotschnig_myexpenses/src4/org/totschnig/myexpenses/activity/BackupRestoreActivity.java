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

package org.totschnig.myexpenses.activity;

import android.content.ComponentName;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;

import com.annimon.stream.Stream;

import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.dialog.BackupListDialogFragment;
import org.totschnig.myexpenses.dialog.BackupSourcesDialogFragment;
import org.totschnig.myexpenses.dialog.CommitSafeDialogFragment;
import org.totschnig.myexpenses.dialog.ConfirmationDialogFragment;
import org.totschnig.myexpenses.dialog.ConfirmationDialogFragment.ConfirmationDialogListener;
import org.totschnig.myexpenses.dialog.DialogUtils;
import org.totschnig.myexpenses.dialog.MessageDialogFragment;
import org.totschnig.myexpenses.preference.PrefKey;
import org.totschnig.myexpenses.task.RestoreTask;
import org.totschnig.myexpenses.task.TaskExecutionFragment;
import org.totschnig.myexpenses.ui.SnackbarAction;
import org.totschnig.myexpenses.util.AppDirHelper;
import org.totschnig.myexpenses.util.PermissionHelper;
import org.totschnig.myexpenses.util.Result;
import org.totschnig.myexpenses.util.ShareUtils;
import org.totschnig.myexpenses.util.Utils;
import org.totschnig.myexpenses.util.io.FileUtils;

import java.util.ArrayList;

import eltos.simpledialogfragment.SimpleDialog;
import eltos.simpledialogfragment.form.Input;
import eltos.simpledialogfragment.form.SimpleFormDialog;
import icepick.Icepick;
import icepick.State;
import timber.log.Timber;

import static org.totschnig.myexpenses.task.RestoreTask.KEY_PASSWORD;

public class BackupRestoreActivity extends ProtectedFragmentActivity
    implements ConfirmationDialogListener, SimpleDialog.OnDialogResultListener {
  public static final String FRAGMENT_TAG = "BACKUP_SOURCE";
  private static final String DIALOG_TAG_PASSWORD = "PASSWORD";

  private boolean calledFromOnboarding = false;

  public static final String ACTION_BACKUP = "BACKUP";
  public static final String ACTION_RESTORE = "RESTORE";
  public static final String ACTION_RESTORE_LEGACY = "RESTORE_LEGACY";

  @State
  int taskResult = RESULT_OK;

  public void onCreate(Bundle savedInstanceState) {
    setTheme(getThemeIdTranslucent());
    super.onCreate(savedInstanceState);
    ComponentName callingActivity = getCallingActivity();
    if (callingActivity != null && Utils.getSimpleClassNameFromComponentName(callingActivity)
        .equals(SplashActivity.class.getSimpleName())) {
      calledFromOnboarding = true;
      Timber.i("Called from onboarding");
    }
    if (savedInstanceState != null) {
      Icepick.restoreInstanceState(this, savedInstanceState);
      return;
    }
    String action = getIntent().getAction();
    switch (action == null ? "" : action) {
      case ACTION_BACKUP: {
        Result appDirStatus = AppDirHelper.checkAppDir(this);
        if (!appDirStatus.isSuccess()) {
          abort(appDirStatus.print(this));
          return;
        }
        DocumentFile appDir = AppDirHelper.getAppDir(this);
        if (appDir == null) {
          abort(getString(R.string.io_error_appdir_null));
          return;
        }
        boolean isProtected = !TextUtils.isEmpty(getPrefHandler().getString(PrefKey.EXPORT_PASSWORD, null));
        StringBuilder message = new StringBuilder();
        message.append(getString(R.string.warning_backup, FileUtils.getPath(this, appDir.getUri())))
            .append(" ");
        if (isProtected) {
          message.append(getString(R.string.warning_backup_protected)).append(" ");
        }
        message.append(getString(R.string.continue_confirmation));
        MessageDialogFragment.newInstance(
            isProtected ? R.string.dialog_title_backup_protected : R.string.menu_backup,
            message.toString(),
            new MessageDialogFragment.Button(android.R.string.yes,
                R.id.BACKUP_COMMAND, null), null,
            MessageDialogFragment.Button.noButton(), isProtected ? R.drawable.ic_lock : 0)
            .show(getSupportFragmentManager(), "BACKUP");
        break;
      }
      case ACTION_RESTORE_LEGACY: {
        Result appDirStatus = AppDirHelper.checkAppDir(this);
        if (appDirStatus.isSuccess()) {
          openBrowse();
        } else {
          abort(appDirStatus.print(this));
        }
        break;
      }
      case ACTION_RESTORE: {
        BackupSourcesDialogFragment.newInstance().show(
            getSupportFragmentManager(), FRAGMENT_TAG);
        break;

      }
    }
  }

  private void abort(String message) {
    showMessage(message);
  }

  private void showRestoreDialog(Uri fileUri, int restorePlanStrategy) {
    Bundle bundle = buildRestoreArgs(fileUri, restorePlanStrategy);
    bundle.putInt(ConfirmationDialogFragment.KEY_TITLE, R.string.pref_restore_title);
    final String message = getString(R.string.warning_restore, DialogUtils.getDisplayName(fileUri))
        + " " + getString(R.string.continue_confirmation);
    bundle.putString(
        ConfirmationDialogFragment.KEY_MESSAGE,
        message);
    bundle.putInt(ConfirmationDialogFragment.KEY_COMMAND_POSITIVE,
        R.id.RESTORE_COMMAND);
    ConfirmationDialogFragment.newInstance(bundle).show(getSupportFragmentManager(),
        "RESTORE");
  }

  private Bundle buildRestoreArgs(Uri fileUri, int restorePlanStrategie) {
    Bundle bundle = new Bundle();
    bundle.putInt(RestoreTask.KEY_RESTORE_PLAN_STRATEGY, restorePlanStrategie);
    bundle.putParcelable(TaskExecutionFragment.KEY_FILE_PATH, fileUri);
    return bundle;
  }

  @Override
  public boolean dispatchCommand(int command, Object tag) {
    if (super.dispatchCommand(command, tag))
      return true;
    switch (command) {
      case R.id.BACKUP_COMMAND:
        if (AppDirHelper.checkAppFolderWarning(this)) {
          doBackup();
        } else {
          Bundle b = new Bundle();
          b.putInt(ConfirmationDialogFragment.KEY_TITLE,
              R.string.dialog_title_attention);
          b.putCharSequence(
              ConfirmationDialogFragment.KEY_MESSAGE,
              Utils.getTextWithAppName(this, R.string.warning_app_folder_will_be_deleted_upon_uninstall));
          b.putInt(ConfirmationDialogFragment.KEY_COMMAND_POSITIVE,
              R.id.BACKUP_COMMAND_DO);
          b.putString(ConfirmationDialogFragment.KEY_PREFKEY,
              PrefKey.APP_FOLDER_WARNING_SHOWN.getKey());
          ConfirmationDialogFragment.newInstance(b).show(
              getSupportFragmentManager(), "APP_FOLDER_WARNING");
        }
        return true;
    }
    return false;
  }

  protected void doBackup() {
    Result appDirStatus = AppDirHelper.checkAppDir(this);//TODO this check leads to strict mode violation, can we get rid of it ?
    if (appDirStatus.isSuccess()) {
      startTaskExecution(TaskExecutionFragment.TASK_BACKUP, null, getPrefHandler().getString(PrefKey.EXPORT_PASSWORD, null),
          R.string.menu_backup, true);
    } else {
      abort(appDirStatus.print(this));
    }
  }

  @Override
  public void onPostExecute(int taskId, Object result) {
    super.onPostExecute(taskId, result);
    Result<DocumentFile> r = (Result<DocumentFile>) result;
    switch (taskId) {
      case TaskExecutionFragment.TASK_BACKUP: {
        if (!r.isSuccess()) {
          onProgressUpdate(r.print(this));
        } else {
          Uri backupFileUri = r.getExtra().getUri();
          onProgressUpdate(getString(r.getMessage(), FileUtils.getPath(this, backupFileUri)));
          if (PrefKey.PERFORM_SHARE.getBoolean(false)) {
            ArrayList<Uri> uris = new ArrayList<>();
            uris.add(backupFileUri);
            Result shareResult = ShareUtils.share(this, uris,
                PrefKey.SHARE_TARGET.getString("").trim(),
                "application/zip");
            if (!shareResult.isSuccess()) {
              onProgressUpdate(shareResult.print(this));
            }
          }
        }
        break;
      }
    }
  }

  @Override
  protected boolean shouldKeepProgress(int taskId) {
    return true;
  }

  @Override
  protected void onPostRestoreTask(Result result) {
    super.onPostRestoreTask(result);
    onProgressUpdate(result);
    if (result.isSuccess()) {
      taskResult = RESULT_RESTORE_OK;
    }
  }

  public void onSourceSelected(Uri mUri, int restorePlanStrategy) {
    if (calledFromOnboarding) {
      final Bundle args = buildRestoreArgs(mUri, restorePlanStrategy);
      if (FileUtils.getPath(this, mUri).endsWith("enc")) {
        SimpleFormDialog.build().msg(R.string.backup_is_encrypted)
            .fields(Input.password(KEY_PASSWORD).required())
            .extra(args)
            .show(this, DIALOG_TAG_PASSWORD);
      } else {
        doRestore(args);
      }
    } else {
      showRestoreDialog(mUri, restorePlanStrategy);
    }
  }

  @Override
  public boolean onResult(@NonNull String dialogTag, int which, @NonNull Bundle extras) {
    if (which == BUTTON_POSITIVE) {
      switch (dialogTag) {
        case DIALOG_TAG_PASSWORD: {
          doRestore(extras);
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public void onPositive(Bundle args) {
    switch (args.getInt(ConfirmationDialogFragment.KEY_COMMAND_POSITIVE)) {
      case R.id.BACKUP_COMMAND_DO:
        doBackup();
        break;
      case R.id.RESTORE_COMMAND:
        doRestore(args);
        break;
    }
  }

  public void openBrowse() {
    if (hasBackups()) {
      BackupListDialogFragment.newInstance().show(
          getSupportFragmentManager(), FRAGMENT_TAG);
    } else {
      abort(getString(R.string.restore_no_backup_found));
    }
  }

  public boolean hasBackups() {
    DocumentFile appDir = AppDirHelper.getAppDir(this);
    return appDir != null && Stream.of(appDir.listFiles())
        .anyMatch(documentFile -> {
          final String name = documentFile.getName();
          return name != null && name.endsWith(".zip");
        });
  }

  @Override
  public void onNegative(Bundle args) {
    setResult(RESULT_CANCELED);
    finish();
  }

  @Override
  public void onDismissOrCancel(Bundle args) {
    setResult(RESULT_CANCELED);
    finish();
  }

  @Override
  public void onProgressDialogDismiss() {
    setResult(taskResult);
    finish();
  }

  @Override
  public void onMessageDialogDismissOrCancel() {
    finish();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    switch (requestCode) {
      case PermissionHelper.PERMISSIONS_REQUEST_WRITE_CALENDAR:
        if (!PermissionHelper.allGranted(grantResults)) {
          ((DialogUtils.CalendarRestoreStrategyChangedListener)
              getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG)).onCalendarPermissionDenied();
        }
        return;
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  @Override
  public void showSnackbar(@NonNull CharSequence message, int duration, SnackbarAction snackbarAction) {
    final CommitSafeDialogFragment fragment = (CommitSafeDialogFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
    if (fragment != null) {
      fragment.showSnackbar(message, duration, snackbarAction);
    } else {
      super.showSnackbar(message, duration, snackbarAction);
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    Icepick.saveInstanceState(this, outState);
  }
}