package com.applozic.mobicomkit.sample;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MessageBuilder;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.listners.ApplozicUIListener;
import com.applozic.mobicomkit.uiwidgets.uikit.AlAttachmentOptions;
import com.applozic.mobicomkit.uiwidgets.uikit.AlAttachmentView;
import com.applozic.mobicomkit.uiwidgets.uikit.AlConversationFragment;
import com.applozic.mobicomkit.uiwidgets.uikit.AlMessageSenderView;
import com.applozic.mobicomkit.uiwidgets.uikit.AlTypingIndicator;

public class SampleActivity extends AppCompatActivity implements AlMessageSenderView.AlMessageViewEvents, ApplozicUIListener {

    AlTypingIndicator typingIndicator;
    AlMessageSenderView messageSenderView;
    AlAttachmentView attachmentView;
    LinearLayout snackBarLayout;
    FrameLayout frameLayout;
    AlConversationFragment conversationFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        typingIndicator = findViewById(R.id.alTypingIndicator);
        messageSenderView = findViewById(R.id.alMessageSenderView);
        attachmentView = findViewById(R.id.alAttachmentView);
        snackBarLayout = findViewById(R.id.snackbarLayout);
        conversationFragment = (AlConversationFragment) getSupportFragmentManager().findFragmentById(R.id.conversationFragment);

        messageSenderView.createView(new AppContactService(this).getContactById("reytum6"), null, this);
        attachmentView.createView();
        Applozic.getInstance(this).registerUIListener(this);
    }

    @Override
    public void onBackPressed() {
        if (attachmentView.getVisibility() == View.VISIBLE) {
            attachmentView.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onTyping(EditText editText, boolean typingStarted) {

    }

    @Override
    public void onFocus(EditText editText, boolean hasFocus) {
        if (attachmentView != null) {
            attachmentView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(EditText editText) {
        if (attachmentView != null) {
            attachmentView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAttachmentButtonClick() {
        if (attachmentView != null) {
            attachmentView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSendButtonClicked(EditText editText) {
        if (editText != null) {
            if (!TextUtils.isEmpty(editText.getText().toString())) {
                new MessageBuilder(this).setTo("reytum7").setContentType(Message.ContentType.DEFAULT.getValue()).setMessage(editText.getText().toString()).send();
                editText.setText("");
            }
        }
    }

    @Override
    public void onRecordButtonClicked() {

    }

    @Override
    protected void onPause() {
        Applozic.disconnectPublish(this);
        typingIndicator.unSubscribe(null, new AppContactService(this).getContactById("reytum6"));
        super.onPause();
    }

    @Override
    protected void onResume() {
        Applozic.connectPublish(this);
        typingIndicator.subscribe(null, new AppContactService(this).getContactById("reytum6"));
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Applozic.getInstance(this).unregisterUIListener();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        AlAttachmentOptions.handleAttachmentOptionsResult(requestCode, resultCode, data, this, "reytum6", null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (AlAttachmentOptions.isApplozicPermissionCode(requestCode)) {
            AlAttachmentOptions.onRequestPermissionsResult(requestCode, permissions, grantResults, snackBarLayout, this);
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onMessageSent(Message message) {
        if (conversationFragment != null) {
            conversationFragment.addMessage(message);
        }
    }

    @Override
    public void onMessageReceived(Message message) {
        if (conversationFragment != null) {
            conversationFragment.addMessage(message);
        }
    }

    @Override
    public void onLoadMore(boolean loadMore) {

    }

    @Override
    public void onMessageSync(Message message, String key) {
        if (conversationFragment != null) {
            conversationFragment.addMessage(message);
        }
    }

    @Override
    public void onMessageDeleted(String messageKey, String userId) {
        if (conversationFragment != null) {
            conversationFragment.notifyAdapter();
        }
    }

    @Override
    public void onMessageDelivered(Message message, String userId) {

    }

    @Override
    public void onAllMessagesDelivered(String userId) {

    }

    @Override
    public void onAllMessagesRead(String userId) {

    }

    @Override
    public void onConversationDeleted(String userId, Integer channelKey, String response) {
        if (conversationFragment != null) {
            if ("success".equals(response)) {
                conversationFragment.removeMessage(userId, channelKey);
            }
        }
    }

    @Override
    public void onUpdateTypingStatus(String userId, String isTyping) {

    }

    @Override
    public void onUpdateLastSeen(String userId) {

    }

    @Override
    public void onMqttDisconnected() {

    }

    @Override
    public void onMqttConnected() {

    }

    @Override
    public void onUserOnline() {

    }

    @Override
    public void onUserOffline() {

    }

    @Override
    public void onChannelUpdated() {
        if (conversationFragment != null) {
            conversationFragment.notifyAdapter();
        }
    }

    @Override
    public void onConversationRead(String userId, boolean isGroup) {

    }

    @Override
    public void onUserDetailUpdated(String userId) {
        if (conversationFragment != null) {
            conversationFragment.notifyAdapter();
        }
    }

    @Override
    public void onMessageMetadataUpdated(String keyString) {
        if (conversationFragment != null) {
            conversationFragment.notifyAdapter();
        }
    }

    @Override
    public void onUserMute(boolean mute, String userId) {

    }
}
