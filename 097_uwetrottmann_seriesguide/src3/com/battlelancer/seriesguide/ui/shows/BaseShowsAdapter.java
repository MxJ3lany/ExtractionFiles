package com.battlelancer.seriesguide.ui.shows;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import com.battlelancer.seriesguide.R;
import com.battlelancer.seriesguide.util.ViewTools;

/**
 * Base adapter for the show item layout.
 */
public abstract class BaseShowsAdapter extends CursorAdapter {

    public interface OnItemClickListener {
        void onItemClick(View anchor, ShowViewHolder viewHolder);

        void onMenuClick(View view, ShowViewHolder viewHolder);

        void onFavoriteClick(int showTvdbId, boolean isFavorite);
    }

    protected OnItemClickListener onItemClickListener;
    private final VectorDrawableCompat drawableStar;
    private final VectorDrawableCompat drawableStarZero;

    protected BaseShowsAdapter(Activity activity, OnItemClickListener listener) {
        super(activity, null, 0);
        this.onItemClickListener = listener;

        Resources.Theme theme = activity.getTheme();
        drawableStar = ViewTools.vectorIconActive(activity, theme,
                R.drawable.ic_star_black_24dp);
        drawableStarZero = ViewTools.vectorIconActive(activity, theme,
                R.drawable.ic_star_border_black_24dp);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_show, parent, false);

        ShowViewHolder viewHolder = new ShowViewHolder(v, onItemClickListener);
        v.setTag(viewHolder);

        return v;
    }

    protected void setFavoriteState(ImageView view, boolean isFavorite) {
        view.setImageDrawable(isFavorite ? drawableStar : drawableStarZero);
        view.setContentDescription(view.getContext()
                .getString(isFavorite ? R.string.context_unfavorite : R.string.context_favorite));
    }

    protected void setRemainingCount(TextView textView, int unwatched) {
        if (unwatched > 0) {
            textView.setText(textView.getResources()
                    .getQuantityString(R.plurals.remaining_episodes_plural, unwatched, unwatched));
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setText(null);
            textView.setVisibility(View.GONE);
        }
    }

    public static class ShowViewHolder {

        public TextView name;
        public TextView timeAndNetwork;
        public TextView episode;
        public TextView episodeTime;
        public TextView remainingCount;
        public ImageView poster;
        public ImageView favorited;
        public ImageView contextMenu;

        public int showTvdbId;
        public int episodeTvdbId;
        public boolean isFavorited;
        public boolean isHidden;

        public ShowViewHolder(View v, @NonNull OnItemClickListener onItemClickListener) {
            name = v.findViewById(R.id.seriesname);
            timeAndNetwork = v.findViewById(R.id.textViewShowsTimeAndNetwork);
            episode = v.findViewById(R.id.TextViewShowListNextEpisode);
            episodeTime = v.findViewById(R.id.episodetime);
            remainingCount = v.findViewById(R.id.textViewShowsRemaining);
            poster = v.findViewById(R.id.showposter);
            favorited = v.findViewById(R.id.favoritedLabel);
            contextMenu = v.findViewById(R.id.imageViewShowsContextMenu);

            // item
            v.setOnClickListener(view -> onItemClickListener.onItemClick(view, this));
            // favorite star
            favorited.setOnClickListener(
                    view -> onItemClickListener.onFavoriteClick(showTvdbId, !isFavorited));
            // context menu
            contextMenu.setOnClickListener(
                    view -> onItemClickListener.onMenuClick(view, ShowViewHolder.this));
        }
    }
}
