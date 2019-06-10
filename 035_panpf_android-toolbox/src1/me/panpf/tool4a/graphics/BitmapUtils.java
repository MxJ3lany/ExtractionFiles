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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

@SuppressWarnings("unused")
public class BitmapUtils {
    /**
     * Drawable转换成Bitmap
     *
     * @param drawable Drawable
     * @return Bitmap
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() == null || bitmapDrawable.getBitmap().isRecycled()) {
                return null;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static void decodeYUV(int[] out, byte[] fg, int width, int height) throws NullPointerException, IllegalArgumentException {
        int sz = width * height;
        if (out == null)
            throw new NullPointerException("buffer out is null");
        if (out.length < sz)
            throw new IllegalArgumentException("buffer out size " + out.length
                    + " < minimum " + sz);
        if (fg == null)
            throw new NullPointerException("buffer 'fg' is null");
        if (fg.length < sz)
            throw new IllegalArgumentException("buffer fg size " + fg.length
                    + " < minimum " + sz * 3 / 2);
        int i, j;
        int Y, Cr = 0, Cb = 0;
        for (j = 0; j < height; j++) {
            int pixPtr = j * width;
            final int jDiv2 = j >> 1;
            for (i = 0; i < width; i++) {
                Y = fg[pixPtr];
                if (Y < 0)
                    Y += 255;
                if ((i & 0x1) != 1) {
                    final int cOff = sz + jDiv2 * width + (i >> 1) * 2;
                    Cb = fg[cOff];
                    if (Cb < 0)
                        Cb += 127;
                    else
                        Cb -= 128;
                    Cr = fg[cOff + 1];
                    if (Cr < 0)
                        Cr += 127;
                    else
                        Cr -= 128;
                }
                int R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);
                if (R < 0)
                    R = 0;
                else if (R > 255)
                    R = 255;
                int G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1)
                        + (Cr >> 3) + (Cr >> 4) + (Cr >> 5);
                if (G < 0)
                    G = 0;
                else if (G > 255)
                    G = 255;
                int B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);
                if (B < 0)
                    B = 0;
                else if (B > 255)
                    B = 255;
                out[pixPtr++] = 0xff000000 + (B << 16) + (G << 8) + R;
            }
        }
    }

    /**
     * 将YUV格式的图片的源数据从横屏模式转为竖屏模式，注意：将源图片的宽高互换一下就是新图片的宽高
     *
     * @param sourceData YUV格式的图片的源数据
     * @param width      源图片的宽
     * @param height     源图片的高
     * @return byte[]
     */
    public final byte[] yuvLandscapeToPortrait(byte[] sourceData, int width, int height) {
        byte[] rotatedData = new byte[sourceData.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++)
                rotatedData[x * height + height - y - 1] = sourceData[x + y * width];
        }
        return rotatedData;
    }

//    /**
//     * 素描效果处理
//     * @return 素描效果处理后的图片
//     */
//    public static Bitmap sketch(Bitmap bitmap) {
//        int pos, row, col, clr;
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//        int[] pixSrc = new int[width * height];
//        int[] pixNvt = new int[width * height];
//        // 先对图象的像素处理成灰度颜色后再取反
//        bitmap.getPixels(pixSrc, 0, width, 0, 0, width, height);
//
//        for (row = 0; row < height; row++) {
//            for (col = 0; col < width; col++) {
//                pos = row * width + col;
//                pixSrc[pos] = (Color.red(pixSrc[pos]) + Color.green(pixSrc[pos]) + Color.blue(pixSrc[pos])) / 3;
//                pixNvt[pos] = 255 - pixSrc[pos];
//            }
//        }
//
//        // 对取反的像素进行高斯模糊, 强度可以设置，暂定为5.0
//        gaussGray(pixNvt, 5.0, 5.0, width, height);
//
//        // 灰度颜色和模糊后像素进行差值运算
//        for (row = 0; row < height; row++) {
//            for (col = 0; col < width; col++) {
//                pos = row * width + col;
//
//                clr = pixSrc[pos] << 8;
//                clr /= 256 - pixNvt[pos];
//                clr = Math.min(clr, 255);
//
//                pixSrc[pos] = Color.rgb(clr, clr, clr);
//            }
//        }
//        bitmap.setPixels(pixSrc, 0, width, 0, 0, width, height);
//        return bitmap;
//    }
//
//	private int gaussGray(int[] psrc, double horz, double vert, int width, int height) {
//		int[] dst, src;
//		double[] n_p, n_m, d_p, d_m, bd_p, bd_m;
//		double[] val_p, val_m;
//		int i, j, t, k, row, col, terms;
//		int[] initial_p, initial_m;
//		double std_dev;
//		int row_stride = width;
//		int max_len = Math.max(width, height);
//		int sp_p_idx, sp_m_idx, vp_idx, vm_idx;
//
//		val_p = new double[max_len];
//		val_m = new double[max_len];
//
//		n_p = new double[5];
//		n_m = new double[5];
//		d_p = new double[5];
//		d_m = new double[5];
//		bd_p = new double[5];
//		bd_m = new double[5];
//
//		src = new int[max_len];
//		dst = new int[max_len];
//
//		initial_p = new int[4];
//		initial_m = new int[4];
//
//		// 垂直方向
//		if (vert > 0.0) {
//			vert = Math.abs(vert) + 1.0;
//			std_dev = Math.sqrt(-(vert * vert) / (2 * Math.log(1.0 / 255.0)));
//
//			// 初试化常量
//			findConstants(n_p, n_m, d_p, d_m, bd_p, bd_m, std_dev);
//
//			for (col = 0; col < width; col++) {
//				for (k = 0; k < max_len; k++) {
//					val_m[k] = val_p[k] = 0;
//				}
//
//				for (t = 0; t < height; t++) {
//					src[t] = psrc[t * row_stride + col];
//				}
//
//				sp_p_idx = 0;
//				sp_m_idx = height - 1;
//				vp_idx = 0;
//				vm_idx = height - 1;
//
//				initial_p[0] = src[0];
//				initial_m[0] = src[height - 1];
//
//				for (row = 0; row < height; row++) {
//					terms = (row < 4) ? row : 4;
//
//					for (i = 0; i <= terms; i++) {
//						val_p[vp_idx] += n_p[i] * src[sp_p_idx - i] - d_p[i] * val_p[vp_idx - i];
//						val_m[vm_idx] += n_m[i] * src[sp_m_idx + i] - d_m[i] * val_m[vm_idx + i];
//					}
//					for (j = i; j <= 4; j++) {
//						val_p[vp_idx] += (n_p[j] - bd_p[j]) * initial_p[0];
//						val_m[vm_idx] += (n_m[j] - bd_m[j]) * initial_m[0];
//					}
//
//					sp_p_idx++;
//					sp_m_idx--;
//					vp_idx++;
//					vm_idx--;
//				}
//
//				transferGaussPixels(val_p, val_m, dst, 1, height);
//
//				for (t = 0; t < height; t++) {
//					psrc[t * row_stride + col] = dst[t];
//				}
//			}
//		}
//
//		// 水平方向
//		if (horz > 0.0) {
//			horz = Math.abs(horz) + 1.0;
//
//			if (horz != vert) {
//				std_dev = Math.sqrt(-(horz * horz) / (2 * Math.log(1.0 / 255.0)));
//
//				// 初试化常量
//				findConstants(n_p, n_m, d_p, d_m, bd_p, bd_m, std_dev);
//			}
//
//			for (row = 0; row < height; row++) {
//				for (k = 0; k < max_len; k++) {
//					val_m[k] = val_p[k] = 0;
//				}
//
//				for (t = 0; t < width; t++) {
//					src[t] = psrc[row * row_stride + t];
//				}
//
//				sp_p_idx = 0;
//				sp_m_idx = width - 1;
//				vp_idx = 0;
//				vm_idx = width - 1;
//
//				initial_p[0] = src[0];
//				initial_m[0] = src[width - 1];
//
//				for (col = 0; col < width; col++) {
//					terms = (col < 4) ? col : 4;
//
//					for (i = 0; i <= terms; i++) {
//						val_p[vp_idx] += n_p[i] * src[sp_p_idx - i] - d_p[i] * val_p[vp_idx - i];
//						val_m[vm_idx] += n_m[i] * src[sp_m_idx + i] - d_m[i] * val_m[vm_idx + i];
//					}
//					for (j = i; j <= 4; j++) {
//						val_p[vp_idx] += (n_p[j] - bd_p[j]) * initial_p[0];
//						val_m[vm_idx] += (n_m[j] - bd_m[j]) * initial_m[0];
//					}
//
//					sp_p_idx++;
//					sp_m_idx--;
//					vp_idx++;
//					vm_idx--;
//				}
//
//				transferGaussPixels(val_p, val_m, dst, 1, width);
//
//				for (t = 0; t < width; t++) {
//					psrc[row * row_stride + t] = dst[t];
//				}
//			}
//		}
//
//		return 0;
//	}
//
//	private void findConstants(double[] n_p, double[] n_m, double[] d_p, double[] d_m, double[] bd_p,
//			double[] bd_m, double std_dev) {
//		double div = Math.sqrt(2 * 3.141593) * std_dev;
//		double x0 = -1.783 / std_dev;
//		double x1 = -1.723 / std_dev;
//		double x2 = 0.6318 / std_dev;
//		double x3 = 1.997 / std_dev;
//		double x4 = 1.6803 / div;
//		double x5 = 3.735 / div;
//		double x6 = -0.6803 / div;
//		double x7 = -0.2598 / div;
//		int i;
//
//		n_p[0] = x4 + x6;
//		n_p[1] = (Math.exp(x1) * (x7 * Math.sin(x3) - (x6 + 2 * x4) * Math.cos(x3)) + Math.exp(x0)
//				* (x5 * Math.sin(x2) - (2 * x6 + x4) * Math.cos(x2)));
//		n_p[2] = (2
//				* Math.exp(x0 + x1)
//				* ((x4 + x6) * Math.cos(x3) * Math.cos(x2) - x5 * Math.cos(x3) * Math.sin(x2) - x7 * Math.cos(x2)
//						* Math.sin(x3)) + x6 * Math.exp(2 * x0) + x4 * Math.exp(2 * x1));
//		n_p[3] = (Math.exp(x1 + 2 * x0) * (x7 * Math.sin(x3) - x6 * Math.cos(x3)) + Math.exp(x0 + 2 * x1)
//				* (x5 * Math.sin(x2) - x4 * Math.cos(x2)));
//		n_p[4] = 0.0;
//
//		d_p[0] = 0.0;
//		d_p[1] = -2 * Math.exp(x1) * Math.cos(x3) - 2 * Math.exp(x0) * Math.cos(x2);
//		d_p[2] = 4 * Math.cos(x3) * Math.cos(x2) * Math.exp(x0 + x1) + Math.exp(2 * x1) + Math.exp(2 * x0);
//		d_p[3] = -2 * Math.cos(x2) * Math.exp(x0 + 2 * x1) - 2 * Math.cos(x3) * Math.exp(x1 + 2 * x0);
//		d_p[4] = Math.exp(2 * x0 + 2 * x1);
//
//		for (i = 0; i <= 4; i++) {
//			d_m[i] = d_p[i];
//		}
//
//		n_m[0] = 0.0;
//		for (i = 1; i <= 4; i++) {
//			n_m[i] = n_p[i] - d_p[i] * n_p[0];
//		}
//
//		double sum_n_p, sum_n_m, sum_d;
//		double a, b;
//
//		sum_n_p = 0.0;
//		sum_n_m = 0.0;
//		sum_d = 0.0;
//
//		for (i = 0; i <= 4; i++) {
//			sum_n_p += n_p[i];
//			sum_n_m += n_m[i];
//			sum_d += d_p[i];
//		}
//
//		a = sum_n_p / (1.0 + sum_d);
//		b = sum_n_m / (1.0 + sum_d);
//
//		for (i = 0; i <= 4; i++) {
//			bd_p[i] = d_p[i] * a;
//			bd_m[i] = d_m[i] * b;
//		}
//	}
//
//	private void transferGaussPixels(double[] src1, double[] src2, int[] dest, int bytes, int width) {
//		int i, j, k, b;
//		int bend = bytes * width;
//		double sum;
//
//		i = j = k = 0;
//		for (b = 0; b < bend; b++) {
//			sum = src1[i++] + src2[j++];
//
//			if (sum > 255)
//				sum = 255;
//			else if (sum < 0)
//				sum = 0;
//
//			dest[k++] = (int) sum;
//		}
//	}
}
