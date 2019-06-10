package mega.privacy.android.app.lollipop.listeners;

import android.content.Context;

import java.util.ArrayList;

import mega.privacy.android.app.R;
import mega.privacy.android.app.lollipop.controllers.ChatController;
import mega.privacy.android.app.lollipop.megachat.ChatActivityLollipop;
import mega.privacy.android.app.lollipop.megachat.NodeAttachmentHistoryActivity;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaChatMessage;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;

public class ChatImportToForwardListener implements MegaRequestListenerInterface {

    Context context;

    public ChatImportToForwardListener(int action, ArrayList<MegaChatMessage> messagesSelected, int counter, Context context, ChatController chatC, long chatId) {
        super();
        this.actionListener = action;
        this.context = context;
        this.counter = counter;
        this.messagesSelected = messagesSelected;
        this.chatC = chatC;
        this.chatId = chatId;
    }

    int counter = 0;
    int error = 0;
    int actionListener = -1;
    String message;
    long chatId;
    ArrayList<MegaChatMessage> messagesSelected;
    ChatController chatC;

    @Override
    public void onRequestUpdate(MegaApiJava api, MegaRequest request) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onRequestTemporaryError(MegaApiJava api, MegaRequest request, MegaError e) {

        log("Counter on onRequestTemporaryError: "+counter);
//			MegaNode node = megaApi.getNodeByHandle(request.getNodeHandle());
//			if(node!=null){
//				log("onRequestTemporaryError: "+node.getName());
//			}
    }

    @Override
    public void onRequestStart(MegaApiJava api, MegaRequest request) {

    }

    @Override
    public void onRequestFinish(MegaApiJava api, MegaRequest request, MegaError e) {
        log("onRequestFinish: "+e.getErrorCode());
        counter--;
        if (e.getErrorCode() != MegaError.API_OK){
            error++;
        }
        int requestType = request.getType();
        log("Counter on RequestFinish: "+counter);
        log("Error on RequestFinish: "+error);
//			MegaNode node = megaApi.getNodeByHandle(request.getNodeHandle());
//			if(node!=null){
//				log("onRequestTemporaryError: "+node.getName());
//			}
        if(counter==0){
            switch (requestType) {

                case MegaRequest.TYPE_COPY:{
                    if(actionListener==Constants.MULTIPLE_FORWARD_MESSAGES){
                        //Many files shared with one contacts
                        if(error>0){
                            message = context.getResources().getQuantityString(R.plurals.error_forwarding_messages, error);
                            if(context instanceof ChatActivityLollipop){
                                ((ChatActivityLollipop) context).removeProgressDialog();
                                ((ChatActivityLollipop) context).showSnackbar(Constants.SNACKBAR_TYPE, message, -1);
                            }
                            else if(context instanceof NodeAttachmentHistoryActivity){
                                ((NodeAttachmentHistoryActivity) context).removeProgressDialog();
                                ((NodeAttachmentHistoryActivity) context).showSnackbar(Constants.SNACKBAR_TYPE, message);
                            }
                        }
                        else{
                            chatC.forwardMessages(messagesSelected, chatId);
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }

    private static void log(String log) {
        Util.log("ChatImportToForwardListener", log);
    }
}
