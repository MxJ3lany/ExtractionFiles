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

package me.panpf.tool4a.hardware.camera;

import android.graphics.Point;
import android.hardware.Camera.Size;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.panpf.tool4a.hardware.camera.BestPreviewSizeCalculator.CameraSize;

/**
 * Camera最佳尺寸计算器
 */
public class BasePreviewAndPictureSizeCalculator {
    private boolean proportionPriority = true;
    private Point screenResolutionPoint;    //屏幕分辨率
    private int minSizePixels = 480 * 320;    //最小预览分辨率
    private List<Size> supportPreviewSizes;
    private List<Size> supportPictureSizes;

    private BasePreviewAndPictureSizeCalculator(Point screenResolutionPoint, List<Size> supportPreviewSizes, List<Size> supportPictureSizes) {
        this.screenResolutionPoint = screenResolutionPoint;
        this.supportPreviewSizes = supportPreviewSizes;
        this.supportPictureSizes = supportPictureSizes;
    }

    /**
     * 获取最佳的预览和输出图片尺寸
     */
    @SuppressWarnings("unchecked")
    public Size[] getPreviewAndPictureSize() {
        if (supportPreviewSizes == null || supportPreviewSizes.size() == 0 || supportPictureSizes == null || supportPictureSizes.size() == 0) {
            return null;
        }

        //去掉小的
        BestPreviewSizeCalculator.removeSmall(supportPreviewSizes, minSizePixels);
        BestPreviewSizeCalculator.removeSmall(supportPictureSizes, minSizePixels);

        if (supportPreviewSizes == null || supportPreviewSizes.size() == 0 || supportPictureSizes == null || supportPictureSizes.size() == 0) {
            return null;
        }

        //从大到小排序
        Collections.sort(supportPreviewSizes, new Comparator<Size>() {
            @Override
            public int compare(Size lhs, Size rhs) {
                return -(lhs.width - rhs.width);
            }
        });
        Collections.sort(supportPictureSizes, new Comparator<Size>() {
            @Override
            public int compare(Size lhs, Size rhs) {
                return -(lhs.width - rhs.width);
            }
        });
        Size[] maxSizes = new Size[]{supportPreviewSizes.get(0), supportPictureSizes.get(0)};    //初始化默认最佳尺寸

        try {
            //计算最佳比例
            if (screenResolutionPoint.x < screenResolutionPoint.y) {    //如果宽度小于高度就将宽高互换
                screenResolutionPoint.x = screenResolutionPoint.x + screenResolutionPoint.y;
                screenResolutionPoint.y = screenResolutionPoint.x - screenResolutionPoint.y;
                screenResolutionPoint.x = screenResolutionPoint.x - screenResolutionPoint.y;
            }
            float optimalProportion = Float.valueOf(new DecimalFormat("0.00").format((float) screenResolutionPoint.x / (float) screenResolutionPoint.y));

			/* 首先获取预览尺寸集合和输出图片尺寸集合 */
            List<CameraSize> previewCameraSizes = new ArrayList<CameraSize>(supportPreviewSizes.size());
            for (Size size2 : supportPreviewSizes) {
                previewCameraSizes.add(new CameraSize(size2));
            }
            List<CameraSize> pictureCameraSizes = new ArrayList<CameraSize>(supportPictureSizes.size());
            for (Size size : supportPictureSizes) {
                pictureCameraSizes.add(new CameraSize(size));
            }
			
			/* 然后去除不相同的 */
            BestPreviewSizeCalculator.removalOfDifferent(previewCameraSizes, pictureCameraSizes);
			
			/* 接下来按比例分组 */
            Map<Float, List<Size>> previewSizeMap = BestPreviewSizeCalculator.groupingByProportion(previewCameraSizes);
            previewCameraSizes.clear();
            previewCameraSizes = null;
            Map<Float, List<Size>> pictureSizeMap = BestPreviewSizeCalculator.groupingByProportion(pictureCameraSizes);
            pictureCameraSizes.clear();
            pictureCameraSizes = null;
			
			/* 按最接近最佳比例排序 */
            List<Entry<Float, List<Size>>> previewSizeEntrys = new ArrayList<Entry<Float, List<Size>>>(previewSizeMap.entrySet());
            previewSizeMap.clear();
            previewSizeMap = null;
            List<Entry<Float, List<Size>>> pictureSizeEntrys = new ArrayList<Entry<Float, List<Size>>>(pictureSizeMap.entrySet());
            pictureSizeMap.clear();
            pictureSizeMap = null;
            BestPreviewSizeCalculator.sortByProportionForEntry(optimalProportion, previewSizeEntrys, pictureSizeEntrys);
			
			/* 按最佳比例一个一个找 */
            Size[] optimalSizes = null;
            List<Size> previewSizes = null;
            List<Size> pictureSizes = null;
            for (int w = 0; w < previewSizeEntrys.size(); w++) {
                previewSizes = previewSizeEntrys.get(w).getValue();
                pictureSizes = pictureSizeEntrys.get(w).getValue();
                optimalSizes = BestPreviewSizeCalculator.tryLookingSame(previewSizes, pictureSizes, screenResolutionPoint.x);    //尝试寻找相同尺寸的一组
                if (optimalSizes != null || proportionPriority) {
                    break;
                }
            }
            if (optimalSizes == null) {//如果还是没有找到就从最佳比例的尺寸组中取宽度最接近的一组
                optimalSizes = BestPreviewSizeCalculator.tryLookingWidthProximal(previewSizeEntrys.get(0).getValue(), pictureSizeEntrys.get(0).getValue(), screenResolutionPoint.x);
            }
            return optimalSizes;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return maxSizes;
        }
    }

    /**
     * 设置是否比例优先
     *
     * @param proportionPriority
     */
    public void setProportionPriority(boolean proportionPriority) {
        this.proportionPriority = proportionPriority;
    }

    /**
     * 设置最小分辨率的像素数
     *
     * @param minSizePixels
     */
    public void setMinSizePixels(int minSizePixels) {
        this.minSizePixels = minSizePixels;
    }
}