package mega.privacy.android.app.lollipop.listeners;

import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;

import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.R;
import mega.privacy.android.app.lollipop.AudioVideoPlayerLollipop;
import mega.privacy.android.app.lollipop.ContactInfoActivityLollipop;
import mega.privacy.android.app.lollipop.FileExplorerActivityLollipop;
import mega.privacy.android.app.lollipop.FileInfoActivityLollipop;
import mega.privacy.android.app.lollipop.FullScreenImageViewerLollipop;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.PdfViewerActivityLollipop;
import mega.privacy.android.app.lollipop.megachat.ChatActivityLollipop;
import mega.privacy.android.app.lollipop.megachat.NodeAttachmentHistoryActivity;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatApiJava;
import nz.mega.sdk.MegaChatError;
import nz.mega.sdk.MegaChatRequest;
import nz.mega.sdk.MegaChatRequestListenerInterface;
import nz.mega.sdk.MegaChatRoom;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaUser;

public class CreateChatToPerformActionListener implements MegaChatRequestListenerInterface {

    public static int SEND_FILE=1;
    public static int START_AUDIO_CALL=2;
    public static int START_VIDEO_CALL=3;

    public static int SEND_FILES = 4;
    public static int SEND_CONTACTS = 5;
    public static int SEND_MESSAGES = 6;
    public static int SEND_FILE_EXPLORER_CONTENT = 7;

    Context context;
    int counter = 0;
    int error = 0;
    String message;
    ArrayList<MegaChatRoom> chats = null;
    ArrayList<MegaUser> usersNoChat = null;
    long fileHandle;
    int action = -1;

    long[] handles;
    int totalCounter;
    long idChat;

    MegaChatApiAndroid megaChatApi;

    public CreateChatToPerformActionListener(ArrayList<MegaChatRoom> chats, ArrayList<MegaUser> usersNoChat, long fileHandle, Context context, int action) {
        super();
        this.context = context;
        this.counter = usersNoChat.size();
        this.chats = chats;
        this.usersNoChat = usersNoChat;
        this.fileHandle = fileHandle;
        this.action = action;

        if (megaChatApi == null) {
            megaChatApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaChatApi();
        }
    }

    public CreateChatToPerformActionListener(ArrayList<MegaChatRoom> chats, ArrayList<MegaUser> usersNoChat, long[] handles, Context context, int action, long idChat) {
        super();
        this.context = context;
        this.counter = usersNoChat.size();
        if (chats != null && !chats.isEmpty()) {
            this.totalCounter = usersNoChat.size() + chats.size();
        }
        else {
            this.totalCounter = this.counter;
        }
        this.chats = chats;
        this.usersNoChat = usersNoChat;
        this.handles = handles;
        this.action = action;
        this.idChat = idChat;

        if (megaChatApi == null) {
            megaChatApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaChatApi();
        }
    }

    private static void log(String log) {
        Util.log("CreateChatToPerformActionListener", log);
    }

    @Override
    public void onRequestStart(MegaChatApiJava api, MegaChatRequest request) {

    }

    @Override
    public void onRequestUpdate(MegaChatApiJava api, MegaChatRequest request) {

    }

    @Override
    public void onRequestFinish(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {
        log("onRequestFinish: "+e.getErrorCode());

        if(request.getType() == MegaChatRequest.TYPE_CREATE_CHATROOM){
            if(action==SEND_FILE){
                log("Action: SEND_FILE");
                counter--;
                log("Counter after decrease: "+counter);
                if (e.getErrorCode() != MegaError.API_OK){
                    error++;
                    log("ERROR creating chat");
                }
                else{
                    if(chats==null){
                        chats = new ArrayList<MegaChatRoom>();
                    }
                    MegaChatRoom chat = megaChatApi.getChatRoom(request.getChatHandle());
                    if(chat!=null){
                        chats.add(chat);
                    }
                }

                if(counter==0){
                    log("Counter is 0 - all requests processed");
                    if((usersNoChat.size() == error) && (chats==null || chats.isEmpty())){
                        //All send files fail
                        message = context.getResources().getString(R.string.number_no_sent, error);
                        if(context instanceof ManagerActivityLollipop){
                            ((ManagerActivityLollipop) context).showSnackbar(Constants.SNACKBAR_TYPE, message, -1);
                        }
                        else if(context instanceof ContactInfoActivityLollipop){
                            ((ContactInfoActivityLollipop) context).showSnackbar(Constants.SNACKBAR_TYPE, message, -1);
                        }

                        message = context.getResources().getQuantityString(R.plurals.num_files_not_send, handles.length, totalCounter);
                        if (context instanceof FullScreenImageViewerLollipop) {
                            ((FullScreenImageViewerLollipop) context).showSnackbar(Constants.SNACKBAR_TYPE, message, -1);
                        }
                        else if (context instanceof AudioVideoPlayerLollipop) {
                            ((AudioVideoPlayerLollipop) context).showSnackbar(Constants.SNACKBAR_TYPE, message, -1);
                        }
                        else if (context instanceof PdfViewerActivityLollipop) {
                            ((PdfViewerActivityLollipop) context).showSnackbar(Constants.SNACKBAR_TYPE, message, -1);
                        }
                        else if (context instanceof FileInfoActivityLollipop) {
                            ((FileInfoActivityLollipop) context).showSnackbar(Constants.SNACKBAR_TYPE, message, -1);
                        }
                    }
                    else {
                        if(context instanceof ManagerActivityLollipop){
                            ((ManagerActivityLollipop) context).sendFileToChatsFromContacts(context, chats, fileHandle);
                        }
                        else if(context instanceof ContactInfoActivityLollipop){
                            ((ContactInfoActivityLollipop) context).sendFileToChatsFromContacts(context, chats, fileHandle);
                        }
                        else if (context instanceof FullScreenImageViewerLollipop) {
                            ((FullScreenImageViewerLollipop) context).sendFileToChatsFromContacts(context, chats, fileHandle);
                        }
                        else if (context instanceof AudioVideoPlayerLollipop) {
                            ((AudioVideoPlayerLollipop) context).sendFileToChatsFromContacts(context, chats, fileHandle);
                        }
                        else if (context instanceof PdfViewerActivityLollipop) {
                            ((PdfViewerActivityLollipop) context).sendFileToChatsFromContacts(context, chats, fileHandle);
                        }
                        else if (context instanceof FileInfoActivityLollipop) {
                            ((FileInfoActivityLollipop) context).sendFileToChatsFromContacts(context, chats, fileHandle);
                        }
                    }
                }
            }
            else if(action==START_AUDIO_CALL){
                log("Action: START_AUDIO_CALL");
                if(context instanceof ContactInfoActivityLollipop){
                    if (e.getErrorCode() != MegaError.API_OK){
                        ((ContactInfoActivityLollipop) context).showSnackbar(Constants.SNACKBAR_TYPE, context.getString(R.string.create_chat_error), -1);
                    }
                    else{
                        MegaChatRoom chat = megaChatApi.getChatRoom(request.getChatHandle());
                        if(chat!=null){
                            megaChatApi.startChatCall(chat.getChatId(), false, (ContactInfoActivityLollipop) context);
                        }
                        else{
                            log("Chatroom not recovered");
                        }
                    }
                }
            }
            else if(action==START_VIDEO_CALL){
                log("Action: START_VIDEO_CALL");
                if(context instanceof ContactInfoActivityLollipop){
                    if (e.getErrorCode() != MegaError.API_OK){
                        ((ContactInfoActivityLollipop) context).showSnackbar(Constants.SNACKBAR_TYPE, context.getString(R.string.create_chat_error), -1);
                    }
                    else{
                        MegaChatRoom chat = megaChatApi.getChatRoom(request.getChatHandle());
                        if(chat!=null){
                            megaChatApi.startChatCall(chat.getChatId(), true, (ContactInfoActivityLollipop) context);
                        }
                        else{
                            log("Chatroom not recovered");
                        }
                    }
                }
            }
            else if (action == SEND_FILES) {
                counter--;
                log("Counter after decrease: "+counter);
                if (e.getErrorCode() != MegaError.API_OK){
                    error++;
                    log("ERROR creating chat");
                }
                else{
                    if(chats==null){
                        chats = new ArrayList<MegaChatRoom>();
                    }
                    MegaChatRoom chat = megaChatApi.getChatRoom(request.getChatHandle());
                    if(chat!=null){
                        chats.add(chat);
                    }
                }

                if(counter==0){
                    log("Counter is 0 - all requests processed");
                    if((usersNoChat.size() == error) && (chats==null || chats.isEmpty())){
                        //All send files fail; Show error
                        message = context.getResources().getQuantityString(R.plurals.num_files_not_send, handles.length, totalCounter);
                        if(context instanceof ManagerActivityLollipop) {
                            ((ManagerActivityLollipop) context).showSnackbar(Constants.SNACKBAR_TYPE, message, -1);
                        }
                    }
                    else {
//                        Send files
                        if(context instanceof ManagerActivityLollipop){
                            ((ManagerActivityLollipop) context).sendFilesToChats(chats, null, handles);
                        }
                    }
                }
            }
            else if (action == SEND_CONTACTS) {
                counter--;
                log("Counter after decrease: "+counter);
                if (e.getErrorCode() != MegaError.API_OK){
                    error++;
                    log("ERROR creating chat");
                }
                else{
                    if(chats==null){
                        chats = new ArrayList<MegaChatRoom>();
                    }
                    MegaChatRoom chat = megaChatApi.getChatRoom(request.getChatHandle());
                    if(chat!=null){
                        chats.add(chat);
                    }
                }

                if(counter==0){
                    log("Counter is 0 - all requests processed");
                    if((usersNoChat.size() == error) && (chats==null || chats.isEmpty())){
                        //All send contacts fail; Show error
                        message = context.getResources().getQuantityString(R.plurals.num_contacts_not_send, handles.length, totalCounter);
                        if(context instanceof ManagerActivityLollipop){
                            ((ManagerActivityLollipop) context).showSnackbar(Constants.SNACKBAR_TYPE, message, -1);
                        }
                    }
                    else {
//                        Send contacts
                        if(context instanceof ManagerActivityLollipop){
                            ((ManagerActivityLollipop) context).sendContactsToChats(chats, null, handles);
                        }
                    }
                }
            }
            else if (action == SEND_MESSAGES) {
                counter--;
                log("Counter after decrease: "+counter);
                if (e.getErrorCode() != MegaError.API_OK){
                    error++;
                    log("ERROR creating chat");
                }
                else{
                    if(chats==null){
                        chats = new ArrayList<MegaChatRoom>();
                    }
                    MegaChatRoom chat = megaChatApi.getChatRoom(request.getChatHandle());
                    if(chat!=null){
                        chats.add(chat);
                    }
                }

                if(counter==0){
                    log("Counter is 0 - all requests processed");
                    if((usersNoChat.size() == error) && (chats==null || chats.isEmpty())){
                        //All send messages fail; Show error
                        message = context.getResources().getQuantityString(R.plurals.num_messages_not_send, handles.length, totalCounter);
                        if (context instanceof ChatActivityLollipop) {
                            ((ChatActivityLollipop) context).showSnackbar(Constants.SNACKBAR_TYPE, message, -1);
                        }
                        else if (context instanceof NodeAttachmentHistoryActivity) {
                            ((NodeAttachmentHistoryActivity) context).showSnackbar(Constants.SNACKBAR_TYPE, message);
                        }
                    }
                    else {
//                        Send messages
                        long[] chatHandles = new long[chats.size()];
                        for (int i=0; i<chats.size(); i++) {
                            chatHandles[i] = chats.get(i).getChatId();
                        }
                        MultipleForwardChatProcessor forwardChatProcessor = new MultipleForwardChatProcessor(context, chatHandles, handles, idChat);
                        forwardChatProcessor.forward(megaChatApi.getChatRoom(idChat));
                    }
                }
            }
            else if (action == SEND_FILE_EXPLORER_CONTENT) {
                counter--;
                log("Counter after decrease: "+counter);
                if (e.getErrorCode() != MegaError.API_OK){
                    error++;
                    log("ERROR creating chat");
                }
                else{
                    if(chats==null){
                        chats = new ArrayList<MegaChatRoom>();
                    }
                    MegaChatRoom chat = megaChatApi.getChatRoom(request.getChatHandle());
                    if(chat!=null){
                        chats.add(chat);
                    }
                }

                if(counter==0){
                    log("Counter is 0 - all requests processed");
                    if((usersNoChat.size() == error) && (chats==null || chats.isEmpty())){
                        //All send messages fail; Show error
                        message = context.getResources().getString(R.string.content_not_send, totalCounter);
                        if (context instanceof FileExplorerActivityLollipop) {
                            ((FileExplorerActivityLollipop) context).showSnackbar(message);
                        }
                    }
                    else {
//                        Send content
                        if (context instanceof FileExplorerActivityLollipop) {
                            ((FileExplorerActivityLollipop) context).sendToChats(chats);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onRequestTemporaryError(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {

    }
}
