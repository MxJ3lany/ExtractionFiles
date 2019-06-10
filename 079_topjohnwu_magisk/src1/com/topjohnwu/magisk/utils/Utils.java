package com.topjohnwu.magisk.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.widget.Toast;

import com.topjohnwu.magisk.App;
import com.topjohnwu.magisk.BuildConfig;
import com.topjohnwu.magisk.ClassMap;
import com.topjohnwu.magisk.Config;
import com.topjohnwu.magisk.Const;
import com.topjohnwu.magisk.R;
import com.topjohnwu.magisk.model.entity.OldModule;
import com.topjohnwu.magisk.model.update.UpdateCheckService;
import com.topjohnwu.net.Networking;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.internal.UiThreadHandler;
import com.topjohnwu.superuser.io.SuFile;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.WorkerThread;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

public class Utils {

    public static void toast(CharSequence msg, int duration) {
        UiThreadHandler.run(() -> Toast.makeText(App.self, msg, duration).show());
    }

    public static void toast(int resId, int duration) {
        UiThreadHandler.run(() -> Toast.makeText(App.self, resId, duration).show());
    }

    public static String dlString(String url) {
        String s = Networking.get(url).execForString().getResult();
        return s == null ? "" : s;
    }

    public static int getPrefsInt(SharedPreferences prefs, String key, int def) {
        return Integer.parseInt(prefs.getString(key, String.valueOf(def)));
    }

    public static int getPrefsInt(SharedPreferences prefs, String key) {
        return getPrefsInt(prefs, key, 0);
    }

    public static int dpInPx(int dp) {
        float scale = App.self.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5);
    }

    public static String fmt(String fmt, Object... args) {
        return String.format(Locale.US, fmt, args);
    }

    public static String getAppLabel(ApplicationInfo info, PackageManager pm) {
        try {
            if (info.labelRes > 0) {
                Resources res = pm.getResourcesForApplication(info);
                Configuration config = new Configuration();
                config.setLocale(LocaleManager.getLocale());
                res.updateConfiguration(config, res.getDisplayMetrics());
                return res.getString(info.labelRes);
            }
        } catch (Exception ignored) {}
        return info.loadLabel(pm).toString();
    }

    public static String getLegalFilename(CharSequence filename) {
        return filename.toString().replace(" ", "_").replace("'", "").replace("\"", "")
                .replace("$", "").replace("`", "").replace("*", "").replace("/", "_")
                .replace("#", "").replace("@", "").replace("\\", "_");
    }

    @WorkerThread
    public static Map<String, OldModule> loadModulesLeanback() {
        final Map<String, OldModule> moduleMap = new ValueSortedMap<>();
        final SuFile path = new SuFile(Const.MAGISK_PATH);
        final SuFile[] modules = path.listFiles((file, name) ->
                !name.equals("lost+found") && !name.equals(".core")
        );
        for (SuFile file : modules) {
            if (file.isFile()) continue;
            OldModule module = new OldModule(Const.MAGISK_PATH + "/" + file.getName());
            moduleMap.put(module.getId(), module);
        }
        return moduleMap;
    }

    public static boolean showSuperUser() {
        return Shell.rootAccess() && (Const.USER_ID == 0 ||
                (int) Config.get(Config.Key.SU_MULTIUSER_MODE) !=
                        Config.Value.MULTIUSER_MODE_OWNER_MANAGED);
    }

    public static boolean isCanary() {
        return BuildConfig.VERSION_NAME.contains("-");
    }

    public static void scheduleUpdateCheck() {
        if (Config.get(Config.Key.CHECK_UPDATES)) {
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresDeviceIdle(true)
                    .build();
            PeriodicWorkRequest request = new PeriodicWorkRequest
                    .Builder(ClassMap.get(UpdateCheckService.class), 12, TimeUnit.HOURS)
                    .setConstraints(constraints)
                    .build();
            WorkManager.getInstance().enqueueUniquePeriodicWork(
                    Const.ID.CHECK_MAGISK_UPDATE_WORKER_ID,
                    ExistingPeriodicWorkPolicy.REPLACE, request);
        } else {
            WorkManager.getInstance().cancelUniqueWork(Const.ID.CHECK_MAGISK_UPDATE_WORKER_ID);
        }
    }

    public static void openLink(Context context, Uri link) {
        Intent intent = new Intent(Intent.ACTION_VIEW, link);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            toast(R.string.open_link_failed_toast, Toast.LENGTH_SHORT);
        }
    }

}
