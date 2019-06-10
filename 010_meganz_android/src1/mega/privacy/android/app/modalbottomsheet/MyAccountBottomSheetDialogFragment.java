package mega.privacy.android.app.modalbottomsheet;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.R;
import mega.privacy.android.app.lollipop.LoginActivityLollipop;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.controllers.AccountController;
import mega.privacy.android.app.lollipop.qrcode.QRCodeActivity;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaChatApi;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatCall;
import nz.mega.sdk.MegaHandleList;

public class MyAccountBottomSheetDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    Context context;
    AccountController aC;

    private BottomSheetBehavior mBehavior;
    private LinearLayout items_layout;

    public LinearLayout mainLinearLayout;
    public LinearLayout optionChoosePicture;
    public LinearLayout optionTakePicture;
    public LinearLayout optionRemovePicture;
    public LinearLayout optionQRcode;
    MegaChatApiAndroid megaChatApi;



    DisplayMetrics outMetrics;
    private int heightDisplay;

    MegaApiAndroid megaApi;
    DatabaseHandler dbH;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (megaApi == null){
            megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
        }

        if(megaChatApi==null){
            megaChatApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaChatApi();
        }

        aC = new AccountController(context);

        dbH = DatabaseHandler.getDbHandler(getActivity());
    }

    @Override
    public void setupDialog(final Dialog dialog, int style) {

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        heightDisplay = outMetrics.heightPixels;

        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.bottom_sheet_my_account, null);

        mainLinearLayout = (LinearLayout) contentView.findViewById(R.id.my_account_bottom_sheet);
        items_layout = (LinearLayout) contentView.findViewById(R.id.items_layout);

        optionChoosePicture= (LinearLayout) contentView.findViewById(R.id.my_account_choose_photo_layout);
        optionTakePicture = (LinearLayout) contentView.findViewById(R.id.my_account_take_photo_layout);
        optionRemovePicture = (LinearLayout) contentView.findViewById(R.id.my_account_delete_layout);
        optionQRcode = (LinearLayout) contentView.findViewById(R.id.my_account_my_QR_code);

        optionChoosePicture.setOnClickListener(this);
        optionTakePicture.setOnClickListener(this);
        optionRemovePicture.setOnClickListener(this);
        optionQRcode.setOnClickListener(this);

        if(aC.existsAvatar()){
            optionRemovePicture.setVisibility(View.VISIBLE);
        }
        else{
            optionRemovePicture.setVisibility(View.GONE);
        }

        dialog.setContentView(contentView);
        mBehavior = BottomSheetBehavior.from((View) mainLinearLayout.getParent());
//        mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//
//        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            mBehavior.setPeekHeight((heightDisplay / 4) * 2);
//        }
//        else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
//            mBehavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);
//        }

        mBehavior.setPeekHeight(UtilsModalBottomSheet.getPeekHeight(items_layout, heightDisplay, context, 48));
        mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

    }


    @Override
    public void onClick(View v) {

        switch(v.getId()){

            case R.id.my_account_choose_photo_layout:{
                log("option choose photo avatar");
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/*");
                ((ManagerActivityLollipop)context).startActivityForResult(Intent.createChooser(intent, null), Constants.CHOOSE_PICTURE_PROFILE_CODE);

                dismissAllowingStateLoss();
                break;
            }
            case R.id.my_account_take_photo_layout:{
                log("option take photo avatar");

                ((ManagerActivityLollipop)context).checkPermissions();
                dismissAllowingStateLoss();
                break;
            }
            case R.id.my_account_delete_layout:{
                log("option delete avatar");
                ((ManagerActivityLollipop) context).showConfirmationDeleteAvatar();
                break;
            }
            case R.id.my_account_my_QR_code: {
                log("option QR code");
                //Check if there is a in progress call:
                boolean activeCall = false;
                if(megaChatApi!=null){
                    MegaHandleList listCalls = megaChatApi.getChatCalls();
                    int contCallNotPresent = 0;
                    if((listCalls!=null)&&(listCalls.size()>0)){
                        for(int i=0; i<listCalls.size(); i++){
                            MegaChatCall call = megaChatApi.getChatCall(listCalls.get(i));
                            if(call!=null){
                                if((call.getStatus() == MegaChatCall.CALL_STATUS_USER_NO_PRESENT)||(call.getStatus() == MegaChatCall.CALL_STATUS_RING_IN)){
                                    contCallNotPresent ++ ;
                                }
                            }
                        }
                        if(contCallNotPresent == listCalls.size()){
                            activeCall = false;
                        }else{
                            activeCall = true;
                        }
                    }
                }
                if(!activeCall){
                    Intent intent = new Intent(context, QRCodeActivity.class);
                    startActivity(intent);
                }

                break;
            }
        }

//        dismiss();
        mBehavior = BottomSheetBehavior.from((View) mainLinearLayout.getParent());
        mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.context = activity;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private static void log(String log) {
        Util.log("MyAccountBottomSheetDialogFragment", log);
    }
}
