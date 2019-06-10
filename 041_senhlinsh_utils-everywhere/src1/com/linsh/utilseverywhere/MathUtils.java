package com.linsh.utilseverywhere;

/**
 * <pre>
 *    author : Senh Linsh
 *    github : https://github.com/SenhLinsh
 *    date   : 2018/02/01
 *    desc   : 工具类: 数学相关
 *             主要是为了扩展 Math 类没有的一些方法
 * </pre>
 */
public class MathUtils {

    /**
     * 设置限制范围, 确保不超出范围; 如果超出, 则取最大或最小值
     *
     * @param src 源数值
     * @param max 最大值, 如果大于最大值, 则返回最大值
     * @param min 最小值, 如果小于最小值, 则返回最小值
     */
    public static int limit(int src, int max, int min) {
        if (src >= max) return max;
        if (src < min) return min;
        return src;
    }

    /**
     * 设置限制范围, 确保不超出范围; 如果超出, 则取最大或最小值
     *
     * @param src 源数值
     * @param max 最大值, 如果大于最大值, 则返回最大值
     * @param min 最小值, 如果小于最小值, 则返回最小值
     */
    public static long limit(long src, long max, long min) {
        if (src >= max) return max;
        if (src < min) return min;
        return src;
    }

    /**
     * 设置限制范围, 确保不超出范围; 如果超出, 则取最大或最小值
     *
     * @param src 源数值
     * @param max 最大值, 如果大于最大值, 则返回最大值
     * @param min 最小值, 如果小于最小值, 则返回最小值
     */
    public static float limit(float src, float max, float min) {
        if (src >= max) return max;
        if (src < min) return min;
        return src;
    }

    /**
     * 设置限制范围, 确保不超出范围; 如果超出, 则取最大或最小值
     *
     * @param src 源数值
     * @param max 最大值, 如果大于最大值, 则返回最大值
     * @param min 最小值, 如果小于最小值, 则返回最小值
     */
    public static double limit(double src, double max, double min) {
        if (src >= max) return max;
        if (src < min) return min;
        return src;
    }
}
