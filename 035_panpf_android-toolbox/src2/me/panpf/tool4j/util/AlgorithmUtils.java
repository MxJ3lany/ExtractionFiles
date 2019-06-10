package me.panpf.tool4j.util;

/**
 * 算法工具箱，提供常用算法
 */
public class AlgorithmUtils {

    /**
     * 计算斐波那契数列第N个元素的值
     *
     * @param number 第N个元素
     * @return 第几个元素的值
     */
    public static long countFBNQ(long number) {
        long one = 0;
        long two = 1;
        long three = -1;
        for (long w = 2; w < number; w++) {
            three = one + two;
            one = two;
            two = three;
        }
        return one + two;
    }

}
