package com.topjohnwu.magisk.model.flash

import android.content.Context
import android.net.Uri
import androidx.core.os.postDelayed
import com.topjohnwu.magisk.tasks.FlashZip
import com.topjohnwu.magisk.utils.inject
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.internal.UiThreadHandler

sealed class Flashing(
    uri: Uri,
    private val console: MutableList<String>,
    log: MutableList<String>,
    private val resultListener: FlashResultListener
) : FlashZip(uri, console, log) {

    override fun onResult(success: Boolean) {
        if (!success) {
            console.add("! Installation failed")
        }

        resultListener.onResult(success)
    }

    class Install(
        uri: Uri,
        console: MutableList<String>,
        log: MutableList<String>,
        resultListener: FlashResultListener
    ) : Flashing(uri, console, log, resultListener) {

        override fun onResult(success: Boolean) {
            if (success) {
                //Utils.loadModules()
            }
            super.onResult(success)
        }

    }

    class Uninstall(
        uri: Uri,
        console: MutableList<String>,
        log: MutableList<String>,
        resultListener: FlashResultListener
    ) : Flashing(uri, console, log, resultListener) {

        private val context: Context by inject()

        override fun onResult(success: Boolean) {
            if (success) {
                UiThreadHandler.handler.postDelayed(3000) {
                    Shell.su("pm uninstall " + context.packageName).exec()
                }
            }
            super.onResult(success)
        }
    }

}