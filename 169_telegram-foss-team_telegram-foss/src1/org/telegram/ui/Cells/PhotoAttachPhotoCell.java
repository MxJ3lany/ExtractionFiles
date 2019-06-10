/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.Cells;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.R;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CheckBox;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.PhotoViewer;

public class PhotoAttachPhotoCell extends FrameLayout {

    private BackupImageView imageView;
    private FrameLayout checkFrame;
    private CheckBox checkBox;
    private TextView videoTextView;
    private FrameLayout videoInfoContainer;
    private AnimatorSet animatorSet;
    private boolean isLast;
    private boolean pressed;
    private static Rect rect = new Rect();
    private PhotoAttachPhotoCellDelegate delegate;
    private boolean isVertical;
    private boolean needCheckShow;

    public interface PhotoAttachPhotoCellDelegate {
        void onCheckClick(PhotoAttachPhotoCell v);
    }

    private MediaController.PhotoEntry photoEntry;

    public PhotoAttachPhotoCell(Context context) {
        super(context);

        imageView = new BackupImageView(context);
        addView(imageView, LayoutHelper.createFrame(80, 80));
        checkFrame = new FrameLayout(context);
        addView(checkFrame, LayoutHelper.createFrame(42, 42, Gravity.LEFT | Gravity.TOP, 38, 0, 0, 0));

        videoInfoContainer = new FrameLayout(context);
        videoInfoContainer.setBackgroundResource(R.drawable.phototime);
        videoInfoContainer.setPadding(AndroidUtilities.dp(3), 0, AndroidUtilities.dp(3), 0);
        addView(videoInfoContainer, LayoutHelper.createFrame(80, 16, Gravity.BOTTOM | Gravity.LEFT));

        ImageView imageView1 = new ImageView(context);
        imageView1.setImageResource(R.drawable.ic_video);
        videoInfoContainer.addView(imageView1, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL));

        videoTextView = new TextView(context);
        videoTextView.setTextColor(0xffffffff);
        videoTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        videoTextView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        videoInfoContainer.addView(videoTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL, 18, -0.7f, 0, 0));

        checkBox = new CheckBox(context, R.drawable.checkbig);
        checkBox.setSize(30);
        checkBox.setCheckOffset(AndroidUtilities.dp(1));
        checkBox.setDrawBackground(true);
        checkBox.setColor(0xff3ccaef, 0xffffffff);
        addView(checkBox, LayoutHelper.createFrame(30, 30, Gravity.LEFT | Gravity.TOP, 46, 4, 0, 0));
        checkBox.setVisibility(VISIBLE);
        setFocusable(true);
    }

    public void setIsVertical(boolean value) {
        isVertical = value;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isVertical) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(80), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(80 + (isLast ? 0 : 6)), MeasureSpec.EXACTLY));
        } else {
            super.onMeasure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(80 + (isLast ? 0 : 6)), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(80), MeasureSpec.EXACTLY));
        }
    }

    public MediaController.PhotoEntry getPhotoEntry() {
        return photoEntry;
    }

    public BackupImageView getImageView() {
        return imageView;
    }

    public CheckBox getCheckBox() {
        return checkBox;
    }

    public FrameLayout getCheckFrame() {
        return checkFrame;
    }

    public View getVideoInfoContainer() {
        return videoInfoContainer;
    }

    public void setPhotoEntry(MediaController.PhotoEntry entry, boolean needCheckShow, boolean last) {
        pressed = false;
        photoEntry = entry;
        isLast = last;
        if (photoEntry.isVideo) {
            imageView.setOrientation(0, true);
            videoInfoContainer.setVisibility(VISIBLE);
            int minutes = photoEntry.duration / 60;
            int seconds = photoEntry.duration - minutes * 60;
            videoTextView.setText(String.format("%d:%02d", minutes, seconds));
        } else {
            videoInfoContainer.setVisibility(INVISIBLE);
        }
        if (photoEntry.thumbPath != null) {
            imageView.setImage(photoEntry.thumbPath, null, getResources().getDrawable(R.drawable.nophotos));
        } else if (photoEntry.path != null) {
            if (photoEntry.isVideo) {
                imageView.setImage("vthumb://" + photoEntry.imageId + ":" + photoEntry.path, null, getResources().getDrawable(R.drawable.nophotos));
            } else {
                imageView.setOrientation(photoEntry.orientation, true);
                imageView.setImage("thumb://" + photoEntry.imageId + ":" + photoEntry.path, null, getResources().getDrawable(R.drawable.nophotos));
            }
        } else {
            imageView.setImageResource(R.drawable.nophotos);
        }
        boolean showing = needCheckShow && PhotoViewer.isShowingImage(photoEntry.path);
        imageView.getImageReceiver().setVisible(!showing, true);
        checkBox.setAlpha(showing ? 0.0f : 1.0f);
        videoInfoContainer.setAlpha(showing ? 0.0f : 1.0f);
        requestLayout();
    }

    public void setChecked(int num, boolean value, boolean animated) {
        checkBox.setChecked(num, value, animated);
    }

    public void setNum(int num) {
        checkBox.setNum(num);
    }

    public void setOnCheckClickLisnener(OnClickListener onCheckClickLisnener) {
        checkFrame.setOnClickListener(onCheckClickLisnener);
    }

    public void setDelegate(PhotoAttachPhotoCellDelegate delegate) {
        this.delegate = delegate;
    }

    public void callDelegate() {
        delegate.onCheckClick(this);
    }

    public void showImage() {
        imageView.getImageReceiver().setVisible(true, true);
    }

    public void showCheck(boolean show) {
        if (show && checkBox.getAlpha() == 1 || !show && checkBox.getAlpha() == 0) {
            return;
        }
        if (animatorSet != null) {
            animatorSet.cancel();
            animatorSet = null;
        }
        animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.setDuration(180);
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(videoInfoContainer, "alpha", show ? 1.0f : 0.0f),
                ObjectAnimator.ofFloat(checkBox, "alpha", show ? 1.0f : 0.0f));
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (animation.equals(animatorSet)) {
                    animatorSet = null;
                }
            }
        });
        animatorSet.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = false;

        checkFrame.getHitRect(rect);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (rect.contains((int) event.getX(), (int) event.getY())) {
                pressed = true;
                invalidate();
                result = true;
            }
        } else if (pressed) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                getParent().requestDisallowInterceptTouchEvent(true);
                pressed = false;
                playSoundEffect(SoundEffectConstants.CLICK);
                sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);
                delegate.onCheckClick(this);
                invalidate();
            } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                pressed = false;
                invalidate();
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (!(rect.contains((int) event.getX(), (int) event.getY()))) {
                    pressed = false;
                    invalidate();
                }
            }
        }
        if (!result) {
            result = super.onTouchEvent(event);
        }

        return result;
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setEnabled(true);
        if (photoEntry.isVideo) {
            info.setText(LocaleController.getString("AttachVideo", R.string.AttachVideo) + ", " + LocaleController.formatCallDuration(photoEntry.duration));
        } else {
            info.setText(LocaleController.getString("AttachPhoto", R.string.AttachPhoto));
        }
        if (checkBox.isChecked())
            info.setSelected(true);
        if (Build.VERSION.SDK_INT >= 21) {
            info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.acc_action_open_photo, LocaleController.getString("Open", R.string.Open)));
        }
    }

    @Override
    public boolean performAccessibilityAction(int action, Bundle arguments) {
        if (action == R.id.acc_action_open_photo) {
            View parent = (View)getParent();
            parent.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, getLeft(), getTop() + getHeight() - 1, 0));
            parent.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, getLeft(), getTop() + getHeight() - 1, 0));
        }
        return super.performAccessibilityAction(action, arguments);
    }
}
