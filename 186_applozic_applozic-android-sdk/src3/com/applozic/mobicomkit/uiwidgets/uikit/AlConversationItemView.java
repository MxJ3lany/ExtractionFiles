package com.applozic.mobicomkit.uiwidgets.uikit;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.R;

/**
 * Created by ashish on 25/05/18.
 */

public class AlConversationItemView extends LinearLayout {

    public AlConversationItemView(Context context) {
        super(context);
    }

    public AlConversationItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AlConversationItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void createView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.al_conversation_item_layout, null);

        TextView alphabeticImage = view.findViewById(R.id.alphabeticImage);
        ImageView profileImage = view.findViewById(R.id.contactImage);
        TextView receiverName = view.findViewById(R.id.smReceivers);
        TextView messageTv = view.findViewById(R.id.message);
        TextView unreadCount = view.findViewById(R.id.unreadSmsCount);
        TextView createdAtTime = view.findViewById(R.id.createdAtTime);
        ImageView attachmentIcon = view.findViewById(R.id.attachmentIcon);
    }

    public void bindView(){

    }

    public String getTitle(Message message) {
        return null;
    }

    public String getMessage(Message message) {
        return message.getMessage();
    }

    public String getUnreadCount(Message message) {
        return null;
    }

    public String getFormattedTime(Message message) {
        return null;
    }
}
