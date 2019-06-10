package com.topjohnwu.magisk.model.entity

import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import com.topjohnwu.magisk.utils.packageInfo
import com.topjohnwu.magisk.utils.processes

class HideAppInfo(
    val info: ApplicationInfo,
    val name: String,
    val icon: Drawable
) {

    val processes = info.packageInfo?.processes?.distinct() ?: listOf(info.packageName)

}
