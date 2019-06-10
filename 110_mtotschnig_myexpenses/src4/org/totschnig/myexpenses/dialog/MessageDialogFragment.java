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

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import org.totschnig.myexpenses.MyApplication;
import org.totschnig.myexpenses.R;

import java.io.Serializable;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_NEUTRAL;
import static android.content.DialogInterface.BUTTON_POSITIVE;

public class MessageDialogFragment extends CommitSafeDialogFragment implements OnClickListener {

  private static final String KEY_TITLE = "title";
  private static final String KEY_MESSAGE = "message";
  private static final String KEY_POSITIVE = "positive";
  private static final String KEY_NEUTRAL = "neutral";
  private static final String KEY_NEGATIVE = "negative";
  private static final String KEY_ICON = "icon";

  public static class Button implements Serializable {
    int label;
    int command;
    Serializable tag;
    boolean keepDialogOpen;

    public Button(int label, int command, Serializable tag) {
      this(label, command, tag, false);
    }

    public Button(int label, int command, Serializable tag, boolean keepDialogOpen) {
      this.label = label;
      this.command = command;
      this.tag = tag;
      this.keepDialogOpen = keepDialogOpen;
    }

    public static Button noButton() {
      return nullButton(android.R.string.cancel);
    }

    public static Button okButton() {
      return nullButton(android.R.string.ok);
    }

    public static Button nullButton(int label) {
      return new Button(label, R.id.NO_COMMAND, null);
    }
  }

  public static MessageDialogFragment newInstance(
      int title, int message, Button positive, Button neutral, Button negative) {
    return newInstance(title, MyApplication.getInstance().getString(message),
        positive, neutral, negative);
  }

  public static MessageDialogFragment newInstance(
      int title, CharSequence message, Button positive, Button neutral, Button negative) {
   return newInstance(title, message, positive, neutral, negative, 0);
  }

  public static MessageDialogFragment newInstance(
      int title, CharSequence message, Button positive, Button neutral, Button negative, int icon) {
    return newInstance(title == 0 ? null : MyApplication.getInstance().getString(title),
        message, positive, neutral, negative, icon);
  }

  public static MessageDialogFragment newInstance(
      CharSequence title, CharSequence message, Button positive, Button neutral, Button negative, int icon) {
    MessageDialogFragment dialogFragment = new MessageDialogFragment();
    Bundle bundle = new Bundle();
    bundle.putCharSequence(KEY_TITLE, title);
    bundle.putCharSequence(KEY_MESSAGE, message);
    bundle.putSerializable(KEY_POSITIVE, positive);
    bundle.putSerializable(KEY_NEUTRAL, neutral);
    bundle.putSerializable(KEY_NEGATIVE, negative);
    bundle.putInt(KEY_ICON, icon);
    dialogFragment.setArguments(bundle);
    return dialogFragment;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    final Bundle bundle = getArguments();
    Activity ctx = getActivity();
    AlertDialog.Builder builder = new AlertDialog.Builder(ctx)
        .setMessage(bundle.getCharSequence(KEY_MESSAGE))
        .setTitle(bundle.getCharSequence(KEY_TITLE));

    Button positive = (Button) bundle.getSerializable(KEY_POSITIVE);
    Button neutral = (Button) bundle.getSerializable(KEY_NEUTRAL);
    Button negative = (Button) bundle.getSerializable(KEY_NEGATIVE);
    if (positive != null) {
      builder.setPositiveButton(positive.label, positive.keepDialogOpen ? null : this);
    }
    if (neutral != null) {
      builder.setNeutralButton(neutral.label, neutral.keepDialogOpen ? null : this);
    }
    if (negative != null) {
      builder.setNegativeButton(negative.label, negative.keepDialogOpen ? null : this);
    }
    final int icon = bundle.getInt(KEY_ICON);
    if (icon != 0) {
      builder.setIcon(icon);
    }
    AlertDialog alertDialog = builder.create();
    alertDialog.setOnShowListener(dialog -> {
      if (shouldKeepDialogOpen(positive)) {
        setOnClickForward(alertDialog, BUTTON_POSITIVE);
      }
      if (shouldKeepDialogOpen(negative)) {
        setOnClickForward(alertDialog, BUTTON_NEGATIVE);
      }
      if (shouldKeepDialogOpen(neutral)) {
        setOnClickForward(alertDialog, BUTTON_NEUTRAL);
      }
    });
    return alertDialog;
  }

  private boolean shouldKeepDialogOpen(Button button) {
    return button != null && button.keepDialogOpen;
  }

  /**
   * prevent automatic dismiss on button click
   * @param alertDialog
   * @param which
   */
  private void setOnClickForward(AlertDialog alertDialog, int which) {
    alertDialog.getButton(which).setOnClickListener(v -> onClick(alertDialog, which));
  }

  @Override
  public void onCancel(DialogInterface dialog) {
    if (getActivity() == null) {
      return;
    }
    ((MessageDialogListener) getActivity()).onMessageDialogDismissOrCancel();
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {
    if (getActivity() == null) {
      return;
    }
    Bundle bundle = getArguments();
    Button clicked = null;
    switch (which) {
      case BUTTON_POSITIVE:
        clicked = (Button) bundle.getSerializable(KEY_POSITIVE);
        break;
      case BUTTON_NEUTRAL:
        clicked = (Button) bundle.getSerializable(KEY_NEUTRAL);
        break;
      case BUTTON_NEGATIVE:
        clicked = (Button) bundle.getSerializable(KEY_NEGATIVE);
        break;
    }
    if (clicked.command == R.id.NO_COMMAND) {
      onCancel(dialog);
    } else {
      ((MessageDialogListener) getActivity())
          .dispatchCommand(clicked.command, clicked.tag);
    }
  }

  public interface MessageDialogListener {
    boolean dispatchCommand(int command, Object tag);

    void onMessageDialogDismissOrCancel();
  }
}