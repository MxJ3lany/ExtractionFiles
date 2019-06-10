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

package me.panpf.tool4j.lang;

import net.sourceforge.pinyin4j.PinyinHelper;


/**
 * <b>字符工具类，提供一些字符常用的方法</b>
 * <p>
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;(01)、获取字符cha的所有拼音：static String[] getPinYinByChar(char cha)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;(02)、获取字符cha的第一个拼音：static String getPinYinByCharOnlyFirst(char cha)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;(03)、判断字符cha是否是中文：static boolean isChinese(char cha)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;(04)、判断Unicode编码为cha的字符是否是中文：static boolean isChinese(int cha)
 */
public class CharUtils {

    /**
     * (01)、获取字符cha的所有拼音
     *
     * @param cha 汉字
     * @return 如果cha不是汉字，返回null
     */
    public static String[] getPinYinByChar(char cha) {
        return PinyinHelper.toHanyuPinyinStringArray(cha);
    }

    /**
     * (02)、获取字符cha的第一个拼音
     *
     * @param cha 汉字
     * @return 如果cha不是汉字，返回null
     */
    public static String getPinYinByCharOnlyFirst(char cha) {
        String[] pinyins = getPinYinByChar(cha);
        if (pinyins != null) {
            return pinyins[0];
        } else {
            return null;
        }
    }

    /**
     * (03)、判断字符cha是否是中文
     *
     * @param cha 字符
     * @return true：是
     */
    public static boolean isChinese(char cha) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(cha);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * (04)、判断Unicode编码为cha的字符是否是中文
     *
     * @param cha 字符的Unicode编码
     * @return true：是
     */
    public static boolean isChinese(int cha) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(cha);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        } else {
            return false;
        }
    }
}
