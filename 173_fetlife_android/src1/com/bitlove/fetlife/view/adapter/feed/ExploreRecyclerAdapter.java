package com.bitlove.fetlife.view.adapter.feed;

import com.bitlove.fetlife.FetLifeApplication;
import com.bitlove.fetlife.view.screen.resource.ExploreActivity;

public class ExploreRecyclerAdapter extends FeedRecyclerAdapter {

    private final ExploreActivity.Explore exploreType;

    public ExploreRecyclerAdapter(FetLifeApplication fetLifeApplication, ExploreActivity.Explore exploreType, OnFeedItemClickListener onFeedItemClickListener, String memberId) {
        super(fetLifeApplication,onFeedItemClickListener,memberId);
        this.exploreType = exploreType;
        loadItems();
    }

    @Override
    protected void loadItems() {
        if (exploreType == null) {
            return;
        }
        itemList = fetLifeApplication.getInMemoryStorage().getExploreFeed(exploreType);
    }
}

