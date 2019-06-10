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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;
import android.net.Uri;
import android.util.TypedValue;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * 位图解码器
 */
public class BitmapDecoder {
    /**
     * 单张图片最大像素数
     */
    private int maxNumOfPixels;

    /**
     * 最小边长，默认为-1
     */
    private int minSlideLength;

    /**
     * 创建一个位图解码器，此解码器将根据最大像素数来缩小位图值合适的尺寸
     *
     * @param maxNumOfPixels
     */
    public BitmapDecoder(int maxNumOfPixels) {
        this.maxNumOfPixels = maxNumOfPixels;
        this.minSlideLength = -1;
    }

    /**
     * 创建一个位图解码器，最大像素数默认为虚拟机可用最大内存的八分之一再除以4，这样可以保证图片不会太大导致内存溢出
     */
    public BitmapDecoder() {
        this((int) (Runtime.getRuntime().maxMemory() / 8 / 4));
    }

    /**
     * 从字节数组中解码位图
     *
     * @param data
     * @param offset
     * @param length
     * @param options
     * @return
     */
    public Bitmap decodeByteArray(byte[] data, int offset, int length, Options options) {
        if (options == null) {
            options = new Options();
        }
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, offset, length, options);
        options.inSampleSize = computeSampleSize(options, minSlideLength, maxNumOfPixels);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, offset, length, options);
    }

    /**
     * 从字节数组中解码位图
     *
     * @param data
     * @param offset
     * @param length
     * @return
     */
    public Bitmap decodeByteArray(byte[] data, int offset, int length) {
        return decodeByteArray(data, offset, length, null);
    }

    /**
     * 从字节数组中解码位图
     *
     * @param data
     * @param options
     * @return
     */
    public Bitmap decodeByteArray(byte[] data, Options options) {
        return decodeByteArray(data, 0, data.length, options);
    }

    /**
     * 从字节数组中解码位图
     *
     * @param data
     * @return
     */
    public Bitmap decodeByteArray(byte[] data) {
        return decodeByteArray(data, 0, data.length);
    }

    /**
     * 从文件中解码位图
     *
     * @param filePath
     * @param options
     * @return
     */
    public Bitmap decodeFile(String filePath, Options options) {
        if (options == null) {
            options = new Options();
        }
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = computeSampleSize(options, minSlideLength, maxNumOfPixels);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 从文件中解码位图
     *
     * @param filePath
     * @return
     */
    public Bitmap decodeFile(String filePath) {
        return decodeFile(filePath, null);
    }

    /**
     * 从文件描述符中解码位图
     *
     * @param fd
     * @param outPadding
     * @param options
     * @return
     */
    public Bitmap decodeFileDescriptor(FileDescriptor fd, Rect outPadding, Options options) {
        if (options == null) {
            options = new Options();
        }
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, outPadding, options);
        options.inSampleSize = computeSampleSize(options, minSlideLength, maxNumOfPixels);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fd, outPadding, options);
    }

    /**
     * 从文件描述符中解码位图
     *
     * @param fd
     * @return
     */
    public Bitmap decodeFileDescriptor(FileDescriptor fd) {
        return decodeFileDescriptor(fd, null, null);
    }

    /**
     * 从资源文件中解码位图
     *
     * @param resource
     * @param id
     * @param options
     * @return
     */
    public Bitmap decodeResource(Resources resource, int id, Options options) {
        if (options == null) {
            options = new Options();
        }
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resource, id, options);
        options.inSampleSize = computeSampleSize(options, minSlideLength, maxNumOfPixels);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resource, id, options);
    }

    /**
     * 从资源文件中解码位图
     *
     * @param resource
     * @param id
     * @return
     */
    public Bitmap decodeResource(Resources resource, int id) {
        return decodeResource(resource, id, null);
    }

    /**
     * 从资源文件流中解码位图
     *
     * @param resource
     * @param value
     * @param inputStreamCreator
     * @param pad
     * @param options
     * @return
     */
    public Bitmap decodeResourceStream(Resources resource, TypedValue value, InputStreamCreator inputStreamCreator, Rect pad, Options options) {
        if (options == null) {
            options = new Options();
        }
        options.inJustDecodeBounds = true;

        InputStream inputStream = inputStreamCreator.onCreateInputStream();
        if (inputStream == null) return null;
        BitmapFactory.decodeResourceStream(resource, value, inputStream, pad, options);
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        options.inSampleSize = computeSampleSize(options, minSlideLength, maxNumOfPixels);
        options.inJustDecodeBounds = false;

        inputStream = inputStreamCreator.onCreateInputStream();
        if (inputStream == null) return null;
        Bitmap bitmap = BitmapFactory.decodeResourceStream(resource, value, inputStream, pad, options);
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    /**
     * 从流中解码位图
     *
     * @param inputStreamCreator
     * @param outPadding
     * @param options
     * @return
     */
    public Bitmap decodeStream(InputStreamCreator inputStreamCreator, Rect outPadding, Options options) {
        if (options == null) {
            options = new Options();
        }
        options.inJustDecodeBounds = true;

        InputStream inputStream = inputStreamCreator.onCreateInputStream();
        if (inputStream == null) return null;
        BitmapFactory.decodeStream(inputStream, outPadding, options);
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        options.inSampleSize = computeSampleSize(options, minSlideLength, maxNumOfPixels);
        options.inJustDecodeBounds = false;

        inputStream = inputStreamCreator.onCreateInputStream();
        if (inputStream == null) return null;
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, outPadding, options);
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    /**
     * 从流中解码位图
     *
     * @param inputStreamCreator
     * @return
     */
    public Bitmap decodeStream(InputStreamCreator inputStreamCreator) {
        return decodeStream(inputStreamCreator, null, null);
    }

    /**
     * 从Assets中解码位图
     *
     * @param context
     * @param fileName
     * @param outPadding
     * @param options
     * @return
     */
    public Bitmap decodeFromAssets(final Context context, final String fileName, Rect outPadding, Options options) {
        return decodeStream(new InputStreamCreator() {
            @Override
            public InputStream onCreateInputStream() {
                try {
                    return context.getAssets().open(fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }, outPadding, options);
    }

    /**
     * 从Assets中解码位图
     *
     * @param context
     * @param fileName
     * @return
     */
    public Bitmap decodeFromAssets(Context context, String fileName) {
        return decodeFromAssets(context, fileName, null, null);
    }

    /**
     * 从Uri中解码位图
     *
     * @param context
     * @param uri
     * @param outPadding
     * @param options
     * @return
     */
    public Bitmap decodeUri(final Context context, final Uri uri, Rect outPadding, Options options) {
        return decodeStream(new InputStreamCreator() {
            @Override
            public InputStream onCreateInputStream() {
                try {
                    return context.getContentResolver().openInputStream(uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }, outPadding, options);
    }

    /**
     * 从Uri中解码位图
     *
     * @param context
     * @param uri
     * @return
     */
    public Bitmap decodeUri(final Context context, final Uri uri) {
        return decodeUri(context, uri, null, null);
    }

    /**
     * 获取最大像素数，一般由图片宽乘以高得出
     *
     * @return
     */
    public int getMaxNumOfPixels() {
        return maxNumOfPixels;
    }

    /**
     * 设置最大像素数，将根据此像素数来缩小图片至合适的大小
     *
     * @param maxNumOfPixels 最大像素数，由图片宽乘以高得出
     */
    public void setMaxNumOfPixels(int maxNumOfPixels) {
        this.maxNumOfPixels = maxNumOfPixels;
    }

    /**
     * 获取图片最小边长
     *
     * @return
     */
    public int getMinSlideLength() {
        return minSlideLength;
    }

    /**
     * 设置图片最小边长，默认为-1
     *
     * @param minSlideLength
     */
    public void setMinSlideLength(int minSlideLength) {
        this.minSlideLength = minSlideLength;
    }

    /**
     * 从字节数组中解码位图的尺寸
     *
     * @param data
     * @param offset
     * @param length
     * @param options
     * @return
     */
    public static Options decodeSizeFromByteArray(byte[] data, int offset, int length, Options options) {
        if (options == null) {
            options = new Options();
        }
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, offset, length, options);
        options.inJustDecodeBounds = false;
        return options;
    }

    /**
     * 从字节数组中解码位图的尺寸
     *
     * @param data
     * @param offset
     * @param length
     * @return
     */
    public static Options decodeSizeFromByteArray(byte[] data, int offset, int length) {
        return decodeSizeFromByteArray(data, offset, length, null);
    }

    /**
     * 从文件中解码位图的尺寸
     *
     * @param filePath
     * @param options
     * @return
     */
    public static Options decodeSizeFromFile(String filePath, Options options) {
        if (options == null) {
            options = new Options();
        }
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inJustDecodeBounds = false;
        return options;
    }

    /**
     * 从文件中解码位图的尺寸
     *
     * @param filePath
     * @return
     */
    public static Options decodeSizeFromFile(String filePath) {
        return decodeSizeFromFile(filePath, null);
    }

    /**
     * 从文件描述符中解码位图的尺寸
     *
     * @param fd
     * @param outPadding
     * @param options
     * @return
     */
    public static Options decodeSizeFromFileDescriptor(FileDescriptor fd, Rect outPadding, Options options) {
        if (options == null) {
            options = new Options();
        }
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, outPadding, options);
        options.inJustDecodeBounds = false;
        return options;
    }

    /**
     * 从文件描述符中解码位图的尺寸
     *
     * @param fd
     * @return
     */
    public static Options decodeSizeFromFileDescriptor(FileDescriptor fd) {
        return decodeSizeFromFileDescriptor(fd, null, null);
    }

    /**
     * 从资源文件中解码位图的尺寸
     *
     * @param resource
     * @param id
     * @param options
     * @return
     */
    public static Options decodeSizeFromResource(Resources resource, int id, Options options) {
        if (options == null) {
            options = new Options();
        }
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resource, id, options);
        options.inJustDecodeBounds = false;
        return options;
    }

    /**
     * 从资源文件中解码位图的尺寸
     *
     * @param resource
     * @param id
     * @return
     */
    public static Options decodeSizeFromResource(Resources resource, int id) {
        return decodeSizeFromResource(resource, id, null);
    }

    /**
     * 从资源流中解码位图的尺寸
     *
     * @param resource
     * @param value
     * @param inputStreamCreator
     * @param pad
     * @param options
     * @return
     */
    public static Options decodeSizeFromResourceStream(Resources resource, TypedValue value, InputStreamCreator inputStreamCreator, Rect pad, Options options) {
        if (options == null) {
            options = new Options();
        }
        options.inJustDecodeBounds = true;
        InputStream inputStream = inputStreamCreator.onCreateInputStream();
        if (inputStream == null) return options;
        BitmapFactory.decodeResourceStream(resource, value, inputStream, pad, options);
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        options.inJustDecodeBounds = false;
        return options;
    }

    /**
     * 从流中解码位图的尺寸
     *
     * @param inputStreamCreator
     * @param outPadding
     * @param options
     * @return
     */
    public static Options decodeSizeFromStream(InputStreamCreator inputStreamCreator, Rect outPadding, Options options) {
        if (options == null) {
            options = new Options();
        }
        options.inJustDecodeBounds = true;
        InputStream inputStream = inputStreamCreator.onCreateInputStream();
        if (inputStream == null) return options;
        BitmapFactory.decodeStream(inputStream, outPadding, options);
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        options.inJustDecodeBounds = false;
        return options;
    }

    /**
     * 从流中解码位图的尺寸
     *
     * @param inputStreamCreator
     * @return
     */
    public static Options decodeSizeFromStream(InputStreamCreator inputStreamCreator) {
        return decodeSizeFromStream(inputStreamCreator, null, null);
    }

    /**
     * 从Assets中解码位图的尺寸
     *
     * @param context
     * @param fileName
     * @param outPadding
     * @param options
     * @return
     */
    public static Options decodeSizeFromAssets(final Context context, final String fileName, Rect outPadding, Options options) {
        return decodeSizeFromStream(new InputStreamCreator() {
            @Override
            public InputStream onCreateInputStream() {
                try {
                    return context.getAssets().open(fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }, outPadding, options);
    }

    /**
     * 从Assets中解码位图的尺寸
     *
     * @param context
     * @param fileName
     * @return
     */
    public static Options decodeSizeFromAssets(Context context, String fileName) {
        return decodeSizeFromAssets(context, fileName, null, null);
    }

    /**
     * 计算合适的缩小倍数，注意在调用此方法之前一定要先通过Options.inJustDecodeBounds属性来获取图片的宽高
     *
     * @param options
     * @param minSideLength  用于指定最小宽度或最小高度
     * @param maxNumOfPixels 最大尺寸，由最大宽高相乘得出
     * @return
     */
    public static int computeSampleSize(Options options, int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(Options options, int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) &&
                (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    public static int calculateInSampleSize(Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public interface InputStreamCreator {
        public InputStream onCreateInputStream();
    }
}