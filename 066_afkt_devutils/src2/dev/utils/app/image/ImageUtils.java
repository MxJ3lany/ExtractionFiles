package dev.utils.app.image;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import dev.DevUtils;
import dev.utils.LogPrintUtils;
import dev.utils.common.CloseUtils;

/**
 * detail: 图片相关工具类
 * @author Ttt
 */
public final class ImageUtils {

    private ImageUtils() {
    }

    // 日志 TAG
    private static final String TAG = ImageUtils.class.getSimpleName();

    /**
     * 获取 bitmap
     * @param file
     * @return
     */
    public static Bitmap getBitmap(final File file) {
        if (file == null) return null;
        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }

    /**
     * 获取 bitmap
     * @param file
     * @param maxWidth  最大宽度
     * @param maxHeight 最大高度
     * @return
     */
    public static Bitmap getBitmap(final File file, final int maxWidth, final int maxHeight) {
        if (file == null) return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }

    /**
     * 获取 bitmap
     * @param filePath
     * @return
     */
    public static Bitmap getBitmap(final String filePath) {
        if (isSpace(filePath)) return null;
        return BitmapFactory.decodeFile(filePath);
    }

    /**
     * 获取 bitmap
     * @param filePath
     * @param maxWidth  最大宽度
     * @param maxHeight 最大高度
     * @return
     */
    public static Bitmap getBitmap(final String filePath, final int maxWidth, final int maxHeight) {
        if (isSpace(filePath)) return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 获取 bitmap
     * @param is 输入流
     * @return
     */
    public static Bitmap getBitmap(final InputStream is) {
        if (is == null) return null;
        return BitmapFactory.decodeStream(is);
    }

    /**
     * 获取 bitmap
     * @param resId 资源 id
     * @return
     */
    public static Bitmap getBitmap(@DrawableRes final int resId) {
        Drawable drawable = ContextCompat.getDrawable(DevUtils.getContext(), resId);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * 获取 bitmap
     * @param resId     资源 id
     * @param maxWidth  最大宽度
     * @param maxHeight 最大高度
     * @return
     */
    public static Bitmap getBitmap(@DrawableRes final int resId, final int maxWidth, final int maxHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        final Resources resources = DevUtils.getContext().getResources();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, resId, options);
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resources, resId, options);
    }

    /**
     * 获取 bitmap
     * @param fd 文件描述
     * @return
     */
    public static Bitmap getBitmap(final FileDescriptor fd) {
        if (fd == null) return null;
        return BitmapFactory.decodeFileDescriptor(fd);
    }

    /**
     * 获取 bitmap
     * @param fd        文件描述
     * @param maxWidth  最大宽度
     * @param maxHeight 最大高度
     * @return
     */
    public static Bitmap getBitmap(final FileDescriptor fd, final int maxWidth, final int maxHeight) {
        if (fd == null) return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, null, options);
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fd, null, options);
    }

    /**
     * 缩放图片
     * @param src       源图片
     * @param newWidth  新宽度
     * @param newHeight 新高度
     * @return 缩放后的图片
     */
    public static Bitmap scale(final Bitmap src, final int newWidth, final int newHeight) {
        return scale(src, newWidth, newHeight, false);
    }

    /**
     * 缩放图片
     * @param src       源图片
     * @param newWidth  新宽度
     * @param newHeight 新高度
     * @param recycle   是否回收
     * @return 缩放后的图片
     */
    public static Bitmap scale(final Bitmap src, final int newWidth, final int newHeight, final boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        Bitmap ret = Bitmap.createScaledBitmap(src, newWidth, newHeight, true);
        if (recycle && !src.isRecycled() && ret != src) src.recycle();
        return ret;
    }

    /**
     * 缩放图片
     * @param src         源图片
     * @param scaleWidth  缩放宽度倍数
     * @param scaleHeight 缩放高度倍数
     * @return 缩放后的图片
     */
    public static Bitmap scale(final Bitmap src, final float scaleWidth, final float scaleHeight) {
        return scale(src, scaleWidth, scaleHeight, false);
    }

    /**
     * 缩放图片
     * @param src         源图片
     * @param scaleWidth  缩放宽度倍数
     * @param scaleHeight 缩放高度倍数
     * @param recycle     是否回收
     * @return 缩放后的图片
     */
    public static Bitmap scale(final Bitmap src, final float scaleWidth, final float scaleHeight, final boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        Matrix matrix = new Matrix();
        matrix.setScale(scaleWidth, scaleHeight);
        Bitmap ret = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        if (recycle && !src.isRecycled() && ret != src) src.recycle();
        return ret;
    }

    /**
     * 裁剪图片
     * @param src    源图片
     * @param x      开始坐标 x
     * @param y      开始坐标 y
     * @param width  裁剪宽度
     * @param height 裁剪高度
     * @return 裁剪后的图片
     */
    public static Bitmap clip(final Bitmap src, final int x, final int y, final int width, final int height) {
        return clip(src, x, y, width, height, false);
    }

    /**
     * 裁剪图片
     * @param src     源图片
     * @param x       开始坐标 x
     * @param y       开始坐标 y
     * @param width   裁剪宽度
     * @param height  裁剪高度
     * @param recycle 是否回收
     * @return 裁剪后的图片
     */
    public static Bitmap clip(final Bitmap src, final int x, final int y, final int width, final int height, final boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        Bitmap ret = Bitmap.createBitmap(src, x, y, width, height);
        if (recycle && !src.isRecycled() && ret != src) src.recycle();
        return ret;
    }

    /**
     * 倾斜图片
     * @param src 源图片
     * @param kx  倾斜因子 x
     * @param ky  倾斜因子 y
     * @return 倾斜后的图片
     */
    public static Bitmap skew(final Bitmap src, final float kx, final float ky) {
        return skew(src, kx, ky, 0, 0, false);
    }

    /**
     * 倾斜图片
     * @param src     源图片
     * @param kx      倾斜因子 x
     * @param ky      倾斜因子 y
     * @param recycle 是否回收
     * @return 倾斜后的图片
     */
    public static Bitmap skew(final Bitmap src, final float kx, final float ky, final boolean recycle) {
        return skew(src, kx, ky, 0, 0, recycle);
    }

    /**
     * 倾斜图片
     * @param src 源图片
     * @param kx  倾斜因子 x
     * @param ky  倾斜因子 y
     * @param px  平移因子 x
     * @param py  平移因子 y
     * @return 倾斜后的图片
     */
    public static Bitmap skew(final Bitmap src, final float kx, final float ky, final float px, final float py) {
        return skew(src, kx, ky, px, py, false);
    }

    /**
     * 倾斜图片
     * @param src     源图片
     * @param kx      倾斜因子 x
     * @param ky      倾斜因子 y
     * @param px      平移因子 x
     * @param py      平移因子 y
     * @param recycle 是否回收
     * @return 倾斜后的图片
     */
    public static Bitmap skew(final Bitmap src, final float kx, final float ky, final float px, final float py, final boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        Matrix matrix = new Matrix();
        matrix.setSkew(kx, ky, px, py);
        Bitmap ret = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        if (recycle && !src.isRecycled() && ret != src) src.recycle();
        return ret;
    }

    /**
     * 旋转图片
     * @param src     源图片
     * @param degrees 旋转角度
     * @param px      旋转点横坐标
     * @param py      旋转点纵坐标
     * @return 旋转后的图片
     */
    public static Bitmap rotate(final Bitmap src, final int degrees, final float px, final float py) {
        return rotate(src, degrees, px, py, false);
    }

    /**
     * 旋转图片
     * @param src     源图片
     * @param degrees 旋转角度
     * @param px      旋转点横坐标
     * @param py      旋转点纵坐标
     * @param recycle 是否回收
     * @return 旋转后的图片
     */
    public static Bitmap rotate(final Bitmap src, final int degrees, final float px, final float py, final boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        if (degrees == 0) return src;
        Matrix matrix = new Matrix();
        matrix.setRotate(degrees, px, py);
        Bitmap ret = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        if (recycle && !src.isRecycled() && ret != src) src.recycle();
        return ret;
    }

    /**
     * 获取图片旋转角度 - 返回 -1 表示异常
     * @param filePath
     * @return 旋转角度
     */
    public static int getRotateDegree(final String filePath) {
        try {
            ExifInterface exifInterface = new ExifInterface(filePath);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return 0;
            }
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "getRotateDegree");
            return -1;
        }
    }

    /**
     * 转为圆形图片
     * @param src 源图片
     * @return 圆形图片
     */
    public static Bitmap toRound(final Bitmap src) {
        return toRound(src, 0, 0, false);
    }

    /**
     * 转为圆形图片
     * @param src     源图片
     * @param recycle 是否回收
     * @return 圆形图片
     */
    public static Bitmap toRound(final Bitmap src, final boolean recycle) {
        return toRound(src, 0, 0, recycle);
    }

    /**
     * 转为圆形图片
     * @param src         源图片
     * @param borderSize  边框尺寸
     * @param borderColor 边框颜色
     * @return 圆形图片
     */
    public static Bitmap toRound(final Bitmap src, @IntRange(from = 0) final int borderSize, @ColorInt final int borderColor) {
        return toRound(src, borderSize, borderColor, false);
    }

    /**
     * 转为圆形图片
     * @param src         源图片
     * @param borderSize  边框尺寸
     * @param borderColor 边框颜色
     * @param recycle     是否回收
     * @return 圆形图片
     */
    public static Bitmap toRound(final Bitmap src, @IntRange(from = 0) final int borderSize, @ColorInt final int borderColor, final boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        int width = src.getWidth();
        int height = src.getHeight();
        int size = Math.min(width, height);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Bitmap ret = Bitmap.createBitmap(width, height, src.getConfig());
        float center = size / 2f;
        RectF rectF = new RectF(0, 0, width, height);
        rectF.inset((width - size) / 2f, (height - size) / 2f);
        Matrix matrix = new Matrix();
        matrix.setTranslate(rectF.left, rectF.top);
        if (width != height) {
            matrix.preScale((float) size / width, (float) size / height);
        }
        BitmapShader shader = new BitmapShader(src, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        shader.setLocalMatrix(matrix);
        paint.setShader(shader);
        Canvas canvas = new Canvas(ret);
        canvas.drawRoundRect(rectF, center, center, paint);
        if (borderSize > 0) {
            paint.setShader(null);
            paint.setColor(borderColor);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(borderSize);
            float radius = center - borderSize / 2f;
            canvas.drawCircle(width / 2f, height / 2f, radius, paint);
        }
        if (recycle && !src.isRecycled() && ret != src) src.recycle();
        return ret;
    }

    /**
     * 转为圆角图片
     * @param src    源图片
     * @param radius 圆角的度数
     * @return 圆角图片
     */
    public static Bitmap toRoundCorner(final Bitmap src, final float radius) {
        return toRoundCorner(src, radius, 0, 0, false);
    }

    /**
     * 转为圆角图片
     * @param src     源图片
     * @param radius  圆角的度数
     * @param recycle 是否回收
     * @return 圆角图片
     */
    public static Bitmap toRoundCorner(final Bitmap src, final float radius, final boolean recycle) {
        return toRoundCorner(src, radius, 0, 0, recycle);
    }

    /**
     * 转为圆角图片
     * @param src         源图片
     * @param radius      圆角的度数
     * @param borderSize  边框尺寸
     * @param borderColor 边框颜色
     * @return 圆角图片
     */
    public static Bitmap toRoundCorner(final Bitmap src, final float radius, @IntRange(from = 0) final int borderSize, @ColorInt final int borderColor) {
        return toRoundCorner(src, radius, borderSize, borderColor, false);
    }

    /**
     * 转为圆角图片
     * @param src         源图片
     * @param radius      圆角的度数
     * @param borderSize  边框尺寸
     * @param borderColor 边框颜色
     * @param recycle     是否回收
     * @return 圆角图片
     */
    public static Bitmap toRoundCorner(final Bitmap src, final float radius, @IntRange(from = 0) final int borderSize, @ColorInt final int borderColor, final boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        int width = src.getWidth();
        int height = src.getHeight();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Bitmap ret = Bitmap.createBitmap(width, height, src.getConfig());
        BitmapShader shader = new BitmapShader(src, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        Canvas canvas = new Canvas(ret);
        RectF rectF = new RectF(0, 0, width, height);
        float halfBorderSize = borderSize / 2f;
        rectF.inset(halfBorderSize, halfBorderSize);
        canvas.drawRoundRect(rectF, radius, radius, paint);
        if (borderSize > 0) {
            paint.setShader(null);
            paint.setColor(borderColor);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(borderSize);
            paint.setStrokeCap(Paint.Cap.ROUND);
            canvas.drawRoundRect(rectF, radius, radius, paint);
        }
        if (recycle && !src.isRecycled() && ret != src) src.recycle();
        return ret;
    }

    /**
     * 添加圆角边框
     * @param src          源图片
     * @param borderSize   边框尺寸
     * @param color        边框颜色
     * @param cornerRadius 圆角半径
     * @return 圆角边框图
     */
    public static Bitmap addCornerBorder(final Bitmap src, @IntRange(from = 1) final int borderSize, @ColorInt final int color, @FloatRange(from = 0) final float cornerRadius) {
        return addBorder(src, borderSize, color, false, cornerRadius, false);
    }

    /**
     * 添加圆角边框
     * @param src          源图片
     * @param borderSize   边框尺寸
     * @param color        边框颜色
     * @param cornerRadius 圆角半径
     * @param recycle      是否回收
     * @return 圆角边框图
     */
    public static Bitmap addCornerBorder(final Bitmap src, @IntRange(from = 1) final int borderSize, @ColorInt final int color, @FloatRange(from = 0) final float cornerRadius, final boolean recycle) {
        return addBorder(src, borderSize, color, false, cornerRadius, recycle);
    }

    /**
     * 添加圆形边框
     * @param src        源图片
     * @param borderSize 边框尺寸
     * @param color      边框颜色
     * @return 圆形边框图
     */
    public static Bitmap addCircleBorder(final Bitmap src, @IntRange(from = 1) final int borderSize, @ColorInt final int color) {
        return addBorder(src, borderSize, color, true, 0, false);
    }

    /**
     * 添加圆形边框
     * @param src        源图片
     * @param borderSize 边框尺寸
     * @param color      边框颜色
     * @param recycle    是否回收
     * @return 圆形边框图
     */
    public static Bitmap addCircleBorder(final Bitmap src, @IntRange(from = 1) final int borderSize, @ColorInt final int color, final boolean recycle) {
        return addBorder(src, borderSize, color, true, 0, recycle);
    }

    /**
     * 添加边框
     * @param src          源图片
     * @param borderSize   边框尺寸
     * @param color        边框颜色
     * @param isCircle     是否画圆
     * @param cornerRadius 圆角半径
     * @param recycle      是否回收
     * @return 边框图
     */
    private static Bitmap addBorder(final Bitmap src, @IntRange(from = 1) final int borderSize, @ColorInt final int color,
                                    final boolean isCircle, final float cornerRadius, final boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        Bitmap ret = recycle ? src : src.copy(src.getConfig(), true);
        int width = ret.getWidth();
        int height = ret.getHeight();
        Canvas canvas = new Canvas(ret);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(borderSize);
        if (isCircle) {
            float radius = Math.min(width, height) / 2f - borderSize / 2f;
            canvas.drawCircle(width / 2f, height / 2f, radius, paint);
        } else {
            int halfBorderSize = borderSize >> 1;
            RectF rectF = new RectF(halfBorderSize, halfBorderSize, width - halfBorderSize, height - halfBorderSize);
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);
        }
        return ret;
    }

    /**
     * 添加倒影
     * @param src              源图片的
     * @param reflectionHeight 倒影高度
     * @return 带倒影图片
     */
    public static Bitmap addReflection(final Bitmap src, final int reflectionHeight) {
        return addReflection(src, reflectionHeight, false);
    }

    /**
     * 添加倒影
     * @param src              源图片的
     * @param reflectionHeight 倒影高度
     * @param recycle          是否回收
     * @return 带倒影图片
     */
    public static Bitmap addReflection(final Bitmap src, final int reflectionHeight, final boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        // 原图与倒影之间的间距
        final int REFLECTION_GAP = 0;
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);
        Bitmap reflectionBitmap = Bitmap.createBitmap(src, 0, srcHeight - reflectionHeight, srcWidth, reflectionHeight, matrix, false);
        Bitmap ret = Bitmap.createBitmap(srcWidth, srcHeight + reflectionHeight, src.getConfig());
        Canvas canvas = new Canvas(ret);
        canvas.drawBitmap(src, 0, 0, null);
        canvas.drawBitmap(reflectionBitmap, 0, srcHeight + REFLECTION_GAP, null);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        LinearGradient shader = new LinearGradient(0, srcHeight, 0, ret.getHeight() + REFLECTION_GAP, 0x70FFFFFF, 0x00FFFFFF, Shader.TileMode.MIRROR);
        paint.setShader(shader);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawRect(0, srcHeight + REFLECTION_GAP, srcWidth, ret.getHeight(), paint);
        if (!reflectionBitmap.isRecycled()) reflectionBitmap.recycle();
        if (recycle && !src.isRecycled() && ret != src) src.recycle();
        return ret;
    }

    /**
     * 添加文字水印
     * @param src      源图片
     * @param content  水印文本
     * @param textSize 水印字体大小
     * @param color    水印字体颜色
     * @param x        起始坐标 x
     * @param y        起始坐标 y
     * @return 带有文字水印的图片
     */
    public static Bitmap addTextWatermark(final Bitmap src, final String content, final int textSize,
                                          @ColorInt final int color, final float x, final float y) {
        return addTextWatermark(src, content, textSize, color, x, y, false);
    }

    /**
     * 添加文字水印
     * @param src      源图片
     * @param content  水印文本
     * @param textSize 水印字体大小
     * @param color    水印字体颜色
     * @param x        起始坐标 x
     * @param y        起始坐标 y
     * @param recycle  是否回收
     * @return 带有文字水印的图片
     */
    public static Bitmap addTextWatermark(final Bitmap src, final String content, final float textSize,
                                          @ColorInt final int color, final float x, final float y, final boolean recycle) {
        if (isEmptyBitmap(src) || content == null) return null;
        Bitmap ret = src.copy(src.getConfig(), true);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Canvas canvas = new Canvas(ret);
        paint.setColor(color);
        paint.setTextSize(textSize);
        Rect bounds = new Rect();
        paint.getTextBounds(content, 0, content.length(), bounds);
        canvas.drawText(content, x, y + textSize, paint);
        if (recycle && !src.isRecycled() && ret != src) src.recycle();
        return ret;
    }

    /**
     * 添加图片水印
     * @param src       源图片
     * @param watermark 图片水印
     * @param x         起始坐标 x
     * @param y         起始坐标 y
     * @param alpha     透明度
     * @return 带有图片水印的图片
     */
    public static Bitmap addImageWatermark(final Bitmap src, final Bitmap watermark, final int x, final int y, final int alpha) {
        return addImageWatermark(src, watermark, x, y, alpha, false);
    }

    /**
     * 添加图片水印
     * @param src       源图片
     * @param watermark 图片水印
     * @param x         起始坐标 x
     * @param y         起始坐标 y
     * @param alpha     透明度
     * @param recycle   是否回收
     * @return 带有图片水印的图片
     */
    public static Bitmap addImageWatermark(final Bitmap src, final Bitmap watermark, final int x, final int y, final int alpha, final boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        Bitmap ret = src.copy(src.getConfig(), true);
        if (!isEmptyBitmap(watermark)) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            Canvas canvas = new Canvas(ret);
            paint.setAlpha(alpha);
            canvas.drawBitmap(watermark, x, y, paint);
        }
        if (recycle && !src.isRecycled() && ret != src) src.recycle();
        return ret;
    }

    /**
     * 转为 alpha 位图
     * @param src 源图片
     * @return alpha 位图
     */
    public static Bitmap toAlpha(final Bitmap src) {
        return toAlpha(src, false);
    }

    /**
     * 转为 alpha 位图
     * @param src     源图片
     * @param recycle 是否回收
     * @return alpha 位图
     */
    public static Bitmap toAlpha(final Bitmap src, final Boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        Bitmap ret = src.extractAlpha();
        if (recycle && !src.isRecycled() && ret != src) src.recycle();
        return ret;
    }

    /**
     * 转为灰度图片
     * @param src 源图片
     * @return 灰度图
     */
    public static Bitmap toGray(final Bitmap src) {
        return toGray(src, false);
    }

    /**
     * 转为灰度图片
     * @param src     源图片
     * @param recycle 是否回收
     * @return 灰度图
     */
    public static Bitmap toGray(final Bitmap src, final boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        Bitmap ret = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        Canvas canvas = new Canvas(ret);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter colorMatrixColorFilter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(colorMatrixColorFilter);
        canvas.drawBitmap(src, 0, 0, paint);
        if (recycle && !src.isRecycled() && ret != src) src.recycle();
        return ret;
    }

    /**
     * 快速模糊 - 先缩小原图, 对小图进行模糊, 再放大回原先尺寸
     * @param src    源图片
     * @param scale  缩放比例(0...1)
     * @param radius 模糊半径
     * @return 模糊后的图片
     */
    public static Bitmap fastBlur(final Bitmap src,
                                  @FloatRange(from = 0, to = 1, fromInclusive = false) final float scale,
                                  @FloatRange(from = 0, to = 25, fromInclusive = false) final float radius) {
        return fastBlur(src, scale, radius, false);
    }

    /**
     * 快速模糊图片 - 先缩小原图, 对小图进行模糊, 再放大回原先尺寸
     * @param src     源图片
     * @param scale   缩放比例(0...1)
     * @param radius  模糊半径(0...25)
     * @param recycle 是否回收
     * @return 模糊后的图片
     */
    public static Bitmap fastBlur(final Bitmap src,
                                  @FloatRange(from = 0, to = 1, fromInclusive = false) final float scale,
                                  @FloatRange(from = 0, to = 25, fromInclusive = false) final float radius, final boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        int width = src.getWidth();
        int height = src.getHeight();
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        Bitmap scaleBitmap = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
        Canvas canvas = new Canvas();
        PorterDuffColorFilter filter = new PorterDuffColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC_ATOP);
        paint.setColorFilter(filter);
        canvas.scale(scale, scale);
        canvas.drawBitmap(scaleBitmap, 0, 0, paint);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            scaleBitmap = renderScriptBlur(scaleBitmap, radius, recycle);
        } else {
            scaleBitmap = stackBlur(scaleBitmap, (int) radius, recycle);
        }
        if (scale == 1) {
            if (recycle && !src.isRecycled() && scaleBitmap != src) src.recycle();
            return scaleBitmap;
        }
        Bitmap ret = Bitmap.createScaledBitmap(scaleBitmap, width, height, true);
        if (!scaleBitmap.isRecycled()) scaleBitmap.recycle();
        if (recycle && !src.isRecycled() && ret != src) src.recycle();
        return ret;
    }

    /**
     * renderScript 模糊图片 - API 大于 17
     * @param src    源图片
     * @param radius 模糊半径(0...25)
     * @return 模糊后的图片
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static Bitmap renderScriptBlur(final Bitmap src, @FloatRange(from = 0, to = 25, fromInclusive = false) final float radius) {
        return renderScriptBlur(src, radius, false);
    }

    /**
     * renderScript 模糊图片 - API 大于 17
     * @param src     源图片
     * @param radius  模糊半径(0...25)
     * @param recycle 是否回收
     * @return 模糊后的图片
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static Bitmap renderScriptBlur(final Bitmap src, @FloatRange(from = 0, to = 25, fromInclusive = false) final float radius, final boolean recycle) {
        RenderScript rs = null;
        Bitmap ret = recycle ? src : src.copy(src.getConfig(), true);
        try {
            rs = RenderScript.create(DevUtils.getContext());
            rs.setMessageHandler(new RenderScript.RSMessageHandler());
            Allocation input = Allocation.createFromBitmap(rs, ret, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
            Allocation output = Allocation.createTyped(rs, input.getType());
            ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            blurScript.setInput(input);
            blurScript.setRadius(radius);
            blurScript.forEach(output);
            output.copyTo(ret);
        } finally {
            if (rs != null) {
                rs.destroy();
            }
        }
        return ret;
    }

    /**
     * stack 模糊图片
     * @param src    源图片
     * @param radius 模糊半径
     * @return stack 模糊后的图片
     */
    public static Bitmap stackBlur(final Bitmap src, final int radius) {
        return stackBlur(src, radius, false);
    }

    /**
     * stack 模糊图片
     * @param src     源图片
     * @param radius  模糊半径
     * @param recycle 是否回收
     * @return stack 模糊后的图片
     */
    public static Bitmap stackBlur(final Bitmap src, final int radius, final boolean recycle) {
        Bitmap ret = recycle ? src : src.copy(src.getConfig(), true);

        // 模糊半径
        int blurRadius = radius;
        // 防止意外
        if (blurRadius < 1) {
            blurRadius = 1;
        }
        int width = ret.getWidth();
        int height = ret.getHeight();

        int[] pix = new int[width * height];
        ret.getPixels(pix, 0, width, 0, 0, width, height);

        int wm = width - 1;
        int hm = height - 1;
        int wh = width * height;
        int div = blurRadius + blurRadius + 1;

        int[] r = new int[wh];
        int[] g = new int[wh];
        int[] b = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int[] vmin = new int[Math.max(width, height)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int[] dv = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = blurRadius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < height; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -blurRadius; i <= blurRadius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + blurRadius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = blurRadius;

            for (x = 0; x < width; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - blurRadius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + blurRadius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += width;
        }
        for (x = 0; x < width; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -blurRadius * width;
            for (i = -blurRadius; i <= blurRadius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + blurRadius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += width;
                }
            }
            yi = x;
            stackpointer = blurRadius;
            for (y = 0; y < height; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - blurRadius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * width;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += width;
            }
        }
        ret.setPixels(pix, 0, width, 0, 0, width, height);
        return ret;
    }

    /**
     * 保存图片
     * @param src      源图片
     * @param filePath
     * @param format   格式
     * @return {@code true} 成功, {@code false} 失败
     */
    public static boolean save(final Bitmap src, final String filePath, final CompressFormat format) {
        return save(src, getFileByPath(filePath), format, false);
    }

    /**
     * 保存图片
     * @param src    源图片
     * @param file
     * @param format 格式
     * @return {@code true} 成功, {@code false} 失败
     */
    public static boolean save(final Bitmap src, final File file, final CompressFormat format) {
        return save(src, file, format, false);
    }

    /**
     * 保存图片
     * @param src      源图片
     * @param filePath
     * @param format   格式
     * @param recycle  是否回收
     * @return {@code true} 成功, {@code false} 失败
     */
    public static boolean save(final Bitmap src, final String filePath, final CompressFormat format, final boolean recycle) {
        return save(src, getFileByPath(filePath), format, recycle);
    }

    /**
     * 保存图片
     * @param src     源图片
     * @param file
     * @param format  格式
     * @param recycle 是否回收
     * @return {@code true} 成功, {@code false} 失败
     */
    public static boolean save(final Bitmap src, final File file, final CompressFormat format, final boolean recycle) {
        if (isEmptyBitmap(src) || !createFileByDeleteOldFile(file)) return false;
        OutputStream os = null;
        boolean ret = false;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file));
            ret = src.compress(format, 100, os);
            if (recycle && !src.isRecycled()) src.recycle();
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "save");
        } finally {
            CloseUtils.closeIO(os);
        }
        return ret;
    }

    /**
     * 根据文件名判断文件是否为图片
     * @param file
     * @return {@code true} 是, {@code false} 否
     */
    public static boolean isImage(final File file) {
        return file != null && isImage(file.getPath());
    }

    /**
     * 根据文件名判断文件是否为图片
     * @param filePath
     * @return {@code true} 是, {@code false} 否
     */
    public static boolean isImage(final String filePath) {
        String path = filePath.toUpperCase();
        return path.endsWith(".PNG") || path.endsWith(".JPG") || path.endsWith(".JPEG") || path.endsWith(".BMP") || path.endsWith(".GIF");
    }

    /**
     * 获取图片类型
     * @param filePath
     * @return 图片类型
     */
    public static String getImageType(final String filePath) {
        return getImageType(getFileByPath(filePath));
    }

    /**
     * 获取图片类型
     * @param file
     * @return 图片类型
     */
    public static String getImageType(final File file) {
        if (file == null) return null;
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            return getImageType(is);
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "getImageType");
            return null;
        } finally {
            CloseUtils.closeIO(is);
        }
    }

    /**
     * 流获取图片类型
     * @param is 图片输入流
     * @return 图片类型
     */
    public static String getImageType(final InputStream is) {
        if (is == null) return null;
        try {
            byte[] bytes = new byte[8];
            return is.read(bytes, 0, 8) != -1 ? getImageType(bytes) : null;
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "getImageType");
            return null;
        }
    }

    /**
     * 获取图片类型
     * @param bytes bitmap 的前 8 字节
     * @return 图片类型
     */
    public static String getImageType(final byte[] bytes) {
        if (isJPEG(bytes)) return "JPEG";
        if (isGif(bytes)) return "GIF";
        if (isPNG(bytes)) return "PNG";
        if (isBMP(bytes)) return "BMP";
        return null;
    }

    /**
     * 判断是否jpg图片
     * @param b
     * @return
     */
    private static boolean isJPEG(final byte[] b) {
        return b.length >= 2 && (b[0] == (byte) 0xFF) && (b[1] == (byte) 0xD8);
    }

    /**
     * 判断是否gif图片
     * @param b
     * @return
     */
    private static boolean isGif(final byte[] b) {
        return b.length >= 6 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8' && (b[4] == '7' || b[4] == '9') && b[5] == 'a';
    }

    /**
     * 判断是否png图片
     * @param b
     * @return
     */
    private static boolean isPNG(final byte[] b) {
        return b.length >= 8
                && (b[0] == (byte) 137 && b[1] == (byte) 80
                && b[2] == (byte) 78 && b[3] == (byte) 71
                && b[4] == (byte) 13 && b[5] == (byte) 10
                && b[6] == (byte) 26 && b[7] == (byte) 10);
    }

    /**
     * 判断是否bmp图片
     * @param b
     * @return
     */
    private static boolean isBMP(final byte[] b) {
        return b.length >= 2 && (b[0] == 0x42) && (b[1] == 0x4d);
    }

    /**
     * 判断 bitmap 对象是否为空
     * @param src 源图片
     * @return {@code true} 是, {@code false} 否
     */
    private static boolean isEmptyBitmap(final Bitmap src) {
        return src == null || src.getWidth() == 0 || src.getHeight() == 0;
    }

    // = 压缩有关 =

    /**
     * 按缩放压缩
     * @param src       源图片
     * @param newWidth  新宽度
     * @param newHeight 新高度
     * @return 缩放压缩后的图片
     */
    public static Bitmap compressByScale(final Bitmap src, final int newWidth, final int newHeight) {
        return scale(src, newWidth, newHeight, false);
    }

    /**
     * 按缩放压缩
     * @param src       源图片
     * @param newWidth  新宽度
     * @param newHeight 新高度
     * @param recycle   是否回收
     * @return 缩放压缩后的图片
     */
    public static Bitmap compressByScale(final Bitmap src, final int newWidth, final int newHeight, final boolean recycle) {
        return scale(src, newWidth, newHeight, recycle);
    }

    /**
     * 按缩放压缩
     * @param src         源图片
     * @param scaleWidth  缩放宽度倍数
     * @param scaleHeight 缩放高度倍数
     * @return 缩放压缩后的图片
     */
    public static Bitmap compressByScale(final Bitmap src, final float scaleWidth, final float scaleHeight) {
        return scale(src, scaleWidth, scaleHeight, false);
    }

    /**
     * 按缩放压缩
     * @param src         源图片
     * @param scaleWidth  缩放宽度倍数
     * @param scaleHeight 缩放高度倍数
     * @param recycle     是否回收
     * @return 缩放压缩后的图片
     */
    public static Bitmap compressByScale(final Bitmap src, final float scaleWidth, final float scaleHeight, final boolean recycle) {
        return scale(src, scaleWidth, scaleHeight, recycle);
    }

    /**
     * 按质量压缩
     * @param src     源图片
     * @param quality 质量
     * @return 质量压缩后的图片
     */
    public static Bitmap compressByQuality(final Bitmap src, @IntRange(from = 0, to = 100) final int quality) {
        return compressByQuality(src, quality, false);
    }

    /**
     * 按质量压缩
     * @param src     源图片
     * @param quality 质量
     * @param recycle 是否回收
     * @return 质量压缩后的图片
     */
    public static Bitmap compressByQuality(final Bitmap src, @IntRange(from = 0, to = 100) final int quality, final boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        src.compress(CompressFormat.JPEG, quality, baos);
        byte[] bytes = baos.toByteArray();
        if (recycle && !src.isRecycled()) src.recycle();
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * 按质量压缩
     * @param src         源图片
     * @param maxByteSize 允许最大值字节数
     * @return 质量压缩压缩过的图片
     */
    public static Bitmap compressByQuality(final Bitmap src, final long maxByteSize) {
        return compressByQuality(src, maxByteSize, false);
    }

    /**
     * 按质量压缩
     * @param src         源图片
     * @param maxByteSize 允许最大值字节数
     * @param recycle     是否回收
     * @return 质量压缩压缩过的图片
     */
    public static Bitmap compressByQuality(final Bitmap src, final long maxByteSize, final boolean recycle) {
        if (isEmptyBitmap(src) || maxByteSize <= 0) return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        src.compress(CompressFormat.JPEG, 100, baos);
        byte[] bytes;
        if (baos.size() <= maxByteSize) { // 最好质量的不大于最大字节, 则返回最佳质量
            bytes = baos.toByteArray();
        } else {
            baos.reset();
            src.compress(CompressFormat.JPEG, 0, baos);
            if (baos.size() >= maxByteSize) { // 最差质量不小于最大字节, 则返回最差质量
                bytes = baos.toByteArray();
            } else {
                // 二分法寻找最佳质量
                int st = 0;
                int end = 100;
                int mid = 0;
                while (st < end) {
                    mid = (st + end) / 2;
                    baos.reset();
                    src.compress(CompressFormat.JPEG, mid, baos);
                    int len = baos.size();
                    if (len == maxByteSize) {
                        break;
                    } else if (len > maxByteSize) {
                        end = mid - 1;
                    } else {
                        st = mid + 1;
                    }
                }
                if (end == mid - 1) {
                    baos.reset();
                    src.compress(CompressFormat.JPEG, st, baos);
                }
                bytes = baos.toByteArray();
            }
        }
        if (recycle && !src.isRecycled()) src.recycle();
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * 按采样大小压缩
     * @param src        源图片
     * @param sampleSize 采样率大小
     * @return 按采样率压缩后的图片
     */

    public static Bitmap compressBySampleSize(final Bitmap src, final int sampleSize) {
        return compressBySampleSize(src, sampleSize, false);
    }

    /**
     * 按采样大小压缩
     * @param src        源图片
     * @param sampleSize 采样率大小
     * @param recycle    是否回收
     * @return 按采样率压缩后的图片
     */
    public static Bitmap compressBySampleSize(final Bitmap src, final int sampleSize, final boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        src.compress(CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        if (recycle && !src.isRecycled()) src.recycle();
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    /**
     * 按采样大小压缩
     * @param src       源图片
     * @param maxWidth  最大宽度
     * @param maxHeight 最大高度
     * @return 按采样率压缩后的图片
     */
    public static Bitmap compressBySampleSize(final Bitmap src, final int maxWidth, final int maxHeight) {
        return compressBySampleSize(src, maxWidth, maxHeight, false);
    }

    /**
     * 按采样大小压缩
     * @param src       源图片
     * @param maxWidth  最大宽度
     * @param maxHeight 最大高度
     * @param recycle   是否回收
     * @return 按采样率压缩后的图片
     */
    public static Bitmap compressBySampleSize(final Bitmap src, final int maxWidth, final int maxHeight, final boolean recycle) {
        if (isEmptyBitmap(src)) return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        src.compress(CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
        options.inJustDecodeBounds = false;
        if (recycle && !src.isRecycled()) src.recycle();
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    /**
     * 计算采样大小
     * @param options   选项
     * @param maxWidth  最大宽度
     * @param maxHeight 最大高度
     * @return 采样大小
     */
    private static int calculateInSampleSize(final BitmapFactory.Options options, final int maxWidth, final int maxHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        while ((width >>= 1) >= maxWidth && (height >>= 1) >= maxHeight) {
            inSampleSize <<= 1;
        }
        return inSampleSize;
    }

    // =

    /**
     * 获取文件
     * @param filePath
     * @return
     */
    private static File getFileByPath(final String filePath) {
        return filePath != null ? new File(filePath) : null;
    }

    /**
     * 判断文件是否存在, 存在则在创建之前删除
     * @param file
     * @return {@code true} 创建成功, {@code false} 创建失败
     */
    private static boolean createFileByDeleteOldFile(final File file) {
        if (file == null) return false;
        // 文件存在并且删除失败返回 false
        if (file.exists() && !file.delete()) return false;
        // 创建目录失败返回 false
        if (!createOrExistsDir(file.getParentFile())) return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            LogPrintUtils.eTag(TAG, e, "createFileByDeleteOldFile");
            return false;
        }
    }

    /**
     * 判断目录是否存在, 不存在则判断是否创建成功
     * @param file 文件
     * @return {@code true} 存在或创建成功, {@code false} 不存在或创建失败
     */
    private static boolean createOrExistsDir(final File file) {
        // 如果存在, 是目录则返回 true, 是文件则返回 false, 不存在则返回是否创建成功
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    /**
     * 判断字符串是否为 null 或全为空白字符
     * @param str 待校验字符串
     * @return {@code true} yes, {@code false} no
     */
    private static boolean isSpace(final String str) {
        if (str == null) return true;
        for (int i = 0, len = str.length(); i < len; ++i) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
