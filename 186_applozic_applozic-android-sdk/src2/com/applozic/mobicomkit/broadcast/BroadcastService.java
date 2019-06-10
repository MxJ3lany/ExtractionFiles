package com.applozic.mobicomkit.broadcast;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.conversation.Message;

import com.applozic.mobicomkit.api.conversation.service.ConversationService;
import com.applozic.mobicomkit.api.notification.NotificationService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.listners.ApplozicUIListener;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;

/**
 * Created by devashish on 24/1/15.
 */
public class BroadcastService {

    private static final String TAG = "BroadcastService";
    private static final String MOBICOMKIT_ALL = "MOBICOMKIT_ALL";

    public static String currentUserId = null;
    public static Integer parentGroupKey = null;
    public static Integer currentConversationId = null;
    public static String currentInfoId = null;
    public static boolean videoCallAcitivityOpend = false;
    public static boolean callRinging = false;
    public static int lastIndexForChats = 0;
    private static boolean contextBasedChatEnabled = false;
    public static String currentUserProfileUserId = null;

    public static void selectMobiComKitAll() {
        currentUserId = MOBICOMKIT_ALL;
    }

    public static boolean isQuick() {
        return currentUserId != null && currentUserId.equals(MOBICOMKIT_ALL);
    }

    public static boolean isChannelInfo() {
        return currentInfoId != null;
    }

    public static boolean isIndividual() {
        return currentUserId != null && !isQuick();
    }

    public static synchronized boolean isContextBasedChatEnabled() {
        return contextBasedChatEnabled;
    }

    public static synchronized boolean setContextBasedChat(boolean contextBasedChat) {
        return contextBasedChatEnabled = contextBasedChat;
    }

    public static void sendLoadMoreBroadcast(Context context, boolean loadMore) {
        Utils.printLog(context, TAG, "Sending " + INTENT_ACTIONS.LOAD_MORE.toString() + " broadcast");
        Intent intent = new Intent();
        intent.setAction(INTENT_ACTIONS.LOAD_MORE.toString());
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("loadMore", loadMore);
        if (ApplozicService.getContext(context) != null && ApplozicService.getContext(context) instanceof ApplozicUIListener) {
            ((ApplozicUIListener) ApplozicService.getContext(context)).onLoadMore(loadMore);
        }
        sendBroadcast(context, intent);
    }

    public static void sendDeliveryReportForContactBroadcast(Context context, String action, String contactId) {
        Utils.printLog(context, TAG, "Sending message delivery report of contact broadcast for " + action + ", " + contactId);
        Intent intentUpdate = new Intent();
        intentUpdate.setAction(action);
        intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
        intentUpdate.putExtra(MobiComKitConstants.CONTACT_ID, contactId);
        if (ApplozicService.getContext(context) != null && ApplozicService.getContext(context) instanceof ApplozicUIListener) {
            if (INTENT_ACTIONS.MESSAGE_READ_AND_DELIVERED_FOR_CONTECT.toString().equals(action)) {
                ((ApplozicUIListener) ApplozicService.getContext(context)).onAllMessagesRead(contactId);
            } else if (INTENT_ACTIONS.MESSAGE_DELIVERY_FOR_CONTACT.toString().equals(action)) {
                ((ApplozicUIListener) ApplozicService.getContext(context)).onAllMessagesDelivered(contactId);
            }
        }
        sendBroadcast(context, intentUpdate);
    }

    public static void sendMessageUpdateBroadcast(Context context, String action, Message message) {
        if (message.isActionMessage() && INTENT_ACTIONS.SYNC_MESSAGE.toString().equals(action) && ApplozicClient.getInstance(context).isActionMessagesHidden()) {
            return;
        }

        Utils.printLog(context, TAG, "Sending message update broadcast for " + action + ", " + message.getKeyString());
        Intent intentUpdate = new Intent();
        intentUpdate.setAction(action);
        intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
        intentUpdate.putExtra(MobiComKitConstants.MESSAGE_JSON_INTENT, GsonUtils.getJsonFromObject(message, Message.class));
        if (ApplozicService.getContext(context) != null && ApplozicService.getContext(context) instanceof ApplozicUIListener) {
            if (!message.isSentToMany() && !message.isTypeOutbox()) {
                ((ApplozicUIListener) ApplozicService.getContext(context)).onMessageReceived(message);
            }

            if (INTENT_ACTIONS.MESSAGE_SYNC_ACK_FROM_SERVER.toString().equals(action)) {
                ((ApplozicUIListener) ApplozicService.getContext(context)).onMessageSent(message);
            } else if (INTENT_ACTIONS.SYNC_MESSAGE.toString().equals(action)) {
                ((ApplozicUIListener) ApplozicService.getContext(context)).onMessageSync(message, message.getKeyString());
            } else if (INTENT_ACTIONS.MESSAGE_DELIVERY.toString().equals(action) || INTENT_ACTIONS.MESSAGE_READ_AND_DELIVERED.toString().equals(action)) {
                ((ApplozicUIListener) ApplozicService.getContext(context)).onMessageDelivered(message, message.getContactIds());
            }
        }
        sendBroadcast(context, intentUpdate);
    }

    public static void sendMessageDeleteBroadcast(Context context, String action, String keyString, String contactNumbers) {
        Utils.printLog(context, TAG, "Sending message delete broadcast for " + action);
        Intent intentDelete = new Intent();
        intentDelete.setAction(action);
        intentDelete.putExtra("keyString", keyString);
        intentDelete.putExtra("contactNumbers", contactNumbers);
        intentDelete.addCategory(Intent.CATEGORY_DEFAULT);
        if (ApplozicService.getContext(context) != null && ApplozicService.getContext(context) instanceof ApplozicUIListener) {
            ((ApplozicUIListener) ApplozicService.getContext(context)).onMessageDeleted(keyString, contactNumbers);
        }
        sendBroadcast(context, intentDelete);
    }

    public static void sendConversationDeleteBroadcast(Context context, String action, String contactNumber, Integer channelKey, String response) {
        Utils.printLog(context, TAG, "Sending conversation delete broadcast for " + action);
        Intent intentDelete = new Intent();
        intentDelete.setAction(action);
        intentDelete.putExtra("channelKey", channelKey);
        intentDelete.putExtra("contactNumber", contactNumber);
        intentDelete.putExtra("response", response);
        intentDelete.addCategory(Intent.CATEGORY_DEFAULT);
        if (ApplozicService.getContext(context) != null && ApplozicService.getContext(context) instanceof ApplozicUIListener) {
            ((ApplozicUIListener) ApplozicService.getContext(context)).onConversationDeleted(contactNumber, channelKey, response);
        }
        sendBroadcast(context, intentDelete);
    }


    public static void sendNotificationBroadcast(Context context, Message message, int index) {
        if (message != null) {

            if (message.getMetadata() != null && message.getMetadata().containsKey("NO_ALERT") && "true".equals(message.getMetadata().get("NO_ALERT"))) {
                return;
            }

            int notificationId = Utils.getLauncherIcon(context.getApplicationContext());
            final NotificationService notificationService =
                    new NotificationService(notificationId, context, 0, 0, 0);

            if (MobiComUserPreference.getInstance(context).isLoggedIn()) {
                Channel channel = ChannelService.getInstance(context).getChannelInfo(message.getGroupId());
                Contact contact = null;
                if (message.getConversationId() != null) {
                    ConversationService.getInstance(context).getConversation(message.getConversationId());
                }
                if (message.getGroupId() == null) {
                    contact = new AppContactService(context).getContactById(message.getContactIds());
                }
                if (ApplozicClient.getInstance(context).isNotificationStacking()) {
                    notificationService.notifyUser(contact, channel, message, index);
                } else {
                    notificationService.notifyUserForNormalMessage(contact, channel, message, index);
                }
            }
        }
    }


    public static void sendUpdateLastSeenAtTimeBroadcast(Context context, String action, String contactId) {
        Utils.printLog(context, TAG, "Sending lastSeenAt broadcast....");
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("contactId", contactId);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        if (ApplozicService.getContext(context) != null && ApplozicService.getContext(context) instanceof ApplozicUIListener) {
            ((ApplozicUIListener) ApplozicService.getContext(context)).onUpdateLastSeen(contactId);
        }
        sendBroadcast(context, intent);
    }

    public static void sendUpdateTypingBroadcast(Context context, String action, String applicationId, String userId, String isTyping) {
        Utils.printLog(context, TAG, "Sending typing Broadcast.......");
        Intent intentTyping = new Intent();
        intentTyping.setAction(action);
        intentTyping.putExtra("applicationId", applicationId);
        intentTyping.putExtra("userId", userId);
        intentTyping.putExtra("isTyping", isTyping);
        intentTyping.addCategory(Intent.CATEGORY_DEFAULT);
        if (ApplozicService.getContext(context) != null && ApplozicService.getContext(context) instanceof ApplozicUIListener) {
            ((ApplozicUIListener) ApplozicService.getContext(context)).onUpdateTypingStatus(userId, isTyping);
        }
        sendBroadcast(context, intentTyping);
    }


    public static void sendUpdate(Context context, boolean isMetadataUpdate, final String action) {
        Utils.printLog(context, TAG, action);
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("isMetadataUpdate", isMetadataUpdate);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        if (ApplozicService.getContext(context) != null && ApplozicService.getContext(context) instanceof ApplozicUIListener) {
            if (INTENT_ACTIONS.MQTT_CONNECTED.toString().equals(action)) {
                ((ApplozicUIListener) ApplozicService.getContext(context)).onMqttConnected();
            } else if (INTENT_ACTIONS.MQTT_DISCONNECTED.toString().equals(action)) {
                ((ApplozicUIListener) ApplozicService.getContext(context)).onMqttDisconnected();
            } else if (INTENT_ACTIONS.USER_ONLINE.toString().equals(action)) {
                ((ApplozicUIListener) ApplozicService.getContext(context)).onUserOnline();
            } else if (INTENT_ACTIONS.USER_OFFLINE.toString().equals(action)) {
                ((ApplozicUIListener) ApplozicService.getContext(context)).onUserOffline();
            } else if (INTENT_ACTIONS.CHANNEL_SYNC.toString().equals(action)) {
                ((ApplozicUIListener) ApplozicService.getContext(context)).onChannelUpdated();
            }
        }
        sendBroadcast(context, intent);
    }

    public static void sendUpdate(Context context, String action) {
        sendUpdate(context, false, action);
    }

    public static void updateMessageMetadata(Context context, String messageKey, String action) {
        Utils.printLog(context, TAG, "Sending Message Metadata Update Broadcast for message key : " + messageKey);
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("keyString", messageKey);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        if (ApplozicService.getContext(context) != null && ApplozicService.getContext(context) instanceof ApplozicUIListener) {
            ((ApplozicUIListener) ApplozicService.getContext(context)).onMessageMetadataUpdated(messageKey);
        }
        sendBroadcast(context, intent);
    }


    public static void sendConversationReadBroadcast(Context context, String action, String currentId, boolean isGroup) {
        Utils.printLog(context, TAG, "Sending  Broadcast for conversation read ......");
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("currentId", currentId);
        intent.putExtra("isGroup", isGroup);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        if (ApplozicService.getContext(context) != null && ApplozicService.getContext(context) instanceof ApplozicUIListener) {
            ((ApplozicUIListener) ApplozicService.getContext(context)).onConversationRead(currentId, isGroup);
        }
        sendBroadcast(context, intent);
    }

    public static void sendMuteUserBroadcast(Context context, String action, boolean mute, String userId) {
        Utils.printLog(context, TAG, "Sending Mute user Broadcast for user : " + userId + ", mute : " + mute);
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("mute", mute);
        intent.putExtra("userId", userId);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        if (ApplozicService.getContext(context) != null && ApplozicService.getContext(context) instanceof ApplozicUIListener) {
            ((ApplozicUIListener) ApplozicService.getContext(context)).onUserMute(mute, userId);
        }
        sendBroadcast(context, intent);
    }

    public static void sendUpdateUserDetailBroadcast(Context context, String action, String contactId) {
        Utils.printLog(context, TAG, "Sending profileImage update....");
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("contactId", contactId);
        if (ApplozicService.getContext(context) != null && ApplozicService.getContext(context) instanceof ApplozicUIListener) {
            ((ApplozicUIListener) ApplozicService.getContext(context)).onUserDetailUpdated(contactId);
        }
        sendBroadcast(context, intent);
    }

    public static void sendUpdateGroupInfoBroadcast(Context context, String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        sendBroadcast(context, intent);
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_ACTIONS.FIRST_TIME_SYNC_COMPLETE.toString());
        intentFilter.addAction(INTENT_ACTIONS.LOAD_MORE.toString());
        intentFilter.addAction(INTENT_ACTIONS.MESSAGE_SYNC_ACK_FROM_SERVER.toString());
        intentFilter.addAction(INTENT_ACTIONS.SYNC_MESSAGE.toString());
        intentFilter.addAction(INTENT_ACTIONS.DELETE_MESSAGE.toString());
        intentFilter.addAction(INTENT_ACTIONS.DELETE_CONVERSATION.toString());
        intentFilter.addAction(INTENT_ACTIONS.MESSAGE_DELIVERY.toString());
        intentFilter.addAction(INTENT_ACTIONS.MESSAGE_DELIVERY_FOR_CONTACT.toString());
        intentFilter.addAction(INTENT_ACTIONS.UPLOAD_ATTACHMENT_FAILED.toString());
        intentFilter.addAction(INTENT_ACTIONS.MESSAGE_ATTACHMENT_DOWNLOAD_DONE.toString());
        intentFilter.addAction(INTENT_ACTIONS.INSTRUCTION.toString());
        intentFilter.addAction(INTENT_ACTIONS.MESSAGE_ATTACHMENT_DOWNLOAD_FAILD.toString());
        intentFilter.addAction(INTENT_ACTIONS.UPDATE_LAST_SEEN_AT_TIME.toString());
        intentFilter.addAction(INTENT_ACTIONS.UPDATE_TYPING_STATUS.toString());
        intentFilter.addAction(INTENT_ACTIONS.MQTT_DISCONNECTED.toString());
        intentFilter.addAction(INTENT_ACTIONS.UPDATE_CHANNEL_NAME.toString());
        intentFilter.addAction(INTENT_ACTIONS.MESSAGE_READ_AND_DELIVERED.toString());
        intentFilter.addAction(INTENT_ACTIONS.MESSAGE_READ_AND_DELIVERED_FOR_CONTECT.toString());
        intentFilter.addAction(INTENT_ACTIONS.CHANNEL_SYNC.toString());
        intentFilter.addAction(INTENT_ACTIONS.UPDATE_TITLE_SUBTITLE.toString());
        intentFilter.addAction(INTENT_ACTIONS.CONVERSATION_READ.toString());
        intentFilter.addAction(INTENT_ACTIONS.UPDATE_USER_DETAIL.toString());
        intentFilter.addAction(INTENT_ACTIONS.MESSAGE_METADATA_UPDATE.toString());
        intentFilter.addAction(INTENT_ACTIONS.MUTE_USER_CHAT.toString());
        intentFilter.addAction(INTENT_ACTIONS.MQTT_CONNECTED.toString());
        intentFilter.addAction(INTENT_ACTIONS.USER_ONLINE.toString());
        intentFilter.addAction(INTENT_ACTIONS.USER_OFFLINE.toString());
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        return intentFilter;
    }

    public static void sendBroadcast(Context context, Intent intent) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public enum INTENT_ACTIONS {
        LOAD_MORE, FIRST_TIME_SYNC_COMPLETE, MESSAGE_SYNC_ACK_FROM_SERVER,
        SYNC_MESSAGE, DELETE_MESSAGE, DELETE_CONVERSATION, MESSAGE_DELIVERY, MESSAGE_DELIVERY_FOR_CONTACT, INSTRUCTION, UPDATE_GROUP_INFO,
        UPLOAD_ATTACHMENT_FAILED, MESSAGE_ATTACHMENT_DOWNLOAD_DONE, MESSAGE_ATTACHMENT_DOWNLOAD_FAILD,
        UPDATE_LAST_SEEN_AT_TIME, UPDATE_TYPING_STATUS, MESSAGE_READ_AND_DELIVERED, MESSAGE_READ_AND_DELIVERED_FOR_CONTECT, CHANNEL_SYNC,
        CONTACT_VERIFIED, NOTIFY_USER, MQTT_DISCONNECTED, UPDATE_CHANNEL_NAME, UPDATE_TITLE_SUBTITLE, CONVERSATION_READ, UPDATE_USER_DETAIL, MESSAGE_METADATA_UPDATE, MUTE_USER_CHAT, MQTT_CONNECTED, USER_ONLINE, USER_OFFLINE
    }
}
