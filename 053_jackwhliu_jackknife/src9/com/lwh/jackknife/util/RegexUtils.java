/*
 * Copyright (C) 2018 The JackKnife Open Source Project
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

package com.lwh.jackknife.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {

    private static final String V_INTEGER = "^-?[1-9]\\d*$";

    private static final String V_Z_INDEX = "^[1-9]\\d*$";

    private static final String V_NEGATIVE_INTEGER = "^-[1-9]\\d*$";

    private static final String V_NUMBER = "^([+-]?)\\d*\\.?\\d+$";

    private static final String V_POSITIVE_NUMBER = "^[1-9]\\d*|0$";

    private static final String V_NEGATINE_NUMBER = "^-[1-9]\\d*|0$";

    private static final String V_FLOAT = "^([+-]?)\\d*\\.\\d+$";

    private static final String V_POSTTIVE_FLOAT = "^[1-9]\\d*.\\d*|0.\\d*[1-9]\\d*$";

    private static final String V_NEGATIVE_FLOAT = "^-([1-9]\\d*.\\d*|0.\\d*[1-9]\\d*)$";

    private static final String V_UNPOSITIVE_FLOAT = "^[1-9]\\d*.\\d*|0.\\d*[1-9]\\d*|0?.0+|0$";

    private static final String V_UNNEGATIVE_FLOAT = "^(-([1-9]\\d*.\\d*|0.\\d*[1-9]\\d*))|0?.0+|0$";

    private static final String V_EMAIL = "^\\w+((-\\w+)|(\\.\\w+))*\\@[A-Za-z0-9]+((\\.|-)[A-Za-z0-9]+)*\\.[A-Za-z0-9]+$";

    private static final String V_COLOR = "^[a-fA-F0-9]{6}$";

    private static final String V_URL = "^http[s]?:\\/\\/([\\w-]+\\.)+[\\w-]+([\\w-./?%&=]*)?$";

    private static final String V_CHINESE = "^[\\u4E00-\\u9FA5\\uF900-\\uFA2D]+$";

    private static final String V_ASCII = "^[\\x00-\\xFF]+$";

    private static final String V_ZIPCODE = "^\\d{6}$";

    private static final String V_MOBILE = "^(1)[0-9]{10}$";

    private static final String V_IP4 = "^(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)$";

    private static final String V_NOTEMPTY = "^\\S+$";

    private static final String V_PICTURE = "(.*)\\.(jpg|bmp|gif|ico|pcx|jpeg|tif|png|raw|tga)$";

    private static final String V_RAR = "(.*)\\.(rar|zip|7zip|tgz)$";

    private static final String V_DATE = "^((((1[6-9]|[2-9]\\d)\\d{2})-(0?[13578]|1[02])-(0?[1-9]|[12]\\d|3[01]))|(((1[6-9]|[2-9]\\d)\\d{2})-(0?[13456789]|1[012])-(0?[1-9]|[12]\\d|30))|(((1[6-9]|[2-9]\\d)\\d{2})-0?2-(0?[1-9]|1\\d|2[0-8]))|(((1[6-9]|[2-9]\\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00))-0?2-29-)) (20|21|22|23|[0-1]?\\d):[0-5]?\\d:[0-5]?\\d$";

    private static final String V_QQ_NUMBER = "^[1-9]*[1-9][0-9]*$";

    private static final String V_TEL = "^(([0\\+]\\d{2,3}-)?(0\\d{2,3})-)?(\\d{7,8})(-(\\d{3,}))?$";

    private static final String V_USERNAME = "^\\w+$";

    private static final String V_LETTER = "^[A-Za-z]+$";

    private static final String V_LETTER_U = "^[A-Z]+$";

    private static final String V_LETTER_I = "^[a-z]+$";

    private static final String V_IDCARD = "^(\\d{15}$|^\\d{18}$|^\\d{17}(\\d|X|x))$";

    private static final String V_PASSWORD_REG = "[A-Za-z]+[0-9]";

    private static final String V_PASSWORD_LENGTH = "^\\d{6,18}$";

    private static final String V_TWO_POINT = "^[0-9]+(.[0-9]{2})?$";

    private static final String V_31DAYS = "^((0?[1-9])|((1|2)[0-9])|30|31)$";

    private RegexUtils() {
    }

    public static boolean isInteger(String value) {
        return match(V_INTEGER, value);
    }

    public static boolean isZIndex(String value) {
        return match(V_Z_INDEX, value);
    }

    public static boolean isNegativeInteger(String value) {
        return match(V_NEGATIVE_INTEGER, value);
    }

    public static boolean isNumber(String value) {
        return match(V_NUMBER, value);
    }

    public static boolean isPositiveNumber(String value) {
        return match(V_POSITIVE_NUMBER, value);
    }

    public static boolean isNegativeNumber(String value) {
        return match(V_NEGATINE_NUMBER, value);
    }

    public static boolean is31Days(String value) {
        return match(V_31DAYS, value);
    }

    public static boolean isASCII(String value) {
        return match(V_ASCII, value);
    }

    public static boolean isChinese(String value) {
        return match(V_CHINESE, value);
    }

    public static boolean isColor(String value) {
        return match(V_COLOR, value);
    }

    public static boolean isDate(String value) {
        return match(V_DATE, value);
    }

    public static boolean isEmail(String value) {
        return match(V_EMAIL, value);
    }

    public static boolean isFloat(String value) {
        return match(V_FLOAT, value);
    }

    public static boolean isIdCard(String value) {
        return match(V_IDCARD, value);
    }

    public static boolean isIP4(String value) {
        return match(V_IP4, value);
    }

    public static boolean isLetter(String value) {
        return match(V_LETTER, value);
    }

    public static boolean isLowerCaseLetter(String value) {
        return match(V_LETTER_I, value);
    }

    public static boolean isUpperCaseLetter(String value) {
        return match(V_LETTER_U, value);
    }

    public static boolean isMobilePhoneNumber(String value) {
        return match(V_MOBILE, value);
    }

    public static boolean isNegativeFloat(String value) {
        return match(V_NEGATIVE_FLOAT, value);
    }

    public static boolean isNotEmpty(String value) {
        return match(V_NOTEMPTY, value);
    }

    public static boolean isNumberLength(String value) {
        return match(V_PASSWORD_LENGTH, value);
    }

    public static boolean isPassword(String value) {
        return match(V_PASSWORD_REG, value);
    }

    public static boolean isPicture(String value) {
        return match(V_PICTURE, value);
    }

    public static boolean isPositiveFloat(String value) {
        return match(V_POSTTIVE_FLOAT, value);
    }

    public static boolean isQqNumber(String value) {
        return match(V_QQ_NUMBER, value);
    }

    public static boolean isRar(String value) {
        return match(V_RAR, value);
    }

    public static boolean isTel(String value) {
        return match(V_TEL, value);
    }

    public static boolean isTwoFloat(String value) {
        return match(V_TWO_POINT, value);
    }

    public static boolean isUnnegativeFloat(String value) {
        return match(V_UNNEGATIVE_FLOAT, value);
    }

    public static boolean isUnpositiveFloat(String value) {
        return match(V_UNPOSITIVE_FLOAT, value);
    }

    public static boolean isUrl(String value) {
        return match(V_URL, value);
    }

    public static boolean isUsername(String value) {
        return match(V_USERNAME, value);
    }

    public static boolean isZipCode(String value) {
        return match(V_ZIPCODE, value);
    }

    private static boolean match(String regex, String str) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }
}
