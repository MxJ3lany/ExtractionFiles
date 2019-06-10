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
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;

import me.panpf.tool4a.view.WindowUtils;

/**
 * 相机管理器
 */
public class CameraManager {
    private static final String LOG_TAG = CameraManager.class.getSimpleName();
    private int frontCameraId = -1;    //前置摄像头的ID
    private int backCameraId = -1;    //后置摄像头的ID
    private int displayOrientation;    //显示方向
    private boolean debugMode;    //Debug模式，开启后将输出运行日志
    private boolean isBackCamera;
    private boolean running;
    private Camera camera;    //Camera
    private Activity activity;
    private SurfaceHolder surfaceHolder;
    private CameraCallback cameraCallback;
    private LoopFocusManager loopFocusManager;

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public CameraManager(Activity activity, SurfaceHolder surfaceHolder, CameraCallback cameraCallback) {
        this.activity = activity;
        this.surfaceHolder = surfaceHolder;
        this.cameraCallback = cameraCallback;
        this.loopFocusManager = new LoopFocusManager();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            this.surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        //获取前置和后置摄像头的ID
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            int cameraIds = Camera.getNumberOfCameras();
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int cameraId = 0; cameraId < cameraIds; cameraId++) {
                Camera.getCameraInfo(cameraId, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    frontCameraId = cameraId;
                } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    backCameraId = cameraId;
                }
            }
        }
    }

    /**
     * 打开后置摄像头
     *
     * @throws me.panpf.tool4a.hardware.camera.CameraManager.CameraBeingUsedException 摄像头被占用
     * @throws java.io.IOException                                                    if the method fails (for example, if the surface is unavailable or unsuitable).
     */
    public void openBackCamera() throws CameraBeingUsedException, IOException {
        if (debugMode) {
            Log.d(LOG_TAG, "openBackCamera");
        }
        release();
        try {
            camera = Camera.open();
            isBackCamera = true;
            loopFocusManager.setCamera(camera);
            initCamera();
            startPreview();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new CameraBeingUsedException();
        }
    }

    /**
     * 打开前置摄像头
     *
     * @return false：没有前置摄像头，已自动打开后置摄像头
     * @throws me.panpf.tool4a.hardware.camera.CameraManager.CameraBeingUsedException 摄像头被占用
     * @throws java.io.IOException                                                    if the method fails (for example, if the surface is unavailable or unsuitable).
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public boolean openFrontCamera() throws CameraBeingUsedException, IOException {
        if (debugMode) {
            Log.d(LOG_TAG, "openFrontCamera");
        }
        release();
        boolean result = false;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && frontCameraId != -1) {
                camera = Camera.open(frontCameraId);
                isBackCamera = false;
                result = true;
            } else {
                camera = Camera.open();
                isBackCamera = true;
            }
            loopFocusManager.setCamera(camera);
            initCamera();
            startPreview();
            return result;
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new CameraBeingUsedException();
        }
    }

    /**
     * 初始化Camera
     */
    private void initCamera() throws IOException {
        if (camera != null) {
            if (debugMode) {
                Log.d(LOG_TAG, "initCamera");
            }

            camera.setPreviewDisplay(surfaceHolder);

            //设置旋转90度
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                setDisplayOrientation(CameraUtils.getOptimalDisplayOrientationByWindowDisplayRotation(activity, isBackCamera ? backCameraId : frontCameraId));
            } else if (!WindowUtils.isLandscape(activity)) {
                setDisplayOrientation(90);
            }

            if (cameraCallback != null) {
                cameraCallback.onInitCamera(camera);    //回调初始化
            }

            Camera.Parameters parameters = camera.getParameters();
            Camera.Size previewSize = parameters.getPreviewSize();
            Camera.Size pictureSize = parameters.getPictureSize();
            if (debugMode) {
                Log.d(LOG_TAG, "previewSize：" + previewSize.width + "x" + previewSize.height + "; pictureSize：" + pictureSize.width + "x" + pictureSize.height);
            }
        }
    }

    /**
     * 开始预览
     */
    public void startPreview() {
        if (!running && camera != null) {
            if (debugMode) {
                Log.d(LOG_TAG, "startPreview");
            }
            camera.startPreview();
            if (cameraCallback != null) {
                cameraCallback.onStartPreview();
            }
            running = true;
        }
    }

    /**
     * 停止预览
     */
    public void stopPreview() {
        if (running && camera != null) {
            if (debugMode) {
                Log.d(LOG_TAG, "stopPreview");
            }
            camera.stopPreview();
            if (cameraCallback != null) {
                cameraCallback.onStopPreview();
            }
            running = false;
        }
    }

    /**
     * 设置闪光模式
     *
     * @param newFlashMode
     */
    public void setFlashMode(String newFlashMode) {
        if (camera != null) {
            if (debugMode) {
                Log.d(LOG_TAG, "setFlashMode：" + newFlashMode);
            }
            Camera.Parameters cameraParameters = camera.getParameters();
            cameraParameters.setFlashMode(newFlashMode);
            camera.setParameters(cameraParameters);
        }
    }

    /**
     * 设置是闪光灯常亮
     *
     * @param enable
     * @return 不支持闪光灯常亮或尚未打开Camera
     */
    public boolean setTorchFlash(boolean enable) {
        if (camera != null) {
            if (enable) {
                if (CameraUtils.isSupportFlashMode(getCamera(), Camera.Parameters.FLASH_MODE_TORCH)) {
                    if (debugMode) {
                        Log.d(LOG_TAG, "打开闪光灯");
                    }
                    setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    return true;
                } else {
                    if (debugMode) {
                        Log.d(LOG_TAG, "打开闪光灯失败，因为当前机器不支持闪光灯常亮");
                    }
                    return false;
                }
            } else {
                setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * 设置显示方向
     *
     * @param displayOrientation
     */
    public void setDisplayOrientation(int displayOrientation) {
        if (camera != null) {
            if (debugMode) {
                Log.d(LOG_TAG, "setDisplayOrientation：" + displayOrientation);
            }

            this.displayOrientation = displayOrientation;
            if (Build.VERSION.SDK_INT >= 9) {
                camera.setDisplayOrientation(displayOrientation);
            } else {
                Camera.Parameters cameraParameters = camera.getParameters();
                cameraParameters.setRotation(displayOrientation);
                camera.setParameters(cameraParameters);
            }
        }
    }

    /**
     * 释放
     */
    public void release() {
        if (camera != null) {
            if (debugMode) {
                Log.d(LOG_TAG, "release");
            }
            stopPreview();
            try {
                camera.setPreviewDisplay(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            camera.setPreviewCallback(null);
            camera.setErrorCallback(null);
            camera.setOneShotPreviewCallback(null);
            camera.setPreviewCallbackWithBuffer(null);
            camera.setZoomChangeListener(null);
            camera.release();
            camera = null;
            loopFocusManager.stop();
            loopFocusManager.setCamera(null);
        }
    }

    /**
     * 获取Camera
     *
     * @return
     */
    public Camera getCamera() {
        return camera;
    }

    /**
     * 设置Camera回调
     *
     * @param cameraCallback
     */
    public void setCameraCallback(CameraCallback cameraCallback) {
        this.cameraCallback = cameraCallback;
    }

    /**
     * 获取屏幕方向
     *
     * @return
     */
    public int getDisplayOrientation() {
        return displayOrientation;
    }

    /**
     * 是否是Debug模式
     *
     * @return
     */
    public boolean isDebugMode() {
        return debugMode;
    }

    /**
     * 设置是否是Debug模式
     *
     * @param debugMode
     */
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        this.loopFocusManager.setDebugMode(true);
    }

    /**
     * 获取循环对焦管理器
     *
     * @return 循环对焦管理器
     */
    public LoopFocusManager getLoopFocusManager() {
        return loopFocusManager;
    }

    /**
     * Camera相关事件回调
     */
    public interface CameraCallback {
        public void onInitCamera(Camera camera);

        public void onStartPreview();

        public void onStopPreview();
    }

    /**
     * 相机被占用异常
     */
    public class CameraBeingUsedException extends Exception {
        private static final long serialVersionUID = -410101242781061339L;
    }
}