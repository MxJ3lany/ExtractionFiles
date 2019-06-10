package me.panpf.tool4a.content.pm;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;

/**
 * 版本管理器
 */
public class VersionManager {
    /**
     * 键 - 旧的版本号
     */
    public static final String PREFERENCES_KEY_OLD_VERSION_CODE = "PREFERENCES_KEY_OLD_VERSION_CODE";
    /**
     * 键 - 旧的版本名称
     */
    public static final String PREFERENCES_KEY_OLD_VERSION_NAME = "PREFERENCES_KEY_OLD_VERSION_NAME";

    private PackageInfo packageInfo;
    private SharedPreferences sharedPreferences;

    public VersionManager(Context context, SharedPreferences sharedPreferences) {
        try {
            this.packageInfo = context.getPackageManager() != null ? context.getPackageManager().getPackageInfo(context.getPackageName(), 0) : null;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        this.sharedPreferences = sharedPreferences;
    }

    public VersionManager(Context context) {
        this(context, PreferenceManager.getDefaultSharedPreferences(context));
    }

    /**
     * 获取旧的版本号
     *
     * @return 旧的版本号
     */
    public int getOldVersionCode() {
        return sharedPreferences.getInt(PREFERENCES_KEY_OLD_VERSION_CODE, -1);
    }

    /**
     * 获取旧的版本名称
     *
     * @return 旧的版本名称
     */
    public String getOldVersionName() {
        return sharedPreferences.getString(PREFERENCES_KEY_OLD_VERSION_NAME, null);
    }

    /**
     * 比较之前安装的版本和当前的版本是否一样
     *
     * @return true：一样；false：不一样
     */
    public boolean isChange() {
        int oldVersion = getOldVersionCode();
        return oldVersion == -1 || oldVersion != packageInfo.versionCode;
    }

    /**
     * 更新旧的版本
     */
    public void updateOldVersion() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PREFERENCES_KEY_OLD_VERSION_CODE, packageInfo.versionCode);
        editor.putString(PREFERENCES_KEY_OLD_VERSION_NAME, packageInfo.versionName);
        editor.commit();
    }
}