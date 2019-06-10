/*
 * Copyright (C) 2017 Peng fei Pan <sky@panpf.me>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.panpf.tool4a.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import me.panpf.tool4a.widget.ToastUtils;

/**
 * 视图工具箱
 */
public class ViewUtils {
    /**
     * 获取一个LinearLayout
     *
     * @param context     上下文
     * @param orientation 流向
     * @param width       宽
     * @param height      高
     * @return LinearLayout
     */
    public static LinearLayout createLinearLayout(Context context, int orientation, int width, int height) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(orientation);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(width, height));
        return linearLayout;
    }

    /**
     * 获取一个LinearLayout
     *
     * @param context     上下文
     * @param orientation 流向
     * @param width       宽
     * @param height      高
     * @param weight      权重
     * @return LinearLayout
     */
    public static LinearLayout createLinearLayout(Context context, int orientation, int width, int height, int weight) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(orientation);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(width, height, weight));
        return linearLayout;
    }

    /**
     * 根据ListView的所有子项的高度设置其高度
     *
     * @param listView
     */
    public static void setListViewHeightByAllChildrenViewHeight(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {
            int totalHeight = 0;
            for (int i = 0; i < listAdapter.getCount(); i++) {
                View listItem = listAdapter.getView(i, null, listView);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }

            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
            ((MarginLayoutParams) params).setMargins(10, 10, 10, 10);
            listView.setLayoutParams(params);
        }
    }

    /**
     * 给给定的视图设置长按提示
     *
     * @param context     上下文
     * @param view        给定的视图
     * @param hintContent 提示内容
     */
    public static void setLongClickHint(final Context context, View view, final String hintContent) {
        view.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ToastUtils.toastS(context, hintContent);
                return true;
            }
        });
    }

    /**
     * 给给定的视图设置长按提示
     *
     * @param context       上下文
     * @param view          给定的视图
     * @param hintContentId 提示内容的ID
     */
    public static void setLongClickHint(final Context context, View view, final int hintContentId) {
        setLongClickHint(context, view, context.getString(hintContentId));
    }

    /**
     * 设置给定视图的高度
     *
     * @param view      给定的视图
     * @param newHeight 新的高度
     */
    public static void setViewHeight(View view, int newHeight) {
        ViewGroup.LayoutParams layoutParams = (ViewGroup.LayoutParams) view.getLayoutParams();
        layoutParams.height = newHeight;
        view.setLayoutParams(layoutParams);
    }

    /**
     * 将给定视图的高度增加一点
     *
     * @param view            给定的视图
     * @param increasedAmount 增加多少
     */
    public static void addViewHeight(View view, int increasedAmount) {
        ViewGroup.LayoutParams headerLayoutParams = (ViewGroup.LayoutParams) view.getLayoutParams();
        headerLayoutParams.height += increasedAmount;
        view.setLayoutParams(headerLayoutParams);
    }

    /**
     * 设置给定视图的宽度
     *
     * @param view     给定的视图
     * @param newWidth 新的宽度
     */
    public static void setViewWidth(View view, int newWidth) {
        ViewGroup.LayoutParams headerLayoutParams = (ViewGroup.LayoutParams) view.getLayoutParams();
        headerLayoutParams.width = newWidth;
        view.setLayoutParams(headerLayoutParams);
    }

    /**
     * 将给定视图的宽度增加一点
     *
     * @param view            给定的视图
     * @param increasedAmount 增加多少
     */
    public static void addViewWidth(View view, int increasedAmount) {
        ViewGroup.LayoutParams headerLayoutParams = (ViewGroup.LayoutParams) view.getLayoutParams();
        headerLayoutParams.width += increasedAmount;
        view.setLayoutParams(headerLayoutParams);
    }

    /**
     * 获取流布局的底部外边距
     *
     * @param linearLayout
     * @return
     */
    public static int getLinearLayoutBottomMargin(LinearLayout linearLayout) {
        return ((LinearLayout.LayoutParams) linearLayout.getLayoutParams()).bottomMargin;
    }

    /**
     * 设置流布局的底部外边距
     *
     * @param linearLayout
     * @param newBottomMargin
     */
    public static void setLinearLayoutBottomMargin(LinearLayout linearLayout, int newBottomMargin) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) linearLayout.getLayoutParams();
        lp.bottomMargin = newBottomMargin;
        linearLayout.setLayoutParams(lp);
    }

    /**
     * 获取流布局的高度
     *
     * @param linearLayout
     * @return
     */
    public static int getLinearLayoutHiehgt(LinearLayout linearLayout) {
        return ((LinearLayout.LayoutParams) linearLayout.getLayoutParams()).height;
    }

    /**
     * 设置输入框的光标到末尾
     *
     * @param editText
     */
    public static final void setEditTextSelectionToEnd(EditText editText) {
        Editable editable = editText.getEditableText();
        Selection.setSelection((Spannable) editable, editable.toString().length());
    }

    /**
     * 执行测量，执行完成之后只需调用View的getMeasuredXXX()方法即可获取测量结果
     *
     * @param view
     * @return
     */
    public static final View measure(View view) {
        ViewGroup.LayoutParams p = view.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        view.measure(childWidthSpec, childHeightSpec);
        return view;
    }

    /**
     * 获取给定视图的测量高度
     *
     * @param view
     * @return
     */
    public static final int getMeasuredHeight(View view) {
        return measure(view).getMeasuredHeight();
    }

    /**
     * 获取给定视图的测量宽度
     *
     * @param view
     * @return
     */
    public static final int getMeasuredWidth(View view) {
        return measure(view).getMeasuredWidth();
    }

    /**
     * 获取视图1相对于视图2的位置，注意在屏幕上看起来视图1应该被视图2包含，但是视图1和视图并不一定是绝对的父子关系也可以是兄弟关系，只是一个大一个小而已
     *
     * @param view1
     * @param view2
     * @return
     */
    public static final Rect getRelativeRect(View view1, View view2) {
        Rect childViewGlobalRect = new Rect();
        Rect parentViewGlobalRect = new Rect();
        view1.getGlobalVisibleRect(childViewGlobalRect);
        view2.getGlobalVisibleRect(parentViewGlobalRect);
        return new Rect(childViewGlobalRect.left - parentViewGlobalRect.left, childViewGlobalRect.top - parentViewGlobalRect.top, childViewGlobalRect.right - parentViewGlobalRect.left, childViewGlobalRect.bottom - parentViewGlobalRect.top);
    }

    /**
     * 删除监听器
     *
     * @param viewTreeObserver
     * @param onGlobalLayoutListener
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static final void removeOnGlobalLayoutListener(ViewTreeObserver viewTreeObserver, ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            viewTreeObserver.removeGlobalOnLayoutListener(onGlobalLayoutListener);
        } else {
            viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayoutListener);
        }
    }

    /**
     * 缩放视图
     *
     * @param view
     * @param scaleX
     * @param scaleY
     * @param originalSize
     */
    public static void zoomView(View view, float scaleX, float scaleY, Point originalSize) {
        int width = (int) (originalSize.x * scaleX);
        int height = (int) (originalSize.y * scaleY);
        ViewGroup.LayoutParams viewGroupParams = view.getLayoutParams();
        if (viewGroupParams != null) {
            viewGroupParams.width = width;
            viewGroupParams.height = height;
        } else {
            viewGroupParams = new ViewGroup.LayoutParams(width, height);
        }
        view.setLayoutParams(viewGroupParams);
    }

    /**
     * 缩放视图
     *
     * @param view
     * @param scaleX
     * @param scaleY
     */
    public static void zoomView(View view, float scaleX, float scaleY) {
        zoomView(view, scaleX, scaleY, new Point(view.getWidth(), view.getHeight()));
    }

    /**
     * 缩放视图
     *
     * @param view
     * @param scale        比例
     * @param originalSize
     */
    public static void zoomView(View view, float scale, Point originalSize) {
        zoomView(view, scale, scale, originalSize);
    }

    /**
     * 缩放视图
     *
     * @param view
     */
    public static void zoomView(View view, float scale) {
        zoomView(view, scale, scale, new Point(view.getWidth(), view.getHeight()));
    }
}