package com.lwh.jackknife.av.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;

import com.lwh.jackknife.av.bean.Lyric;
import com.lwh.jackknife.av.bean.Sentence;

import java.util.ArrayList;
import java.util.List;

public class LyricView extends ScrollView {

    private Lyric mLyric;
    private List<Sentence> mSentences;
    private List<TextView> mTextViews;
    private final LinearLayout.LayoutParams mLinearParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    private final LayoutParams mTextParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DY);
    private Scroller mScroller;
    private int mIndex = 0;
    private float midHeight;
    private static final int DY = 30; // 每一行的间隔
    private static final float fontSize = 16;

    public LyricView(Context context) {
        super(context);
        init();
    }

    public LyricView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public LyricView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /***
     * 配合Scroller 进行动态滚动。
     */
    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            this.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            this.postInvalidate();
        }
    }

    public void setLyric(Lyric lyric) {
        this.mLyric = lyric;
        LayoutParams spaceParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) midHeight);
        this.mSentences = mLyric.getSentences();
        LinearLayout layout = new LinearLayout(this.getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(mLinearParams);
        View spaceView = new View(layout.getContext());
        spaceView.setLayoutParams(spaceParams);
        layout.addView(spaceView);
        for (Sentence sentence : this.mSentences) {
            TextView textView = new TextView(layout.getContext());
            textView.setText(sentence.getContent());
            textView.setVisibility(VISIBLE);
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(Color.WHITE);
            textView.setLayoutParams(mTextParams);
            mTextViews.add(textView);
            layout.addView(textView);
        }
        spaceView = new View(layout.getContext());
        spaceView.setLayoutParams(spaceParams);
        layout.addView(spaceView);
        layout.setVisibility(VISIBLE);
        this.addView(layout);
    }

    public void clear() {
        this.removeAllViews();
        mTextViews.clear();
        this.mSentences = null;
        this.scrollTo(0, 0);
        mIndex = 0;
    }

    private void init() {
        mTextViews = new ArrayList<>();
        mScroller = new Scroller(this.getContext());
    }

    /**
     * 更新歌词序列。
     */
    public void updateIndex(long time) {
        if (mLyric == null) return;
        // 歌词序号
        int t = mLyric.getCurSentenceIndex(time);
        if (mIndex == -1)
            return;
        if (mIndex != t) {
            TextView oldOne = mTextViews.get(mIndex);
            oldOne.setTextColor(Color.WHITE);
            TextView newOne = mTextViews.get(t);
            newOne.setTextColor(Color.YELLOW);
            int oldHeight = oldOne.getTop();
            mIndex = t;
            mScroller.startScroll(this.getScrollX(), (int) (oldHeight + fontSize - midHeight + DY / 2), 0, 30, 800);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        midHeight = h * 0.5f;
    }

    /**
     * 用于禁止手动滚动歌词。
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }
}