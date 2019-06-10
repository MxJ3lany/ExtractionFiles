package org.fdroid.fdroid.views.whatsnew;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import org.fdroid.fdroid.BuildConfig;
import org.fdroid.fdroid.R;
import org.fdroid.fdroid.data.App;
import org.fdroid.fdroid.views.categories.AppCardController;

public class WhatsNewAdapter extends RecyclerView.Adapter<AppCardController> {

    private Cursor cursor;
    private final Activity activity;
    private final RecyclerView.ItemDecoration appListDecorator;

    public WhatsNewAdapter(Activity activity) {
        this.activity = activity;
        appListDecorator = new WhatsNewAdapter.ItemDecorator(activity);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        recyclerView.addItemDecoration(appListDecorator);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.removeItemDecoration(appListDecorator);
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @NonNull
    @Override
    public AppCardController onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout;
        switch (viewType) {
            case R.id.whats_new_large_tile:
                layout = R.layout.app_card_large;
                break;
            case R.id.whats_new_small_tile:
                layout = R.layout.app_card_horizontal;
                break;
            case R.id.whats_new_regular_list:
                layout = R.layout.app_card_list_item;
                break;
            default:
                throw new IllegalArgumentException("Unknown view type when rendering \"Whats New\": " + viewType);
        }

        return new AppCardController(activity, activity.getLayoutInflater().inflate(layout, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        int relativePositionInCycle = position % 5;

        if (BuildConfig.FLAVOR.startsWith("basic")) {
            if (relativePositionInCycle > 0) {
                return R.id.whats_new_small_tile;
            } else {
                return R.id.whats_new_regular_list;
            }
        }

        if (position == 0) {
            return R.id.whats_new_regular_list;
        } else {
            switch (relativePositionInCycle) {
                case 1:
                case 2:
                    return R.id.whats_new_large_tile;

                case 3:
                case 4:
                    return R.id.whats_new_small_tile;

                case 0:
                default:
                    return R.id.whats_new_regular_list;
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull AppCardController holder, int position) {
        cursor.moveToPosition(position);
        final App app = new App(cursor);
        holder.bindApp(app);
    }

    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    public void setAppsCursor(Cursor cursor) {
        if (this.cursor == cursor) {
            //don't notify when the cursor did not change
            return;
        }
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    public static class SpanSizeLookup extends GridLayoutManager.SpanSizeLookup {
        @Override
        public int getSpanSize(int position) {
            int relativePositionInCycle = position % 5;
            if (relativePositionInCycle == 0) {
                return 2;
            } else {
                return 1;
            }
        }
    }

    /**
     * Applies padding to items, ensuring that the spacing on the left, centre, and right all match.
     * The vertical padding is slightly shorter than the horizontal padding also.
     *
     * @see org.fdroid.fdroid.R.dimen#whats_new__padding__app_card__horizontal
     * @see org.fdroid.fdroid.R.dimen#whats_new__padding__app_card__vertical
     */
    private static class ItemDecorator extends RecyclerView.ItemDecoration {
        private final Context context;

        ItemDecorator(Context context) {
            this.context = context.getApplicationContext();
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            Resources resources = context.getResources();
            int horizontalPadding = (int) resources.getDimension(R.dimen.whats_new__padding__app_card__horizontal);
            int verticalPadding = (int) resources.getDimension(R.dimen.whats_new__padding__app_card__vertical);

            int relativePositionInCycle = position % 5;
            if (relativePositionInCycle != 0) {
                // The item on the left will have both left and right padding. The item on the right
                // will only have padding on the right. This will allow the same amount of padding
                // on the left, centre, and right of the grid, rather than double the padding in the
                // middle (which would happen if both left+right paddings were set for both items).
                boolean isLtr = ViewCompat.getLayoutDirection(parent) == ViewCompat.LAYOUT_DIRECTION_LTR;
                boolean isAtStart = relativePositionInCycle == 1 || relativePositionInCycle == 3;
                int paddingStart = isAtStart ? horizontalPadding : 0;
                int paddingLeft = isLtr ? paddingStart : horizontalPadding;
                int paddingRight = isLtr ? horizontalPadding : paddingStart;
                outRect.set(paddingLeft, 0, paddingRight, verticalPadding);
            } else {
                outRect.set(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
            }
        }
    }
}
