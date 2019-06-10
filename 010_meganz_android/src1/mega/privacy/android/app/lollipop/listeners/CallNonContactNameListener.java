package mega.privacy.android.app.lollipop.listeners;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;

import java.io.File;
import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.lollipop.megachat.calls.ChatCallActivity;
import mega.privacy.android.app.lollipop.megachat.chatAdapters.GroupCallAdapter;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaChatApiJava;
import nz.mega.sdk.MegaChatError;
import nz.mega.sdk.MegaChatRequest;
import nz.mega.sdk.MegaChatRequestListenerInterface;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;

public class CallNonContactNameListener implements MegaChatRequestListenerInterface, MegaRequestListenerInterface {

    Context context;
    DatabaseHandler dbH;
    String mail;
    long peerId;
    boolean isAvatar;
    String name;
    int position;
    RecyclerView.ViewHolder holder;
    RecyclerView.Adapter adapter;

    public CallNonContactNameListener(Context context, long peerId, boolean isAvatar, String name) {
        log("CallNonContactNameListener");

        this.context = context;
        this.peerId = peerId;
        this.isAvatar = isAvatar;
        this.name = name;
        this.holder = null;
        this.adapter = null;
        this.position = -1;
        dbH = DatabaseHandler.getDbHandler(context);
    }

    public CallNonContactNameListener(Context context, RecyclerView.ViewHolder holder, RecyclerView.Adapter adapter, long peerId, String fullName, int position) {
        log("CallNonContactNameListener");

        this.context = context;
        this.peerId = peerId;
        this.name = fullName;
        this.isAvatar = false;
        dbH = DatabaseHandler.getDbHandler(context);
        if(adapter instanceof GroupCallAdapter){
            this.holder = (GroupCallAdapter.ViewHolderGroupCall) holder;
            this.adapter = (GroupCallAdapter) adapter;
            this.position = position;
        }else{
            this.holder = null;
            this.adapter = null;
            this.position = -1;
        }
    }


    @Override
    public void onRequestStart(MegaChatApiJava api, MegaChatRequest request) { }

    @Override
    public void onRequestUpdate(MegaChatApiJava api, MegaChatRequest request) { }

    @Override
    public void onRequestFinish(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {
        if(request.getType()==MegaChatRequest.TYPE_GET_EMAIL){
            if (e.getErrorCode() == MegaError.API_OK){
                log("onRequestFinish:TYPE_GET_EMAIL OK");

                mail = request.getText();
                if(mail!=null){
                    if(!mail.trim().isEmpty()){
                        dbH.setNonContactEmail(mail, request.getUserHandle()+"");
                    }
                }
                if((holder!=null)&&(holder instanceof GroupCallAdapter.ViewHolderGroupCall)){
                    log("onRequestFinish:GroupCallAdapter:setProfile");
                    ((GroupCallAdapter) adapter).setProfile(peerId, name, mail, (GroupCallAdapter.ViewHolderGroupCall)holder, position);
                }else{
                    if(isAvatar){
                        log("onRequestFinish:PeerSelected:setProfilePeerSelected");
                        ((ChatCallActivity)context).setProfilePeerSelected(peerId, name, mail);
                    }else{
                        log("onRequestFinish:GetName:updateNonContactName");
                        ((ChatCallActivity)context).updateNonContactName(peerId, mail);
                    }
                }
            }else{
                log("ERROR: TYPE_GET_EMAIL: "+request.getRequestString());
            }
        }

        }

    @Override
    public void onRequestTemporaryError(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {}



    @Override
    public void onRequestStart(MegaApiJava api, MegaRequest request) {
        log("onRequestStart()");
    }

    @Override
    public void onRequestFinish(MegaApiJava api, MegaRequest request, MegaError e) {
        log("onRequestFinish(): "+e.getErrorCode());
        if(request.getType() == MegaRequest.TYPE_GET_ATTR_USER) {

            if (e.getErrorCode() != MegaError.API_OK) {
                log("onRequestFinish:TYPE_GET_ATTR_USER: OK");
                if((holder!=null) && (holder instanceof GroupCallAdapter.ViewHolderGroupCall)){
                    File avatar = null;
                    Bitmap bitmap = null;

                    if (context.getExternalCacheDir() != null){
                        avatar = new File(context.getExternalCacheDir().getAbsolutePath(), request.getEmail() + ".jpg");
                    }else{
                        avatar = new File(context.getCacheDir().getAbsolutePath(), request.getEmail() + ".jpg");
                    }
                    if (avatar.exists()){
                        if (avatar.length() > 0){
                            BitmapFactory.Options bOpts = new BitmapFactory.Options();
                            bOpts.inPurgeable = true;
                            bOpts.inInputShareable = true;
                            bitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(), bOpts);
                            if (bitmap == null) {
                                avatar.delete();
                            }else{
                                log("onRequestFinish:GroupCallAdapter:setImageView ");
                                ((GroupCallAdapter.ViewHolderGroupCall) holder).setImageView(bitmap);
                            }
                        }
                    }
                }


            }
        }
    }

    @Override
    public void onRequestTemporaryError(MegaApiJava api,MegaRequest request, MegaError e) { }

    @Override
    public void onRequestUpdate(MegaApiJava api, MegaRequest request) { }

    private static void log(String log) {
        Util.log("CallNonContactNameListener", log);
    }
}