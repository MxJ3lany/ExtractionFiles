/*
 * Copyright (c) 2016 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.douya.broadcast.ui;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;

import butterknife.BindDimen;
import me.zhanghai.android.douya.R;
import me.zhanghai.android.douya.broadcast.content.TimelineBroadcastListResource;
import me.zhanghai.android.douya.util.FragmentUtils;

public class BroadcastListFragment extends BaseTimelineBroadcastListFragment {

    private static final String KEY_PREFIX = BroadcastListFragment.class.getName() + '.';

    private static final String EXTRA_USER_ID_OR_UID = KEY_PREFIX + "user_id_or_uid";
    private static final String EXTRA_TOPIC = KEY_PREFIX + "topic";

    @BindDimen(R.dimen.toolbar_height)
    int mToolbarHeight;

    private String mUserIdOrUid;
    private String mTopic;

    public static BroadcastListFragment newInstance(String userIdOrUid, String topic) {
        //noinspection deprecation
        BroadcastListFragment fragment = new BroadcastListFragment();
        FragmentUtils.getArgumentsBuilder(fragment)
                .putString(EXTRA_USER_ID_OR_UID, userIdOrUid)
                .putString(EXTRA_TOPIC, topic);
        return fragment;
    }

    /**
     * @deprecated Use {@link #newInstance(String, String)} instead.
     */
    public BroadcastListFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        mUserIdOrUid = arguments.getString(EXTRA_USER_ID_OR_UID);
        mTopic = arguments.getString(EXTRA_TOPIC);
    }

    @Override
    protected int getExtraPaddingTop() {
        return mToolbarHeight;
    }

    @Override
    protected TimelineBroadcastListResource onAttachResource() {
        return TimelineBroadcastListResource.attachTo(mUserIdOrUid, mTopic, this);
    }

    @Override
    protected void onSendBroadcast() {
        Activity activity = getActivity();
        activity.startActivity(SendBroadcastActivity.makeTopicIntent(mTopic, activity));
    }
}
