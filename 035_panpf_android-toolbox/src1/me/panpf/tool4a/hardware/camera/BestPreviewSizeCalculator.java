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

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera.Size;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 最佳预览分辨率计算器
 */
public class BestPreviewSizeCalculator {
    private boolean proportionPriority = true;    //如果为true，最佳比例集合中会先在寻找宽高完全相同的一组，如果找不到就取最接近目标宽高的一组；如果为false，会首先在所有比例集合中寻找宽高完全相同的一组，如果找不到就在最佳比例集合中取最接近目标宽高的一组
    private Point screenResolutionPoint;    //屏幕分辨率
    private List<Size> supportPreviewSizes;    //支持的预览分辨率集合
    private int minPreviewSizePixels = 480 * 320;    //最小预览分辨率

    public BestPreviewSizeCalculator(Point screenResolutionPoint, List<Size> supportPreviewSizes) {
        this.screenResolutionPoint = screenResolutionPoint;
        this.supportPreviewSizes = supportPreviewSizes;
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public BestPreviewSizeCalculator(Context context, List<Size> supportPreviewSizes) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            screenResolutionPoint = new Point();
            display.getSize(screenResolutionPoint);
        } else {
            screenResolutionPoint = new Point(display.getWidth(), display.getHeight());
        }
        this.supportPreviewSizes = supportPreviewSizes;
    }

    /**
     * 获取最佳预览分辨率
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public Size getPreviewSize() {
        if (supportPreviewSizes == null || supportPreviewSizes.size() == 0) {
            return null;
        }

        //去掉小的
        removeSmall(supportPreviewSizes, minPreviewSizePixels);

        if (supportPreviewSizes == null || supportPreviewSizes.size() == 0) {
            return null;
        }

        //从大到小排序
        Collections.sort(supportPreviewSizes, new Comparator<Size>() {
            @Override
            public int compare(Size lhs, Size rhs) {
                return -(lhs.width - rhs.width);
            }
        });
        Size maxSize = supportPreviewSizes.get(0);

        try {
            //计算最佳比例
            if (screenResolutionPoint.x < screenResolutionPoint.y) {    //如果宽度小于高度就将宽高互换
                screenResolutionPoint.x = screenResolutionPoint.x + screenResolutionPoint.y;
                screenResolutionPoint.y = screenResolutionPoint.x - screenResolutionPoint.y;
                screenResolutionPoint.x = screenResolutionPoint.x - screenResolutionPoint.y;
            }
            float optimalProportion = Float.valueOf(new DecimalFormat("0.00").format((float) screenResolutionPoint.x / (float) screenResolutionPoint.y));

            //按比例分组
            List<CameraSize> previewCameraSizes = new ArrayList<CameraSize>(supportPreviewSizes.size());
            for (Size size2 : supportPreviewSizes) {
                previewCameraSizes.add(new CameraSize(size2));
            }
            Map<Float, List<Size>> previewSizeMap = groupingByProportion(previewCameraSizes);
            previewCameraSizes.clear();
            previewCameraSizes = null;
            List<Entry<Float, List<Size>>> previewSizeEntrys = new ArrayList<Entry<Float, List<Size>>>(previewSizeMap.entrySet());
            previewSizeMap.clear();
            previewSizeMap = null;

            //按最接近最佳比例排序
            sortByProportionForEntry(optimalProportion, previewSizeEntrys);

            //试图寻找宽高一模一样的
            Size optimalSize = null;
            List<Size> previewSizes = null;
            for (int w = 0; w < previewSizeEntrys.size(); w++) {
                previewSizes = previewSizeEntrys.get(w).getValue();
                optimalSize = findSame(previewSizes, screenResolutionPoint);
                if (optimalSize != null || proportionPriority) {
                    break;
                }
            }

            //如果找不到宽高一模一样的就从最近的一组中找出宽度最接近的
            if (optimalSize == null) {
                optimalSize = lookingWidthProximal(previewSizeEntrys.get(0).getValue(), screenResolutionPoint.x);
            }

            return optimalSize;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return maxSize;
        }
    }

    /**
     * 设置是否比例优先
     *
     * @param proportionPriority 如果为true，最佳比例集合中会先在寻找宽高完全相同的一组，如果找不到就取最接近目标宽高的一组；如果为false，会首先在所有比例集合中寻找宽高完全相同的一组，如果找不到就在最佳比例集合中取最接近目标宽高的一组
     */
    public void setProportionPriority(boolean proportionPriority) {
        this.proportionPriority = proportionPriority;
    }

    /**
     * 设置屏幕分辨率
     *
     * @param screenResolutionPoint
     */
    public void setScreenResolutionPoint(Point screenResolutionPoint) {
        this.screenResolutionPoint = screenResolutionPoint;
    }

    /**
     * 设置最小预览分辨率像素数
     *
     * @param minPreviewSizePixels
     */
    public void setMinPreviewSizePixels(int minPreviewSizePixels) {
        this.minPreviewSizePixels = minPreviewSizePixels;
    }

    /**
     * 查找宽度最接近的
     *
     * @param cameraSizes
     * @param surfaceViewWidth
     * @return
     */
    private static final Size lookingWidthProximal(List<Size> cameraSizes, final int surfaceViewWidth) {
        Collections.sort(cameraSizes, new Comparator<Size>() {
            @Override
            public int compare(Size lhs, Size rhs) {
                return (Math.abs(lhs.width - surfaceViewWidth)) - Math.abs((rhs.width - surfaceViewWidth));
            }
        });
        return cameraSizes.get(0);
    }

    /**
     * 给实体按最接近最佳比例排序
     */
    public static final void sortByProportionForEntry(final float optimalProportion, List<Entry<Float, List<Size>>>... cameraSizeEntrysList) {
        Comparator<Entry<Float, List<Size>>> comparator = new Comparator<Entry<Float, List<Size>>>() {
            @Override
            public int compare(Entry<Float, List<Size>> lhs, Entry<Float, List<Size>> rhs) {
                int result = (int) (((Math.abs((lhs.getKey() - optimalProportion)) * 100) - (Math.abs((rhs.getKey() - optimalProportion)) * 100)));
                if (result == 0) {
                    result = (int) ((lhs.getKey() - rhs.getKey()) * 100) * -1;
                }
                return result;
            }
        };
        for (List<Entry<Float, List<Size>>> entry : cameraSizeEntrysList) {
            Collections.sort(entry, comparator);
        }
    }

    /**
     * 按比例分组
     *
     * @param cameraSizes
     * @return
     */
    public static final Map<Float, List<Size>> groupingByProportion(List<CameraSize> cameraSizes) {
        Map<Float, List<Size>> previewSizeMap = new HashMap<Float, List<Size>>();
        for (CameraSize cameraSize : cameraSizes) {
            if (previewSizeMap.containsKey(cameraSize.getProportion())) {
                previewSizeMap.get(cameraSize.getProportion()).add(cameraSize.getSize());
            } else {
                List<Size> tempCameraSizes = new ArrayList<Size>();
                tempCameraSizes.add(cameraSize.getSize());
                previewSizeMap.put(cameraSize.getProportion(), tempCameraSizes);
            }
        }
        return previewSizeMap;
    }

    /**
     * 查找宽高一样的
     *
     * @param cameraSizes
     * @return
     */
    public static final Size findSame(List<Size> cameraSizes, Point screenResolutionPoint) {
        for (Size tempSize : cameraSizes) {
            if (tempSize.width == screenResolutionPoint.x && tempSize.height == screenResolutionPoint.y) {
                return tempSize;
            }
        }
        return null;
    }

    /**
     * 删除那些小的
     *
     * @param cameraSizes
     * @param minPreviewPixels 最小预览像素数
     */
    public static void removeSmall(List<Size> cameraSizes, int minPreviewPixels) {
        Iterator<Size> iterator = cameraSizes.iterator();
        while (iterator.hasNext()) {
            Size size = iterator.next();
            if (size.width * size.height < minPreviewPixels) {
                iterator.remove();
            }
        }
    }


    /**
     * 视图按宽度最接近的原则查找出最佳的尺寸
     *
     * @param previewSizes
     * @param pictureSizes
     * @param surfaceViewWidth
     * @return
     */
    public static final Size[] tryLookingWidthProximal(List<Size> previewSizes, List<Size> pictureSizes, int surfaceViewWidth) {
        Size[] optimalSizes = new Size[2];
        optimalSizes[0] = lookingWidthProximal(previewSizes, surfaceViewWidth);
        optimalSizes[1] = lookingWidthProximal(pictureSizes, surfaceViewWidth);
        return optimalSizes;
    }

    /**
     * 视图按相同的原则查找出最佳的尺寸
     *
     * @param previewSizes
     * @param pictureSizes
     * @param surfaceViewWidth
     * @return
     */
    public static final Size[] tryLookingSame(List<Size> previewSizes, List<Size> pictureSizes, int surfaceViewWidth) {
        List<Size> sames = lookingSame(previewSizes, pictureSizes, surfaceViewWidth);    //查找出所有相同的
        if (sames != null) {    //如果存在相同的
            Size[] optimalSizes = new Size[2];
            Size optimalSize = null;
            if (sames.size() > 1) {    //如果相同的还不止一个，就查找出最接近的
                optimalSize = lookingWidthProximal(sames, surfaceViewWidth);
            } else {
                optimalSize = sames.get(0);
            }
            optimalSizes[0] = optimalSize;
            optimalSizes[1] = optimalSize;
            return optimalSizes;
        } else {
            return null;
        }
    }

    /**
     * 查找相同的
     *
     * @param cameraSizes1
     * @param cameraSizes2
     * @return
     */
    public static final List<Size> lookingSame(List<Size> cameraSizes1, List<Size> cameraSizes2, int surfaceViewWidth) {
        List<Size> sames = null;
        for (Size size : cameraSizes1) {
            if (exist(size, cameraSizes2)) {
                if (sames == null) {
                    sames = new ArrayList<Size>();
                }
                sames.add(size);
            }
        }
        return sames;
    }

    /**
     * 删除孤独的
     *
     * @param previewCameraSizes
     * @param pictureCameraSizes
     */
    public static final void removalOfDifferent(List<CameraSize> previewCameraSizes, List<CameraSize> pictureCameraSizes) {
        CameraSize tempCameraSize;
        Iterator<CameraSize> iterator = previewCameraSizes.iterator();
        while (iterator.hasNext()) {
            tempCameraSize = iterator.next();
            if (!exist(tempCameraSize, pictureCameraSizes)) {
                iterator.remove();
            }
        }

        iterator = pictureCameraSizes.iterator();
        while (iterator.hasNext()) {
            tempCameraSize = iterator.next();
            if (!exist(tempCameraSize, previewCameraSizes)) {
                iterator.remove();
            }
        }
    }

    /**
     * 判断指定的尺寸在指定的尺寸列表中是否存在
     *
     * @param cameraSize
     * @param cameraSizes
     * @return
     */
    public static final boolean exist(CameraSize cameraSize, List<CameraSize> cameraSizes) {
        for (CameraSize currentCameraSize : cameraSizes) {
            if (currentCameraSize.getProportion() == cameraSize.getProportion()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断指定的尺寸在指定的尺寸列表中是否存在
     *
     * @param cameraSize
     * @param cameraSizes
     * @return
     */
    public static final boolean exist(Size cameraSize, List<Size> cameraSizes) {
        for (Size currentCameraSize : cameraSizes) {
            if (currentCameraSize.width == cameraSize.width && currentCameraSize.height == cameraSize.height) {
                return true;
            }
        }
        return false;
    }

    public static class CameraSize {
        private Size size;
        private float proportion;

        public CameraSize(Size size) {
            this.size = size;
            proportion = Float.valueOf(new DecimalFormat("0.00").format((float) size.width / (float) size.height));
        }

        public Size getSize() {
            return size;
        }

        public void setSize(Size size) {
            this.size = size;
        }

        public float getProportion() {
            return proportion;
        }

        public void setProportion(float proportion) {
            this.proportion = proportion;
        }
    }
}