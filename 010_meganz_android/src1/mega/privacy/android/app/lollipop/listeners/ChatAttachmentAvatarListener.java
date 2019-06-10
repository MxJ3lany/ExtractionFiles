package mega.privacy.android.app.lollipop.listeners;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

import mega.privacy.android.app.lollipop.megachat.chatAdapters.MegaChatLollipopAdapter;
import mega.privacy.android.app.lollipop.megachat.chatAdapters.MegaListChatLollipopAdapter;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;

public class ChatAttachmentAvatarListener implements MegaRequestListenerInterface {

    Context context;
    MegaChatLollipopAdapter.ViewHolderMessageChat holder;
    MegaChatLollipopAdapter adapter;
    boolean myOwnMsg;

    public ChatAttachmentAvatarListener(Context context, MegaChatLollipopAdapter.ViewHolderMessageChat holder, MegaChatLollipopAdapter adapter, boolean myOwnMsg) {
        this.context = context;
        this.holder = holder;
        this.adapter = adapter;
        this.myOwnMsg = myOwnMsg;
    }

    @Override
    public void onRequestStart(MegaApiJava api, MegaRequest request) {
        log("onRequestStart()");
    }

    @Override
    public void onRequestFinish(MegaApiJava api, MegaRequest request, MegaError e) {
        log("onRequestFinish()");
        if (e.getErrorCode() == MegaError.API_OK){

            String mail = "";
            if(myOwnMsg){
                mail = (String) holder.contentOwnMessageContactEmail.getText();
            }
            else{
                mail = (String) holder.contentContactMessageContactEmail.getText();
            }

            if (mail.compareTo(request.getEmail()) == 0){
                File avatar = null;
                if (context.getExternalCacheDir() != null){
                    avatar = new File(context.getExternalCacheDir().getAbsolutePath(), mail + ".jpg");
                }
                else{
                    avatar = new File(context.getCacheDir().getAbsolutePath(), mail + ".jpg");
                }
                Bitmap bitmap = null;
                if (avatar.exists()){
                    if (avatar.length() > 0){
                        BitmapFactory.Options bOpts = new BitmapFactory.Options();
                        bOpts.inPurgeable = true;
                        bOpts.inInputShareable = true;
                        bitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(), bOpts);
                        if (bitmap == null) {
                            avatar.delete();
                        }
                        else{
                            if(myOwnMsg){
                                holder.setMyImageView(bitmap);
                            }
                            else{
                                holder.setContactImageView(bitmap);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onRequestTemporaryError(MegaApiJava api,MegaRequest request, MegaError e) {
        log("onRequestTemporaryError");
    }

    @Override
    public void onRequestUpdate(MegaApiJava api, MegaRequest request) {
        // TODO Auto-generated method stub
    }

    private static void log(String log) {
        Util.log("ChatAttachmentAvatarListener", log);
    }

}