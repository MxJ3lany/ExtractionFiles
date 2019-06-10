package dev.utils.app;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.text.TextUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.DevUtils;
import dev.utils.LogPrintUtils;
import dev.utils.common.DevCommonUtils;

/**
 * detail: ADB shell 工具类
 * @author Ttt
 * <pre>
 *     Awesome ADB 一份超全超详细的 ADB 用法大全
 *     @see <a href="https://github.com/mzlogin/awesome-adb"/>
 *     <p></p>
 *     Process.waitFor() 的返回值含义
 *     @see <a href="https://blog.csdn.net/qq_35661171/article/details/79096786"/>
 *     <p></p>
 *     adb shell input
 *     @see <a href="https://blog.csdn.net/soslinken/article/details/49587497"/>
 *     <p></p>
 *     android 上发送 ADB 指令, 不需要加 adb shell
 *     @see <a href="https://www.imooc.com/qadetail/198264"/>
 *     <p></p>
 *     grep 是 linux 下的命令, windows 用 findstr
 *     开启 Thread 执行, 非主线程, 否则无响应并无效
 * </pre>
 */
public final class ADBUtils {

    private ADBUtils() {
    }

    // 日志 TAG
    private static final String TAG = ADBUtils.class.getSimpleName();
    // 正则 - 空格
    private static final String SPACE_STR = "\\s";
    // 换行字符串
    private static final String NEW_LINE_STR = System.getProperty("line.separator");

    /**
     * 判断设备是否 root
     * @return {@code true} yes, {@code false} no
     */
    public static boolean isDeviceRooted() {
        String su = "su";
        String[] locations = {"/system/bin/", "/system/xbin/", "/sbin/", "/system/sd/xbin/",
                "/system/bin/failsafe/", "/data/local/xbin/", "/data/local/bin/", "/data/local/"};
        for (String location : locations) {
            if (new File(location + su).exists()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 请求 Root 权限
     */
    public static void requestRoot() {
        ShellUtils.execCmd("exit", true);
    }

    /**
     * 判断 App 是否授权 Root 权限
     * @return {@code true} yes, {@code false} no
     */
    public static boolean isGrantedRoot() {
        ShellUtils.CommandResult result = ShellUtils.execCmd("exit", true);
        return result.isSuccess2();
    }

    // ============
    // = 应用管理 =
    // ============

    // ============
    // = 应用列表 =
    // ============

    /**
     * 获取 App 列表(包名)
     * <pre>
     *     @see <a href="https://blog.csdn.net/henni_719/article/details/62222439"/>
     * </pre>
     * @param type options
     * @return 返回对应选项的应用包名列表
     */
    public static List<String> getAppList(final String type) {
        // adb shell pm list packages [options]
        String typeStr = isSpace(type) ? "" : " " + type;
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd("pm list packages" + typeStr, false);
        if (result.isSuccess3()) {
            try {
                String[] arrays = result.successMsg.split(NEW_LINE_STR);
                return Arrays.asList(arrays);
            } catch (Exception e) {
                LogPrintUtils.eTag(TAG, e, "getAppList type => " + typeStr);
            }
        }
        return null;
    }

    /**
     * 获取 App 安装列表(包名)
     * @return App 安装列表(包名)
     */
    public static List<String> getInstallAppList() {
        return getAppList(null);
    }

    /**
     * 获取用户安装的应用列表(包名)
     * @return 用户安装的应用列表(包名)
     */
    public static List<String> getUserAppList() {
        return getAppList("-3");
    }

    /**
     * 获取系统应用列表(包名)
     * @return 系统应用列表(包名)
     */
    public static List<String> getSystemAppList() {
        return getAppList("-s");
    }

    /**
     * 获取启用的应用列表(包名)
     * @return 启用的应用列表(包名)
     */
    public static List<String> getEnableAppList() {
        return getAppList("-e");
    }

    /**
     * 获取禁用的应用列表(包名)
     * @return 禁用的应用列表(包名)
     */
    public static List<String> getDisableAppList() {
        return getAppList("-d");
    }

    /**
     * 获取包名包含字符串 xxx 的应用列表
     * @param filter 过滤获取字符串
     * @return 包名包含字符串 xxx 的应用列表
     */
    public static List<String> getAppListToFilter(final String filter) {
        if (isSpace(filter)) return null;
        return getAppList("| grep " + filter.trim());
    }

    /**
     * 判断是否安装应用
     * @param packageName 应用包名
     * @return {@code true} yes, {@code false} no
     */
    public static boolean isInstalledApp(final String packageName) {
        if (isSpace(packageName)) return false;
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd("pm path " + packageName, false);
        return result.isSuccess3();
    }

    /**
     * 查看应用安装路径
     * @param packageName 应用包名
     * @return 应用安装路径
     */
    public static String getAppInstallPath(final String packageName) {
        if (isSpace(packageName)) return null;
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd("pm path " + packageName, false);
        if (result.isSuccess3()) {
            return result.successMsg;
        }
        return null;
    }

    /**
     * 清除应用数据与缓存 - 相当于在设置里的应用信息界面点击了「清除缓存」和「清除数据」
     * @param packageName 应用包名
     * @return {@code true} success, {@code false} fail
     */
    public static boolean clearAppDataCache(final String packageName) {
        if (isSpace(packageName)) return false;
        // adb shell pm clear <packagename>
        String cmd = "pm clear %s";
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd(String.format(cmd, packageName), true);
        return result.isSuccess4("success");
    }

    // ============
    // = 应用信息 =
    // ============

    /**
     * 查看应用详细信息
     * <pre>
     *     输出中包含很多信息, 包括 Activity Resolver Table、Registered ContentProviders、
     *     包名、userId、安装后的文件资源代码等路径、版本信息、权限信息和授予状态、签名版本信息等
     * </pre>
     * @param packageName 应用包名
     * @return 应用详细信息
     */
    public static String getAppMessage(final String packageName) {
        if (isSpace(packageName)) return null;
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd("dumpsys package " + packageName, true);
        if (result.isSuccess3()) {
            return result.successMsg;
        }
        return null;
    }

    /**
     * 获取 App versionCode
     * @param packageName 应用包名
     * @return versionCode
     */
    public static int getVersionCode(final String packageName) {
        if (isSpace(packageName)) return 0;
        try {
            // 执行 shell
            ShellUtils.CommandResult result = ShellUtils.execCmd("dumpsys package " + packageName + " | grep version", true);
            if (result.isSuccess3()) {
                String[] arrays = result.successMsg.split(SPACE_STR);
                for (String str : arrays) {
                    if (!TextUtils.isEmpty(str)) {
                        try {
                            String[] datas = str.split("=");
                            if (datas.length == 2) {
                                if (datas[0].toLowerCase().equals("versionCode".toLowerCase())) {
                                    return Integer.parseInt(datas[1]);
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "getVersionCode");
        }
        return 0;
    }

    /**
     * 获取 App versionName
     * @param packageName 应用包名
     * @return versionName
     */
    public static String getVersionName(final String packageName) {
        if (isSpace(packageName)) return null;
        try {
            // 执行 shell
            ShellUtils.CommandResult result = ShellUtils.execCmd("dumpsys package " + packageName + " | grep version", true);
            if (result.isSuccess3()) {
                String[] arrays = result.successMsg.split(SPACE_STR);
                for (String str : arrays) {
                    if (!TextUtils.isEmpty(str)) {
                        try {
                            String[] datas = str.split("=");
                            if (datas.length == 2) {
                                if (datas[0].toLowerCase().equals("versionName".toLowerCase())) {
                                    return datas[1];
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "getVersionName");
        }
        return null;
    }

    // =============
    // = 安装/卸载 =
    // =============

    /**
     * 安装应用
     * @param filePath /sdcard/xxx/x.apk
     * @return {@code true} success, {@code false} fail
     */
    public static boolean installApp(final String filePath) {
        return installApp(filePath, "-rtsd");
    }

    /**
     * 安装应用
     * <pre>
     *     -l 将应用安装到保护目录 /mnt/asec
     *     -r 允许覆盖安装
     *     -t 允许安装 AndroidManifest.xml 里 application 指定 android:testOnly="true" 的应用
     *     -s 将应用安装到 sdcard
     *     -d 允许降级覆盖安装
     *     -g 授予所有运行时权限
     *     <p></p>
     *     android:testOnly="true"(ide 绿色三角运行)
     *     @see <a href="https://blog.csdn.net/lihenhao/article/details/79146211"/>
     * </pre>
     * @param filePath /sdcard/xxx/x.apk
     * @param params   安装选项
     * @return {@code true} success, {@code false} fail
     */
    public static boolean installApp(final String filePath, final String params) {
        if (isSpace(params)) return false;
        boolean isRoot = isDeviceRooted();
        // adb install [-lrtsdg] <path_to_apk>
        String cmd = "adb install %s %s";
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd(String.format(cmd, params, filePath), isRoot);
        // 判断是否成功
        return result.isSuccess4("success");
    }

    /**
     * 静默安装应用
     * @param filePath 文件路径
     * @return {@code true} success, {@code false} fail
     */
    public static boolean installAppSilent(final String filePath) {
        return installAppSilent(getFileByPath(filePath), null);
    }

    /**
     * 静默安装应用
     * @param file 文件
     * @return {@code true} success, {@code false} fail
     */
    public static boolean installAppSilent(final File file) {
        return installAppSilent(file, null);
    }

    /**
     * 静默安装应用
     * @param filePath 文件路径
     * @param params   安装选项
     * @return {@code true} success, {@code false} fail
     */
    public static boolean installAppSilent(final String filePath, final String params) {
        return installAppSilent(getFileByPath(filePath), params, isDeviceRooted());
    }

    /**
     * 静默安装应用
     * @param file   文件
     * @param params 安装选项
     * @return {@code true} success, {@code false} fail
     */
    public static boolean installAppSilent(final File file, final String params) {
        return installAppSilent(file, params, isDeviceRooted());
    }

    /**
     * 静默安装应用
     * @param file     文件
     * @param params   安装选项
     * @param isRooted 是否 root
     * @return {@code true} success, {@code false} fail
     */
    public static boolean installAppSilent(final File file, final String params, final boolean isRooted) {
        if (!isFileExists(file)) return false;
        String filePath = '"' + file.getAbsolutePath() + '"';
        String command = "LD_LIBRARY_PATH=/vendor/lib*:/system/lib* pm install " + (params == null ? "" : params + " ") + filePath;
        ShellUtils.CommandResult result = ShellUtils.execCmd(command, isRooted);
        return result.isSuccess4("success");
    }

    // =

    /**
     * 卸载应用
     * @param packageName 应用包名
     * @return {@code true} success, {@code false} fail
     */
    public static boolean uninstallApp(final String packageName) {
        return uninstallApp(packageName, false);
    }

    /**
     * 卸载应用
     * @param packageName 应用包名
     * @param isKeepData  -k 参数可选, 表示卸载应用但保留数据和缓存目录
     * @return {@code true} success, {@code false} fail
     */
    public static boolean uninstallApp(final String packageName, final boolean isKeepData) {
        if (isSpace(packageName)) return false;
        boolean isRoot = isDeviceRooted();
        // adb uninstall [-k] <packagename>
        String cmd = "adb uninstall ";
        if (isKeepData) {
            cmd += " -k ";
        }
        cmd += packageName;
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd(cmd, isRoot);
        // 判断是否成功
        return result.isSuccess4("success");
    }

    /**
     * 静默卸载应用
     * @param packageName 应用包名
     * @return {@code true} success, {@code false} fail
     */
    public static boolean uninstallAppSilent(final String packageName) {
        return uninstallAppSilent(packageName, false, isDeviceRooted());
    }

    /**
     * 静默卸载应用
     * @param packageName 应用包名
     * @param isKeepData  -k 参数可选, 表示卸载应用但保留数据和缓存目录
     * @return {@code true} success, {@code false} fail
     */
    public static boolean uninstallAppSilent(final String packageName, final boolean isKeepData) {
        return uninstallAppSilent(packageName, isKeepData, isDeviceRooted());
    }

    /**
     * 静默卸载应用
     * @param packageName 应用包名
     * @param isKeepData  -k 参数可选, 表示卸载应用但保留数据和缓存目录
     * @param isRooted    是否 root
     * @return {@code true} success, {@code false} fail
     */
    public static boolean uninstallAppSilent(final String packageName, final boolean isKeepData, final boolean isRooted) {
        if (isSpace(packageName)) return false;
        String command = "LD_LIBRARY_PATH=/vendor/lib*:/system/lib* pm uninstall " + (isKeepData ? "-k " : "") + packageName;
        ShellUtils.CommandResult result = ShellUtils.execCmd(command, isRooted);
        return result.isSuccess4("success");
    }

    // ===========
    // = dumpsys =
    // ===========

    /**
     * 获取对应包名应用启动的 Activity
     * <pre>
     *     android.intent.category.LAUNCHER (android.intent.action.MAIN)
     * </pre>
     * @param packageName 应用包名
     * @return package.xx.Activity.className
     */
    public static String getActivityToLauncher(final String packageName) {
        if (isSpace(packageName)) return null;
        String cmd = "dumpsys package %s";
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd(String.format(cmd, packageName), true);
        if (result.isSuccess3()) {
            String mainStr = "android.intent.action.MAIN:";
            int start = result.successMsg.indexOf(mainStr);
            // 防止都为 null
            if (start != -1) {
                try {
                    // 进行裁剪字符串
                    String subData = result.successMsg.substring(start + mainStr.length());
                    // 进行拆分
                    String[] arrays = subData.split(NEW_LINE_STR);
                    for (String str : arrays) {
                        if (!TextUtils.isEmpty(str)) {
                            // 存在包名才处理
                            if (str.indexOf(packageName) != -1) {
                                String[] splitArys = str.split(SPACE_STR);
                                for (String strData : splitArys) {
                                    if (!TextUtils.isEmpty(strData)) {
                                        // 属于 包名/ 前缀的
                                        if (strData.indexOf(packageName + "/") != -1) {
                                            // 防止属于 包名/.xx.Main_Activity
                                            if (strData.indexOf("/.") != -1) {
                                                // 包名/.xx.Main_Activity => 包名/包名.xx.Main_Activity
                                                strData = strData.replace("/", "/" + packageName);
                                            }
                                            return strData;
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    LogPrintUtils.eTag(TAG, e, "getActivityToLauncher " + packageName);
                }
            }
        }
        return null;
    }

    // ===================
    // = 获取当前 Window =
    // ===================

    /**
     * 获取当前显示的 Window
     * <pre>
     *     adb shell dumpsys window -h
     * </pre>
     * @return package.xx.Activity.className
     */
    public static String getWindowCurrent() {
        String cmd = "dumpsys window w | grep \\/  |  grep name=";
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd(cmd, true);
        if (result.isSuccess3()) {
            try {
                String nameStr = "name=";
                String[] arrays = result.successMsg.split(NEW_LINE_STR);
                for (String str : arrays) {
                    if (!TextUtils.isEmpty(str)) {
                        int start = str.indexOf(nameStr);
                        if (start != -1) {
                            try {
                                String subData = str.substring(start + nameStr.length());
                                if (subData.indexOf(")") != -1) {
                                    return subData.substring(0, subData.length() - 1);
                                }
                                return subData;
                            } catch (Exception e) {
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LogPrintUtils.eTag(TAG, e, "getWindowCurrent");
            }
        }
        return null;
    }

    /**
     * 获取当前显示的 Window
     * @return package/package.xx.Activity.className
     */
    public static String getWindowCurrent2() {
        String cmd = "dumpsys window windows | grep Current";
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd(cmd, true);
        if (result.isSuccess3()) {
            try {
                // 拆分换行, 并循环
                String[] arrays = result.successMsg.split(NEW_LINE_STR);
                for (String str : arrays) {
                    if (!TextUtils.isEmpty(str)) {
                        String[] splitArys = str.split(SPACE_STR);
                        if (splitArys != null && splitArys.length != 0) {
                            for (String splitStr : splitArys) {
                                if (!TextUtils.isEmpty(splitStr)) {
                                    int start = splitStr.indexOf("/");
                                    int lastIndex = splitStr.lastIndexOf("}");
                                    if (start != -1 && lastIndex != -1) {
                                        // 获取裁剪数据
                                        String strData = splitStr.substring(0, lastIndex);
                                        // 防止属于 包名/.xx.Main_Activity
                                        if (strData.indexOf("/.") != -1) {
                                            // 包名/.xx.Main_Activity => 包名/包名.xx.Main_Activity
                                            strData = strData.replace("/", "/" + splitStr.substring(0, start));
                                        }
                                        return strData;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LogPrintUtils.eTag(TAG, e, "getWindowCurrent2");
            }
        }
        return null;
    }

    /**
     * 获取对应包名显示的 Window
     * @param packageName 应用包名
     * @return package/package.xx.Activity.className
     */
    public static String getWindowCurrentToPackage(final String packageName) {
        if (isSpace(packageName)) return null;
        String cmd = "dumpsys window windows | grep %s";
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd(String.format(cmd, packageName), true);
        if (result.isSuccess3()) {
            try {
                // 拆分换行, 并循环
                String[] arrays = result.successMsg.split(NEW_LINE_STR);
                for (String str : arrays) {
                    if (!TextUtils.isEmpty(str)) {
                        String[] splitArys = str.split(SPACE_STR);
                        if (splitArys != null && splitArys.length != 0) {
                            for (String splitStr : splitArys) {
                                if (!TextUtils.isEmpty(splitStr)) {
                                    int start = splitStr.indexOf("/");
                                    int lastIndex = splitStr.lastIndexOf("}");
                                    if (start != -1 && lastIndex != -1 && splitStr.indexOf(packageName) == 0) {
                                        // 获取裁剪数据
                                        String strData = splitStr.substring(0, lastIndex);
                                        // 防止属于 包名/.xx.Main_Activity
                                        if (strData.indexOf("/.") != -1) {
                                            // 包名/.xx.Main_Activity => 包名/包名.xx.Main_Activity
                                            strData = strData.replace("/", "/" + packageName);
                                        }
                                        return strData;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LogPrintUtils.eTag(TAG, e, "getWindowCurrentToPackage");
            }
        }
        return null;
    }

    // =====================
    // = 获取当前 Activity =
    // =====================

    /**
     * 获取当前显示的 Activity
     * @return package.xx.Activity.className
     */
    public static String getActivityCurrent() {
        String cmd = "dumpsys activity activities | grep mFocusedActivity";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            cmd = "dumpsys activity activities | grep mResumedActivity";
        }
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd(cmd, true);
        if (result.isSuccess3()) {
            try {
                // 拆分换行, 并循环
                String[] arrays = result.successMsg.split(NEW_LINE_STR);
                for (String str : arrays) {
                    if (!TextUtils.isEmpty(str)) {
                        String[] splitArys = str.split(SPACE_STR);
                        if (splitArys != null && splitArys.length != 0) {
                            for (String splitStr : splitArys) {
                                if (!TextUtils.isEmpty(splitStr)) {
                                    int start = splitStr.indexOf("/");
                                    if (start != -1) {
                                        // 获取裁剪数据
                                        String strData = splitStr;
                                        // 防止属于 包名/.xx.Main_Activity
                                        if (strData.indexOf("/.") != -1) {
                                            // 包名/.xx.Main_Activity => 包名/包名.xx.Main_Activity
                                            strData = strData.replace("/", "/" + splitStr.substring(0, start));
                                        }
                                        return strData;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LogPrintUtils.eTag(TAG, e, "getActivityCurrent");
            }
        }
        return null;
    }

    /**
     * 获取 Activity 栈
     * @return 当前全部 Activity 栈信息
     */
    public static String getActivitys() {
        return getActivitys(null);
    }

    /**
     * 获取 Activity 栈
     * @param append 追加筛选条件
     * @return 当前全部 Activity 栈信息
     */
    public static String getActivitys(final String append) {
        String cmd = "dumpsys activity activities";
        if (!isSpace(append)) {
            cmd += " " + append.trim();
        }
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd(cmd, true);
        if (result.isSuccess3()) {
            return result.successMsg;
        }
        return null;
    }

    /**
     * 获取对应包名的 Activity 栈
     * @param packageName 应用包名
     * @return 对应包名的 Activity 栈信息
     */
    public static String getActivitysToPackage(final String packageName) {
        if (isSpace(packageName)) {
            return null;
        }
        return getActivitys("| grep " + packageName);
    }

    /**
     * 获取对应包名的 Activity 栈(处理成 List) - 最新的 Activity 越靠后
     * @param packageName 应用包名
     * @return 对应包名的 Activity 栈信息集合
     */
    public static List<String> getActivitysToPackageLists(final String packageName) {
        // 获取对应包名的 Activity 数据结果
        String result = getActivitysToPackage(packageName);
        // 防止数据为 null
        if (!TextUtils.isEmpty(result)) {
            try {
                List<String> lists = new ArrayList<>();
                String[] dataSplit = result.split(NEW_LINE_STR);
                // 拆分后, 数据长度
                int splitLength = dataSplit.length;
                // 获取 Activity 栈字符串
                String activities = null;
                // 判断最后一行是否符合条件
                if (dataSplit[splitLength - 1].indexOf("Activities=") != -1) {
                    activities = dataSplit[splitLength - 1];
                } else {
                    for (String str : dataSplit) {
                        if (str.indexOf("Activities=") != -1) {
                            activities = str;
                            break;
                        }
                    }
                }
                // 进行特殊处理 Activities=[ActivityRecord{xx},ActivityRecord{xx}];
                int startIndex = activities.indexOf("Activities=[");
                activities = activities.substring(startIndex + "Activities=[".length(), activities.length() - 1);
                // 再次进行拆分
                String[] activityArys = activities.split("ActivityRecord");
                for (String data : activityArys) {
                    try {
                        String[] splitArys = data.split(SPACE_STR);
                        if (splitArys != null && splitArys.length != 0) {
                            for (String splitStr : splitArys) {
                                int start = splitStr.indexOf(packageName + "/");
                                if (start != -1) {
                                    // 获取裁剪数据
                                    String strData = splitStr;
                                    // 防止属于 包名/.xx.XxxActivity
                                    if (strData.indexOf("/.") != -1) {
                                        // 包名/.xx.XxxActivity => 包名/包名.xx.XxxActivity
                                        strData = strData.replace("/", "/" + splitStr.substring(0, start));
                                    }
                                    // 保存数据
                                    lists.add(strData);
                                }
                            }
                        }
                    } catch (Exception e) {
                    }
                }
                return lists;
            } catch (Exception e) {
                LogPrintUtils.eTag(TAG, e, "getActivitysToPackageLists");
            }
        }
        return null;
    }

    // =

    /**
     * 判断 Activity 栈顶是否重复
     * @param packageName 应用包名
     * @param activity    Activity Name
     * @return {@code true} yes, {@code false} no
     */
    public static boolean isActivityTopRepeat(final String packageName, final String activity) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        } else if (TextUtils.isEmpty(activity)) {
            return false;
        }
        // 判断是否重复
        boolean isRepeat = false;
        // 获取
        List<String> lists = ADBUtils.getActivitysToPackageLists(packageName);
        // 数据长度
        int length = DevCommonUtils.length(lists);
        // 防止数据为 null
        if (length >= 2) { // 两个页面以上, 才能够判断是否重复
            try {
                if (lists.get(length - 1).endsWith(activity)) {
                    // 倒序遍历, 越后面是 Activity 栈顶
                    for (int i = length - 2; i >= 0; i--) {
                        String data = lists.get(i);
                        // 判断是否该页面结尾
                        return data.endsWith(activity);
                    }
                }
            } catch (Exception e) {
                LogPrintUtils.eTag(TAG, e, "isActivityTopRepeat");
            }
        }
        return isRepeat;
    }

    /**
     * 判断 Activity 栈顶是否重复
     * @param packageName 应用包名
     * @param activitys   Activity Name 集合
     * @return {@code true} yes, {@code false} no
     */
    public static boolean isActivityTopRepeat(final String packageName, final List<String> activitys) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        } else if (activitys == null || activitys.size() == 0) {
            return false;
        }
        // 判断是否重复
        boolean isRepeat = false;
        // 获取
        List<String> lists = ADBUtils.getActivitysToPackageLists(packageName);
        // 数据长度
        int length = DevCommonUtils.length(lists);
        // 防止数据为 null
        if (length >= 2) { // 两个页面以上, 才能够判断是否重复
            // 循环判断
            for (String activity : activitys) {
                try {
                    if (lists.get(length - 1).endsWith(activity)) {
                        // 倒序遍历, 越后面是 Activity 栈顶
                        for (int i = length - 2; i >= 0; i--) {
                            String data = lists.get(i);
                            // 判断是否该页面结尾
                            return data.endsWith(activity);
                        }
                    }
                } catch (Exception e) {
                    LogPrintUtils.eTag(TAG, e, "isActivityTopRepeat");
                }
            }
        }
        return isRepeat;
    }

    // =

    /**
     * 获取 Activity 栈顶重复总数
     * @param packageName 应用包名
     * @param activity    Activity Name
     * @return 指定 Activity 在栈顶重复总数
     */
    public static int getActivityTopRepeatCount(final String packageName, final String activity) {
        if (TextUtils.isEmpty(packageName)) {
            return 0;
        } else if (TextUtils.isEmpty(activity)) {
            return 0;
        }
        // 重复数量
        int number = 0;
        // 获取
        List<String> lists = ADBUtils.getActivitysToPackageLists(packageName);
        // 数据长度
        int length = DevCommonUtils.length(lists);
        // 防止数据为 null
        if (length >= 2) { // 两个页面以上, 才能够判断是否重复
            try {
                if (lists.get(length - 1).endsWith(activity)) {
                    // 倒序遍历, 越后面是 Activity 栈顶
                    for (int i = length - 2; i >= 0; i--) {
                        String data = lists.get(i);
                        // 判断是否该页面结尾
                        if (data.endsWith(activity)) {
                            number++;
                        } else {
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                LogPrintUtils.eTag(TAG, e, "getActivityTopRepeatCount");
            }
        }
        return number;
    }

    /**
     * 获取 Activity 栈顶重复总数
     * @param packageName 应用包名
     * @param activitys   Activity Name 集合
     * @return 指定 Activity 在栈顶重复总数
     */
    public static int getActivityTopRepeatCount(final String packageName, final List<String> activitys) {
        if (TextUtils.isEmpty(packageName)) {
            return 0;
        } else if (activitys == null || activitys.size() == 0) {
            return 0;
        }
        // 获取
        List<String> lists = ADBUtils.getActivitysToPackageLists(packageName);
        // 数据长度
        int length = DevCommonUtils.length(lists);
        // 防止数据为 null
        if (length >= 2) { // 两个页面以上, 才能够判断是否重复
            // 循环判断
            for (String activity : activitys) {
                try {
                    // 重复数量
                    int number = 0;
                    // 判断是否对应页面结尾
                    if (lists.get(length - 1).endsWith(activity)) {
                        // 倒序遍历, 越后面是 Activity 栈顶
                        for (int i = length - 2; i >= 0; i--) {
                            String data = lists.get(i);
                            // 判断是否该页面结尾
                            if (data.endsWith(activity)) {
                                number++;
                            } else {
                                break;
                            }
                        }
                        // 进行判断处理
                        return number;
                    }
                } catch (Exception e) {
                    LogPrintUtils.eTag(TAG, e, "getActivityTopRepeatCount");
                }
            }
        }
        return 0;
    }

    // =======================
    // = 正在运行的 Services =
    // =======================

    /**
     * 查看正在运行的 Services
     * @return 运行中的 Services 信息
     */
    public static String getServices() {
        return getServices(null);
    }

    /**
     * 查看正在运行的 Services
     * @param packageName 应用包名, 参数不是必须的, 指定 <packagename> 表示查看与某个包名相关的 Services,
     *                    不指定表示查看所有 Services, <packagename> 不一定要给出完整的包名,
     *                    比如运行 adb shell dumpsys activity services org.mazhuang
     *                    那么包名 org.mazhuang.demo1、org.mazhuang.demo2 和 org.mazhuang123 等相关的 Services 都会列出来
     * @return 运行中的 Services 信息
     */
    public static String getServices(final String packageName) {
        String cmd = "dumpsys activity services" + ((isSpace(packageName) ? "" : " " + packageName));
        ShellUtils.CommandResult result = ShellUtils.execCmd(cmd, true);
        if (result.isSuccess3()) {
            return result.successMsg;
        }
        return null;
    }

    // ======
    // = am =
    // ======

    /**
     * 启动自身应用
     * @return {@code true} success, {@code false} fail
     */
    public static boolean startSelfApp() {
        return startSelfApp(false);
    }

    /**
     * 启动自身应用
     * @param closeActivity 是否关闭 Activity 所属的 App 进程后再启动 Activity
     * @return {@code true} success, {@code false} fail
     */
    public static boolean startSelfApp(final boolean closeActivity) {
        try {
            // 获取包名
            String packageName = DevUtils.getContext().getPackageName();
            // 获取 Launcher Activity
            String activity = ActivityUtils.getLauncherActivity();
            // 跳转应用启动页(启动应用)
            startActivity(packageName + "/" + activity, closeActivity);
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "startSelfApp");
        }
        return false;
    }

    /**
     * 跳转页面 Activity
     * @param packageAndLauncher package/package.xx.Activity.className
     * @param closeActivity      是否关闭 Activity 所属的 App 进程后再启动 Activity
     * @return {@code true} success, {@code false} fail
     */
    public static boolean startActivity(final String packageAndLauncher, final boolean closeActivity) {
        return startActivity(packageAndLauncher, null, closeActivity);
    }

    /**
     * 跳转页面 Activity
     * @param packageAndLauncher package/package.xx.Activity.className
     * @param append             追加的信息, 例如传递参数等
     * @param closeActivity      是否关闭 Activity 所属的 App 进程后再启动 Activity
     * @return {@code true} success, {@code false} fail
     */
    public static boolean startActivity(final String packageAndLauncher, final String append, final boolean closeActivity) {
        if (isSpace(packageAndLauncher)) return false;
        try {
            // am start [options] <INTENT>
            String cmd = "am start %s";
            if (closeActivity) {
                cmd = String.format(cmd, "-S " + packageAndLauncher);
            } else {
                cmd = String.format(cmd, packageAndLauncher);
            }
            // 判断是否追加
            if (!isSpace(append)) {
                cmd += " " + append.trim();
            }
            // 执行 shell
            ShellUtils.CommandResult result = ShellUtils.execCmd(cmd, true);
            return result.isSuccess2();
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "startActivity");
        }
        return false;
    }

    /**
     * 启动服务
     * @param packageAndService package/package.xx.Service.className
     * @return {@code true} success, {@code false} fail
     */
    public static boolean startService(final String packageAndService) {
        return startService(packageAndService, null);
    }

    /**
     * 启动服务
     * @param packageAndService package/package.xx.Service.className
     * @param append            追加的信息, 例如传递参数等
     * @return {@code true} success, {@code false} fail
     */
    public static boolean startService(final String packageAndService, final String append) {
        if (isSpace(packageAndService)) return false;
        try {
            // am startservice [options] <INTENT>
            String cmd = "am startservice %s";
            // 进行格式化
            cmd = String.format(cmd, packageAndService);
            // 判断是否追加
            if (!isSpace(append)) {
                cmd += " " + append.trim();
            }
            // 执行 shell
            ShellUtils.CommandResult result = ShellUtils.execCmd(cmd, true);
            return result.isSuccess2();
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "startService");
        }
        return false;
    }

    /**
     * 停止服务
     * @param packageAndService package/package.xx.Service.className
     * @return {@code true} success, {@code false} fail
     */
    public static boolean stopService(final String packageAndService) {
        return stopService(packageAndService, null);
    }

    /**
     * 停止服务
     * @param packageAndService package/package.xx.Service.className
     * @param append            追加的信息, 例如传递参数等
     * @return {@code true} success, {@code false} fail
     */
    public static boolean stopService(final String packageAndService, final String append) {
        if (isSpace(packageAndService)) return false;
        try {
            // am stopservice [options] <INTENT>
            String cmd = "am stopservice %s";
            // 进行格式化
            cmd = String.format(cmd, packageAndService);
            // 判断是否追加
            if (!isSpace(append)) {
                cmd += " " + append.trim();
            }
            // 执行 shell
            ShellUtils.CommandResult result = ShellUtils.execCmd(cmd, true);
            return result.isSuccess3();
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "stopService");
        }
        return false;
    }

    /**
     * 发送广播(向所有组件发送)
     * <pre>
     *     向所有组件广播 BOOT_COMPLETED
     *     adb shell am broadcast -a android.intent.action.BOOT_COMPLETED
     * </pre>
     * @param broadcast 广播 INTENT
     * @return {@code true} success, {@code false} fail
     */
    public static boolean sendBroadcastToAll(final String broadcast) {
        if (isSpace(broadcast)) return false;
        try {
            // am broadcast [options] <INTENT>
            String cmd = "am broadcast -a %s";
            // 执行 shell
            ShellUtils.CommandResult result = ShellUtils.execCmd(String.format(cmd, broadcast), true);
            return result.isSuccess3();
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "sendBroadcastAll");
        }
        return false;
    }

    /**
     * 发送广播
     * <pre>
     *     只向 org.mazhuang.boottimemeasure/.BootCompletedReceiver 广播 BOOT_COMPLETED
     *     adb shell am broadcast -a android.intent.action.BOOT_COMPLETED -n org.mazhuang.boottimemeasure/.BootCompletedReceiver
     * </pre>
     * @param packageAndBroadcast package/package.xx.Receiver.className
     * @param broadcast           广播 INTENT
     * @return {@code true} success, {@code false} fail
     */
    public static boolean sendBroadcast(final String packageAndBroadcast, final String broadcast) {
        if (isSpace(packageAndBroadcast)) return false;
        if (isSpace(broadcast)) return false;
        try {
            // am broadcast [options] <INTENT>
            String cmd = "am broadcast -a %s -n %s";
            // 执行 shell
            ShellUtils.CommandResult result = ShellUtils.execCmd(String.format(cmd, broadcast, packageAndBroadcast), true);
            return result.isSuccess3();
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "sendBroadcast");
        }
        return false;
    }

    // =

    /**
     * 销毁进程
     * @param packageName 应用包名
     * @return {@code true} success, {@code false} fail
     */
    public static boolean kill(final String packageName) {
        if (isSpace(packageName)) return false;
        try {
            String cmd = "am force-stop %s";
            // 执行 shell
            ShellUtils.CommandResult result = ShellUtils.execCmd(String.format(cmd, packageName), true);
            return result.isSuccess2();
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "kill");
        }
        return false;
    }

    /**
     * 收紧内存
     * @param pid   进程 ID
     * @param level HIDDEN、RUNNING_MODERATE、BACKGROUND、RUNNING_LOW、MODERATE、RUNNING_CRITICAL、COMPLETE
     * @return {@code true} success, {@code false} fail
     */
    public static boolean sendTrimMemory(final int pid, final String level) {
        if (isSpace(level)) return false;
        try {
            String cmd = "am send-trim-memory %s %s";
            // 执行 shell
            ShellUtils.CommandResult result = ShellUtils.execCmd(String.format(cmd, pid, level), true);
            return result.isSuccess2();
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "sendTrimMemory");
        }
        return false;
    }

//    // ============
//    // = 文件管理 =
//    // ============
//
//    /**
//     * 复制设备里的文件到电脑
//     * @param remote 设备里的文件路径
//     * @param local  电脑上的目录
//     * @return {@code true} success, {@code false} fail
//     */
//    public static boolean pull(final String remote, final String local) {
//        if (isSpace(remote)) return false;
//        try {
//            // adb pull <设备里的文件路径> [电脑上的目录]
//            String cmd = "adb pull %s";
//            // 判断是否存到默认地址
//            if (!isSpace(local)) {
//                cmd += " " + local;
//            }
//            // 执行 shell
//            ShellUtils.CommandResult result = ShellUtils.execCmd(String.format(cmd, remote), true);
//            return result.isSuccess2();
//        } catch (Exception e) {
//            LogPrintUtils.eTag(TAG, e, "pull");
//        }
//        return false;
//    }
//
//    /**
//     * 复制电脑里的文件到设备
//     * @param local  电脑上的文件路径
//     * @param remote 设备里的目录
//     * @return {@code true} success, {@code false} fail
//     */
//    public static boolean push(final String local, final String remote) {
//        if (isSpace(local)) return false;
//        if (isSpace(remote)) return false;
//        try {
//            // adb push <电脑上的文件路径> <设备里的目录>
//            String cmd = "adb push %s %s";
//            // 执行 shell
//            ShellUtils.CommandResult result = ShellUtils.execCmd(String.format(cmd, local, remote), true);
//            return result.isSuccess2();
//        } catch (Exception e) {
//            LogPrintUtils.eTag(TAG, e, "push");
//        }
//        return false;
//    }

    // =========
    // = Input =
    // =========

    // ===============================
    // = tap - 模拟 touch 屏幕的事件 =
    // ===============================

    /**
     * 点击某个区域
     * @param x X 轴坐标
     * @param y Y 轴坐标
     * @return {@code true} success, {@code false} fail
     */
    public static boolean tap(final float x, final float y) {
        try {
            // input [touchscreen|touchpad|touchnavigation] tap <x> <y>
            // input [屏幕、触摸板、导航键] tap
            String cmd = "input touchscreen tap %s %s";
            // 执行 shell
            ShellUtils.CommandResult result = ShellUtils.execCmd(String.format(cmd, (int) x, (int) y), true);
            return result.isSuccess2();
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "tap");
        }
        return false;
    }

    // ====================
    // = swipe - 滑动事件 =
    // ====================

    /**
     * 按压某个区域(点击)
     * @param x X 轴坐标
     * @param y Y 轴坐标
     * @return {@code true} success, {@code false} fail
     */
    public static boolean swipeClick(final float x, final float y) {
        return swipe(x, y, x, y, 100L);
    }

    /**
     * 按压某个区域 time 大于一定时间变成长按
     * @param x    X 轴坐标
     * @param y    Y 轴坐标
     * @param time 按压时间
     * @return {@code true} success, {@code false} fail
     */
    public static boolean swipeClick(final float x, final float y, final long time) {
        return swipe(x, y, x, y, time);
    }

    /**
     * 滑动到某个区域
     * @param x    X 轴坐标
     * @param y    Y 轴坐标
     * @param toX  滑动到 X 轴坐标
     * @param toY  滑动到 Y 轴坐标
     * @param time 滑动时间(毫秒)
     * @return {@code true} success, {@code false} fail
     */
    public static boolean swipe(final float x, final float y, final float toX, final float toY, final long time) {
        try {
            // input [touchscreen|touchpad|touchnavigation] swipe <x1> <y1> <x2> <y2> [duration(ms)]
            String cmd = "input touchscreen swipe %s %s %s %s %s";
            // 执行 shell
            ShellUtils.CommandResult result = ShellUtils.execCmd(String.format(cmd, (int) x, (int) y, (int) toX, (int) toY, time), true);
            return result.isSuccess2();
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "swipe");
        }
        return false;
    }

    // ===================
    // = text - 模拟输入 =
    // ===================

    /**
     * 输入文本 - 不支持中文
     * @param txt 文本内容
     * @return {@code true} success, {@code false} fail
     */
    public static boolean text(final String txt) {
        if (isSpace(txt)) return false;
        try {
            // input text <string>
            String cmd = "input text %s";
            // 执行 shell
            ShellUtils.CommandResult result = ShellUtils.execCmd(String.format(cmd, txt), true); // false 可以执行
            return result.isSuccess2();
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "text");
        }
        return false;
    }

    // =======================
    // = keyevent - 按键操作 =
    // =======================

    /**
     * 触发某些按键
     * @param keyCode KeyEvent.xxx => KeyEvent.KEYCODE_BACK(返回键)
     * @return {@code true} success, {@code false} fail
     */
    public static boolean keyevent(final int keyCode) {
        try {
            // input keyevent <key code number or name>
            String cmd = "input keyevent %s";
            // 执行 shell
            ShellUtils.CommandResult result = ShellUtils.execCmd(String.format(cmd, keyCode), true); // false 可以执行
            return result.isSuccess2();
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "keyevent");
        }
        return false;
    }

    // ============
    // = 实用功能 =
    // ============

    /**
     * 屏幕截图
     * @param path 保存路径 /sdcard/xxx/x.png
     * @return {@code true} success, {@code false} fail
     */
    public static boolean screencap(final String path) {
        return screencap(path, 0);
    }

    /**
     * 屏幕截图
     * @param path      保存路径 /sdcard/xxx/x.png
     * @param displayId -d display-id 指定截图的显示屏编号(有多显示屏的情况下) 默认 0
     * @return {@code true} success, {@code false} fail
     */
    public static boolean screencap(final String path, final int displayId) {
        if (isSpace(path)) return false;
        try {
            String cmd = "screencap -p -d %s %s";
            // 执行 shell
            ShellUtils.CommandResult result = ShellUtils.execCmd(String.format(cmd, Math.max(displayId, 0), path), true);
            return result.isSuccess2();
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "screencap");
        }
        return false;
    }

    /**
     * 录制屏幕(以 mp4 格式保存到 /sdcard)
     * @param path 保存路径 /sdcard/xxx/x.mp4
     * @return {@code true} success, {@code false} fail
     */
    public static boolean screenrecord(final String path) {
        return screenrecord(path, null, -1, -1);
    }

    /**
     * 录制屏幕(以 mp4 格式保存到 /sdcard)
     * @param path 保存路径 /sdcard/xxx/x.mp4
     * @param time 录制时长, 单位秒(默认/最长 180秒)
     * @return {@code true} success, {@code false} fail
     */
    public static boolean screenrecord(final String path, final int time) {
        return screenrecord(path, null, -1, time);
    }

    /**
     * 录制屏幕(以 mp4 格式保存到 /sdcard)
     * @param path 保存路径 /sdcard/xxx/x.mp4
     * @param size 视频的尺寸, 比如 1280x720, 默认是屏幕分辨率
     * @param time 录制时长, 单位秒(默认/最长 180秒)
     * @return {@code true} success, {@code false} fail
     */
    public static boolean screenrecord(final String path, final String size, final int time) {
        return screenrecord(path, size, -1, time);
    }

    /**
     * 录制屏幕(以 mp4 格式保存到 /sdcard)
     * @param path    保存路径 /sdcard/xxx/x.mp4
     * @param size    视频的尺寸, 比如 1280x720, 默认是屏幕分辨率
     * @param bitRate 视频的比特率, 默认是 4Mbps
     * @param time    录制时长, 单位秒(默认/最长 180秒)
     * @return {@code true} success, {@code false} fail
     */
    public static boolean screenrecord(final String path, final String size, final int bitRate, final int time) {
        if (isSpace(path)) return false;
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("screenrecord");
            if (!isSpace(size)) {
                builder.append(" --size " + size);
            }
            if (bitRate > 0) {
                builder.append(" --bit-rate " + bitRate);
            }
            if (time > 0) {
                builder.append(" --time-limit " + time);
            }
            builder.append(" " + path);
            // 执行 shell
            ShellUtils.CommandResult result = ShellUtils.execCmd(builder.toString(), true);
            return result.isSuccess2();
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "screenrecord");
        }
        return false;
    }

    /**
     * 查看连接过的 Wifi 密码
     * @return 连接过的 Wifi 密码
     */
    public static String wifiConf() {
        try {
            String cmd = "cat /data/misc/wifi/*.conf";
            // 执行 shell
            ShellUtils.CommandResult result = ShellUtils.execCmd(cmd, true);
            if (result.isSuccess3()) {
                return result.successMsg;
            }
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "wifiConf");
        }
        return null;
    }

    /**
     * 开启/关闭 Wifi
     * @param open 是否开启
     * @return {@code true} success, {@code false} fail
     */
    public static boolean wifiSwitch(final boolean open) {
        String cmd = "svc wifi %s";
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd(String.format(cmd, open ? "enable" : "disable"), true);
        return result.isSuccess2();
    }

    /**
     * 设置系统时间
     * @param time yyyyMMdd.HHmmss 20160823.131500
     *             表示将系统日期和时间更改为 2016 年 08 月 23 日 13 点 15 分 00 秒
     * @return {@code true} success, {@code false} fail
     */
    public static boolean setSystemTime(final String time) {
        if (isSpace(time)) return false;
        try {
            String cmd = "date -s %s";
            // 执行 shell
            ShellUtils.CommandResult result = ShellUtils.execCmd(String.format(cmd, time), true);
            return result.isSuccess2();
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "setSystemTime");
        }
        return false;
    }

    /**
     * 设置系统时间
     * @param time MMddHHmmyyyy.ss 082313152016.00
     *             表示将系统日期和时间更改为 2016 年 08 月 23 日 13 点 15 分 00 秒
     * @return {@code true} success, {@code false} fail
     */
    public static boolean setSystemTime2(final String time) {
        if (isSpace(time)) return false;
        try {
            String cmd = "date %s";
            // 执行 shell
            ShellUtils.CommandResult result = ShellUtils.execCmd(String.format(cmd, time), true);
            return result.isSuccess2();
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "setSystemTime2");
        }
        return false;
    }

    /**
     * 设置系统时间
     * @param time 时间戳转换 MMddHHmmyyyy.ss
     * @return {@code true} success, {@code false} fail
     */
    public static boolean setSystemTime2(final long time) {
        if (time < 0) return false;
        try {
            String cmd = "date %s";
            // 执行 shell
            ShellUtils.CommandResult result = ShellUtils.execCmd(String.format(cmd, new SimpleDateFormat("MMddHHmmyyyy.ss").format(time)), true);
            return result.isSuccess2();
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "setSystemTime2");
        }
        return false;
    }

    // ================
    // = 刷机相关命令 =
    // ================

    /**
     * 关机(需要 root 权限)
     * @return {@code true} success, {@code false} fail
     */
    public static boolean shutdown() {
        try {
            ShellUtils.execCmd("reboot -p", true);
            Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
            intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
            DevUtils.getContext().startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            return true;
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "shutdown");
        }
        return false;
    }

    /**
     * 重启设备(需要 root 权限)
     * @return {@code true} success, {@code false} fail
     */
    public static boolean reboot() {
        try {
            ShellUtils.execCmd("reboot", true);
            Intent intent = new Intent(Intent.ACTION_REBOOT);
            intent.putExtra("nowait", 1);
            intent.putExtra("interval", 1);
            intent.putExtra("window", 0);
            DevUtils.getContext().sendBroadcast(intent);
            return true;
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "reboot");
        }
        return false;
    }

    /**
     * 重启设备(需要 root 权限) - 并进行特殊的引导模式 (recovery、Fastboot)
     * @param reason 传递给内核来请求特殊的引导模式, 如"recovery"
     *               重启到 Fastboot 模式 bootloader
     */
    public static void reboot(final String reason) {
        if (isSpace(reason)) return;
        try {
            PowerManager mPowerManager = (PowerManager) DevUtils.getContext().getSystemService(Context.POWER_SERVICE);
            if (mPowerManager == null)
                return;
            mPowerManager.reboot(reason);
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "reboot");
        }
    }

    /**
     * 重启引导到 recovery (需要 root 权限)
     * @return {@code true} success, {@code false} fail
     */
    public static boolean rebootToRecovery() {
        ShellUtils.CommandResult result = ShellUtils.execCmd("reboot recovery", true);
        return result.isSuccess2();
    }

    /**
     * 重启引导到 bootloader (需要 root 权限)
     * @return {@code true} success, {@code false} fail
     */
    public static boolean rebootToBootloader() {
        ShellUtils.CommandResult result = ShellUtils.execCmd("reboot bootloader", true);
        return result.isSuccess2();
    }

    // ============
    // = 滑动方法 =
    // ============

    /**
     * 发送事件滑动
     * @param x      X 轴坐标
     * @param y      Y 轴坐标
     * @param toX    滑动到 X 轴坐标
     * @param toY    滑动到 Y 轴坐标
     * @param number 循环次数
     */
    public static void sendEventSlide(final float x, final float y, final float toX, final float toY, final int number) {
        List<String> lists = new ArrayList<>();
        // = 开头 =
        lists.add("sendevent /dev/input/event1 3 57 109");
        lists.add("sendevent /dev/input/event1 3 53 " + x);
        lists.add("sendevent /dev/input/event1 3 54 " + y);
        // 发送 touch 事件(必须使用 0 0 0 配对)
        lists.add("sendevent /dev/input/event1 1 330 1");
        lists.add("sendevent /dev/input/event1 0 0 0");

        // 判断方向(手势是否从左到右) - View 往左滑, 手势操作往右滑
        boolean isLeftToRight = toX > x;
        // 判断方向(手势是否从上到下) - View 往上滑, 手势操作往下滑
        boolean isTopToBottom = toY > y;

        // 计算差数
        float diffX = isLeftToRight ? (toX - x) : (x - toX);
        float diffY = isTopToBottom ? (toY - y) : (y - toY);

        if (!isLeftToRight) {
            diffX = -diffX;
        }

        if (!isTopToBottom) {
            diffY = -diffY;
        }

        // 平均值
        float averageX = diffX / number;
        float averageY = diffY / number;
        // 上次位置
        int oldX = (int) x;
        int oldY = (int) y;

        // 循环处理
        for (int i = 0; i <= number; i++) {
            if (averageX != 0f) {
                // 进行判断处理
                int calcX = (int) (x + averageX * i);
                if (oldX != calcX) {
                    oldX = calcX;
                    lists.add("sendevent /dev/input/event1 3 53 " + calcX);
                }
            }

            if (averageY != 0f) {
                // 进行判断处理
                int calcY = (int) (y + averageY * i);
                if (oldY != calcY) {
                    oldY = calcY;
                    lists.add("sendevent /dev/input/event1 3 54 " + calcY);
                }
            }
            // 每次操作结束发送
            lists.add("sendevent /dev/input/event1 0 0 0");
        }
        // = 结尾 =
        lists.add("sendevent /dev/input/event1 3 57 4294967295");
        // 释放 touch 事件(必须使用 0 0 0 配对)
        lists.add("sendevent /dev/input/event1 1 330 0");
        lists.add("sendevent /dev/input/event1 0 0 0");

        // 执行 shell
        ShellUtils.execCmd(lists, true);
    }

    // ================
    // = 查看设备信息 =
    // ================

    /**
     * 获取 SDK 版本
     * @return SDK 版本
     */
    public static String getSDKVersion() {
        ShellUtils.CommandResult result = ShellUtils.execCmd("getprop ro.build.version.sdk", false);
        if (result.isSuccess3()) {
            return result.successMsg;
        }
        return null;
    }

    /**
     * 获取 Android 系统版本
     * @return Android 系统版本
     */
    public static String getAndroidVersion() {
        ShellUtils.CommandResult result = ShellUtils.execCmd("getprop ro.build.version.release", false);
        if (result.isSuccess3()) {
            return result.successMsg;
        }
        return null;
    }

    /**
     * 获取设备型号 - 如 RedmiNote4X
     * @return 设备型号
     */
    public static String getModel() {
        // android.os.Build 内部有信息 android.os.Build.MODEL
        ShellUtils.CommandResult result = ShellUtils.execCmd("getprop ro.product.model", false);
        if (result.isSuccess3()) {
            return result.successMsg;
        }
        return null;
    }

    /**
     * 获取设备品牌
     * @return 设备品牌
     */
    public static String getBrand() {
        ShellUtils.CommandResult result = ShellUtils.execCmd("getprop ro.product.brand", false);
        if (result.isSuccess3()) {
            return result.successMsg;
        }
        return null;
    }

    /**
     * 获取设备名
     * @return 设备名
     */
    public static String getDeviceName() {
        ShellUtils.CommandResult result = ShellUtils.execCmd("getprop ro.product.name", false);
        if (result.isSuccess3()) {
            return result.successMsg;
        }
        return null;
    }

    /**
     * 获取 CPU 支持的 abi 列表
     * @return CPU 支持的 abi 列表
     */
    public static String getCpuAbiList() {
        ShellUtils.CommandResult result = ShellUtils.execCmd("cat /system/build.prop | grep ro.product.cpu.abi", false);
        if (result.isSuccess3()) {
            return result.successMsg;
        }
        return null;
    }

    /**
     * 获取每个应用程序的内存上限
     * @return 每个应用程序的内存上限
     */
    public static String getAppHeapsize() {
        ShellUtils.CommandResult result = ShellUtils.execCmd("getprop dalvik.vm.heapsize", false);
        if (result.isSuccess3()) {
            return result.successMsg;
        }
        return null;
    }

    /**
     * 获取电池状况
     * @return 电池状况
     */
    public static String getBattery() {
        ShellUtils.CommandResult result = ShellUtils.execCmd("dumpsys battery", true);
        if (result.isSuccess3()) { // scale 代表最大电量, level 代表当前电量
            return result.successMsg;
        }
        return null;
    }

    /**
     * 获取屏幕密度
     * @return 屏幕密度
     */
    public static String getDensity() {
        ShellUtils.CommandResult result = ShellUtils.execCmd("getprop ro.sf.lcd_density", false);
        if (result.isSuccess3()) {
            return result.successMsg;
        }
        return null;
    }

    /**
     * 获取屏幕分辨率
     * @return 屏幕分辨率
     */
    public static String getScreenSize() {
        ShellUtils.CommandResult result = ShellUtils.execCmd("wm size", true);
        if (result.isSuccess3()) {
            // 正常返回 Physical size: 1080 x 1920
            // 如果使用命令修改过, 那输出可能是
            // Physical size: 1080 x 1920
            // Override size: 480 x 1024
            // 表明设备的屏幕分辨率原本是 1080px * 1920px, 当前被修改为 480px * 1024px
            return result.successMsg;
        }
        return null;
    }

    /**
     * 获取显示屏参数
     * @return 显示屏参数
     */
    public static String getDisplays() {
        ShellUtils.CommandResult result = ShellUtils.execCmd("dumpsys window displays", true);
        if (result.isSuccess3()) {
            return result.successMsg;
        }
        return null;
    }

    /**
     * 获取 Android id
     * @return Android id
     */
    public static String getAndroidId() {
        ShellUtils.CommandResult result = ShellUtils.execCmd("settings get secure android_id", true);
        if (result.isSuccess3()) {
            return result.successMsg;
        }
        return null;
    }

    /**
     * 获取 IMEI 码
     * @return IMEI 码
     */
    public static String getIMEI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ShellUtils.CommandResult result = ShellUtils.execCmd("service call iphonesubinfo 1", true);
            if (result.isSuccess3()) {
                try {
                    int index = 0;
                    StringBuilder builder = new StringBuilder();
                    String subStr = result.successMsg.replaceAll("\\.", "");
                    subStr = subStr.substring(subStr.indexOf("'") + 1, subStr.indexOf("')"));
                    // 添加数据
                    builder.append(subStr.substring(0, subStr.indexOf("'")));
                    // 从指定索引开始
                    index = subStr.indexOf("'", builder.toString().length() + 1);
                    // 再次裁剪
                    subStr = subStr.substring(index + 1);
                    // 添加数据
                    builder.append(subStr.substring(0, subStr.indexOf("'")));
                    // 从指定索引开始
                    index = subStr.indexOf("'", builder.toString().length() + 1);
                    // 再次裁剪
                    subStr = subStr.substring(index + 1);
                    // 最后进行添加
                    builder.append(subStr.split(SPACE_STR)[0]);
                    // 返回对应的数据
                    return builder.toString();
                } catch (Exception e) {
                    LogPrintUtils.eTag(TAG, e, "getIMEI");
                }
            }
        } else {
            // 在 Android 4.4 及以下版本可通过如下命令获取 IMEI
            ShellUtils.CommandResult result = ShellUtils.execCmd("dumpsys iphonesubinfo", true);
            if (result.isSuccess3()) { // 返回值中的 Device ID 就是 IMEI
                try {
                    String[] splitArys = result.successMsg.split(NEW_LINE_STR);
                    for (String str : splitArys) {
                        if (!TextUtils.isEmpty(str)) {
                            if (str.toLowerCase().indexOf("device") != -1) {
                                // 进行拆分
                                String[] arrays = str.split(SPACE_STR);
                                return arrays[arrays.length - 1];
                            }
                        }
                    }
                } catch (Exception e) {
                    LogPrintUtils.eTag(TAG, e, "getIMEI");
                }
            }
        }
        return null;
    }

    /**
     * 获取 IP 地址
     * @return IP 地址
     */
    public static String getIPAddress() {
        boolean isRoot = false;
        ShellUtils.CommandResult result = ShellUtils.execCmd("ifconfig | grep Mask", isRoot);
        if (result.isSuccess3()) {
            return result.successMsg;
        } else { // 如果设备连着 Wifi, 可以使用如下命令来查看局域网 IP
            result = ShellUtils.execCmd("ifconfig wlan0", isRoot);
            if (result.isSuccess3()) {
                return result.successMsg;
            } else {
                // 可以看到网络连接名称、启用状态、IP 地址和 Mac 地址等信息
                result = ShellUtils.execCmd("netcfg", isRoot);
                if (result.isSuccess3()) {
                    return result.successMsg;
                }
            }
        }
        return null;
    }

    /**
     * 获取 Mac 地址
     * @return Mac 地址
     */
    public static String getMac() {
        ShellUtils.CommandResult result = ShellUtils.execCmd("cat /sys/class/net/wlan0/address", false);
        if (result.isSuccess3()) {
            return result.successMsg;
        }
        return null;
    }

    /**
     * 获取 CPU 信息
     * @return CPU 信息
     */
    public static String getCPU() {
        ShellUtils.CommandResult result = ShellUtils.execCmd("cat /proc/cpuinfo", false);
        if (result.isSuccess3()) {
            return result.successMsg;
        }
        return null;
    }

    /**
     * 获取内存信息
     * @return 内存信息
     */
    public static String getMeminfo() {
        ShellUtils.CommandResult result = ShellUtils.execCmd("cat /proc/meminfo", false);
        if (result.isSuccess3()) {
            return result.successMsg;
        }
        return null;
    }

    // ============
    // = 修改设置 =
    // ============

    /**
     * 设置屏幕大小
     * @param width  屏幕宽度
     * @param height 屏幕高度
     * @return {@code true} success, {@code false} fail
     */
    public static boolean setScreenSize(final int width, final int height) {
        String cmd = "wm size %sx%s";
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd(String.format(cmd, width, height), true);
        return result.isSuccess2();
    }

    /**
     * 恢复原分辨率命令
     * @return {@code true} success, {@code false} fail
     */
    public static boolean resetScreen() {
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd("wm size reset", true);
        return result.isSuccess2();
    }

    /**
     * 设置屏幕密度
     * @param density 屏幕密度
     * @return {@code true} success, {@code false} fail
     */
    public static boolean setDensity(final int density) {
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd("wm density " + density, true);
        return result.isSuccess2();
    }

    /**
     * 恢复原屏幕密度
     * @return {@code true} success, {@code false} fail
     */
    public static boolean resetDensity() {
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd("wm density reset", true);
        return result.isSuccess2();
    }

    /**
     * 显示区域 (设置留白边距)
     * @param left   left padding
     * @param top    top padding
     * @param right  right padding
     * @param bottom bottom padding
     * @return {@code true} success, {@code false} fail
     */
    public static boolean setOverscan(final int left, final int top, final int right, final int bottom) {
        String cmd = "wm overscan %s,%s,%s,%s";
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd(String.format(cmd, left, top, right, bottom), true);
        return result.isSuccess2();
    }

    /**
     * 恢复原显示区域
     * @return {@code true} success, {@code false} fail
     */
    public static boolean resetOverscan() {
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd("wm overscan reset", true);
        return result.isSuccess2();
    }

    /**
     * 获取亮度是否为自动获取 (自动调节亮度)
     * @return 1 开启、0 未开启、-1 未知
     */
    public static int getScreenBrightnessMode() {
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd("settings get system screen_brightness_mode", true);
        if (result.isSuccess3()) {
            try {
                return Integer.parseInt(result.successMsg);
            } catch (Exception e) {
            }
        }
        return -1;
    }

    /**
     * 设置亮度是否为自动获取 (自动调节亮度)
     * @param isAuto 是否自动调节
     * @return {@code true} success, {@code false} fail
     */
    public static boolean setScreenBrightnessMode(final boolean isAuto) {
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd("settings put system screen_brightness_mode " + (isAuto ? 1 : 0), true);
        return result.isSuccess3();
    }

    /**
     * 获取屏幕亮度值
     * @return 屏幕亮度值
     */
    public static String getScreenBrightness() {
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd("settings get system screen_brightness", true);
        if (result.isSuccess3()) {
            String suc = result.successMsg;
            if (suc.startsWith("\"")) {
                suc = suc.substring(1);
            }
            if (suc.endsWith("\"")) {
                suc = suc.substring(0, suc.length() - 1);
            }
            return suc;
        }
        return null;
    }

    /**
     * 更改屏幕亮度值 (亮度值在 0-255 之间)
     * @param brightness 亮度值
     * @return {@code true} success, {@code false} fail
     */
    public static boolean setScreenBrightness(final int brightness) {
        if (brightness < 0) {
            return false;
        } else if (brightness > 255) {
            return false;
        }
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd("settings put system screen_brightness " + brightness, true);
        return result.isSuccess2();
    }

    /**
     * 获取自动锁屏休眠时间 (单位毫秒)
     * @return 自动锁屏休眠时间
     */
    public static String getScreenOffTimeout() {
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd("settings get system screen_off_timeout", true);
        if (result.isSuccess3()) {
            return result.successMsg;
        }
        return null;
    }

    /**
     * 设置自动锁屏休眠时间 (单位毫秒)
     * <pre>
     *     设置永不休眠 Integer.MAX_VALUE
     * </pre>
     * @param time 休眠时间 (单位毫秒)
     * @return {@code true} success, {@code false} fail
     */
    public static boolean setScreenOffTimeout(final long time) {
        if (time <= 0) {
            return false;
        }
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd("settings put system screen_off_timeout " + time, true);
        return result.isSuccess2();
    }

    /**
     * 获取日期时间选项中通过网络获取时间的状态
     * @return 1 允许、0 不允许、-1 未知
     */
    public static int getGlobalAutoTime() {
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd("settings get global auto_time", true);
        if (result.isSuccess3()) {
            try {
                return Integer.parseInt(result.successMsg);
            } catch (Exception e) {
            }
        }
        return -1;
    }

    /**
     * 修改日期时间选项中通过网络获取时间的状态, 设置是否开启
     * @param isOpen 是否设置通过网络获取时间
     * @return {@code true} success, {@code false} fail
     */
    public static boolean setGlobalAutoTime(final boolean isOpen) {
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd("settings put global auto_time " + (isOpen ? 1 : 0), true);
        return result.isSuccess3();
    }

    /**
     * 关闭 USB 调试模式
     * @return {@code true} success, {@code false} fail
     */
    public static boolean disableADB() {
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd("settings put global adb_enabled 0", true);
        return result.isSuccess2();
    }

    /**
     * 允许访问非 SDK API
     * <pre>
     *     不需要设备获得 Root 权限
     * </pre>
     * @return 执行结果
     */
    public static int putHiddenApi() {
        String[] cmds = new String[2];
        cmds[0] = "settings put global hidden_api_policy_pre_p_apps 1";
        cmds[1] = "settings put global hidden_api_policy_p_apps 1";
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd(cmds, true);
        return result.result;
    }

    /**
     * 禁止访问非 SDK API
     * <pre>
     *     不需要设备获得 Root 权限
     * </pre>
     * @return 执行结果
     */
    public static int deleteHiddenApi() {
        String[] cmds = new String[2];
        cmds[0] = "settings delete global hidden_api_policy_pre_p_apps";
        cmds[1] = "settings delete global hidden_api_policy_p_apps";
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd(cmds, true);
        return result.result;
    }

    /**
     * 开启无障碍辅助功能
     * @param packageName              应用包名
     * @param accessibilityServiceName 无障碍服务名
     * @return {@code true} success, {@code false} fail
     */
    public static boolean openAccessibility(final String packageName, final String accessibilityServiceName) {
        if (isSpace(packageName)) return false;
        if (isSpace(accessibilityServiceName)) return false;

        String cmd = "settings put secure enabled_accessibility_services %s/%s";
        // 格式化 shell 命令
        String[] cmds = new String[2];
        cmds[0] = String.format(cmd, packageName, accessibilityServiceName);
        cmds[1] = "settings put secure accessibility_enabled 1";
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd(cmds, true);
        return result.isSuccess2();
    }

    /**
     * 关闭无障碍辅助功能
     * @param packageName              应用包名
     * @param accessibilityServiceName 无障碍服务名
     * @return {@code true} success, {@code false} fail
     */
    public static boolean closeAccessibility(final String packageName, final String accessibilityServiceName) {
        if (isSpace(packageName)) return false;
        if (isSpace(accessibilityServiceName)) return false;

        String cmd = "settings put secure enabled_accessibility_services %s/%s";
        // 格式化 shell 命令
        String[] cmds = new String[2];
        cmds[0] = String.format(cmd, packageName, accessibilityServiceName);
        cmds[1] = "settings put secure accessibility_enabled 0";
        // 执行 shell
        ShellUtils.CommandResult result = ShellUtils.execCmd(cmds, true);
        return result.isSuccess2();
    }

    // ============
    // = 内部方法 =
    // ============

    /**
     * 检查是否存在某个文件
     * @param file 文件路径
     * @return {@code true} yes, {@code false} no
     */
    private static boolean isFileExists(final File file) {
        return file != null && file.exists();
    }

    /**
     * 获取文件
     * @param filePath 文件路径
     * @return 文件 {@link File}
     */
    private static File getFileByPath(final String filePath) {
        return filePath != null ? new File(filePath) : null;
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
