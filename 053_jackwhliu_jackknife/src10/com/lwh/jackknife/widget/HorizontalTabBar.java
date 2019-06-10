/*
 * Copyright (C) 2017 The JackKnife Open Source Project
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

package com.lwh.jackknife.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class HorizontalTabBar extends HorizontalScrollView {

    /**
     * Current selected location.
     */
    private int mPosition;

    /**
     * The current selected position offset.
     */
    private float mPositionOffset;

    /**
     * Default layout parameters, wrap tab content size.
     */
    private LinearLayout.LayoutParams mWrapTabLayoutParams;

    /**
     * If the screen width is greater than the width of all tabs, then divide tabs{@literal '} total
     * width to layout.
     */
    private LinearLayout.LayoutParams mAverageTabLayoutParams;

    /**
     * Store all tabs{@literal '} containers.
     */
    private LinearLayout mTabContainer;

    /**
     * The text size of the tab.
     */
    private float mTabTextSize;

    /**
     * Default tab text color.
     */
    private final int DEFAULT_TAB_TEXT_COLOR = Color.BLACK;

    /**
     * The text color of the tab.
     */
    private int mTabTextColor;

    /**
     * The height of the indicator.
     */
    private int mIndicatorHeight;

    /**
     * Default indicator color, it{@literal '} s far too ugly, so you can invoke
     * {@link #setIndicatorColor(int)} method making it beautiful.
     */
    private final int DEFAULT_INDICATOR_COLOR = 0xFFFFA500;

    /**
     * The color of the indicator.
     */
    private int mIndicatorColor;

    /**
     * The height of the underline.
     */
    private int mUnderlineHeight;

    /**
     * Default underline color.
     */
    private final int DEFAULT_UNDERLINE_COLOR = Color.TRANSPARENT;

    /**
     * The color of the underline.
     */
    private int mUnderlineColor;

    /**
     * The width of the dividing line.
     */
    private int mDividerWidth;

    /**
     * Default dividing line color.
     */
    private final int DEFAULT_DIVIDER_COLOR = Color.TRANSPARENT;

    /**
     * The color of the dividing line.
     */
    private int mDividerColor;

    /**
     * The top-padding of the dividing line.
     */
    private int mDividerPaddingTop;

    /**
     * The bottom-padding of the dividing line.
     */
    private int mDividerPaddingBottom;

    /**
     * The left-padding of the tab.
     */
    private int mTabPaddingLeft;

    /**
     * The right-padding of the tab.
     */
    private int mTabPaddingRight;

    /**
     * Default tab background color.
     */
    private final int DEFAULT_TAB_COLOR = Color.TRANSPARENT;

    /**
     * The background color of the tab.
     */
    private int mTabColor;

    /**
     * The number of tabs.
     */
    private int mTabCount;

    /**
     * The position of the selected tab.
     */
    private int mSelectedPosition;

    /**
     * Default selected tab{@literal '} text color.
     */
    private final int DEFAULT_SELECTED_TAB_TEXT_COLOR = 0xFFFFA500;

    /**
     * The text color of the selected tab.
     */
    private int mSelectedTabTextColor;

    /**
     * The typeface of the tab.
     */
    private Typeface mTabTypeface;

    /**
     * The typeface style of the tab.
     */
    private int mTabTypefaceStyle = Typeface.NORMAL;

    /**
     * All letters are automatically to upper case.
     */
    private boolean mTextAllCaps;

    /**
     * Look up {@link #mAverageTabLayoutParams}.
     */
    private boolean mAverage;

    /**
     * The paint of the indicator and underline{@literal '}s.
     */
    private Paint mRectPaint;

    /**
     * The paint of the dividing line.
     */
    private Paint mDividerPaint;

    /**
     * Interface definition for a callback to be invoked when the {@link HorizontalTabBar}
     * {@literal '}s tab is clicked.
     */
    private OnTabClickListener mOnTabClickListener;

    /**
     * The all titles of tabs.
     */
    private List<String> mTabTitles;

    /**
     * The x coordinate last time scrolled to stay.
     */
    private int mScrollX;

    /**
     * The saved instance state.
     */
    private static final String STATE_INSTANCE = "state_instance";

    /**
     * The current position state.
     */
    private static final String STATE_POSITION = "state_position";

    /**
     * The rectangular area of the underline.
     */
    private RectF mUnderlineRect;

    /**
     * The rectangular area of the indicator.
     */
    private RectF mIndicatorRect;

    /**
     * A structure describing general information about a display, such as its
     * size, density, and font scaling.
     */
    private DisplayMetrics mMetrics;

    /**
     * {@code Locale} represents a language/country/variant combination. Locales are used to
     * alter the presentation of information such as numbers or dates to suit the conventions
     * in the region they describe.
     */
    private Locale mLocale;

    public HorizontalTabBar(Context context) {
        this(context, null);
    }

    public HorizontalTabBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.horizontalTabBarStyle);
    }

    public HorizontalTabBar(Context context, AttributeSet attrs,
                            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        setFillViewport(true);
        setHorizontalScrollBarEnabled(false);
        mAverageTabLayoutParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        mWrapTabLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mMetrics = getResources().getDisplayMetrics();
        mLocale = getResources().getConfiguration().locale;
        initAttrs(context, attrs, defStyleAttr);
        initRects();
        initPaints();
        initTabContainer();
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        mIndicatorHeight = (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, mMetrics);
        mUnderlineHeight = (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, mMetrics);
        mDividerWidth = (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, mMetrics);
        mDividerPaddingTop = (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, mMetrics);
        mDividerPaddingBottom = (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, mMetrics);
        mTabPaddingLeft = (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, mMetrics);
        mTabPaddingRight = (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, mMetrics);
        mTabTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, mMetrics);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HorizontalTabBar,
                defStyleAttr, 0);
        mIndicatorColor = a.getColor(R.styleable.HorizontalTabBar_horizontaltabbar_indicatorColor,
                DEFAULT_INDICATOR_COLOR);
        mUnderlineColor = a.getColor(R.styleable.HorizontalTabBar_horizontaltabbar_underlineColor,
                DEFAULT_UNDERLINE_COLOR);
        mDividerColor = a.getColor(R.styleable.HorizontalTabBar_horizontaltabbar_dividerColor,
                DEFAULT_DIVIDER_COLOR);
        mIndicatorHeight = a.getDimensionPixelSize(
                R.styleable.HorizontalTabBar_horizontaltabbar_indicatorHeight, mIndicatorHeight);
        mUnderlineHeight = a.getDimensionPixelSize(
                R.styleable.HorizontalTabBar_horizontaltabbar_underlineHeight, mUnderlineHeight);
        mDividerPaddingTop = a.getDimensionPixelSize(
                R.styleable.HorizontalTabBar_horizontaltabbar_dividerPaddingTop,
                mDividerPaddingTop);
        mDividerPaddingBottom = a.
                getDimensionPixelSize(
                        R.styleable.HorizontalTabBar_horizontaltabbar_dividerPaddingBottom,
                        mDividerPaddingBottom);
        mTabPaddingLeft = a.getDimensionPixelSize(
                R.styleable.HorizontalTabBar_horizontaltabbar_tabPaddingLeft, mTabPaddingLeft);
        mTabPaddingRight = a.getDimensionPixelSize(
                R.styleable.HorizontalTabBar_horizontaltabbar_tabPaddingRight, mTabPaddingRight);
        mTabColor = a.getResourceId(R.styleable.HorizontalTabBar_horizontaltabbar_tabColor,
                DEFAULT_TAB_COLOR);
        mTabTextColor = a.getColor(R.styleable.HorizontalTabBar_horizontaltabbar_tabTextColor,
                DEFAULT_TAB_TEXT_COLOR);
        mSelectedTabTextColor = a.getColor(
                R.styleable.HorizontalTabBar_horizontaltabbar_tabSelectedTextColor,
                DEFAULT_SELECTED_TAB_TEXT_COLOR);
        mTabTextSize = a.getDimension(
                R.styleable.HorizontalTabBar_horizontaltabbar_tabTextSize, mTabTextSize);
        mAverage = a.getBoolean(R.styleable.HorizontalTabBar_horizontaltabbar_isAverage, mAverage);
        mTextAllCaps = a.getBoolean(R.styleable.HorizontalTabBar_horizontaltabbar_textAllCaps,
                mTextAllCaps);
        a.recycle();
    }

    private void initRects() {
        mUnderlineRect = new RectF();
        mIndicatorRect = new RectF();
    }

    private void initPaints() {
        mRectPaint = new Paint();
        mRectPaint.setAntiAlias(true);
        mRectPaint.setStyle(Style.FILL);
        mDividerPaint = new Paint();
        mDividerPaint.setAntiAlias(true);
        mDividerPaint.setStrokeWidth(mDividerWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode() || mTabCount == 0) {
            return;
        }
        int height = getHeight();
        drawUnderline(height, canvas);
        drawIndicator(height, canvas);
        drawDivider(height, canvas);
    }

    /**
     * Draw divider, divider is vertical lines between tabs.
     */
    private void drawDivider(int height, Canvas canvas) {
        mDividerPaint.setColor(mDividerColor);
        for (int i = 0; i < mTabCount - 1; i++) {
            View tab = mTabContainer.getChildAt(i);
            canvas.drawLine(tab.getRight(), mDividerPaddingTop, tab.getRight(), height -
                    mDividerPaddingBottom, mDividerPaint);
        }
    }

    /**
     * Draw underline, underline is under all tabs.
     */
    private void drawUnderline(int height, Canvas canvas) {
        mRectPaint.setColor(mUnderlineColor);
        mUnderlineRect.set(0, height - mUnderlineHeight, getTabContainerWidth(), height);
        canvas.drawRect(mUnderlineRect, mRectPaint);
    }

    /**
     * Draw indicator, indicator is traveling in underline while their height is same.
     */
    private void drawIndicator(int height, Canvas canvas) {
        mRectPaint.setColor(mIndicatorColor);
        View currentTab = mTabContainer.getChildAt(mPosition);
        float lineLeft = currentTab.getLeft();
        float lineRight = currentTab.getRight();
        if (mPositionOffset > 0.0f && mPosition < mTabCount - 1) {
            View nextTab = mTabContainer.getChildAt(mPosition + 1);
            float nextTabLeft = nextTab.getLeft();
            float nextTabRight = nextTab.getRight();
            lineLeft = (mPositionOffset * nextTabLeft + (1.0f - mPositionOffset) * lineLeft);
            lineRight = (mPositionOffset * nextTabRight + (1.0f - mPositionOffset) * lineRight);
        }
        mIndicatorRect.set(lineLeft, height - mIndicatorHeight, lineRight, height);
        canvas.drawRect(mIndicatorRect, mRectPaint);
    }

    /**
     * Initializes the Tab title, if you not invoke the method, there are no display on layout.
     */
    public void setTitles(String[] titles) {
        if (mTabTitles == null) {
            mTabTitles = new ArrayList<>();
        }
        if (titles != null && titles.length != 0) {
            mTabTitles.addAll(Arrays.asList(titles));
            notifyDataSetChanged();
        }
    }

    /**
     * Gets all titles of all tabs.
     */
    public String[] getTitles() {
        if (mTabTitles != null && mTabTitles.size() > 0) {
            return (String[]) mTabTitles.toArray();
        }
        return null;
    }

    /**
     * Check whether the position belongs to a title index.
     */
    private boolean isInScope(int position) {
        return mTabTitles != null && mTabTitles.size() > 0 && position >= 0
                && position < mTabTitles.size();
    }

    /**
     * Gets the Tab title of the specified location.
     */
    public String getTabTilte(int position) {
        if (isInScope(position)) {
            return mTabTitles.get(position);
        }
        return null;
    }

    /**
     * Gets the width of the tab container.
     */
    public int getTabContainerWidth() {
        return mTabContainer.getWidth();
    }

    /**
     * Check that all letters are automatically converted to capital letters.
     */
    public boolean isTextAllCaps() {
        return mTextAllCaps;
    }

    /**
     * True represents converting all letters to upper case, false ignore.
     */
    public void setTextAllCaps(boolean caps) {
        if (caps != mTextAllCaps) {
            this.mTextAllCaps = caps;
            refreshTabs();
        }
    }

    /**
     * Gets the selected position.
     */
    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    /**
     * Interface definition for a callback to be invoked when the {@link HorizontalTabBar}
     * {@literal '}s tab is clicked.
     */
    public interface OnTabClickListener {

        /**
         * @return True represents consumption events, such as when used with ViewPager, false
         * otherwise.
         */
        boolean onTabClick(View view, int position);
    }

    /**
     * Set an observer for {@link HorizontalTabBar}.
     */
    public void setOnTabClickListener(OnTabClickListener l) {
        this.mOnTabClickListener = l;
    }

    /**
     * When the property of the tab changes, the style of each tab changes.
     */
    public void refreshTabs() {
        for (int i = 0; i < mTabCount; i++) {
            View v = mTabContainer.getChildAt(i);
            v.setBackgroundColor(mTabColor);
            if (v instanceof TextView) {
                TextView tab = (TextView) v;
                tab.setTextSize(mTabTextSize);
                tab.setTypeface(mTabTypeface, mTabTypefaceStyle);
                tab.setTextColor(mTabTextColor);
                if (mTextAllCaps) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        tab.setAllCaps(true);
                    } else {
                        tab.setText(tab.getText().toString().toUpperCase(mLocale));
                    }
                }
                if (i == mSelectedPosition) {
                    tab.setTextColor(mSelectedTabTextColor);
                }
            }
        }
    }

    private void initTabContainer() {
        mTabContainer = new LinearLayout(getContext());
        mTabContainer.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        mTabContainer.setOrientation(LinearLayout.HORIZONTAL);
        addView(mTabContainer);
    }

    public int getPosition() {
        return mPosition;
    }

    public float getPositionOffset() {
        return mPositionOffset;
    }

    /**
     * You can invoke the method in a scrollable container{@literal '}s callback method.
     */
    public void setSelectedPosition(int position) {
        if (position != mSelectedPosition) {
            mSelectedPosition = position;
            refreshTabs();
        }
    }

    /**
     * You can invoke the method in a scrollable container{@literal '}s callback method.
     */
    public void setPositionOffset(int position, float positionOffset) {
        if (position != mPosition || positionOffset != mPositionOffset) {
            mPosition = position;
            mPositionOffset = positionOffset;
            scrollToChild(position, (int) (positionOffset * getTabWidth(position)));
            invalidateView();
        }
    }

    /**
     * Sets the typeface of the tab.
     */
    public void setTypeface(Typeface typeface, int style) {
        if (typeface != mTabTypeface || style != mTabTypefaceStyle) {
            this.mTabTypeface = typeface;
            this.mTabTypefaceStyle = style;
            refreshTabs();
        }
    }

    /**
     * Gets the tab container.
     */
    public LinearLayout getTabContainer() {
        return mTabContainer;
    }

    /**
     * Scroll to the location of a tab.
     */
    public void scrollToChild(int position, int offsetPixels) {
        if (mTabCount == 0) {
            return;
        }
        int width = getWidth();
        int scrollOffset = (width - getTabWidth(position)) / 2;
        int scrollX = getTabLeft(position) + offsetPixels;
        if (position > 0 || offsetPixels > 0) {
            scrollX -= scrollOffset;
        }
        if (scrollX != mScrollX) {
            this.mScrollX = scrollX;
            smoothScrollTo(scrollX, 0);
        }
    }

    /**
     * Gets the tab of the specified position.
     */
    public View getTab(int position) {
        return mTabContainer.getChildAt(position);
    }

    /**
     * Gets the tab{@literal '}s width of the specified position.
     */
    public int getTabWidth(int position) {
        return getTab(position).getWidth();
    }

    /**
     * Gets the tab{@literal '}s left x coordinate of the specified position.
     */
    public int getTabLeft(int position) {
        return getTab(position).getLeft();
    }

    /**
     * Gets the tab{@literal '}s right x coordinate of the specified position.
     */
    public int getTabRight(int position) {
        return getTab(position).getRight();
    }

    /**
     * Sets the indicator color.
     */
    public void setIndicatorColor(int color) {
        if (color != mIndicatorColor) {
            this.mIndicatorColor = color;
            invalidateView();
        }
    }

    /**
     * Sets the indicator color.
     */
    public void setIndicatorColorResource(int resId) {
        setIndicatorColor(getResources().getColor(resId));
    }

    /**
     * Gets the indicator color.
     */
    public int getIndicatorColor() {
        return this.mIndicatorColor;
    }

    /**
     * Sets the indicator height.
     */
    public void setIndicatorHeight(int height) {
        if (height != mIndicatorHeight) {
            this.mIndicatorHeight = height;
            invalidateView();
        }
    }

    /**
     * Gets the indicator height.
     */
    public int getIndicatorHeight() {
        return mIndicatorHeight;
    }

    /**
     * Sets the underline color.
     */
    public void setUnderlineColor(int color) {
        if (color != mUnderlineColor) {
            this.mUnderlineColor = color;
            invalidateView();
        }
    }

    /**
     * Sets the underline color.
     */
    public void setUnderlineColorResource(int resId) {
        setUnderlineColor(getResources().getColor(resId));
    }

    /**
     * Gets the underline color.
     */
    public int getUnderlineColor() {
        return mUnderlineColor;
    }

    /**
     * Sets the divider color.
     */
    public void setDividerColor(int color) {
        if (color != mDividerColor) {
            this.mDividerColor = color;
            invalidateView();
        }
    }

    /**
     * Sets the divider color.
     */
    public void setDividerColorResource(int resId) {
        setDividerColor(getResources().getColor(resId));
    }

    /**
     * Gets the divider color.
     */
    public int getDividerColor() {
        return mDividerColor;
    }

    /**
     * Sets the underline height.
     */
    public void setUnderlineHeight(int height) {
        if (height != mUnderlineHeight) {
            this.mUnderlineHeight = height;
            invalidateView();
        }
    }

    /**
     * Gets the underline height.
     */
    public int getUnderlineHeight() {
        return mUnderlineHeight;
    }

    /**
     * Sets top-padding of the divider.
     */
    public void setDividerPaddingTop(int padding) {
        if (padding != mDividerPaddingTop) {
            this.mDividerPaddingTop = padding;
            invalidateView();
        }
    }

    /**
     * Sets bottom-padding of the divider.
     */
    public void setDividerPaddingBottom(int padding) {
        if (padding != mDividerPaddingBottom) {
            this.mDividerPaddingBottom = padding;
            invalidateView();
        }
    }

    /**
     * Gets top-padding of the divider.
     */
    public int getDividerPaddingTop() {
        return mDividerPaddingTop;
    }

    /**
     * Sets bottom-padding of the divider.
     */
    public int getDividerPaddingBottom() {
        return mDividerPaddingBottom;
    }

    /**
     * Check layout parameters is {@link #mAverageTabLayoutParams}.
     */
    public boolean isAverage() {
        return mAverage;
    }

    /**
     * True represents using {@link #mAverageTabLayoutParams}, the otherwise using
     * {@link #mWrapTabLayoutParams}.
     */
    public void setAverage(boolean isAverage) {
        if (isAverage != mAverage) {
            this.mAverage = isAverage;
            notifyDataSetChanged();
        }
    }

    /**
     * Sets the text size of the tab.
     */
    public void setTabTextSize(float size) {
        if (size != mTabTextSize) {
            this.mTabTextSize = size;
            refreshTabs();
        }
    }

    /**
     * Gets the text size of the tab.
     */
    public float getTabTextSize() {
        return mTabTextSize;
    }

    /**
     * Sets the text color of the tab.
     */
    public void setTabTextColor(int color) {
        if (color != mTabTextColor) {
            this.mTabTextColor = color;
            refreshTabs();
        }
    }

    /**
     * Sets the text color of the tab.
     */
    public void setTabTextColorResource(int resId) {
        setTabTextColor(getResources().getColor(resId));
    }

    /**
     * Gets the text color of the tab.
     */
    public int getTabTextColor() {
        return mTabTextColor;
    }

    /**
     * Sets the text color of the selected tab.
     */
    public void setSelectedTextColor(int color) {
        if (color != mSelectedTabTextColor) {
            this.mSelectedTabTextColor = color;
            refreshTabs();
        }
    }

    /**
     * Sets the text color of the selected tab.
     */
    public void setSelectedTextColorResource(int resId) {
        setSelectedTextColor(getResources().getColor(resId));
    }

    /**
     * Gets the text color of the selected tab.
     */
    public int getSelectedTabTextColor() {
        return mSelectedTabTextColor;
    }

    /**
     * Sets the background color of the tab.
     */
    public void setTabColor(int color) {
        if (color != mTabColor) {
            this.mTabColor = color;
            refreshTabs();
        }
    }

    /**
     * Sets the background color of the tab.
     */
    public void setTabColorResource(int resId) {
        setTabColor(getResources().getColor(resId));
    }

    /**
     * Gets the background color of the tab.
     */
    public int getTabColor() {
        return mTabColor;
    }

    /**
     * Sets the left-padding of the tab.
     */
    public void setTabPaddingLeft(int padding) {
        if (padding != mTabPaddingLeft) {
            this.mTabPaddingLeft = padding;
            refreshTabs();
        }
    }

    /**
     * Sets the right-padding of the tab.
     */
    public void setTabPaddingRight(int padding) {
        if (padding != mTabPaddingRight) {
            this.mTabPaddingRight = padding;
            refreshTabs();
        }
    }

    /**
     * The refresh view operation of the automatic processing thread.
     */
    private void invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    /**
     * Gets the left-padding of the tab.
     */
    public int getTabPaddingLeft() {
        return mTabPaddingLeft;
    }

    /**
     * Gets the right-padding of the tab.
     */
    public int getTabPaddingRight() {
        return mTabPaddingRight;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(STATE_INSTANCE, super.onSaveInstanceState());
        bundle.putFloat(STATE_POSITION, mSelectedPosition);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable parcelable) {
        if (parcelable instanceof Bundle) {
            Bundle bundle = (Bundle) parcelable;
            mSelectedPosition = bundle.getInt(STATE_POSITION);
            super.onRestoreInstanceState(bundle.getParcelable(STATE_INSTANCE));
        } else {
            super.onRestoreInstanceState(parcelable);
        }
    }

    /**
     * Adds a tab.You can look up {@link #addTextTab(int, String)}.
     */
    private void addTab(final int position, View tab) {
        tab.setFocusable(true);
        tab.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean isSwallowed = false;
                if (mOnTabClickListener != null) {
                    isSwallowed = mOnTabClickListener.onTabClick(v, position);
                }
                if (!isSwallowed) {
                    setSelectedPosition(position);
                }
            }
        });
        tab.setPadding(mTabPaddingLeft, 0, mTabPaddingRight, mUnderlineHeight);
        mTabContainer.addView(tab, position, mAverage ? mAverageTabLayoutParams
                : mWrapTabLayoutParams);
    }

    public void addTextTab(int position, String title) {
        TextView textTab = new TextView(getContext());
        textTab.setText(title);
        textTab.setGravity(Gravity.CENTER);
        textTab.setSingleLine();
        addTab(position, textTab);
    }

    /**
     * You can invoke the method if you need to rearrange.
     */
    public void notifyDataSetChanged() {
        mTabCount = mTabTitles.size();
        mTabContainer.removeAllViews();
        if (mTabTitles != null && mTabTitles.size() > 0) {
            for (int i = 0; i < mTabTitles.size(); i++) {
                addTextTab(i, mTabTitles.get(i));
            }
        }
        refreshTabs();
        getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            public void onGlobalLayout() {
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
                scrollToChild(mPosition, 0);
            }
        });
    }
}
