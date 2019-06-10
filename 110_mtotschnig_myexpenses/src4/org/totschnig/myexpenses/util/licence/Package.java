package org.totschnig.myexpenses.util.licence;

import android.content.Context;
import android.support.annotation.NonNull;

import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.model.CurrencyUnit;
import org.totschnig.myexpenses.model.Money;
import org.totschnig.myexpenses.util.CurrencyFormatter;
import org.totschnig.myexpenses.util.Preconditions;

public enum Package {
  Contrib(350), Upgrade(300), Extended(500), Professional_1(100), Professional_6(500),
  Professional_12(900), Professional_18(950), Professional_30(1400),
  Professional_Amazon(900), Professional_Blackberry(2000);

  /**
   * Extra months credited for professional licence to holders of extended licence
   */
  private static final int DURATION_EXTRA = 3;

  public long getDefaultPrice() {
    return defaultPrice;
  }

  private final long defaultPrice;

  Package(long price) {
    this.defaultPrice = price;
  }

  public boolean isProfessional() {
    return name().startsWith("Professional");
  }

  public String getFormattedPrice(Context context, CurrencyUnit currencyUnit, boolean withExtra) {
    String formatted = getFormattedPriceRaw(currencyUnit);
    return getFormattedPrice(context, formatted, withExtra);
  }

  public String getFormattedPrice(Context context, String formatted, boolean withExta) {
    return isProfessional() ? formatWithDuration(context, formatted, withExta) : formatted;
  }

  public String getFormattedPriceRaw(CurrencyUnit currencyUnit) {
    return CurrencyFormatter.instance().formatCurrency(
        new Money(currencyUnit, getDefaultPrice()));
  }

  @NonNull
  String formatWithDuration(Context context, String formattedPrice, boolean withExtra) {
    Preconditions.checkState(isProfessional());
    int duration = getDuration(withExtra);
    String formattedDuration;
    String format = "%s (%s)";
    switch (duration) {
      case 1:
        formattedDuration = context.getString(R.string.monthly);
        break;
      case 12:
        formattedDuration = context.getString(R.string.yearly_plain);
        break;
      default:
        format= "%s / %s";
        formattedDuration= context.getString(R.string.n_months, duration);
    }
    return String.format(format, formattedPrice, formattedDuration);
  }

  int getDuration(boolean withExtra) {
    final int base = Integer.parseInt(extractDuration());
    return withExtra ? base + DURATION_EXTRA : base;
  }

  long getMonthlyPrice(boolean withExtra) {
    return (long) Math.ceil((double) getDefaultPrice() / getDuration(withExtra));
  }

  @NonNull
  private String extractDuration() {
    return name().substring(name().lastIndexOf("_") + 1);
  }
}
