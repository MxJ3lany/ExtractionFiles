package com.dev.utils.text;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import dev.DevUtils;
import dev.utils.app.TextViewUtils;
import dev.utils.app.logger.DevLogger;

/**
 * detail: 计算字体宽度、高度
 * @author Ttt
 */
public final class TextCalcUse {

    private TextCalcUse() {
    }

    // 日志 TAG
    private static final String TAG = TextCalcUse.class.getSimpleName();

    /**
     * 计算字体宽度、高度
     */
    protected void textCalcUse() {
        LinearLayout vid_linear = null;
        // 打印信息
        for (int i = 0, len = vid_linear.getChildCount(); i < len; i++) {
            View view = vid_linear.getChildAt(i);
            if (view != null && view instanceof TextView) {
                printInfo((TextView) view);
            }
        }

//        // 计算第几位超过宽度(600)
//        int pos = TextViewUtils.calcTextWidth(vid_tv.getPaint(), "测试内容", 600);

        TextView tv = new TextView(DevUtils.getContext());
        // 获取字体高度
        TextViewUtils.getTextHeight(tv);
        // 获取字体大小
        TextViewUtils.reckonTextSize(90); // 获取字体高度为90的字体大小
    }

    // =

    /**
     * 打印信息
     * @param textView
     */
    private void printInfo(TextView textView) {
        StringBuilder builder = new StringBuilder();
        builder.append("\n内容: " + textView.getText().toString());
        builder.append("\n高度: " + TextViewUtils.getTextHeight(textView));
        builder.append("\n偏移高度: " + TextViewUtils.getTextTopOffsetHeight(textView));
        builder.append("\n宽度: " + TextViewUtils.getTextWidth(textView));
        builder.append("\n字体大小: " + textView.getTextSize());
        builder.append("\n计算字体大小: " + TextViewUtils.reckonTextSize(TextViewUtils.getTextHeight(textView)));
        // 打印日志
        DevLogger.dTag(TAG, builder.toString());
    }
}
