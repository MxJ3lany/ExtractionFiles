package org.odk.collect.android.utilities;

/*
Copyright 2018 Theodoros Tyrovouzis

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.view.Surface;

import org.odk.collect.android.application.Collect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import timber.log.Timber;

public class CameraUtils {

    private CameraUtils() {

    }

    public static Camera getCameraInstance(Activity activity, int cameraId) {
        Camera camera = Camera.open(cameraId);
        camera.setDisplayOrientation(90);

        // Set the rotation of the camera which the output picture need.
        Camera.Parameters parameters = camera.getParameters();
        int rotation = getRotationInt(activity.getWindowManager().getDefaultDisplay().getRotation());
        parameters.setRotation(calcCameraRotation(cameraId, rotation));
        camera.setParameters(parameters);

        return camera;
    }

    private static int getRotationInt(int rotation) {
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
            default:
                Timber.e(new IllegalArgumentException(), "Invalid rotation");
                return -1;
        }
    }

    public static int getFrontCameraId() {
        for (int camNo = 0; camNo < Camera.getNumberOfCameras(); camNo++) {
            Camera.CameraInfo camInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(camNo, camInfo);

            if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return camNo;
            }
        }
        Timber.w("No Available front camera");
        return -1;
    }

    /**
     * Calculates the front camera rotation
     * <p>
     * This calculation is applied to the output JPEG either via Exif Orientation tag
     * or by actually transforming the bitmap. (Determined by vendor camera API implementation)
     * <p>
     * Note: This is not the same calculation as the display orientation
     *
     * @param screenOrientationDegrees Screen orientation in degrees
     * @return Number of degrees to rotate image in order for it to view correctly.
     */
    public static int calcCameraRotation(int cameraId, int screenOrientationDegrees) {
        Camera.CameraInfo camInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, camInfo);
        return (camInfo.orientation + screenOrientationDegrees) % 360;
    }

    public static void savePhoto(String path, byte[] data) {
        File tempFile = new File(path);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(data);
            fos.flush();
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    public static boolean isFrontCameraAvailable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                //https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics.html
                CameraManager cameraManager = (CameraManager) Collect.getInstance()
                        .getSystemService(Context.CAMERA_SERVICE);
                if (cameraManager != null) {
                    String[] cameraId = cameraManager.getCameraIdList();
                    for (String id : cameraId) {
                        CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                        Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                        if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                            return true;
                        }
                    }
                }
            } catch (CameraAccessException | NullPointerException e) {
                Timber.e(e);
            }
        } else {
            //https://developer.android.com/guide/topics/media/camera.html#check-camera-features
            for (int camNo = 0; camNo < Camera.getNumberOfCameras(); camNo++) {
                Camera.CameraInfo camInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(camNo, camInfo);
                if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    return true;
                }
            }
        }
        return false; // No front-facing camera found
    }
  
    public static String getVideoFilePath(Context context) {
        final File dir = context.getExternalFilesDir(null);
        return (dir == null ? "" : (dir.getAbsolutePath() + "/"))
                + System.currentTimeMillis() + ".mp4";
    }
}
