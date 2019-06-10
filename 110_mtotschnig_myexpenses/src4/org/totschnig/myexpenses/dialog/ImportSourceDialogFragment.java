package org.totschnig.myexpenses.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.activity.ProtectedFragmentActivity;
import org.totschnig.myexpenses.dialog.MessageDialogFragment.MessageDialogListener;
import org.totschnig.myexpenses.util.ImportFileResultHandler;
import org.totschnig.myexpenses.util.PermissionHelper;

public abstract class ImportSourceDialogFragment extends CommitSafeDialogFragment
    implements OnClickListener, DialogInterface.OnClickListener, ImportFileResultHandler.FileNameHostFragment {

  protected EditText mFilename;

  public Uri getUri() {
    return mUri;
  }

  @Override
  public void setUri(Uri uri) {
    mUri = uri;
  }

  @Override
  public EditText getFilenameEditText() {
    return mFilename;
  }

  protected Uri mUri;

  public ImportSourceDialogFragment() {
    super();
  }
  abstract int getLayoutId();
  abstract String getLayoutTitle();
  public boolean checkTypeParts(String[] typeParts) {
   return ImportFileResultHandler.checkTypePartsDefault(typeParts);
  }

  @Override
  public void onCancel (DialogInterface dialog) {
    if (getActivity()==null) {
      return;
    }
    //TODO: we should not depend on 
    ((MessageDialogListener) getActivity()).onMessageDialogDismissOrCancel();
  }
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    LayoutInflater li = LayoutInflater.from(getActivity());
    dialogView = li.inflate(getLayoutId(), null);
    setupDialogView(dialogView);
    return new AlertDialog.Builder(getActivity())
      .setTitle(getLayoutTitle())
      .setView(dialogView)
      .setPositiveButton(android.R.string.ok,this)
      .setNegativeButton(android.R.string.cancel,this)
      .create();
  }
  @Override
  public void onDestroyView() {
    super.onDestroyView();
    mFilename = null;
  }

  protected void setupDialogView(View view) {
    mFilename = DialogUtils.configureFilename(view);

    view.findViewById(R.id.btn_browse).setOnClickListener(this);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == ProtectedFragmentActivity.IMPORT_FILENAME_REQUESTCODE) {
      if (resultCode == Activity.RESULT_OK && data != null) {
        try {
          mUri = ImportFileResultHandler.handleFilenameRequestResult(this, data);
        } catch (Throwable throwable) {
          mUri = null;
          showSnackbar(throwable.getMessage(), Snackbar.LENGTH_LONG, null);
        }
      }
    }
  }

  @Override
  public void onClick(DialogInterface dialog, int id) {
    if (id == AlertDialog.BUTTON_NEGATIVE) {
      onCancel(dialog);
    }
  }
  @Override
  public void onResume() {
    super.onResume();
    ImportFileResultHandler.handleFileNameHostOnResume(this);
    setButtonState();
  }

  //we cannot persist document Uris because we use ACTION_GET_CONTENT instead of ACTION_OPEN_DOCUMENT
  protected void maybePersistUri() {
    ImportFileResultHandler.maybePersistUri(this);
  }

  @Override
  public void onClick(View v) {
   DialogUtils.openBrowse(mUri, this);
  }

  protected boolean isReady() {
    return mUri != null && PermissionHelper.canReadUri(mUri, getContext());
  }

  protected void setButtonState() {
    ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(isReady());
  }
  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (mUri != null) {
      outState.putString(getPrefKey(), mUri.toString());
    }
  }
  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    if (savedInstanceState != null) {
      String restoredUriString = savedInstanceState.getString(getPrefKey());
      if (restoredUriString != null) {
        Uri restoredUri = Uri.parse(restoredUriString);
        String displayName = DialogUtils.getDisplayName(restoredUri);
        if (displayName != null) {
          mUri = restoredUri;
          mFilename.setText(displayName);
        }
      }
    }
  }
}