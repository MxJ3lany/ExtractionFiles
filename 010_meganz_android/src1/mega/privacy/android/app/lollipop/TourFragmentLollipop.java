package mega.privacy.android.app.lollipop;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import mega.privacy.android.app.R;
import mega.privacy.android.app.TourImageAdapter;
import mega.privacy.android.app.components.LoopViewPager;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;

public class TourFragmentLollipop extends Fragment implements View.OnClickListener{

    Context context;
    private TourImageAdapter adapter;
    private LoopViewPager viewPager;
    private ImageView firstItem;
    private ImageView secondItem;
    private ImageView thirdItem;
    private ImageView fourthItem;
    private Button bRegister;
    private Button bLogin;

    @Override
    public void onCreate (Bundle savedInstanceState){
        log("onCreate");
        super.onCreate(savedInstanceState);

        if(context==null){
            log("context is null");
            return;
        }
    }

    void setStatusBarColor (int position) {
        switch (position) {
            case 0: {
                ((LoginActivityLollipop) context).getWindow().setStatusBarColor(ContextCompat.getColor(context, R.color.statusbar_tour_1));
                break;
            }
            case 1: {
                ((LoginActivityLollipop) context).getWindow().setStatusBarColor(ContextCompat.getColor(context, R.color.statusbar_tour_2));
                break;
            }
            case 2: {
                ((LoginActivityLollipop) context).getWindow().setStatusBarColor(ContextCompat.getColor(context, R.color.statusbar_tour_3));
                break;
            }
            case 3: {
                ((LoginActivityLollipop) context).getWindow().setStatusBarColor(ContextCompat.getColor(context, R.color.statusbar_tour_4));
                break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setStatusBarColor(viewPager.getCurrentItem());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        log("onCreateView");

        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        View v = inflater.inflate(R.layout.fragment_tour, container, false);
        viewPager = (LoopViewPager) v.findViewById(R.id.pager);
        firstItem = (ImageView) v.findViewById(R.id.first_item);
        secondItem = (ImageView) v.findViewById(R.id.second_item);
        thirdItem = (ImageView) v.findViewById(R.id.third_item);
        fourthItem = (ImageView) v.findViewById(R.id.fourth_item);

        bLogin = (Button) v.findViewById(R.id.button_login_tour);
        bRegister = (Button) v.findViewById(R.id.button_register_tour);

        bLogin.setOnClickListener(this);
        bRegister.setOnClickListener(this);

        adapter = new TourImageAdapter((LoginActivityLollipop)context);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);

        firstItem.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.selection_circle_page_adapter));
        secondItem.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.not_selection_circle_page_adapter));
        thirdItem.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.not_selection_circle_page_adapter));
        fourthItem.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.not_selection_circle_page_adapter));
        setStatusBarColor(0);

        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){

            @Override
            public void onPageSelected (int position){

                setStatusBarColor(position);

                switch(position){
                    case 0:{
                        firstItem.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.selection_circle_page_adapter));
                        secondItem.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.not_selection_circle_page_adapter));
                        thirdItem.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.not_selection_circle_page_adapter));
                        fourthItem.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.not_selection_circle_page_adapter));
                        break;
                    }
                    case 1:{
                        firstItem.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.not_selection_circle_page_adapter));
                        secondItem.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.selection_circle_page_adapter));
                        thirdItem.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.not_selection_circle_page_adapter));
                        fourthItem.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.not_selection_circle_page_adapter));
                        break;
                    }
                    case 2:{
                        firstItem.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.not_selection_circle_page_adapter));
                        secondItem.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.not_selection_circle_page_adapter));
                        thirdItem.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.selection_circle_page_adapter));
                        fourthItem.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.not_selection_circle_page_adapter));
                        break;
                    }
                    case 3:{
                        firstItem.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.not_selection_circle_page_adapter));
                        secondItem.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.not_selection_circle_page_adapter));
                        thirdItem.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.not_selection_circle_page_adapter));
                        fourthItem.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.selection_circle_page_adapter));
                        break;
                    }
                }
            }
        });

        return v;
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()){
            case R.id.button_register_tour:
                log("onRegisterClick");
                ((LoginActivityLollipop)context).showFragment(Constants.CREATE_ACCOUNT_FRAGMENT);
                break;
            case R.id.button_login_tour:
                log("onLoginClick");
                ((LoginActivityLollipop)context).showFragment(Constants.LOGIN_FRAGMENT);
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        log("onAttach");
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onAttach(Activity context) {
        log("onAttach Activity");
        super.onAttach(context);
        this.context = context;
    }

    public static void log(String message) {
        Util.log("TourFragmentLollipop", message);
    }

}
