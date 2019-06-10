/*
 * Copyright (C) 2019 The JackKnife Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lwh.jackknife.widget.wheelview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import com.lwh.jackknife.widget.R;
import com.lwh.jackknife.widget.wheelview.adapter.WheelAdapter;
import com.lwh.jackknife.widget.wheelview.listener.LoopViewGestureListener;
import com.lwh.jackknife.widget.wheelview.listener.OnItemSelectListener;
import com.lwh.jackknife.widget.wheelview.timer.InertiaTimerTask;
import com.lwh.jackknife.widget.wheelview.timer.MessageHandler;
import com.lwh.jackknife.widget.wheelview.timer.SmoothScrollTimerTask;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class WheelView extends View {

    public enum ACTION {
        CLICK, FLING, DAGGER
    }

    public enum DividerType {
        FILL, WRAP
    }

    private static final String[] TIME_NUM = {"00", "01", "02", "03", "04", "05", "06", "07", "08", "09"};
    private DividerType mDividerType;
    private Context mContext;
    private Handler mHandler;
    private GestureDetector mGestureDetector;
    private OnItemSelectListener mOnItemSelectListener;
    private boolean mOptions = false;
    private boolean mCenterLabel = true;
    private ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> mFuture;
    private TextPaint mOuterTextPaint;
    private TextPaint mCenterTextPaint;
    private Paint mIndicatorPaint;
    private WheelAdapter mAdapter;
    /**
     * 附加单位。
     */
    private String mUnitLabel;
    private int mTextSize;
    private int mMaxTextWidth;
    private int mMaxTextHeight;
    private int mTextXOffset;
    private float mItemHeight;
    private Typeface mTypeface = Typeface.MONOSPACE;
    private int mOuterTextColor;
    private int mCenterTextColor;
    private int mDividerColor;

    /**
     * 条目间距倍数。
     */
    private float mLineSpacingMultiplier = 1.6f;
    private boolean mLoop;

    private float mFirstLineY;
    private float mSecondLineY;
    private float mCenterY;

    private float mTotalScrollY;

    private int mPosition;

    private int mSelectedItem;
    private int mPreCurrentIndex;
    /**
     * 滚动偏移值,用于记录滚动了多少个item。
     */
    private int mScrollOffset;

    /**
     * 绘制几个条目，实际上第一项和最后一项Y轴压缩成0%了，所以可见的数目实际为9。
     */
    private int mVisibleItemNums = 11;

    private int mMeasuredWidth;
    private int mMeasuredHeight;

    private int mRadius;

    private int mOffset = 0;
    private float mPreviousY = 0;
    private long mStartTime = 0;

    /**
     * 这个值决定滑行速度
     */
    private static final int VELOCITY_FLING = 5;
    private int mWidthMeasureSpec;

    private int mGravity = Gravity.CENTER;
    private int mDrawCenterContentStart = 0;
    private int mDrawOuterContentStart = 0;
    private static final float SCALE_CONTENT = 0.8f;
    private float CENTER_CONTENT_OFFSET;
    private final float DEFAULT_TEXT_TARGET_SKEW_X = 0.5f;

    public WheelView(Context context) {
        this(context, null);
    }

    public WheelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WheelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mTextSize = getResources().getDimensionPixelSize(R.dimen.wheelview_textsize);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float density = dm.density;
        if (density < 1) {
            CENTER_CONTENT_OFFSET = 2.4f;
        } else if (1 <= density && density < 2) {
            CENTER_CONTENT_OFFSET = 3.6f;
        } else if (1 <= density && density < 2) {
            CENTER_CONTENT_OFFSET = 4.5f;
        } else if (2 <= density && density < 3) {
            CENTER_CONTENT_OFFSET = 6.0f;
        } else if (density >= 3) {
            CENTER_CONTENT_OFFSET = density * 2.5F;
        }
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WheelView, defStyleAttr, 0);
            mGravity = a.getInt(R.styleable.WheelView_wv_gravity, Gravity.CENTER);
            mOuterTextColor = a.getColor(R.styleable.WheelView_wv_textColorOut, 0xffa8a8a8);
            mCenterTextColor = a.getColor(R.styleable.WheelView_wv_textColorCenter, 0xff2a2a2a);
            mDividerColor = a.getColor(R.styleable.WheelView_wv_dividerColor, 0xffd5d5d5);
            mTextSize = a.getDimensionPixelOffset(R.styleable.WheelView_wv_textSize, mTextSize);
            mLineSpacingMultiplier = a.getFloat(R.styleable.WheelView_wv_lineSpacingMultiplier, mLineSpacingMultiplier);
            a.recycle();
        }
        judgeLineSpace();
        initLoopView(context);
    }

    private void judgeLineSpace() {
        if (mLineSpacingMultiplier < 1.0f) {
            mLineSpacingMultiplier = 1.0f;
        } else if (mLineSpacingMultiplier > 4.0f) {
            mLineSpacingMultiplier = 4.0f;
        }
    }

    private void initLoopView(Context context) {
        this.mContext = context;
        mHandler = new MessageHandler(this);
        mGestureDetector = new GestureDetector(context, new LoopViewGestureListener(this));
        mGestureDetector.setIsLongpressEnabled(false);
        mLoop = true;
        mTotalScrollY = 0;
        mPosition = -1;
        initPaints();
    }

    private void initPaints() {
        mOuterTextPaint = new TextPaint();
        mOuterTextPaint.setColor(mOuterTextColor);
        mOuterTextPaint.setAntiAlias(true);
        mOuterTextPaint.setTypeface(mTypeface);
        mOuterTextPaint.setTextSize(mTextSize);

        mCenterTextPaint = new TextPaint();
        mCenterTextPaint.setColor(mCenterTextColor);
        mCenterTextPaint.setAntiAlias(true);
        mCenterTextPaint.setTextScaleX(1.1F);
        mCenterTextPaint.setTypeface(mTypeface);
        mCenterTextPaint.setTextSize(mTextSize);

        mIndicatorPaint = new Paint();
        mIndicatorPaint.setColor(mDividerColor);
        mIndicatorPaint.setAntiAlias(true);

        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    private void performMeasure() {
        if (mAdapter == null) {
            return;
        }
        measureTextWidthHeight();
        int halfCircumference = (int) (mItemHeight * (mVisibleItemNums - 1));
        mMeasuredHeight = (int) ((halfCircumference * 2) / Math.PI);
        mRadius = (int) (halfCircumference / Math.PI);
        mMeasuredWidth = MeasureSpec.getSize(mWidthMeasureSpec);
        mFirstLineY = (mMeasuredHeight - mItemHeight) / 2.0f;
        mSecondLineY = (mMeasuredHeight + mItemHeight) / 2.0f;
        mCenterY = mSecondLineY - (mItemHeight - mMaxTextHeight) / 2.0f - CENTER_CONTENT_OFFSET;
        if (mPosition == -1) {
            if (mLoop) {
                mPosition = (mAdapter.getItemCount() + 1) / 2;
            } else {
                mPosition = 0;
            }
        }
        mPreCurrentIndex = mPosition;
    }

    private void measureTextWidthHeight() {
        Rect rect = new Rect();
        for (int i = 0; i < mAdapter.getItemCount(); i++) {
            String s1 = getContentText(mAdapter.getItem(i));
            mCenterTextPaint.getTextBounds(s1, 0, s1.length(), rect);

            int textWidth = rect.width();
            if (textWidth > mMaxTextWidth) {
                mMaxTextWidth = textWidth;
            }
        }
        mCenterTextPaint.getTextBounds("\u661F\u671F", 0, 2, rect);
        mMaxTextHeight = rect.height() + 2;
        mItemHeight = mLineSpacingMultiplier * mMaxTextHeight;
    }

    public void smoothScroll(ACTION action) {
        cancelFuture();
        if (action == ACTION.FLING || action == ACTION.DAGGER) {
            mOffset = (int) ((mTotalScrollY % mItemHeight + mItemHeight) % mItemHeight);
            if ((float) mOffset > mItemHeight / 2.0F) { //如果超过Item高度的一半，滚动到下一个Item去
                mOffset = (int) (mItemHeight - (float) mOffset);
            } else {
                mOffset = -mOffset;
            }
        }
        //停止的时候，位置有偏移，不是全部都能正确停止到中间位置的，这里把文字位置挪回中间去
        mFuture = mExecutor.scheduleWithFixedDelay(new SmoothScrollTimerTask(this, mOffset), 0, 10, TimeUnit.MILLISECONDS);
    }

    public final void scrollBy(float velocityY) {
        //滚动惯性的实现
        cancelFuture();
        mFuture = mExecutor.scheduleWithFixedDelay(new InertiaTimerTask(this, velocityY), 0, VELOCITY_FLING, TimeUnit.MILLISECONDS);
    }

    public void cancelFuture() {
        if (mFuture != null && !mFuture.isCancelled()) {
            mFuture.cancel(true);
            mFuture = null;
        }
    }

    /**
     * 设置是否循环滚动。
     *
     * @param loop 是否循环
     */
    public final void setLoop(boolean loop) {
        mLoop = loop;
    }

    public final void setTypeface(Typeface typeface) {
        mTypeface = typeface;
        mOuterTextPaint.setTypeface(mTypeface);
        mCenterTextPaint.setTypeface(mTypeface);
    }

    public final void setTextSize(float size) {
        if (size > 0.0f) {
            mTextSize = (int) (mContext.getResources().getDisplayMetrics().density * size);
            mOuterTextPaint.setTextSize(mTextSize);
            mCenterTextPaint.setTextSize(mTextSize);
        }
    }

    public final void setCurrentItem(int currentItem) {
        this.mSelectedItem = currentItem;
        this.mPosition = currentItem;
        mTotalScrollY = 0;
        invalidate();
    }

    public final void setOnItemSelectListener(OnItemSelectListener l) {
        this.mOnItemSelectListener = l;
    }

    public final void setAdapter(WheelAdapter adapter) {
        this.mAdapter = adapter;
        performMeasure();
        invalidate();
    }

    public final WheelAdapter getAdapter() {
        return mAdapter;
    }

    public final int getCurrentItem() {
        // return mSelectedItem;
        if (mAdapter == null) {
            return 0;
        }
        if (mLoop && (mSelectedItem < 0 || mSelectedItem >= mAdapter.getItemCount())) {
            return Math.max(0, Math.min(Math.abs(Math.abs(mSelectedItem) - mAdapter.getItemCount()), mAdapter.getItemCount() - 1));
        }
        return Math.max(0, Math.min(mSelectedItem, mAdapter.getItemCount() - 1));
    }

    public final void onItemSelected() {
        if (mOnItemSelectListener != null) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    mOnItemSelectListener.onItemSelected(getCurrentItem());
                }
            }, 200l);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mAdapter == null) {
            return;
        }
        mPosition = Math.min(Math.max(0, mPosition), mAdapter.getItemCount() - 1);
        Object visibles[] = new Object[mVisibleItemNums];
        mScrollOffset = (int) (mTotalScrollY / mItemHeight);
        try {
            mPreCurrentIndex = mPosition + mScrollOffset % mAdapter.getItemCount();
        } catch (ArithmeticException e) {
            e.printStackTrace();
        }
        if (!mLoop) {
            if (mPreCurrentIndex < 0) {
                mPreCurrentIndex = 0;
            }
            if (mPreCurrentIndex > mAdapter.getItemCount() - 1) {
                mPreCurrentIndex = mAdapter.getItemCount() - 1;
            }
        } else {
            if (mPreCurrentIndex < 0) {
                mPreCurrentIndex = mAdapter.getItemCount() + mPreCurrentIndex;
            }
            if (mPreCurrentIndex > mAdapter.getItemCount() - 1) {//同理上面,自己脑补一下
                mPreCurrentIndex = mPreCurrentIndex - mAdapter.getItemCount();
            }
        }
        // 跟滚动流畅度有关，总滑动距离与每个item高度取余，即并不是一格格的滚动，每个item不一定滚到对应Rect里的，
        // 这个item对应格子的偏移值
        float itemHeightOffset = (mTotalScrollY % mItemHeight);
        // 设置数组中每个元素的值
        int count = 0;
        while (count < mVisibleItemNums) {
            int index = mPreCurrentIndex - (mVisibleItemNums / 2 - count);//索引值，即当前在控件中间的item看作数据源的中间，计算出相对源数据源的index值
            //判断是否循环，如果是循环数据源也使用相对循环的position获取对应的item值，如果不是循环则超出数据源范围使用""空白字符串填充，在界面上形成空白无数据的item项
            if (mLoop) {
                index = getLoopMappingIndex(index);
                visibles[count] = mAdapter.getItem(index);
            } else if (index < 0) {
                visibles[count] = "";
            } else if (index > mAdapter.getItemCount() - 1) {
                visibles[count] = "";
            } else {
                visibles[count] = mAdapter.getItem(index);
            }
            count++;
        }

        //绘制中间两条横线
//        if (dividerType == DividerType.WRAP) {//横线长度仅包裹内容
//            float startX;
//            float endX;
//
//            if (TextUtils.isEmpty(mUnitLabel)) {//隐藏Label的情况
//                startX = (mMeasuredWidth - mMaxTextWidth) / 2 - 12;
//            } else {
//                startX = (mMeasuredWidth - mMaxTextWidth) / 4 - 12;
//            }
//
//            if (startX <= 0) {//如果超过了WheelView的边缘
//                startX = 10;
//            }
//            endX = mMeasuredWidth - startX;
//            canvas.drawLine(startX, mFirstLineY, endX, mFirstLineY, mIndicatorPaint);
//            canvas.drawLine(startX, mSecondLineY, endX, mSecondLineY, mIndicatorPaint);
//        } else {
//            canvas.drawLine(0.0F, mFirstLineY, mMeasuredWidth, mFirstLineY, mIndicatorPaint);
//            canvas.drawLine(0.0F, mSecondLineY, mMeasuredWidth, mSecondLineY, mIndicatorPaint);
//        }

        //只显示选中项Label文字的模式，并且Label文字不为空，则进行绘制
        if (!TextUtils.isEmpty(mUnitLabel) && mCenterLabel) {
            //绘制文字，靠右并留出空隙
            int drawRightContentStart = mMeasuredWidth - getTextWidth(mCenterTextPaint, mUnitLabel);
            canvas.drawText(mUnitLabel, drawRightContentStart - CENTER_CONTENT_OFFSET, mCenterY, mCenterTextPaint);
        }
        count = 0;
        while (count < mVisibleItemNums) {
            canvas.save();
            // 弧长 L = mItemHeight * counter - itemHeightOffset
            // 求弧度 α = L / r  (弧长/半径) [0,π]
            double radian = ((mItemHeight * count - itemHeightOffset)) / mRadius;
            // 弧度转换成角度(把半圆以Y轴为轴心向右转90度，使其处于第一象限及第四象限
            // angle [-90°,90°]
            float angle = (float) (90d - (radian / Math.PI) * 180d);//item第一项,从90度开始，逐渐递减到 -90度
            // 计算取值可能有细微偏差，保证负90°到90°以外的不绘制
            if (angle >= 90f || angle <= -90f) {
                canvas.restore();
            } else {
                // 根据当前角度计算出偏差系数，用以在绘制时控制文字的 水平移动 透明度 倾斜程度
                float offsetCoefficient = (float) Math.pow(Math.abs(angle) / 90f, 2.2);
                //获取内容文字
                String contentText;
                //如果是label每项都显示的模式，并且item内容不为空、mUnitLabel 也不为空
                if (!mCenterLabel && !TextUtils.isEmpty(mUnitLabel) && !TextUtils.isEmpty(getContentText(visibles[count]))) {
                    contentText = getContentText(visibles[count]) + mUnitLabel;
                } else {
                    contentText = getContentText(visibles[count]);
                }
                performMeasureTextSize(contentText);
                //计算开始绘制的位置
                measureCenterContentStart(contentText);
                measureOuterContentStart(contentText);
                float translateY = (float) (mRadius - Math.cos(radian) * mRadius - (Math.sin(radian) * mMaxTextHeight) / 2d);
                //根据Math.sin(radian)来更改canvas坐标系原点，然后缩放画布，使得文字高度进行缩放，形成弧形3d视觉差
                canvas.translate(0.0f, translateY);
//                canvas.scale(1.0F, (float) Math.sin(radian));
                if (translateY <= mFirstLineY && mMaxTextHeight + translateY >= mFirstLineY) {
                    // 条目经过第一条线
                    canvas.save();
                    canvas.clipRect(0, 0, mMeasuredWidth, mFirstLineY - translateY);
                    canvas.scale(1.0f, (float) Math.sin(radian) * SCALE_CONTENT);
                    canvas.drawText(contentText, mDrawOuterContentStart, mMaxTextHeight, mOuterTextPaint);
                    canvas.restore();
                    canvas.save();
                    canvas.clipRect(0, mFirstLineY - translateY, mMeasuredWidth, (int) (mItemHeight));
                    canvas.scale(1.0f, (float) Math.sin(radian) * 1.0f);
                    canvas.drawText(contentText, mDrawCenterContentStart, mMaxTextHeight - CENTER_CONTENT_OFFSET, mCenterTextPaint);
                    canvas.restore();
                } else if (translateY <= mSecondLineY && mMaxTextHeight + translateY >= mSecondLineY) {
                    // 条目经过第二条线
                    canvas.save();
                    canvas.clipRect(0, 0, mMeasuredWidth, mSecondLineY - translateY);
                    canvas.scale(1.0f, (float) Math.sin(radian) * 1.0f);
                    canvas.drawText(contentText, mDrawCenterContentStart, mMaxTextHeight - CENTER_CONTENT_OFFSET, mCenterTextPaint);
                    canvas.restore();
                    canvas.save();
                    canvas.clipRect(0, mSecondLineY - translateY, mMeasuredWidth, (int) (mItemHeight));
                    canvas.scale(1.0f, (float) Math.sin(radian) * SCALE_CONTENT);
                    canvas.drawText(contentText, mDrawOuterContentStart, mMaxTextHeight, mOuterTextPaint);
                    canvas.restore();
                } else if (translateY >= mFirstLineY && mMaxTextHeight + translateY <= mSecondLineY) {
                    // 中间条目
                    // canvas.clipRect(0, 0, mMeasuredWidth, mMaxTextHeight);
                    //让文字居中
                    float Y = mMaxTextHeight - CENTER_CONTENT_OFFSET;//因为圆弧角换算的向下取值，导致角度稍微有点偏差，加上画笔的基线会偏上，因此需要偏移量修正一下
                    mIndicatorPaint.setColor(0x77ffffff);
                    int dp8 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics());
                    canvas.drawRect(0, -dp8, mMeasuredWidth, mMaxTextHeight +dp8, mIndicatorPaint);
                    canvas.drawText(contentText, mDrawCenterContentStart, Y, mCenterTextPaint);
                    //设置选中项
                    mSelectedItem = mPreCurrentIndex - (mVisibleItemNums / 2 - count);
                } else {
                    // 其他条目
                    canvas.save();
                    canvas.clipRect(0, 0, mMeasuredWidth, (int) (mItemHeight));
                    canvas.scale(1.0F, (float) Math.sin(radian) * SCALE_CONTENT);
                    // 控制文字倾斜角度
                    mOuterTextPaint.setTextSkewX((mTextXOffset == 0 ? 0 : (mTextXOffset > 0 ? 1 : -1)) * (angle > 0 ? -1 : 1) * DEFAULT_TEXT_TARGET_SKEW_X * offsetCoefficient);
                    // 控制透明度
                    mOuterTextPaint.setAlpha((int) ((1 - offsetCoefficient) * 255));
                    // 控制文字水平偏移距离
                    canvas.drawText(contentText, mDrawOuterContentStart + mTextXOffset * offsetCoefficient, mMaxTextHeight, mOuterTextPaint);
                    canvas.restore();
                }
                canvas.restore();
                mCenterTextPaint.setTextSize(mTextSize);
            }
            count++;
        }
    }

    /**
     * reset the size of the text Let it can fully display
     *
     * @param contentText item text content.
     */
    private void performMeasureTextSize(String contentText) {
        Rect rect = new Rect();
        mCenterTextPaint.getTextBounds(contentText, 0, contentText.length(), rect);
        int width = rect.width();
        int size = mTextSize;
        while (width > mMeasuredWidth) {
            size--;
            //设置2条横线中间的文字大小
            mCenterTextPaint.setTextSize(size);
            mCenterTextPaint.getTextBounds(contentText, 0, contentText.length(), rect);
            width = rect.width();
        }
        //设置2条横线外面的文字大小
        mOuterTextPaint.setTextSize(size);
    }

    private int getLoopMappingIndex(int index) {
        if (index < 0) {
            index = index + mAdapter.getItemCount();
            index = getLoopMappingIndex(index);
        } else if (index > mAdapter.getItemCount() - 1) {
            index = index - mAdapter.getItemCount();
            index = getLoopMappingIndex(index);
        }
        return index;
    }

    private String getContentText(Object item) {
        if (item == null) {
            return "";
        } else if (item instanceof IPickerViewData) {
            return ((IPickerViewData) item).getPickerViewText();
        } else if (item instanceof Integer) {
            //如果为整形则最少保留两位数.
            return getFixNum((int) item);
        }
        return item.toString();
    }

    private String getFixNum(int timeNum) {
        return timeNum >= 0 && timeNum < 10 ? TIME_NUM[timeNum] : String.valueOf(timeNum);
    }

    private void measureCenterContentStart(String content) {
        Rect rect = new Rect();
        mCenterTextPaint.getTextBounds(content, 0, content.length(), rect);
        switch (mGravity) {
            case Gravity.CENTER://显示内容居中
                if (mOptions || mUnitLabel == null || mUnitLabel.equals("") || !mCenterLabel) {
                    mDrawCenterContentStart = (int) ((mMeasuredWidth - rect.width()) * 0.5);
                } else {//只显示中间label时，时间选择器内容偏左一点，留出空间绘制单位标签
                    mDrawCenterContentStart = (int) ((mMeasuredWidth - rect.width()) * 0.25);
                }
                break;
            case Gravity.LEFT:
                mDrawCenterContentStart = 0;
                break;
            case Gravity.RIGHT://添加偏移量
                mDrawCenterContentStart = mMeasuredWidth - rect.width() - (int) CENTER_CONTENT_OFFSET;
                break;
        }
    }

    private void measureOuterContentStart(String content) {
        Rect rect = new Rect();
        mOuterTextPaint.getTextBounds(content, 0, content.length(), rect);
        switch (mGravity) {
            case Gravity.CENTER:
                if (mOptions || mUnitLabel == null || mUnitLabel.equals("") || !mCenterLabel) {
                    mDrawOuterContentStart = (int) ((mMeasuredWidth - rect.width()) * 0.5);
                } else {//只显示中间label时，时间选择器内容偏左一点，留出空间绘制单位标签
                    mDrawOuterContentStart = (int) ((mMeasuredWidth - rect.width()) * 0.25);
                }
                break;
            case Gravity.LEFT:
                mDrawOuterContentStart = 0;
                break;
            case Gravity.RIGHT:
                mDrawOuterContentStart = mMeasuredWidth - rect.width() - (int) CENTER_CONTENT_OFFSET;
                break;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.mWidthMeasureSpec = widthMeasureSpec;
        performMeasure();
        setMeasuredDimension(mMeasuredWidth, mMeasuredHeight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean eventConsumed = mGestureDetector.onTouchEvent(event);
        boolean isIgnore = false;//超过边界滑动时，不再绘制UI
        float top = -mPosition * mItemHeight;
        float bottom = (mAdapter.getItemCount() - 1 - mPosition) * mItemHeight;
        float ratio = 0.25f;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartTime = System.currentTimeMillis();
                cancelFuture();
                mPreviousY = event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                float dy = mPreviousY - event.getRawY();
                mPreviousY = event.getRawY();
                mTotalScrollY = mTotalScrollY + dy;

                // normal mode。
                if (!mLoop) {
                    if ((mTotalScrollY - mItemHeight * ratio < top && dy < 0)
                            || (mTotalScrollY + mItemHeight * ratio > bottom && dy > 0)) {
                        //快滑动到边界了，设置已滑动到边界的标志
                        mTotalScrollY -= dy;
                        isIgnore = true;
                    } else {
                        isIgnore = false;
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            default:
                if (!eventConsumed) {//未消费掉事件
                    /**
                     *@describe <关于弧长的计算>
                     *
                     * 弧长公式： L = α*R
                     * 反余弦公式：arccos(cosα) = α
                     * 由于之前是有顺时针偏移90度，
                     * 所以实际弧度范围α2的值 ：α2 = π/2-α    （α=[0,π] α2 = [-π/2,π/2]）
                     * 根据正弦余弦转换公式 cosα = sin(π/2-α)
                     * 代入，得： cosα = sin(π/2-α) = sinα2 = (R - y) / R
                     * 所以弧长 L = arccos(cosα)*R = arccos((R - y) / R)*R
                     */
                    float y = event.getY();
                    double L = Math.acos((mRadius - y) / mRadius) * mRadius;
                    //item0 有一半是在不可见区域，所以需要加上 mItemHeight / 2
                    int circlePosition = (int) ((L + mItemHeight / 2) / mItemHeight);
                    float extraOffset = (mTotalScrollY % mItemHeight + mItemHeight) % mItemHeight;
                    //已滑动的弧长值
                    mOffset = (int) ((circlePosition - mVisibleItemNums / 2) * mItemHeight - extraOffset);

                    if ((System.currentTimeMillis() - mStartTime) > 120) {
                        // 处理拖拽事件
                        smoothScroll(ACTION.DAGGER);
                    } else {
                        // 处理条目点击事件
                        smoothScroll(ACTION.CLICK);
                    }
                }
                break;
        }
        if (!isIgnore && event.getAction() != MotionEvent.ACTION_DOWN) {
            invalidate();
        }
        return true;
    }

    public int getItemsCount() {
        return mAdapter != null ? mAdapter.getItemCount() : 0;
    }

    public void setUnitLabel(String unitLabel) {
        this.mUnitLabel = unitLabel;
    }

    public void isCenterLabel(boolean isCenterLabel) {
        this.mCenterLabel = isCenterLabel;
    }

    public void setGravity(int gravity) {
        this.mGravity = gravity;
    }

    public int getTextWidth(Paint paint, String str) { //calculate text width
        int iRet = 0;
        if (str != null && str.length() > 0) {
            int len = str.length();
            float[] widths = new float[len];
            paint.getTextWidths(str, widths);
            for (int j = 0; j < len; j++) {
                iRet += (int) Math.ceil(widths[j]);
            }
        }
        return iRet;
    }

    public void setIsOptions(boolean options) {
        mOptions = options;
    }

    public void setOuterTextColor(int color) {

        this.mOuterTextColor = color;
        mOuterTextPaint.setColor(this.mOuterTextColor);
    }

    public void setCenterTextColor(int color) {
        this.mCenterTextColor = color;
        mCenterTextPaint.setColor(this.mCenterTextColor);
    }

    public void setTextXOffset(int offset) {
        this.mTextXOffset = offset;
        if (mTextXOffset != 0) {
            mCenterTextPaint.setTextScaleX(1.0f);
        }
    }

    public void setDividerColor(int color) {
        this.mDividerColor = color;
        mIndicatorPaint.setColor(color);
    }

    public void setDividerType(DividerType dividerType) {
        this.mDividerType = dividerType;
    }

    public void setLineSpacingMultiplier(float lineSpacingMultiplier) {
        if (mLineSpacingMultiplier != 0) {
            this.mLineSpacingMultiplier = lineSpacingMultiplier;
            judgeLineSpace();
        }
    }

    public boolean isLoop() {
        return mLoop;
    }

    public float getTotalScrollY() {
        return mTotalScrollY;
    }

    public void setTotalScrollY(float mTotalScrollY) {
        this.mTotalScrollY = mTotalScrollY;
    }

    public float getItemHeight() {
        return mItemHeight;
    }

    public int getPosition() {
        return mPosition;
    }

    @Override
    public Handler getHandler() {
        return mHandler;
    }
}