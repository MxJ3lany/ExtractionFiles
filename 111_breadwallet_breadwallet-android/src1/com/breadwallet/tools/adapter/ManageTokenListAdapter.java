package com.breadwallet.tools.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.breadwallet.R;
import com.breadwallet.presenter.activities.AddWalletsActivity;
import com.breadwallet.presenter.customviews.BaseTextView;
import com.breadwallet.presenter.entities.TokenItem;
import com.breadwallet.tools.animation.ItemTouchHelperAdapter;
import com.breadwallet.tools.animation.ItemTouchHelperViewHolder;
import com.breadwallet.tools.listeners.OnStartDragListener;
import com.breadwallet.tools.util.TokenUtil;
import com.breadwallet.tools.util.Utils;
import com.platform.entities.TokenListMetaData;
import com.platform.tools.KVStoreManager;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class ManageTokenListAdapter extends RecyclerView.Adapter<ManageTokenListAdapter.ManageTokenItemViewHolder> implements ItemTouchHelperAdapter {

    private static final String TAG = ManageTokenListAdapter.class.getSimpleName();
    private Context mContext;
    private ArrayList<TokenItem> mTokens;
    private OnTokenShowOrHideListener mListener;
    private OnStartDragListener mStartDragListener;
    private static final int VIEW_TYPE_WALLET = 0;
    private static final int VIEW_TYPE_ADD_WALLET = 1;

    public interface OnTokenShowOrHideListener {

        void onShowToken(TokenItem item);

        void onHideToken(TokenItem item);
    }

    public ManageTokenListAdapter(Context context, ArrayList<TokenItem> tokens, OnTokenShowOrHideListener listener, OnStartDragListener dragListener) {
        this.mContext = context;
        this.mTokens = tokens;
        this.mListener = listener;
        this.mStartDragListener = dragListener;
    }

    @Override
    public void onBindViewHolder(@NonNull final ManageTokenListAdapter.ManageTokenItemViewHolder holder, int position) {

        if (getItemViewType(position) == VIEW_TYPE_WALLET) {
            final TokenItem item = mTokens.get(position);
            String currencyCode = item.symbol.toLowerCase();

            String tokenIconPath = TokenUtil.getTokenIconPath(mContext, currencyCode, true);
            GradientDrawable iconDrawable = (GradientDrawable) holder.iconParent.getBackground();

            if (!Utils.isNullOrEmpty(tokenIconPath)) {
                holder.tokenIcon.setVisibility(View.VISIBLE);
                holder.tokenLetter.setVisibility(View.GONE);
                File iconFile = new File(tokenIconPath);
                Picasso.get().load(iconFile).into(holder.tokenIcon);
                iconDrawable.setColor(Color.TRANSPARENT);
            } else {
                // If no icon is present, then use the capital first letter of the token currency code instead.
                holder.tokenIcon.setVisibility(View.GONE);
                holder.tokenLetter.setVisibility(View.VISIBLE);
                holder.tokenLetter.setText(currencyCode.substring(0, 1).toUpperCase());
                iconDrawable.setColor(Color.parseColor(TokenUtil.getTokenStartColor(currencyCode)));
            }

            holder.tokenName.setText(mTokens.get(position).name);
            holder.tokenTicker.setText(mTokens.get(position).symbol);

            Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/CircularPro-Book.otf");
            holder.showHide.setTypeface(typeface);

            boolean isHidden = KVStoreManager.getTokenListMetaData(mContext).isCurrencyHidden(item.symbol);

            TypedValue showWalletTypedValue = new TypedValue();
            TypedValue hideWalletTypedValue = new TypedValue();

            mContext.getTheme().resolveAttribute(R.attr.show_wallet_button_background, showWalletTypedValue, true);
            mContext.getTheme().resolveAttribute(R.attr.hide_wallet_button_background, hideWalletTypedValue, true);

            holder.showHide.setBackground(mContext.getDrawable(isHidden ? showWalletTypedValue.resourceId : hideWalletTypedValue.resourceId));
            holder.showHide.setText(isHidden ? mContext.getString(R.string.TokenList_show) : mContext.getString(R.string.TokenList_hide));
            holder.showHide.setTextColor(mContext.getColor(isHidden ? R.color.button_add_wallet_text : R.color.button_cancel_add_wallet_text));

            holder.showHide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // If token is already hidden, show it
                    if (KVStoreManager.getTokenListMetaData(mContext).isCurrencyHidden(item.symbol)) {
                        mListener.onShowToken(item);
                        // If token is already showing, hide it
                    } else {
                        mListener.onHideToken(item);
                    }
                }
            });

            holder.dragHandle.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) ==
                            MotionEvent.ACTION_DOWN) {
                        mStartDragListener.onStartDrag(holder);
                    }
                    return false;
                }
            });
        } else {
            if (holder instanceof AddWalletItemViewHolder) {
                ((AddWalletItemViewHolder) holder).mParent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, AddWalletsActivity.class);
                        mContext.startActivity(intent);
                    }
                });
            }
        }

    }

    @NonNull
    @Override
    public ManageTokenListAdapter.ManageTokenItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        View convertView;

        if (viewType == VIEW_TYPE_WALLET) {
            convertView = inflater.inflate(R.layout.manage_wallets_list_item, parent, false);
            return new ManageTokenItemViewHolder(convertView);

        } else {
            convertView = inflater.inflate(R.layout.add_wallets_item, parent, false);
            return new AddWalletItemViewHolder(convertView);

        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < mTokens.size()) {
            return VIEW_TYPE_WALLET;
        } else {
            return VIEW_TYPE_ADD_WALLET;
        }
    }

    @Override
    public int getItemCount() {
        // We add 1 here because this adapter has an "extra" item at the bottom, which is the
        // "Add Wallets" footer
        return mTokens.size() + 1;
    }


    public class ManageTokenItemViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        private ImageButton dragHandle;
        private BaseTextView tokenTicker;
        private BaseTextView tokenName;
        private BaseTextView tokenBalance;
        private Button showHide;
        private ImageView tokenIcon;
        private BaseTextView tokenLetter;
        private View iconParent;

        public ManageTokenItemViewHolder(View view) {
            super(view);

            dragHandle = view.findViewById(R.id.drag_icon);
            tokenTicker = view.findViewById(R.id.token_symbol);
            tokenName = view.findViewById(R.id.token_name);
            tokenBalance = view.findViewById(R.id.token_balance);
            showHide = view.findViewById(R.id.show_hide_button);
            tokenIcon = view.findViewById(R.id.token_icon);
            tokenLetter = view.findViewById(R.id.icon_letter);
            iconParent = view.findViewById(R.id.icon_parent);

        }

        @Override
        public void onItemClear() {

        }

        @Override
        public void onItemSelected() {

        }

        public void setDragHandle(ImageButton dragHandle) {
            this.dragHandle = dragHandle;
        }
    }

    public class AddWalletItemViewHolder extends ManageTokenItemViewHolder {

        private BaseTextView mAddWalletsLabel;
        private View mParent;

        public AddWalletItemViewHolder(View view) {
            super(view);

            mAddWalletsLabel = view.findViewById(R.id.add_wallets);
            mParent = view.findViewById(R.id.wallet_card);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, mAddWalletsLabel.getId());
            mAddWalletsLabel.setLayoutParams(layoutParams);
            int leftPadding = (int) mContext.getResources().getDimension(R.dimen.manage_wallets_footer_padding_left);
            mAddWalletsLabel.setPadding(Utils.getPixelsFromDps(mContext, leftPadding), 0, 0, 0);
        }
    }

    @Override
    public void onItemDismiss(int position) {

    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        notifyItemMoved(fromPosition, toPosition);

        TokenListMetaData currentMd = KVStoreManager.getTokenListMetaData(mContext);

        // Only move this token to position that is not the last position, which is the "Add Wallet"
        // button
        if (toPosition < mTokens.size()) {
            Collections.swap(currentMd.enabledCurrencies, fromPosition, toPosition);
            Collections.swap(mTokens, fromPosition, toPosition);
            KVStoreManager.putTokenListMetaData(mContext, currentMd);
        }

    }
}
