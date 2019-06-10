package com.oraclechain.pocketeos.view;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.liaoinstan.springview.container.BaseHeader;
import com.oraclechain.pocketeos.R;

/**
 * Created by pocketEos on 2017/12/7.
 * app默认下拉刷新效果
 */

public class AppDefeatHeadView extends BaseHeader {
    private Context context;
    private int rotationSrc;
    private int arrowSrc;
    private int logoSrc;
    private boolean isShowText;

    private final int ROTATE_ANIM_DURATION = 180;
    private RotateAnimation mRotateUpAnim;
    private RotateAnimation mRotateDownAnim;

    private TextView headerTitle;
    private ImageView headerArrow;
    private ImageView headerLogo;
    private ProgressBar headerProgressbar;
    private View frame;

    public AppDefeatHeadView(Context context){
        this(context, 0, R.mipmap.whitearrow,0,false);
    }

    public AppDefeatHeadView(Context context,boolean isShowText){
        this(context, 0,R.mipmap.whitearrow,0,isShowText);
    }

    public AppDefeatHeadView(Context context,int logoSrc){
        this(context, 0,R.mipmap.whitearrow,logoSrc,false);
    }

    public AppDefeatHeadView(Context context,int logoSrc,boolean isShowText){
        this(context, 0,R.mipmap.whitearrow,logoSrc,isShowText);
    }

    public AppDefeatHeadView(Context context,int rotationSrc,int arrowSrc,int logoSrc,boolean isShowText){
        this.context = context;
        this.rotationSrc = rotationSrc;
        this.arrowSrc = arrowSrc;
        this.logoSrc = logoSrc;
        this.isShowText = isShowText;
        mRotateUpAnim = new RotateAnimation(0.0f, -180.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,0.5f);
        mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateUpAnim.setFillAfter(true);
        mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,0.5f);
        mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateDownAnim.setFillAfter(true);
    }

    @Override
    public View getView(LayoutInflater inflater, ViewGroup viewGroup) {
        View view = inflater.inflate(R.layout.app_defeat_header_view, viewGroup, true);
        headerTitle = (TextView) view.findViewById(R.id.app_defeat_header_text);
        headerArrow = (ImageView) view.findViewById(R.id.app_defeat_header_arrow);
        headerLogo = (ImageView) view.findViewById(R.id.app_defeat_header_logo);
        headerProgressbar = (ProgressBar) view.findViewById(R.id.app_defeat_header_progressbar);
        frame = view.findViewById(R.id.app_defeat_frame);
        if(logoSrc!=0) headerLogo.setImageResource(logoSrc);
        if(!isShowText) headerTitle.setVisibility(View.GONE);
        if(rotationSrc!=0) headerProgressbar.setIndeterminateDrawable(ContextCompat.getDrawable(context, rotationSrc));
        headerArrow.setImageResource(arrowSrc);
        return view;
    }

    @Override
    public int getDragSpringHeight(View rootView) {
        return frame.getMeasuredHeight();
    }

    @Override
    public int getDragLimitHeight(View rootView) {
        return frame.getMeasuredHeight();
    }

    @Override
    public void onPreDrag(View rootView) {
    }

    @Override
    public void onDropAnim(View rootView, int dy) {
    }

    @Override
    public void onLimitDes(View rootView, boolean upORdown) {
        if (!upORdown){
            headerTitle.setText("松开刷新");
            if (headerArrow.getVisibility()==View.VISIBLE)
                headerArrow.startAnimation(mRotateUpAnim);
        }
        else {
            headerTitle.setText("下拉刷新");
            if (headerArrow.getVisibility()==View.VISIBLE)
                headerArrow.startAnimation(mRotateDownAnim);
        }
    }

    @Override
    public void onStartAnim() {
        headerTitle.setText("正在刷新");
        headerArrow.setVisibility(View.INVISIBLE);
        headerArrow.clearAnimation();
        headerProgressbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFinishAnim() {
        headerTitle.setText("下拉刷新");
        headerArrow.setVisibility(View.VISIBLE);
        headerProgressbar.setVisibility(View.INVISIBLE);
    }
}
