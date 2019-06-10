package org.totschnig.myexpenses.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.model.Transaction;

public class CrStatusAdapter extends ArrayAdapter<Transaction.CrStatus> {

  private static final int TEXT_VIEW_RESOURCE_ID = android.R.id.text1;

  public CrStatusAdapter(Context context) {
    super(context, R.layout.custom_spinner_item, TEXT_VIEW_RESOURCE_ID, Transaction.CrStatus.values());
    setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
  }

  @NonNull
  @Override
  public View getView(int position, View convertView, @NonNull ViewGroup parent) {
    View row = super.getView(position, convertView, parent);
    setColor(position, row);
    setText(position, row);
    row.findViewById(android.R.id.text1).setVisibility(View.GONE);
    return row;
  }

  @Override
  public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
    View row = super.getDropDownView(position, convertView, parent);
    setColor(position, row);
    setText(position, row);
    row.findViewById(android.R.id.text1).setEnabled(isEnabled(position));
    return row;
  }

  private void setText(int position, View row) {
    ((TextView) row.findViewById(TEXT_VIEW_RESOURCE_ID)).setText(getItem(position).toStringRes());
  }

  private void setColor(int position, View row) {
    View color = row.findViewById(R.id.color1);
    color.setBackgroundColor(getItem(position).color);
  }
}
