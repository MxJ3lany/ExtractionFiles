/*
 * Copyright (C) 2017 The JackKnife Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lwh.jackknife.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.view.Gravity;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtils {

    public static float dp2px(float dpVal, Context context) {
        return DensityUtils.dp2px(dpVal, context);
    }

    public static float sp2px(float spVal, Context context) {
        return DensityUtils.sp2px(spVal, context);
    }

    public static float px2dp(float pxVal, Context context) {
        return DensityUtils.px2dp(pxVal, context);
    }

    public static float px2sp(float pxVal, Context context) {
        return DensityUtils.px2sp(pxVal, context);
    }

    public static void saveAsJpeg(Bitmap bitmap, String path, int quality) {
        FileOutputStream fos = null;
        try {
            File file = new File(path);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void saveAsJpeg(Bitmap bitmap, String path) {
        saveAsJpeg(bitmap, path, 100);
    }

    public static void saveAsPng(Bitmap bitmap, String path, int quality) {
        FileOutputStream fos = null;
        try {
            File file = new File(path);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, fos);
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void saveAsPng(Bitmap bitmap, String path) {
        saveAsPng(bitmap, path, 100);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int requiredWidth,
                                             int requiredHeight) {
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;
        if (height > requiredHeight || width > requiredWidth) {
            int widthRatio = Math.round((float) width / (float) requiredWidth);
            int heightRatio = Math.round((float) height / (float) requiredHeight);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmap(Resources res, int resId, int requiredWidth,
                                             int requiredHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        options.inSampleSize = calculateInSampleSize(options, requiredWidth,
                requiredHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap takeScreenShot(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        Rect rect = new Rect();
        view.getWindowVisibleDisplayFrame(rect);
        int statusBarHeight = rect.top;
        Bitmap outputBitmap = Bitmap.createBitmap(bitmap, 0, statusBarHeight,
                SystemUtils.getScreenWidth(activity), SystemUtils.getScreenHeight(activity) - statusBarHeight);
        view.destroyDrawingCache();
        return outputBitmap;
    }

    public static int getPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap createBitmap(Context context, int resId) {
        return BitmapFactory.decodeResource(context.getResources(), resId);
    }

    public static Bitmap createBitmap(byte[] bytes) {
        if (bytes != null && bytes.length != 0) {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    public static Bitmap createBitmap(Bitmap bitmap, int width, int height) {
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    public static Bitmap createBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof NinePatchDrawable) {
            Bitmap bitmap = Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                            : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        } else {
            return null;
        }
    }

    public static Bitmap composeWatermark(Bitmap dstBitmap, Bitmap markBitmap, int gravity, float offsetX, float offsetY) {
        if (dstBitmap == null) {
            return null;
        }
        Bitmap outputBitmap = Bitmap.createBitmap(dstBitmap.getWidth(), dstBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outputBitmap);
        canvas.drawBitmap(dstBitmap, 0, 0, null);
        Rect outRect = new Rect();
        Rect containerRect = new Rect(0, 0, dstBitmap.getWidth(), dstBitmap.getHeight());
        Gravity.apply(gravity, outputBitmap.getWidth(), outputBitmap.getHeight(), containerRect, outRect);
        int left = (int) (outRect.left + offsetX);
        int top = (int) (outRect.top + offsetY);
        int right = (int) (outRect.right + offsetX);
        int bottom = (int) (outRect.bottom + offsetY);
        outRect.set(left, top, right, bottom);
        canvas.drawBitmap(markBitmap, null, outRect, null);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return outputBitmap;
    }

    public static Bitmap composeWatermark(Bitmap dstBitmap, Bitmap markBitmap) {
        return composeWatermark(dstBitmap, markBitmap, Gravity.END | Gravity.BOTTOM, 0, 0);
    }

    public static Bitmap zoomBitmap(Bitmap bitmap, int requiredWidth, int requiredHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) requiredWidth) / width;
        float scaleHeight = ((float) requiredHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

    public static Bitmap rotateBitmap(int angle, Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap outputBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        return outputBitmap;
    }

    public static void recycle(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
        System.gc();
    }

    public static Bitmap createImageThumbnail(String imagePath, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inJustDecodeBounds = false;
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        int w = outWidth / width;
        int h = outHeight / height;
        int inSampleSize;
        if (w < h) {
            inSampleSize = w;
        } else {
            inSampleSize = h;
        }
        if (inSampleSize <= 0) {
            inSampleSize = 1;
        }
        options.inSampleSize = inSampleSize;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    public static Bitmap makeReflectionBitmap(Bitmap bitmap) {
        int reflectionGap = 4;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);
        Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height / 2, width, height / 2, matrix,
                false);
        Bitmap bitmapWithReflection = Bitmap.createBitmap(width, (height + height / 2),
                Bitmap.Config.ARGB_8888);
        Paint defaultPaint = new Paint();
        Canvas canvas = new Canvas(bitmapWithReflection);
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.drawRect(0, height, width, height + reflectionGap, defaultPaint);
        canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);
        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0,
                bitmapWithReflection.getHeight() + reflectionGap, 0x70FFFFFF, 0x00FFFFFF,
                Shader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() + reflectionGap, paint);
        return bitmapWithReflection;
    }

    public static Bitmap makeBlackBitmap(Bitmap bitmap) {
        Bitmap outputBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(outputBitmap);
        Paint paint = new Paint();
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(matrix);
        paint.setColorFilter(f);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return outputBitmap;
    }

    public static Bitmap makeRoundBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left = 0, top = 0, right, bottom, dstLeft = 0, dstTop = 0, dstRight, dstBottom;
        if (width <= height) {
            roundPx = width / 2;
            right = width;
            bottom = width;
            height = width;
            dstRight = width;
            dstBottom = width;
        } else {
            roundPx = height / 2;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            bottom = height;
            width = height;
            dstRight = height;
            dstBottom = height;
        }
        Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outputBitmap);
        Paint paint = new Paint();
        Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
        Rect dst = new Rect((int) dstLeft, (int) dstTop, (int) dstRight, (int) dstBottom);
        RectF rectF = new RectF(dst);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, paint);
        return outputBitmap;
    }

    public static Bitmap makeRoundCornerBitmap(Bitmap bitmap, int pixels, int color) {
        Bitmap outputBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outputBitmap);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF rectF = new RectF(rect);
        float roundPx = pixels;
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return outputBitmap;
    }
}
