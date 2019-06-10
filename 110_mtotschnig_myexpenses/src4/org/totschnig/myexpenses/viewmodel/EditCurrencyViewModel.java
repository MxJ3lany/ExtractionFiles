package org.totschnig.myexpenses.viewmodel;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.totschnig.myexpenses.MyApplication;
import org.totschnig.myexpenses.model.CurrencyContext;
import org.totschnig.myexpenses.provider.TransactionProvider;
import org.totschnig.myexpenses.util.CurrencyFormatter;

import javax.inject.Inject;

import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_CODE;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_LABEL;

public class EditCurrencyViewModel extends CurrencyViewModel {

  interface UpdateListener {
    void onUpdateComplete(int token, int result);
  }

  interface InsertListener {
    void onInsertComplete(int token, boolean success);
  }

  interface DeleteListener {
    void onDeleteComplete(int token, boolean success);
  }

  private static final int TOKEN_UPDATE_FRACTION_DIGITS = 1;
  private static final int TOKEN_UPDATE_LABEL = 2;
  private static final int TOKEN_INSERT_CURRENCY = 3;
  private static final int TOKEN_DELETE_CURRENCY = 4;

  @Inject
  protected CurrencyContext currencyContext;

  private final DatabaseHandler asyncDatabaseHandler;
  private int updateOperationsCount = 0;
  private Integer updatedAccountsCount = null;

  private MutableLiveData<Integer> updateComplete = new MutableLiveData<>();

  private MutableLiveData<Boolean> insertComplete = new MutableLiveData<>();

  private MutableLiveData<Boolean> deleteComplete = new MutableLiveData<>();

  public LiveData<Integer> getUpdateComplete() {
    return updateComplete;
  }

  public LiveData<Boolean> getInsertComplete() {
    return insertComplete;
  }

  public LiveData<Boolean> getDeleteComplete() {
    return deleteComplete;
  }

  public EditCurrencyViewModel(@NonNull Application application) {
    super(application);
    final ContentResolver contentResolver = application.getContentResolver();
    asyncDatabaseHandler = new DatabaseHandler(contentResolver);

    ((MyApplication) application).getAppComponent().inject(this);
  }

  public void save(String currency, String symbol, int fractionDigits, String label, boolean withUpdate) {
    UpdateListener updateListener = (token, result) -> {
      updateOperationsCount--;
      if (token == TOKEN_UPDATE_FRACTION_DIGITS) {
        updatedAccountsCount = result;
      }
      if (updateOperationsCount == 0) {
        updateComplete.postValue(updatedAccountsCount);
      }
    };
    CurrencyFormatter.instance().invalidate(currency);
    currencyContext.storeCustomSymbol(currency, symbol);
    if (withUpdate) {
      updateOperationsCount++;
      asyncDatabaseHandler.startUpdate(TOKEN_UPDATE_FRACTION_DIGITS, updateListener,
          TransactionProvider.CURRENCIES_URI.buildUpon()
              .appendPath(TransactionProvider.URI_SEGMENT_CHANGE_FRACTION_DIGITS)
              .appendPath(currency)
              .appendPath(String.valueOf(fractionDigits))
              .build(), null, null, null);
    } else {
      currencyContext.storeCustomFractionDigits(currency, fractionDigits);
    }
    if (label != null) {
      updateOperationsCount++;
      ContentValues contentValues = new ContentValues(1);
      contentValues.put(KEY_LABEL, label);
      asyncDatabaseHandler.startUpdate(TOKEN_UPDATE_LABEL, updateListener, buildItemUri(currency),
          contentValues, null, null);
    }
    if (updateOperationsCount == 0) {
      updateComplete.postValue(null);
    }
  }


  public void newCurrency(String code, String symbol, int fractionDigits, String label) {
    ContentValues contentValues = new ContentValues(2);
    contentValues.put(KEY_LABEL, label);
    contentValues.put(KEY_CODE, code);
    asyncDatabaseHandler.startInsert(TOKEN_INSERT_CURRENCY, (InsertListener) (token, success) -> {
      if (success) {
        currencyContext.storeCustomSymbol(code, symbol);
        currencyContext.storeCustomFractionDigits(code, fractionDigits);
      }
      insertComplete.postValue(success);
    }, TransactionProvider.CURRENCIES_URI, contentValues);
  }


  public void deleteCurrency(String currency) {
    asyncDatabaseHandler.startDelete(TOKEN_DELETE_CURRENCY, (DeleteListener) (token, success) -> {
      deleteComplete.postValue(success);
    }, buildItemUri(currency), null, null);
  }

  protected Uri buildItemUri(String currency) {
    return TransactionProvider.CURRENCIES_URI.buildUpon().appendPath(currency).build();
  }

  static class DatabaseHandler extends AsyncQueryHandler {

    public DatabaseHandler(ContentResolver cr) {
      super(cr);
    }

    @Override
    protected void onUpdateComplete(int token, Object cookie, int result) {
      ((UpdateListener) cookie).onUpdateComplete(token, result);
    }

    @Override
    protected void onInsertComplete(int token, Object cookie, Uri uri) {
      ((InsertListener) cookie).onInsertComplete(token, uri != null);
    }

    @Override
    protected void onDeleteComplete(int token, Object cookie, int result) {
      ((DeleteListener) cookie).onDeleteComplete(token, result == 1);
    }
  }
}
