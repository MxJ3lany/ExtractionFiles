/*
 * Copyright (C) 2017 Schürmann & Breitmoser GbR
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.sufficientlysecure.keychain.ui.adapter;


import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.bouncycastle.bcpg.sig.KeyFlags;
import org.sufficientlysecure.keychain.R;
import org.sufficientlysecure.keychain.service.SaveKeyringParcel;
import org.sufficientlysecure.keychain.ui.dialog.AddSubkeyDialogFragment;
import org.sufficientlysecure.keychain.ui.util.KeyFormattingUtils;

public class SubkeysAddedAdapter extends ArrayAdapter<SaveKeyringParcel.SubkeyAdd> {
    private LayoutInflater mInflater;
    private Activity mActivity;

    public SubkeysAddedAdapter(Activity activity, List<SaveKeyringParcel.SubkeyAdd> data) {
        super(activity, -1, data);
        mActivity = activity;
        mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    static class ViewHolder {
        public View itemView;
        public TextView vKeyId;
        public TextView vKeyDetails;
        public TextView vKeyExpiry;
        public ImageView vCertifyIcon;
        public ImageView vSignIcon;
        public ImageView vEncryptIcon;
        public ImageView vAuthenticateIcon;
        // also hold a reference to the model item
        public SaveKeyringParcel.SubkeyAdd mModel;
    }


    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            // Not recycled, inflate a new view
            convertView = mInflater.inflate(R.layout.view_key_adv_subkey_item, parent, false);
            final ViewHolder holder = new ViewHolder();
            holder.itemView = convertView;
            holder.vKeyId = convertView.findViewById(R.id.subkey_item_key_id);
            holder.vKeyDetails = convertView.findViewById(R.id.subkey_item_details);
            holder.vKeyExpiry = convertView.findViewById(R.id.subkey_item_status);
            holder.vCertifyIcon = convertView.findViewById(R.id.subkey_item_ic_certify);
            holder.vSignIcon = convertView.findViewById(R.id.subkey_item_ic_sign);
            holder.vEncryptIcon = convertView.findViewById(R.id.subkey_item_ic_encrypt);
            holder.vAuthenticateIcon = convertView.findViewById(R.id.subkey_item_ic_authenticate);

            convertView.setTag(holder);
        }

        final ViewHolder holder = (ViewHolder) convertView.getTag();

        // save reference to model item
        holder.mModel = getItem(position);

        String algorithmStr = KeyFormattingUtils.getAlgorithmInfo(
                mActivity,
                holder.mModel.getAlgorithm(),
                holder.mModel.getKeySize(),
                holder.mModel.getCurve()
        );

        boolean isMasterKey = position == 0;
        if (isMasterKey) {
            holder.vKeyId.setTypeface(null, Typeface.BOLD);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // swapping out the old master key with newly set master key
                    AddSubkeyDialogFragment addSubkeyDialogFragment =
                            AddSubkeyDialogFragment.newInstance(true);
                    addSubkeyDialogFragment
                            .setOnAlgorithmSelectedListener(
                                    new AddSubkeyDialogFragment.OnAlgorithmSelectedListener() {
                                        @Override
                                        public void onAlgorithmSelected(SaveKeyringParcel.SubkeyAdd newSubkey) {
                                            // calculate manually as the provided position variable
                                            // is not always accurate
                                            int pos = SubkeysAddedAdapter.this.getPosition(holder.mModel);
                                            SubkeysAddedAdapter.this.remove(holder.mModel);
                                            SubkeysAddedAdapter.this.insert(newSubkey, pos);
                                        }
                                    }
                            );
                    addSubkeyDialogFragment.show(
                            ((FragmentActivity)mActivity).getSupportFragmentManager()
                            , "addSubkeyDialog");
                }
            });
        } else {
            holder.vKeyId.setTypeface(null, Typeface.NORMAL);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // remove reference model item from adapter (data and notify about change)
                    SubkeysAddedAdapter.this.remove(holder.mModel);
                }
            });
        }

        holder.vKeyId.setText(R.string.edit_key_new_subkey);
        holder.vKeyDetails.setText(algorithmStr);

        if (holder.mModel.getExpiry() != 0L) {
            Date expiryDate = new Date(holder.mModel.getExpiry() * 1000);
            Calendar expiryCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            expiryCal.setTime(expiryDate);
            // convert from UTC to time zone of device
            expiryCal.setTimeZone(TimeZone.getDefault());

            holder.vKeyExpiry.setText(getContext().getString(R.string.label_expiry) + ": "
                    + DateFormat.getDateFormat(getContext()).format(expiryCal.getTime()));
        } else {
            holder.vKeyExpiry.setText(getContext().getString(R.string.label_expiry) + ": "
                    + getContext().getString(R.string.none));
        }

        int flags = holder.mModel.getFlags();
        if ((flags & KeyFlags.CERTIFY_OTHER) > 0) {
            holder.vCertifyIcon.setVisibility(View.VISIBLE);
        } else {
            holder.vCertifyIcon.setVisibility(View.GONE);
        }
        if ((flags & KeyFlags.SIGN_DATA) > 0) {
            holder.vSignIcon.setVisibility(View.VISIBLE);
        } else {
            holder.vSignIcon.setVisibility(View.GONE);
        }
        if (((flags & KeyFlags.ENCRYPT_COMMS) > 0)
                || ((flags & KeyFlags.ENCRYPT_STORAGE) > 0)) {
            holder.vEncryptIcon.setVisibility(View.VISIBLE);
        } else {
            holder.vEncryptIcon.setVisibility(View.GONE);
        }
        if ((flags & KeyFlags.AUTHENTICATION) > 0) {
            holder.vAuthenticateIcon.setVisibility(View.VISIBLE);
        } else {
            holder.vAuthenticateIcon.setVisibility(View.GONE);
        }

        return convertView;
    }

}
