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

package org.sufficientlysecure.keychain.ui.transfer.view;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewAnimator;

import org.sufficientlysecure.keychain.R;
import org.sufficientlysecure.keychain.ui.util.recyclerview.DividerItemDecoration;


public class ReceivedSecretKeyList extends RecyclerView {
    private static final int STATE_INVISIBLE = 0;
    // private static final int STATE_BUTTON = 1; // used in TransferSecretKeyList
    private static final int STATE_PROGRESS = 2;
    private static final int STATE_TRANSFERRED = 3;
    private static final int STATE_IMPORT_BUTTON = 4;


    public ReceivedSecretKeyList(Context context) {
        super(context);
        init(context);
    }

    public ReceivedSecretKeyList(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ReceivedSecretKeyList(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        setLayoutManager(new LinearLayoutManager(context));
        addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL_LIST, true));
    }

    public static class ReceivedKeyAdapter extends Adapter<ReceivedKeyViewHolder> {
        private final Context context;
        private final LayoutInflater layoutInflater;
        private final OnClickImportKeyListener onClickImportKeyListener;

        private Long focusedMasterKeyId;
        private List<ReceivedKeyItem> data = new ArrayList<>();
        private ArrayList<Long> finishedItems = new ArrayList<>();


        public ReceivedKeyAdapter(Context context, LayoutInflater layoutInflater,
                OnClickImportKeyListener onClickImportKeyListener) {
            this.context = context;
            this.layoutInflater = layoutInflater;
            this.onClickImportKeyListener = onClickImportKeyListener;
        }

        @Override
        public ReceivedKeyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ReceivedKeyViewHolder(layoutInflater.inflate(R.layout.key_transfer_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ReceivedKeyViewHolder holder, int position) {
            ReceivedKeyItem item = data.get(position);
            boolean isFinished = finishedItems.contains(item.masterKeyId);
            holder.bind(context, item, onClickImportKeyListener, focusedMasterKeyId, isFinished);
        }

        @Override
        public int getItemCount() {
            return data != null ? data.size() : 0;
        }

        @Override
        public long getItemId(int position) {
            return data.get(position).masterKeyId;
        }

        public void addToFinishedItems(long masterKeyId) {
            finishedItems.add(masterKeyId);
            // doeesn't notify, because it's non-trivial and this is called in conjunction with other refreshing things!
        }

        public void focusItem(Long masterKeyId) {
            focusedMasterKeyId = masterKeyId;
            notifyItemRangeChanged(0, getItemCount());
        }

        public void addItem(ReceivedKeyItem receivedKeyItem) {
            data.add(receivedKeyItem);
            notifyItemInserted(data.size() -1);
        }

        public void clear() {
            data.clear();
            finishedItems.clear();
            focusedMasterKeyId = null;
            notifyDataSetChanged();
        }
    }

    static class ReceivedKeyViewHolder extends ViewHolder {
        private final TextView vName;
        private final TextView vEmail;
        private final TextView vCreation;
        private final View vImportButton;
        private final ViewAnimator vState;

        ReceivedKeyViewHolder(View itemView) {
            super(itemView);

            vName = itemView.findViewById(R.id.key_list_item_name);
            vEmail = itemView.findViewById(R.id.key_list_item_email);
            vCreation = itemView.findViewById(R.id.key_list_item_creation);

            vImportButton = itemView.findViewById(R.id.button_import);
            vState = itemView.findViewById(R.id.transfer_state);
        }

        private void bind(Context context, final ReceivedKeyItem item,
                final OnClickImportKeyListener onClickReceiveKeyListener, Long focusedMasterKeyId,
                boolean isFinished) {
            if (item.name != null) {
                vName.setText(item.name);
                vName.setVisibility(View.VISIBLE);
            } else {
                vName.setVisibility(View.GONE);
            }
            if (item.email != null) {
                vEmail.setText(item.email);
                vEmail.setVisibility(View.VISIBLE);
            } else {
                vEmail.setVisibility(View.GONE);
            }

            String dateTime = DateUtils.formatDateTime(context, item.creationMillis,
                    DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME |
                            DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_ABBREV_MONTH);
            vCreation.setText(context.getString(R.string.label_key_created, dateTime));

            if (focusedMasterKeyId != null) {
                if (focusedMasterKeyId != item.masterKeyId) {
                    itemView.animate().alpha(0.2f).start();
                    vState.setDisplayedChild(isFinished ? STATE_TRANSFERRED : STATE_INVISIBLE);
                } else {
                    itemView.setAlpha(1.0f);
                    vState.setDisplayedChild(STATE_PROGRESS);
                }
            } else {
                itemView.animate().alpha(1.0f).start();
                vState.setDisplayedChild(isFinished ? STATE_TRANSFERRED : STATE_IMPORT_BUTTON);
            }

            if (focusedMasterKeyId == null && onClickReceiveKeyListener != null) {
                vImportButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickReceiveKeyListener.onUiClickImportKey(item.masterKeyId, item.keyData);
                    }
                });
            } else {
                vImportButton.setOnClickListener(null);
            }
        }
    }

    public interface OnClickImportKeyListener {
        void onUiClickImportKey(long masterKeyId, String keyData);
    }

    public static class ReceivedKeyItem {
        private final String keyData;

        private final long masterKeyId;
        private final long creationMillis;
        private final String name;
        private final String email;

        public ReceivedKeyItem(String keyData, long masterKeyId, long creationMillis, String name, String email) {
            this.keyData = keyData;
            this.masterKeyId = masterKeyId;
            this.creationMillis = creationMillis;
            this.name = name;
            this.email = email;
        }
    }
}
