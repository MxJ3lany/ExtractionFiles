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

package com.lwh.jackknife.mvp;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseModel<BEAN> {

    protected List<BEAN> mDatas;

    protected Class<BEAN> mDataClass;

    private Comparator<BEAN> mComparator;
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

    public BaseModel(Class<BEAN> dataClass) {
        if (dataClass == null) {
            throw new IllegalArgumentException("Unknown bean type.");
        }
        mDataClass = dataClass;
        mDatas = new ArrayList<>();
    }

    public enum SortStrategy {
        ASC, DESC
    }

    public BaseModel add(BEAN datas) {
        mDatas.add(datas);
        return this;
    }

    public BaseModel add(List<BEAN> datas) {
        mDatas.addAll(datas);
        return this;
    }

    public BaseModel clear() {
        mDatas.clear();
        return this;
    }

    public BaseModel sort(final String sortKey, final SortStrategy strategy) {
        mComparator = new Comparator<BEAN>() {

            @Override
            public int compare(BEAN lhs, BEAN rhs) {
                int reverse = 1;
                if (strategy == SortStrategy.DESC) {
                    reverse = -reverse;
                }
                try {
                    Field field = mDataClass.getDeclaredField(sortKey);
                    field.setAccessible(true);
                    Class<?> fieldType = field.getType();
                    if (String.class.isAssignableFrom(fieldType)) {
                        return (getPinyinFromSentence((String) field.get(lhs)).compareTo
                                (getPinyinFromSentence((String) field.get(rhs)))) * reverse;
                    } else {
                        return (field.get(rhs).hashCode() - field.get(lhs).hashCode()) * reverse;
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                throw new RuntimeException("Unknown condition.");
            }

            @Override
            public boolean equals(Object object) {
                return false;
            }
        };
        Collections.sort(mDatas, mComparator);
        return this;
    }

    public BaseModel sort(String sortKey) {
        return sort(sortKey, SortStrategy.ASC);
    }

    public interface OnLoadListener<BEAN> {
        void onLoad(List<BEAN> beans);
    }

    public interface OnExtractListener<ELEMENT> {
        void onExtract(String elementName, List<ELEMENT> elements);
    }

    public List<BEAN> getDatas() {
        return mDatas;
    }

    public int getCount() {
        if (mDatas != null) {
            return mDatas.size();
        }
        return -1;
    }

    protected int countObjects(Selector selector) {
        List<BEAN> objects = findObjects(selector);
        if (objects != null) {
            return objects.size();
        }
        return 0;
    }

    protected <ELEMENT> List<ELEMENT> extractElement(String elementName, Class<ELEMENT> elementClass) {
        return extractElement(null, elementName, elementClass);
    }

    protected <ELEMENT> List<ELEMENT> extractElement(Selector selector, String elementName, Class<ELEMENT> elementClass) {
        List<ELEMENT> elements = new ArrayList<>();
        List<BEAN> datas = findObjects(selector);
        if (datas.size() > 0) {
            for (BEAN bean : datas) {
                Field[] fields = mDataClass.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    if (field.getName().equals(elementName)) {
                        ELEMENT element = null;
                        try {
                            element = (ELEMENT) field.get(bean);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        elements.add(element);
                    }
                }
            }
        }
        return elements;
    }

    protected List<BEAN> findObjects() {
        return findObjects(null);
    }

    protected List<BEAN> findObjects(Selector selector) {
        if (selector == null) {
            return mDatas;
        }
        List<BEAN> result = new ArrayList<>();
        Map<String, Object> map = selector.getConditionMap();
        Set<String> keys = map.keySet();
        for (int i = 0; i < mDatas.size(); i++) {
            int matchesCount = 0;
            BEAN bean = mDatas.get(i);
            Iterator<String> iterator = keys.iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                String[] keyPart = key.split(Selector.SPACE);
                String elementName = keyPart[0];
                Field targetField;
                try {
                    targetField = mDataClass.getDeclaredField(elementName);
                    targetField.setAccessible(true);
                    Object leftValue = map.get(key);
                    Object rightValue = targetField.get(bean);
                    if (matchCondition(key, leftValue, rightValue)) {
                        matchesCount++;
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            if (matchesCount == keys.size()) {
                result.add(bean);
            }
        }
        return result;
    }

    private boolean isAssignableFromByte(Class<?> fieldType) {
        return byte.class.isAssignableFrom(fieldType) || Byte.class.isAssignableFrom(fieldType);
    }

    private boolean isAssignableFromNumber(Class<?> fieldType) {
        return isAssignableFromByte(fieldType) ||
                isAssignableFromShort(fieldType) ||
                isAssignableFromInteger(fieldType) ||
                isAssignableFromLong(fieldType) ||
                isAssignableFromFloat(fieldType) ||
                isAssignableFromDouble(fieldType);
    }

    private boolean isAssignableFromShort(Class<?> fieldType) {
        return short.class.isAssignableFrom(fieldType) || Short.class.isAssignableFrom(fieldType);
    }

    private boolean isAssignableFromInteger(Class<?> fieldType) {
        return int.class.isAssignableFrom(fieldType) || Integer.class.isAssignableFrom(fieldType);
    }

    private boolean isAssignableFromLong(Class<?> fieldType) {
        return long.class.isAssignableFrom(fieldType) || Long.class.isAssignableFrom(fieldType);
    }

    private boolean isAssignableFromFloat(Class<?> fieldType) {
        return float.class.isAssignableFrom(fieldType) || Float.class.isAssignableFrom(fieldType);
    }

    private boolean isAssignableFromDouble(Class<?> fieldType) {
        return double.class.isAssignableFrom(fieldType) || Double.class.isAssignableFrom(fieldType);
    }

    private boolean isAssinableFromCharSequence(Class<?> fieldType) {
        return CharSequence.class.isAssignableFrom(fieldType);
    }

    private boolean matchEqualTo(Object requiredValue, Object actualValue) {
        return requiredValue.equals(actualValue);
    }

    private boolean matchNotEqualTo(Object requiredValue, Object actualValue) {
        return !requiredValue.equals(actualValue);
    }

    private boolean matchGreatorThan(Number requiredValue, Number actualValue) {
        return requiredValue.doubleValue() < actualValue.doubleValue();
    }

    private boolean matchLessThan(Number requiredValue, Number actualValue) {
        return requiredValue.doubleValue() > actualValue.doubleValue();
    }

    private boolean matchGreatorThanOrEqualTo(Number requiredValue, Number actualValue) {
        return requiredValue.doubleValue() <= actualValue.doubleValue();
    }

    private boolean matchLessThanOrEqualTo(Number requiredValue, Number actualValue) {
        return requiredValue.doubleValue() >= actualValue.doubleValue();
    }

    private boolean matchContains(String requiredValue, String actualValue) {
        return actualValue.contains(requiredValue);
    }

    private boolean matchStartsWith(String requiredValue, String actualValue) {
        return actualValue.startsWith(requiredValue);
    }

    private boolean matchEndsWith(String requiredValue, String actualValue) {
        return actualValue.endsWith(requiredValue);
    }

    private boolean matchCondition(String key, Object requiredValue, Object actualValue)
            throws IllegalAccessException, NoSuchFieldException {
        String[] keyPart = key.split(Selector.SPACE);
        String elementName = keyPart[0];
        String condition = keyPart[1];
        Field field = mDataClass.getDeclaredField(elementName);
        field.setAccessible(true);
        Class<?> fieldType = requiredValue.getClass();
        if (condition.equals(Selector.EQUAL_TO_HOLDER)) {
            return matchEqualTo(requiredValue, actualValue);
        }
        if (condition.equals(Selector.NOT_EQUAL_TO_HOLDER)) {
            return matchNotEqualTo(requiredValue, actualValue);
        }
        if (condition.equals(Selector.GREATOR_THAN_HOLDER)
                && isAssignableFromNumber(fieldType)) {
            return matchGreatorThan((Number) requiredValue, (Number) actualValue);
        }
        if (condition.equals(Selector.LESS_THAN_HOLDER)
                && isAssignableFromNumber(fieldType)) {
            return matchLessThan((Number) requiredValue, (Number) actualValue);
        }
        if (condition.equals(Selector.GREATOR_THAN_OR_EQUAL_TO_HOLDER)
                && isAssignableFromNumber(fieldType)) {
            return matchGreatorThanOrEqualTo((Number) requiredValue, (Number) actualValue);
        }
        if (condition.equals(Selector.LESS_THAN_OR_EQUAL_TO_HOLDER)
                && isAssignableFromNumber(fieldType)) {
            return matchLessThanOrEqualTo((Number) requiredValue, (Number) actualValue);
        }
        if (condition.equals(Selector.CONTAINS_HOLDER)
                && isAssinableFromCharSequence(fieldType)) {
            return matchContains(requiredValue.toString(), actualValue.toString());
        }
        if (condition.equals(Selector.STARTS_WITH_HOLDER)
                && isAssinableFromCharSequence(fieldType)) {
            return matchStartsWith(requiredValue.toString(), actualValue.toString());
        }
        if (condition.equals(Selector.ENDS_WITH_HOLDER)
                && isAssinableFromCharSequence(fieldType)) {
            return matchEndsWith(requiredValue.toString(), actualValue.toString());
        }
        throw new UndeclaredExpressionException("Condition key is illegal.");
    }

    public static class UndeclaredExpressionException extends RuntimeException {
        public UndeclaredExpressionException() {
        }

        public UndeclaredExpressionException(String detailMessage) {
            super(detailMessage);
        }
    }

    public static class Selector {

        private Map<String, Object> mConditionMap;

        private static final String SPACE = " ";

        private static final String EQUAL_TO_HOLDER = "=?";

        private static final String NOT_EQUAL_TO_HOLDER = "!=?";

        private static final String GREATOR_THAN_HOLDER = ">?";

        private static final String LESS_THAN_HOLDER = "<?";

        private static final String GREATOR_THAN_OR_EQUAL_TO_HOLDER = ">=?";

        private static final String LESS_THAN_OR_EQUAL_TO_HOLDER = "<=?";

        private static final String CONTAINS_HOLDER = "contains?";

        private static final String STARTS_WITH_HOLDER = "startswith?";

        private static final String ENDS_WITH_HOLDER = "endswith?";

        private Selector() {
            mConditionMap = new ConcurrentHashMap<>();
        }

        public static Selector create() {
            return new Selector();
        }

        protected Map<String, Object> getConditionMap() {
            return mConditionMap;
        }

        public Selector addWhereEqualTo(String elementName, Object value) {
            String key = elementName + SPACE + EQUAL_TO_HOLDER;
            mConditionMap.put(key, value);
            return this;
        }

        public Selector addWhereNotEqualTo(String elementName, Object value) {
            String key = elementName + SPACE + NOT_EQUAL_TO_HOLDER;
            mConditionMap.put(key, value);
            return this;
        }

        public Selector addWhereGreatorThan(String elementName, Number value) {
            String key = elementName + SPACE + GREATOR_THAN_HOLDER;
            mConditionMap.put(key, value);
            return this;
        }

        public Selector addWhereLessThan(String elementName, Number value) {
            String key = elementName + SPACE + LESS_THAN_HOLDER;
            mConditionMap.put(key, value);
            return this;
        }

        public Selector addWhereGreatorThanOrEqualTo(String elementName, Number value) {
            String key = elementName + SPACE + GREATOR_THAN_OR_EQUAL_TO_HOLDER;
            mConditionMap.put(key, value);
            return this;
        }

        public Selector addWhereLessThanOrEqualTo(String elementName, Number value) {
            String key = elementName + SPACE + LESS_THAN_OR_EQUAL_TO_HOLDER;
            mConditionMap.put(key, value);
            return this;
        }

        public Selector addWhereContains(String elementName, String value) {
            String key = elementName + SPACE + CONTAINS_HOLDER;
            mConditionMap.put(key, value);
            return this;
        }

        public Selector addWhereStartsWith(String elementName, String prefix) {
            String key = elementName + SPACE + STARTS_WITH_HOLDER;
            mConditionMap.put(key, prefix);
            return this;
        }

        public Selector addWhereEndsWith(String elementName, String suffix) {
            String key = elementName + SPACE + ENDS_WITH_HOLDER;
            mConditionMap.put(key, suffix);
            return this;
        }
    }

    private int getASCII(String ch) {
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

    private String getPinyin(String ch) {
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

    private String getPinyinFromSentence(String chs) {
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
}
