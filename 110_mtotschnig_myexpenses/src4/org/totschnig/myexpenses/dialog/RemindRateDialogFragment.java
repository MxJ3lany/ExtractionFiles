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

package org.totschnig.myexpenses.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;

import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.activity.MyExpenses;
import org.totschnig.myexpenses.dialog.MessageDialogFragment.MessageDialogListener;
import org.totschnig.myexpenses.preference.PrefKey;
import org.totschnig.myexpenses.util.Utils;

public class RemindRateDialogFragment extends CommitSafeDialogFragment implements OnClickListener, OnRatingBarChangeListener {

  private RatingBar mRating;
  private TextView mRatingRemind;
  private int POSITIVE_RATING = 5;

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    MyExpenses ctx = (MyExpenses) getActivity();
    //Context wrappedCtx = DialogUtils.wrapContext1(ctx);
    LayoutInflater li = LayoutInflater.from(ctx);
    //noinspection InflateParams
    View view = li.inflate(R.layout.remind_rate, null);
    ((TextView) view.findViewById(R.id.rating_how_many)).setText(Utils.getTextWithAppName(ctx, R.string.dialog_remind_rate_how_many_stars));
    mRating = view.findViewById(R.id.rating);
    mRating.setOnRatingBarChangeListener(this);
    mRatingRemind = view.findViewById(R.id.rating_remind);
    setRatingRemindText(true);
    AlertDialog dialog = new AlertDialog.Builder(ctx)
        .setTitle(R.string.app_name)
        .setView(view)
        .setCancelable(false)
        .setPositiveButton(R.string.dialog_remind_rate_yes, this)
        .setNeutralButton(R.string.dialog_remind_later, this)
        .setNegativeButton(R.string.dialog_remind_no, this)
        .create();
    dialog.setOnShowListener(new ButtonOnShowDisabler());
    return dialog;
  }

  private void setRatingRemindText(boolean isPositive) {
    CharSequence rate1 = isPositive ? Utils.getTextWithAppName(getContext(), R.string.dialog_remind_rate_1) :
        getString(R.string.dialog_remind_rate_1_suggest_improvement);
    mRatingRemind.setText(TextUtils.concat(rate1, " ", getString(R.string.dialog_remind_rate_2)));
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {
    if (getActivity() == null) {
      return;
    }
    if (which == AlertDialog.BUTTON_POSITIVE) {
      PrefKey.NEXT_REMINDER_RATE.putLong(-1);
      ((MessageDialogListener) getActivity())
          .dispatchCommand(mRating.getRating() >= POSITIVE_RATING ? R.id.RATE_COMMAND : R.id.FEEDBACK_COMMAND, null);
    } else if (which == AlertDialog.BUTTON_NEUTRAL) {
      ((MessageDialogListener) getActivity())
          .dispatchCommand(R.id.REMIND_LATER_RATE_COMMAND, null);
    } else {
      ((MessageDialogListener) getActivity())
          .dispatchCommand(R.id.REMIND_NO_RATE_COMMAND, null);
    }
  }

  @Override
  public void onRatingChanged(RatingBar ratingBar, float rating,
                              boolean fromUser) {
    if (fromUser) {
      if (rating < 1) {
        ratingBar.setRating(1);
      }
      setRatingRemindText(rating >= POSITIVE_RATING);
      Button b = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
      b.setEnabled(true);
      b.setText(rating >= POSITIVE_RATING ? R.string.dialog_remind_rate_yes : R.string.pref_send_feedback_title);
      b.invalidate();
    }
  }
}