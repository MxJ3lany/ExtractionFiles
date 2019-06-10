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

import java.text.DecimalFormat;

/**
 * 数学相关工具箱
 */
public class MathUtils {
    /**
     * 勾股定理
     *
     * @param value1
     * @param value2
     * @return
     */
    public static final double pythagoreanProposition(double value1, double value2) {
        return Math.sqrt((value1 * value1) + (value2 * value2));
    }

    /**
     * 计算百分比
     *
     * @param value1                  值1
     * @param value2                  值2
     * @param decimalPointAfterLength 小数点位数
     * @param replaceWithZero         当小数点位数不足时是否使用0代替
     * @param removePercent           删除字符串末尾的百分号
     * @return
     */
    public static String percent(double value1, double value2, int decimalPointAfterLength, boolean replaceWithZero, boolean removePercent) {
        StringBuffer buffString = new StringBuffer();
        buffString.append("#");
        if (decimalPointAfterLength > 0) {
            buffString.append(".");
        }
        for (int w = 0; w < decimalPointAfterLength; w++) {
            buffString.append(replaceWithZero ? "0" : "#");
        }
        buffString.append("%");
        if (buffString.length() > 0) {
            String result = new DecimalFormat(buffString.toString()).format(value1 / value2);
            if (removePercent && result.length() > 0) {
                result = result.substring(0, result.length() - 1);
            }
            return result;
        } else {
            return null;
        }
    }

    /**
     * 计算百分比
     *
     * @param value1                  值1
     * @param value2                  值2
     * @param decimalPointAfterLength 小数点位数
     * @param replaceWithZero         当小数点位数不足时是否使用0代替
     * @return
     */
    public static String percent(double value1, double value2, int decimalPointAfterLength, boolean replaceWithZero) {
        return percent(value1, value2, decimalPointAfterLength, replaceWithZero, false);
    }

    /**
     * 计算百分比
     *
     * @param value1                  值1
     * @param value2                  值2
     * @param decimalPointAfterLength 小数点位数
     * @return 例如：25.22%
     */
    public static String percent(double value1, double value2, int decimalPointAfterLength) {
        return percent(value1, value2, decimalPointAfterLength, false, false);
    }

    /**
     * 计算百分比
     *
     * @param value1 值1
     * @param value2 值2
     * @return 例如：25%
     */
    public static String percent(double value1, double value2) {
        return percent(value1, value2, 0, false, false);
    }
}