package com.linsh.utilseverywhere;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>
 *    author : Senh Linsh
 *    github : https://github.com/SenhLinsh
 *    date   : 2017/11/10
 *    desc   : 工具类: 正则相关
 *
 *             注: 部分 API 直接参考或使用 https://github.com/l123456789jy/Lazy 中 RegexUtils 类里面的方法
 * </pre>
 */
public class RegexUtils {
    // 全网IP
    public static final String IP_REGEX = "^((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))$";
    // 手机号码
    public static final String PHONE_NUMBER_REGEX = "^1\\d{10}$";
    // 邮箱 ("www."可省略不写)
    public static final String EMAIL_REGEX = "^(www\\.)?\\w+(\\.\\w+)*@\\w+(\\.\\w+)+$";
    // 汉字 (个数限制为一个或多个)
    public static final String CHINESE_REGEX = "^[\u4e00-\u9f5a]+$";
    // 身份证号
    public static final String ID_CARD = "^(^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$)|(^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])((\\d{4})|\\d{3}[Xx])$)$";
    // 网址
    public static final String URL = "^(([hH][tT]{2}[pP][sS]?)|([fF][tT][pP]))://[\\w.-]+\\.\\w{2,4}((/.*)?|(\\?.+))$";
    // 汉字
    public static final String CHAR_CN = "^[\\u4e00-\\u9fa5]+$";

    private RegexUtils() {
    }

    /**
     * 匹配邮箱账号，"www."可省略不写
     */
    public static boolean isEmail(String string) {
        return string.matches(EMAIL_REGEX);
    }

    /**
     * 匹配手机号码 (由于号码段资源非常丰富, 只对 1 开头 11 位数字作为限制条件)
     */
    public static boolean isMobilePhoneNumber(String string) {
        return string.matches(PHONE_NUMBER_REGEX);
    }

    /**
     * 匹配全网IP
     */
    public static boolean isIp(String string) {
        return string.matches(IP_REGEX);
    }

    /**
     * 是否全部由汉子组成
     */
    public static boolean isChinese(String string) {
        return string.matches(CHINESE_REGEX);
    }

    /**
     * 匹配身份证号
     */
    public static boolean isIdCard(String string) {
        return string.matches(ID_CARD);
    }

    /**
     * 验证给定的字符串是否是URL，仅支持http、https、ftp
     */
    public static boolean isURL(String string) {
        return string.matches(URL);
    }

    /**
     * 查找字符串
     *
     * @param input 用于匹配的输入源
     * @param regex 正则字符串
     * @param group 所查找的字符串在正则中的分组索引
     * @return 目标字符串, 匹配失败返回 null
     */
    public static String find(String input, String regex, int group) {
        Matcher matcher = Pattern.compile(regex).matcher(input);
        if (matcher.find()) {
            return matcher.group(group);
        }
        return null;
    }

    /**
     * 查找字符串
     *
     * @param input  用于匹配的输入源
     * @param regex  正则字符串
     * @param groups 所查找的字符串在正则中的分组索引
     * @return 目标字符串的数组, 匹配失败返回 null
     */
    public static String[] find(String input, String regex, int[] groups) {
        if (groups == null) return null;
        Matcher matcher = Pattern.compile(regex).matcher(input);
        if (matcher.find()) {
            String[] matches = new String[groups.length];
            for (int i = 0; i < groups.length; i++) {
                matches[i] = matcher.group(groups[i]);
            }
            return matches;
        }
        return null;
    }

    /**
     * 查找字符串
     *
     * @param input 用于匹配的输入源
     * @param regex 正则字符串
     * @return 如果匹配成功则返回该正则匹配对象, 匹配失败返回 null
     */
    public static Matcher find(String input, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(input);
        if (matcher.find()) {
            return matcher;
        }
        return null;
    }

}
