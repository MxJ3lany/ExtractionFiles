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

package me.panpf.tool4a.graphics;

import android.graphics.Point;
import android.graphics.Rect;

/**
 * 矩形操作工具箱
 */
public class RectUtils {
    /**
     * 缩放Rect
     *
     * @param rect
     * @param scaleX
     * @param scaleY
     * @return rect
     */
    public static Rect zoom(Rect rect, float scaleX, float scaleY) {
        rect.left *= scaleX;
        rect.top *= scaleY;
        rect.right *= scaleX;
        rect.bottom *= scaleY;
        return rect;
    }

    /**
     * 缩放Rect
     *
     * @param rect
     * @param scale
     * @return rect
     */
    public static Rect zoom(Rect rect, float scale) {
        rect.left *= scale;
        rect.top *= scale;
        rect.right *= scale;
        rect.bottom *= scale;
        return rect;
    }

    /**
     * 映射矩形，将源尺寸中的一个矩形映射到目标尺寸中
     *
     * @param rectIntSourceSize 源尺寸中的一个矩形
     * @param sourceSize        源尺寸
     * @param targetSize        目标尺寸
     * @return 源尺寸中的矩形映射到目标尺寸中后的矩形
     */
    public static Rect mapping(Rect rectIntSourceSize, Point sourceSize, Point targetSize) {
        Rect rect = new Rect(rectIntSourceSize);
        float scaleX = (float) targetSize.x / sourceSize.x;
        float scaleY = (float) targetSize.y / sourceSize.y;
        rect.left *= scaleX;
        rect.top *= scaleY;
        rect.right *= scaleX;
        rect.bottom *= scaleY;
        return rect;
    }

    /**
     * 映射矩形，将源尺寸中的一个矩形映射到目标尺寸中
     *
     * @param rectIntSourceSize       源尺寸中的一个矩形
     * @param sourceSize              源尺寸
     * @param targetSize              目标尺寸
     * @param isTargetSizeInterchange 是否将targetSize的宽高互换
     * @return 源尺寸中的矩形映射到目标尺寸中后的矩形
     */
    public static Rect mapping(Rect rectIntSourceSize, Point sourceSize, Point targetSize, boolean isTargetSizeInterchange) {
        if (isTargetSizeInterchange) {
            targetSize.x = targetSize.x + targetSize.y;
            targetSize.y = targetSize.x - targetSize.y;
            targetSize.x = targetSize.x - targetSize.y;
        }
        Rect rect = new Rect(rectIntSourceSize);
        float scaleX = (float) targetSize.x / sourceSize.x;
        float scaleY = (float) targetSize.y / sourceSize.y;
        rect.left *= scaleX;
        rect.top *= scaleY;
        rect.right *= scaleX;
        rect.bottom *= scaleY;
        return rect;
    }
}