package mega.privacy.android.app.lollipop.listeners;

import android.app.Activity;
import android.content.Context;

import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.R;
import mega.privacy.android.app.lollipop.controllers.ChatController;
import mega.privacy.android.app.lollipop.megachat.AndroidMegaChatMessage;
import mega.privacy.android.app.lollipop.megachat.ChatActivityLollipop;
import mega.privacy.android.app.lollipop.megachat.NodeAttachmentHistoryActivity;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatApiJava;
import nz.mega.sdk.MegaChatContainsMeta;
import nz.mega.sdk.MegaChatError;
import nz.mega.sdk.MegaChatMessage;
import nz.mega.sdk.MegaChatRequest;
import nz.mega.sdk.MegaChatRequestListenerInterface;
import nz.mega.sdk.MegaChatRoom;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaNodeList;


//Listener for  multi forward
public class MultipleForwardChatProcessor implements MegaChatRequestListenerInterface {

    Context context;

    long[] chatHandles;
    long[] idMessages;
    long idChat;

    MegaApiAndroid megaApi;
    MegaChatApiAndroid megaChatApi;

    ChatController cC;

    public MultipleForwardChatProcessor(Context context, long[] chatHandles, long[] idMessages, long idChat) {
        super();
        this.context = context;

        this.idMessages = idMessages;
        this.chatHandles = chatHandles;
        this.idChat = idChat;

        if (megaApi == null) {
            megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
        }

        if (megaChatApi == null) {
            megaChatApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaChatApi();
        }

        cC = new ChatController(context);
    }

    int counter = 0;
    int error = 0;
    int errorNotAvailable = 0;
    int totalMessages = 0;

    private static void log(String log) {
        Util.log("MultipleForwardChatProcessor", log);
    }

    public void forward(MegaChatRoom chatRoom){

        if(chatHandles.length==1){
            log("Forward to one chat");
            for(int i=0;i<idMessages.length;i++){
                MegaChatMessage messageToForward = megaChatApi.getMessage(idChat, idMessages[i]);
                if(messageToForward!=null){
                    int type = messageToForward.getType();
                    log("Type of message to forward: "+type);
                    switch(type){
                        case MegaChatMessage.TYPE_NORMAL:{
                            String text = messageToForward.getContent();

                            if(chatHandles[0]==idChat){
                                ((ChatActivityLollipop) context).sendMessage(text);
                            }
                            else{
                                megaChatApi.sendMessage(chatHandles[0], text);
                            }
                            checkTotalMessages();
                            break;
                        }
                        case MegaChatMessage.TYPE_CONTACT_ATTACHMENT:{
                            MegaChatMessage contactMessage = megaChatApi.forwardContact(idChat, messageToForward.getMsgId(),chatHandles[0]);
                            if(chatHandles[0]==idChat){
                                if(contactMessage!=null){
                                    AndroidMegaChatMessage androidMsgSent = new AndroidMegaChatMessage(contactMessage);
                                    ((ChatActivityLollipop) context).sendMessageToUI(androidMsgSent);
                                }
                            }
                            checkTotalMessages();
                            break;
                        }
                        case MegaChatMessage.TYPE_NODE_ATTACHMENT:{

                            if(messageToForward.getUserHandle()!=megaChatApi.getMyUserHandle()){
                                MegaNodeList nodeList = messageToForward.getMegaNodeList();
                                if(nodeList != null) {
                                    for (int j = 0; j < nodeList.size(); j++) {
                                        MegaNode temp = nodeList.get(j);
                                        String name = temp.getName();
                                        MegaNode chatFolder = megaApi.getNodeByPath(Constants.CHAT_FOLDER, megaApi.getRootNode());
                                        if(chatFolder==null){
                                            log("Error no chat folder - return");
                                            return;
                                        }
                                        MegaNode nodeToAttach = megaApi.getNodeByPath(name, chatFolder);
                                        if(nodeToAttach!=null){
                                            nodeToAttach = cC.authorizeNodeIfPreview(nodeToAttach, chatRoom);
                                            if(chatHandles[0]==idChat){
                                                megaChatApi.attachNode(chatHandles[0], nodeToAttach.getHandle(), this);
                                            }
                                            else{
                                                megaChatApi.attachNode(chatHandles[0], nodeToAttach.getHandle(), this);
                                            }
                                        }
                                        else{
                                            log("ERROR - Node to attach is NULL - one node not attached");
                                        }
                                    }
                                }
                            }
                            else{
                                MegaNodeList nodeList = messageToForward.getMegaNodeList();
                                if(nodeList != null) {
                                    for (int j = 0; j < nodeList.size(); j++) {
                                        MegaNode temp = nodeList.get(j);
                                        if(chatHandles[0]==idChat){
                                            megaChatApi.attachNode(chatHandles[0], temp.getHandle(), this);
                                        }
                                        else{
                                            megaChatApi.attachNode(chatHandles[0], temp.getHandle(), this);
                                        }
                                    }
                                }
                            }

                            break;
                        }case MegaChatMessage.TYPE_CONTAINS_META:{
                            MegaChatContainsMeta meta = messageToForward.getContainsMeta();
                            String text = "";
                            if(meta!=null && meta.getType()==MegaChatContainsMeta.CONTAINS_META_RICH_PREVIEW){
                                text = meta.getRichPreview().getText();
                            }else{
                            }
                            if(chatHandles[0]==idChat){
                                ((ChatActivityLollipop) context).sendMessage(text);
                            }
                            else{
                                megaChatApi.sendMessage(chatHandles[0], text);
                            }
                            checkTotalMessages();
                            break;
                        }
                    }
                }
                else{
                    log("ERROR -> message is null on forwarding");
                }
            }
        }
        else{
            log("Forward to many chats");
            for(int k=0;k<chatHandles.length;k++){
                for(int i=0;i<idMessages.length;i++){
                    MegaChatMessage messageToForward = megaChatApi.getMessage(idChat, idMessages[i]);
                    log("Forward: "+ idMessages[i] + " chat id: "+chatHandles[k]);
                    if(messageToForward!=null){
                        int type = messageToForward.getType();
                        log("Type of message to forward: "+type);
                        switch(type){
                            case MegaChatMessage.TYPE_NORMAL:{
                                String text = messageToForward.getContent();
                                if(chatHandles[k]==idChat){
                                    ((ChatActivityLollipop) context).sendMessage(text);
                                }
                                else{
                                    megaChatApi.sendMessage(chatHandles[k], text);
                                }
                                checkTotalMessages();
                                break;
                            }
                            case MegaChatMessage.TYPE_CONTACT_ATTACHMENT:{

                                MegaChatMessage contactMessage = megaChatApi.forwardContact(idChat, messageToForward.getMsgId(),chatHandles[k]);
                                if(chatHandles[k]==idChat){
                                    if(contactMessage!=null){
                                        AndroidMegaChatMessage androidMsgSent = new AndroidMegaChatMessage(contactMessage);
                                        ((ChatActivityLollipop) context).sendMessageToUI(androidMsgSent);
                                    }
                                }
                                checkTotalMessages();
                                break;
                            }
                            case MegaChatMessage.TYPE_NODE_ATTACHMENT:{

                                if(messageToForward.getUserHandle()!=megaChatApi.getMyUserHandle()){
                                    MegaNodeList nodeList = messageToForward.getMegaNodeList();
                                    if(nodeList != null) {
                                        for (int j = 0; j < nodeList.size(); j++) {
                                            MegaNode temp = nodeList.get(j);
                                            String name = temp.getName();
                                            MegaNode chatFolder = megaApi.getNodeByPath(Constants.CHAT_FOLDER, megaApi.getRootNode());
                                            if(chatFolder==null){
                                                log("Error no chat folder - return");
                                                return;
                                            }
                                            MegaNode nodeToAttach = megaApi.getNodeByPath(name, chatFolder);
                                            if(nodeToAttach!=null){
                                                nodeToAttach = cC.authorizeNodeIfPreview(nodeToAttach, chatRoom);
                                                if(chatHandles[k]==idChat){
                                                    megaChatApi.attachNode(chatHandles[k], nodeToAttach.getHandle(), this);
                                                }
                                                else{
                                                    megaChatApi.attachNode(chatHandles[k], nodeToAttach.getHandle(), this);
                                                }
                                            }
                                            else{
                                                log("ERROR - Node to attach is NULL - one node not attached");
                                            }
                                        }
                                    }
                                }
                                else{
                                    MegaNodeList nodeList = messageToForward.getMegaNodeList();
                                    if(nodeList != null) {
                                        for (int j = 0; j < nodeList.size(); j++) {
                                            MegaNode temp = nodeList.get(j);

                                            if(chatHandles[k]==idChat){
                                                megaChatApi.attachNode(chatHandles[k], temp.getHandle(), this);
                                            }
                                            else{
                                                megaChatApi.attachNode(chatHandles[k], temp.getHandle(), this);
                                            }
                                        }
                                    }
                                }

                                break;
                            }case MegaChatMessage.TYPE_CONTAINS_META:{
                                MegaChatContainsMeta meta = messageToForward.getContainsMeta();
                                String text = "";
                                if(meta!=null && meta.getType()==MegaChatContainsMeta.CONTAINS_META_RICH_PREVIEW){
                                    text = meta.getRichPreview().getText();
                                }else{
                                }
                                if(chatHandles[k]==idChat){
                                    ((ChatActivityLollipop) context).sendMessage(text);
                                }
                                else{
                                    megaChatApi.sendMessage(chatHandles[k], text);
                                }
                                checkTotalMessages();
                                break;
                            }
                        }
                    }
                    else{
                        log("ERROR -> message is null on forwarding");
                    }
                }
            }
        }


    }

    @Override
    public void onRequestStart(MegaChatApiJava api, MegaChatRequest request) {
        log("onRequestStart");
    }

    @Override
    public void onRequestUpdate(MegaChatApiJava api, MegaChatRequest request) {

    }

    @Override
    public void onRequestFinish(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {
        log("onRequestFinish");

        if(e.getErrorCode() == MegaError.API_ENOENT){
            errorNotAvailable++;
            log("MultipleForwardChatProcessor: "+context.getResources().getQuantityString(R.plurals.messages_forwarded_error_not_available, errorNotAvailable, errorNotAvailable)+" "+e.getErrorCode());
        }
        else if (e.getErrorCode() != MegaError.API_OK){
            error++;
            log("Attach node error: "+e.getErrorString()+"__"+e.getErrorCode());
        }

        checkTotalMessages();
    }

    @Override
    public void onRequestTemporaryError(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {
        log("Counter on onRequestTemporaryError: "+counter);
    }

    private void checkTotalMessages(){
        totalMessages++;
        log("Total messages processed: "+totalMessages);
        if(totalMessages >= chatHandles.length*idMessages.length){

            log("All messages processed");

            int success = totalMessages - error - errorNotAvailable;

            if(context instanceof ChatActivityLollipop){
                if(success>0){
                    //A message has been forwarded
                    String text = null;
                    int totalErrors = error+errorNotAvailable;
                    if(totalErrors == 0){
                        if(chatHandles.length>1) {
                            text = context.getString(R.string.messages_forwarded_success);
                        }
                    }
                    else if(totalErrors==errorNotAvailable){
                        text = context.getResources().getQuantityString(R.plurals.messages_forwarded_error_not_available, totalErrors, totalErrors);
                    }
                    else{
                        text = context.getResources().getQuantityString(R.plurals.messages_forwarded_partial_error, totalErrors, totalErrors);
                    }

                    if(chatHandles.length==1){
                        ((ChatActivityLollipop) context).openChatAfterForward(chatHandles[0], text);
                    }
                    else {
                        ((ChatActivityLollipop) context).openChatAfterForward(-1, text);
                    }
                }
                else{
                    //No messages forwarded
                    int totalErrors = error+errorNotAvailable;
                    if(totalErrors==errorNotAvailable){
                        ((ChatActivityLollipop) context).showSnackbar(Constants.SNACKBAR_TYPE, context.getResources().getQuantityString(R.plurals.messages_forwarded_error_not_available, totalErrors, totalErrors), -1);
                    }
                    else{
                        String text = context.getResources().getQuantityString(R.plurals.messages_forwarded_partial_error, totalErrors, totalErrors);
                        ((ChatActivityLollipop) context).showSnackbar(Constants.SNACKBAR_TYPE, text, -1);
                    }

                    ((ChatActivityLollipop) context).removeProgressDialog();
                }
            }
            else if(context instanceof NodeAttachmentHistoryActivity){
                if(success>0){
                    //A message has been forwarded
                    String text = null;
                    int totalErrors = error+errorNotAvailable;
                    if(totalErrors == 0){
                        text = context.getResources().getQuantityString(R.plurals.messages_forwarded_success_plural, totalMessages);
                    }
                    else if(totalErrors==errorNotAvailable){
                        text = context.getResources().getQuantityString(R.plurals.messages_forwarded_error_not_available, totalErrors, totalErrors);
                    }
                    else{
                        text = context.getResources().getQuantityString(R.plurals.messages_forwarded_partial_error, totalErrors, totalErrors);
                    }

                    ((NodeAttachmentHistoryActivity) context).showSnackbar(Constants.SNACKBAR_TYPE, text);

//                    if(chatHandles.length==1){
//                        ((NodeAttachmentHistoryActivity) context).openChatAfterForward(chatHandles[0], text);
//                    }
//                    else {
//                        ((NodeAttachmentHistoryActivity) context).openChatAfterForward(-1, text);
//                    }
                }
                else{
                    //No messages forwarded
                    int totalErrors = error+errorNotAvailable;
                    if(totalErrors==errorNotAvailable){
                        ((NodeAttachmentHistoryActivity) context).showSnackbar(Constants.SNACKBAR_TYPE, context.getResources().getQuantityString(R.plurals.messages_forwarded_error_not_available, totalErrors, totalErrors));
                    }
                    else{
                        String text = context.getResources().getQuantityString(R.plurals.messages_forwarded_partial_error, totalErrors, totalErrors);
                        ((NodeAttachmentHistoryActivity) context).showSnackbar(Constants.SNACKBAR_TYPE, text);
                    }
                }
                ((NodeAttachmentHistoryActivity) context).removeProgressDialog();
            }
        }
    }
};
