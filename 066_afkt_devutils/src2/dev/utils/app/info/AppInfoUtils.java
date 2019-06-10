package dev.utils.app.info;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dev.DevUtils;
import dev.utils.LogPrintUtils;

/**
 * detail: App 信息获取工具类
 * @author Ttt
 */
public final class AppInfoUtils {

    private AppInfoUtils() {
    }

    // 日志 TAG
    private static final String TAG = AppInfoUtils.class.getSimpleName();
    // 换行字符串
    private static final String NEW_LINE_STR = System.getProperty("line.separator");

    /**
     * 通过 APK 路径 初始化 PackageInfo
     * @param file APK 文件路径
     * @return {@link PackageInfo}
     */
    public static PackageInfo getPackageInfoToFile(final File file) {
        if (!isFileExists(file)) return null;
        return getPackageInfoToPath(file.getAbsolutePath());
    }

    /**
     * 通过 APK 路径 初始化 PackageInfo
     * @param apkUri APK 文件路径
     * @return {@link PackageInfo}
     */
    public static PackageInfo getPackageInfoToPath(final String apkUri) {
        try {
            PackageManager packageManager = DevUtils.getContext().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageArchiveInfo(apkUri, PackageManager.GET_ACTIVITIES);
            // 设置 APK 位置信息
            ApplicationInfo appInfo = packageInfo.applicationInfo;
            // 必须加这两句, 不然下面 icon 获取是 default icon 而不是应用包的 icon
            appInfo.sourceDir = apkUri;
            appInfo.publicSourceDir = apkUri;
            return packageInfo;
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "getPackageInfoToPath");
        }
        return null;
    }

    /**
     * 获取当前应用 PackageInfo
     * @return {@link PackageInfo}
     */
    public static PackageInfo getPackageInfo() {
        return getPackageInfo(DevUtils.getContext().getPackageName());
    }

    /**
     * 通过包名 获取 PackageInfo
     * <pre>
     *     @see <a href="https://blog.csdn.net/sljjyy/article/details/17370665"/>
     * </pre>
     * @param packageName 应用包名
     * @return {@link PackageInfo}
     */
    public static PackageInfo getPackageInfo(final String packageName) {
        try {
            PackageManager packageManager = DevUtils.getContext().getPackageManager();
            // 获取对应的 PackageInfo (原始的 PackageInfo 获取 signatures 等于 null, 需要这样获取)
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            // 返回 App 信息
            return packageInfo;
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "getPackageInfo");
        }
        return null;
    }

    // ================
    // = 获取基本信息 =
    // ================

    /**
     * 通过 APK 路径 获取 AppInfoBean
     * @param file APK 文件路径
     * @return {@link AppInfoBean}
     */
    public static AppInfoBean getAppInfoBeanToFile(final File file) {
        return AppInfoBean.obtain(getPackageInfoToFile(file));
    }

    /**
     * 通过 APK 路径 获取 AppInfoBean
     * @param apkUri APK 文件路径
     * @return {@link AppInfoBean}
     */
    public static AppInfoBean getAppInfoBeanToPath(final String apkUri) {
        return AppInfoBean.obtain(getPackageInfoToPath(apkUri));
    }

    /**
     * 获取当前应用 AppInfoBean
     * @return {@link AppInfoBean}
     */
    public static AppInfoBean getAppInfoBean() {
        return AppInfoBean.obtain(getPackageInfo());
    }

    /**
     * 通过包名 获取 AppInfoBean
     * @param packageName 应用包名
     * @return {@link AppInfoBean}
     */
    public static AppInfoBean getAppInfoBean(final String packageName) {
        return AppInfoBean.obtain(getPackageInfo(packageName));
    }

    // ================
    // = 获取详细信息 =
    // ================

    /**
     * 获取 APK 详细信息
     * @param file APK 文件路径
     * @return {@link ApkInfoItem}
     */
    public static ApkInfoItem getApkInfoItem(final File file) {
        if (!isFileExists(file)) return null;
        return getApkInfoItem(file.getAbsolutePath());
    }

    /**
     * 获取 APK 详细信息
     * @param apkUri APK 文件路径
     * @return {@link ApkInfoItem}
     */
    public static ApkInfoItem getApkInfoItem(final String apkUri) {
        try {
            return ApkInfoItem.obtain(getPackageInfoToPath(apkUri));
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "getApkInfoItem");
            return null;
        }
    }

    // =

    /**
     * 获取 App 详细信息
     * @return {@link AppInfoItem}
     */
    public static AppInfoItem getAppInfoItem() {
        try {
            return AppInfoItem.obtain(getPackageInfo());
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "getAppInfoItem");
            return null;
        }
    }

    /**
     * 获取 App 详细信息
     * @param packageName 应用包名
     * @return {@link AppInfoItem}
     */
    public static AppInfoItem getAppInfoItem(final String packageName) {
        try {
            return AppInfoItem.obtain(getPackageInfo(packageName));
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "getAppInfoItem");
            return null;
        }
    }

    // =

    /**
     * 获取全部 App 列表
     * @return 返回 App 列表
     */
    public static List<AppInfoBean> getAppLists() {
        return getAppLists(AppInfoBean.AppType.ALL);
    }

    /**
     * 获取 App 列表
     * @param appType App 类型
     * @return 返回 App 列表
     */
    public static List<AppInfoBean> getAppLists(final AppInfoBean.AppType appType) {
        // App 信息
        ArrayList<AppInfoBean> listApps = new ArrayList<>();
        // 防止为 null
        if (appType != null) {
            // 管理应用程序包
            PackageManager packageManager = DevUtils.getContext().getPackageManager();
            // 获取手机内所有应用
            List<PackageInfo> packlist = packageManager.getInstalledPackages(0);
            // 判断是否属于添加全部
            if (appType == AppInfoBean.AppType.ALL) {
                // 遍历 App 列表
                for (int i = 0, len = packlist.size(); i < len; i++) {
                    PackageInfo packageInfo = packlist.get(i);
                    // 添加符合条件的 App 应用信息
                    listApps.add(new AppInfoBean(packageInfo, packageManager));
                }
            } else {
                // 遍历 App 列表
                for (int i = 0, len = packlist.size(); i < len; i++) {
                    PackageInfo packageInfo = packlist.get(i);
                    // 获取 App 类型
                    AppInfoBean.AppType cAppType = AppInfoBean.getAppType(packageInfo);
                    // 判断类型
                    if (appType == cAppType) {
                        // 添加符合条件的 App 应用信息
                        listApps.add(new AppInfoBean(packageInfo, packageManager));
                    }
                }
            }
        }
        return listApps;
    }

    // =

    /**
     * 获取 APK 注册的权限
     * @return APK 注册的权限数组
     */
    public static String[] getApkPermission() {
        return getApkPermission(DevUtils.getContext().getPackageName());
    }

    /**
     * 获取 APK 注册的权限
     * @param packageName 应用包名
     * @return APK 注册的权限数组
     */
    public static String[] getApkPermission(final String packageName) {
        try {
            PackageManager packageManager = DevUtils.getContext().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            return packageInfo.requestedPermissions;
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "getApkPermission");
        }
        return null;
    }

    /**
     * 打印 APK 注册的权限
     * <pre>
     *     @see <a href="https://www.cnblogs.com/leaven/p/5485864.html"/>
     * </pre>
     * @param packageName 应用包名
     */
    public static void printApkPermission(final String packageName) {
        try {
            StringBuilder builder = new StringBuilder();
            // =
            PackageManager packageManager = DevUtils.getContext().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            String[] usesPermissionsArray = packageInfo.requestedPermissions;
            for (int i = 0; i < usesPermissionsArray.length; i++) {
                // 获取每个权限的名字, 如: android.permission.INTERNET
                String usesPermissionName = usesPermissionsArray[i];
                // 拼接日志
                builder.append("usesPermissionName = " + usesPermissionName);
                builder.append(NEW_LINE_STR);

                // 通过 usesPermissionName 获取该权限的详细信息
                PermissionInfo permissionInfo = packageManager.getPermissionInfo(usesPermissionName, 0);

                // 获取该权限属于哪个权限组, 如: 网络通信
                PermissionGroupInfo permissionGroupInfo = packageManager.getPermissionGroupInfo(permissionInfo.group, 0);
                // 拼接日志
                builder.append("permissionGroup = " + permissionGroupInfo.loadLabel(packageManager).toString());
                builder.append(NEW_LINE_STR);

                // 获取该权限的标签信息, 比如: 完全的网络访问权限
                String permissionLabel = permissionInfo.loadLabel(packageManager).toString();
                // 拼接日志
                builder.append("permissionLabel = " + permissionLabel);
                builder.append(NEW_LINE_STR);

                // 获取该权限的详细描述信息, 比如: 允许该应用创建网络套接字和使用自定义网络协议
                // 浏览器和其他某些应用提供了向互联网发送数据的途径, 因此应用无需该权限即可向互联网发送数据
                String permissionDescription = permissionInfo.loadDescription(packageManager).toString();
                // 拼接日志
                builder.append("permissionDescription = " + permissionDescription);
                builder.append(NEW_LINE_STR);
            }
            // 打印日志
            LogPrintUtils.dTag(TAG, builder.toString());
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "printApkPermission");
        }
    }

    // =

    /**
     * 检查是否存在某个文件
     * @param file 文件路径
     * @return {@code true} yes, {@code false} no
     */
    private static boolean isFileExists(final File file) {
        return file != null && file.exists();
    }
}
