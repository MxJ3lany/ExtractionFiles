package com.battlelancer.seriesguide.ui;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ImageView;
import androidx.appcompat.app.ActionBar;
import com.battlelancer.seriesguide.R;
import com.battlelancer.seriesguide.util.ServiceUtils;
import com.battlelancer.seriesguide.util.SystemUiHider;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import uk.co.senab.photoview.PhotoView;

/**
 * Displays an image URL full screen in a zoomable view. If a preview image URL is provided, it is
 * shown as a placeholder until the higher resolution image loads. The preview image has to be
 * cached by Picasso already.
 */
public class FullscreenImageActivity extends BaseActivity {

    /**
     * Image URL that has been cached already. Will show initially before replacing with larger
     * version.
     */
    public static final String EXTRA_PREVIEW_IMAGE = "PREVIEW_IMAGE";
    public static final String EXTRA_IMAGE = "IMAGE";

    private static final int DELAY_100_MS = 100;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider systemUiHider;

    /**
     * Displays the poster or episode preview
     */
    private PhotoView photoView;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);
        setupActionBar();

        setupViews();
    }

    @Override
    protected void setupActionBar() {
        super.setupActionBar();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private void setupViews() {
        photoView = findViewById(R.id.fullscreen_content);

        // try to immediately show cached preview image
        String previewImagePath = getIntent().getStringExtra(EXTRA_PREVIEW_IMAGE);
        if (!TextUtils.isEmpty(previewImagePath)) {
            ServiceUtils.loadWithPicasso(this, previewImagePath)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(photoView, new Callback() {
                        @Override
                        public void onSuccess() {
                            loadLargeImage(true);
                        }

                        @Override
                        public void onError(Exception e) {
                            loadLargeImage(false);
                        }
                    });
        } else {
            loadLargeImage(false);
        }

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        systemUiHider = SystemUiHider.getInstance(this, photoView,
                SystemUiHider.FLAG_FULLSCREEN);
        systemUiHider.setup();

        photoView.setOnViewTapListener((view, x, y) -> systemUiHider.toggle());
    }

    private void loadLargeImage(boolean hasPreviewImage) {
        String imagePath = getIntent().getStringExtra(EXTRA_IMAGE);
        if (TextUtils.isEmpty(imagePath)) {
            imagePath = null; // set to null so picasso shows error drawable
        }
        RequestCreator requestCreator = ServiceUtils.loadWithPicasso(this, imagePath);
        if (hasPreviewImage) {
            // keep showing preview image if loading full image fails
            requestCreator.noPlaceholder().into(photoView);
        } else {
            // no preview image? show error image instead if loading full image fails
            requestCreator
                    .error(R.drawable.ic_photo_gray_24dp)
                    .into(photoView, new Callback() {
                        @Override
                        public void onSuccess() {
                            photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        }

                        @Override
                        public void onError(Exception e) {
                            photoView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        }
                    });
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        delayedHide();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isFinishing()) {
            // Always cancel the request here, this is safe to call even if the image has been loaded.
            // This ensures that the anonymous callback we have does not prevent the activity from
            // being garbage collected. It also prevents our callback from getting invoked even after the
            // activity has finished.
            Picasso.get().cancelRequest(photoView);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    Handler hideHandler = new Handler();

    Runnable hideRunnable = new Runnable() {
        @Override
        public void run() {
            systemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any previously scheduled
     * calls.
     */
    private void delayedHide() {
        hideHandler.removeCallbacks(hideRunnable);
        hideHandler.postDelayed(hideRunnable, DELAY_100_MS);
    }
}
