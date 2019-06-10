package com.topjohnwu.magisk.data.repository

import com.topjohnwu.magisk.Const
import com.topjohnwu.magisk.data.database.LogDao
import com.topjohnwu.magisk.data.database.base.suRaw
import com.topjohnwu.magisk.model.entity.MagiskLog
import com.topjohnwu.magisk.model.entity.WrappedMagiskLog
import com.topjohnwu.magisk.utils.toSingle
import com.topjohnwu.superuser.Shell
import timber.log.Timber
import java.util.concurrent.TimeUnit


class LogRepository(
    private val logDao: LogDao
) {

    fun fetchLogs() = logDao.fetchAll()
        .map { it.sortByDescending { it.date.time }; it }
        .map { it.wrap() }

    fun fetchMagiskLogs() = "tail -n 5000 ${Const.MAGISK_LOG}".suRaw()
        .filter { it.isNotEmpty() }
        .map { Timber.i(it.toString()); it }

    fun clearLogs() = logDao.deleteAll()
    fun clearOutdated() = logDao.deleteOutdated()

    fun clearMagiskLogs() = Shell.su("echo -n > " + Const.MAGISK_LOG)
        .toSingle()
        .map { it.exec() }

    fun put(log: MagiskLog) = logDao.put(log)

    private fun List<MagiskLog>.wrap(): List<WrappedMagiskLog> {
        val day = TimeUnit.DAYS.toMillis(1)
        return groupBy { it.date.time / day }
            .map { WrappedMagiskLog(it.key * day, it.value) }
    }


}