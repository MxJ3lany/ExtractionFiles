/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.douya.util;

import android.os.Build;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import me.zhanghai.android.douya.reflected.ReflectedMethod;

/**
 * @see StorageManager
 * @see <a href="https://android.googlesource.com/platform/frameworks/base/+/ics-mr0-release/core/java/android/os/storage/StorageManager.java">
 *      ics-mr0-release/StorageManager.java</a>
 */
public class StorageManagerCompat {

    @NonNull
    private static final ReflectedMethod sGetVolumeListMethod = new ReflectedMethod(
            StorageManager.class, "getVolumeList");

    private StorageManagerCompat() {}

    /**
     * @see StorageManager#getStorageVolumes()
     */
    @NonNull
    public static List<StorageVolume> getStorageVolumes(@NonNull StorageManager storageManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return storageManager.getStorageVolumes();
        } else {
            StorageVolume[] storageVolumes = sGetVolumeListMethod.invoke(storageManager);
            return Arrays.asList(storageVolumes);
        }
    }
}
