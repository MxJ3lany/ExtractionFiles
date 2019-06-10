package mega.privacy.android.app.lollipop.megachat.calls;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import java.nio.ByteBuffer;

import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.R;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatApiJava;
import nz.mega.sdk.MegaChatVideoListenerInterface;


public class LocalCameraCallFullScreenFragment extends Fragment implements MegaChatVideoListenerInterface {

    int width = 0;
    int height = 0;
    Bitmap bitmap;
    MegaChatApiAndroid megaChatApi;
    Context context;
    long chatId;

    public SurfaceView localFullScreenSurfaceView;
    MegaSurfaceRenderer localRenderer;

    public static LocalCameraCallFullScreenFragment newInstance(long chatId) {
        log("newInstance");
        LocalCameraCallFullScreenFragment f = new LocalCameraCallFullScreenFragment();
        Bundle args = new Bundle();
        args.putLong("chatId", chatId);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate (Bundle savedInstanceState){
        log("onCreate");
        if (megaChatApi == null){
            megaChatApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaChatApi();
        }

        Bundle args = getArguments();
        this.chatId = args.getLong("chatId", -1);
        super.onCreate(savedInstanceState);
        log("after onCreate called super");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (!isAdded()) {
            return null;
        }

        View v = inflater.inflate(R.layout.fragment_local_camera_call_full_screen, container, false);

        localFullScreenSurfaceView = (SurfaceView)v.findViewById(R.id.surface_local_video);
        localFullScreenSurfaceView.setZOrderMediaOverlay(true);
        SurfaceHolder localSurfaceHolder = localFullScreenSurfaceView.getHolder();
        localSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        localRenderer = new MegaSurfaceRenderer(localFullScreenSurfaceView);

        log("onCreateView() addChatLocalVideoListener chatId: "+chatId);
        megaChatApi.addChatLocalVideoListener(chatId, this);

        return v;
    }

    @Override
    public void onChatVideoData(MegaChatApiJava api, long chatid, int width, int height, byte[] byteBuffer) {
        if((width == 0) || (height == 0)){
            return;
        }

        if (this.width != width || this.height != height) {
            this.width = width;
            this.height = height;

            SurfaceHolder holder = localFullScreenSurfaceView.getHolder();
            if (holder != null) {
                int viewWidth = localFullScreenSurfaceView.getWidth();
                int viewHeight = localFullScreenSurfaceView.getHeight();
                if ((viewWidth != 0) && (viewHeight != 0)) {
                    int holderWidth = viewWidth < width ? viewWidth : width;
                    int holderHeight = holderWidth * viewHeight / viewWidth;
                    if (holderHeight > viewHeight) {
                        holderHeight = viewHeight;
                        holderWidth = holderHeight * viewWidth / viewHeight;
                    }
                    this.bitmap = localRenderer.CreateBitmap(width, height);
                    holder.setFixedSize(holderWidth, holderHeight);
                }
                else{
                    this.width = -1;
                    this.height = -1;
                }
            }
        }

        if (bitmap != null) {
            bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(byteBuffer));

            // Instead of using this WebRTC renderer, we should probably draw the image by ourselves.
            // The renderer has been modified a bit and an update of WebRTC could break our app
            localRenderer.DrawBitmap(false, true);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDestroy(){
        log("onDestroy()");
        removeSurfaceView();
        super.onDestroy();
    }
    @Override
    public void onResume() {
        log("onResume");
        this.width=0;
        this.height=0;
        localFullScreenSurfaceView.setVisibility(View.VISIBLE);

        super.onResume();
    }
    public void removeSurfaceView(){
        log("removeSurfaceView()");
        if(localFullScreenSurfaceView.getParent()!=null){
            if(localFullScreenSurfaceView.getParent().getParent()!=null){
                log("removeSurfaceView() removeView chatId: "+chatId);
                ((ViewGroup)localFullScreenSurfaceView.getParent()).removeView(localFullScreenSurfaceView);
            }
        }
        localFullScreenSurfaceView.setVisibility(View.GONE);
        megaChatApi.removeChatVideoListener(chatId, -1, -1,this);
    }

    private static void log(String log) {
        Util.log("LocalCameraCallFullScreenFragment", log);
    }
}
