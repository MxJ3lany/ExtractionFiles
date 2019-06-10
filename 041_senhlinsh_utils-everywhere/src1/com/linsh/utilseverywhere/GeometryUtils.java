package com.linsh.utilseverywhere;

import android.graphics.Point;

/**
 * <pre>
 *    author : Senh Linsh
 *    github : https://github.com/SenhLinsh
 *    date   : 2018/02/01
 *    desc   : 工具类: 几何运算 相关
 * </pre>
 */
public class GeometryUtils {

    /**
     * 计算两点之间的距离
     *
     * @param p1 点1
     * @param p2 点2
     * @return 距离
     */
    public static double getDistance(Point p1, Point p2) {
        int x = Math.abs(p2.x - p1.x);
        int y = Math.abs(p2.y - p1.y);
        return Math.sqrt(x * x + y * y);
    }
}
