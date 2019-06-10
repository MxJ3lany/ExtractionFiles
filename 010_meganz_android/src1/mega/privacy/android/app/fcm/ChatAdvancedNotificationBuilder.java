package mega.privacy.android.app.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.R;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.controllers.ChatController;
import mega.privacy.android.app.lollipop.megachat.ChatItemPreferences;
import mega.privacy.android.app.lollipop.megachat.ChatSettings;
import mega.privacy.android.app.lollipop.megachat.calls.CallNotificationIntentService;
import mega.privacy.android.app.lollipop.megachat.calls.ChatCallActivity;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatCall;
import nz.mega.sdk.MegaChatListItem;
import nz.mega.sdk.MegaChatMessage;
import nz.mega.sdk.MegaChatRequest;
import nz.mega.sdk.MegaChatRoom;
import nz.mega.sdk.MegaHandleList;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaNodeList;

public final class ChatAdvancedNotificationBuilder {

    private static final String GROUP_KEY = "Karere";

    private final Context context;
    private NotificationManager notificationManager;
    DatabaseHandler dbH;
    MegaApiAndroid megaApi;
    MegaChatApiAndroid megaChatApi;

    private NotificationCompat.Builder mBuilderCompat;

    private String notificationChannelIdChatSimple = Constants.NOTIFICATION_CHANNEL_CHAT_ID;
    private String notificationChannelNameChatSimple = Constants.NOTIFICATION_CHANNEL_CHAT_NAME;
    private String notificationChannelIdChatSummary = Constants.NOTIFICATION_CHANNEL_CHAT_SUMMARY_ID;
    private String notificationChannelNameChatSummary = Constants.NOTIFICATION_CHANNEL_CHAT_SUMMARY_NAME;
    private String notificationChannelIdChatSummaryNoVibrate = Constants.NOTIFICATION_CHANNEL_CHAT_SUMMARY_NO_VIBRATE_ID;
    private String notificationChannelNameChatSummaryNoVibrate = Constants.NOTIFICATION_CHANNEL_CHAT_SUMMARY_NO_VIBRATE_NAME;
    private String notificationChannelIdInProgressMissedCall = Constants.NOTIFICATION_CHANNEL_INPROGRESS_MISSED_CALLS_ID;
    private String notificationChannelNameInProgressMissedCall = Constants.NOTIFICATION_CHANNEL_INPROGRESS_MISSED_CALLS_NAME;
    private String notificationChannelIdIncomingCall = Constants.NOTIFICATION_CHANNEL_INCOMING_CALLS_ID;
    private String notificationChannelNameIncomingCall = Constants.NOTIFICATION_CHANNEL_INCOMING_CALLS_NAME;

    public static ChatAdvancedNotificationBuilder newInstance(Context context, MegaApiAndroid megaApi, MegaChatApiAndroid megaChatApi) {
        Context appContext = context.getApplicationContext();
        Context safeContext = ContextCompat.createDeviceProtectedStorageContext(appContext);
        if (safeContext == null) {
            safeContext = appContext;
        }
        NotificationManager notificationManager = (NotificationManager) safeContext.getSystemService(Context.NOTIFICATION_SERVICE);

        return new ChatAdvancedNotificationBuilder(safeContext, notificationManager, megaApi, megaChatApi);
    }

    public ChatAdvancedNotificationBuilder(Context context, NotificationManager notificationManager, MegaApiAndroid megaApi, MegaChatApiAndroid megaChatApi) {
        this.context = context.getApplicationContext();
        this.notificationManager = notificationManager;

        dbH = DatabaseHandler.getDbHandler(context);
        this.megaApi = megaApi;
        this.megaChatApi = megaChatApi;
    }

    public void sendBundledNotification(Uri uriParameter, String vibration, long chatId, MegaHandleList unreadHandleList) {
        log("sendBundledNotification");
        MegaChatRoom chat = megaChatApi.getChatRoom(chatId);

        ArrayList<MegaChatMessage> unreadMessages = new ArrayList<>();
        for(int i=0;i<unreadHandleList.size();i++){
            MegaChatMessage message = megaChatApi.getMessage(chatId, unreadHandleList.get(i));
            log("Chat: "+chat.getTitle()+" messagID: "+unreadHandleList.get(i));
            if(message!=null){
                unreadMessages.add(message);
            }
            else{
                log("sendBundledNotification: Message cannot be recovered");
            }
        }

        Collections.sort(unreadMessages, new Comparator<MegaChatMessage>() {
            public int compare(MegaChatMessage c1, MegaChatMessage c2) {
                long timestamp1 = c1.getTimestamp();
                long timestamp2 = c2.getTimestamp();

                long result = timestamp2 - timestamp1;
                return (int) result;
            }
        });

        Notification notification = buildNotification(uriParameter, vibration, GROUP_KEY, chat, unreadMessages);

        String chatString = MegaApiJava.userHandleToBase64(chat.getChatId());

        int notificationId = chatString.hashCode();
        notificationManager.notify(notificationId, notification);
    }

    public void buildNotificationPreN(Uri uriParameter, String vibration, MegaChatRequest request){
        log("buildNotificationPreN");

        MegaHandleList chatHandleList = request.getMegaHandleList();

        ArrayList<MegaChatListItem> chats = new ArrayList<>();
        for(int i=0; i<chatHandleList.size(); i++){
            MegaChatListItem chat = megaChatApi.getChatListItem(chatHandleList.get(i));
            if(chat!=null){
                chats.add(chat);
            }
            else{
                log("ERROR:chatNotRecovered:NULL");
            }
        }

        PendingIntent pendingIntent = null;

        if(chats.size()>1){
            Intent intent = new Intent(context, ManagerActivityLollipop.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setAction(Constants.ACTION_CHAT_SUMMARY);
            intent.putExtra("CHAT_ID", -1);
            pendingIntent = PendingIntent.getActivity(context, (int)chats.get(0).getChatId() , intent, PendingIntent.FLAG_ONE_SHOT);

            //Order by last interaction
            Collections.sort(chats, new Comparator<MegaChatListItem> (){

                public int compare(MegaChatListItem c1, MegaChatListItem c2) {
                    long timestamp1 = c1.getLastTimestamp();
                    long timestamp2 = c2.getLastTimestamp();

                    long result = timestamp2 - timestamp1;
                    return (int)result;
                }
            });
        }
        else if (chats.size()==1){
            Intent intent = new Intent(context, ManagerActivityLollipop.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setAction(Constants.ACTION_CHAT_NOTIFICATION_MESSAGE);
            intent.putExtra("CHAT_ID", chats.get(0).getChatId());
            pendingIntent = PendingIntent.getActivity(context, (int)chats.get(0).getChatId() , intent, PendingIntent.FLAG_ONE_SHOT);
        }
        else {
            log("ERROR:chatSIZE=0:return");
            return;
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            notificationBuilder.setColor(ContextCompat.getColor(context,R.color.mega));
        }

        notificationBuilder.setShowWhen(true);

        if(uriParameter!=null){
            notificationBuilder.setSound(uriParameter);
        }

        if(vibration!=null){
            if(vibration.equals("true")){
                notificationBuilder.setVibrate(new long[] {0, 500});
            }
        }

        notificationBuilder.setStyle(inboxStyle);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1){
            //API 25 = Android 7.1
            notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        }
        else{
            notificationBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        }

//        notificationBuilder.setFullScreenIntent(pendingIntent, true);

        for(int i=0; i<chats.size(); i++){
            if(MegaApplication.getOpenChatId() != chats.get(i).getChatId()){
                MegaHandleList handleListUnread = request.getMegaHandleListByChat(chats.get(i).getChatId());

                for(int j=0;j<handleListUnread.size();j++){
                    log("Get message id: "+handleListUnread.get(j)+" from chatId: "+chats.get(i).getChatId());
                    MegaChatMessage message = megaChatApi.getMessage(chats.get(i).getChatId(), handleListUnread.get(j));
                    if(message!=null){

                        String messageContent = "";

                        if(message.getType()==MegaChatMessage.TYPE_NODE_ATTACHMENT){

                            MegaNodeList nodeList = message.getMegaNodeList();
                            if(nodeList != null) {
                                if (nodeList.size() == 1) {
                                    MegaNode node = nodeList.get(0);
                                    log("Node Name: " + node.getName());
                                    messageContent = node.getName();
                                }
                            }
                        }
                        else if(message.getType()==MegaChatMessage.TYPE_CONTACT_ATTACHMENT){

                            long userCount  = message.getUsersCount();

                            if(userCount==1) {
                                String name = "";
                                name = message.getUserName(0);
                                if (name.trim().isEmpty()) {
                                    name = message.getUserName(0);
                                }
                                String email = message.getUserName(0);
                                log("Contact Name: " + name);
                                messageContent = email;
                            }
                            else{
                                StringBuilder name = new StringBuilder("");
                                name.append(message.getUserName(0));
                                for (int k = 1; k < userCount; k++) {
                                    name.append(", " + message.getUserName(k));
                                }
                                messageContent = name.toString();
                            }
                        }
                        else if(message.getType()==MegaChatMessage.TYPE_TRUNCATE){
                            log("Type TRUNCATE message");
                            messageContent = context.getString(R.string.history_cleared_message);
                        }
                        else{
                            messageContent = message.getContent();
                        }

                        CharSequence cs = " ";
                        String title = chats.get(i).getTitle();
                        if(chats.get(i).isGroup()){
                            long lastMsgSender = message.getUserHandle();

                            MegaChatRoom chatRoom = megaChatApi.getChatRoom(chats.get(i).getChatId());
                            String nameAction = chatRoom.getPeerFirstnameByHandle(lastMsgSender);
                            if(nameAction==null){
                                nameAction = "";
                            }

                            if(nameAction.trim().length()<=0){
                                ChatController cC = new ChatController(context);
                                nameAction = cC.getFirstName(lastMsgSender, chatRoom);
                            }

                            cs = nameAction + " @ " + title + ": " + messageContent;
                        }
                        else{
                            cs = title +": " + messageContent;
                        }

                        inboxStyle.addLine(cs);
                    }
                    else{
                        log("Message NULL cannot be recovered");
                        break;
                    }
                }
            }
            else{
                log("Do not show notification - opened chat");
            }
        }

        String textToShow = context.getResources().getQuantityString(R.plurals.plural_number_messages_chat_notification, (int)chats.size(), chats.size());

        notificationBuilder.setContentTitle("MEGA");
        notificationBuilder.setContentText(textToShow);
        inboxStyle.setSummaryText(textToShow);

        Notification notif = notificationBuilder.build();

        if(notif!=null){
            notificationManager.notify(Constants.NOTIFICATION_SUMMARY_CHAT, notif);
        }
        else{
            notificationManager.cancel(Constants.NOTIFICATION_SUMMARY_CHAT);
        }
    }

    public Notification buildNotification(Uri uriParameter, String vibration, String groupKey, MegaChatRoom chat, ArrayList<MegaChatMessage> unreadMessageList) {
        log("buildChatNotification");
        Intent intent = new Intent(context, ManagerActivityLollipop.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(Constants.ACTION_CHAT_NOTIFICATION_MESSAGE);
        intent.putExtra("CHAT_ID", chat.getChatId());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int)chat.getChatId() , intent, PendingIntent.FLAG_ONE_SHOT);

        String title;
        int unreadMessages = chat.getUnreadCount();
        log("Unread messages: "+unreadMessages+"  chatID: "+chat.getChatId());
        if(unreadMessages!=0){

            if(unreadMessages<0){
                unreadMessages = Math.abs(unreadMessages);
                log("unread number: "+unreadMessages);

                if(unreadMessages>1){
                    String numberString = "+"+unreadMessages;
                    title = chat.getTitle() + " (" + numberString + " " + context.getString(R.string.messages_chat_notification) + ")";
                }
                else{
                    title = chat.getTitle();
                }
            }
            else{

                if(unreadMessages>1){
                    String numberString = unreadMessages+"";
                    title = chat.getTitle() + " (" + numberString + " " + context.getString(R.string.messages_chat_notification) + ")";
                }
                else{
                    title = chat.getTitle();
                }
            }
        }
        else{
            title = chat.getTitle();
        }

//        Spanned notificationContent;

        NotificationCompat.Builder notificationBuilderO = null;
        Notification.Builder notificationBuilder = null;
        Notification.MessagingStyle messagingStyleContent = null;
        NotificationCompat.MessagingStyle messagingStyleContentO = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(notificationChannelIdChatSimple, notificationChannelNameChatSimple, NotificationManager.IMPORTANCE_LOW);
            channel.setShowBadge(true);
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{ 0 });
            if (notificationManager != null){
                notificationManager.createNotificationChannel(channel);
            }

            notificationBuilderO = new NotificationCompat.Builder(context, notificationChannelIdChatSimple);
            notificationBuilderO
                    .setSmallIcon(R.drawable.ic_stat_notify)
                    .setAutoCancel(true)
                    .setShowWhen(true)
                    .setGroup(groupKey)
                    .setColor(ContextCompat.getColor(context, R.color.mega));

            messagingStyleContentO = new NotificationCompat.MessagingStyle(chat.getTitle());
        }
        else {
            notificationBuilder = new Notification.Builder(context)
                    .setSmallIcon(R.drawable.ic_stat_notify)
                    .setAutoCancel(true)
                    .setShowWhen(true)
                    .setGroup(groupKey);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notificationBuilder.setColor(ContextCompat.getColor(context, R.color.mega));
            }

            messagingStyleContent = new Notification.MessagingStyle(chat.getTitle());
        }

        int sizeFor= (int) unreadMessageList.size()-1;
        for(int i=sizeFor; i>=0;i--){
            MegaChatMessage msg = unreadMessageList.get(i);
            log("getMessage: chatID: "+chat.getChatId()+" "+unreadMessageList.get(i));
            String messageContent = "";

            if(msg!=null){
                if(msg.getType()==MegaChatMessage.TYPE_NODE_ATTACHMENT){

                    MegaNodeList nodeList = msg.getMegaNodeList();
                    if(nodeList != null) {
                        if (nodeList.size() == 1) {
                            MegaNode node = nodeList.get(0);
                            log("Node Name: " + node.getName());
                            messageContent = node.getName();
                        }
                    }
                }
                else if(msg.getType()==MegaChatMessage.TYPE_CONTACT_ATTACHMENT){

                    long userCount  = msg.getUsersCount();

                    if(userCount==1) {
                        String name = "";
                        name = msg.getUserName(0);
                        if (name.trim().isEmpty()) {
                            name = msg.getUserName(0);
                        }
                        String email = msg.getUserName(0);
                        log("Contact EMail: " + name);
                        messageContent = email;
                    }
                    else{
                        StringBuilder name = new StringBuilder("");
                        name.append(msg.getUserName(0));
                        for (int j = 1; j < userCount; j++) {
                            name.append(", " + msg.getUserName(j));
                        }
                        messageContent = name.toString();
                    }
                }
                else if(msg.getType()==MegaChatMessage.TYPE_TRUNCATE){
                    log("Type TRUNCATE message");
                    messageContent = context.getString(R.string.history_cleared_message);
                }
                else{
                    messageContent = msg.getContent();
                }

                String sender = chat.getPeerFirstnameByHandle(msg.getUserHandle());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    messagingStyleContentO.addMessage(messageContent, msg.getTimestamp(), sender);
                }
                else{
                    messagingStyleContent.addMessage(messageContent, msg.getTimestamp(), sender);
                }
            }
            else{
                log("ERROR:buildIPCNotification:messageNULL");
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            messagingStyleContentO.setConversationTitle(title);

            notificationBuilderO.setStyle(messagingStyleContentO)
                    .setContentIntent(pendingIntent);
        }
        else {
            messagingStyleContent.setConversationTitle(title);

            notificationBuilder.setStyle(messagingStyleContent)
                    .setContentIntent(pendingIntent);
        }

        //Set when on notification
        int size = (int) unreadMessageList.size();

        MegaChatMessage lastMsg = unreadMessageList.get(0);
        if(lastMsg!=null){
            log("Last message: "+lastMsg.getContent()+" "+lastMsg.getTimestamp());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationBuilderO.setWhen(lastMsg.getTimestamp()*1000);
            }
            else{
                notificationBuilder.setWhen(lastMsg.getTimestamp()*1000);
            }
        }

        if(uriParameter!=null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationBuilderO.setSound(uriParameter);
            }
            else{
                notificationBuilder.setSound(uriParameter);
            }
        }

        if(vibration!=null){
            if(vibration.equals("true")){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    notificationBuilderO.setVibrate(new long[] {0, 500});
                }
                else{
                    notificationBuilder.setVibrate(new long[] {0, 500});
                }
            }
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1){
            //API 25 = Android 7.1
            notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        }
        else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                notificationBuilderO.setPriority(NotificationManager.IMPORTANCE_HIGH);
            }
            else{
                notificationBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH);
            }
        }

//        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();

//        if(chat.isGroup()){
//
//            if(msgUserHandle!=-1){
//                String nameAction = getParticipantShortName(msgUserHandle);
//
//                if(nameAction.isEmpty()){
//                    notificationBuilder.setContentText(msgContent);
//                    bigTextStyle.bigText(msgContent);
//                }
//                else{
//                    String source = "<b>"+nameAction+": </b>"+msgContent;
//
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        notificationContent = Html.fromHtml(source,Html.FROM_HTML_MODE_LEGACY);
//                    } else {
//                        notificationContent = Html.fromHtml(source);
//                    }
//                    notificationBuilder.setContentText(notificationContent);
//                    bigTextStyle.bigText(notificationContent);
//                }
//            }
//            else{
//                notificationBuilder.setContentText(msgContent);
//                bigTextStyle.bigText(msgContent);
//            }
//
//        }
//        else{
//            notificationBuilder.setContentText(msgContent);
//            bigTextStyle.bigText(msgContent);
//        }

        Bitmap largeIcon = setUserAvatar(chat);
        if(largeIcon!=null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationBuilderO.setLargeIcon(largeIcon);
            }
            else{
                notificationBuilder.setLargeIcon(largeIcon);
            }
        }

//        notificationBuilder.setStyle(bigTextStyle);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return notificationBuilderO.build();
        }
        else{
            return notificationBuilder.build();
        }
    }

    public Bitmap setUserAvatar(MegaChatRoom chat){
        log("setUserAvatar");

        if(chat.isGroup()){
            return createDefaultAvatar(chat);
        }
        else{

            String contactMail = chat.getPeerEmail(0);

            File avatar = null;
            if (context.getExternalCacheDir() != null){
                avatar = new File(context.getExternalCacheDir().getAbsolutePath(), contactMail + ".jpg");
            }
            else{
                avatar = new File(context.getCacheDir().getAbsolutePath(), contactMail + ".jpg");
            }
            Bitmap bitmap = null;
            if (avatar.exists()){
                if (avatar.length() > 0){
                    BitmapFactory.Options bOpts = new BitmapFactory.Options();
                    bOpts.inPurgeable = true;
                    bOpts.inInputShareable = true;
                    bitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(), bOpts);
                    if (bitmap == null) {
                        return createDefaultAvatar(chat);
                    }
                    else{
                        return Util.getCircleBitmap(bitmap);
                    }
                }
                else{
                    return createDefaultAvatar(chat);
                }
            }
            else{
                return createDefaultAvatar(chat);
            }
        }
    }

    public Bitmap createDefaultAvatar(MegaChatRoom chat){
        log("createDefaultAvatar()");

        Bitmap defaultAvatar = Bitmap.createBitmap(Constants.DEFAULT_AVATAR_WIDTH_HEIGHT,Constants.DEFAULT_AVATAR_WIDTH_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(defaultAvatar);
        Paint paintText = new Paint();
        Paint paintCircle = new Paint();

        paintText.setColor(Color.WHITE);
        paintText.setTextSize(150);
        paintText.setAntiAlias(true);
        paintText.setTextAlign(Paint.Align.CENTER);
        Typeface face = Typeface.SANS_SERIF;
        paintText.setTypeface(face);
        paintText.setAntiAlias(true);
        paintText.setSubpixelText(true);
        paintText.setStyle(Paint.Style.FILL);

        if(chat.isGroup()){
            paintCircle.setColor(ContextCompat.getColor(context,R.color.divider_upgrade_account));
        }
        else{
            String color = megaApi.getUserAvatarColor(MegaApiAndroid.userHandleToBase64(chat.getPeerHandle(0)));
            if(color!=null){
                log("The color to set the avatar is "+color);
                paintCircle.setColor(Color.parseColor(color));
                paintCircle.setAntiAlias(true);
            }
            else{
                log("Default color to the avatar");
                paintCircle.setColor(ContextCompat.getColor(context, R.color.lollipop_primary_color));
                paintCircle.setAntiAlias(true);
            }
        }

        int radius;
        if (defaultAvatar.getWidth() < defaultAvatar.getHeight())
            radius = defaultAvatar.getWidth()/2;
        else
            radius = defaultAvatar.getHeight()/2;

        c.drawCircle(defaultAvatar.getWidth()/2, defaultAvatar.getHeight()/2, radius,paintCircle);

        if(chat.getTitle()!=null){
            if(!chat.getTitle().isEmpty()){
                char title = chat.getTitle().charAt(0);
                String firstLetter = new String(title+"");

                if(!firstLetter.equals("(")){

                    Rect bounds = new Rect();

                    paintText.getTextBounds(firstLetter,0,firstLetter.length(),bounds);
                    int xPos = (c.getWidth()/2);
                    int yPos = (int)((c.getHeight()/2)-((paintText.descent()+paintText.ascent()/2))+20);
                    c.drawText(firstLetter.toUpperCase(Locale.getDefault()), xPos, yPos, paintText);
                }

            }
        }
        return defaultAvatar;
    }

    public Notification buildSummary (String groupKey, boolean beep){
        Intent intent = new Intent(context, ManagerActivityLollipop.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(Constants.ACTION_CHAT_SUMMARY);
        intent.putExtra("CHAT_ID", -1);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 , intent, PendingIntent.FLAG_ONE_SHOT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!beep) {
                NotificationChannel channel = new NotificationChannel(notificationChannelIdChatSimple, notificationChannelNameChatSimple, NotificationManager.IMPORTANCE_LOW);
                channel.setShowBadge(true);
                channel.enableVibration(false);
                channel.setVibrationPattern(new long[]{ 0 });
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }

                NotificationCompat.Builder notificationBuilderO = new NotificationCompat.Builder(context, notificationChannelIdChatSimple);
                notificationBuilderO.setColor(ContextCompat.getColor(context, R.color.mega));

                notificationBuilderO.setSmallIcon(R.drawable.ic_stat_notify)
                        .setShowWhen(true)
                        .setGroup(groupKey)
                        .setGroupSummary(true)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setVibrate(null);

                return notificationBuilderO.build();
            } else {
                boolean vibrationEnabled = true;
                ChatSettings chatSettings = dbH.getChatSettings();
                if (chatSettings != null){
                    if (chatSettings.getVibrationEnabled()!=null && !chatSettings.getVibrationEnabled().isEmpty()){
                        if (chatSettings.getVibrationEnabled().compareTo("false") == 0){
                            vibrationEnabled = false;
                        }
                    }
                }

                NotificationChannel channel = null;
                if (vibrationEnabled){
                    channel = new NotificationChannel(notificationChannelIdChatSummary, notificationChannelNameChatSummary, NotificationManager.IMPORTANCE_HIGH);
                    channel.setShowBadge(true);
                }
                else{
                    channel = new NotificationChannel(notificationChannelIdChatSummaryNoVibrate, notificationChannelNameChatSummaryNoVibrate, NotificationManager.IMPORTANCE_HIGH);
                    channel.setShowBadge(true);
                    channel.enableVibration(false);
                    channel.setVibrationPattern(new long[] {0L});
                }

                if (notificationManager != null) {
                    if (notificationManager.getNotificationChannel(notificationChannelIdChatSummary) != null){
                        notificationManager.deleteNotificationChannel(notificationChannelIdChatSummary);
                    }
                    if (notificationManager.getNotificationChannel(notificationChannelIdChatSummaryNoVibrate) != null){
                        notificationManager.deleteNotificationChannel(notificationChannelIdChatSummaryNoVibrate);
                    }

                    notificationManager.createNotificationChannel(channel);
                }

                NotificationCompat.Builder notificationBuilderO = null;
                if (vibrationEnabled){
                    notificationBuilderO = new NotificationCompat.Builder(context, notificationChannelIdChatSummary);
                }
                else{
                    notificationBuilderO = new NotificationCompat.Builder(context, notificationChannelIdChatSummaryNoVibrate);
                }

                notificationBuilderO.setColor(ContextCompat.getColor(context, R.color.mega));

                notificationBuilderO.setSmallIcon(R.drawable.ic_stat_notify)
                        .setShowWhen(true)
                        .setGroup(groupKey)
                        .setGroupSummary(true)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

                return notificationBuilderO.build();
            }
        }
        else{
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notificationBuilder.setColor(ContextCompat.getColor(context, R.color.mega));
            }

            notificationBuilder.setSmallIcon(R.drawable.ic_stat_notify)
                    .setShowWhen(true)
                    .setGroup(groupKey)
                    .setGroupSummary(true)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            return notificationBuilder.build();
        }
    }

    public Notification buildSummary(String groupKey) {
        Intent intent = new Intent(context, ManagerActivityLollipop.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(Constants.ACTION_CHAT_SUMMARY);
        intent.putExtra("CHAT_ID", -1);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 , intent, PendingIntent.FLAG_ONE_SHOT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(notificationChannelIdChatSummary, notificationChannelNameChatSummary, NotificationManager.IMPORTANCE_HIGH);
            channel.setShowBadge(true);
            if (notificationManager != null){
                notificationManager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder notificationBuilderO = new NotificationCompat.Builder(context, notificationChannelIdChatSummary);
            notificationBuilderO.setColor(ContextCompat.getColor(context, R.color.mega));

            notificationBuilderO.setSmallIcon(R.drawable.ic_stat_notify)
                    .setShowWhen(true)
                    .setGroup(groupKey)
                    .setGroupSummary(true)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            return notificationBuilderO.build();
        }
        else {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notificationBuilder.setColor(ContextCompat.getColor(context, R.color.mega));
            }

            notificationBuilder.setSmallIcon(R.drawable.ic_stat_notify)
                    .setShowWhen(true)
                    .setGroup(groupKey)
                    .setGroupSummary(true)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            return notificationBuilder.build();
        }
    }

    public void removeAllChatNotifications(){
        log("removeAllChatNotifications");
        notificationManager.cancel(Constants.NOTIFICATION_SUMMARY_CHAT);
        notificationManager.cancel(Constants.NOTIFICATION_GENERAL_PUSH_CHAT);
    }

    public void showSimpleNotification(){
        log("showSimpleNotification");
    
        Intent myService = new Intent(context, IncomingMessageService.class);
        context.stopService(myService);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(notificationChannelIdChatSimple, notificationChannelNameChatSimple, NotificationManager.IMPORTANCE_LOW);
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{ 0 });
            channel.setShowBadge(true);
            if (notificationManager == null) {
                notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            }
            notificationManager.createNotificationChannel(channel);

            Intent intent = new Intent(context, ManagerActivityLollipop.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setAction(Constants.ACTION_CHAT_SUMMARY);
            intent.putExtra("CHAT_ID", -1);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            NotificationCompat.Builder notificationBuilderO = new NotificationCompat.Builder(context, notificationChannelIdChatSimple);
            notificationBuilderO
                    .setSmallIcon(R.drawable.ic_stat_notify)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true).setTicker(context.getString(R.string.notification_chat_undefined_title))
                    .setContentTitle(context.getString(R.string.notification_chat_undefined_title)).setContentText(context.getString(R.string.notification_chat_undefined_content))
                    .setOngoing(false)
                    .setColor(ContextCompat.getColor(context, R.color.mega));

            notificationManager.notify(Constants.NOTIFICATION_GENERAL_PUSH_CHAT, notificationBuilderO.build());
        }
        else {

            mBuilderCompat = new NotificationCompat.Builder(context);

            if (notificationManager == null) {
                notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            }

            Intent intent = new Intent(context, ManagerActivityLollipop.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setAction(Constants.ACTION_CHAT_SUMMARY);
            intent.putExtra("CHAT_ID", -1);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            mBuilderCompat
                    .setSmallIcon(R.drawable.ic_stat_notify)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true).setTicker(context.getString(R.string.notification_chat_undefined_title))
                    .setContentTitle(context.getString(R.string.notification_chat_undefined_title)).setContentText(context.getString(R.string.notification_chat_undefined_content))
                    .setOngoing(false);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBuilderCompat.setColor(ContextCompat.getColor(context, R.color.mega));
            }

            notificationManager.notify(Constants.NOTIFICATION_GENERAL_PUSH_CHAT, mBuilderCompat.build());
        }
    }



    public void showIncomingCallNotification(MegaChatCall callToAnswer, MegaChatCall callInProgress) {
        log("showIncomingCallNotification");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1){
            MegaChatRoom chatToAnswer = megaChatApi.getChatRoom(callToAnswer.getChatid());

            MegaChatRoom chatInProgress = megaChatApi.getChatRoom(callInProgress.getChatid());
            long chatHandleInProgress = -1;
            if(chatInProgress!=null){
                chatHandleInProgress = callInProgress.getChatid();
            }
            log("showIncomingCallNotification:chatInProgress: "+callInProgress.getChatid());

//        int notificationId = Constants.NOTIFICATION_INCOMING_CALL;
            long chatCallId = callToAnswer.getId();
            String notificationCallId = MegaApiJava.userHandleToBase64(chatCallId);
            int notificationId = (notificationCallId).hashCode();

            Intent ignoreIntent = new Intent(context, CallNotificationIntentService.class);
            ignoreIntent.putExtra("chatHandleInProgress", chatHandleInProgress);
            ignoreIntent.putExtra("chatHandleToAnswer", callToAnswer.getChatid());
            ignoreIntent.setAction(CallNotificationIntentService.IGNORE);
            int requestCodeIgnore = notificationId + 1;
            PendingIntent pendingIntentIgnore = PendingIntent.getService(context, requestCodeIgnore, ignoreIntent,  PendingIntent.FLAG_CANCEL_CURRENT);

            Intent answerIntent = new Intent(context, CallNotificationIntentService.class);
            answerIntent.putExtra("chatHandleInProgress", chatHandleInProgress);
            answerIntent.putExtra("chatHandleToAnswer", callToAnswer.getChatid());
            answerIntent.setAction(CallNotificationIntentService.ANSWER);
            int requestCodeAnswer = notificationId + 2;
            PendingIntent pendingIntentAnswer = PendingIntent.getService(context, requestCodeAnswer /* Request code */, answerIntent,  PendingIntent.FLAG_CANCEL_CURRENT);

            NotificationCompat.Action actionAnswer = new NotificationCompat.Action.Builder(R.drawable.ic_call_filled, context.getString(R.string.answer_call_incoming).toUpperCase(), pendingIntentAnswer).build();
            NotificationCompat.Action actionIgnore = new NotificationCompat.Action.Builder(R.drawable.ic_remove_not, context.getString(R.string.ignore_call_incoming).toUpperCase(), pendingIntentIgnore).build();


            long[] pattern = {0, 1000, 1000, 1000, 1000, 1000, 1000};



            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                log("showIncomingCallNotification:  oreo");

                //Create a channel for android Oreo or higher
                NotificationChannel channel = new NotificationChannel(notificationChannelIdIncomingCall, notificationChannelNameIncomingCall, NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("");
                channel.enableLights(true);
                channel.enableVibration(true);
                channel.setShowBadge(true);

                notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.createNotificationChannel(channel);

                NotificationCompat.Builder notificationBuilderO = new NotificationCompat.Builder(context, notificationChannelIdIncomingCall);
                notificationBuilderO
                        .setSmallIcon(R.drawable.ic_stat_notify)
                        .setContentText(context.getString(R.string.notification_subtitle_incoming))
                        .setAutoCancel(false)
                        .setContentIntent(null)
                        .setVibrate(pattern)
                        .addAction(actionAnswer)
                        .addAction(actionIgnore)
                        .setColor(ContextCompat.getColor(context, R.color.mega))
                        .setPriority(NotificationManager.IMPORTANCE_HIGH);

                if(chatToAnswer.isGroup()){
                    notificationBuilderO.setContentTitle(chatToAnswer.getTitle());
                }
                else{
                    notificationBuilderO.setContentTitle(chatToAnswer.getPeerFullname(0));
                }

                Bitmap largeIcon = setUserAvatar(chatToAnswer);
                if (largeIcon != null) {
                    notificationBuilderO.setLargeIcon(largeIcon);
                }

                notificationManager.notify(notificationId, notificationBuilderO.build());

            }else{
                log("showIncomingCallNotification:  nougat");

                notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, notificationChannelIdIncomingCall);
                notificationBuilder
                        .setSmallIcon(R.drawable.ic_stat_notify)
                        .setContentText(context.getString(R.string.notification_subtitle_incoming))
                        .setAutoCancel(false)
                        .setContentIntent(null)
                        .addAction(actionAnswer)
                        .addAction(actionIgnore);

                if(chatToAnswer.isGroup()){
                    notificationBuilder.setContentTitle(chatToAnswer.getTitle());
                }else{
                    notificationBuilder.setContentTitle(chatToAnswer.getPeerFullname(0));
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    notificationBuilder.setColor(ContextCompat.getColor(context, R.color.mega));
                }
                Bitmap largeIcon = setUserAvatar(chatToAnswer);
                if (largeIcon != null) {
                    notificationBuilder.setLargeIcon(largeIcon);
                }

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
                    //API 25 = Android 7.1
                    notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
                } else {
                    notificationBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH);
                }

                //Show the notification:
                notificationManager.notify(notificationId, notificationBuilder.build());
            }
        }
        else{
            log("Not supported incoming call notification: "+Build.VERSION.SDK_INT);
        }
    }

    public void checkQueuedCalls(){
        log("checkQueuedCalls");

        MegaHandleList handleList = megaChatApi.getChatCalls();
        if(handleList!=null){
            long numberOfCalls = handleList.size();
            log("Number of calls in progress: "+numberOfCalls);
            if (numberOfCalls>1){
                log("MORE than one call in progress: "+numberOfCalls);
                MegaChatCall callInProgress = null;
                MegaChatCall callIncoming = null;

                for(int i=0; i<handleList.size(); i++){
                    MegaChatCall call = megaChatApi.getChatCall(handleList.get(i));
                    if(call!=null){
                        log("Call ChatID: "+call.getChatid()+" Status: "+call.getStatus());
                        if((call.getStatus()>=MegaChatCall.CALL_STATUS_IN_PROGRESS) && (call.getStatus()<MegaChatCall.CALL_STATUS_TERMINATING_USER_PARTICIPATION)){
                            callInProgress = call;
                            log("FOUND Call in progress: "+callInProgress.getChatid());
                            break;
                        }
                    }
                }

                if(callInProgress==null){
                    long openCallChatId = MegaApplication.getOpenCallChatId();
                    log("openCallId: "+openCallChatId);
                    if(openCallChatId!=-1){
                        MegaChatCall possibleCall = megaChatApi.getChatCall(openCallChatId);
                        if(possibleCall.getStatus()<MegaChatCall.CALL_STATUS_TERMINATING_USER_PARTICIPATION){
                            callInProgress = possibleCall;
                            log("FOUND Call activity shown: "+callInProgress.getChatid());
                        }
                    }
                }

                for(int i=0; i<handleList.size(); i++){
                    MegaChatCall call = megaChatApi.getChatCall(handleList.get(i));
                    if(call!=null){

                        if(call.getStatus()<MegaChatCall.CALL_STATUS_IN_PROGRESS && (!call.isIgnored())){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if(notificationManager == null){
                                    notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                }

                                StatusBarNotification[] notifs = notificationManager.getActiveNotifications();
                                boolean shown=false;

                                long chatCallId = call.getId();
                                String notificationCallId = MegaApiJava.userHandleToBase64(chatCallId);
                                int notificationId = (notificationCallId).hashCode();

                                log("Active Notifications: "+ notifs.length);
                                for(int k = 0; k< notifs.length; k++){
                                    if(notifs[k].getId()==notificationId){
                                        log("Notification for this call already shown");
                                        shown = true;
                                        break;
                                    }
                                }

                                if(!shown){
                                    if(callInProgress.getId()!=call.getId()){
                                        callIncoming = call;
                                        log("(1) FOUND Call incoming and NOT shown and NOT ignored: "+callIncoming.getChatid());
                                        break;
                                    }
                                }
                            }
                            else{
                                callIncoming = call;
                                log("(2) FOUND Call incoming and NOT shown and NOT ignored: "+callIncoming.getChatid());
                                break;
                            }
                        }
                    }
                }

                if(callInProgress!=null){
                    if(callIncoming!=null){
                        showIncomingCallNotification(callIncoming, callInProgress);
                    }
                    else{
                        log("ERROR:callIncoming is NULL");
                    }
                }
                else{
                    log("callInProgress NOT found");
                }
            }
            else{
                log("No calls to launch");
            }
        }
    }

    public void showMissedCallNotification(MegaChatCall call) {
        log("showMissedCallNotification");

        MegaChatRoom chat = megaChatApi.getChatRoom(call.getChatid());

        String notificationContent = chat.getPeerFullname(0);

        long chatCallId = call.getId();
        String notificationCallId = MegaApiJava.userHandleToBase64(chatCallId);
        int notificationId = (notificationCallId).hashCode() + Constants.NOTIFICATION_MISSED_CALL;

        Intent intent = new Intent(context, ManagerActivityLollipop.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(Constants.ACTION_CHAT_NOTIFICATION_MESSAGE);
        intent.putExtra("CHAT_ID", chat.getChatId());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int)chat.getChatId() , intent, PendingIntent.FLAG_ONE_SHOT);

        long[] pattern = {0, 1000};

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(notificationChannelIdInProgressMissedCall, notificationChannelNameInProgressMissedCall, NotificationManager.IMPORTANCE_HIGH);
            channel.setShowBadge(true);
            if (notificationManager == null) {
                notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            }
            notificationManager.createNotificationChannel(channel);

            NotificationCompat.Builder notificationBuilderO = new NotificationCompat.Builder(context, notificationChannelIdInProgressMissedCall);
            notificationBuilderO
                    .setSmallIcon(R.drawable.ic_stat_notify)
                    .setContentTitle(context.getString(R.string.missed_call_notification_title))
                    .setContentText(notificationContent)
                    .setAutoCancel(true)
                    .setVibrate(pattern)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
                    .setColor(ContextCompat.getColor(context, R.color.mega))
                    .setPriority(NotificationManager.IMPORTANCE_HIGH);

            if (chat.getPeerEmail(0) != null) {

                Bitmap largeIcon = setUserAvatar(chat);
                if (largeIcon != null) {
                    notificationBuilderO.setLargeIcon(largeIcon);
                }
            }

            notificationManager.notify(notificationId, notificationBuilderO.build());
        }
        else {

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_stat_notify)
                    .setContentTitle(context.getString(R.string.missed_call_notification_title))
                    .setContentText(notificationContent)
                    .setAutoCancel(true)
                    .setVibrate(pattern)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notificationBuilder.setColor(ContextCompat.getColor(context, R.color.mega));
            }

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
                //API 25 = Android 7.1
                notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
            } else {
                notificationBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH);
            }

            if (chat.getPeerEmail(0) != null) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Bitmap largeIcon = setUserAvatar(chat);
                    if (largeIcon != null) {
                        notificationBuilder.setLargeIcon(largeIcon);
                    }
                }
            }

            if (notificationManager == null) {
                notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            }

            notificationManager.notify(notificationId, notificationBuilder.build());
        }
    }

    public void generateChatNotification(MegaChatRequest request){
        log("generateChatNotification");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            log("generateChatNotification:POST Android O");
            Intent myService = new Intent(context, IncomingMessageService.class);
            context.stopService(myService);
            newGenerateChatNotification(request);
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            String manufacturer = "xiaomi";
            if(!manufacturer.equalsIgnoreCase(Build.MANUFACTURER)) {
                log("generateChatNotification:POST Android N");
                newGenerateChatNotification(request);
            }
            else{
                log("generateChatNotification:XIAOMI POST Android N");
                generateChatNotificationPreN(request);
            }
        }
        else {
            log("generateChatNotification:PRE Android N");
            generateChatNotificationPreN(request);
        }
    }

    public void newGenerateChatNotification(MegaChatRequest request){
        log("newGenerateChatNotification");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            boolean beep = request.getFlag();
            log("should beep: " + beep);

            MegaHandleList chatHandleList = request.getMegaHandleList();
            ArrayList<MegaChatListItem> chats = new ArrayList<>();
            for (int i = 0; i < chatHandleList.size(); i++) {
                MegaChatListItem chat = megaChatApi.getChatListItem(chatHandleList.get(i));
                chats.add(chat);
            }

            //Order by last interaction
            Collections.sort(chats, new Comparator<MegaChatListItem>() {

                public int compare(MegaChatListItem c1, MegaChatListItem c2) {
                    long timestamp1 = c1.getLastTimestamp();
                    long timestamp2 = c2.getLastTimestamp();

                    long result = timestamp2 - timestamp1;
                    return (int) result;
                }
            });
            //Check if the last chat notification is enabled

            long lastChatId = -1;
            if (chats != null) {
                if (!(chats.isEmpty())) {
                    lastChatId = chats.get(0).getChatId();
                } else {
                    log("ERROR:chatsEMPTY:removeAllChatNotifications");
                    removeAllChatNotifications();
                    return;
                }
            } else {
                log("ERROR:chatsNULL:removeAllChatNotifications");
                removeAllChatNotifications();
                return;
            }

            log("generateChatNotification for: " + chats.size() + " chats");

            boolean showNotif = false;

            if (MegaApplication.getOpenChatId() != lastChatId) {

                MegaHandleList handleListUnread = request.getMegaHandleListByChat(lastChatId);

                showNotif = shouldShowChatNotification(lastChatId, handleListUnread, beep);

                if (!showNotif) {
                    log("Muted chat - do not show notification");
                }
            }

            log("generateChatNotification for: " + chats.size() + " chats");
            if (showNotif) {
                for (int i = 0; i < chats.size(); i++) {
                    if (MegaApplication.getOpenChatId() != chats.get(i).getChatId()) {

                        MegaHandleList handleListUnread = request.getMegaHandleListByChat(chats.get(i).getChatId());

                        boolean showN = shouldCheckNotificationsSound(chats.get(i).getChatId(), handleListUnread, beep);
                        if (showN) {
                            showChatNotification(chats.get(i).getChatId(), handleListUnread, beep);
                            if (beep) {
                                beep = false;
                            }
                        }
                    } else {
                        log("Do not show notification - opened chat");
                    }
                }

                Notification summary = buildSummary(GROUP_KEY, request.getFlag());
                notificationManager.notify(Constants.NOTIFICATION_SUMMARY_CHAT, summary);
            } else {
                log("Mute for the last chat");
            }
        }
        else{
            boolean beep = request.getFlag();
            log("should beep: " + beep);

            MegaHandleList chatHandleList = request.getMegaHandleList();
            ArrayList<MegaChatListItem> chats = new ArrayList<>();
            for (int i = 0; i < chatHandleList.size(); i++) {
                MegaChatListItem chat = megaChatApi.getChatListItem(chatHandleList.get(i));
                chats.add(chat);
            }

            //Order by last interaction
            Collections.sort(chats, new Comparator<MegaChatListItem>() {

                public int compare(MegaChatListItem c1, MegaChatListItem c2) {
                    long timestamp1 = c1.getLastTimestamp();
                    long timestamp2 = c2.getLastTimestamp();

                    long result = timestamp2 - timestamp1;
                    return (int) result;
                }
            });


            //Check if the last chat notification is enabled

            long lastChatId = -1;
            if (chats != null) {
                if (!(chats.isEmpty())) {
                    lastChatId = chats.get(0).getChatId();
                } else {
                    log("ERROR:chatsEMPTY:return");
                    return;
                }
            } else {
                log("ERROR:chatsNULL:return");
                return;
            }

            log("generateChatNotification for: " + chats.size() + " chats");

            boolean showNotif = false;

            if (MegaApplication.getOpenChatId() != lastChatId) {

                MegaHandleList handleListUnread = request.getMegaHandleListByChat(lastChatId);

                showNotif = shouldShowChatNotification(lastChatId, handleListUnread, beep);

                if (!showNotif) {
                    log("Muted chat - do not show notification");
                }
            }

            if (showNotif) {
                for (int i = 0; i < chats.size(); i++) {
                    if (MegaApplication.getOpenChatId() != chats.get(i).getChatId()) {

                        MegaHandleList handleListUnread = request.getMegaHandleListByChat(chats.get(i).getChatId());

                        boolean showN = shouldCheckNotificationsSound(chats.get(i).getChatId(), handleListUnread, beep);
                        if (showN) {
                            showChatNotification(chats.get(i).getChatId(), handleListUnread, beep);
                            if (beep) {
                                beep = false;
                            }
                        }
                    } else {
                        log("Do not show notification - opened chat");
                    }
                }

                Notification summary = buildSummary(GROUP_KEY, request.getFlag());
                notificationManager.notify(Constants.NOTIFICATION_SUMMARY_CHAT, summary);
            } else {
                log("Mute for the last chat");
            }
        }
    }

    public void generateChatNotificationPreN(MegaChatRequest request){
        log("generateChatNotificationPreN");
        boolean beep = request.getFlag();
        log("should beep: "+beep);

        MegaHandleList chatHandleList = request.getMegaHandleList();
        log("size chatHandleList: "+chatHandleList.size());
        ArrayList<MegaChatListItem> chats = new ArrayList<>();
        for(int i=0; i<chatHandleList.size(); i++){
            MegaChatListItem chat = megaChatApi.getChatListItem(chatHandleList.get(i));
            chats.add(chat);
        }

        //Order by last interaction
        Collections.sort(chats, new Comparator<MegaChatListItem> (){

            public int compare(MegaChatListItem c1, MegaChatListItem c2) {
                long timestamp1 = c1.getLastTimestamp();
                long timestamp2 = c2.getLastTimestamp();

                long result = timestamp2 - timestamp1;
                return (int)result;
            }
        });

        log("generateChatNotificationPreN for: "+chats.size()+" chats");
        long lastChatId = -1;
        if(chats!=null && (!(chats.isEmpty()))){
            lastChatId = chats.get(0).getChatId();
            showChatNotificationPreN(request, beep, lastChatId);
        }else{
            removeAllChatNotifications();
        }
    }

    public void showChatNotificationPreN(MegaChatRequest request, boolean beep, long lastChatId){
        log("showChatNotification");

        if(beep){
            ChatSettings chatSettings = dbH.getChatSettings();

            if (chatSettings != null) {

                if (chatSettings.getNotificationsEnabled()==null){
                    log("getNotificationsEnabled NULL --> Notifications ON");
                    checkNotificationsSoundPreN(request, beep, lastChatId);
                }
                else{
                    if (chatSettings.getNotificationsEnabled().equals("true")) {
                        log("Notifications ON for all chats");
                        checkNotificationsSoundPreN(request, beep, lastChatId);
                    } else {
                        log("Notifications OFF");
                    }
                }

            } else {
                log("Notifications DEFAULT ON");

                Uri defaultSoundUri2 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                buildNotificationPreN(defaultSoundUri2, "true", request);
            }
        }
        else{
            buildNotificationPreN(null, "false", request);
        }
    }

    public void checkNotificationsSoundPreN(MegaChatRequest request, boolean beep, long lastChatId) {
        log("checkNotificationsSound: " + beep);

        ChatSettings chatSettings = dbH.getChatSettings();
        ChatItemPreferences chatItemPreferences = dbH.findChatPreferencesByHandle(String.valueOf(lastChatId));

        if (chatItemPreferences == null || chatItemPreferences.getNotificationsEnabled() == null || chatItemPreferences.getNotificationsEnabled().isEmpty() || chatItemPreferences.getNotificationsEnabled().equals("true")) {
            log("checkNotificationsSound: Notifications OFF for this chat");

            if (chatSettings.getNotificationsSound() == null){
                log("Notification sound is NULL");
                Uri defaultSoundUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
                buildNotificationPreN(defaultSoundUri, chatSettings.getVibrationEnabled(), request);
            }
            else if(chatSettings.getNotificationsSound().equals("-1")){
                log("Silent notification Notification sound -1");
                buildNotificationPreN(null, chatSettings.getVibrationEnabled(), request);
            }
            else{
                String soundString = chatSettings.getNotificationsSound();
                Uri uri = Uri.parse(soundString);
                log("Uri: " + uri);

                if (soundString.equals("true") || soundString.equals("")) {

                    Uri defaultSoundUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
                    buildNotificationPreN(defaultSoundUri, chatSettings.getVibrationEnabled(), request);
                } else if (soundString.equals("-1")) {
                    log("Silent notification");
                    buildNotificationPreN(null, chatSettings.getVibrationEnabled(), request);
                } else {
                    Ringtone sound = RingtoneManager.getRingtone(context, uri);
                    if (sound == null) {
                        log("Sound is null");
                        buildNotificationPreN(null, chatSettings.getVibrationEnabled(), request);
                    } else {
                        buildNotificationPreN(uri, chatSettings.getVibrationEnabled(), request);
                    }
                }
            }

        } else {
            log("checkNotificationsSound: Notifications OFF for this chat");
        }
    }

    public boolean showChatNotification(long chatid, MegaHandleList handleListUnread, boolean beep){
        log("showChatNotification: "+beep);

        if(beep){

            ChatSettings chatSettings = dbH.getChatSettings();
            if (chatSettings != null) {
                if (chatSettings.getNotificationsEnabled()==null){
                    log("getNotificationsEnabled NULL --> Notifications ON");

                    return checkNotificationsSound(chatid, handleListUnread, beep);
                }
                else{
                    if (chatSettings.getNotificationsEnabled().equals("true")) {
                        log("Notifications ON for all chats");

                        return checkNotificationsSound(chatid, handleListUnread, beep);
                    } else {
                        log("Notifications OFF");
                        return false;
                    }
                }

            } else {
                log("Notifications DEFAULT ON");

                Uri defaultSoundUri2 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                sendBundledNotification(defaultSoundUri2, "true", chatid, handleListUnread);
                return true;
            }
        }
        else{
            sendBundledNotification(null, "false", chatid, handleListUnread);
            return true;
        }
    }

    public boolean shouldShowChatNotification(long chatid, MegaHandleList handleListUnread, boolean beep){
        log("shouldShowChatNotification: "+beep);

        if(beep){

            ChatSettings chatSettings = dbH.getChatSettings();
            if (chatSettings != null) {
                if (chatSettings.getNotificationsEnabled()==null){
                    log("getNotificationsEnabled NULL --> Notifications ON");

                    return shouldCheckNotificationsSound(chatid, handleListUnread, beep);
                }
                else{
                    if (chatSettings.getNotificationsEnabled().equals("true")) {
                        log("Notifications ON for all chats");

                        return shouldCheckNotificationsSound(chatid, handleListUnread, beep);
                    } else {
                        log("Notifications OFF");
                        return false;
                    }
                }

            } else {
                log("Notifications DEFAULT ON");
                return true;
            }
        }
        else{
            return true;
        }
    }

    public boolean checkNotificationsSound(long chatid, MegaHandleList handleListUnread, boolean beep){
        log("checkNotificationsSound: "+beep);

        ChatSettings chatSettings = dbH.getChatSettings();
        ChatItemPreferences chatItemPreferences = dbH.findChatPreferencesByHandle(String.valueOf(chatid));

        if (chatItemPreferences == null || chatItemPreferences.getNotificationsEnabled() == null || chatItemPreferences.getNotificationsEnabled().isEmpty() ||chatItemPreferences.getNotificationsEnabled().equals("true")) {
            log("checkNotificationsSound: Notifications ON for this chat");

            removeAllChatNotifications();

            if (chatSettings.getNotificationsSound() == null){
                log("Notification sound is NULL");
                Uri defaultSoundUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
                sendBundledNotification(defaultSoundUri, chatSettings.getVibrationEnabled(), chatid, handleListUnread);

            }
            else if(chatSettings.getNotificationsSound().equals("-1")){
                log("Silent notification Notification sound -1");
                sendBundledNotification(null, chatSettings.getVibrationEnabled(), chatid, handleListUnread);
            }
            else{
                String soundString = chatSettings.getNotificationsSound();
                Uri uri = Uri.parse(soundString);
                log("Uri: " + uri);

                if (soundString.equals("true") || soundString.equals("")) {

                    Uri defaultSoundUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
                    sendBundledNotification(defaultSoundUri, chatSettings.getVibrationEnabled(), chatid, handleListUnread);
                } else if (soundString.equals("-1")) {
                    log("Silent notification");
                    sendBundledNotification(null, chatSettings.getVibrationEnabled(), chatid, handleListUnread);
                } else {
                    Ringtone sound = RingtoneManager.getRingtone(context, uri);
                    if (sound == null) {
                        log("Sound is null");
                        sendBundledNotification(null, chatSettings.getVibrationEnabled(), chatid, handleListUnread);
                    } else {
                        sendBundledNotification(uri, chatSettings.getVibrationEnabled(), chatid, handleListUnread);
                    }
                }
            }
            return true;
        } else {
            log("checkNotificationsSound: Notifications OFF for this chat");
            return false;
        }
    }

    public boolean shouldCheckNotificationsSound(long chatid, MegaHandleList handleListUnread, boolean beep){
        log("shouldCheckNotificationsSound: "+beep);

        ChatSettings chatSettings = dbH.getChatSettings();
        ChatItemPreferences chatItemPreferences = dbH.findChatPreferencesByHandle(String.valueOf(chatid));

        if (chatItemPreferences == null || chatItemPreferences.getNotificationsEnabled() == null || chatItemPreferences.getNotificationsEnabled().isEmpty() ||chatItemPreferences.getNotificationsEnabled().equals("true")) {
            log("shouldCheckNotificationsSound: Notifications ON for this chat");
            return true;
        } else {
            log("shouldCheckNotificationsSound: Notifications OFF for this chat");
            return false;
        }
    }

    public static void log(String message) {
        Util.log("ChatAdvancedNotificationBuilder", message);
    }

}
