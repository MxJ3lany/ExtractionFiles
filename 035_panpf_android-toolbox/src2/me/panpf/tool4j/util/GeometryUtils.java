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

/**
 * 几何工具箱
 */
public class GeometryUtils {
    /**
     * 使用X轴射线法判断给定的点是否在给定的多边形内部
     *
     * @param pointX
     * @param pointY
     * @param vertexPoints
     * @return
     */
    public static boolean isPolygonContainPoint(int pointX, int pointY, int[][] vertexPoints) {
        int nCross = 0;
        for (int i = 0; i < vertexPoints.length; i++) {
            int[] p1 = vertexPoints[i];
            int[] p2 = vertexPoints[(i + 1) % vertexPoints.length];
            if (p1[1] == p2[1])
                continue;
            if (pointY < Math.min(p1[1], p2[1]))
                continue;
            if (pointY >= Math.max(p1[1], p2[1]))
                continue;
            double x = (double) (pointY - p1[1]) * (double) (p2[0] - p1[0]) / (double) (p2[1] - p1[1]) + p1[0];
            if (x > pointX)
                nCross++;
        }
        return (nCross % 2 == 1);
    }
}