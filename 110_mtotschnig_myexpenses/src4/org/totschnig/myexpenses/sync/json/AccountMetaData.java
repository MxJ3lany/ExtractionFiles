package org.totschnig.myexpenses.sync.json;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import org.totschnig.myexpenses.model.Account;
import org.totschnig.myexpenses.model.AccountType;
import org.totschnig.myexpenses.model.CurrencyContext;
import org.totschnig.myexpenses.model.CurrencyUnit;
import org.totschnig.myexpenses.preference.PrefKey;

@AutoValue
public abstract class AccountMetaData implements Parcelable {
  public static TypeAdapter<AccountMetaData> typeAdapter(Gson gson) {
    return new AutoValue_AccountMetaData.GsonTypeAdapter(gson);
  }

  public static Builder builder() {
    return new AutoValue_AccountMetaData.Builder();
  }

  public abstract String label();

  public abstract String currency();

  public abstract int color();

  public abstract String uuid();

  public abstract long openingBalance();

  public abstract String description();

  public abstract String type();

  @Nullable
  public abstract Double exchangeRate();

  @Nullable
  public abstract String exchangeRateOtherCurrency();

  @Override
  public String toString() {
    return label() + " (" + currency() + ")";
  }

  public Account toAccount(CurrencyContext currencyContext) {
    AccountType accountType;
    try {
      accountType = AccountType.valueOf(type());
    } catch (IllegalArgumentException e) {
      accountType = AccountType.CASH;
    }
    Account account = new Account(label(),
        currencyContext.get(currency()),
        openingBalance(), description(), accountType, color());
    account.uuid = uuid();
    String homeCurrency = PrefKey.HOME_CURRENCY.getString(null);
    final Double exchangeRate = exchangeRate();
    if (exchangeRate != null && homeCurrency != null && homeCurrency.equals(exchangeRateOtherCurrency())) {
      account.setExchangeRate(exchangeRate);
    }
    return account;
  }

  public static AccountMetaData from(Account account) {
    String homeCurrency = PrefKey.HOME_CURRENCY.getString(null);
    final String accountCurrency = account.getCurrencyUnit().code();
    final Builder builder = builder()
        .setCurrency(accountCurrency)
        .setColor(account.color)
        .setUuid(account.uuid)
        .setDescription(account.description)
        .setLabel(account.getLabel())
        .setOpeningBalance(account.openingBalance.getAmountMinor())
        .setType(account.getType().name());
    if (homeCurrency != null && !homeCurrency.equals(accountCurrency)) {
      builder.setExchangeRate(account.getExchangeRate()).setExchangeRateOtherCurrency(homeCurrency);
    }
    return builder.build();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setLabel(String label);
    public abstract Builder setCurrency(String currency);
    public abstract Builder setColor(int color);
    public abstract Builder setUuid(String uuid);
    public abstract Builder setOpeningBalance(long openingBalance);
    public abstract Builder setDescription(String description);
    public abstract Builder setType(String type);
    public abstract Builder setExchangeRate(Double exchangeRate);
    public abstract Builder setExchangeRateOtherCurrency(String otherCurrency);

    public abstract AccountMetaData build();
  }
}
