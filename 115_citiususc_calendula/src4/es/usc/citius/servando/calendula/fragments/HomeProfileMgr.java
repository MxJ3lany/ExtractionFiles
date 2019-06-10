/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2014-2018 CiTIUS - University of Santiago de Compostela
 *
 *    Calendula is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;
import es.usc.citius.servando.calendula.util.Snack;

/**
 * A simple {@link Fragment} subclass.
 * to handle interaction events.
 */
public class HomeProfileMgr {


    public static final int BG_COUNT = 8;
    public static final String BG_IMAGE_PATTERN = "home_bg_%d.webp";

    private static HashMap<String, Integer> cache = new HashMap<>();
    int[] moodRes = new int[]{
            R.drawable.mood_1,
            R.drawable.mood_2,
            R.drawable.mood_3,
            R.drawable.mood_4,
            R.drawable.mood_5,
    };
    int[] moodColor = new int[]{
            R.color.android_red,
            R.color.android_orange,
            R.color.white,
            R.color.android_blue,
            R.color.android_green
    };
    ImageView background;
    View profileImageContainer;
    TextView profileUsername;
    RelativeLayout profileContainer;
    //CustomDigitalClock clock;
    TextView monthTv;
    TextView dayTv;
    ImageView moodImg;
    RoundedImageView modFabButton;
    ListAdapter moodsAdapter;
    ImageView bottomShadow;
    String[] moods;
    int currentBgFileIdx = 0;
    View profileInfo;
    private Activity context;
    private View rootView;

    public HomeProfileMgr() {

    }

    public static int colorForCurrent(Context ctx) {

        String path = getBackgroundPath();
        int color = Color.BLACK;
        if (!cache.containsKey(path)) {
            Bitmap bm = getBitmapFromAsset(ctx, path);
            if (bm != null) {
                Palette p = Palette.generate(bm);
                color = p.getVibrantColor(color);
            }
            cache.put(path, color);
        }
        return cache.get(path);
    }

    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            // handle exception
        }

        return bitmap;
    }

    static String getBackgroundPath() {
        Integer idx = PreferenceUtils.getInt(PreferenceKeys.HOME_PROFILE_BACKGROUND_INDEX, 1);
        return String.format(BG_IMAGE_PATTERN, idx);
    }

    public void init(View view, final Activity ctx) {
        this.context = ctx;
        this.rootView = view;

//        Animation in = AnimationUtils.loadAnimation(ctx, android.R.anim.fade_in);
//        Animation out = AnimationUtils.loadAnimation(ctx, android.R.anim.fade_out);
        moods = ctx.getResources().getStringArray(R.array.moods);
        monthTv = (TextView) view.findViewById(R.id.month_text);
        dayTv = (TextView) view.findViewById(R.id.day_text);
        //clock = (CustomDigitalClock) view.findViewById(R.id.home_clock);
        bottomShadow = (ImageView) view.findViewById(R.id.bottom_shadow);
        profileInfo = view.findViewById(R.id.profile_info);

        profileUsername = (TextView) view.findViewById(R.id.profile_username);
        profileContainer = (RelativeLayout) view.findViewById(R.id.profile_container);
        profileImageContainer = view.findViewById(R.id.profile_image_container);
        background = (ImageView) view.findViewById(R.id.image_switcher);

        modFabButton = (RoundedImageView) view.findViewById(R.id.mod_circle);
        moodImg = (ImageView) view.findViewById(R.id.mood_button);
        moodImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoodsDialog();
            }
        });

        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBackground();
            }
        });

        updateModButton();
        updateProfileInfo();

        profileInfo.setVisibility(View.INVISIBLE);
        background.setVisibility(View.INVISIBLE);
        bottomShadow.setVisibility(View.INVISIBLE);

        Picasso.with(context)
                .load("file:///android_asset/" + getBackgroundPath())
                .into(background);

        background.post(new Runnable() {
            @Override
            public void run() {
                bottomShadow.setVisibility(View.VISIBLE);
                background.setVisibility(View.VISIBLE);
                background.animate().alpha(1).setDuration(200);
            }
        });

        background.postDelayed(new Runnable() {
            @Override
            public void run() {
                profileInfo.setVisibility(View.VISIBLE);
                profileInfo.setAlpha(0);
                profileInfo.animate().alpha(1).setDuration(400);
            }
        }, 300);

    }

    public void updateModButton() {
        int mood = PreferenceUtils.getInt(PreferenceKeys.HOME_LAST_MOOD, 2);
        int color = moodColor[mood];
        int res = moodRes[mood];
        modFabButton.setImageResource(color);
        moodImg.setImageResource(res);
    }

//    Bitmap getBackgroundBitmap() {
//        int width = (int) ScreenUtils.getDpSize(context).x;
//        int height = context.getResources().getDimensionPixelSize(R.dimen.header_height);
//        return ScreenUtils.getResizedBitmap(context, getBackgroundPath(), width, height);
//    }

//    Bitmap getRandomBackground() {
//        int width = (int) ScreenUtils.getDpSize(context).x;
//        int height = context.getResources().getDimensionPixelSize(R.dimen.header_height);
//        return ScreenUtils.getResizedBitmap(context, getRandomBackgroundPath(), width, height);
//    }

    public void updateBackground() {
        Picasso.with(context)
                .load("file:///android_asset/" + getRandomBackgroundPath())
                .centerCrop()
                .resize(background.getWidth(), background.getHeight())
                .placeholder(background.getDrawable())
                .into(background);

        CalendulaApp.eventBus().post(new BackgroundUpdatedEvent());
    }

    public void updateDate() {
        DateTime dt = DateTime.now();
        String dayStr = dt.dayOfMonth().getAsShortText();
        String monthStr = dt.monthOfYear().getAsShortText().toUpperCase();
        dayTv.setText(dayStr);
        monthTv.setText(monthStr);
    }

    public String getRandomBackgroundPath() {
        int rand = (((int) (Math.random() * 1000)) % BG_COUNT) + 1;
        if (rand == currentBgFileIdx) {
            rand = ((rand + 1) % BG_COUNT) + 1;
        }
        currentBgFileIdx = rand;
        PreferenceUtils.edit().putInt(PreferenceKeys.HOME_PROFILE_BACKGROUND_INDEX.key(), rand).apply();
        return String.format(BG_IMAGE_PATTERN, rand);

    }

    public void showMoodsDialog() {

        moodsAdapter = new MoodListAdapter(context, R.layout.mood_list_item, moods);

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
        builderSingle.setCancelable(true)
                .setTitle(context.getString(R.string.moods_dialog_title))
                .setAdapter(moodsAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Snack.show("Mood saved!", context);
                        PreferenceUtils.edit().putInt(PreferenceKeys.HOME_LAST_MOOD.key(), which).apply();
                        updateModButton();
                    }
                }).show();
    }

    public void onCollapse() {
        profileInfo.animate().alpha(0);
    }

    public void onExpand() {
        profileInfo.animate().alpha(1);
    }

    void updateProfileInfo() {
        String displayName = PreferenceUtils.getString(PreferenceKeys.HOME_DISPLAY_NAME, context.getString(R.string.app_name));
        profileUsername.setText(displayName);
        updateDate();
    }

    public class MoodListAdapter extends ArrayAdapter<String> {


        public MoodListAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        public MoodListAdapter(Context context, int resource, String[] items) {
            super(context, resource, items);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View v = convertView;
            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.mood_list_item, null);
            }
            int res = moodRes[position];
            int color = moodColor[position];
            v.findViewById(R.id.textView).setBackgroundColor(context.getResources().getColor(color));
            ((ImageView) v.findViewById(R.id.mood_image)).setImageResource(res);
            return v;
        }
    }

    public class BackgroundUpdatedEvent {
    }
}
