package com.applozic.mobicomkit.uiwidgets.uikit.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.R;

import java.util.List;

/**
 * Created by ashish on 01/06/18.
 */

public abstract class AlFooterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static int LOADER_VIEW = 1;
    public static int CONVERSATION_VIEW = 2;
    private boolean setLoading;
    protected List<Message> mItems;
    protected LayoutInflater mInflater;

    public AlFooterAdapter(Context context, List<Message> mItems) {
        mInflater = LayoutInflater.from(context);
        this.mItems = mItems;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == LOADER_VIEW) {
            View view = mInflater.inflate(R.layout.al_footer_view_layout, viewGroup, false);
            return new FooterViewHolder(view);

        } else if (viewType == CONVERSATION_VIEW) {
            return getConversationViewHolder(viewGroup);
        }

        throw new IllegalArgumentException("Invalid ViewType: " + viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        if (viewHolder instanceof FooterViewHolder) {
            FooterViewHolder loaderViewHolder = (FooterViewHolder) viewHolder;
            loaderViewHolder.progressBar.setVisibility(setLoading ? View.VISIBLE : View.GONE);

            return;
        }

        bindConversationViewHolder(viewHolder, position);
    }

    @Override
    public int getItemViewType(int position) {
        if (position != 0 && position == getItemCount() - 1) {
            return LOADER_VIEW;
        }
        return CONVERSATION_VIEW;
    }

    @Override
    public int getItemCount() {
        if (mItems == null || mItems.size() == 0) {
            return 0;
        }
        return mItems.size() + 1;
    }

    public void showLoading(boolean status) {
        setLoading = status;
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;

        public FooterViewHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.loadingProgress);
        }
    }

    public abstract RecyclerView.ViewHolder getConversationViewHolder(ViewGroup parent);
    public abstract void bindConversationViewHolder(RecyclerView.ViewHolder holder, int position);
}
