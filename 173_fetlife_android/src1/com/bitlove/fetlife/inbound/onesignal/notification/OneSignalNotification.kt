package com.bitlove.fetlife.inbound.onesignal.notification

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bitlove.fetlife.FetLifeApplication
import com.bitlove.fetlife.R
import com.bitlove.fetlife.model.pojos.fetlife.db.NotificationHistoryItem
import com.bitlove.fetlife.util.AppUtil
import com.bitlove.fetlife.util.NotificationUtil
import com.bitlove.fetlife.view.screen.BaseActivity
import com.bitlove.fetlife.webapp.navigation.WebAppNavigation
import com.bitlove.fetlife.webapp.screen.FetLifeWebViewActivity
import org.json.JSONObject
import java.util.concurrent.ArrayBlockingQueue

//TODO anonym, delete intent, inner launches, move hardcoded urls
abstract class OneSignalNotification(val notificationType: String,
                                     val notificationIdRange: Int,
                                     val title: String?,
                                     val message: String?,
                                     val launchUrl: String?,
                                     val mergeId: String?,
                                     val collapseId: String?,
                                     val additionalData: JSONObject?,
                                     private val preferenceKey: String?) {

    open fun getNotificationChannelId(): String = notificationType
    open fun getNotificationChannelName(context: Context): String? = null
    open fun getNotificationChannelDescription(context: Context): String? = null
    @TargetApi(Build.VERSION_CODES.N)
    open fun getNotificationChannelImportance(): Int = NotificationManager.IMPORTANCE_DEFAULT

    open fun getSummaryTitle(notificationCount: Int, context: Context): String? = null
    open fun getSummaryText(notificationCount: Int, context: Context): String? = null

    open fun getNotificationTitle(oneSignalNotification: OneSignalNotification, notificationCount: Int, context: Context): String? = oneSignalNotification.title
    open fun getNotificationText(oneSignalNotification: OneSignalNotification, notificationCount: Int, context: Context): String? = oneSignalNotification.message
    open fun getNotificationIntent(oneSignalNotification: OneSignalNotification, context: Context, order: Int): PendingIntent? = null
    open fun getWebViewIntent(fetLifeApplication: FetLifeApplication, oneSignalNotification: OneSignalNotification, order: Int): PendingIntent? {
        return if (fetLifeApplication.webAppNavigation.openFromNotification(oneSignalNotification.launchUrl)) {
            val baseIntent = FetLifeWebViewActivity.createIntent(fetLifeApplication, WebAppNavigation.WEBAPP_BASE_URL + "/inbox", true, R.id.navigation_bottom_inbox, false)
            val contentIntent = FetLifeWebViewActivity.createIntent(fetLifeApplication, oneSignalNotification.launchUrl!!, false, null, false).apply {
                putExtra(BaseActivity.EXTRA_NOTIFICATION_SOURCE_TYPE, oneSignalNotification.notificationType)
                putExtra(BaseActivity.EXTRA_NOTIFICATION_MERGE_ID, oneSignalNotification.mergeId)
            }
            PendingIntent.getActivities(fetLifeApplication, order, arrayOf(baseIntent, contentIntent), PendingIntent.FLAG_CANCEL_CURRENT)
        } else {
            null
        }
    }

    open fun getLegacySummaryIntent(context: Context): PendingIntent? = getNotificationIntent(this, context, notificationIdRange)

    open fun getNotificationItemLaunchUrl(): String? = launchUrl

    open fun handle(fetLifeApplication: FetLifeApplication): Boolean = false

    open fun display(fetLifeApplication: FetLifeApplication) {

        val groupingEnabled = fetLifeApplication.userSessionManager.activeUserPreferences.getBoolean(fetLifeApplication.getString(R.string.settings_key_notification_grouping_enabled), true)

        //Selecting storage
        val liveNotifications = liveNotificationMap[notificationType]
                ?: ArrayList(LIMIT_UNREAD_NOTIFICATION_COUNT)
        if (liveNotifications.isEmpty()) liveNotificationMap[notificationType] = liveNotifications

        //Create notification channel
        val channelId = getNotificationChannelId()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = getNotificationChannelName(fetLifeApplication)
            val channelDescription = getNotificationChannelDescription(fetLifeApplication)
            val channelImportance = getNotificationChannelImportance()

            val channel = NotificationChannel(channelId, channelName, channelImportance).apply { if (channelDescription != null) this.description = channelDescription }
            fetLifeApplication.getSystemService(NotificationManager::class.java)!!.createNotificationChannel(channel)
        }

        addToLiveNotifications(liveNotifications, fetLifeApplication)

        //Sending notifications
        val notificationManager = NotificationManagerCompat.from(fetLifeApplication)

        val summaryTitle = getSummaryTitle(liveNotifications.size, fetLifeApplication)
        val summaryText = getSummaryText(liveNotifications.size, fetLifeApplication)

        val inboxStyle = NotificationCompat.InboxStyle()
                .setBigContentTitle(summaryTitle)
                .setSummaryText(summaryText)

        var i = notificationIdRange + 1
        for (oneSignalNotificationEntry in liveNotifications.reversed().groupBy { it.mergeId }) {
            val groupCount = oneSignalNotificationEntry.value.size
            val referenceNotification = oneSignalNotificationEntry.value.first()
            val title = getNotificationTitle(referenceNotification, groupCount, fetLifeApplication)
            val text = getNotificationText(referenceNotification, groupCount, fetLifeApplication)
            val pendingIntent = getWebViewIntent(fetLifeApplication, referenceNotification, i)
                    ?: getNotificationIntent(referenceNotification, fetLifeApplication, i)

            val groupId = if (groupingEnabled) notificationType else null
            val groupedNotification = getDefaultNotificationBuilder(channelId, groupId, fetLifeApplication, pendingIntent, title, text).build()
            notificationManager.notify(i++, groupedNotification)
            inboxStyle.addLine("$title $text")
            if (i >+ notificationIdRange + 1 + LIMIT_UNREAD_NOTIFICATION_COUNT) {
                break
            }
        }

        val summaryNotification = getDefaultNotificationBuilder(channelId, notificationType, fetLifeApplication, null, summaryTitle, summaryText).apply {
            setGroupSummary(true)
            setStyle(inboxStyle)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                setContentIntent(getLegacySummaryIntent(fetLifeApplication))
            }
        }.build()
        if (groupingEnabled) {
            notificationManager.notify(notificationIdRange, summaryNotification)
        }

        //Saving notification item
        saveNotificationItem(notificationIdRange)
    }

    open fun addToLiveNotifications(liveNotifications: MutableList<OneSignalNotification>, fetLifeApplication: FetLifeApplication) {
        //Collapsing notification with same collapseId
        if (collapseId != null) {
            val collapseIndex = liveNotifications.withIndex().firstOrNull { it.value.collapseId == collapseId }?.index
            if (collapseIndex != null) NotificationUtil.cancelNotification(fetLifeApplication, notificationIdRange + collapseIndex + 1)
        }
        liveNotifications.add(this)
    }

    open fun isEnabled(fetLifeApplication: FetLifeApplication): Boolean {
        if (preferenceKey == null) return false
        return fetLifeApplication.userSessionManager.activeUserPreferences.getBoolean(preferenceKey, true)
    }


    private fun getDefaultNotificationBuilder(notificationChannelId: String, groupId: String?, fetLifeApplication: FetLifeApplication, contentIntent: PendingIntent?, title: String?, text: String?): NotificationCompat.Builder {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) NotificationCompat.Builder(fetLifeApplication, notificationChannelId) else NotificationCompat.Builder(fetLifeApplication)
        return builder.apply {
            setAutoCancel(true)
            setContentIntent(contentIntent)

            setContentTitle(title)
            setContentText(text)

            setGroup(groupId)
            setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)

            setLargeIcon(BitmapFactory.decodeResource(fetLifeApplication.resources, R.mipmap.app_icon_kinky))
            setSmallIcon(R.drawable.ic_stat_onesignal_default)
            setLights(fetLifeApplication.userSessionManager.notificationColor, 1000, 1000)
            setSound(fetLifeApplication.userSessionManager.notificationRingtone)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setChannelId(notificationChannelId)
            }

            if (AppUtil.useAnonymNotifications(FetLifeApplication.getInstance())) {
                setVisibility(NotificationCompat.VISIBILITY_SECRET)
            } else {
                setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            }

            val vibrationSetting = fetLifeApplication.userSessionManager.notificationVibration
            if (vibrationSetting != null) {
                setVibrate(vibrationSetting)
            } else {
                setDefaults(Notification.DEFAULT_VIBRATE)
            }
        }
    }

    open fun saveNotificationItem(notificationId: Int) {
        NotificationHistoryItem().apply {
            this.displayId = notificationId
            this.displayHeader = this@OneSignalNotification.title
            this.displayMessage = this@OneSignalNotification.message
            this.launchUrl = getNotificationItemLaunchUrl()
            this.collapseId = this@OneSignalNotification.collapseId
            this.timeStamp = try {
                (this@OneSignalNotification.additionalData!!.getDouble("sent_at") * 1000).toLong()
            } catch (exception: Throwable) {
                -1
            }
        }.save()
    }

    companion object {
        const val LIMIT_UNREAD_NOTIFICATION_COUNT = 5
        const val NOTIFICATION_CHANNEL_DEFUALT = "NOTIFICATION_CHANNEL_DEFUALT"

        //Launch target string parts
        const val LAUNCH_URL_PARAM_SEPARATOR = ":"
        const val LAUNCH_URL_PREFIX = "FetLifeApp://"

        val liveNotificationMap: MutableMap<String, MutableList<OneSignalNotification>> = HashMap()

        fun clearNotifications(notificationType: String? = null, mergeId: String? = null) {
            if (notificationType == null) {
                liveNotificationMap.clear()
            }
            val liveNotifications = liveNotificationMap[notificationType] ?: return
            if (mergeId == null) {
                liveNotificationMap.remove(notificationType)
            } else {
                liveNotifications.removeAll { it.mergeId == mergeId }
            }
        }
    }

}