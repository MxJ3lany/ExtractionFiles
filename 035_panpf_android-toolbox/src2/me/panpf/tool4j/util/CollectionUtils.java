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

package me.panpf.tool4j.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import me.panpf.tool4j.lang.StringUtils;

/**
 * 集合工具类
 */
public class CollectionUtils {
    /**
     * 把给定的集合按给定的方式排序
     *
     * @param list       给定的集合
     * @param comparator 判断大小
     * @param startIndex 起始索引（包括）
     * @param endIndex   结束索引（包括）
     * @param ascending  true：升序；false：降序
     */
    public static <T> void sort(List<T> list, Comparator<? super T> comparator, int startIndex, int endIndex, boolean ascending) {
        Stack<Integer> sa = new Stack<Integer>();
        sa.push(startIndex);
        sa.push(endIndex);
        while (!sa.isEmpty()) {
            int end = ((Integer) sa.pop()).intValue();
            int start = ((Integer) sa.pop()).intValue();
            int i = start;
            int j = end;
            T tmp = list.get(i);
            if (ascending) {
                while (i < j) {
                    while (comparator.compare(list.get(j), tmp) > 0 && i < j) {
                        j--;
                    }
                    if (i < j) {
                        list.set(i, list.get(j));
                        i++;
                    }
                    for (; comparator.compare(list.get(i), tmp) < 0 && i < j; i++) ;
                    if (i < j) {
                        list.set(j, list.get(i));
                        j--;
                    }
                }
            } else {
                while (i < j) {
                    while (comparator.compare(list.get(j), tmp) < 0 && i < j)
                        j--;
                    if (i < j) {
                        list.set(i, list.get(j));
                        i++;
                    }
                    for (; comparator.compare(list.get(i), tmp) > 0 && i < j; i++) ;
                    if (i < j) {
                        list.set(j, list.get(i));
                        j--;
                    }
                }
            }

            list.set(i, tmp);

            if (start < i - 1) {
                sa.push(Integer.valueOf(start));
                sa.push(Integer.valueOf(i - 1));
            }
            if (end > i + 1) {
                sa.push(Integer.valueOf(i + 1));
                sa.push(Integer.valueOf(end));
            }
        }
    }

    /**
     * 把给定的集合按正序的方式排序
     *
     * @param list       给定的集合
     * @param comparator 判断大小
     * @param startIndex 起始索引（包括）
     * @param endIndex   结束索引（包括）
     */
    public static <T> void sort(List<T> list, Comparator<? super T> comparator, int startIndex, int endIndex) {
        sort(list, comparator, startIndex, endIndex, true);
    }

    /**
     * 把给定的集合按给定的方式排序
     *
     * @param list       给定的集合
     * @param comparator 判断大小
     * @param ascending  true：升序；false：降序
     */
    public static <T> void sort(List<T> list, Comparator<? super T> comparator, boolean ascending) {
        sort(list, comparator, 0, list.size() - 1, ascending);
    }

    /**
     * 把给定的集合按正序的方式排序
     *
     * @param list       给定的集合
     * @param comparator 判断大小
     */
    public static <T> void sort(List<T> list, Comparator<? super T> comparator) {
        sort(list, comparator, 0, list.size() - 1, true);
    }

    /**
     * 将给定的集合转换成字符串
     *
     * @param objects      给定的集合
     * @param startSymbols 开始符号
     * @param separator    分隔符
     * @param endSymbols   结束符号
     * @return 例如开始符号为"{"，分隔符为", "，结束符号为"}"，那么结果为：{你好, 我好, 大家好}
     */
    public static String toString(Collection<?> objects, String startSymbols, String separator, String endSymbols) {
        boolean addSeparator = false;
        StringBuffer sb = new StringBuffer();
        //如果开始符号不为null且不空
        if (StringUtils.isNotEmpty(startSymbols)) {
            sb.append(startSymbols);
        }

        //循环所有的对象
        for (Object object : objects) {
            //如果需要添加分隔符
            if (addSeparator) {
                sb.append(separator);
                addSeparator = false;
            }
            sb.append(object.toString());
            addSeparator = true;
        }

        //如果结束符号不为null且不空
        if (StringUtils.isNotEmpty(endSymbols)) {
            sb.append(endSymbols);
        }
        return sb.toString();
    }

    /**
     * 将给定的集合转换成字符串
     *
     * @param objects   给定的集合
     * @param separator 分隔符
     * @return 例如分隔符为", "那么结果为：你好, 我好, 大家好
     */
    public static String toString(Collection<?> objects, String separator) {
        return toString(objects, null, separator, null);
    }

    /**
     * 删除给定列表中给定索引数组的对象
     *
     * @param list   给定列表
     * @param indexs 给定索引数组
     * @return 被删除的对象
     */
    public static Object[] removes(Collection<?> list, int... indexs) {
        Object[] objects = null;
        if (list != null && !list.isEmpty() && indexs != null && indexs.length > 0) {
            objects = new Object[indexs.length];
            int r = 0;
            //首先对索引进行降序排序
            ArrayUtils.sortingByFastStack(indexs, false);
            for (int w : indexs) {
                if (w < list.size()) {
                    objects[r++] = list.remove(w);
                }
            }
        }
        return objects;
    }
}