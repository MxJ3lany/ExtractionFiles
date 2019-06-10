package mega.privacy.android.app.lollipop.megachat.calls;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.nio.ByteBuffer;

import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.R;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatApiJava;
import nz.mega.sdk.MegaChatVideoListenerInterface;


public class LocalCameraCallFragment extends Fragment implements MegaChatVideoListenerInterface {

    int width = 0;
    int height = 0;
    Bitmap bitmap;
    MegaChatApiAndroid megaChatApi;
    Context context;
    long chatId;

    public SurfaceView localSurfaceView;
    MegaSurfaceRenderer localRenderer;
    ImageView microIcon;

    public static LocalCameraCallFragment newInstance(long chatId) {
        log("newInstance() chatId: "+chatId);
        LocalCameraCallFragment f = new LocalCameraCallFragment();
        Bundle args = new Bundle();
        args.putLong("chatId", chatId);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate (Bundle savedInstanceState){
        if (megaChatApi == null){
            megaChatApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaChatApi();
        }
        Bundle args = getArguments();
        this.chatId = args.getLong("chatId", -1);
        log("onCreate() chatId: "+chatId);
        super.onCreate(savedInstanceState);
        log("after onCreate called super");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (!isAdded()) {
            return null;
        }

        View v = inflater.inflate(R.layout.fragment_local_camera_call, container, false);
        log("onCreateView()");
        localSurfaceView = (SurfaceView)v.findViewById(R.id.surface_local_video);
        localSurfaceView.setZOrderMediaOverlay(true);
        SurfaceHolder localSurfaceHolder = localSurfaceView.getHolder();
        localSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        localRenderer = new MegaSurfaceRenderer(localSurfaceView);

        microIcon = (ImageView) v.findViewById(R.id.micro_surface_view);
        microIcon.setVisibility(View.GONE);
        ((ChatCallActivity)context).refreshOwnMicro();
        megaChatApi.addChatLocalVideoListener(chatId, this);

        return v;
    }

    @Override
    public void onChatVideoData(MegaChatApiJava api, long chatid, int width, int height, byte[] byteBuffer) {
        if((width == 0) || (height == 0)){
            return;
        }

        if (this.width != width || this.height != height)
        {
            this.width = width;
            this.height = height;

            SurfaceHolder holder = localSurfaceView.getHolder();
            if (holder != null) {
                int viewWidth = localSurfaceView.getWidth();
                int viewHeight = localSurfaceView.getHeight();
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

        if (bitmap != null){
            bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(byteBuffer));
            localRenderer.DrawBitmap(true, true);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDestroy(){
        log("onDestroy");
        removeSurfaceView();
        super.onDestroy();
    }
    @Override
    public void onResume() {
        log("onResume()");
        this.width=0;
        this.height=0;
        localSurfaceView.setVisibility(View.VISIBLE);

        super.onResume();
    }

    public void showMicro(boolean isShouldShown){
        log("showMicro");

        if(microIcon!=null){
            if(isShouldShown){
                microIcon.setVisibility(View.VISIBLE);
            }else{
                microIcon.setVisibility(View.GONE);
            }
        }
    }

    public void removeSurfaceView(){
        log("removeSurfaceView()");
        if(microIcon!=null){
            microIcon.setVisibility(View.GONE);
        }
        if(localSurfaceView!=null){
            if(localSurfaceView.getParent()!=null){
                if(localSurfaceView.getParent().getParent()!=null){
                    log("removeSurfaceView() removeView chatId: "+chatId);
                    ((ViewGroup)localSurfaceView.getParent()).removeView(localSurfaceView);
                }
            }
            localSurfaceView.setVisibility(View.GONE);
        }

        megaChatApi.removeChatVideoListener(chatId, -1, -1, this);
    }


    private static void log(String log) {
        Util.log("LocalCameraCallFragment", log);
    }
}
