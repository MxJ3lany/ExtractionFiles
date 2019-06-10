package com.applozic.mobicomkit.uiwidgets.uikit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;

/**
 * Created by ashish on 14/05/18.
 */

public class AlTypingIndicator extends LinearLayout {

    private ALTypingReceiver mReceiver;
    String typerUserId;
    boolean isTyping = false;
    private TextView typingText;


    public AlTypingIndicator(Context context) {
        super(context);
    }

    public AlTypingIndicator(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlTypingIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * This method is always called whenever the typing status change is received from the subscribed userId or Group
     * Be careful to instantiate your views here.
     */
    public void createView() {
        Contact contact = new AppContactService(getContext()).getContactById(typerUserId);
        removeAllViews();

        if (typingText == null) {
            typingText = new TextView(getContext());
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            typingText.setLayoutParams(params);
            typingText.setTextSize(14);
            typingText.setTextColor(getContext().getResources().getColor(R.color.apploizc_black_color));
        }

        addView(typingText);

        if (isTyping) {
            typingText.setText((contact != null ? contact.getDisplayName() : "") + " is Typing...");
        } else {
            typingText.setText((contact != null ? contact.getDisplayName() : "") + " is not Typing...");
        }
    }

    public boolean isTyping() {
        return isTyping;
    }

    public String getTyperUserId() {
        return typerUserId;
    }

    public void subscribe(Channel channel, Contact contact) {
        Applozic.subscribeToTyping(getContext(), channel, contact);
        if (mReceiver == null) {
            mReceiver = new ALTypingReceiver();
        }
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, new IntentFilter(BroadcastService.INTENT_ACTIONS.UPDATE_TYPING_STATUS.toString()));
    }

    public void unSubscribe(Channel channel, Contact contact) {
        Applozic.unSubscribeToTyping(getContext(), channel, contact);
        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    private class ALTypingReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            typerUserId = intent.getStringExtra("userId");
            String typing = intent.getStringExtra("isTyping");
            isTyping = "1".equals(typing);

            createView();
        }
    }
}
