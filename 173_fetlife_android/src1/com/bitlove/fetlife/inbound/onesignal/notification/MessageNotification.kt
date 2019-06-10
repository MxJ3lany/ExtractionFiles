package com.bitlove.fetlife.inbound.onesignal.notification

import android.app.PendingIntent
import android.content.Context
import com.bitlove.fetlife.FetLifeApplication
import com.bitlove.fetlife.R
import com.bitlove.fetlife.event.NewMessageEvent
import com.bitlove.fetlife.inbound.onesignal.NotificationParser
import com.bitlove.fetlife.model.service.FetLifeApiIntentService
import com.bitlove.fetlife.view.screen.BaseActivity
import com.bitlove.fetlife.webapp.navigation.WebAppNavigation
import com.bitlove.fetlife.webapp.screen.FetLifeWebViewActivity
import com.crashlytics.android.Crashlytics
import org.json.JSONObject

class MessageNotification(notificationType: String, notificationIdRange: Int, title: String, message: String, launchUrl: String, mergeId: String?, collapseId: String?, additionalData: JSONObject, preferenceKey: String?) : OneSignalNotification(notificationType, notificationIdRange, title, message, launchUrl, mergeId, collapseId, additionalData, preferenceKey) {

    private var conversationId: String? = additionalData.optString(NotificationParser.JSON_FIELD_STRING_CONVERSATION_ID)
    private var nickname: String? = additionalData.optString(NotificationParser.JSON_FIELD_STRING_NICKNAME)

    override fun handle(fetLifeApplication: FetLifeApplication): Boolean {
        if (conversationId == null) {
            Crashlytics.logException(Exception("Missing conversation id"))
            return false
        }

        var conversationInForeground = false
        val appInForeground = fetLifeApplication.isAppInForeground

        if (appInForeground) {
            fetLifeApplication.eventBus.post(NewMessageEvent(conversationId))
            val foregroundActivity = fetLifeApplication.foregroundActivity
            if (foregroundActivity is FetLifeWebViewActivity) {
                conversationInForeground = launchUrl == (foregroundActivity as? FetLifeWebViewActivity)?.getCurrentUrl()
            }
            if (!conversationInForeground) {
                FetLifeApiIntentService.startApiCall(fetLifeApplication, FetLifeApiIntentService.ACTION_APICALL_NOTIFICATION_COUNTS)
            }
        }

        //TODO: display in app notification if the user is not on the same message screen
        return conversationInForeground
    }

    override fun getNotificationChannelName(context: Context): String? {
        return context.getString(R.string.settings_title_notification_messages_enabled)
    }

    override fun getNotificationChannelDescription(context: Context): String? {
        return context.getString(R.string.settings_summary_notification_messages_enabled)
    }

    override fun getSummaryTitle(notificationCount: Int, context: Context): String? {
        return context.resources.getQuantityString(R.plurals.noification_summary_title_messages_new_message, notificationCount, notificationCount)
    }

    override fun getSummaryText(notificationCount: Int, context: Context): String? {
        return context.getString(R.string.noification_summary_text_messages_new_message)
    }

    override fun getNotificationTitle(oneSignalNotification: OneSignalNotification, notificationCount: Int, context: Context): String? {
        return (oneSignalNotification as? MessageNotification)?.nickname
                ?: oneSignalNotification.title
    }

    override fun getNotificationText(oneSignalNotification: OneSignalNotification, notificationCount: Int, context: Context): String? {
        return context.resources.getQuantityString(R.plurals.noification_summary_text_messages_new_message, notificationCount, notificationCount)
    }

    override fun getNotificationIntent(oneSignalNotification: OneSignalNotification, context: Context, order: Int): PendingIntent? {
        if (launchUrl == null) {
            Crashlytics.logException(Exception("Launch url is null"))
            return null
        }
        val baseIntent = FetLifeWebViewActivity.createIntent(context, WebAppNavigation.WEBAPP_BASE_URL + "/inbox", true, R.id.navigation_bottom_inbox, false)
        val contentIntent = FetLifeWebViewActivity.createIntent(context, oneSignalNotification.launchUrl!!, false, null, false).apply {
            putExtra(BaseActivity.EXTRA_NOTIFICATION_SOURCE_TYPE, oneSignalNotification.notificationType)
            putExtra(BaseActivity.EXTRA_NOTIFICATION_MERGE_ID, oneSignalNotification.mergeId)
        }
        return PendingIntent.getActivities(context, order, arrayOf(baseIntent, contentIntent), PendingIntent.FLAG_CANCEL_CURRENT)
    }

    override fun getLegacySummaryIntent(context: Context): PendingIntent? {
        return PendingIntent.getActivity(context, notificationIdRange, FetLifeWebViewActivity.createIntent(context, WebAppNavigation.WEBAPP_BASE_URL + "/inbox", true, R.id.navigation_bottom_inbox, false), PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun saveNotificationItem(notificationId: Int) {}

}