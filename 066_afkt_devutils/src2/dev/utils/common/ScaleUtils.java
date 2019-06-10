package dev.utils.common;

import dev.utils.JCLogUtils;

/**
 * detail: 计算比例工具类
 * @author Ttt
 */
public final class ScaleUtils {

    private ScaleUtils() {
    }

    // 日志 TAG
    private static final String TAG = ScaleUtils.class.getSimpleName();

    // =======
    // = int =
    // =======

    /**
     * 计算缩放比例 - 根据宽度比例转换高度
     * @param targetWidth   需要的最终宽度
     * @param currentWidth  当前宽度
     * @param currentHeight 当前高度
     * @return int[] {宽度, 高度}
     */
    public static int[] calcScaleToWidth(final int targetWidth, final int currentWidth, final int currentHeight) {
        try {
            if (currentWidth == 0) {
                return new int[]{0, 0};
            }
            // 计算比例
            float scale = ((float) targetWidth) / ((float) currentWidth);
            // 计算缩放后的高度
            int scaleHeight = (int) (scale * (float) currentHeight);
            // 返回对应的数据
            return new int[]{targetWidth, scaleHeight};
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "calcScaleToWidth");
        }
        return null;
    }

    /**
     * 计算缩放比例 - 根据高度比例转换宽度
     * @param targetHeight  需要的最终高度
     * @param currentWidth  当前宽度
     * @param currentHeight 当前高度
     * @return int[] {宽度, 高度}
     */
    public static int[] calcScaleToHeight(final int targetHeight, final int currentWidth, final int currentHeight) {
        try {
            if (currentHeight == 0) {
                return new int[]{0, 0};
            }
            // 计算比例
            float scale = ((float) targetHeight) / ((float) currentHeight);
            // 计算缩放后的宽度
            int scaleWidth = (int) (scale * (float) currentWidth);
            // 返回对应的数据
            return new int[]{scaleWidth, targetHeight};
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "calcScaleToHeight");
        }
        return null;
    }

    /**
     * 通过宽度、高度根据对应的比例, 转换成对应的比例宽度高度 - 智能转换
     * @param width       宽度
     * @param height      高度
     * @param widthScale  宽度比例
     * @param heightScale 高度比例
     * @return int[] {宽度, 高度}
     */
    public static int[] calcWidthHeightToScale(final int width, final int height, final float widthScale, final float heightScale) {
        try {
            // 如果宽度的比例, 大于等于高度比例
            if (widthScale >= heightScale) { // 以宽度为基准
                // 设置宽度, 以宽度为基准
                int scaleWidth = width;
                // 计算宽度
                int scaleHeight = (int) (((float) scaleWidth) * (heightScale / widthScale));
                // 返回对应的比例
                return new int[]{scaleWidth, scaleHeight};
            } else { // 以高度为基准
                // 设置高度
                int scaleHeight = height;
                // 同步缩放比例
                int scaleWidth = (int) (((float) scaleHeight) * (widthScale / heightScale));
                // 返回对应的比例
                return new int[]{scaleWidth, scaleHeight};
            }
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "calcWidthHeightToScale");
        }
        return null;
    }

    /**
     * 以宽度为基准, 转换对应比例的高度
     * @param width       宽度
     * @param widthScale  宽度比例
     * @param heightScale 高度比例
     * @return int[] {宽度, 高度}
     */
    public static int[] calcWidthToScale(final int width, final float widthScale, final float heightScale) {
        try {
            // 设置宽度
            int scaleWidth = width;
            // 计算高度
            int scaleHeight = (int) (((float) scaleWidth) * (heightScale / widthScale));
            // 返回对应的比例
            return new int[]{scaleWidth, scaleHeight};
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "calcWidthToScale");
        }
        return null;
    }

    /**
     * 以高度为基准, 转换对应比例的宽度
     * @param height      高度
     * @param widthScale  宽度比例
     * @param heightScale 高度比例
     * @return int[] {宽度, 高度}
     */
    public static int[] calcHeightToScale(final int height, final float widthScale, final float heightScale) {
        try {
            // 设置高度
            int scaleHeight = height;
            // 计算宽度
            int scaleWidth = (int) (((float) scaleHeight) * (widthScale / heightScale));
            // 返回对应的比例
            return new int[]{scaleWidth, scaleHeight};
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "calcHeightToScale");
        }
        return null;
    }

    // ==========
    // = double =
    // ==========

    /**
     * 计算缩放比例 - 根据宽度比例转换高度
     * @param targetWidth   需要的最终宽度
     * @param currentWidth  当前宽度
     * @param currentHeight 当前高度
     * @return double[] {宽度, 高度}
     */
    public static double[] calcScaleToWidth(final double targetWidth, final double currentWidth, final double currentHeight) {
        try {
            if (currentWidth == 0d) {
                return new double[]{0d, 0d};
            }
            // 计算比例
            double scale = targetWidth / currentWidth;
            // 计算缩放后的高度
            double scaleHeight = scale * currentHeight;
            // 返回对应的数据
            return new double[]{targetWidth, scaleHeight};
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "calcScaleToWidth");
        }
        return null;
    }

    /**
     * 计算缩放比例 - 根据高度比例转换宽度
     * @param targetHeight  需要的最终高度
     * @param currentWidth  当前宽度
     * @param currentHeight 当前高度
     * @return double[] {宽度, 高度}
     */
    public static double[] calcScaleToHeight(final double targetHeight, final double currentWidth, final double currentHeight) {
        try {
            if (currentHeight == 0d) {
                return new double[]{0d, 0d};
            }
            // 计算比例
            double scale = targetHeight / currentHeight;
            // 计算缩放后的宽度
            double scaleWidth = scale * currentWidth;
            // 返回对应的数据
            return new double[]{scaleWidth, targetHeight};
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "calcScaleToHeight");
        }
        return null;
    }

    /**
     * 通过宽度、高度根据对应的比例, 转换成对应的比例宽度高度 - 智能转换
     * @param width       宽度
     * @param height      高度
     * @param widthScale  宽度比例
     * @param heightScale 高度比例
     * @return double[] {宽度, 高度}
     */
    public static double[] calcWidthHeightToScale(final double width, final double height, final double widthScale, final double heightScale) {
        try {
            // 如果宽度的比例, 大于等于高度比例
            if (widthScale >= heightScale) { // 以宽度为基准
                // 设置宽度, 以宽度为基准
                double scaleWidth = width;
                // 计算宽度
                double scaleHeight = scaleWidth * (heightScale / widthScale);
                // 返回对应的比例
                return new double[]{scaleWidth, scaleHeight};
            } else { // 以高度为基准
                // 设置高度
                double scaleHeight = height;
                // 同步缩放比例
                double scaleWidth = scaleHeight * (widthScale / heightScale);
                // 返回对应的比例
                return new double[]{scaleWidth, scaleHeight};
            }
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "calcWidthHeightToScale");
        }
        return null;
    }

    /**
     * 以宽度为基准, 转换对应比例的高度
     * @param width       宽度
     * @param widthScale  宽度比例
     * @param heightScale 高度比例
     * @return double[] {宽度, 高度}
     */
    public static double[] calcWidthToScale(final double width, final double widthScale, final double heightScale) {
        try {
            // 设置宽度
            double scaleWidth = width;
            // 计算高度
            double scaleHeight = scaleWidth * (heightScale / widthScale);
            // 返回对应的比例
            return new double[]{scaleWidth, scaleHeight};
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "calcWidthToScale");
        }
        return null;
    }

    /**
     * 以高度为基准, 转换对应比例的宽度
     * @param height      高度
     * @param widthScale  宽度比例
     * @param heightScale 高度比例
     * @return double[] {宽度, 高度}
     */
    public static double[] calcHeightToScale(final double height, final double widthScale, final double heightScale) {
        try {
            // 设置高度
            double scaleHeight = height;
            // 计算宽度
            double scaleWidth = scaleHeight * (widthScale / heightScale);
            // 返回对应的比例
            return new double[]{scaleWidth, scaleHeight};
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "calcHeightToScale");
        }
        return null;
    }

    // =========
    // = long =
    // =========

    /**
     * 计算缩放比例 - 根据宽度比例转换高度
     * @param targetWidth   需要的最终宽度
     * @param currentWidth  当前宽度
     * @param currentHeight 当前高度
     * @return long[] {宽度, 高度}
     */
    public static long[] calcScaleToWidth(final long targetWidth, final long currentWidth, final long currentHeight) {
        try {
            if (currentWidth == 0L) {
                return new long[]{0L, 0L};
            }
            // 计算比例
            long scale = targetWidth / currentWidth;
            // 计算缩放后的高度
            long scaleHeight = scale * currentHeight;
            // 返回对应的数据
            return new long[]{targetWidth, scaleHeight};
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "calcScaleToWidth");
        }
        return null;
    }

    /**
     * 计算缩放比例 - 根据高度比例转换宽度
     * @param targetHeight  需要的最终高度
     * @param currentWidth  当前宽度
     * @param currentHeight 当前高度
     * @return long[] {宽度, 高度}
     */
    public static long[] calcScaleToHeight(final long targetHeight, final long currentWidth, final long currentHeight) {
        try {
            if (currentHeight == 0L) {
                return new long[]{0L, 0L};
            }
            // 计算比例
            long scale = targetHeight / currentHeight;
            // 计算缩放后的宽度
            long scaleWidth = scale * currentWidth;
            // 返回对应的数据
            return new long[]{scaleWidth, targetHeight};
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "calcScaleToHeight");
        }
        return null;
    }

    /**
     * 通过宽度、高度根据对应的比例, 转换成对应的比例宽度高度 - 智能转换
     * @param width       宽度
     * @param height      高度
     * @param widthScale  宽度比例
     * @param heightScale 高度比例
     * @return long[] {宽度, 高度}
     */
    public static long[] calcWidthHeightToScale(final long width, final long height, final long widthScale, final long heightScale) {
        try {
            // 如果宽度的比例, 大于等于高度比例
            if (widthScale >= heightScale) { // 以宽度为基准
                // 设置宽度, 以宽度为基准
                long scaleWidth = width;
                // 计算宽度
                long scaleHeight = scaleWidth * (heightScale / widthScale);
                // 返回对应的比例
                return new long[]{scaleWidth, scaleHeight};
            } else { // 以高度为基准
                // 设置高度
                long scaleHeight = height;
                // 同步缩放比例
                long scaleWidth = scaleHeight * (widthScale / heightScale);
                // 返回对应的比例
                return new long[]{scaleWidth, scaleHeight};
            }
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "calcWidthHeightToScale");
        }
        return null;
    }

    /**
     * 以宽度为基准, 转换对应比例的高度
     * @param width       宽度
     * @param widthScale  宽度比例
     * @param heightScale 高度比例
     * @return long[] {宽度, 高度}
     */
    public static long[] calcWidthToScale(final long width, final long widthScale, final long heightScale) {
        try {
            // 设置宽度
            long scaleWidth = width;
            // 计算高度
            long scaleHeight = scaleWidth * (heightScale / widthScale);
            // 返回对应的比例
            return new long[]{scaleWidth, scaleHeight};
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "calcWidthToScale");
        }
        return null;
    }

    /**
     * 以高度为基准, 转换对应比例的宽度
     * @param height      高度
     * @param widthScale  宽度比例
     * @param heightScale 高度比例
     * @return long[] {宽度, 高度}
     */
    public static long[] calcHeightToScale(final long height, final long widthScale, final long heightScale) {
        try {
            // 设置高度
            long scaleHeight = height;
            // 计算宽度
            long scaleWidth = scaleHeight * (widthScale / heightScale);
            // 返回对应的比例
            return new long[]{scaleWidth, scaleHeight};
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "calcHeightToScale");
        }
        return null;
    }

    // =========
    // = float =
    // =========

    /**
     * 计算缩放比例 - 根据宽度比例转换高度
     * @param targetWidth   需要的最终宽度
     * @param currentWidth  当前宽度
     * @param currentHeight 当前高度
     * @return float[] {宽度, 高度}
     */
    public static float[] calcScaleToWidth(final float targetWidth, final float currentWidth, final float currentHeight) {
        try {
            if (currentWidth == 0f) {
                return new float[]{0f, 0f};
            }
            // 计算比例
            float scale = targetWidth / currentWidth;
            // 计算缩放后的高度
            float scaleHeight = scale * currentHeight;
            // 返回对应的数据
            return new float[]{targetWidth, scaleHeight};
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "calcScaleToWidth");
        }
        return null;
    }

    /**
     * 计算缩放比例 - 根据高度比例转换宽度
     * @param targetHeight  需要的最终高度
     * @param currentWidth  当前宽度
     * @param currentHeight 当前高度
     * @return float[] {宽度, 高度}
     */
    public static float[] calcScaleToHeight(final float targetHeight, final float currentWidth, final float currentHeight) {
        try {
            if (currentHeight == 0f) {
                return new float[]{0f, 0f};
            }
            // 计算比例
            float scale = targetHeight / currentHeight;
            // 计算缩放后的宽度
            float scaleWidth = scale * currentWidth;
            // 返回对应的数据
            return new float[]{scaleWidth, targetHeight};
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "calcScaleToHeight");
        }
        return null;
    }

    /**
     * 通过宽度、高度根据对应的比例, 转换成对应的比例宽度高度 - 智能转换
     * @param width       宽度
     * @param height      高度
     * @param widthScale  宽度比例
     * @param heightScale 高度比例
     * @return float[] {宽度, 高度}
     */
    public static float[] calcWidthHeightToScale(final float width, final float height, final float widthScale, final float heightScale) {
        try {
            // 如果宽度的比例, 大于等于高度比例
            if (widthScale >= heightScale) { // 以宽度为基准
                // 设置宽度, 以宽度为基准
                float scaleWidth = width;
                // 计算宽度
                float scaleHeight = scaleWidth * (heightScale / widthScale);
                // 返回对应的比例
                return new float[]{scaleWidth, scaleHeight};
            } else { // 以高度为基准
                // 设置高度
                float scaleHeight = height;
                // 同步缩放比例
                float scaleWidth = scaleHeight * (widthScale / heightScale);
                // 返回对应的比例
                return new float[]{scaleWidth, scaleHeight};
            }
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "calcWidthHeightToScale");
        }
        return null;
    }

    /**
     * 以宽度为基准, 转换对应比例的高度
     * @param width       宽度
     * @param widthScale  宽度比例
     * @param heightScale 高度比例
     * @return float[] {宽度, 高度}
     */
    public static float[] calcWidthToScale(final float width, final float widthScale, final float heightScale) {
        try {
            // 设置宽度
            float scaleWidth = width;
            // 计算高度
            float scaleHeight = scaleWidth * (heightScale / widthScale);
            // 返回对应的比例
            return new float[]{scaleWidth, scaleHeight};
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "calcWidthToScale");
        }
        return null;
    }

    /**
     * 以高度为基准, 转换对应比例的宽度
     * @param height      高度
     * @param widthScale  宽度比例
     * @param heightScale 高度比例
     * @return float[] {宽度, 高度}
     */
    public static float[] calcHeightToScale(final float height, final float widthScale, final float heightScale) {
        try {
            // 设置高度
            float scaleHeight = height;
            // 计算宽度
            float scaleWidth = scaleHeight * (widthScale / heightScale);
            // 返回对应的比例
            return new float[]{scaleWidth, scaleHeight};
        } catch (Exception e) {
            JCLogUtils.eTag(TAG, e, "calcHeightToScale");
        }
        return null;
    }
}
