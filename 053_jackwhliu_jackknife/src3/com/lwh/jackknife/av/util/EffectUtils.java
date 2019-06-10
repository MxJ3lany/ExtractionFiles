package com.lwh.jackknife.av.util;

public class EffectUtils {

    /**
     * 音效的类型：正常。
     */
    public static final int MODE_NORMAL = 0x0;

    /**
     * 音效的类型：萝莉。
     */
    public static final int MODE_LUOLI = 0x1;

    /**
     * 音效的类型：大叔。
     */
    public static final int MODE_DASHU = 0x2;

    /**
     * 音效的类型：惊悚。
     */
    public static final int MODE_JINGSONG = 0x3;

    /**
     * 音效的类型：搞怪。
     */
    public static final int MODE_GAOGUAI = 0x4;

    /**
     * 音效的类型：空灵。
     */
    public static final int MODE_KONGLING = 0x5;

    /**
     * 音效处理。
     *
     * @param path 需要变声的原音频文件
     * @param type 音效的类型
     */
    public native static void fix(String path, int type);

    static {
        System.loadLibrary("fmodL");
        System.loadLibrary("fmod");
        System.loadLibrary("jknfav");
    }
}
