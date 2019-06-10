package dev.utils.app.info;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.annotation.Keep;

import dev.DevUtils;
import dev.utils.LogPrintUtils;
import dev.utils.common.FileUtils;

/**
 * detail: App 信息实体类
 * @author Ttt
 */
public class AppInfoBean {

    // 日志 TAG
    private static final String TAG = AppInfoBean.class.getSimpleName();
    @Keep // App 包名
    private String appPackName;
    @Keep // App 应用名
    private String appName;
    @Keep // App 图标
    private transient Drawable appIcon;
    @Keep // App 类型
    private AppType appType;
    @Keep // App 版本号
    private int versionCode;
    @Keep // App 版本名
    private String versionName;
    @Keep // App 首次安装时间
    private long firstInstallTime;
    @Keep // App 最后一次更新时间
    private long lastUpdateTime;
    @Keep // App 地址
    private String sourceDir;
    @Keep // APK 大小
    private long apkSize;

    /**
     * 获取 AppInfoBean
     * @param packageInfo {@link PackageInfo}
     * @return {@link AppInfoBean}
     */
    protected static AppInfoBean obtain(final PackageInfo packageInfo) {
        try {
            return new AppInfoBean(packageInfo);
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "obtain");
        }
        return null;
    }

    /**
     * 初始化 AppInfoBean
     * @param packageInfo {@link PackageInfo}
     */
    protected AppInfoBean(final PackageInfo packageInfo) {
        this(packageInfo, DevUtils.getContext().getPackageManager());
    }

    /**
     * 初始化 AppInfoBean
     * @param packageInfo    {@link PackageInfo}
     * @param packageManager {@link PackageManager}
     */
    protected AppInfoBean(final PackageInfo packageInfo, final PackageManager packageManager) {
        // App 包名
        appPackName = packageInfo.applicationInfo.packageName;
        // App 应用名
        appName = packageManager.getApplicationLabel(packageInfo.applicationInfo).toString();
        // App 图标
        appIcon = packageManager.getApplicationIcon(packageInfo.applicationInfo);
        // App 类型
        appType = AppInfoBean.getAppType(packageInfo);
        // App 版本号
        versionCode = packageInfo.versionCode;
        // App 版本名
        versionName = packageInfo.versionName;
        // App 首次安装时间
        firstInstallTime = packageInfo.firstInstallTime;
        // App 最后一次更新时间
        lastUpdateTime = packageInfo.lastUpdateTime;
        // App 地址
        sourceDir = packageInfo.applicationInfo.sourceDir;
        // APK 大小
        apkSize = FileUtils.getFileLength(sourceDir);
    }

    /**
     * 获取 App 包名
     * @return App 包名
     */
    public String getAppPackName() {
        return appPackName;
    }

    /**
     * 获取 App 应用名
     * @return App 应用名
     */
    public String getAppName() {
        return appName;
    }

    /**
     * 获取 App 图标
     * @return App 图标
     */
    public Drawable getAppIcon() {
        return appIcon;
    }

    /**
     * 获取 App 类型
     * @return App 类型
     */
    public AppType getAppType() {
        return appType;
    }

    /**
     * 获取 versionCode
     * @return versionCode
     */
    public int getVersionCode() {
        return versionCode;
    }

    /**
     * 获取 versionName
     * @return versionName
     */
    public String getVersionName() {
        return versionName;
    }

    /**
     * 获取 App 首次安装时间
     * @return App 首次安装时间
     */
    public long getFirstInstallTime() {
        return firstInstallTime;
    }

    /**
     * 获取 App 最后更新时间
     * @return App 最后更新时间
     */
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    /**
     * 获取 APK 地址
     * @return APK 地址
     */
    public String getSourceDir() {
        return sourceDir;
    }

    /**
     * 获取 APK 大小
     * @return APK 大小
     */
    public long getApkSize() {
        return apkSize;
    }

    // =

    /**
     * detail: 应用类型
     * @author Ttt
     */
    public enum AppType {

        USER, // 用户 App

        SYSTEM, // 系统 App

        ALL // 全部 App
    }

    /**
     * 获取 App 类型
     * @param packageInfo {@link PackageInfo}
     * @return {@link AppType} 应用类型
     */
    public static AppType getAppType(final PackageInfo packageInfo) {
        if (!isSystemApp(packageInfo) && !isSystemUpdateApp(packageInfo)) {
            return AppType.USER;
        }
        return AppType.SYSTEM;
    }

    /**
     * 是否系统程序
     * @param packageInfo {@link PackageInfo}
     * @return {@code true} yes, {@code false} no
     */
    public static boolean isSystemApp(final PackageInfo packageInfo) {
        return ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    /**
     * 是否系统程序被手动更新后, 也成为第三方应用程序
     * @param packageInfo {@link PackageInfo}
     * @return {@code true} yes, {@code false} no
     */
    public static boolean isSystemUpdateApp(final PackageInfo packageInfo) {
        return ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
    }
}
