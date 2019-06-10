package com.applozic.mobicomkit.uiwidgets.uikit;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by ashish on 29/05/18.
 */

public abstract class AlScrollListener extends RecyclerView.OnScrollListener {
    private int mScrollThreshold = 40;
    private int scrolledDistance = 0;
    private static final int HIDE_THRESHOLD = 20;
    private int previousTotal = 0; // The total number of items in the dataset after the last load
    private boolean loading = true; // True if we are still waiting for the last set of data to load.
    private int visibleThreshold = 2; // The minimum amount of items to have below your current scroll position before loading more.
    private int firstVisibleItem, visibleItemCount, totalItemCount;
    private boolean infiniteScrollingEnabled = true;
    private boolean controlsVisible = true;

    public abstract void onScrollUp();

    public abstract void onScrollDown();

    public abstract void onLoadMore();

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();

        visibleItemCount = recyclerView.getChildCount();
        if (manager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) manager;
            firstVisibleItem = gridLayoutManager.findFirstVisibleItemPosition();
            totalItemCount = gridLayoutManager.getItemCount();
        } else if (manager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) manager;
            firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
            totalItemCount = linearLayoutManager.getItemCount();
        }


        if (infiniteScrollingEnabled) {
            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                }
            }

            if ((totalItemCount - visibleItemCount) == 0) {
                return;
            }


            if (!loading && (totalItemCount - visibleItemCount <= firstVisibleItem + visibleThreshold)) {
                // End has been reached
                // do something
                onLoadMore();
                loading = true;
            }
        }

        if (firstVisibleItem == 0) {
            if (!controlsVisible) {
                onScrollUp();
                controlsVisible = true;
            }

            return;
        }

        if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
            onScrollDown();
            controlsVisible = false;
            scrolledDistance = 0;
        } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
            onScrollUp();
            controlsVisible = true;
            scrolledDistance = 0;
        }

        if ((controlsVisible && dy > 0) || (!controlsVisible && dy < 0)) {
            scrolledDistance += dy;
        }
    }

    public void setScrollThreshold(int scrollThreshold) {
        mScrollThreshold = scrollThreshold;
    }

    public void stopInfiniteScrolling() {
        infiniteScrollingEnabled = false;
    }

    public void onDataCleared() {
        previousTotal = 0;
    }
}
