package com.applozic.mobicomkit.uiwidgets.uikit;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.adapter.MobicomMultimediaPopupAdapter;

import java.util.Arrays;

/**
 * Created by ashish on 15/05/18.
 */

public class AlAttachmentView extends ApplozicComponents implements AdapterView.OnItemClickListener {

    Activity activity;

    public AlAttachmentView(Context context) {
        super(context);
    }

    public AlAttachmentView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AlAttachmentView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void createView() {
        removeAllViews();
        GridView gridView = new GridView(getContext());
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        gridView.setLayoutParams(params);
        gridView.setNumColumns(3);
        gridView.setHorizontalSpacing(1);
        gridView.setVerticalSpacing(1);
        gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        prepareAttachmentData(gridView);
        gridView.setOnItemClickListener(this);

        addView(gridView);
    }

    public void prepareAttachmentData(GridView gridView) {
        String[] allValues = getResources().getStringArray(R.array.multimediaOptions_without_price_text);
        String[] allIcons = getResources().getStringArray(R.array.multimediaOptionIcons_without_price);

        MobicomMultimediaPopupAdapter adapter = new MobicomMultimediaPopupAdapter(getContext(), Arrays.asList(allIcons), Arrays.asList(allValues));
        gridView.setAdapter(adapter);
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                AlAttachmentOptions.processLocationAction(getActivity(), null);
                break;
            case 1:
                AlAttachmentOptions.processCameraAction(getActivity(), null);
                break;
            case 2:
                AlAttachmentOptions.processFileAction(getActivity(), null);
                break;
            case 3:
                AlAttachmentOptions.processAudioAction(getAppComaptActivity(), null);
                break;
            case 4:
                AlAttachmentOptions.processVideoAction(getActivity(), null);
                break;
            case 5:
                AlAttachmentOptions.processContactAction(getActivity(), null);
                break;
        }
    }

    @Override
    public Activity getActivity() {
        if (activity != null) {
            return activity;
        }
        return super.getActivity();
    }

    public AppCompatActivity getAppComaptActivity() {
        if (activity instanceof AppCompatActivity) {
            return (AppCompatActivity) activity;
        }
        return super.getAppCompatActivity();
    }
}
