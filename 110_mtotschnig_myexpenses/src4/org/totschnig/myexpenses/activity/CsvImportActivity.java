package org.totschnig.myexpenses.activity;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;

import org.apache.commons.csv.CSVRecord;
import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.dialog.ConfirmationDialogFragment;
import org.totschnig.myexpenses.export.qif.QifDateFormat;
import org.totschnig.myexpenses.fragment.CsvImportDataFragment;
import org.totschnig.myexpenses.fragment.CsvImportParseFragment;
import org.totschnig.myexpenses.model.AccountType;
import org.totschnig.myexpenses.model.ContribFeature;
import org.totschnig.myexpenses.model.CurrencyUnit;
import org.totschnig.myexpenses.task.TaskExecutionFragment;
import org.totschnig.myexpenses.util.Result;

import java.util.ArrayList;


public class CsvImportActivity extends TabbedActivity implements
    ConfirmationDialogFragment.ConfirmationDialogListener {

  public static final String KEY_DATA_READY = "KEY_DATA_READY";
  public static final String KEY_USAGE_RECORDED = "KEY_USAGE_RECORDED";

  private boolean mDataReady = false;
  private boolean mUsageRecorded = false;

  private void setmDataReady(boolean mDataReady) {
    this.mDataReady = mDataReady;
    mSectionsPagerAdapter.notifyDataSetChanged();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(getThemeId());
    super.onCreate(savedInstanceState);

    final ActionBar actionBar = getSupportActionBar();
    actionBar.setTitle(getString(R.string.pref_import_title, "CSV"));
  }

  @Override
  protected void setupTabs(Bundle savedInstanceState) {
    //we only add the first tab, the second one once data has been parsed
    addTab(0);
    if (savedInstanceState != null) {
      mUsageRecorded = savedInstanceState.getBoolean(KEY_USAGE_RECORDED);
      if (savedInstanceState.getBoolean(KEY_DATA_READY)) {
        addTab(1);
        setmDataReady(true);
      }
    }
  }

  private void addTab(int index) {
    switch (index) {
      case 0:
        mSectionsPagerAdapter.addFragment(CsvImportParseFragment.newInstance(), getString(
            R.string.menu_parse));
        break;
      case 1:
        mSectionsPagerAdapter.addFragment(CsvImportDataFragment.newInstance(), getString(
            R.string.csv_import_preview));
        break;
    }
  }

  @Override
  public void onPositive(Bundle args) {
    switch (args.getInt(ConfirmationDialogFragment.KEY_COMMAND_POSITIVE)) {
      case R.id.SET_HEADER_COMMAND:
        CsvImportDataFragment df = (CsvImportDataFragment) getSupportFragmentManager().findFragmentByTag(
            mSectionsPagerAdapter.getFragmentName(1));
        df.setHeader();
    }
  }

  @Override
  public void onNegative(Bundle args) {

  }

  @Override
  public void onDismissOrCancel(Bundle args) {

  }

  @Override
  public void onPostExecute(int taskId, Object result) {
    super.onPostExecute(taskId, result);
    switch (taskId) {
      case TaskExecutionFragment.TASK_CSV_PARSE:
        if (result != null) {
          ArrayList<CSVRecord> data = (ArrayList<CSVRecord>) result;
          if (!data.isEmpty()) {
            if (!mDataReady) {
              addTab(1);
              setmDataReady(true);
            }
            CsvImportDataFragment df = (CsvImportDataFragment) getSupportFragmentManager().findFragmentByTag(
                mSectionsPagerAdapter.getFragmentName(1));
            if (df != null) {
              df.setData(data);
              mViewPager.setCurrentItem(1);
            }
            break;
          }
        }
        showSnackbar(R.string.parse_error_no_data_found, Snackbar.LENGTH_LONG);
        break;
      case TaskExecutionFragment.TASK_CSV_IMPORT:
        Result r = (Result) result;
        String msg;
        if (r.isSuccess()) {
          if (!mUsageRecorded) {
            recordUsage(ContribFeature.CSV_IMPORT);
            mUsageRecorded = true;
          }
        }
        showSnackbar(r.print(this), Snackbar.LENGTH_LONG);
    }
  }

  @Override
  public void onProgressUpdate(Object progress) {
    if (progress instanceof String) {
      showSnackbar((String) progress, Snackbar.LENGTH_LONG);
    } else {
      super.onProgressUpdate(progress);
    }
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putBoolean(KEY_DATA_READY, mDataReady);
    outState.putBoolean(KEY_USAGE_RECORDED, mUsageRecorded);
  }

  public long getAccountId() {
    CsvImportParseFragment pf = getParseFragment();
    return pf.getAccountId();
  }

  private CsvImportParseFragment getParseFragment() {
    return (CsvImportParseFragment) getSupportFragmentManager().findFragmentByTag(
        mSectionsPagerAdapter.getFragmentName(0));
  }

  public CurrencyUnit getCurrency() {
    CsvImportParseFragment pf = getParseFragment();
    return currencyContext.get(pf.getCurrency());
  }

  public QifDateFormat getDateFormat() {
    CsvImportParseFragment pf = getParseFragment();
    return pf.getDateFormat();
  }

  public AccountType getAccountType() {
    CsvImportParseFragment pf = getParseFragment();
    return pf.getAccountType();
  }

}
