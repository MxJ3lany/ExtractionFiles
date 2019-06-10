package com.applozic.mobicomkit.uiwidgets.uikit;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;

/**
 * Created by ashish on 14/05/18.
 */

public class AlMessageSenderView extends LinearLayout {
    EditText messageEditText;
    ImageButton sendMessageButton;
    ImageButton audioRecordButton;
    ImageButton attachmentButton;
    ImageButton emoticonsButton;
    boolean typingStarted;
    private AlMessageViewEvents listener;


    public AlMessageSenderView(Context context) {
        this(context, null, 0);
    }

    public AlMessageSenderView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlMessageSenderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    public void createView(Contact contact, Channel channel, final AlMessageViewEvents listener) {
        removeAllViews();
        this.listener = listener;

        LayoutInflater inflater = (LayoutInflater) getContext().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.al_message_sender_view, null);

        LinearLayout mainEditTextLayout = view.findViewById(R.id.main_edit_text_linear_layout);
        messageEditText = mainEditTextLayout.findViewById(R.id.conversation_message);
        attachmentButton = mainEditTextLayout.findViewById(R.id.attach_button);
        emoticonsButton = mainEditTextLayout.findViewById(R.id.emoticons_btn);
        emoticonsButton.setVisibility(GONE);
        messageEditText.setHint("Write a message...");

        FrameLayout actionButtonLayout = view.findViewById(R.id.actionButtonLayout);
        sendMessageButton = actionButtonLayout.findViewById(R.id.conversation_send);
        audioRecordButton = actionButtonLayout.findViewById(R.id.record_button);
        audioRecordButton.setVisibility(GONE);
        sendMessageButton.setVisibility(VISIBLE);

        ((LinearLayout) view).removeAllViews();

        publishTypingStatus(messageEditText, contact, channel);

        attachListeners();

        addView(mainEditTextLayout);
        addView(actionButtonLayout);
    }

    public void attachListeners() {
        attachmentButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onAttachmentButtonClick();
                }
            }
        });

        sendMessageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onSendButtonClicked(messageEditText);
                }
            }
        });

        audioRecordButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onRecordButtonClicked();
                }
            }
        });
    }

    public void publishTypingStatus(final EditText editText, final Contact contact, final Channel channel) {

        if (editText == null || (contact == null && channel == null)) {
            return;
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (!TextUtils.isEmpty(s.toString()) && s.toString().trim().length() > 0 && !typingStarted) {
                        typingStarted = true;
                        if (contact != null || (channel != null && !Channel.GroupType.OPEN.getValue().equals(channel.getType()))) {
                            Applozic.publishTypingStatus(getContext(), channel, contact, true);
                        }
                    } else if (s.toString().trim().length() == 0 && typingStarted) {
                        typingStarted = false;
                        if (contact != null || (channel != null && !Channel.GroupType.OPEN.getValue().equals(channel.getType()))) {
                            Applozic.publishTypingStatus(getContext(), channel, contact, false);
                        }
                    }

                    if (listener != null) {
                        listener.onTyping(editText, typingStarted);
                    }

                } catch (Exception e) {

                }
            }
        });

        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (listener != null) {
                    listener.onFocus(editText, hasFocus);
                }

                if (hasFocus && typingStarted && (contact != null || (channel != null && !Channel.GroupType.OPEN.getValue().equals(channel.getType())))) {
                    Applozic.publishTypingStatus(getContext(), channel, contact, typingStarted);
                }

            }
        });

        editText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(editText);
                }
            }
        });
    }

    public interface AlMessageViewEvents {
        void onTyping(EditText editText, boolean typingStarted);

        void onFocus(EditText editText, boolean hasFocus);

        void onClick(EditText editText);

        void onAttachmentButtonClick();

        void onSendButtonClicked(EditText editText);

        void onRecordButtonClicked();
    }

}
