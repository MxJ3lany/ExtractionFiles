package org.totschnig.myexpenses.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.totschnig.myexpenses.MyApplication;
import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.model.CurrencyContext;
import org.totschnig.myexpenses.model.CurrencyUnit;
import org.totschnig.myexpenses.util.Utils;
import org.totschnig.myexpenses.util.form.FormFieldNotEmptyValidator;
import org.totschnig.myexpenses.util.form.FormValidator;
import org.totschnig.myexpenses.util.form.NumberRangeValidator;
import org.totschnig.myexpenses.viewmodel.EditCurrencyViewModel;
import org.totschnig.myexpenses.viewmodel.data.Currency;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_CURRENCY;
import static org.totschnig.myexpenses.util.Utils.isFrameworkCurrency;

public class EditCurrencyDialog extends CommitSafeDialogFragment {

  public static final String KEY_RESULT = "result";
  @BindView(R.id.edt_currency_symbol)
  EditText editTextSymbol;

  @BindView(R.id.edt_currency_fraction_digits)
  EditText editTextFractionDigits;

  @BindView(R.id.edt_currency_code)
  EditText editTextCode;

  @BindView(R.id.edt_currency_label)
  EditText editTextLabel;

  @BindView(R.id.container_currency_label)
  ViewGroup containerLabel;

  @BindView(R.id.container_currency_code)
  ViewGroup containerCode;

  @BindView(R.id.checkBox)
  CheckBox checkBox;

  @BindView(R.id.warning_change_fraction_digits)
  TextView warning;

  @Inject
  CurrencyContext currencyContext;

  private EditCurrencyViewModel editCurrencyViewModel;

  public static EditCurrencyDialog newInstance(Currency currency) {
    Bundle arguments = new Bundle(1);
    arguments.putSerializable(KEY_CURRENCY, currency);
    EditCurrencyDialog editCurrencyDialog = new EditCurrencyDialog();
    editCurrencyDialog.setArguments(arguments);
    return editCurrencyDialog;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    MyApplication.getInstance().getAppComponent().inject(this);
    editCurrencyViewModel = ViewModelProviders.of(this).get(EditCurrencyViewModel.class);
    editCurrencyViewModel.getUpdateComplete().observe(this, this::dismiss);
    editCurrencyViewModel.getInsertComplete().observe(this, success -> {
      if (success != null && success) {
        dismiss();
      } else {
        showSnackbar(R.string.currency_code_already_definded);
        setButtonState(true);
      }
    });
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Activity ctx = getActivity();
    LayoutInflater li = LayoutInflater.from(ctx);
    //noinspection InflateParams
    dialogView = li.inflate(R.layout.edit_currency, null);
    ButterKnife.bind(this, dialogView);
    Currency currency = getCurrency();
    boolean frameworkCurrency;
    String title = null;
    if (currency != null) {
      CurrencyUnit currencyUnit = currencyContext.get(currency.code());
      editTextSymbol.setText(currencyUnit.symbol());
      editTextCode.setText(currency.code());

      final String displayName = currency.toString();
      frameworkCurrency = isFrameworkCurrency(currency.code());
      if (frameworkCurrency) {
        editTextSymbol.requestFocus();
        title = String.format(Locale.ROOT, "%s (%s)", displayName, currency.code());
        containerLabel.setVisibility(View.GONE);
        containerCode.setVisibility(View.GONE);
      } else {
        editTextLabel.setText(displayName);
      }
      editTextFractionDigits.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
          final int newValue = readFractionDigitsFromUI();
          final int oldValue = currentFractionDigits();
          final boolean valueUpdate = newValue != -1 && newValue != oldValue;
          checkBox.setVisibility(valueUpdate ? View.VISIBLE : View.GONE);
          warning.setVisibility(valueUpdate ? View.VISIBLE : View.GONE);
          if (valueUpdate) {
            String message = getString(R.string.warning_change_fraction_digits_1);
            int delta = oldValue - newValue;
            message += " " + getString(
                delta > 0 ? R.string.warning_change_fraction_digits_2_multiplied :
                    R.string.warning_change_fraction_digits_2_divided,
                Utils.pow(10, Math.abs(delta)));
            if (delta > 0) {
              message += " " + getString(R.string.warning_change_fraction_digits_3);
            }
            warning.setText(message);
          }
        }
      });
    } else {
      title = getString(R.string.dialog_title_new_currency);
      editTextCode.setFocusable(true);
      editTextCode.setFocusableInTouchMode(true);
      editTextCode.setEnabled(true);
      editTextCode.setFilters(new InputFilter[] {new InputFilter.AllCaps(), new InputFilter.LengthFilter(3)});
    }
    editTextFractionDigits.setText(String.valueOf(currentFractionDigits()));
    final AlertDialog alertDialog = new AlertDialog.Builder(ctx)
        .setView(dialogView)
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(android.R.string.ok, null)
        .setTitle(title)
        .create();
    alertDialog.setOnShowListener(dialog -> {
      Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
      button.setOnClickListener(this::onOkClick);
    });
    return alertDialog;
  }

  private String readSymbolfromUI() {
    return editTextSymbol.getText().toString();
  }

  private String readLabelfromUI() {
    return editTextLabel.getText().toString();
  }

  private String readCodefromUI() {
    return editTextCode.getText().toString();
  }

  private int currentFractionDigits() {
    final Currency currency = getCurrency();
    if (currency != null) {
      return currencyContext.get(currency.code()).fractionDigits();
    }
    return 2;
  }

  private int readFractionDigitsFromUI() {
    try {
      return Integer.parseInt(editTextFractionDigits.getText().toString());
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  @Nullable
  private Currency getCurrency() {
    return (Currency) getArguments().getSerializable(KEY_CURRENCY);
  }

  private void onOkClick(View view) {
    final Currency currency = getCurrency();
    FormValidator validator = new FormValidator();
    validator.add(new FormFieldNotEmptyValidator(editTextSymbol));
    validator.add(new NumberRangeValidator(editTextFractionDigits, 0, 8));
    if (currency == null) {
      validator.add(new FormFieldNotEmptyValidator(editTextCode));
      validator.add(new FormFieldNotEmptyValidator(editTextLabel));
    }
    if (validator.validate()) {
      final boolean withUpdate = checkBox.isChecked();
      String label = readLabelfromUI();
      final String symbol = readSymbolfromUI();
      final int fractionDigits = readFractionDigitsFromUI();
      if (currency == null) {
        editCurrencyViewModel.newCurrency(readCodefromUI(), symbol, fractionDigits, label);
        setButtonState(false);
      } else {
        final boolean frameworkCurrency = isFrameworkCurrency(currency.code());
        editCurrencyViewModel.save(currency.code(), symbol, fractionDigits, frameworkCurrency ? null : label, withUpdate);
        if (!withUpdate && frameworkCurrency) {
          dismiss();
        } else {
          setButtonState(false);
        }
      }
    }
  }

  private void setButtonState(boolean enabled) {
    ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(enabled);
  }

  public void dismiss(Integer result) {
    Intent data = null;
    final Fragment targetFragment = getTargetFragment();
    if (targetFragment != null) {
      if (result != null) {
        data = new Intent();
        data.putExtra(KEY_RESULT, result.intValue());
        data.putExtra(KEY_CURRENCY, getCurrency().code());
      }
      targetFragment.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
    }
    super.dismiss();
  }
}
