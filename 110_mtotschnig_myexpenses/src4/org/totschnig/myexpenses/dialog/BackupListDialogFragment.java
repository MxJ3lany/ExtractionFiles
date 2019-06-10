package org.totschnig.myexpenses.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.annimon.stream.Stream;

import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.activity.BackupRestoreActivity;
import org.totschnig.myexpenses.dialog.MessageDialogFragment.MessageDialogListener;
import org.totschnig.myexpenses.util.AppDirHelper;

public class BackupListDialogFragment extends CommitSafeDialogFragment
    implements DialogInterface.OnClickListener,DialogUtils.CalendarRestoreStrategyChangedListener {
  RadioGroup mRestorePlanStrategie;
  Spinner selectBackupSpinner;
  RadioGroup.OnCheckedChangeListener mCalendarRestoreButtonCheckedChangeListener;
  DocumentFile[] backupFiles;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    backupFiles = listBackups();
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    LayoutInflater li = LayoutInflater.from(getActivity());
    //noinspection InflateParams
    View view = li.inflate(R.layout.backup_restore_fallback_dialog, null);
    ArrayAdapter<DocumentFileItem> adapter = new ArrayAdapter<>(getActivity(),
        android.R.layout.simple_spinner_item,
        Stream.of(backupFiles).map(DocumentFileItem::new).toArray(DocumentFileItem[]::new));
    selectBackupSpinner = view.findViewById(R.id.select_backup);
    selectBackupSpinner.setAdapter(adapter);
    adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
    mRestorePlanStrategie = DialogUtils.configureCalendarRestoreStrategy(view);
    if (mRestorePlanStrategie != null) {
      mCalendarRestoreButtonCheckedChangeListener =
          DialogUtils.buildCalendarRestoreStrategyChangedListener(getActivity(), this);
      mRestorePlanStrategie.setOnCheckedChangeListener(mCalendarRestoreButtonCheckedChangeListener);
    }
    return new AlertDialog.Builder(getActivity())
        .setTitle(R.string.pref_restore_title)
        .setView(view)
        .setPositiveButton(android.R.string.ok, this)
        .setNegativeButton(android.R.string.cancel, this)
        .create();
  }

  public static BackupListDialogFragment newInstance() {
    BackupListDialogFragment f = new BackupListDialogFragment();
    return f;
  }
  @Override
  public void onCancel (DialogInterface dialog) {
    if (getActivity()==null) {
      return;
    }
    ((MessageDialogListener) getActivity()).onMessageDialogDismissOrCancel();
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {
    if (getActivity()==null) {
      return;
    }
    if (which == AlertDialog.BUTTON_POSITIVE) {
      int position = selectBackupSpinner.getSelectedItemPosition();
      if (position!= AdapterView.INVALID_POSITION) {
        ((BackupRestoreActivity) getActivity()).onSourceSelected(
            backupFiles[position].getUri(),
            mRestorePlanStrategie == null ? R.id.restore_calendar_handling_ignore :
                mRestorePlanStrategie.getCheckedRadioButtonId());
        return;
      }
    }
    onCancel(dialog);
  }

  @Override
  public void onResume() {
    super.onResume();
    setButtonState();
  }

  private void setButtonState() {
    ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(
        mRestorePlanStrategie == null || mRestorePlanStrategie.getCheckedRadioButtonId() != -1);
  }

  @Override
  public void onCheckedChanged() {
    setButtonState();
  }

  @Override
  public void onCalendarPermissionDenied() {
    mRestorePlanStrategie.setOnCheckedChangeListener(null);
    mRestorePlanStrategie.clearCheck();
    mRestorePlanStrategie.setOnCheckedChangeListener(mCalendarRestoreButtonCheckedChangeListener);
    setButtonState();
  }

  public DocumentFile[] listBackups() {
    DocumentFile appDir = AppDirHelper.getAppDir(getContext());
    return appDir != null ? Stream.of(appDir.listFiles())
        .filter(documentFile -> documentFile.getName().endsWith(".zip"))
        .toArray(DocumentFile[]::new) : new DocumentFile[]{};
  }

  private static class DocumentFileItem {
    private final DocumentFile documentFile;

    private DocumentFileItem(DocumentFile documentFile) {
      this.documentFile = documentFile;
    }

    @Override
    public String toString() {
      return documentFile.getName();
    }

  }
}
