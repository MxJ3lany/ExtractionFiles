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

package com.lwh.jackknife.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {

    public static String[] mPinyinKey = new String[]{"a", "ai", "an", "ang", "ao", "ba", "bai",
            "ban", "bang", "bao", "bei", "ben", "beng", "bi", "bian", "biao", "bie", "bin", "bing",
            "bo", "bu", "ca", "cai", "can", "cang", "cao", "ce", "ceng", "cha", "chai", "chan",
            "chang", "chao", "che", "chen", "cheng", "chi", "chong", "chou", "chu", "chuai",
            "chuan", "chuang", "chui", "chun", "chuo", "ci", "cong", "cou", "cu", "cuan", "cui",
            "cun", "cuo", "da", "dai", "dan", "dang", "dao", "de", "deng", "di", "dian", "diao",
            "die", "ding", "diu", "dong", "dou", "du", "duan", "dui", "dun", "duo", "e", "en",
            "er", "fa", "fan", "fang", "fei", "fen", "feng", "fo", "fou", "fu", "ga", "gai", "gan",
            "gang", "gao", "ge", "gei", "gen", "geng", "gong", "gou", "gu", "gua", "guai", "guan",
            "guang", "gui", "gun", "guo", "ha", "hai", "han", "hang", "hao", "he", "hei", "hen",
            "heng", "hong", "hou", "hu", "hua", "huai", "huan", "huang", "hui", "hun", "huo", "ji",
            "jia", "jian", "jiang", "jiao", "jie", "jin", "jing", "jiong", "jiu", "ju", "juan",
            "jue", "jun", "ka", "kai", "kan", "kang", "kao", "ke", "ken", "keng", "kong", "kou",
            "ku", "kua", "kuai", "kuan", "kuang", "kui", "kun", "kuo", "la", "lai", "lan", "lang",
            "lao", "le", "lei", "leng", "li", "lia", "lian", "liang", "liao", "lie", "lin", "ling",
            "liu", "long", "lou", "lu", "lv", "luan", "lue", "lun", "luo", "ma", "mai", "man",
            "mang", "mao", "me", "mei", "men", "meng", "mi", "mian", "miao", "mie", "min", "ming",
            "miu", "mo", "mou", "mu", "na", "nai", "nan", "nang", "nao", "ne", "nei", "nen", "neng",
            "ni", "nian", "niang", "niao", "nie", "nin", "ning", "niu", "nong", "nu", "nv", "nuan",
            "nue", "nuo", "o", "ou", "pa", "pai", "pan", "pang", "pao", "pei", "pen", "peng", "pi",
            "pian", "piao", "pie", "pin", "ping", "po", "pu", "qi", "qia", "qian", "qiang", "qiao",
            "qie", "qin", "qing", "qiong", "qiu", "qu", "quan", "que", "qun", "ran", "rang", "rao",
            "re", "ren", "reng", "ri", "rong", "rou", "ru", "ruan", "rui", "run", "ruo", "sa",
            "sai", "san", "sang", "sao", "se", "sen", "seng", "sha", "shai", "shan", "shang",
            "shao", "she", "shen", "sheng", "shi", "shou", "shu", "shua", "shuai", "shuan",
            "shuang", "shui", "shun", "shuo", "si", "song", "sou", "su", "suan", "sui", "sun",
            "suo", "ta", "tai", "tan", "tang", "tao", "te", "teng", "ti", "tian", "tiao", "tie",
            "ting", "tong", "tou", "tu", "tuan", "tui", "tun", "tuo", "wa", "wai", "wan", "wang",
            "wei", "wen", "weng", "wo", "wu", "xi", "xia", "xian", "xiang", "xiao", "xie", "xin",
            "xing", "xiong", "xiu", "xu", "xuan", "xue", "xun", "ya", "yan", "yang", "yao", "ye",
            "yi", "yin", "ying", "yo", "yong", "you", "yu", "yuan", "yue", "yun", "za", "zai",
            "zan", "zang", "zao", "ze", "zei", "zen", "zeng", "zha", "zhai", "zhan", "zhang",
            "zhao", "zhe", "zhen", "zheng", "zhi", "zhong", "zhou", "zhu", "zhua", "zhuai", "zhuan",
            "zhuang", "zhui", "zhun", "zhuo", "zi", "zong", "zou", "zu", "zuan", "zui", "zun", "zuo"};

    private static int[] mPinyinValue = new int[]{-20319, -20317, -20304, -20295, -20292, -20283,
            -20265, -20257, -20242, -20230, -20051, -20036, -20032, -20026, -20002, -19990, -19986,
            -19982, -19976, -19805, -19784, -19775, -19774, -19763, -19756, -19751, -19746, -19741,
            -19739, -19728, -19725, -19715, -19540, -19531, -19525, -19515, -19500, -19484, -19479,
            -19467, -19289, -19288, -19281, -19275, -19270, -19263, -19261, -19249, -19243, -19242,
            -19238, -19235, -19227, -19224, -19218, -19212, -19038, -19023, -19018, -19006, -19003,
            -18996, -18977, -18961, -18952, -18783, -18774, -18773, -18763, -18756, -18741, -18735,
            -18731, -18722, -18710, -18697, -18696, -18526, -18518, -18501, -18490, -18478, -18463,
            -18448, -18447, -18446, -18239, -18237, -18231, -18220, -18211, -18201, -18184, -18183,
            -18181, -18012, -17997, -17988, -17970, -17964, -17961, -17950, -17947, -17931, -17928,
            -17922, -17759, -17752, -17733, -17730, -17721, -17703, -17701, -17697, -17692, -17683,
            -17676, -17496, -17487, -17482, -17468, -17454, -17433, -17427, -17417, -17202, -17185,
            -16983, -16970, -16942, -16915, -16733, -16708, -16706, -16689, -16664, -16657, -16647,
            -16474, -16470, -16465, -16459, -16452, -16448, -16433, -16429, -16427, -16423, -16419,
            -16412, -16407, -16403, -16401, -16393, -16220, -16216, -16212, -16205, -16202, -16187,
            -16180, -16171, -16169, -16158, -16155, -15959, -15958, -15944, -15933, -15920, -15915,
            -15903, -15889, -15878, -15707, -15701, -15681, -15667, -15661, -15659, -15652, -15640,
            -15631, -15625, -15454, -15448, -15436, -15435, -15419, -15416, -15408, -15394, -15385,
            -15377, -15375, -15369, -15363, -15362, -15183, -15180, -15165, -15158, -15153, -15150,
            -15149, -15144, -15143, -15141, -15140, -15139, -15128, -15121, -15119, -15117, -15110,
            -15109, -14941, -14937, -14933, -14930, -14929, -14928, -14926, -14922, -14921, -14914,
            -14908, -14902, -14894, -14889, -14882, -14873, -14871, -14857, -14678, -14674, -14670,
            -14668, -14663, -14654, -14645, -14630, -14594, -14429, -14407, -14399, -14384, -14379,
            -14368, -14355, -14353, -14345, -14170, -14159, -14151, -14149, -14145, -14140, -14137,
            -14135, -14125, -14123, -14122, -14112, -14109, -14099, -14097, -14094, -14092, -14090,
            -14087, -14083, -13917, -13914, -13910, -13907, -13906, -13905, -13896, -13894, -13878,
            -13870, -13859, -13847, -13831, -13658, -13611, -13601, -13406, -13404, -13400, -13398,
            -13395, -13391, -13387, -13383, -13367, -13359, -13356, -13343, -13340, -13329, -13326,
            -13318, -13147, -13138, -13120, -13107, -13096, -13095, -13091, -13076, -13068, -13063,
            -13060, -12888, -12875, -12871, -12860, -12858, -12852, -12849, -12838, -12831, -12829,
            -12812, -12802, -12607, -12597, -12594, -12585, -12556, -12359, -12346, -12320, -12300,
            -12120, -12099, -12089, -12074, -12067, -12058, -12039, -11867, -11861, -11847, -11831,
            -11798, -11781, -11604, -11589, -11536, -11358, -11340, -11339, -11324, -11303, -11097,
            -11077, -11067, -11055, -11052, -11045, -11041, -11038, -11024, -11020, -11019, -11018,
            -11014, -10838, -10832, -10815, -10800, -10790, -10780, -10764, -10587, -10544, -10533,
            -10519, -10331, -10329, -10328, -10322, -10315, -10309, -10307, -10296, -10281, -10274,
            -10270, -10262, -10260, -10256, -10254};

    private TextUtils() {
    }

    public static boolean isEmpty(CharSequence str) {
        return android.text.TextUtils.isEmpty(str);
    }

    public static boolean isNotEmpty(CharSequence str) {
        return !isEmpty(str);
    }

    public static boolean checkEmpty(String... text) {
        for (String element : text) {
            boolean isSucceed = isEmpty(element);
            if (!isSucceed) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkNotEmpty(String... text) {
        for (String element : text) {
            boolean isSucceed = isNotEmpty(element);
            if (!isSucceed) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEqualTo(String lhss, String rhss) {
        return android.text.TextUtils.equals(lhss, rhss);
    }

    public static boolean isNotEqualTo(String lhss, String rhss) {
        return !isEqualTo(lhss, rhss);
    }

    public static String getUUID() {
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("-", "");
    }

    public static boolean match(String text, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher matcher = p.matcher(text);
        if (matcher.matches()) {
            return true;
        }
        return false;
    }

    private static int getASCII(String ch) {
        int asc = 0;
        byte[] bytes = new byte[0];
        try {
            bytes = ch.getBytes("gb2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (bytes == null || bytes.length > 2 || bytes.length <= 0) {
            throw new RuntimeException("Illegal resource string.");
        }
        if (bytes.length == 1) {
            asc = bytes[0];
        }
        if (bytes.length == 2) {
            int highByte = 256 + bytes[0];
            int lowByte = 256 + bytes[1];
            asc = (256 * highByte + lowByte) - 256 * 256;
        }
        return asc;
    }

    public static String getPinyin(String ch) {
        String result = null;
        int ascii = getASCII(ch);
        if (ascii > 0 && ascii < 160) {
            result = String.valueOf((char) ascii);
        } else {
            for (int i = (mPinyinValue.length - 1); i >= 0; i--) {
                if (mPinyinValue[i] <= ascii) {
                    result = mPinyinKey[i];
                    break;
                }
            }
        }
        return result;
    }

    public static String getPinyinFromSentence(String chs) {
        String key, value;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < chs.length(); i++) {
            key = chs.substring(i, i + 1);
            if (key.getBytes().length >= 2) {
                value = getPinyin(key);
                if (value == null) {
                    value = "";
                }
            } else {
                value = key;
            }
            builder.append(value);
        }
        return builder.toString();
    }

    public static String subsection(String text, String divider, int sectionLength) {
        if (text.length() < sectionLength) {
            return text;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < text.length(); i++) {
            sb.append(text.substring(i, i + sectionLength)).append(divider);
        }
        return sb.toString();
    }

    public static String parseJson(String str, String name) {
        if (str.isEmpty() || name.isEmpty()) {
            return null;
        }
        String result = null;
        try {
            JSONObject jo = new JSONObject(str);
            result = jo.has(name) ? jo.getString(name) : null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static enum CharType {
        DELIMITER, // 非字母截止字符，例如，．）（　等等　（ 包含U0000-U0080）
        NUM, // 2字节数字１２３４
        LETTER, // gb2312中的，例如:ＡＢＣ，2字节字符同时包含 1字节能表示的 basic latin and latin-1
        OTHER, // 其他字符
        CHINESE;// 中文字
    }

    /**
     * 判断输入char类型变量的字符类型
     *
     * @param c char类型变量
     * @return CharType 字符类型
     */
    public static CharType checkType(char c) {
        CharType ct = null;

        // 中文，编码区间0x4e00-0x9fbb
        if ((c >= 0x4e00) && (c <= 0x9fbb)) {
            ct = CharType.CHINESE;
        }

        // Halfwidth and Fullwidth Forms， 编码区间0xff00-0xffef
        else if ((c >= 0xff00) && (c <= 0xffef)) { // 2字节英文字
            if (((c >= 0xff21) && (c <= 0xff3a))
                    || ((c >= 0xff41) && (c <= 0xff5a))) {
                ct = CharType.LETTER;
            }

            // 2字节数字
            else if ((c >= 0xff10) && (c <= 0xff19)) {
                ct = CharType.NUM;
            }

            // 其他字符，可以认为是标点符号
            else
                ct = CharType.DELIMITER;
        }

        // basic latin，编码区间 0000-007f
        else if ((c >= 0x0021) && (c <= 0x007e)) { // 1字节数字
            if ((c >= 0x0030) && (c <= 0x0039)) {
                ct = CharType.NUM;
            } // 1字节字符
            else if (((c >= 0x0041) && (c <= 0x005a))
                    || ((c >= 0x0061) && (c <= 0x007a))) {
                ct = CharType.LETTER;
            }
            // 其他字符，可以认为是标点符号
            else
                ct = CharType.DELIMITER;
        }

        // latin-1，编码区间0080-00ff
        else if ((c >= 0x00a1) && (c <= 0x00ff)) {
            if ((c >= 0x00c0) && (c <= 0x00ff)) {
                ct = CharType.LETTER;
            } else
                ct = CharType.DELIMITER;
        } else
            ct = CharType.OTHER;

        return ct;
    }
}
