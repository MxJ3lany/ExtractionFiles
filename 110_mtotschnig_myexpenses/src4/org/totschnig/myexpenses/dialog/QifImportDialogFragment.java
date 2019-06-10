package org.totschnig.myexpenses.dialog;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import org.totschnig.myexpenses.MyApplication;
import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.activity.QifImport;
import org.totschnig.myexpenses.adapter.CurrencyAdapter;
import org.totschnig.myexpenses.export.qif.QifDateFormat;
import org.totschnig.myexpenses.model.ExportFormat;
import org.totschnig.myexpenses.provider.TransactionProvider;
import org.totschnig.myexpenses.util.UiUtils;
import org.totschnig.myexpenses.util.Utils;
import org.totschnig.myexpenses.viewmodel.CurrencyViewModel;
import org.totschnig.myexpenses.viewmodel.data.Currency;

import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_ACCOUNTID;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_CURRENCY;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_LABEL;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_ROWID;

public class QifImportDialogFragment extends TextSourceDialogFragment implements
    LoaderManager.LoaderCallbacks<Cursor>, OnItemSelectedListener {
  Spinner mAccountSpinner, mDateFormatSpinner, mCurrencySpinner, mEncodingSpinner;
  private SimpleCursorAdapter mAccountsAdapter;

  public static final String PREFKEY_IMPORT_DATE_FORMAT = "import_qif_date_format";
  public static final String PREFKEY_IMPORT_ENCODING = "import_qif_encoding";
  private MergeCursor mAccountsCursor;
  private long accountId = 0;
  private String currency = null;
  private CurrencyViewModel currencyViewModel;

  public static QifImportDialogFragment newInstance() {
    return new QifImportDialogFragment();
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    currencyViewModel = ViewModelProviders.of(this).get(CurrencyViewModel.class);
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    if (savedInstanceState != null) {
      accountId = savedInstanceState.getLong(KEY_ACCOUNTID);
      currency = savedInstanceState.getString(KEY_CURRENCY);
    }
    return super.onCreateDialog(savedInstanceState);
  }

  @Override
  protected int getLayoutId() {
    return R.layout.import_dialog;
  }

  @Override
  protected String getLayoutTitle() {
    return getString(R.string.pref_import_title,
        getFormat().name());
  }

  private ExportFormat getFormat() {
    return ExportFormat.QIF;
  }

  @Override
  public String getTypeName() {
    return getFormat().name();
  }

  @Override
  public String getPrefKey() {
    return "import_" + getFormat().getExtension() + "_file_uri";
  }

  @Override
  public void onClick(DialogInterface dialog, int id) {
    if (getActivity() == null) {
      return;
    }
    if (id == AlertDialog.BUTTON_POSITIVE) {
      QifDateFormat format = (QifDateFormat) mDateFormatSpinner.getSelectedItem();
      String encoding = (String) mEncodingSpinner.getSelectedItem();
      maybePersistUri();
      MyApplication.getInstance().getSettings().edit()
          .putString(PREFKEY_IMPORT_ENCODING, encoding)
          .putString(PREFKEY_IMPORT_DATE_FORMAT, format.name())
          .apply();
      ((QifImport) getActivity()).onSourceSelected(
          mUri,
          format,
          mAccountSpinner.getSelectedItemId(),
          ((Currency) mCurrencySpinner.getSelectedItem()).code(),
          mImportTransactions.isChecked(),
          mImportCategories.isChecked(),
          mImportParties.isChecked(),
          encoding
      );
    } else {
      super.onClick(dialog, id);
    }
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    if (getActivity() == null) {
      return null;
    }
    CursorLoader cursorLoader = new CursorLoader(
        getActivity(),
        TransactionProvider.ACCOUNTS_BASE_URI,
        new String[]{
            KEY_ROWID,
            KEY_LABEL,
            KEY_CURRENCY},
        null, null, null);
    return cursorLoader;
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    MatrixCursor extras = new MatrixCursor(new String[]{
        KEY_ROWID,
        KEY_LABEL,
        KEY_CURRENCY
    });
    extras.addRow(new String[]{
        "0",
        getString(R.string.menu_create_account),
        Utils.getHomeCurrency().code()
    });
    mAccountsCursor = new MergeCursor(new Cursor[]{extras, data});
    mAccountsAdapter.swapCursor(mAccountsCursor);
    UiUtils.selectSpinnerItemByValue(mAccountSpinner, accountId);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    mAccountsCursor = null;
    mAccountsAdapter.swapCursor(null);
  }

  @Override
  protected void setupDialogView(View view) {
    super.setupDialogView(view);
    mAccountSpinner = view.findViewById(R.id.Account);
    Context wrappedCtx = view.getContext();
    mAccountsAdapter = new SimpleCursorAdapter(wrappedCtx, android.R.layout.simple_spinner_item, null,
        new String[]{KEY_LABEL}, new int[]{android.R.id.text1}, 0);
    mAccountsAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
    mAccountSpinner.setAdapter(mAccountsAdapter);
    mAccountSpinner.setOnItemSelectedListener(this);
    getLoaderManager().initLoader(0, null, this);

    mDateFormatSpinner = DialogUtils.configureDateFormat(view, wrappedCtx, PREFKEY_IMPORT_DATE_FORMAT);

    mEncodingSpinner = DialogUtils.configureEncoding(view, wrappedCtx, PREFKEY_IMPORT_ENCODING);

    mCurrencySpinner = DialogUtils.configureCurrencySpinner(view, this);
    currencyViewModel.getCurrencies().observe(this, currencies -> {
      final CurrencyAdapter adapter = (CurrencyAdapter) mCurrencySpinner.getAdapter();
      adapter.addAll(currencies);
      mCurrencySpinner.setSelection(adapter.getPosition(currencyViewModel.getDefault()));
    });
    currencyViewModel.loadCurrencies();
    view.findViewById(R.id.AccountType).setVisibility(View.GONE);//QIF data should specify type
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position,
                             long id) {
    if (parent.getId() == R.id.Currency) {
      if (accountId == 0) {
        currency = ((Currency) parent.getSelectedItem()).code();
      }
      return;
    }
    if (mAccountsCursor != null) {
      accountId = id;
      mAccountsCursor.moveToPosition(position);

      String currency = (accountId == 0 && this.currency != null) ?
          this.currency :
          mAccountsCursor.getString(2);//2=KEY_CURRENCY
      mCurrencySpinner.setSelection(
          ((ArrayAdapter<Currency>) mCurrencySpinner.getAdapter())
              .getPosition(Currency.create(currency)));
      mCurrencySpinner.setEnabled(position == 0);
    }
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putLong(KEY_ACCOUNTID, accountId);
    outState.putSerializable(KEY_CURRENCY, currency);
  }

}
