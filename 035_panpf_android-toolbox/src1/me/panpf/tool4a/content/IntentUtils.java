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

package me.panpf.tool4a.content;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;

/**
 * Intent工具箱
 */
public class IntentUtils {
    /**
     * 获取使用相机拍照的Intent
     *
     * @param saveFileUri 保存照片的文件
     * @return 使用相机拍照的Intent
     */
    public static Intent getTakePhotosIntent(Uri saveFileUri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (saveFileUri != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, saveFileUri);
        }
        return intent;
    }

    /**
     * 获取使用相机拍照的Intent
     *
     * @return 使用相机拍照的Intent
     */
    public static Intent getTakePhotosIntent() {
        return new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    }

    /**
     * 获取录音Intent
     *
     * @return 录音Intent
     */
    public static Intent getRecordingIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/amr");
        return intent;
    }

    /**
     * 获取安装给定APK文件的应用程序的Intent
     *
     * @param apkFile 给定APK文件
     * @return 安装给定APK文件的Intent
     */
    public static Intent getInstallAppIntent(File apkFile) {
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        return intent;
    }

    /**
     * 获取卸载给定包名的应用程序的Intent
     *
     * @param packageName 给定包名，例如：com.example
     * @return 卸载给定包名的应用程序的Intent
     */
    public static Intent getUninstallAppIntent(String packageName) {
        return new Intent(Intent.ACTION_DELETE, Uri.parse("package: " + packageName));
    }

    /**
     * 获取能够启动指定应用程序的Intent
     *
     * @param context     上下文
     * @param packageName 要启动的应用程序的包名，例如：com.example
     * @return Intent
     */
    public static Intent getLaunchAppIntent(Context context, String packageName) {
        return context.getPackageManager().getLaunchIntentForPackage(packageName);
    }

    /**
     * 获取能够跳转到指定App的系统应用信息页面的Intent
     *
     * @param packageName 包名
     **/
    public static Intent getLaunchAppInfoPageIntent(String packageName) {
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.applications.InstalledAppDetails"));
        intent.setData(Uri.parse("package:" + packageName));
        return intent;
    }

    /**
     * 获取能够打开拨号界面的Inteng，需要CALL_PHONE权限
     *
     * @param phoneNumber 要呼叫的电话号码
     */
    public static Intent getLaunchDialingIntent(String phoneNumber) {
        return new Intent(Intent.ACTION_DIAL, UriUtils.getCallUri(phoneNumber));
    }

    /**
     * 获取呼叫给定的电话号码的Intent，需要CALL_PHONE权限
     *
     * @param phoneNumber 要呼叫的电话号码
     */
    public static Intent getCallPhoneIntent(String phoneNumber) {
        return new Intent(Intent.ACTION_CALL, UriUtils.getCallUri(phoneNumber));
    }

    /**
     * 获取打开给定的页面的Intent
     *
     * @param url 要打开的web页面的地址
     */
    public static Intent getLaunchWebBrowserIntent(String url) {
        return new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    }

    /**
     * 获取能够启动发送短信页面的Intent
     *
     * @param phoneNumber    目标手机号
     * @param messageContent 短信内容
     */
    public static Intent getLaunchSendSmsIntent(String phoneNumber, String messageContent) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, UriUtils.getSmsUri(phoneNumber));
        intent.putExtra("sms_body", messageContent);
        return intent;
    }

    /**
     * 从图库获取图片Intent构造器
     */
    public static class GetImageFromGalleryIntentBuilder {
        private boolean crop;   // 是否裁剪
        private int width;  // 裁剪后的得到的图片的宽度
        private int height; // 裁剪后的得到的图片的高度
        private Uri saveFileUri;    // 保存裁剪后的图片的文件的Uri
        private boolean scale;  // 设置是否将裁剪后得到的图片缩放后再放在Intent中返回

        public Intent build() {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            if (crop) {
                //设置裁剪框的宽高比例
                intent.putExtra("aspectX", width);
                intent.putExtra("aspectY", height);
                //设置裁剪后得到的图片的宽度
                intent.putExtra("outputX", width);
                //设置裁剪后得到的图片的高度
                intent.putExtra("outputY", height);
                if (saveFileUri != null) {
                    //设置将裁剪后得到的图片保存在给东Uri的文件中，此文件必须存在。”output“与“return-data”只能任选其一
                    intent.putExtra("output", saveFileUri);
                } else {
                    //设置将裁剪后得到的图片保存在Inteng中返回。“return-data”与”output“只能任选其一
                    intent.putExtra("return-data", true);
                    //设置是否将裁剪后得到的图片缩放后再放在Intent中返回
                    intent.putExtra("scale", scale);
                }
            }
            return intent;
        }

        public void crop(int width, int height) {
            this.crop = true;
            this.width = width;
            this.height = height;
        }

        public void savePath(Uri saveFileUri) {
            this.saveFileUri = saveFileUri;
        }

        public void scale() {
            this.scale = true;
        }
    }

    /**
     * 图片裁剪Intent构造器
     */
    public static class ImageCropIntentBuilder {
        private Uri sourceFileUri;  // 要裁剪的文件的Uri
        private Uri saveFileUri;    // 保存裁剪后的图片的文件的Uri
        private int width;  // 裁剪后的得到的图片的宽度
        private int height; // 裁剪后的得到的图片的高度
        private boolean scale;  // 设置是否将裁剪后得到的图片缩放后再放在Intent中返回

        /**
         * 创建图片裁剪Intent构造器
         *
         * @param sourceFileUri 要裁剪的图片的Uri
         * @param saveFileUri   保存裁剪后得到的图片的文件的Uri
         * @param width         裁剪后得到的图片的宽度
         * @param height        裁剪后得到的图片的高度
         */
        public ImageCropIntentBuilder(Uri sourceFileUri, Uri saveFileUri, int width, int height) {
            setSourceFileUri(sourceFileUri);
            setSaveFileUri(saveFileUri);
            setWidth(width);
            setHeight(height);
        }

        /**
         * 创建图片裁剪Intent构造器
         *
         * @param sourceFileUri 要裁剪的图片的Uri
         * @param width         裁剪后得到的图片的宽度
         * @param height        裁剪后得到的图片的高度
         */
        public ImageCropIntentBuilder(Uri sourceFileUri, int width, int height) {
            this(sourceFileUri, null, width, height);
        }

        /**
         * 创建Intent
         *
         * @return
         */
        public Intent build() {
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(getSourceFileUri(), "image/*");
            //设置要让系统的camea执行裁剪操作
            intent.putExtra("crop", true);
            //设置裁剪框的宽高比例
            intent.putExtra("aspectX", getWidth());
            intent.putExtra("aspectY", getHeight());
            //设置裁剪后得到的图片的宽度
            intent.putExtra("outputX", getWidth());
            //设置裁剪后得到的图片的高度
            intent.putExtra("outputY", getHeight());
            if (getSaveFileUri() != null) {
                //设置将裁剪后得到的图片保存在给东Uri的文件中，此文件必须存在。”output“与“return-data”只能任选其一
                intent.putExtra("output", getSaveFileUri());
            } else {
                //设置将裁剪后得到的图片保存在Inteng中返回。“return-data”与”output“只能任选其一
                intent.putExtra("return-data", true);
                //设置是否将裁剪后得到的图片缩放后再放在Intent中返回
                intent.putExtra("scale", isScale());
            }
            return intent;
        }

        public Uri getSourceFileUri() {
            return sourceFileUri;
        }

        public void setSourceFileUri(Uri sourceFileUri) {
            this.sourceFileUri = sourceFileUri;
        }

        public Uri getSaveFileUri() {
            return saveFileUri;
        }

        public void setSaveFileUri(Uri saveFileUri) {
            this.saveFileUri = saveFileUri;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public boolean isScale() {
            return scale;
        }

        public void setScale(boolean scale) {
            this.scale = scale;
        }
    }
}
