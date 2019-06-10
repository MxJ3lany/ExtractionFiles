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
import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Build;
import android.view.OrientationEventListener;

import me.panpf.tool4a.view.WindowUtils;

/**
 * 相机工具箱
 */
public class CameraUtils {
    /**
     * 根据当前窗口的显示方向设置相机的显示方向
     *
     * @param activity 用来获取当前窗口的显示方向
     * @param cameraId 相机ID，用于区分是前置摄像头还是后置摄像头，在API级别xiaoyu9d系统下此参数无用
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static int getOptimalDisplayOrientationByWindowDisplayRotation(Activity activity, int cameraId) {
        int degrees = WindowUtils.getDisplayRotation(activity);
        if (Build.VERSION.SDK_INT >= 9) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(cameraId, info);
            int result;
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360;
            } else {
                result = (info.orientation - degrees + 360) % 360;
            }
            return result;
        } else {
            return 0;
        }
    }

    /**
     * 根据当前窗口的显示方向设置相机的显示方向
     *
     * @param activity 用来获取当前窗口的显示方向
     * @param cameraId 相机ID，用于区分是前置摄像头还是后置摄像头
     * @param camera
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void setDisplayOrientationByWindowDisplayRotation(Activity activity, int cameraId, Camera camera) {
        int degrees = WindowUtils.getDisplayRotation(activity);
        int result = degrees;
        if (Build.VERSION.SDK_INT >= 9) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(cameraId, info);
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360;
            } else {
                result = (info.orientation - degrees + 360) % 360;
            }
        }
        camera.setDisplayOrientation(result);
    }

    /**
     * @param orientation OrientationEventListener类中onOrientationChanged()方法的参数
     * @param cameraId
     * @return
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static int getOptimalParametersOrientationByWindowDisplayRotation(int orientation, int cameraId) {
        if (orientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
            //计算方向
            int rotation = 0;
            if (Build.VERSION.SDK_INT >= 9) {
                CameraInfo info = new CameraInfo();
                Camera.getCameraInfo(cameraId, info);
                orientation = (orientation + 45) / 90 * 90;
                if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                    rotation = (info.orientation - orientation + 360) % 360;
                } else {
                    rotation = (info.orientation + orientation) % 360;
                }
            }
            return rotation;
        } else {
            return -1;
        }
    }

    /**
     * OrientationEventListener类中onOrientationChanged()方法的参数
     *
     * @param orientation
     * @param cameraId
     * @param camera
     */
    public static void setParametersOrientationByWindowDisplayRotation(int orientation, int cameraId, Camera camera) {
        int rotation = getOptimalParametersOrientationByWindowDisplayRotation(orientation, cameraId);
        if (rotation >= 0) {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setRotation(rotation);
            camera.setParameters(parameters);
        }
    }

    /**
     * 判断给定的相机是否支持给定的闪光模式
     *
     * @param camera    给定的相机
     * @param flashMode 给定的闪光模式
     * @return
     */
    public static boolean isSupportFlashMode(Camera camera, String flashMode) {
        return camera != null ? camera.getParameters().getSupportedFlashModes().contains(flashMode) : false;
    }

    /**
     * 将YUV格式的图片的源数据从横屏模式转为竖屏模式，注意：将源图片的宽高互换一下就是新图片的宽高
     *
     * @param sourceData YUV格式的图片的源数据
     * @param width      源图片的宽
     * @param height     源图片的高
     * @return
     */
    public static final byte[] yuvLandscapeToPortrait(byte[] sourceData, int width, int height) {
        byte[] rotatedData = new byte[sourceData.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++)
                rotatedData[x * height + height - y - 1] = sourceData[x + y * width];
        }
        return rotatedData;
    }
}