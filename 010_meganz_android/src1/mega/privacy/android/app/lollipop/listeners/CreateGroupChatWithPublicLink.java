package mega.privacy.android.app.lollipop.listeners;


import android.content.Context;
import android.content.Intent;

import mega.privacy.android.app.lollipop.FileExplorerActivityLollipop;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.megachat.ChatActivityLollipop;
import mega.privacy.android.app.lollipop.megachat.ChatExplorerActivity;
import mega.privacy.android.app.lollipop.megachat.GroupChatInfoActivityLollipop;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaChatApiJava;
import nz.mega.sdk.MegaChatError;
import nz.mega.sdk.MegaChatRequest;
import nz.mega.sdk.MegaChatRequestListenerInterface;

public class CreateGroupChatWithPublicLink implements MegaChatRequestListenerInterface {

    Context context;
    String title;

    public CreateGroupChatWithPublicLink(Context context, String title) {
        this.context = context;
        this.title = title;
    }

    @Override
    public void onRequestStart(MegaChatApiJava api, MegaChatRequest request) {

    }

    @Override
    public void onRequestUpdate(MegaChatApiJava api, MegaChatRequest request) {

    }

    @Override
    public void onRequestFinish(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {
        log("onRequestFinish: "+e.getErrorCode()+"_"+request.getRequestString());

        if(request.getType() == MegaChatRequest.TYPE_CREATE_CHATROOM) {
            if (e.getErrorCode() == MegaChatError.ERROR_OK) {
                log("Chat created - get link");
                api.createChatLink(request.getChatHandle(), this);
            }
            else{
                if(context instanceof ManagerActivityLollipop){
                    ((ManagerActivityLollipop) context).onRequestFinishCreateChat(e.getErrorCode(), request.getChatHandle());
                }
                else if(context instanceof FileExplorerActivityLollipop){
                    ((FileExplorerActivityLollipop) context).onRequestFinishCreateChat(e.getErrorCode(), request.getChatHandle(), false);
                }
                else if(context instanceof ChatExplorerActivity){
                    ((ChatExplorerActivity) context).onRequestFinishCreateChat(e.getErrorCode(), request.getChatHandle(), false);
                }
                else if(context instanceof GroupChatInfoActivityLollipop){
                    ((GroupChatInfoActivityLollipop) context).onRequestFinishCreateChat(e.getErrorCode(), request.getChatHandle(), false);
                }
            }
        }
        else if (request.getType() == MegaChatRequest.TYPE_CHAT_LINK_HANDLE) {
            log("MegaChatRequest.TYPE_CHAT_LINK_HANDLE finished!!!");
            if (request.getFlag() == false) {
               if (request.getNumRetry() == 1) {
                   log("Chat link exported!");

                   if(context instanceof ManagerActivityLollipop){
                       Intent intent = new Intent(context, ChatActivityLollipop.class);
                       intent.setAction(Constants.ACTION_CHAT_SHOW_MESSAGES);
                       intent.putExtra("CHAT_ID", request.getChatHandle());
                       intent.putExtra("PUBLIC_LINK", e.getErrorCode());
                       intent.putExtra("CHAT_LINK", request.getText());
                       context.startActivity(intent);
                   }
                   else if(context instanceof FileExplorerActivityLollipop){
                       ((FileExplorerActivityLollipop) context).onRequestFinishCreateChat(e.getErrorCode(), request.getChatHandle(), true);
                   }
                   else if(context instanceof ChatExplorerActivity){
                       ((ChatExplorerActivity) context).onRequestFinishCreateChat(e.getErrorCode(), request.getChatHandle(), true);
                   }
                   else if(context instanceof GroupChatInfoActivityLollipop){
                       ((GroupChatInfoActivityLollipop) context).onRequestFinishCreateChat(e.getErrorCode(), request.getChatHandle(), true);
                   }
                }
            }
        }
    }

    @Override
    public void onRequestTemporaryError(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {

    }

    private static void log(String log) {
        Util.log("CreateGroupChatWithPublicLink", log);
    }
}
