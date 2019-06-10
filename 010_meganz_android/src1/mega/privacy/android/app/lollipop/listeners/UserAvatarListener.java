package mega.privacy.android.app.lollipop.listeners;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;

import java.io.File;

import mega.privacy.android.app.lollipop.adapters.LastContactsAdapter;
import mega.privacy.android.app.lollipop.adapters.MegaContactsLollipopAdapter;
import mega.privacy.android.app.lollipop.megachat.calls.ChatCallActivity;
import mega.privacy.android.app.utils.ThumbnailUtilsLollipop;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;

public class UserAvatarListener implements MegaRequestListenerInterface {
    
    Context context;
    MegaContactsLollipopAdapter.ViewHolderContacts holder;
    
    public UserAvatarListener(Context context,MegaContactsLollipopAdapter.ViewHolderContacts holder) {
        this.context = context;
        this.holder = holder;
    }
    
    public UserAvatarListener(Context context) {
        this.context = context;
    }
    
    @Override
    public void onRequestStart(MegaApiJava api,MegaRequest request) {
    }
    
    @Override
    public void onRequestFinish(MegaApiJava api,MegaRequest request,MegaError e) {
        if (e.getErrorCode() == MegaError.API_OK) {
            if (holder.contactMail.compareTo(request.getEmail()) == 0) {
                File avatar = null;
                if (context.getExternalCacheDir() != null) {
                    avatar = new File(context.getExternalCacheDir().getAbsolutePath(),holder.contactMail + ".jpg");
                } else {
                    avatar = new File(context.getCacheDir().getAbsolutePath(),holder.contactMail + ".jpg");
                }
                Bitmap bitmap = null;
                if (avatar.exists()) {
                    if (avatar.length() > 0) {
                        BitmapFactory.Options bOpts = new BitmapFactory.Options();
                        bOpts.inPurgeable = true;
                        bOpts.inInputShareable = true;
                        bitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(),bOpts);
                        if (bitmap == null) {
                            avatar.delete();
                        } else {
//								holder.imageView.setImageBitmap(bitmap);
                            if (holder instanceof MegaContactsLollipopAdapter.ViewHolderContactsGrid) {
                                bitmap = ThumbnailUtilsLollipop.getRoundedRectBitmap(context,bitmap,3);
                                ((MegaContactsLollipopAdapter.ViewHolderContactsGrid)holder).imageView.setImageBitmap(bitmap);
                            } else if (holder instanceof MegaContactsLollipopAdapter.ViewHolderContactsList) {
                                ((MegaContactsLollipopAdapter.ViewHolderContactsList)holder).imageView.setImageBitmap(bitmap);
                            } else if (holder instanceof LastContactsAdapter.ViewHolder) {
                                ((LastContactsAdapter.ViewHolder)holder).avatarImage.setImageBitmap(bitmap);
                            }
                            holder.contactInitialLetter.setVisibility(View.GONE);
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public void onRequestTemporaryError(MegaApiJava api,MegaRequest request,MegaError e) {
    }
    
    @Override
    public void onRequestUpdate(MegaApiJava api,MegaRequest request) {
    }
    
}

