package mega.privacy.android.app.modalbottomsheet.chatmodalbottomsheet;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.R;
import mega.privacy.android.app.lollipop.megachat.AndroidMegaChatMessage;
import mega.privacy.android.app.lollipop.megachat.ChatActivityLollipop;
import mega.privacy.android.app.modalbottomsheet.UtilsModalBottomSheet;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatMessage;
import nz.mega.sdk.MegaChatRoom;
import nz.mega.sdk.MegaHandleList;
import nz.mega.sdk.MegaNodeList;

public class MessageNotSentBottomSheetDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    Context context;

    private BottomSheetBehavior mBehavior;
    private LinearLayout items_layout;

    public LinearLayout mainLinearLayout;
    public TextView titleSlidingPanel;

    public LinearLayout optionRetryLayout;
    public LinearLayout optionDeleteLayout;
    ////

    DisplayMetrics outMetrics;

    MegaChatApiAndroid megaChatApi;
    MegaChatRoom selectedChat = null;

    AndroidMegaChatMessage selectedMessage = null;
    MegaChatMessage originalMsg = null;

    long chatId;
    long messageId;

    private int heightDisplay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (megaChatApi == null){
            megaChatApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaChatApi();
        }

        if(savedInstanceState!=null) {
            log("Bundle is NOT NULL");
            chatId = savedInstanceState.getLong("chatId", -1);
            log("Handle of the chat: "+chatId);
            messageId = savedInstanceState.getLong("messageId", -1);
            log("Row of the MS message: "+messageId);
            MegaChatMessage messageMega = megaChatApi.getManualSendingMessage(chatId, messageId);
            if(messageMega!=null){
                selectedMessage = new AndroidMegaChatMessage(messageMega);
            }
            selectedChat = megaChatApi.getChatRoom(chatId);
        }
        else{
            log("Bundle NULL");

            chatId = ((ChatActivityLollipop) context).idChat;
            log("Handle of the chat: "+chatId);
            messageId = ((ChatActivityLollipop) context).selectedMessageId;
            MegaChatMessage messageMega = megaChatApi.getManualSendingMessage(chatId, messageId);
            log("Row of the MS message: "+messageId);
            if(messageMega!=null){
                selectedMessage = new AndroidMegaChatMessage(messageMega);
            }
            selectedChat = megaChatApi.getChatRoom(chatId);
        }

        if(selectedMessage!=null){
            log("selectedMessage content: "+selectedMessage.getMessage().getContent());
            log("Temporal id of MS message: "+selectedMessage.getMessage().getTempId());
        }
        else{
            log("Error the selectedMessage is NULL");
            return;
        }
    }
    @Override
    public void setupDialog(final Dialog dialog, int style) {

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        heightDisplay = outMetrics.heightPixels;

        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.msg_not_sent_bottom_sheet, null);

        mainLinearLayout = (LinearLayout) contentView.findViewById(R.id.msg_not_sent_bottom_sheet);
        items_layout = (LinearLayout) contentView.findViewById(R.id.items_layout);

        titleSlidingPanel = (TextView)  contentView.findViewById(R.id.msg_not_sent_title_text);
        optionRetryLayout = (LinearLayout) contentView.findViewById(R.id.msg_not_sent_retry_layout);
        optionDeleteLayout = (LinearLayout) contentView.findViewById(R.id.msg_not_sent_delete_layout);
        optionDeleteLayout.setOnClickListener(this);

        titleSlidingPanel.setText(getString(R.string.title_message_not_sent_options));

        LinearLayout separator = (LinearLayout) contentView.findViewById(R.id.separator);

        if(selectedMessage!=null&&selectedChat!=null){
            if(selectedMessage.getMessage().isEdited()){
                log("Message edited : final id: "+selectedMessage.getMessage().getMsgId()+" temp id: "+selectedMessage.getMessage().getTempId());
                originalMsg = megaChatApi.getMessage(selectedChat.getChatId(), selectedMessage.getMessage().getTempId());
                if(originalMsg!=null){
                    if(originalMsg.isEditable()){
                        if((selectedChat.getOwnPrivilege()==MegaChatRoom.PRIV_STANDARD)||(selectedChat.getOwnPrivilege()==MegaChatRoom.PRIV_MODERATOR)){
                            optionRetryLayout.setVisibility(View.VISIBLE);
                            optionRetryLayout.setOnClickListener(this);
                            separator.setVisibility(View.VISIBLE);

                        }
                        else{
                            optionRetryLayout.setVisibility(View.GONE);
                            separator.setVisibility(View.GONE);
                        }
                    }
                    else{
                        optionRetryLayout.setVisibility(View.GONE);
                        separator.setVisibility(View.GONE);
                    }
                }
                else{
                    log("Null recovering the original msg");
                    optionRetryLayout.setVisibility(View.GONE);
                    separator.setVisibility(View.GONE);
                }
            }
            else{
                if((selectedChat.getOwnPrivilege()==MegaChatRoom.PRIV_STANDARD)||(selectedChat.getOwnPrivilege()==MegaChatRoom.PRIV_MODERATOR)){
                    optionRetryLayout.setVisibility(View.VISIBLE);
                    optionRetryLayout.setOnClickListener(this);
                    separator.setVisibility(View.VISIBLE);
                }
                else{
                    optionRetryLayout.setVisibility(View.GONE);
                    separator.setVisibility(View.GONE);
                }
            }
        }

        dialog.setContentView(contentView);

        mBehavior = BottomSheetBehavior.from((View) mainLinearLayout.getParent());
//        mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//
//        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            mBehavior.setPeekHeight((heightDisplay / 4) * 2);
//        }
//        else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
//            mBehavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);
//        }
        mBehavior.setPeekHeight(UtilsModalBottomSheet.getPeekHeight(items_layout, heightDisplay, context, 48));
        mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onClick(View v) {
        log("onClick");

        switch(v.getId()){

            case R.id.msg_not_sent_retry_layout: {
                log("retry option click");
                if(selectedMessage!=null&&selectedChat!=null){
                    if(selectedMessage.getMessage().getType()==MegaChatMessage.TYPE_NODE_ATTACHMENT){

                        MegaNodeList nodeList = selectedMessage.getMessage().getMegaNodeList();
                        if(nodeList!=null){
                            long nodeHandle = nodeList.get(0).getHandle();

                            ((ChatActivityLollipop) context).removeMsgNotSent();
                            megaChatApi.removeUnsentMessage(selectedChat.getChatId(), selectedMessage.getMessage().getRowId());

                            ((ChatActivityLollipop) context).retryNodeAttachment(nodeHandle);
                        }
                        else{
                            log("Error the nodeList cannot be recovered");
                        }
                    }
                    else if(selectedMessage.getMessage().getType()==MegaChatMessage.TYPE_CONTACT_ATTACHMENT){
                        long userCount  = selectedMessage.getMessage().getUsersCount();

                        MegaHandleList handleList = MegaHandleList.createInstance();

                        for(int i=0; i<userCount;i++){
                            long handle = selectedMessage.getMessage().getUserHandle(i);
                            handleList.addMegaHandle(handle);
                        }

                        ((ChatActivityLollipop) context).removeMsgNotSent();
                        megaChatApi.removeUnsentMessage(selectedChat.getChatId(), selectedMessage.getMessage().getRowId());

                        ((ChatActivityLollipop) context).retryContactAttachment(handleList);
                    }
                    else{
                        ((ChatActivityLollipop) context).removeMsgNotSent();
                        megaChatApi.removeUnsentMessage(selectedChat.getChatId(), selectedMessage.getMessage().getRowId());

                        if(selectedMessage.getMessage().isEdited()){
                            log("Message is edited --> edit");
                            if(originalMsg!=null){
                                ((ChatActivityLollipop) context).editMessageMS(selectedMessage.getMessage().getContent(), originalMsg);
                            }
                        }
                        else{
                            log("Message NOT edited --> send");
                            ((ChatActivityLollipop) context).sendMessage(selectedMessage.getMessage().getContent());
                        }
                    }
                }
                else{
                    log("onClick: Chat or message are NULL");
                }

                break;
            }

            case R.id.msg_not_sent_delete_layout: {
                log("delete option click");
                if(selectedMessage!=null&&selectedChat!=null){
                    ((ChatActivityLollipop) context).removeMsgNotSent();
                    megaChatApi.removeUnsentMessage(selectedChat.getChatId(), selectedMessage.getMessage().getRowId());
                }
                else{
                    log("onClick: Chat or message are NULL");
                }

                break;
            }
        }
//        dismiss();
        mBehavior = BottomSheetBehavior.from((View) mainLinearLayout.getParent());
        mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        log("onSaveInstanceState");
        super.onSaveInstanceState(outState);

        outState.putLong("chatId", chatId);
        outState.putLong("messageId", messageId);
    }

    private static void log(String log) {
        Util.log("MessageNotSentBottomSheetDialogFragment", log);
    }
}
