package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

public class FragmentPro extends FragmentBase implements SharedPreferences.OnSharedPreferenceChangeListener {
    private TextView tvPending;
    private TextView tvActivated;
    private TextView tvList;
    private Button btnPurchase;
    private TextView tvPrice;
    private TextView tvPriceHint;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setSubtitle(R.string.menu_pro);

        View view = inflater.inflate(R.layout.fragment_pro, container, false);

        tvPending = view.findViewById(R.id.tvPending);
        tvActivated = view.findViewById(R.id.tvActivated);
        tvList = view.findViewById(R.id.tvList);
        btnPurchase = view.findViewById(R.id.btnPurchase);
        tvPrice = view.findViewById(R.id.tvPrice);
        tvPriceHint = view.findViewById(R.id.tvPriceHint);

        tvList.setText(HtmlHelper.fromHtml(
                "<a href=\"" + BuildConfig.PRO_FEATURES_URI + "\">" + Html.escapeHtml(getString(R.string.title_pro_list)) + "</a>"));
        tvList.setMovementMethod(LinkMovementMethod.getInstance());

        btnPurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
                lbm.sendBroadcast(new Intent(ActivityView.ACTION_PURCHASE));
            }
        });

        tvPriceHint.setMovementMethod(LinkMovementMethod.getInstance());

        tvPending.setVisibility(View.GONE);
        tvActivated.setVisibility(View.GONE);
        btnPurchase.setEnabled(false);
        tvPrice.setText(null);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        addBillingListener(new ActivityBilling.IBillingListener() {
            @Override
            public void onSkuDetails(String sku, String price) {
                if (ActivityBilling.getSkuPro().equals(sku)) {
                    tvPrice.setText(price);
                    btnPurchase.setEnabled(true);
                }
            }

            @Override
            public void onPurchasePending(String sku) {
                if (ActivityBilling.getSkuPro().equals(sku)) {
                    btnPurchase.setEnabled(false);
                    tvPending.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPurchased(String sku) {
                if (ActivityBilling.getSkuPro().equals(sku)) {
                    btnPurchase.setEnabled(false);
                    tvPending.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.registerOnSharedPreferenceChangeListener(this);
        onSharedPreferenceChanged(prefs, "pro");
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if ("pro".equals(key)) {
            boolean pro = prefs.getBoolean(key, false);
            tvActivated.setVisibility(pro ? View.VISIBLE : View.GONE);

            if (!Helper.isPlayStoreInstall(getContext()))
                btnPurchase.setEnabled(!pro || BuildConfig.DEBUG);
        }
    }
}
