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
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import org.totschnig.myexpenses.MyApplication;
import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.activity.ContribInfoDialogActivity;
import org.totschnig.myexpenses.activity.ProtectedFragmentActivity;
import org.totschnig.myexpenses.model.ContribFeature;
import org.totschnig.myexpenses.preference.PrefHandler;
import org.totschnig.myexpenses.util.DistribHelper;
import org.totschnig.myexpenses.util.Utils;
import org.totschnig.myexpenses.util.licence.LicenceHandler;
import org.totschnig.myexpenses.util.licence.LicenceStatus;
import org.totschnig.myexpenses.util.licence.Package;
import org.totschnig.myexpenses.util.tracking.Tracker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static org.totschnig.myexpenses.activity.ContribInfoDialogActivity.KEY_FEATURE;
import static org.totschnig.myexpenses.util.TextUtils.concatResStrings;
import static org.totschnig.myexpenses.util.licence.LicenceStatus.CONTRIB;
import static org.totschnig.myexpenses.util.licence.LicenceStatus.EXTENDED;
import static org.totschnig.myexpenses.util.licence.LicenceStatus.PROFESSIONAL;

public class ContribDialogFragment extends CommitSafeDialogFragment implements DialogInterface.OnClickListener, View.OnClickListener {
  @Nullable
  private ContribFeature feature;
  private RadioButton contribButton, extendedButton, professionalButton;
  private boolean contribVisible;
  private boolean extendedVisible;
  private Package selectedPackage = null;
  @Inject
  LicenceHandler licenceHandler;
  @Inject
  PrefHandler prefHandler;
  private TextView professionalPriceTextView;
  View dialogView;

  public static ContribDialogFragment newInstance(String feature, Serializable tag) {
    ContribDialogFragment dialogFragment = new ContribDialogFragment();
    Bundle bundle = new Bundle();
    bundle.putString(KEY_FEATURE, feature);
    bundle.putSerializable(ContribInfoDialogActivity.KEY_TAG, tag);
    dialogFragment.setArguments(bundle);
    return dialogFragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    String featureStringExtra = getArguments().getString(KEY_FEATURE);
    if (featureStringExtra != null) {
      feature = ContribFeature.valueOf(featureStringExtra);
    }
    MyApplication.getInstance().getAppComponent().inject(this);
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    ProtectedFragmentActivity ctx = (ProtectedFragmentActivity) getActivity();
    Context wrappedContext = new ContextThemeWrapper(ctx,  R.style.ThemeDark);
    LicenceStatus licenceStatus = licenceHandler.getLicenceStatus();
    //noinspection InflateParams
    dialogView = LayoutInflater.from(ctx).inflate(R.layout.contrib_dialog, null);
    AlertDialog.Builder builder = new AlertDialog.Builder(ctx,
        ctx.getThemeType().equals(ProtectedFragmentActivity.ThemeType.dark) ?
            R.style.ContribDialogThemeDark : R.style.ContribDialogThemeLight);

    //preapre HEADER
    CharSequence message;
    if (feature != null) {
      CharSequence featureDescription = feature.buildFullInfoString(ctx),
          linefeed = Html.fromHtml("<br>"),
          removePhrase = feature.buildRemoveLimitation(getActivity(), true);
      message = TextUtils.concat(featureDescription, linefeed, removePhrase);
      if (feature.hasTrial()) {
        TextView usagesLeftTextView = dialogView.findViewById(R.id.usages_left);
        usagesLeftTextView.setText(feature.buildUsagesLefString(ctx, prefHandler));
        usagesLeftTextView.setVisibility(View.VISIBLE);
      }
    } else {
      CharSequence contribText2 = Utils.getTextWithAppName(getContext(), R.string.dialog_contrib_text_2);
      if (DistribHelper.isGithub()) {
        message = TextUtils.concat(Utils.getTextWithAppName(getContext(), R.string.dialog_contrib_text_1), " ",
            contribText2);
      } else {
        message = contribText2;
      }
    }
    ((TextView) dialogView.findViewById(R.id.feature_info)).setText(message);

    List<CharSequence> contribFeatureLabelsAsList = Utils.getContribFeatureLabelsAsList(ctx, CONTRIB);
    List<CharSequence> extendedFeatureLabelsAsList = Utils.getContribFeatureLabelsAsList(ctx, EXTENDED);

    //prepare CONTRIB section
    View contribContainer = dialogView.findViewById(R.id.contrib_feature_container);
    contribContainer.setBackgroundColor(getResources().getColor(R.color.premium_licence));
    if (licenceStatus == null && CONTRIB.covers(feature)) {
     contribVisible = true;
      CharSequence contribList = Utils.makeBulletList(wrappedContext, contribFeatureLabelsAsList, R.drawable.ic_menu_done);
      ((TextView) contribContainer.findViewById(R.id.package_feature_list)).setText(contribList);
    } else {
      contribContainer.setVisibility(View.GONE);
    }

    //prepare EXTENDED section
    View extendedContainer = dialogView.findViewById(R.id.extended_feature_container);
    extendedContainer.setBackgroundColor(getResources().getColor(R.color.extended_licence));
    if (CONTRIB.greaterOrEqual(licenceStatus) && EXTENDED.covers(feature)) {
      extendedVisible = true;
      ArrayList<CharSequence> lines = new ArrayList<>();
      if (contribVisible) {
        lines.add(getString(R.string.all_contrib_key_features) + "\n+");
      } else if (licenceStatus == null && feature != null && feature.isExtended()) {
        lines.addAll(contribFeatureLabelsAsList);
      }
      lines.addAll(extendedFeatureLabelsAsList);
      ((TextView) extendedContainer.findViewById(R.id.package_feature_list)).setText(Utils.makeBulletList(wrappedContext, lines, R.drawable.ic_menu_done));
    } else {
      extendedContainer.setVisibility(View.GONE);
    }

    //prepare PROFESSIONAL section
    ArrayList<CharSequence> lines = new ArrayList<>();
    View professionalContainer = dialogView.findViewById(R.id.professional_feature_container);
    professionalContainer.setBackgroundColor(getResources().getColor(R.color.professional_licence));
    if (extendedVisible) {
      lines.add(getString(R.string.all_extended_key_features) + "\n+");
    } else if(feature != null && feature.isProfessional()) {
      if (licenceStatus == null) {
        lines.addAll(contribFeatureLabelsAsList);
      }
      if (CONTRIB.greaterOrEqual(licenceStatus)) {
        lines.addAll(extendedFeatureLabelsAsList);
      }
    }
    lines.addAll(Utils.getContribFeatureLabelsAsList(ctx, PROFESSIONAL));
    ((TextView) professionalContainer.findViewById(R.id.package_feature_list))
        .setText(Utils.makeBulletList(wrappedContext, lines, R.drawable.ic_menu_done));

    //FOOTER
    final TextView githubExtraInfo = dialogView.findViewById(R.id.github_extra_info);
    if (DistribHelper.isGithub()) {
      githubExtraInfo.setVisibility(View.VISIBLE);
      githubExtraInfo.setText(concatResStrings(getActivity(),
          ". ", R.string.professional_key_fallback_info, R.string.eu_vat_info));
    }

    builder.setTitle(feature == null ? R.string.menu_contrib : R.string.dialog_title_contrib_feature)
        .setView(dialogView)
        .setNegativeButton(R.string.dialog_contrib_no, this)
        .setIcon(R.mipmap.ic_launcher_alt)
        .setPositiveButton(R.string.upgrade_now, this);
    AlertDialog dialog = builder.create();

    if (contribVisible) {
      contribButton = contribContainer.findViewById(R.id.package_button);
      ((TextView) contribContainer.findViewById(R.id.package_label)).setText(R.string.contrib_key);
      ((TextView) contribContainer.findViewById(R.id.package_price)).setText(
          licenceHandler.getFormattedPrice(Package.Contrib));
      contribContainer.setOnClickListener(this);
      contribButton.setOnClickListener(this);
    }
    if (extendedVisible) {
      extendedButton = extendedContainer.findViewById(R.id.package_button);
      ((TextView) extendedContainer.findViewById(R.id.package_label)).setText(R.string.extended_key);
      ((TextView) extendedContainer.findViewById(R.id.package_price)).setText(
          licenceHandler.getFormattedPrice(licenceStatus == null ? Package.Extended : Package.Upgrade));
      extendedContainer.setOnClickListener(this);
      extendedButton.setOnClickListener(this);
    }
    professionalButton = professionalContainer.findViewById(R.id.package_button);
    ((TextView) professionalContainer.findViewById(R.id.package_label)).setText(R.string.professional_key);
    professionalPriceTextView = professionalContainer.findViewById(R.id.package_price);
    professionalPriceTextView.setText(licenceHandler.getProfessionalPriceShortInfo());
    Package[] proPackages = licenceHandler.getProPackages();
    if(!contribVisible && !extendedVisible && proPackages.length == 1) {
      professionalButton.setChecked(true);
      selectedPackage = proPackages[0];
    } else {
      dialogView.findViewById(R.id.professional_feature_container).setOnClickListener(this);
      professionalButton.setOnClickListener(this);
      dialog.setOnShowListener(new ButtonOnShowDisabler());
    }

    return dialog;
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {
    ContribInfoDialogActivity ctx = (ContribInfoDialogActivity) getActivity();
    if (ctx == null) {
      return;
    }
    if (which == AlertDialog.BUTTON_POSITIVE) {
      if (selectedPackage != null) {
        ctx.contribBuyDo(selectedPackage);
      } else {
        //should not happen
        ctx.finish(true);
      }
    } else {
      //BUTTON_NEGATIV
      ctx.logEvent(Tracker.EVENT_CONTRIB_DIALOG_NEGATIVE, null);
      ctx.finish(false);
    }
  }

  @Override
  public void onCancel(DialogInterface dialog) {
    ContribInfoDialogActivity ctx = (ContribInfoDialogActivity) getActivity();
    if (ctx != null) {
      ctx.logEvent(Tracker.EVENT_CONTRIB_DIALOG_CANCEL, null);
      ctx.finish(true);
    }
  }

  @Override
  public void onClick(View v) {
    final LicenceStatus licenceStatus = licenceHandler.getLicenceStatus();
    if (v.getId() == R.id.contrib_feature_container || v == contribButton) {
      selectedPackage = Package.Contrib;
      updateButtons(contribButton);
    } else if (v.getId() == R.id.extended_feature_container || v == extendedButton) {
      selectedPackage = licenceStatus == null ? Package.Extended : Package.Upgrade;
      updateButtons(extendedButton);
    } else {
      Package[] proPackages = licenceHandler.getProPackages();
      if (proPackages.length == 1) {
        selectedPackage = proPackages[0];
        updateButtons(professionalButton);
      } else {
        PopupMenu popup = new PopupMenu(getActivity(), v);
        popup.setOnMenuItemClickListener(item -> {
          selectedPackage = Package.values()[item.getItemId()];
          String formattedPrice = licenceHandler.getFormattedPrice(selectedPackage);
          if (formattedPrice != null) {
            if (licenceStatus == EXTENDED) {
              String extendedUpgradeGoodieMessage = licenceHandler.getExtendedUpgradeGoodieMessage(selectedPackage);
              if (extendedUpgradeGoodieMessage != null) {
                formattedPrice += String.format(" (%s)", extendedUpgradeGoodieMessage);
              }
            }
            professionalPriceTextView.setText(formattedPrice);
          }
          updateButtons(professionalButton);
          return true;
        });
        for (Package aPackage : proPackages) {
          String title = licenceHandler.getFormattedPrice(aPackage);
          if (title == null) title = aPackage.name(); //fallback if prices have not been loaded
          popup.getMenu().add(Menu.NONE, aPackage.ordinal(), Menu.NONE, title);
        }
        popup.setOnDismissListener(menu -> {
          if (selectedPackage == null || !selectedPackage.isProfessional()) {
            professionalButton.setChecked(false);
          }
        });
        popup.show();
      }
    }
  }

  private void updateButtons(RadioButton selected) {
    if (contribVisible) contribButton.setChecked(contribButton == selected);
    if (extendedVisible) extendedButton.setChecked(extendedButton == selected);
    professionalButton.setChecked(professionalButton == selected);
    ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
  }
}