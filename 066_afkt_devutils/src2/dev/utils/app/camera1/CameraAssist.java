package dev.utils.app.camera1;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import dev.DevUtils;
import dev.utils.LogPrintUtils;

import static android.hardware.Camera.Parameters.FLASH_MODE_OFF;
import static android.hardware.Camera.Parameters.FLASH_MODE_TORCH;

/**
 * detail: 摄像头辅助类
 * @author Ttt
 */
public final class CameraAssist {

    // 日志 TAG
    private final String TAG = CameraAssist.class.getSimpleName();
    // 摄像头对象
    private Camera mCamera;
    // 是否预览中
    private boolean mPreviewing;
    // 自动对焦时间间隔
    private long mAutoInterval = 2000L;
    // 预览通知
    private PreviewNotify mPreviewNotify;

    // ==============
    // = 内部工具类 =
    // ==============

    // 摄像头大小计算
    private CameraSizeAssist mCameraSizeAssist;
    // 自动获取焦点辅助类
    private AutoFocusAssist mAutoFocusAssist;

    public CameraAssist() {
    }

    /**
     * 构造函数
     * @param camera {@link android.hardware.Camera}
     */
    public CameraAssist(final Camera camera) {
        setCamera(camera);
    }

    /**
     * 构造函数
     * @param camera   {@link android.hardware.Camera}
     * @param interval 自动对焦时间间隔
     */
    public CameraAssist(final Camera camera, final long interval) {
        this.mAutoInterval = interval;
        setCamera(camera);
    }

    // ============
    // = 操作方法 =
    // ============

    /**
     * 打开摄像头程序
     * @param holder {@link SurfaceHolder}
     * @return {@link CameraAssist}
     * @throws Exception 设置预览画面, 异常时抛出
     */
    public synchronized CameraAssist openDriver(final SurfaceHolder holder) throws Exception {
        Camera theCamera = mCamera;
        // 设置预览 Holder
        theCamera.setPreviewDisplay(holder);
        return this;
    }

    /**
     * 关闭摄像头程序
     */
    public synchronized void closeDriver() {
        // 释放摄像头资源
        freeCameraResource();
    }

    // ============
    // = 预览相关 =
    // ============

    /**
     * 开始将 Camera 画面预览到手机上
     */
    public synchronized void startPreview() {
        Camera theCamera = mCamera;
        if (theCamera != null && !mPreviewing) {
            // 开始预览
            theCamera.startPreview();
            // 表示预览中
            mPreviewing = true;
            // 初始化自动获取焦点
            mAutoFocusAssist = new AutoFocusAssist(mCamera, mAutoInterval);
            // 开始预览通知
            if (mPreviewNotify != null) {
                mPreviewNotify.startPreviewNotify();
            }
        }
    }

    /**
     * 停止 Camera 画面预览
     */
    public synchronized void stopPreview() {
        if (mAutoFocusAssist != null) {
            mAutoFocusAssist.stop();
            mAutoFocusAssist = null;
        }
        if (mCamera != null && mPreviewing) {
            // 停止预览
            mCamera.stopPreview();
            // 表示非预览中
            mPreviewing = false;
            // 停止预览通知
            if (mPreviewNotify != null) {
                mPreviewNotify.stopPreviewNotify();
            }
        }
    }

    /**
     * 释放摄像头资源
     */
    private void freeCameraResource() {
        try {
            if (mCamera != null) {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.lock();
                mCamera.release();
            }
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "freeCameraResource");
        } finally {
            mCamera = null;
        }
    }

    // ==============
    // = 摄像头相关 =
    // ==============

    // 预览尺寸大小
    private Camera.Size mPreviewSize = null;

    /**
     * 获取相机分辨率
     * @return {@link Camera.Size} 相机分辨率
     */
    public Camera.Size getCameraResolution() {
        if (mPreviewSize == null) {
            // 获取预览大小
            mPreviewSize = mCameraSizeAssist.getPreviewSize();
            return mPreviewSize;
        }
        return mPreviewSize;
    }

    /**
     * 获取预览分辨率
     * @return {@link Camera.Size} 预览分辨率
     */
    public Camera.Size getPreviewSize() {
        if (null != mCamera && null != mCamera.getParameters()) {
            return mCamera.getParameters().getPreviewSize();
        }
        return null;
    }

    /**
     * 获取 Camera.Size 计算辅助类
     * @return {@link CameraSizeAssist}
     */
    public CameraSizeAssist getCameraSizeAssist() {
        return mCameraSizeAssist;
    }

    /**
     * 获取摄像头
     * @return {@link android.hardware.Camera}
     */
    public Camera getCamera() {
        return mCamera;
    }

    /**
     * 设置摄像头
     * @param camera {@link android.hardware.Camera}
     */
    public void setCamera(final Camera camera) {
        this.mCamera = camera;
        // 初始化 Camera 大小
        this.mCameraSizeAssist = new CameraSizeAssist(mCamera);
    }

    /**
     * 设置预览回调
     * @param previewNotify 预览通知接口
     * @return {@link CameraSizeAssist}
     */
    public CameraAssist setPreviewNotify(final PreviewNotify previewNotify) {
        this.mPreviewNotify = previewNotify;
        return this;
    }

    /**
     * 设置是否开启自动对焦
     * @param autoFocus 是否自动对焦
     * @return {@link CameraSizeAssist}
     */
    public CameraAssist setAutoFocus(final boolean autoFocus) {
        if (mAutoFocusAssist != null) {
            mAutoFocusAssist.setAutoFocus(autoFocus);
        }
        return this;
    }

    /**
     * 是否预览中
     * @return {@code true} 预览中, {@code false} 非预览
     */
    public boolean isPreviewing() {
        return mPreviewing;
    }

    /**
     * 设置自动对焦时间间隔
     * @param autoInterval 自动对焦时间间隔
     */
    public void setAutoInterval(final long autoInterval) {
        this.mAutoInterval = autoInterval;
    }

    // ================================
    // = FlashlightUtils - 闪光灯相关 =
    // ================================

    /**
     * 打开闪光灯
     */
    public void setFlashlightOn() {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (!FLASH_MODE_TORCH.equals(parameters.getFlashMode())) {
            parameters.setFlashMode(FLASH_MODE_TORCH);
            mCamera.setParameters(parameters);
        }
    }

    /**
     * 关闭闪光灯
     */
    public void setFlashlightOff() {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (FLASH_MODE_TORCH.equals(parameters.getFlashMode())) {
            parameters.setFlashMode(FLASH_MODE_OFF);
            mCamera.setParameters(parameters);
        }
    }

    /**
     * 是否打开闪光灯
     * @return {@code true} 打开, {@code false} 关闭
     */
    public boolean isFlashlightOn() {
        if (mCamera == null) {
            return false;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        return FLASH_MODE_TORCH.equals(parameters.getFlashMode());
    }

    /**
     * 是否支持手机闪光灯
     * @return {@code true} 支持, {@code false} 不支持
     */
    public static boolean isFlashlightEnable() {
        return DevUtils.getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    // ========
    // = 接口 =
    // ========

    /**
     * detail: 预览通知接口
     * @author Ttt
     */
    public interface PreviewNotify {

        /**
         * 停止预览通知
         */
        void stopPreviewNotify();

        /**
         * 开始预览通知
         */
        void startPreviewNotify();
    }
}
