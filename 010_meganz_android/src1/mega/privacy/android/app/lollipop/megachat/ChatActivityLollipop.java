package mega.privacy.android.app.lollipop.megachat;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MimeTypeList;
import mega.privacy.android.app.R;
import mega.privacy.android.app.ShareInfo;
import mega.privacy.android.app.components.MarqueeTextView;
import mega.privacy.android.app.components.NpaLinearLayoutManager;
import mega.privacy.android.app.components.twemoji.EmojiEditText;
import mega.privacy.android.app.components.twemoji.EmojiKeyboard;
import mega.privacy.android.app.lollipop.AddContactActivityLollipop;
import mega.privacy.android.app.lollipop.AudioVideoPlayerLollipop;
import mega.privacy.android.app.lollipop.ContactInfoActivityLollipop;
import mega.privacy.android.app.lollipop.FileLinkActivityLollipop;
import mega.privacy.android.app.lollipop.FileStorageActivityLollipop;
import mega.privacy.android.app.lollipop.FolderLinkActivityLollipop;
import mega.privacy.android.app.lollipop.LoginActivityLollipop;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.PdfViewerActivityLollipop;
import mega.privacy.android.app.lollipop.PinActivityLollipop;
import mega.privacy.android.app.lollipop.controllers.ChatController;
import mega.privacy.android.app.lollipop.listeners.ChatLinkInfoListener;
import mega.privacy.android.app.lollipop.listeners.CreateChatToPerformActionListener;
import mega.privacy.android.app.lollipop.listeners.MultipleForwardChatProcessor;
import mega.privacy.android.app.lollipop.listeners.MultipleGroupChatRequestListener;
import mega.privacy.android.app.lollipop.listeners.MultipleRequestListener;
import mega.privacy.android.app.lollipop.megachat.calls.ChatCallActivity;
import mega.privacy.android.app.lollipop.megachat.chatAdapters.MegaChatLollipopAdapter;
import mega.privacy.android.app.lollipop.tasks.FilePrepareTask;
import mega.privacy.android.app.modalbottomsheet.chatmodalbottomsheet.AttachmentUploadBottomSheetDialogFragment;
import mega.privacy.android.app.modalbottomsheet.chatmodalbottomsheet.ContactAttachmentBottomSheetDialogFragment;
import mega.privacy.android.app.modalbottomsheet.chatmodalbottomsheet.MessageNotSentBottomSheetDialogFragment;
import mega.privacy.android.app.modalbottomsheet.chatmodalbottomsheet.NodeAttachmentBottomSheetDialogFragment;
import mega.privacy.android.app.modalbottomsheet.chatmodalbottomsheet.PendingMessageBottomSheetDialogFragment;
import mega.privacy.android.app.utils.ChatUtil;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.MegaApiUtils;
import mega.privacy.android.app.utils.TimeUtils;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaChatApi;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatApiJava;
import nz.mega.sdk.MegaChatCall;
import nz.mega.sdk.MegaChatCallListenerInterface;
import nz.mega.sdk.MegaChatContainsMeta;
import nz.mega.sdk.MegaChatError;
import nz.mega.sdk.MegaChatListItem;
import nz.mega.sdk.MegaChatListenerInterface;
import nz.mega.sdk.MegaChatMessage;
import nz.mega.sdk.MegaChatPeerList;
import nz.mega.sdk.MegaChatPresenceConfig;
import nz.mega.sdk.MegaChatRequest;
import nz.mega.sdk.MegaChatRequestListenerInterface;
import nz.mega.sdk.MegaChatRoom;
import nz.mega.sdk.MegaChatRoomListenerInterface;
import nz.mega.sdk.MegaContactRequest;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaHandleList;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaNodeList;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import nz.mega.sdk.MegaTransfer;
import nz.mega.sdk.MegaUser;

import static mega.privacy.android.app.utils.Util.adjustForLargeFont;
import static mega.privacy.android.app.utils.Util.context;
import static mega.privacy.android.app.utils.Util.toCDATA;

public class ChatActivityLollipop extends PinActivityLollipop implements MegaChatCallListenerInterface, MegaChatRequestListenerInterface, MegaRequestListenerInterface, MegaChatListenerInterface, MegaChatRoomListenerInterface,  View.OnClickListener{

    public MegaChatLollipopAdapter.ViewHolderMessageChat holder_imageDrag;
    public int position_imageDrag = -1;

    public static int NUMBER_MESSAGES_TO_LOAD = 20;
    public static int NUMBER_MESSAGES_BEFORE_LOAD = 8;

    public static int MEGA_FILE_LINK = 1;
    public static int MEGA_FOLDER_LINK = 2;
    public static int MEGA_CHAT_LINK = 3;

    public static int SHOW_WRITING_LAYOUT = 1;
    public static int SHOW_JOIN_LAYOUT = 2;
    public static int SHOW_NOTHING_LAYOUT = 3;
    public static int INITIAL_PRESENCE_STATUS = -55;

    String mOutputFilePath;

    boolean newVisibility = false;
    boolean getMoreHistory=false;

    int minutesLastGreen = -1;

    boolean isLoadingHistory = false;

    private AlertDialog errorOpenChatDialog;

    long numberToLoad = -1;

    private android.support.v7.app.AlertDialog downloadConfirmationDialog;
    private AlertDialog chatAlertDialog;

    ProgressDialog dialog;
    ProgressDialog statusDialog;

    boolean retryHistory = false;

    public long lastIdMsgSeen = -1;
    public long generalUnreadCount = -1;
    boolean lastSeenReceived = false;
    int positionToScroll = -1;
    public int positionNewMessagesLayout = -1;

    boolean isTakePicture = false;
    MegaApiAndroid megaApi;
    MegaChatApiAndroid megaChatApi;

    Handler handlerReceive;
    Handler handlerSend;
    Handler handlerKeyboard;
    Handler handlerEmojiKeyboard;

    TextView emptyTextView;
    ImageView emptyImageView;
    LinearLayout emptyLayout;

    boolean pendingMessagesLoaded = false;

    public boolean activityVisible = false;
    boolean setAsRead = false;

    boolean isOpeningChat = true;

    int selectedPosition;
    public long selectedMessageId = -1;
    MegaChatRoom chatRoom;

    public long idChat;

    boolean noMoreNoSentMessages = false;

    public int showRichLinkWarning = Constants.RICH_WARNING_TRUE;

    private BadgeDrawerArrowDrawable badgeDrawable;

    ChatController chatC;
    boolean scrollingUp = false;

    long myUserHandle;

    ActionBar aB;
    Toolbar tB;
    LinearLayout toolbarElements;
    RelativeLayout toolbarElementsInside;

    TextView titleToolbar;
    MarqueeTextView individualSubtitleToobar;
    TextView groupalSubtitleToolbar;
    LinearLayout subtitleCall;
    Chronometer chronoCall;
    LinearLayout participantsLayout;
    TextView participantsText;
    ImageView iconStateToolbar;

    ImageView privateIconToolbar;

    float density;
    DisplayMetrics outMetrics;
    Display display;

    boolean editingMessage = false;
    MegaChatMessage messageToEdit = null;

    CoordinatorLayout fragmentContainer;
    RelativeLayout writingContainerLayout;
    RelativeLayout writingLayout;
    RelativeLayout disabledWritingLayout;

    RelativeLayout joinChatLinkLayout;
    Button joinButton;

    RelativeLayout chatRelativeLayout;
    RelativeLayout userTypingLayout;
    TextView userTypingText;
    boolean sendIsTyping=true;
    long userTypingTimeStamp = -1;
    ImageButton keyboardTwemojiButton;
    ImageButton mediaButton;
    ImageButton sendContactButton ;
    ImageButton pickFileSystemButton;
    ImageButton pickCloudDriveButton;
    ImageButton pickFileStorageButton;

    EmojiKeyboard emojiKeyboard;
    LinearLayout linearLayoutTwemoji;

    RelativeLayout rLKeyboardTwemojiButton;
    RelativeLayout rLMediaButton;
    RelativeLayout rLSendContactButton ;
    RelativeLayout rLPickFileSystemButton;
    RelativeLayout rLPickCloudDriveButton;
    RelativeLayout rLPickFileStorageButton;

    RelativeLayout callInProgressLayout;
    TextView callInProgressText;
    Chronometer callInProgressChrono;

    boolean startVideo = false;

    EmojiEditText textChat;
    ImageButton sendIcon;
    RelativeLayout messagesContainerLayout;

    RelativeLayout observersLayout;
    TextView observersNumberText;

    RecyclerView listView;
    NpaLinearLayoutManager mLayoutManager;

    ChatActivityLollipop chatActivity;

    MenuItem importIcon;
    MenuItem callMenuItem;
    MenuItem videoMenuItem;
    MenuItem inviteMenuItem;
    MenuItem clearHistoryMenuItem;
    MenuItem contactInfoMenuItem;
    MenuItem leaveMenuItem;
    MenuItem archiveMenuItem;

    String intentAction;
    MegaChatLollipopAdapter adapter;
    int stateHistory;

    DatabaseHandler dbH = null;

    FrameLayout fragmentContainerFileStorage;
    RelativeLayout fileStorageLayout;
    private ChatFileStorageFragment fileStorageF;

    ArrayList<AndroidMegaChatMessage> messages;
    ArrayList<AndroidMegaChatMessage> bufferMessages;
    ArrayList<AndroidMegaChatMessage> bufferManualSending;
    ArrayList<AndroidMegaChatMessage> bufferSending;

    public static int TYPE_MESSAGE_JUMP_TO_LEAST = 0;
    public static int TYPE_MESSAGE_NEW_MESSAGE = 1;
    RelativeLayout messageJumpLayout;
    TextView messageJumpText;
    boolean isHideJump = false;
    int typeMessageJump = 0;
    boolean visibilityMessageJump=false;
    boolean isTurn = false;
    Handler handler;

    private boolean isShareLinkDialogDismissed = false;

    private ActionMode actionMode;

    private class UserTyping {
        MegaChatParticipant participantTyping;
        long timeStampTyping;

        public UserTyping(MegaChatParticipant participantTyping) {
            this.participantTyping = participantTyping;
        }

        public MegaChatParticipant getParticipantTyping() {
            return participantTyping;
        }

        public void setParticipantTyping(MegaChatParticipant participantTyping) {
            this.participantTyping = participantTyping;
        }

        public long getTimeStampTyping() {
            return timeStampTyping;
        }

        public void setTimeStampTyping(long timeStampTyping) {
            this.timeStampTyping = timeStampTyping;
        }
    }

    private BroadcastReceiver dialogConnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            log("Network broadcast received on chatActivity!");

            if (intent != null){
                showConfirmationConnect();
            }
        }
    };

    ArrayList<UserTyping> usersTyping;
    List<UserTyping> usersTypingSync;

    public void openMegaLink(String url, boolean isFile){
        log("openMegaLink");
        if(isFile){
            Intent openFileIntent = new Intent(this, FileLinkActivityLollipop.class);
            openFileIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            openFileIntent.setAction(Constants.ACTION_OPEN_MEGA_LINK);
            openFileIntent.setData(Uri.parse(url));
            startActivity(openFileIntent);
        }
        else{
            Intent openFolderIntent = new Intent(this, FolderLinkActivityLollipop.class);
            openFolderIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            openFolderIntent.setAction(Constants.ACTION_OPEN_MEGA_FOLDER_LINK);
            openFolderIntent.setData(Uri.parse(url));
            startActivity(openFolderIntent);
        }
    }

    public void showMessageInfo(int positionInAdapter){
        int position = positionInAdapter-1;

        if(position<messages.size()) {
            AndroidMegaChatMessage androidM = messages.get(position);
            StringBuilder messageToShow = new StringBuilder("");
            String token = FirebaseInstanceId.getInstance().getToken();
            if(token!=null){
                messageToShow.append("FCM TOKEN: " +token);
            }
            messageToShow.append("\nCHAT ID: " + MegaApiJava.userHandleToBase64(idChat));
            messageToShow.append("\nMY USER HANDLE: " +MegaApiJava.userHandleToBase64(megaChatApi.getMyUserHandle()));
            if(androidM!=null){
                MegaChatMessage m = androidM.getMessage();
                if(m!=null){
                    messageToShow.append("\nMESSAGE TYPE: " +m.getType());
                    messageToShow.append("\nMESSAGE TIMESTAMP: " +m.getTimestamp());
                    messageToShow.append("\nMESSAGE USERHANDLE: " +MegaApiJava.userHandleToBase64(m.getUserHandle()));
                    messageToShow.append("\nMESSAGE ID: " +MegaApiJava.userHandleToBase64(m.getMsgId()));
                    messageToShow.append("\nMESSAGE TEMP ID: " +MegaApiJava.userHandleToBase64(m.getTempId()));
                }
            }

            Toast.makeText(this, messageToShow, Toast.LENGTH_SHORT).show();
            log("showMessageInfo: "+messageToShow);
        }
    }

    public void showGroupInfoActivity(){
        log("showGroupInfoActivity");
        if(chatRoom.isGroup()){
            Intent i = new Intent(this, GroupChatInfoActivityLollipop.class);
            i.putExtra("handle", chatRoom.getChatId());
            this.startActivity(i);
        }
        else{
            Intent i = new Intent(this, ContactInfoActivityLollipop.class);
            i.putExtra("handle", chatRoom.getChatId());
            this.startActivity(i);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log("onCreate");
//        stopService(new Intent(this,IncomingCallService.class));
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        if (megaApi == null) {
            MegaApplication app = (MegaApplication) getApplication();
            megaApi = app.getMegaApi();
        }

        if (megaChatApi == null) {
            MegaApplication app = (MegaApplication) getApplication();
            megaChatApi = app.getMegaChatApi();
        }

        if(megaChatApi==null||megaChatApi.getInitState()==MegaChatApi.INIT_ERROR||megaChatApi.getInitState()==MegaChatApi.INIT_NOT_DONE){
            log("Refresh session - karere");
            Intent intent = new Intent(this, LoginActivityLollipop.class);
            intent.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return;
        }

        megaChatApi.addChatListener(this);
        megaChatApi.addChatCallListener(this);

        dbH = DatabaseHandler.getDbHandler(this);

        handler = new Handler();

        chatActivity = this;
        chatC = new ChatController(chatActivity);

        LocalBroadcastManager.getInstance(this).registerReceiver(dialogConnectReceiver, new IntentFilter(Constants.BROADCAST_ACTION_INTENT_CONNECTIVITY_CHANGE_DIALOG));

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.lollipop_dark_primary_color));

        setContentView(R.layout.activity_chat);
        display = getWindowManager().getDefaultDisplay();
        outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        density  = getResources().getDisplayMetrics().density;

        //Set toolbar
        tB = (Toolbar) findViewById(R.id.toolbar_chat);
        setSupportActionBar(tB);
        aB = getSupportActionBar();
        aB.setDisplayHomeAsUpEnabled(true);
        aB.setDisplayShowHomeEnabled(true);
        aB.setTitle(null);
        aB.setSubtitle(null);

        tB.setOnClickListener(this);
        toolbarElements = (LinearLayout) tB.findViewById(R.id.toolbar_elements);
        toolbarElementsInside = (RelativeLayout) findViewById(R.id.toolbar_elements_inside);
        titleToolbar = (TextView) tB.findViewById(R.id.title_toolbar);
        individualSubtitleToobar = (MarqueeTextView) tB.findViewById(R.id.individual_subtitle_toolbar);
        groupalSubtitleToolbar = (TextView) tB.findViewById(R.id.groupal_subtitle_toolbar);
        subtitleCall = (LinearLayout) tB.findViewById(R.id.subtitle_call);
        chronoCall = (Chronometer) tB.findViewById(R.id.chrono_call);
        participantsLayout = (LinearLayout) tB.findViewById(R.id.ll_participants);
        participantsText = (TextView) tB.findViewById(R.id.participants_text);
        iconStateToolbar = (ImageView) tB.findViewById(R.id.state_icon_toolbar);
        privateIconToolbar = (ImageView) tB.findViewById(R.id.private_icon_toolbar);

        titleToolbar.setText("");
        individualSubtitleToobar.setText("");
        individualSubtitleToobar.setVisibility(View.GONE);
        groupalSubtitleToolbar.setText("");
        groupalSubtitleToolbar.setVisibility(View.GONE);
        subtitleCall.setVisibility(View.GONE);
        chronoCall.setVisibility(View.GONE);
        participantsLayout.setVisibility(View.GONE);
        iconStateToolbar.setVisibility(View.GONE);
        privateIconToolbar.setVisibility(View.GONE);

        badgeDrawable = new BadgeDrawerArrowDrawable(getSupportActionBar().getThemedContext());
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        emptyLayout = (LinearLayout) findViewById(R.id.empty_messages_layout);
        emptyTextView = (TextView) findViewById(R.id.empty_text_chat_recent);
        emptyImageView = (ImageView) findViewById(R.id.empty_image_view_chat);

        updateNavigationToolbarIcon();

        fragmentContainer = (CoordinatorLayout) findViewById(R.id.fragment_container_chat);
        writingContainerLayout = (RelativeLayout) findViewById(R.id.writing_container_layout_chat_layout);

        joinChatLinkLayout = (RelativeLayout) findViewById(R.id.join_chat_layout_chat_layout);
        joinButton = (Button) findViewById(R.id.join_button);
        joinButton.setOnClickListener(this);

        messageJumpLayout = (RelativeLayout) findViewById(R.id.message_jump_layout);
        messageJumpText = (TextView) findViewById(R.id.message_jump_text);
        messageJumpLayout.setVisibility(View.GONE);

        writingLayout = (RelativeLayout) findViewById(R.id.writing_linear_layout_chat);
        disabledWritingLayout = (RelativeLayout) findViewById(R.id.writing_disabled_linear_layout_chat);

        linearLayoutTwemoji = (LinearLayout)findViewById(R.id.linear_layout_twemoji);
        rLKeyboardTwemojiButton = (RelativeLayout) findViewById(R.id.rl_keyboard_twemoji_chat);
        rLMediaButton = (RelativeLayout) findViewById(R.id.rl_media_icon_chat);
        rLSendContactButton = (RelativeLayout) findViewById(R.id.rl_send_contact_icon_chat);
        rLPickFileSystemButton = (RelativeLayout) findViewById(R.id.rl_pick_file_system_icon_chat);
        rLPickFileStorageButton = (RelativeLayout) findViewById(R.id.rl_pick_file_storage_icon_chat);
        rLPickCloudDriveButton = (RelativeLayout) findViewById(R.id.rl_pick_cloud_drive_icon_chat);

        keyboardTwemojiButton = (ImageButton) findViewById(R.id.keyboard_twemoji_chat);
        mediaButton = (ImageButton) findViewById(R.id.media_icon_chat);
        sendContactButton = (ImageButton) findViewById(R.id.send_contact_icon_chat);
        pickFileSystemButton = (ImageButton) findViewById(R.id.pick_file_system_icon_chat);
        pickFileStorageButton = (ImageButton) findViewById(R.id.pick_file_storage_icon_chat);
        pickCloudDriveButton = (ImageButton) findViewById(R.id.pick_cloud_drive_icon_chat);

        textChat = (EmojiEditText) findViewById(R.id.edit_text_chat);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            textChat.setEmojiSize(Util.scaleWidthPx(10, outMetrics));
        }else{
            textChat.setEmojiSize(Util.scaleWidthPx(20, outMetrics));
        }

        emojiKeyboard = (EmojiKeyboard)findViewById(R.id.emojiView);
        emojiKeyboard.init(this, textChat, keyboardTwemojiButton);
        handlerKeyboard = new Handler();
        handlerEmojiKeyboard = new Handler();

        callInProgressLayout = (RelativeLayout) findViewById(R.id.call_in_progress_layout);
        callInProgressLayout.setVisibility(View.GONE);
        callInProgressText = (TextView) findViewById(R.id.call_in_progress_text);
        callInProgressChrono = (Chronometer) findViewById(R.id.call_in_progress_chrono);
        callInProgressChrono.setVisibility(View.GONE);

        rLKeyboardTwemojiButton.setOnClickListener(this);
        rLMediaButton.setOnClickListener(this);
        rLSendContactButton.setOnClickListener(this);
        rLPickFileSystemButton.setOnClickListener(this);
        rLPickFileStorageButton.setOnClickListener(this);
        rLPickCloudDriveButton.setOnClickListener(this);

        keyboardTwemojiButton.setOnClickListener(this);
        mediaButton.setOnClickListener(this);
        sendContactButton.setOnClickListener(this);
        pickFileSystemButton.setOnClickListener(this);
        pickFileStorageButton.setOnClickListener(this);
        pickCloudDriveButton.setOnClickListener(this);

        messageJumpLayout.setOnClickListener(this);

        fragmentContainerFileStorage = (FrameLayout) findViewById(R.id.fragment_container_file_storage);
        fileStorageLayout = (RelativeLayout) findViewById(R.id.relative_layout_file_storage);
        fileStorageLayout.setVisibility(View.GONE);
        pickFileStorageButton.setImageResource(R.drawable.ic_b_select_image);

        observersLayout = (RelativeLayout) findViewById(R.id.observers_layout);
        observersNumberText = (TextView) findViewById(R.id.observers_text);

        textChat.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s != null) {
                    if (s.length() > 0) {
                        String temp = s.toString();
                        if(temp.trim().length()>0){
                            sendIcon.setEnabled(true);
                            sendIcon.setImageDrawable(ContextCompat.getDrawable(chatActivity, R.drawable.ic_send_black));

                            textChat.setHint(" ");
                            textChat.setMinLines(1);
                            textChat.setMaxLines(5);

                        }else{
                            sendIcon.setEnabled(false);
                            sendIcon.setImageDrawable(ContextCompat.getDrawable(chatActivity, R.drawable.ic_send_trans));
                            log("textChat:TextChangedListener:onTextChanged:lengthInvalid1:sendStopTypingNotification");
                            megaChatApi.sendStopTypingNotification(chatRoom.getChatId());

                            if(chatRoom.hasCustomTitle()){
                                textChat.setHint(getString(R.string.type_message_hint_with_customized_title, chatRoom.getTitle()));
                            }else{
                               textChat.setHint(getString(R.string.type_message_hint_with_default_title, chatRoom.getTitle()));
                            }

                            textChat.setMinLines(1);
                            textChat.setMaxLines(1);

                        }
                    }else {
                        sendIcon.setEnabled(false);
                        sendIcon.setImageDrawable(ContextCompat.getDrawable(chatActivity, R.drawable.ic_send_trans));
                        log("textChat:TextChangedListener:onTextChanged:lengthInvalid2:sendStopTypingNotification");
                        if (chatRoom != null) {
                            megaChatApi.sendStopTypingNotification(chatRoom.getChatId());

                            if (chatRoom.hasCustomTitle()) {
                                textChat.setHint(getString(R.string.type_message_hint_with_customized_title, chatRoom.getTitle()));
                            }
                            else {
                                textChat.setHint(getString(R.string.type_message_hint_with_default_title, chatRoom.getTitle()));
                            }
                        }
                        textChat.setMinLines(1);
                        textChat.setMaxLines(1);
                    }
                }else{
                    sendIcon.setEnabled(false);
                    sendIcon.setImageDrawable(ContextCompat.getDrawable(chatActivity, R.drawable.ic_send_trans));
                    log("textChat:TextChangedListener:onTextChanged:nullText:sendStopTypingNotification");
                    megaChatApi.sendStopTypingNotification(chatRoom.getChatId());

                    if(chatRoom.hasCustomTitle()){
                        textChat.setHint(getString(R.string.type_message_hint_with_customized_title, chatRoom.getTitle()));
                    }else{
                        textChat.setHint(getString(R.string.type_message_hint_with_default_title, chatRoom.getTitle()));
                    }

                    textChat.setMinLines(1);
                    textChat.setMaxLines(1);

                }

                if(getCurrentFocus() == textChat){
                    // is only executed if the EditText was directly changed by the user

                    if(sendIsTyping){
                        log("textChat:TextChangedListener:onTextChanged:sendIsTyping:sendTypingNotification");
                        sendIsTyping=false;
                        megaChatApi.sendTypingNotification(chatRoom.getChatId());

                        int interval = 4000;
                        Runnable runnable = new Runnable(){
                            public void run() {
                                sendIsTyping=true;
                            }
                        };
                        handlerSend = new Handler();
                        handlerSend.postDelayed(runnable, interval);
                    }

                    if(megaChatApi.isSignalActivityRequired()){
                        megaChatApi.signalPresenceActivity();
                    }
                }
                else{
                    log("textChat:TextChangedListener:onTextChanged:nonFocusTextChat:sendStopTypingNotification");
                    if (chatRoom != null) {
                        megaChatApi.sendStopTypingNotification(chatRoom.getChatId());
                    }
                }
            }
        });


        textChat.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Hide fileStorageLayout
                if(fileStorageLayout.isShown()){
                    hideFileStorageSection();
                }
                if(emojiKeyboard!=null){
                    emojiKeyboard.showLetterKeyboard();
                }
                return false;
            }
        });

        textChat.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                //Hide fileStorageLayout
                if(fileStorageLayout.isShown()){
                    hideFileStorageSection();
                }
                if(emojiKeyboard!=null) {
                    emojiKeyboard.showLetterKeyboard();
                }
                return false;
            }
        });

        textChat.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //Hide fileStorageLayout
                    if(fileStorageLayout.isShown()){
                        if(fileStorageF != null){
                            fileStorageF.clearSelections();
                            fileStorageF.hideMultipleSelect();
                        }
                        fileStorageLayout.setVisibility(View.GONE);
                    }
                    if(emojiKeyboard!=null){
                        emojiKeyboard.showLetterKeyboard();
                    }

                }
                return false;
            }
        });

        chatRelativeLayout  = (RelativeLayout) findViewById(R.id.relative_chat_layout);

        sendIcon = (ImageButton) findViewById(R.id.send_message_icon_chat);
        sendIcon.setOnClickListener(this);
        sendIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_send_trans));
        sendIcon.setEnabled(false);

        listView = (RecyclerView) findViewById(R.id.messages_chat_list_view);
        listView.setClipToPadding(false);;
        listView.setNestedScrollingEnabled(false);
        ((SimpleItemAnimator) listView.getItemAnimator()).setSupportsChangeAnimations(false);

        mLayoutManager = new NpaLinearLayoutManager(this);
        mLayoutManager.setStackFromEnd(true);
        listView.setLayoutManager(mLayoutManager);
//        listView.addOnItemTouchListener(this);

        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                // Get the first visible item

                if((messages!=null)&&(messages.size()>0)){
                    int lastPosition = messages.size()-1;
                    AndroidMegaChatMessage msg = messages.get(lastPosition);

                    while (!msg.isUploading() && msg.getMessage().getStatus() == MegaChatMessage.STATUS_SENDING_MANUAL) {
                        lastPosition--;
                        msg = messages.get(lastPosition);
                    }
                    if(lastPosition == (messages.size()-1)){
                        //Scroll to end
                        if((messages.size()-1) == (mLayoutManager.findLastVisibleItemPosition()-1)){
                            hideMessageJump();
                        }else if((messages.size()-1) > (mLayoutManager.findLastVisibleItemPosition()-1)){
                            if(newVisibility){
                                showJumpMessage();
                            }
                        }
                    }else{
                        lastPosition++;
                        if(lastPosition == (mLayoutManager.findLastVisibleItemPosition()-1)){
                            hideMessageJump();
                        }else if(lastPosition != (mLayoutManager.findLastVisibleItemPosition()-1)){
                            if(newVisibility){
                                showJumpMessage();
                            }
                        }
                    }
                }

                if(stateHistory!=MegaChatApi.SOURCE_NONE){
                    if (dy > 0) {
                        // Scrolling up
                        scrollingUp = true;
                    } else {
                        // Scrolling down
                        scrollingUp = false;
                    }

                    if(!scrollingUp){
                        int pos = mLayoutManager.findFirstVisibleItemPosition();
                        if(pos<=NUMBER_MESSAGES_BEFORE_LOAD&&getMoreHistory){
                            log("DE->loadMessages:scrolling up");
                            isLoadingHistory = true;
                            stateHistory = megaChatApi.loadMessages(idChat, NUMBER_MESSAGES_TO_LOAD);
                            positionToScroll = -1;
                            getMoreHistory = false;
                        }
                    }
                }
            }
        });

        messagesContainerLayout = (RelativeLayout) findViewById(R.id.message_container_chat_layout);

        userTypingLayout = (RelativeLayout) findViewById(R.id.user_typing_layout);
        userTypingLayout.setVisibility(View.GONE);
        userTypingText = (TextView) findViewById(R.id.user_typing_text);

        initAfterIntent(getIntent(), savedInstanceState);

        log("FINISH on Create");
    }

    public void initAfterIntent(Intent newIntent, Bundle savedInstanceState){
        log("initAfterIntent");

        if (newIntent != null){
            log("Intent is not null");
            intentAction = newIntent.getAction();
            if (intentAction != null){
                if (intentAction.equals(Constants.ACTION_OPEN_CHAT_LINK) || intentAction.equals(Constants.ACTION_JOIN_OPEN_CHAT_LINK)){
                    String link = newIntent.getDataString();
                    megaChatApi.openChatPreview(link, this);
                }
                else{
                    idChat = newIntent.getLongExtra("CHAT_ID", -1);

                    myUserHandle = megaChatApi.getMyUserHandle();

                    if(savedInstanceState!=null) {
                        log("Bundle is NOT NULL");
                        selectedMessageId = savedInstanceState.getLong("selectedMessageId", -1);
                        log("Handle of the message: "+selectedMessageId);
                        selectedPosition = savedInstanceState.getInt("selectedPosition", -1);
                        isHideJump = savedInstanceState.getBoolean("isHideJump",false);
                        typeMessageJump = savedInstanceState.getInt("typeMessageJump",-1);
                        visibilityMessageJump = savedInstanceState.getBoolean("visibilityMessageJump",false);
                        mOutputFilePath = savedInstanceState.getString("mOutputFilePath");
                        isShareLinkDialogDismissed = savedInstanceState.getBoolean("isShareLinkDialogDismissed", false);

                        if(visibilityMessageJump){
                            if(typeMessageJump == TYPE_MESSAGE_NEW_MESSAGE){
                                messageJumpText.setText(getResources().getString(R.string.message_new_messages));
                                messageJumpLayout.setVisibility(View.VISIBLE);
                            }else if(typeMessageJump == TYPE_MESSAGE_JUMP_TO_LEAST){
                                messageJumpText.setText(getResources().getString(R.string.message_jump_latest));
                                messageJumpLayout.setVisibility(View.VISIBLE);
                            }
                        }

                        lastIdMsgSeen = savedInstanceState.getLong("lastMessageSeen",-1);
                        if(lastIdMsgSeen != -1){
                            isTurn = true;
                        }

                        generalUnreadCount = savedInstanceState.getLong("generalUnreadCount",-1);
                    }

                    String text = null;
                    if (intentAction.equals(Constants.ACTION_CHAT_SHOW_MESSAGES)) {
                        log("ACTION_CHAT_SHOW_MESSAGES");
                        isOpeningChat = true;

                        int errorCode = newIntent.getIntExtra("PUBLIC_LINK", 1);
                        if (savedInstanceState == null) {
                            text = newIntent.getStringExtra("showSnackbar");
                            if (text == null) {
                                if (errorCode != 1) {
                                    if (errorCode == MegaChatError.ERROR_OK) {
                                        text = getString(R.string.chat_link_copied_clipboard);
                                    }
                                    else {
                                        log("initAfterIntent:publicLinkError:errorCode");
                                        text = getString(R.string.general_error) + ": " + errorCode;
                                    }
                                }
                            }
                        }
                        else if (errorCode != 1 && errorCode == MegaChatError.ERROR_OK && !isShareLinkDialogDismissed) {
                                text = getString(R.string.chat_link_copied_clipboard);
                        }

                    }
                    showChat(text);
                }
            }
        }
        else{
            log("INTENT is NULL");
        }
    }

    public void showChat(String textSnackbar){
        if(idChat!=-1) {
            //REcover chat
            log("Recover chat with id: " + idChat);
            chatRoom = megaChatApi.getChatRoom(idChat);
            if(chatRoom==null){
                log("Chatroom is NULL - finish activity!!");
                finish();
            }

            if(chatRoom.hasCustomTitle()){
                textChat.setHint(getString(R.string.type_message_hint_with_customized_title, chatRoom.getTitle()));
            }
            else{
                textChat.setHint(getString(R.string.type_message_hint_with_default_title, chatRoom.getTitle()));
            }

            textChat.setMinLines(1);
            textChat.setMaxLines(1);

            ChatItemPreferences prefs = dbH.findChatPreferencesByHandle(Long.toString(idChat));
            if(prefs!=null){
                String written = prefs.getWrittenText();
                if(written!=null && (!written.isEmpty())){
                    textChat.setText(written);
                }
            }
            else{
                prefs = new ChatItemPreferences(Long.toString(idChat), Boolean.toString(true), "");
                dbH.setChatItemPreferences(prefs);
            }

            megaChatApi.closeChatRoom(idChat, null);

            boolean result = megaChatApi.openChatRoom(idChat, this);
            log("Result of open chat: " + result);
            if(result){
                MegaApplication.setClosedChat(false);
            }

            if(!result){
                log("----Error on openChatRoom");
                if(errorOpenChatDialog==null){
                    android.support.v7.app.AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
                    }
                    else{
                        builder = new AlertDialog.Builder(this);
                    }
                    builder.setTitle(getString(R.string.chat_error_open_title));
                    builder.setMessage(getString(R.string.chat_error_open_message));

                    builder.setPositiveButton(getString(R.string.cam_sync_ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    finish();
                                }
                            }
                    );
                    errorOpenChatDialog = builder.create();
                    errorOpenChatDialog.show();
                }
            }
            else {
                int chatConnection = megaChatApi.getChatConnectionState(idChat);
                log("Chat connection (" + idChat + ") is: " + chatConnection);

                messages = new ArrayList<AndroidMegaChatMessage>();
                bufferMessages = new ArrayList<AndroidMegaChatMessage>();
                bufferManualSending = new ArrayList<AndroidMegaChatMessage>();
                bufferSending = new ArrayList<AndroidMegaChatMessage>();

                if (adapter == null) {
                    adapter = new MegaChatLollipopAdapter(this, chatRoom, messages, listView);
                    adapter.setHasStableIds(true);
                    listView.setAdapter(adapter);
                }

                setPreviewersView();

                titleToolbar.setText(chatRoom.getTitle());
                setChatSubtitle();

                if (!chatRoom.isPublic()) {
                    privateIconToolbar.setVisibility(View.VISIBLE);
                }
                else {
                    privateIconToolbar.setVisibility(View.GONE);
                }

                isOpeningChat = true;

                LinearLayout.LayoutParams emptyTextViewParams1 = (LinearLayout.LayoutParams) emptyImageView.getLayoutParams();

                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    emptyImageView.setImageResource(R.drawable.chat_empty_landscape);
                    emptyTextViewParams1.setMargins(0, Util.scaleHeightPx(40, outMetrics), 0, Util.scaleHeightPx(24, outMetrics));
                } else {
                    emptyImageView.setImageResource(R.drawable.ic_empty_chat_list);
                    emptyTextViewParams1.setMargins(0, Util.scaleHeightPx(100, outMetrics), 0, Util.scaleHeightPx(24, outMetrics));
                }

                emptyImageView.setLayoutParams(emptyTextViewParams1);

                String textToShowB = String.format(getString(R.string.chat_loading_messages));

                try {
                    textToShowB = textToShowB.replace("[A]", "<font color=\'#7a7a7a\'>");
                    textToShowB = textToShowB.replace("[/A]", "</font>");
                    textToShowB = textToShowB.replace("[B]", "<font color=\'#000000\'>");
                    textToShowB = textToShowB.replace("[/B]", "</font>");
                } catch (Exception e) {
                }
                Spanned resultB = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    resultB = Html.fromHtml(textToShowB, Html.FROM_HTML_MODE_LEGACY);
                } else {
                    resultB = Html.fromHtml(textToShowB);
                }

                emptyTextView.setText(resultB);
                emptyTextView.setVisibility(View.VISIBLE);
                emptyLayout.setVisibility(View.VISIBLE);

                chatRelativeLayout.setVisibility(View.GONE);

                if(textSnackbar!=null){
                    String chatLink = getIntent().getStringExtra("CHAT_LINK");
                    if (chatLink != null && !isShareLinkDialogDismissed) {
                        ChatUtil.showShareChatLinkDialog(this, chatRoom, chatLink);
                    }
                    else {
                        showSnackbar(Constants.SNACKBAR_TYPE, textSnackbar, -1);
                    }
                }

                loadHistory();
                log("On create: stateHistory: " + stateHistory);
            }
        }
        else{
            log("Chat ID -1 error");
        }

        log("FINISH on Create");
    }

    public void removeChatLink(){
        log("removeChatLink");
        megaChatApi.removeChatLink(idChat, this);
    }

    public void loadHistory(){
        log("loadHistory");

        isLoadingHistory = true;

        long unreadCount = chatRoom.getUnreadCount();
        if (unreadCount == 0) {
            if(!isTurn) {
                lastIdMsgSeen = -1;
                generalUnreadCount = -1;
                stateHistory = megaChatApi.loadMessages(idChat, NUMBER_MESSAGES_TO_LOAD);
                numberToLoad=NUMBER_MESSAGES_TO_LOAD;
            }else{
                if (generalUnreadCount < 0) {
                    log("loadHistory:A->loadMessages " + chatRoom.getUnreadCount());
                    long unreadAbs = Math.abs(generalUnreadCount);
                    numberToLoad =  (int) unreadAbs+NUMBER_MESSAGES_TO_LOAD;
                    stateHistory = megaChatApi.loadMessages(idChat, (int) numberToLoad);
                }
                else{
                    log("loadHistory:B->loadMessages " + chatRoom.getUnreadCount());
                    numberToLoad =  (int) generalUnreadCount+NUMBER_MESSAGES_TO_LOAD;
                    stateHistory = megaChatApi.loadMessages(idChat, (int) numberToLoad);
                }
            }
            lastSeenReceived = true;
            log("loadHistory:C->loadMessages:unread is 0");
        } else {
            if(!isTurn){
                lastIdMsgSeen = megaChatApi.getLastMessageSeenId(idChat);
                generalUnreadCount = unreadCount;
            }
            else{
                log("Do not change lastSeenId --> rotating screen");
            }

            if (lastIdMsgSeen != -1) {
                log("loadHistory:lastSeenId: " + lastIdMsgSeen);
            } else {
                log("loadHistory:Error:InvalidLastMessage");
            }

            lastSeenReceived = false;
            if (unreadCount < 0) {
                log("loadHistory:A->loadMessages " + chatRoom.getUnreadCount());
                long unreadAbs = Math.abs(unreadCount);
                numberToLoad =  (int) unreadAbs+NUMBER_MESSAGES_TO_LOAD;
                stateHistory = megaChatApi.loadMessages(idChat, (int) numberToLoad);
            }
            else{
                log("loadHistory:B->loadMessages " + chatRoom.getUnreadCount());
                numberToLoad =  (int) unreadCount+NUMBER_MESSAGES_TO_LOAD;
                stateHistory = megaChatApi.loadMessages(idChat, (int) numberToLoad);
            }
        }
        log("loadHistory:END:numberToLoad: "+numberToLoad);
    }

    void setSubtitleVisibility() {
        if (chatRoom.isGroup()) {
            individualSubtitleToobar.setVisibility(View.GONE);
            groupalSubtitleToolbar.setVisibility(View.VISIBLE);
            iconStateToolbar.setVisibility(View.GONE);
        }
        else {
            individualSubtitleToobar.setVisibility(View.VISIBLE);
            groupalSubtitleToolbar.setVisibility(View.GONE);
        }
        subtitleCall.setVisibility(View.GONE);
    }

    void setPreviewGroupalSubtitle () {
        long participants = chatRoom.getPeerCount();
        if (participants > 0) {
            groupalSubtitleToolbar.setVisibility(View.VISIBLE);
            groupalSubtitleToolbar.setText(adjustForLargeFont(getString(R.string.number_of_participants, participants)));
        }
        else {
            groupalSubtitleToolbar.setVisibility(View.GONE);
        }
    }

    public void setChatSubtitle(){
        log("setChatSubtitle");
        if(chatRoom==null){
            return;
        }

        setSubtitleVisibility();

        if (chatC.isInAnonymousMode() && megaChatApi.getChatConnectionState(idChat)==MegaChatApi.CHAT_CONNECTION_ONLINE) {
            log("setChatSubtitle:isPreview");
            setPreviewGroupalSubtitle();
            tB.setOnClickListener(this);

            setBottomLayout(SHOW_JOIN_LAYOUT);
        }
        else if(megaChatApi.getConnectionState()!=MegaChatApi.CONNECTED||megaChatApi.getChatConnectionState(idChat)!=MegaChatApi.CHAT_CONNECTION_ONLINE){
            log("Chat not connected ConnectionState: "+megaChatApi.getConnectionState()+" ChatConnectionState: "+megaChatApi.getChatConnectionState(idChat));

            if(chatRoom.isPreview()){
                log("Chat not connected:setChatSubtitle:isPreview");
                setPreviewGroupalSubtitle();
                tB.setOnClickListener(this);

                setBottomLayout(SHOW_NOTHING_LAYOUT);
                return;
            }
            if (chatRoom.isGroup()) {
                groupalSubtitleToolbar.setText(adjustForLargeFont(getString(R.string.invalid_connection_state)));
            }
            else {
                individualSubtitleToobar.setText(adjustForLargeFont(getString(R.string.invalid_connection_state)));
            }

            tB.setOnClickListener(null);

        }
        else{
            log("karere connection state: "+megaChatApi.getConnectionState());
            log("chat connection state: "+megaChatApi.getChatConnectionState(idChat));

            int permission = chatRoom.getOwnPrivilege();

            if (chatRoom.isGroup()) {
                tB.setOnClickListener(this);
                if(chatRoom.isPreview()){
                    log("setChatSubtitle:isPreview");
                    setPreviewGroupalSubtitle();
                    tB.setOnClickListener(this);

                    if (getIntent() != null && getIntent().getAction() != null && getIntent().getAction().equals(Constants.ACTION_JOIN_OPEN_CHAT_LINK)) {
                        setBottomLayout(SHOW_NOTHING_LAYOUT);
                    }
                    else {
                        setBottomLayout(SHOW_JOIN_LAYOUT);
                    }

                    return;
                }
                else {
                    log("Check permissions group chat");
                    if (permission == MegaChatRoom.PRIV_RO) {
                        log("Permission RO");
                        setBottomLayout(SHOW_NOTHING_LAYOUT);

                        if (chatRoom.isArchived()) {
                            log("Chat is archived");
                            groupalSubtitleToolbar.setText(adjustForLargeFont(getString(R.string.archived_chat)));
                        }
                        else {
                            groupalSubtitleToolbar.setText(adjustForLargeFont(getString(R.string.observer_permission_label_participants_panel)));
                        }
                    }
                    else if (permission == MegaChatRoom.PRIV_RM) {
                        log("Permission RM");
                        setBottomLayout(SHOW_NOTHING_LAYOUT);

                        if (chatRoom.isArchived()) {
                            log("Chat is archived");
                            groupalSubtitleToolbar.setText(adjustForLargeFont(getString(R.string.archived_chat)));
                        }
                        else if (!chatRoom.isActive()) {
                            groupalSubtitleToolbar.setText(adjustForLargeFont(getString(R.string.inactive_chat)));
                        }
                        else {
                            groupalSubtitleToolbar.setText(null);
                            groupalSubtitleToolbar.setVisibility(View.GONE);
                        }
                    }
                    else{
                        log("permission: "+permission);
                        setBottomLayout(SHOW_WRITING_LAYOUT);

                        if(chatRoom.isArchived()){
                            log("Chat is archived");
                            groupalSubtitleToolbar.setText(adjustForLargeFont(getString(R.string.archived_chat)));
                        }
                        else if(chatRoom.hasCustomTitle()){
                            setCustomSubtitle();
                        }
                        else{
                            long participantsLabel = chatRoom.getPeerCount()+1; //Add one to include me
                            groupalSubtitleToolbar.setText(adjustForLargeFont(getResources().getQuantityString(R.plurals.subtitle_of_group_chat, (int) participantsLabel, participantsLabel)));
                        }
                    }
                }
            }
            else{
                log("Check permissions one to one chat");
                if(permission==MegaChatRoom.PRIV_RO) {
                    log("Permission RO");

                    if(megaApi!=null){
                        if(megaApi.getRootNode()!=null){
                            long chatHandle = chatRoom.getChatId();
                            MegaChatRoom chat = megaChatApi.getChatRoom(chatHandle);
                            long userHandle = chat.getPeerHandle(0);
                            String userHandleEncoded = MegaApiAndroid.userHandleToBase64(userHandle);
                            MegaUser user = megaApi.getContact(userHandleEncoded);

                            if(user!=null && user.getVisibility() == MegaUser.VISIBILITY_VISIBLE){
                                tB.setOnClickListener(this);
                            }
                            else{
                                tB.setOnClickListener(null);
                            }
                        }
                    }
                    else{
                        tB.setOnClickListener(null);
                    }

                    setBottomLayout(SHOW_NOTHING_LAYOUT);

                    if(chatRoom.isArchived()){
                        log("Chat is archived");
                        individualSubtitleToobar.setText(adjustForLargeFont(getString(R.string.archived_chat)));
                    }
                    else{
                        individualSubtitleToobar.setText(adjustForLargeFont(getString(R.string.observer_permission_label_participants_panel)));
                    }
                }
                else if(permission==MegaChatRoom.PRIV_RM) {
                    tB.setOnClickListener(this);

                    log("Permission RM");
                    setBottomLayout(SHOW_NOTHING_LAYOUT);

                    if(chatRoom.isArchived()){
                        log("Chat is archived");
                        individualSubtitleToobar.setText(adjustForLargeFont(getString(R.string.archived_chat)));
                    }
                    else if(!chatRoom.isActive()){
                        individualSubtitleToobar.setText(adjustForLargeFont(getString(R.string.inactive_chat)));
                    }
                    else{
                        individualSubtitleToobar.setText(null);
                        individualSubtitleToobar.setVisibility(View.GONE);
                    }
                }
                else{
                    tB.setOnClickListener(this);

                    long userHandle = chatRoom.getPeerHandle(0);
                    setStatus(userHandle);
                    setBottomLayout(SHOW_WRITING_LAYOUT);
                }
            }
        }
    }

    public void setBottomLayout(int show) {
        log("setBottomLayout");

        if (show == SHOW_JOIN_LAYOUT) {
            writingContainerLayout.setVisibility(View.GONE);
            joinChatLinkLayout.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) messagesContainerLayout.getLayoutParams();
            params.addRule(RelativeLayout.ABOVE, R.id.join_chat_layout_chat_layout);
            messagesContainerLayout.setLayoutParams(params);
        }
        else if (show == SHOW_NOTHING_LAYOUT) {
            writingContainerLayout.setVisibility(View.GONE);
            joinChatLinkLayout.setVisibility(View.GONE);
        }
        else {
            writingContainerLayout.setVisibility(View.VISIBLE);
            joinChatLinkLayout.setVisibility(View.GONE);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) messagesContainerLayout.getLayoutParams();
            params.addRule(RelativeLayout.ABOVE, R.id.writing_container_layout_chat_layout);
            messagesContainerLayout.setLayoutParams(params);
        }
    }

    public void setCustomSubtitle(){
        log("setCustomSubtitle");

        long participantsCount = chatRoom.getPeerCount();
        StringBuilder customSubtitle = new StringBuilder("");
        for(int i=0;i<participantsCount;i++) {

            if(i!=0){
                customSubtitle.append(", ");
            }

            String participantName = chatRoom.getPeerFirstname(i);
            if(participantName==null){
                //Get the lastname
                String participantLastName = chatRoom.getPeerLastname(i);
                if(participantLastName==null){
                    //Get the email
                    String participantEmail = chatRoom.getPeerEmail(i);
                    customSubtitle.append(participantEmail);
                }
                else{
                    if(participantLastName.trim().isEmpty()){
                        //Get the email
                        String participantEmail = chatRoom.getPeerEmail(i);
                        customSubtitle.append(participantEmail);
                    }
                    else{
                        //Append last name to the title
                        customSubtitle.append(participantLastName);
                    }
                }
            }
            else{
                if(participantName.trim().isEmpty()){
                    //Get the lastname
                    String participantLastName = chatRoom.getPeerLastname(i);
                    if(participantLastName==null){
                        //Get the email
                        String participantEmail = chatRoom.getPeerEmail(i);
                        customSubtitle.append(participantEmail);
                    }
                    else{
                        if(participantLastName.trim().isEmpty()){
                            //Get the email
                            String participantEmail = chatRoom.getPeerEmail(i);
                            customSubtitle.append(participantEmail);
                        }
                        else{
                            //Append last name to the title
                            customSubtitle.append(participantLastName);
                        }
                    }
                }
                else{
                    //Append first name to the title
                    customSubtitle.append(participantName);
                }
            }
        }
        if (customSubtitle.toString().trim().isEmpty()){
            groupalSubtitleToolbar.setText(null);
            groupalSubtitleToolbar.setVisibility(View.GONE);
        }
        else {
            groupalSubtitleToolbar.setText(adjustForLargeFont(customSubtitle.toString()));
        }
    }

    public void setLastGreen(String date){
        individualSubtitleToobar.setText(date);
        individualSubtitleToobar.isMarqueeIsNecessary(this);
        if(subtitleCall.getVisibility()!=View.VISIBLE && groupalSubtitleToolbar.getVisibility()!=View.VISIBLE){
            individualSubtitleToobar.setVisibility(View.VISIBLE);
        }
    }

    public void requestLastGreen(int state){
        log("requestLastGreen: "+state);

        if(chatRoom!=null && !chatRoom.isGroup() && !chatRoom.isArchived()){
            if(state == INITIAL_PRESENCE_STATUS){
                state = megaChatApi.getUserOnlineStatus(chatRoom.getPeerHandle(0));
            }

            if(state != MegaChatApi.STATUS_ONLINE && state != MegaChatApi.STATUS_BUSY && state != MegaChatApi.STATUS_INVALID){
                log("Request last green for user");
                megaChatApi.requestLastGreen(chatRoom.getPeerHandle(0), this);
            }
        }
    }

    public void setStatus(long userHandle){

        iconStateToolbar.setVisibility(View.GONE);

        if(megaChatApi.getConnectionState()!=MegaChatApi.CONNECTED){
            log("Chat not connected");
            individualSubtitleToobar.setText(adjustForLargeFont(getString(R.string.invalid_connection_state)));
        }
        else if(chatRoom.isArchived()){
            log("Chat is archived");
            individualSubtitleToobar.setText(adjustForLargeFont(getString(R.string.archived_chat)));
        }
        else if(!chatRoom.isGroup()){
            int state = megaChatApi.getUserOnlineStatus(userHandle);

            if(state == MegaChatApi.STATUS_ONLINE){
                log("This user is connected");
                individualSubtitleToobar.setText(adjustForLargeFont(getString(R.string.online_status)));
                iconStateToolbar.setVisibility(View.VISIBLE);
                iconStateToolbar.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_online));

            }
            else if(state == MegaChatApi.STATUS_AWAY){
                log("This user is away");
                individualSubtitleToobar.setText(adjustForLargeFont(getString(R.string.away_status)));
                iconStateToolbar.setVisibility(View.VISIBLE);
                iconStateToolbar.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_away));

            }
            else if(state == MegaChatApi.STATUS_BUSY){
                log("This user is busy");
                individualSubtitleToobar.setText(adjustForLargeFont(getString(R.string.busy_status)));
                iconStateToolbar.setVisibility(View.VISIBLE);
                iconStateToolbar.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_busy));

            }
            else if(state == MegaChatApi.STATUS_OFFLINE){
                log("This user is offline");
                individualSubtitleToobar.setText(adjustForLargeFont(getString(R.string.offline_status)));
                iconStateToolbar.setVisibility(View.VISIBLE);
                iconStateToolbar.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_offline));

            }
            else if(state == MegaChatApi.STATUS_INVALID){
                log("INVALID status: "+state);
                individualSubtitleToobar.setText(null);
                individualSubtitleToobar.setVisibility(View.GONE);
            }
            else{
                log("This user status is: "+state);
                individualSubtitleToobar.setText(null);
                individualSubtitleToobar.setVisibility(View.GONE);
            }
        }
    }

    public int compareTime(AndroidMegaChatMessage message, AndroidMegaChatMessage previous){
        return compareTime(message.getMessage().getTimestamp(), previous.getMessage().getTimestamp());
    }

    public int compareTime(long timeStamp, AndroidMegaChatMessage previous){
        return compareTime(timeStamp, previous.getMessage().getTimestamp());
    }

    public int compareTime(long timeStamp, long previous){
        if(previous!=-1){

            Calendar cal = Util.calculateDateFromTimestamp(timeStamp);
            Calendar previousCal =  Util.calculateDateFromTimestamp(previous);

            TimeUtils tc = new TimeUtils(TimeUtils.TIME);

            int result = tc.compare(cal, previousCal);
            log("RESULTS compareTime: "+result);
            return result;
        }
        else{
            log("return -1");
            return -1;
        }
    }

    public int compareDate(AndroidMegaChatMessage message, AndroidMegaChatMessage previous){
        return compareDate(message.getMessage().getTimestamp(), previous.getMessage().getTimestamp());
    }

    public int compareDate(long timeStamp, AndroidMegaChatMessage previous){
        return compareDate(timeStamp, previous.getMessage().getTimestamp());
    }

    public int compareDate(long timeStamp, long previous){
        log("compareDate");

        if(previous!=-1){
            Calendar cal = Util.calculateDateFromTimestamp(timeStamp);
            Calendar previousCal =  Util.calculateDateFromTimestamp(previous);

            TimeUtils tc = new TimeUtils(TimeUtils.DATE);

            int result = tc.compare(cal, previousCal);
            log("RESULTS compareDate: "+result);
            return result;
        }
        else{
            log("return -1");
            return -1;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        log("onCreateOptionsMenuLollipop");
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_action, menu);

        callMenuItem = menu.findItem(R.id.cab_menu_call_chat);
        videoMenuItem = menu.findItem(R.id.cab_menu_video_chat);
        inviteMenuItem = menu.findItem(R.id.cab_menu_invite_chat);
        clearHistoryMenuItem = menu.findItem(R.id.cab_menu_clear_history_chat);
        contactInfoMenuItem = menu.findItem(R.id.cab_menu_contact_info_chat);
        leaveMenuItem = menu.findItem(R.id.cab_menu_leave_chat);
        archiveMenuItem = menu.findItem(R.id.cab_menu_archive_chat);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        log("onPrepareOptionsMenu");

        if(chatRoom!=null){
            log("onPrepareOptionsMenu chatRoom!=null");
            callMenuItem.setVisible(megaChatApi.areGroupChatCallEnabled());
            callMenuItem.setEnabled(false);
            callMenuItem.setIcon(Util.mutateIcon(this, R.drawable.ic_phone_white, R.color.white_50_opacity));
            if (chatRoom.isGroup()) {
                videoMenuItem.setVisible(false);
            }else{
                videoMenuItem.setVisible(megaChatApi.areGroupChatCallEnabled());
                videoMenuItem.setEnabled(false);
                videoMenuItem.setIcon(Util.mutateIcon(this, R.drawable.ic_videocam_white, R.color.white_50_opacity));
            }


            if ((chatRoom.isPreview()) || (megaChatApi.getConnectionState() != MegaChatApi.CONNECTED)
                        || (megaChatApi.getChatConnectionState(idChat) != MegaChatApi.CHAT_CONNECTION_ONLINE)) {
                log("onPrepareOptionsMenu chatRoom.isPreview || megaChatApi.getConnectionState() "+megaChatApi.getConnectionState()+" || megaChatApi.getChatConnectionState(idChat) "+(megaChatApi.getChatConnectionState(idChat)));

                leaveMenuItem.setVisible(false);
                clearHistoryMenuItem.setVisible(false);
                inviteMenuItem.setVisible(false);
                contactInfoMenuItem.setVisible(false);
                archiveMenuItem.setVisible(false);
            }else {
                if(megaChatApi.getNumCalls() <= 0){
                    callMenuItem.setVisible(megaChatApi.areGroupChatCallEnabled());
                    callMenuItem.setEnabled(true);
                    callMenuItem.setIcon(Util.mutateIcon(this, R.drawable.ic_phone_white, R.color.background_chat));

                    if (chatRoom.isGroup()) {
                        videoMenuItem.setVisible(false);
                    }else{
                        videoMenuItem.setVisible(megaChatApi.areGroupChatCallEnabled());
                        videoMenuItem.setEnabled(true);
                        videoMenuItem.setIcon(Util.mutateIcon(this, R.drawable.ic_videocam_white, R.color.background_chat));
                    }

                }else{
                    if( (megaChatApi!=null) && (!ChatUtil.participatingInACall(megaChatApi)) && (!megaChatApi.hasCallInChatRoom(chatRoom.getChatId()))){
                        callMenuItem.setVisible(megaChatApi.areGroupChatCallEnabled());
                        callMenuItem.setEnabled(true);
                        callMenuItem.setIcon(Util.mutateIcon(this, R.drawable.ic_phone_white, R.color.background_chat));

                        if (chatRoom.isGroup()) {
                            videoMenuItem.setVisible(false);
                        }else{
                            videoMenuItem.setVisible(megaChatApi.areGroupChatCallEnabled());
                            videoMenuItem.setEnabled(true);
                            videoMenuItem.setIcon(Util.mutateIcon(this, R.drawable.ic_videocam_white, R.color.background_chat));
                        }
                    }
                }

                archiveMenuItem.setVisible(true);
                if(chatRoom.isArchived()){
                    archiveMenuItem.setTitle(getString(R.string.general_unarchive));
                }
                else{
                    archiveMenuItem.setTitle(getString(R.string.general_archive));
                }

                int permission = chatRoom.getOwnPrivilege();
                log("Permission in the chat: " + permission);
                if (chatRoom.isGroup()) {

                    if (permission == MegaChatRoom.PRIV_MODERATOR) {

                        inviteMenuItem.setVisible(true);

                        int lastMessageIndex = messages.size() - 1;
                        if (lastMessageIndex >= 0) {
                            AndroidMegaChatMessage lastMessage = messages.get(lastMessageIndex);
                            if (!lastMessage.isUploading()) {
                                if (lastMessage.getMessage().getType() == MegaChatMessage.TYPE_TRUNCATE) {
                                    log("Last message is TRUNCATE");
                                    clearHistoryMenuItem.setVisible(false);
                                } else {
                                    log("Last message is NOT TRUNCATE");
                                    clearHistoryMenuItem.setVisible(true);
                                }
                            } else {
                                log("Last message is UPLOADING");
                                clearHistoryMenuItem.setVisible(true);
                            }
                        }
                        else {
                            clearHistoryMenuItem.setVisible(false);
                        }

                        leaveMenuItem.setVisible(true);
                    } else if (permission == MegaChatRoom.PRIV_RM) {
                        log("Group chat PRIV_RM");
                        leaveMenuItem.setVisible(false);
                        clearHistoryMenuItem.setVisible(false);
                        inviteMenuItem.setVisible(false);
                        callMenuItem.setVisible(false);
                        videoMenuItem.setVisible(false);
                    } else if (permission == MegaChatRoom.PRIV_RO) {
                        log("Group chat PRIV_RO");
                        leaveMenuItem.setVisible(true);
                        clearHistoryMenuItem.setVisible(false);
                        inviteMenuItem.setVisible(false);
                        callMenuItem.setVisible(false);
                        videoMenuItem.setVisible(false);
                    } else if(permission == MegaChatRoom.PRIV_STANDARD){
                        log("Group chat PRIV_STANDARD");
                        leaveMenuItem.setVisible(true);
                        clearHistoryMenuItem.setVisible(false);
                        inviteMenuItem.setVisible(false);
                    }else{
                        log("Permission: " + permission);
                        leaveMenuItem.setVisible(true);
                        clearHistoryMenuItem.setVisible(false);
                        inviteMenuItem.setVisible(false);
                    }

                    contactInfoMenuItem.setTitle(getString(R.string.group_chat_info_label));
                    contactInfoMenuItem.setVisible(true);
                }
                else {
                    inviteMenuItem.setVisible(false);
                    if (permission == MegaChatRoom.PRIV_RO) {
                        clearHistoryMenuItem.setVisible(false);
                        contactInfoMenuItem.setVisible(false);
                        callMenuItem.setVisible(false);
                        videoMenuItem.setVisible(false);
                    } else {
                        clearHistoryMenuItem.setVisible(true);
                        contactInfoMenuItem.setTitle(getString(R.string.contact_properties_activity));
                        contactInfoMenuItem.setVisible(true);
                    }
                    leaveMenuItem.setVisible(false);
                }
            }

        }else{
            log("Chatroom NULL on create menu");
            leaveMenuItem.setVisible(false);
            callMenuItem.setVisible(false);
            videoMenuItem.setVisible(false);
            clearHistoryMenuItem.setVisible(false);
            inviteMenuItem.setVisible(false);
            contactInfoMenuItem.setVisible(false);
            archiveMenuItem.setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    void ifAnonymousModeLogin(boolean pendingJoin) {
        if(chatC.isInAnonymousMode()){
            Intent loginIntent = new Intent(this, LoginActivityLollipop.class);
            loginIntent.putExtra("visibleFragment", Constants. LOGIN_FRAGMENT);
            if (pendingJoin && getIntent() != null && getIntent().getDataString() != null) {
                loginIntent.setAction(Constants.ACTION_JOIN_OPEN_CHAT_LINK);
                loginIntent.setData(Uri.parse(getIntent().getDataString()));
                loginIntent.putExtra("idChatToJoin", idChat);
                closeChat(true);
            }
            startActivity(loginIntent);
        }
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        log("onOptionsItemSelected");

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home: {
                closeChat(true);
                ifAnonymousModeLogin(false);
                break;
            }
            case R.id.cab_menu_call_chat:{
                log("cab_menu_call_chat");
                startVideo = false;
                if(checkPermissionsCall()){
                    startCall();
                }
                break;
            }
            case R.id.cab_menu_video_chat:{
                log("cab_menu_video_chat");
                startVideo = true;
                if(checkPermissionsCall()){
                    startCall();
                }
                break;
            }
            case R.id.cab_menu_invite_chat:{
                chooseAddParticipantDialog();
                break;
            }
            case R.id.cab_menu_contact_info_chat:{
                if(chatRoom.isGroup()){
                    Intent i = new Intent(this, GroupChatInfoActivityLollipop.class);
                    i.putExtra("handle", chatRoom.getChatId());
                    this.startActivity(i);
                }
                else{
                    Intent i = new Intent(this, ContactInfoActivityLollipop.class);
                    i.putExtra("handle", chatRoom.getChatId());
                    this.startActivity(i);
                }
                break;
            }
            case R.id.cab_menu_clear_history_chat:{
                log("Clear history selected!");
                showConfirmationClearChat(chatRoom);
                break;
            }
            case R.id.cab_menu_leave_chat:{
                log("Leave selected!");
                showConfirmationLeaveChat(chatRoom);
                break;
            }
            case R.id.cab_menu_archive_chat:{
                log("Archive/unarchive selected!");
                ChatController chatC = new ChatController(chatActivity);
                chatC.archiveChat(chatRoom);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void startCall(){
        log("startCall ");
        if(megaChatApi != null){
            MegaChatCall callInThisChat = megaChatApi.getChatCall(chatRoom.getChatId());
            if(callInThisChat != null){
                log("startCall there is a call in this chat");

                if((megaChatApi!=null)&&(ChatUtil.participatingInACall(megaChatApi))){
                    long chatIdCallInProgress = ChatUtil.getChatCallInProgress(megaChatApi);
                    if(chatIdCallInProgress == chatRoom.getChatId()){
                        log("startCall:I'm in this call");
                        ChatUtil.returnCall(this, megaChatApi);
                    }else{
                        log("startCall:I'm in other call");
                        showConfirmationToJoinCall(chatRoom);
                    }

                }else{
                    if(callInThisChat.getStatus() == MegaChatCall.CALL_STATUS_RING_IN){
                        log("startCall:I'm not in a call, this call is ring in");
                        MegaApplication.setShowPinScreen(false);
                        Intent intent = new Intent(this, ChatCallActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("chatHandle", idChat);
                        startActivity(intent);

                    }else if (callInThisChat.getStatus() == MegaChatCall.CALL_STATUS_USER_NO_PRESENT){
                        log("startCall:I'm not in a call, this call is user no present");
                        megaChatApi.startChatCall(idChat, startVideo, this);
                    }
                }
            }else{
                if((megaChatApi!=null)&&(!ChatUtil.participatingInACall(megaChatApi))){
                    log("startCall there is not a call in this chat and i'm not in other call");
                    megaChatApi.startChatCall(idChat, startVideo, this);
                }
            }
        }
    }

    private boolean checkPermissionsCall(){
        log("checkPermissionsCall() ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            boolean hasCameraPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
            if (!hasCameraPermission) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Constants.REQUEST_CAMERA);
                return false;
            }

            boolean hasRecordAudioPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
            if (!hasRecordAudioPermission) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, Constants.RECORD_AUDIO);
                return false;
            }

            return true;
        }
        return true;
    }

    private boolean checkPermissionsTakePicture(){
        log("checkPermissionsCall");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            boolean hasCameraPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
            if (!hasCameraPermission) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Constants.REQUEST_CAMERA);
                return false;
            }

            boolean hasRecordAudioPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            if (!hasRecordAudioPermission) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.REQUEST_WRITE_STORAGE);
                return false;
            }

            return true;
        }
        return true;
    }

    private boolean checkPermissionsReadStorage(){
        log("checkPermissionsReadStorage");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            boolean hasReadStoragePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            if (!hasReadStoragePermission) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.REQUEST_READ_STORAGE);
                return false;
            }

            return true;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        log("onRequestPermissionsResult");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.REQUEST_CAMERA: {
                log("REQUEST_CAMERA");
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(!isTakePicture){
                        if(checkPermissionsCall()){
                            startCall();
                        }
                    }
                    else{
                        if(checkPermissionsTakePicture()){
                            takePicture();
                        }
                    }
                }
                break;
            }
            case Constants.RECORD_AUDIO: {
                log("RECORD_AUDIO");
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(checkPermissionsCall()){
                        startCall();
                    }
                }
                break;
            }
            case Constants.REQUEST_WRITE_STORAGE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(checkPermissionsTakePicture()){
                        takePicture();
                    }
                }
                break;
            }
            case Constants.REQUEST_READ_STORAGE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(checkPermissionsReadStorage()){
                        this.attachFromFileStorage();
                    }
                }
                break;
            }
        }
    }

    public void chooseAddParticipantDialog(){
        log("chooseAddContactDialog");

        if(megaApi!=null && megaApi.getRootNode()!=null){
            ArrayList<MegaUser> contacts = megaApi.getContacts();
            if(contacts==null){
                showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.no_contacts_invite), -1);
            }
            else {
                if(contacts.isEmpty()){
                    showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.no_contacts_invite), -1);
                }
                else{
                    Intent in = new Intent(this, AddContactActivityLollipop.class);
                    in.putExtra("contactType", Constants.CONTACT_TYPE_MEGA);
                    in.putExtra("chat", true);
                    in.putExtra("chatId", idChat);
                    in.putExtra("aBtitle", getString(R.string.add_participants_menu_item));
                    startActivityForResult(in, Constants.REQUEST_ADD_PARTICIPANTS);
                }
            }
        }
        else{
            log("Online but not megaApi");
            Util.showErrorAlertDialog(getString(R.string.error_server_connection_problem), false, this);
        }
    }

    public void chooseContactsDialog(){
        log("chooseContactsDialog");

        if(megaApi!=null && megaApi.getRootNode()!=null){
            ArrayList<MegaUser> contacts = megaApi.getContacts();
            if(contacts==null){
                showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.no_contacts_invite), -1);
            }
            else {
                if(contacts.isEmpty()){
                    showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.no_contacts_invite), -1);
                }
                else{
                    Intent in = new Intent(this, AddContactActivityLollipop.class);
                    in.putExtra("contactType", Constants.CONTACT_TYPE_MEGA);
                    in.putExtra("chat", true);
                    in.putExtra("aBtitle", getString(R.string.add_contacts));
                    startActivityForResult(in, Constants.REQUEST_SEND_CONTACTS);
                }
            }
        }
        else{
            log("Online but not megaApi");
            Util.showErrorAlertDialog(getString(R.string.error_server_connection_problem), false, this);
        }
    }

    public void disablePinScreen(){
        log("disablePinScreen");
        MegaApplication.setShowPinScreen(false);
    }

    public void showProgressForwarding(){
        log("showProgressForwarding");

        statusDialog = new ProgressDialog(this);
        statusDialog.setMessage(getString(R.string.general_forwarding));
        statusDialog.show();
    }

    public void forwardMessages(ArrayList<AndroidMegaChatMessage> messagesSelected){
        log("forwardMessages");
        chatC.prepareAndroidMessagesToForward(messagesSelected, idChat);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        log("onActivityResult, resultCode: " + resultCode);
        if (requestCode == Constants.REQUEST_ADD_PARTICIPANTS && resultCode == RESULT_OK) {
            if (intent == null) {
                log("Return.....");
                return;
            }

            final ArrayList<String> contactsData = intent.getStringArrayListExtra(AddContactActivityLollipop.EXTRA_CONTACTS);
            MultipleGroupChatRequestListener multipleListener = null;

            if (contactsData != null) {

                if (contactsData.size() == 1) {
                    MegaUser user = megaApi.getContact(contactsData.get(0));
                    if (user != null) {
                        megaChatApi.inviteToChat(chatRoom.getChatId(), user.getHandle(), MegaChatPeerList.PRIV_STANDARD, this);
                    }
                } else {
                    log("Add multiple participants "+contactsData.size());
                    multipleListener = new MultipleGroupChatRequestListener(this);
                    for (int i = 0; i < contactsData.size(); i++) {
                        MegaUser user = megaApi.getContact(contactsData.get(i));
                        if (user != null) {
                            megaChatApi.inviteToChat(chatRoom.getChatId(), user.getHandle(), MegaChatPeerList.PRIV_STANDARD, multipleListener);
                        }
                    }
                }
            }
        }
        else if (requestCode == Constants.REQUEST_CODE_SELECT_IMPORT_FOLDER && resultCode == RESULT_OK) {
            if(!Util.isOnline(this) || megaApi==null) {
                removeProgressDialog();
                showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.error_server_connection_problem), -1);
                return;
            }

            final long toHandle = intent.getLongExtra("IMPORT_TO", 0);

            final long[] importMessagesHandles = intent.getLongArrayExtra("HANDLES_IMPORT_CHAT");

            importNodes(toHandle, importMessagesHandles);
        }
        else if (requestCode == Constants.REQUEST_SEND_CONTACTS && resultCode == RESULT_OK) {
            final ArrayList<String> contactsData = intent.getStringArrayListExtra(AddContactActivityLollipop.EXTRA_CONTACTS);
            if (contactsData != null) {
                MegaHandleList handleList = MegaHandleList.createInstance();
                for(int i=0; i<contactsData.size();i++){
                    MegaUser user = megaApi.getContact(contactsData.get(i));
                    if (user != null) {
                        handleList.addMegaHandle(user.getHandle());

                    }
                }
                MegaChatMessage contactMessage = megaChatApi.attachContacts(idChat, handleList);
                if(contactMessage!=null){
                    AndroidMegaChatMessage androidMsgSent = new AndroidMegaChatMessage(contactMessage);
                    sendMessageToUI(androidMsgSent);
                }
            }
        }
        else if (requestCode == Constants.REQUEST_CODE_SELECT_FILE && resultCode == RESULT_OK) {
            if (intent == null) {
                log("Return.....");
                return;
            }

//            final ArrayList<String> selectedContacts = intent.getStringArrayListExtra("SELECTED_CONTACTS");
//            final long fileHandle = intent.getLongExtra("NODE_HANDLES", 0);
//            MegaNode node = megaApi.getNodeByHandle(fileHandle);
//            if(node!=null){
//                log("Node to send: "+node.getName());
//                MegaNodeList nodeList = MegaNodeList.createInstance();
//                nodeList.addNode(node);
//                megaChatApi.attachNodes(idChat, nodeList, this);
//
//            }

            long handles[] = intent.getLongArrayExtra("NODE_HANDLES");
            log("Number of files to send: "+handles.length);

            for(int i=0; i<handles.length; i++){
                megaChatApi.attachNode(idChat, handles[i], this);
            }
            log("---- no more files to send");
        }
        else if (requestCode == Constants.REQUEST_CODE_GET && resultCode == RESULT_OK) {
            if (intent == null) {
                log("Return.....");
                return;
            }

            intent.setAction(Intent.ACTION_GET_CONTENT);
            FilePrepareTask filePrepareTask = new FilePrepareTask(this);
            filePrepareTask.execute(intent);
            ProgressDialog temp = null;
            try{
                temp = new ProgressDialog(this);
                temp.setMessage(getString(R.string.upload_prepare));
                temp.show();
            }
            catch(Exception e){
                return;
            }
            statusDialog = temp;
        }
        else if (requestCode == Constants.REQUEST_CODE_SELECT_CHAT && resultCode == RESULT_OK) {
            if(!Util.isOnline(this)) {
                removeProgressDialog();

                showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.error_server_connection_problem), -1);
                return;
            }

            showProgressForwarding();

            long[] idMessages = intent.getLongArrayExtra("ID_MESSAGES");
            log("Send "+idMessages.length+" messages");

            long[] chatHandles = intent.getLongArrayExtra("SELECTED_CHATS");
            log("Send to "+chatHandles.length+" chats");

            long[] contactHandles = intent.getLongArrayExtra("SELECTED_USERS");

            if (chatHandles != null && chatHandles.length > 0 && idMessages != null) {
                if (contactHandles != null && contactHandles.length > 0) {
                    ArrayList<MegaUser> users = new ArrayList<>();
                    ArrayList<MegaChatRoom> chats =  new ArrayList<>();

                    for (int i=0; i<contactHandles.length; i++) {
                        MegaUser user = megaApi.getContact(MegaApiAndroid.userHandleToBase64(contactHandles[i]));
                        if (user != null) {
                            users.add(user);
                        }
                    }

                    for (int i=0; i<chatHandles.length; i++) {
                        MegaChatRoom chatRoom = megaChatApi.getChatRoom(chatHandles[i]);
                        if (chatRoom != null) {
                            chats.add(chatRoom);
                        }
                    }

                    CreateChatToPerformActionListener listener = new CreateChatToPerformActionListener(chats, users, idMessages, this, CreateChatToPerformActionListener.SEND_MESSAGES, idChat);

                    for (MegaUser user : users) {
                        MegaChatPeerList peers = MegaChatPeerList.createInstance();
                        peers.addPeer(user.getHandle(), MegaChatPeerList.PRIV_STANDARD);
                        megaChatApi.createChat(false, peers, listener);
                    }
                }
                else {
                    int countChat = chatHandles.length;
                    log("Selected: " + countChat + " chats to send");

                    MultipleForwardChatProcessor forwardChatProcessor = new MultipleForwardChatProcessor(this, chatHandles, idMessages, idChat);
                    forwardChatProcessor.forward(chatRoom);
                }
            }
            else {
                log("Error on sending to chat");
            }
        }
        else if (requestCode == Constants.TAKE_PHOTO_CODE && resultCode == RESULT_OK) {
            if (resultCode == Activity.RESULT_OK) {
                log("TAKE_PHOTO_CODE ");
//                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.temporalPicDIR + "/picture.jpg";
//                File imgFile = new File(filePath);
//                String name = Util.getPhotoSyncName(imgFile.lastModified(), imgFile.getAbsolutePath());
//                String newPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.temporalPicDIR + "/" + name;
//                File newFile = new File(newPath);
//                imgFile.renameTo(newFile);
//                uploadPicture(newPath);

                onCaptureImageResult();
//                uploadPicture(finalUri);

            } else {
                log("TAKE_PHOTO_CODE--->ERROR!");
            }

        }
        else if (requestCode == Constants.REQUEST_CODE_SELECT_LOCAL_FOLDER && resultCode == RESULT_OK) {
            log("local folder selected");
            String parentPath = intent.getStringExtra(FileStorageActivityLollipop.EXTRA_PATH);
            long[] hashes = intent.getLongArrayExtra(FileStorageActivityLollipop.EXTRA_DOCUMENT_HASHES);
            if (hashes != null) {
                ArrayList<MegaNode> megaNodes = new ArrayList<>();
                for (int i=0; i<hashes.length; i++) {
                    MegaNode node = megaApi.getNodeByHandle(hashes[i]);
                    if (node != null) {
                        megaNodes.add(node);
                    }
                    else {
                        log("Node NULL, not added");
                    }
                }
                if (megaNodes.size() > 0) {
                    chatC.checkSizeBeforeDownload(parentPath, megaNodes);
                }
            }
        }
        else{
            log("Error onActivityResult");
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    public void importNodes(final long toHandle, final long[] importMessagesHandles){
        log("importNode: "+toHandle+ " -> "+ importMessagesHandles.length);
        statusDialog = new ProgressDialog(this);
        statusDialog.setMessage(getString(R.string.general_importing));
        statusDialog.show();

        MegaNode target = null;
        target = megaApi.getNodeByHandle(toHandle);
        if(target == null){
            target = megaApi.getRootNode();
        }
        log("TARGET: " + target.getName() + "and handle: " + target.getHandle());

        if(importMessagesHandles.length==1){
            for (int k = 0; k < importMessagesHandles.length; k++){
                MegaChatMessage message = megaChatApi.getMessage(idChat, importMessagesHandles[k]);
                if(message!=null){

                    MegaNodeList nodeList = message.getMegaNodeList();

                    for(int i=0;i<nodeList.size();i++){
                        MegaNode document = nodeList.get(i);
                        if (document != null) {
                            log("DOCUMENT: " + document.getName() + "_" + document.getHandle());
                            document = chatC.authorizeNodeIfPreview(document, chatRoom);
                            if (target != null) {
//                            MegaNode autNode = megaApi.authorizeNode(document);

                                megaApi.copyNode(document, target, this);
                            } else {
                                log("TARGET: null");
                               showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.import_success_error), -1);
                            }
                        }
                        else{
                            log("DOCUMENT: null");
                            showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.import_success_error), -1);
                        }
                    }

                }
                else{
                    log("MESSAGE is null");
                    showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.import_success_error), -1);
                }
            }
        }
        else {
            MultipleRequestListener listener = new MultipleRequestListener(Constants.MULTIPLE_CHAT_IMPORT, this);

            for (int k = 0; k < importMessagesHandles.length; k++){
                MegaChatMessage message = megaChatApi.getMessage(idChat, importMessagesHandles[k]);
                if(message!=null){

                    MegaNodeList nodeList = message.getMegaNodeList();

                    for(int i=0;i<nodeList.size();i++){
                        MegaNode document = nodeList.get(i);
                        if (document != null) {
                            log("DOCUMENT: " + document.getName() + "_" + document.getHandle());
                            document = chatC.authorizeNodeIfPreview(document, chatRoom);
                            if (target != null) {
//                            MegaNode autNode = megaApi.authorizeNode(document);
                                megaApi.copyNode(document, target, listener);
                            } else {
                                log("TARGET: null");
                            }
                        }
                        else{
                            log("DOCUMENT: null");
                        }
                    }
                }
                else{
                    log("MESSAGE is null");
                    showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.import_success_error), -1);
                }
            }
        }
    }

    public void retryNodeAttachment(long nodeHandle){
        megaChatApi.attachNode(idChat, nodeHandle, this);
    }

    public void retryContactAttachment(MegaHandleList handleList){
        log("retryContactAttachment");
        MegaChatMessage contactMessage = megaChatApi.attachContacts(idChat, handleList);
        if(contactMessage!=null){
            AndroidMegaChatMessage androidMsgSent = new AndroidMegaChatMessage(contactMessage);
            sendMessageToUI(androidMsgSent);
        }
    }

    public void retryPendingMessage(long idMessage){
        log("retryPendingMessage: "+idMessage);

        PendingMessageSingle pendMsg = dbH.findPendingMessageById(idMessage);

        if(pendMsg!=null){

            if(pendMsg.getNodeHandle()!=-1){
                removePendingMsg(idMessage);
                retryNodeAttachment(pendMsg.getNodeHandle());
            }
            else{
                log("The file was not uploaded yet");

                ////Retry to send

                String filePath = pendMsg.getFilePath();

                File f = new File(filePath);
                if (!f.exists()) {
                    showSnackbar(Constants.SNACKBAR_TYPE, getResources().getQuantityString(R.plurals.messages_forwarded_error_not_available, 1, 1), -1);
                    return;
                }

                //Remove the old message from the UI and DB
                removePendingMsg(idMessage);

                Intent intent = new Intent(this, ChatUploadService.class);

                PendingMessageSingle pMsgSingle = new PendingMessageSingle();
                pMsgSingle.setChatId(idChat);
                long timestamp = System.currentTimeMillis()/1000;
                pMsgSingle.setUploadTimestamp(timestamp);

                String fingerprint = megaApi.getFingerprint(f.getAbsolutePath());

                pMsgSingle.setFilePath(f.getAbsolutePath());
                pMsgSingle.setName(f.getName());
                pMsgSingle.setFingerprint(fingerprint);

                long idMessageDb = dbH.addPendingMessage(pMsgSingle);
                pMsgSingle.setId(idMessageDb);
                if(idMessageDb!=-1){
                    intent.putExtra(ChatUploadService.EXTRA_ID_PEND_MSG, idMessageDb);

                    if(!isLoadingHistory){
                        AndroidMegaChatMessage newNodeAttachmentMsg = new AndroidMegaChatMessage(pMsgSingle, true);
                        sendMessageToUI(newNodeAttachmentMsg);
                    }

//                ArrayList<String> filePaths = newPendingMsg.getFilePaths();
//                filePaths.add("/home/jfjf.jpg");

                    intent.putExtra(ChatUploadService.EXTRA_CHAT_ID, idChat);

                    startService(intent);
                }
                else{
                    log("Error when adding pending msg to the database");
                }
            }
        }
        else{
            log("Pending message does not exist");
            showSnackbar(Constants.SNACKBAR_TYPE, getResources().getQuantityString(R.plurals.messages_forwarded_error_not_available, 1, 1), -1);
        }
    }

    private void endCall(long chatHang){
        log("endCall");
        if(megaChatApi!=null){
            megaChatApi.hangChatCall(chatHang, this);
        }
    }

    private void showConfirmationToJoinCall(final MegaChatRoom c){
        log("showConfirmationToJoinCall");

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        log("showConfirmationToJoinCall: END & JOIN");
                        //Find the call in progress:
                        if(megaChatApi!=null){
                            endCall(ChatUtil.getChatCallInProgress(megaChatApi));
                        }
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        String message= getResources().getString(R.string.text_join_call);
        builder.setTitle(R.string.title_join_call);
        builder.setMessage(message).setPositiveButton(context.getString(R.string.answer_call_incoming).toUpperCase(), dialogClickListener).setNegativeButton(R.string.general_cancel, dialogClickListener).show();
    }

    public void showConfirmationOpenCamera(final MegaChatRoom c){
        log("showConfirmationOpenCamera");

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        log("Open camera and lost the camera in the call");
                        //Find the call in progress:
                        if(megaChatApi!=null){
                            long chatIdCallInProgress = ChatUtil.getChatCallInProgress(megaChatApi);

                            MegaChatCall callInProgress = megaChatApi.getChatCall(chatIdCallInProgress);
                            if(callInProgress!=null){
                                if(callInProgress.hasLocalVideo()){
                                    megaChatApi.disableVideo(chatIdCallInProgress, null);
                                }
                                openCameraApp();
                            }
                        }
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        String message= getResources().getString(R.string.confirmation_open_camera_on_chat);
        builder.setTitle(R.string.title_confirmation_open_camera_on_chat);
        builder.setMessage(message).setPositiveButton(R.string.context_open_link, dialogClickListener).setNegativeButton(R.string.general_cancel, dialogClickListener).show();
    }

    public void showConfirmationClearChat(final MegaChatRoom c){
        log("showConfirmationClearChat");

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        log("Clear chat!");
//						megaChatApi.truncateChat(chatHandle, MegaChatHandle.MEGACHAT_INVALID_HANDLE);
                        log("Clear history selected!");
                        chatC.clearHistory(c);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        android.support.v7.app.AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        }
        else{
            builder = new AlertDialog.Builder(this);
        }
        String message= getResources().getString(R.string.confirmation_clear_group_chat);
        builder.setTitle(R.string.title_confirmation_clear_group_chat);
        builder.setMessage(message).setPositiveButton(R.string.general_clear, dialogClickListener)
                .setNegativeButton(R.string.general_cancel, dialogClickListener).show();
    }

    public void showConfirmationLeaveChat (final MegaChatRoom c){
        log("showConfirmationLeaveChat");

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE: {
                        ChatController chatC = new ChatController(chatActivity);
                        chatC.leaveChat(c);
                        break;
                    }
                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        android.support.v7.app.AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        }
        else{
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle(getResources().getString(R.string.title_confirmation_leave_group_chat));
        String message= getResources().getString(R.string.confirmation_leave_group_chat);
        builder.setMessage(message).setPositiveButton(R.string.general_leave, dialogClickListener)
                .setNegativeButton(R.string.general_cancel, dialogClickListener).show();
    }

    public void showConfirmationRejoinChat(final long publicHandle){
        log("showConfirmationRejoinChat");

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE: {
                        log("Rejoin chat!: " + publicHandle);
                        megaChatApi.autorejoinPublicChat(idChat, publicHandle, chatActivity);
                        break;
                    }
                    case DialogInterface.BUTTON_NEGATIVE: {
                        //No button clicked
                        break;
                    }
                }
            }
        };

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        String message= getResources().getString(R.string.confirmation_rejoin_chat_link);
        builder.setMessage(message).setPositiveButton(R.string.action_join, dialogClickListener)
                .setNegativeButton(R.string.general_cancel, dialogClickListener).show();
    }

    @Override
    public void onBackPressed() {
        log("onBackPressed");
        retryConnectionsAndSignalPresence();

        closeChat(true);

        if(emojiKeyboard!=null && ((emojiKeyboard.getLetterKeyboardShown())||(emojiKeyboard.getEmojiKeyboardShown()))){
            emojiKeyboard.hideBothKeyboard(this);
        }
        else{
            if(fileStorageLayout.isShown()){
                hideFileStorageSection();
            }
            else{
                if (handlerEmojiKeyboard != null){
                    handlerEmojiKeyboard.removeCallbacksAndMessages(null);
                }
                if (handlerKeyboard != null){
                    handlerKeyboard.removeCallbacksAndMessages(null);
                }
                ifAnonymousModeLogin(false);
            }
        }
    }

    public static void log(String message) {
        Util.log("ChatActivityLollipop",message);
    }

    @Override
    public void onClick(View v) {
        log("onClick");

        switch (v.getId()) {
            case R.id.home:{
                onBackPressed();
                break;
            }
            case R.id.call_in_progress_layout:{
                log("call_in_progress_layout");
                startVideo = false;
                if(checkPermissionsCall()){
                    startCall();
                }
                break;
            }
            case R.id.send_message_icon_chat:{
                log("onClick:send_message_icon_chat");

                writingLayout.setClickable(false);
                String text = textChat.getText().toString();

                if((fileStorageF != null)&&(fileStorageF.isMultipleselect())){
                    fileStorageF.sendImages();
                }else{

                    if(!text.isEmpty()) {

                        if (editingMessage) {
                            log("onClick:send_message_icon_chat:editingMessage");
                            editMessage(text);
                            clearSelections();
                            hideMultipleSelect();
                            actionMode.invalidate();
                        } else {
                            log("onClick:send_message_icon_chat:sendindMessage");
                            sendMessage(text);
                        }

//                        textChat.getText().clear();
                        textChat.setText("", TextView.BufferType.EDITABLE);
                    }
                }

                break;
            }
            case R.id.keyboard_twemoji_chat:
            case R.id.rl_keyboard_twemoji_chat:{
                log("onClick:keyboard_icon_chat:  ");
                if(fileStorageLayout.isShown()){
                    hideFileStorageSection();
                }
                if(emojiKeyboard!=null){
                    if(emojiKeyboard.getLetterKeyboardShown()){
                        emojiKeyboard.hideLetterKeyboard();
                        handlerKeyboard.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                emojiKeyboard.showEmojiKeyboard();
                            }
                        },250);

                    }else if(emojiKeyboard.getEmojiKeyboardShown()){
                        emojiKeyboard.showLetterKeyboard();

                    }else{
                        emojiKeyboard.showEmojiKeyboard();

                    }
                }

                break;
            }

            case R.id.media_icon_chat:
            case R.id.rl_media_icon_chat:{
                log("onClick:media_icon_chat: chatRoom: "+chatRoom);
                if(fileStorageLayout.isShown()){
                    hideFileStorageSection();
                }
                if(emojiKeyboard!=null){
                    emojiKeyboard.hideBothKeyboard(this);
                }

                if((megaChatApi!=null)&&(ChatUtil.participatingInACall(megaChatApi))){
                    showConfirmationOpenCamera(chatRoom);
                }else{
                    openCameraApp();
                }

                break;
            }

            case R.id.send_contact_icon_chat:
            case R.id.rl_send_contact_icon_chat:{
                if(fileStorageLayout.isShown()){
                    hideFileStorageSection();
                }
                if(emojiKeyboard!=null){
                    emojiKeyboard.hideBothKeyboard(this);
                }
                attachContact();
                break;
            }

            case R.id.pick_file_system_icon_chat:
            case R.id.rl_pick_file_system_icon_chat:{
                if(fileStorageLayout.isShown()){
                    hideFileStorageSection();
                }
                if(emojiKeyboard!=null){
                    emojiKeyboard.hideBothKeyboard(this);
                }
                attachPhotoVideo();
                break;
            }

            case R.id.pick_cloud_drive_icon_chat:
            case R.id.rl_pick_cloud_drive_icon_chat:{
                if(fileStorageLayout.isShown()){
                    hideFileStorageSection();
                }
                if(emojiKeyboard!=null){
                    emojiKeyboard.hideBothKeyboard(this);
                }

                attachFromCloud();
                break;
            }

            case R.id.pick_file_storage_icon_chat:
            case R.id.rl_pick_file_storage_icon_chat:{
                log("file storage icon ");
                if(fileStorageLayout.isShown()){
                    hideFileStorageSection();
                }else{

                    if((emojiKeyboard!=null)&&(emojiKeyboard.getLetterKeyboardShown())){
                        emojiKeyboard.hideBothKeyboard(this);
                        handlerEmojiKeyboard.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    boolean hasStoragePermission = (ContextCompat.checkSelfPermission(chatActivity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
                                    if (!hasStoragePermission) {
                                        ActivityCompat.requestPermissions(chatActivity,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},Constants.REQUEST_READ_STORAGE);
                                    }else{
                                        chatActivity.attachFromFileStorage();
                                    }
                                }
                                else{
                                    chatActivity.attachFromFileStorage();
                                }
                            }
                        },250);
                    }else{
                        if(emojiKeyboard!=null){
                            emojiKeyboard.hideBothKeyboard(this);
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            boolean hasStoragePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
                            if (!hasStoragePermission) {
                                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},Constants.REQUEST_READ_STORAGE);

                            }else{
                                this.attachFromFileStorage();
                            }
                        }
                        else{
                            this.attachFromFileStorage();
                        }
                    }

//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                        boolean hasStoragePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
//                        if (!hasStoragePermission) {
//                            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},Constants.REQUEST_READ_STORAGE);
//
//                        }else{
//                            this.attachFromFileStorage();
//                        }
//                    }
//                    else{
//                        this.attachFromFileStorage();
//                    }
                }
                break;
            }
            case R.id.toolbar_chat:{
                log("onClick:toolbar_chat");
                showGroupInfoActivity();
                break;
            }
            case R.id.message_jump_layout:{
                goToEnd();
                break;
            }
            case R.id.join_button:{
                if (chatC.isInAnonymousMode()) {
                    ifAnonymousModeLogin(true);
                }
                else {
                    megaChatApi.autojoinPublicChat(idChat, this);
                }
                break;
            }
		}
    }

    public void attachFromFileStorage(){
//        if (isFirstTimeStorage) {
            fileStorageF = ChatFileStorageFragment.newInstance();

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container_file_storage, fileStorageF,"fileStorageF")
                    .commitNowAllowingStateLoss();
//            isFirstTimeStorage = false;
//        }
        fileStorageLayout.setVisibility(View.VISIBLE);
        pickFileStorageButton.setImageResource(R.drawable.ic_g_select_image);

//        fileStorageF = ChatFileStorageFragment.newInstance();
//        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        ft.replace(R.id.fragment_container_file_storage, fileStorageF, "fileStorageF");
//        ft.commitNow();

        //ft.commitAllowingStateLoss();
    }

    public void attachFromCloud(){
        log("attachFromCloud");
        if(megaApi!=null && megaApi.getRootNode()!=null){
            ChatController chatC = new ChatController(this);
            chatC.pickFileToSend();
        }
        else{
            log("Online but not megaApi");
            Util.showErrorAlertDialog(getString(R.string.error_server_connection_problem), false, this);
        }
    }

    public void attachContact(){
        log("attachContact");
        chooseContactsDialog();
    }

    public void attachPhotoVideo(){
        log("attachPhotoVideo");

        disablePinScreen();

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("*/*");

        startActivityForResult(Intent.createChooser(intent, null), Constants.REQUEST_CODE_GET);
    }

    public void sendMessage(String text){
        log("sendMessage: "+text);

        MegaChatMessage msgSent = megaChatApi.sendMessage(idChat, text);
        AndroidMegaChatMessage androidMsgSent = new AndroidMegaChatMessage(msgSent);
        sendMessageToUI(androidMsgSent);
    }

    public void hideNewMessagesLayout(){
        log("hideNewMessagesLayout");

        int position = positionNewMessagesLayout;

        positionNewMessagesLayout = -1;
        lastIdMsgSeen = -1;
        generalUnreadCount = -1;
        lastSeenReceived = true;
        newVisibility = false;

        if(adapter!=null){
            adapter.notifyItemChanged(position);
        }
    }

    public void openCameraApp(){
        log("openCameraApp()");
        isTakePicture = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean hasStoragePermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            if (!hasStoragePermission) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Constants.REQUEST_WRITE_STORAGE);
            }

            boolean hasCameraPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
            if (!hasCameraPermission) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        Constants.REQUEST_CAMERA);
            }

            if (hasStoragePermission && hasCameraPermission){
                this.takePicture();
            }
        }else{
            this.takePicture();
        }
    }

    public void sendMessageToUI(AndroidMegaChatMessage androidMsgSent){
        log("sendMessageToUI");

        if(positionNewMessagesLayout!=-1){
            hideNewMessagesLayout();
        }

        int infoToShow = -1;

        int index = messages.size()-1;
        if(androidMsgSent!=null){
            if(androidMsgSent.isUploading()){
                log("Name of the file uploading: "+androidMsgSent.getPendingMessage().getName());
            }
            else{
                log("Sent message with id temp: "+androidMsgSent.getMessage().getTempId());
                log("State of the message: "+androidMsgSent.getMessage().getStatus());
            }

            if(index==-1){
                //First element
                log("First element!");
                messages.add(androidMsgSent);
                messages.get(0).setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_ALL);
            }
            else{
                //Not first element
                //Find where to add in the queue

                AndroidMegaChatMessage msg = messages.get(index);

                if(!androidMsgSent.isUploading()){
                    while(msg.isUploading()){
                        index--;
                        if (index == -1) {
                            break;
                        }
                        msg = messages.get(index);
                    }
                }


                while (!msg.isUploading() && msg.getMessage().getStatus() == MegaChatMessage.STATUS_SENDING_MANUAL) {
                    index--;
                    if (index == -1) {
                        break;
                    }
                    msg = messages.get(index);
                }

                index++;
                log("Add in position: "+index);
                messages.add(index, androidMsgSent);
                infoToShow = adjustInfoToShow(index);
            }

            if (adapter == null){
                log("adapter NULL");
                adapter = new MegaChatLollipopAdapter(this, chatRoom, messages, listView);
                adapter.setHasStableIds(true);
                listView.setLayoutManager(mLayoutManager);
                listView.setAdapter(adapter);
                adapter.setMessages(messages);
            }
            else{
                log("adapter is NOT null");
                adapter.addMessage(messages, index);
                if(infoToShow== AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_ALL){
                    mLayoutManager.scrollToPositionWithOffset(index, Util.scaleHeightPx(50, outMetrics));
                }else{
                    mLayoutManager.scrollToPositionWithOffset(index, Util.scaleHeightPx(20, outMetrics));
                }
            }
        }
        else{
            log("Error sending message!");
        }
    }

    public void editMessage(String text){
        log("editMessage: "+text);
        MegaChatMessage msgEdited = null;

        if(messageToEdit.getMsgId()!=-1){
            msgEdited = megaChatApi.editMessage(idChat, messageToEdit.getMsgId(), text);
        }
        else{
            msgEdited = megaChatApi.editMessage(idChat, messageToEdit.getTempId(), text);
        }

        if(msgEdited!=null){
            log("Edited message: status: "+msgEdited.getStatus());
            AndroidMegaChatMessage androidMsgEdited = new AndroidMegaChatMessage(msgEdited);
            modifyMessageReceived(androidMsgEdited, false);
        }
        else{
            log("Message cannot be edited!");
            showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.error_editing_message), -1);
        }
    }

    public void editMessageMS(String text, MegaChatMessage messageToEdit){
        log("editMessageMS: "+text);
        MegaChatMessage msgEdited = null;

        if(messageToEdit.getMsgId()!=-1){
            msgEdited = megaChatApi.editMessage(idChat, messageToEdit.getMsgId(), text);
        }
        else{
            msgEdited = megaChatApi.editMessage(idChat, messageToEdit.getTempId(), text);
        }

        if(msgEdited!=null){
            log("Edited message: status: "+msgEdited.getStatus());
            AndroidMegaChatMessage androidMsgEdited = new AndroidMegaChatMessage(msgEdited);
            modifyMessageReceived(androidMsgEdited, false);
        }
        else{
            log("Message cannot be edited!");
            showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.error_editing_message), -1);
        }
    }

    public void showUploadPanel(){
        AttachmentUploadBottomSheetDialogFragment bottomSheetDialogFragment = new AttachmentUploadBottomSheetDialogFragment();
        bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
    }

    public void activateActionMode(){
        log("activateActionMode");
        if (!adapter.isMultipleSelect()){
            adapter.setMultipleSelect(true);
            actionMode = startSupportActionMode(new ActionBarCallBack());
        }
    }


    /////Multiselect/////
    private class  ActionBarCallBack implements ActionMode.Callback {

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            ArrayList<AndroidMegaChatMessage> messagesSelected = adapter.getSelectedMessages();

            switch(item.getItemId()){
                case R.id.chat_cab_menu_edit:{
                    log("Edit text");
                    editingMessage = true;
                    messageToEdit = messagesSelected.get(0).getMessage();
                    textChat.setText(messageToEdit.getContent());
                    textChat.setSelection(textChat.getText().length());
                    //Show keyboard

                    break;
                }
                case R.id.chat_cab_menu_forward:{
                    log("Forward message");
                    forwardMessages(messagesSelected);
                    break;
                }
                case R.id.chat_cab_menu_copy:{
                    clearSelections();
                    hideMultipleSelect();

                    String text = "";

                    if(messagesSelected.size()==1){
                        AndroidMegaChatMessage message = messagesSelected.get(0);
                        text = chatC.createSingleManagementString(message, chatRoom);
                    }
                    else{
                        text = copyMessages(messagesSelected);
                    }

                    if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboard.setText(text);
                    } else {
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
                        clipboard.setPrimaryClip(clip);
                    }
                    showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.messages_copied_clipboard), -1);

                    break;
                }
                case R.id.chat_cab_menu_delete:{
                    clearSelections();
                    hideMultipleSelect();
                    //Delete
                    showConfirmationDeleteMessages(messagesSelected, chatRoom);
                    break;
                }
                case R.id.chat_cab_menu_download:{
                    clearSelections();
                    hideMultipleSelect();
                    ArrayList<MegaNodeList> list = new ArrayList<>();
                    for(int i = 0; i<messagesSelected.size();i++){
                        MegaNodeList megaNodeList = messagesSelected.get(i).getMessage().getMegaNodeList();
                        list.add(megaNodeList);
                    }

                    chatC.prepareForChatDownload(list);
                    break;
                }
                case R.id.chat_cab_menu_import:{
                    clearSelections();
                    hideMultipleSelect();
                    chatC.importNodesFromAndroidMessages(messagesSelected);
                    break;
                }
                case R.id.chat_cab_menu_offline:{
                    clearSelections();
                    hideMultipleSelect();
                    chatC.saveForOfflineWithAndroidMessages(messagesSelected, chatRoom);
                    break;
                }
            }
            return false;
        }

        public String copyMessages(ArrayList<AndroidMegaChatMessage> messagesSelected){
            log("copyMessages");
            ChatController chatC = new ChatController(chatActivity);
            StringBuilder builder = new StringBuilder();

            for(int i=0;i<messagesSelected.size();i++){
                AndroidMegaChatMessage messageSelected = messagesSelected.get(i);
                builder.append("[");
                String timestamp = TimeUtils.formatShortDateTime(messageSelected.getMessage().getTimestamp());
                builder.append(timestamp);
                builder.append("] ");
                String messageString = chatC.createManagementString(messageSelected, chatRoom);
                builder.append(messageString);
                builder.append("\n");
            }
            return builder.toString();
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.messages_chat_action, menu);

            importIcon = menu.findItem(R.id.chat_cab_menu_import);
            menu.findItem(R.id.chat_cab_menu_offline).setIcon(Util.mutateIconSecondary(chatActivity, R.drawable.ic_b_save_offline, R.color.white));

            Util.changeStatusBarColorActionMode(chatActivity, getWindow(), handler, 1);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode arg0) {
            log("onDestroyActionMode");
            adapter.setMultipleSelect(false);
//            textChat.getText().clear();
            editingMessage = false;
            clearSelections();
            Util.changeStatusBarColorActionMode(chatActivity, getWindow(), handler, 0);
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            log("onPrepareActionMode");
            List<AndroidMegaChatMessage> selected = adapter.getSelectedMessages();
            if (selected.size() !=0) {
//                MenuItem unselect = menu.findItem(R.id.cab_menu_unselect_all);
                if((chatRoom.getOwnPrivilege()==MegaChatRoom.PRIV_RM||chatRoom.getOwnPrivilege()==MegaChatRoom.PRIV_RO) && !chatRoom.isPreview()){

                    boolean showCopy = true;
                    for(int i=0; i<selected.size();i++) {
                        if (showCopy) {
                            if (selected.get(i).getMessage().getType() == MegaChatMessage.TYPE_NODE_ATTACHMENT || selected.get(i).getMessage().getType() == MegaChatMessage.TYPE_CONTACT_ATTACHMENT) {
                                showCopy = false;
                            }
                        }
                    }
                    menu.findItem(R.id.chat_cab_menu_edit).setVisible(false);
                    menu.findItem(R.id.chat_cab_menu_copy).setVisible(showCopy);
                    menu.findItem(R.id.chat_cab_menu_delete).setVisible(false);
                    menu.findItem(R.id.chat_cab_menu_forward).setVisible(false);
                    menu.findItem(R.id.chat_cab_menu_download).setVisible(false);
                    menu.findItem(R.id.chat_cab_menu_offline).setVisible(false);
                    importIcon.setVisible(false);
                }
                else{
                    log("Chat with permissions or preview");
                    if(Util.isOnline(chatActivity) && !chatC.isInAnonymousMode()){
                        menu.findItem(R.id.chat_cab_menu_forward).setVisible(true);
                    }else{
                        menu.findItem(R.id.chat_cab_menu_forward).setVisible(false);
                    }

                    if (selected.size() == 1) {
                        if(selected.get(0).isUploading()){
                            menu.findItem(R.id.chat_cab_menu_copy).setVisible(false);
                            menu.findItem(R.id.chat_cab_menu_delete).setVisible(false);
                            menu.findItem(R.id.chat_cab_menu_edit).setVisible(false);
                            menu.findItem(R.id.chat_cab_menu_forward).setVisible(false);
                            menu.findItem(R.id.chat_cab_menu_download).setVisible(false);
                            menu.findItem(R.id.chat_cab_menu_offline).setVisible(false);
                            importIcon.setVisible(false);
                        }
                        else if(selected.get(0).getMessage().getType()==MegaChatMessage.TYPE_NODE_ATTACHMENT){
                            log("TYPE_NODE_ATTACHMENT selected");
                            menu.findItem(R.id.chat_cab_menu_copy).setVisible(false);
                            menu.findItem(R.id.chat_cab_menu_edit).setVisible(false);

                            if(selected.get(0).getMessage().getUserHandle()==myUserHandle && selected.get(0).getMessage().isDeletable()){
                                log("one message Message DELETABLE");
                                menu.findItem(R.id.chat_cab_menu_delete).setVisible(true);
                            }
                            else{
                                menu.findItem(R.id.chat_cab_menu_delete).setVisible(false);
                            }

                            if(Util.isOnline(chatActivity)){
                                menu.findItem(R.id.chat_cab_menu_download).setVisible(true);
                                if (chatC.isInAnonymousMode()) {
                                    menu.findItem(R.id.chat_cab_menu_offline).setVisible(false);
                                    importIcon.setVisible(false);
                                }
                                else {
                                    menu.findItem(R.id.chat_cab_menu_offline).setVisible(true);
                                    importIcon.setVisible(true);
                                }
                            }
                            else{
                                menu.findItem(R.id.chat_cab_menu_download).setVisible(false);
                                menu.findItem(R.id.chat_cab_menu_offline).setVisible(false);
                                importIcon.setVisible(false);
                            }
                        }
                        else if(selected.get(0).getMessage().getType()==MegaChatMessage.TYPE_CONTACT_ATTACHMENT){
                            menu.findItem(R.id.chat_cab_menu_copy).setVisible(false);
                            menu.findItem(R.id.chat_cab_menu_edit).setVisible(false);

                            if(selected.get(0).getMessage().getUserHandle()==myUserHandle && selected.get(0).getMessage().isDeletable()){
                                log("one message Message DELETABLE");
                                menu.findItem(R.id.chat_cab_menu_delete).setVisible(true);
                            }
                            else{
                                log("one message Message NOT DELETABLE");
                                menu.findItem(R.id.chat_cab_menu_delete).setVisible(false);
                            }

                            menu.findItem(R.id.chat_cab_menu_download).setVisible(false);
                            menu.findItem(R.id.chat_cab_menu_offline).setVisible(false);
                            importIcon.setVisible(false);
                        }
                        else{
                            MegaChatMessage messageSelected= megaChatApi.getMessage(idChat, selected.get(0).getMessage().getMsgId());
                            menu.findItem(R.id.chat_cab_menu_copy).setVisible(true);

                            if(messageSelected.getUserHandle()==myUserHandle){
                                if(messageSelected.isEditable()){
                                    log("Message EDITABLE");
                                    menu.findItem(R.id.chat_cab_menu_edit).setVisible(true);
                                    menu.findItem(R.id.chat_cab_menu_delete).setVisible(true);
                                }
                                else{
                                    log("Message NOT EDITABLE");
                                    menu.findItem(R.id.chat_cab_menu_edit).setVisible(false);
                                    menu.findItem(R.id.chat_cab_menu_delete).setVisible(false);
                                }

                                int type = selected.get(0).getMessage().getType();
                                if (!Util.isOnline(chatActivity) || type == MegaChatMessage.TYPE_TRUNCATE||type == MegaChatMessage.TYPE_ALTER_PARTICIPANTS||type == MegaChatMessage.TYPE_CHAT_TITLE||type == MegaChatMessage.TYPE_PRIV_CHANGE||type == MegaChatMessage.TYPE_CALL_ENDED||type == MegaChatMessage.TYPE_CALL_STARTED) {
                                    menu.findItem(R.id.chat_cab_menu_forward).setVisible(false);
                                }
                                else{
                                    menu.findItem(R.id.chat_cab_menu_forward).setVisible(true);
                                }
                            }
                            else{
                                menu.findItem(R.id.chat_cab_menu_edit).setVisible(false);
                                menu.findItem(R.id.chat_cab_menu_delete).setVisible(false);
                                importIcon.setVisible(false);

                                int type = selected.get(0).getMessage().getType();
                                if (chatC.isInAnonymousMode() || !Util.isOnline(chatActivity)
                                        || type == MegaChatMessage.TYPE_TRUNCATE||type == MegaChatMessage.TYPE_ALTER_PARTICIPANTS||type == MegaChatMessage.TYPE_CHAT_TITLE||type == MegaChatMessage.TYPE_PRIV_CHANGE||type == MegaChatMessage.TYPE_CALL_ENDED||type == MegaChatMessage.TYPE_CALL_STARTED) {
                                    menu.findItem(R.id.chat_cab_menu_forward).setVisible(false);
                                }
                                else{
                                    menu.findItem(R.id.chat_cab_menu_forward).setVisible(true);
                                }
                            }

                            menu.findItem(R.id.chat_cab_menu_download).setVisible(false);
                            menu.findItem(R.id.chat_cab_menu_offline).setVisible(false);
                            importIcon.setVisible(false);
                        }
                    }
                    else{
                        log("Many items selected");
                        boolean isUploading = false;
                        boolean showDelete = true;
                        boolean showCopy = true;
                        boolean showForward = true;
                        boolean allNodeAttachments = true;

                        for(int i=0; i<selected.size();i++) {

                            if (!isUploading) {
                                if (selected.get(i).isUploading()) {
                                    isUploading = true;
                                }
                            }

                            if (showCopy) {
                                if (selected.get(i).getMessage().getType() == MegaChatMessage.TYPE_NODE_ATTACHMENT || selected.get(i).getMessage().getType() == MegaChatMessage.TYPE_CONTACT_ATTACHMENT) {
                                    showCopy = false;
                                }
                            }

                            if (showDelete) {
                                if (selected.get(i).getMessage().getUserHandle() == myUserHandle) {
                                    if (selected.get(i).getMessage().getType() == MegaChatMessage.TYPE_NORMAL || selected.get(i).getMessage().getType() == MegaChatMessage.TYPE_NODE_ATTACHMENT || selected.get(i).getMessage().getType() == MegaChatMessage.TYPE_CONTACT_ATTACHMENT || selected.get(i).getMessage().getType() == MegaChatMessage.TYPE_CONTAINS_META) {
                                        if (!(selected.get(i).getMessage().isDeletable())) {
                                            showDelete = false;
                                        }
                                    } else {
                                        showDelete = false;
                                    }
                                } else {
                                    showDelete = false;
                                }
                            }

                            if (showForward) {
                                int type = selected.get(i).getMessage().getType();
                                if (type == MegaChatMessage.TYPE_TRUNCATE||type == MegaChatMessage.TYPE_ALTER_PARTICIPANTS||type == MegaChatMessage.TYPE_CHAT_TITLE||type == MegaChatMessage.TYPE_PRIV_CHANGE||type == MegaChatMessage.TYPE_CALL_ENDED||type == MegaChatMessage.TYPE_CALL_STARTED) {
                                    showForward = false;
                                }
                            }

                            if (allNodeAttachments) {
                                if (selected.get(i).getMessage().getType() != MegaChatMessage.TYPE_NODE_ATTACHMENT) {
                                    allNodeAttachments = false;
                                }
                            }
                        }

                        if (isUploading) {
                            menu.findItem(R.id.chat_cab_menu_copy).setVisible(false);
                            menu.findItem(R.id.chat_cab_menu_delete).setVisible(false);
                            menu.findItem(R.id.chat_cab_menu_edit).setVisible(false);
                            menu.findItem(R.id.chat_cab_menu_forward).setVisible(false);
                            menu.findItem(R.id.chat_cab_menu_download).setVisible(false);
                            menu.findItem(R.id.chat_cab_menu_offline).setVisible(false);
                            importIcon.setVisible(false);
                        }
                        else {
                            if(allNodeAttachments && Util.isOnline(chatActivity)){
                                menu.findItem(R.id.chat_cab_menu_download).setVisible(true);
                                if (chatC.isInAnonymousMode()){
                                    menu.findItem(R.id.chat_cab_menu_offline).setVisible(false);
                                    importIcon.setVisible(false);
                                }
                                else {
                                    menu.findItem(R.id.chat_cab_menu_offline).setVisible(true);
                                    importIcon.setVisible(true);
                                }
                            }
                            else{
                                menu.findItem(R.id.chat_cab_menu_download).setVisible(false);
                                menu.findItem(R.id.chat_cab_menu_offline).setVisible(false);
                                importIcon.setVisible(false);
                            }

                            menu.findItem(R.id.chat_cab_menu_edit).setVisible(false);
                            if (chatC.isInAnonymousMode()){
                                menu.findItem(R.id.chat_cab_menu_copy).setVisible(false);
                                menu.findItem(R.id.chat_cab_menu_delete).setVisible(false);
                            }
                            else {
                                menu.findItem(R.id.chat_cab_menu_copy).setVisible(showCopy);
                                menu.findItem(R.id.chat_cab_menu_delete).setVisible(showDelete);
                            }
                            if(Util.isOnline(chatActivity) && !chatC.isInAnonymousMode()){
                                menu.findItem(R.id.chat_cab_menu_forward).setVisible(showForward);
                            }
                            else{
                                menu.findItem(R.id.chat_cab_menu_forward).setVisible(false);
                            }
                        }
                    }
                }
            }
            return false;
        }

    }

    public boolean showSelectMenuItem(){
        if (adapter != null){
            return adapter.isMultipleSelect();
        }

        return false;
    }

    public void showConfirmationDeleteMessages(final ArrayList<AndroidMegaChatMessage> messages, final MegaChatRoom chat){
        log("showConfirmationDeleteMessages");

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        ChatController cC = new ChatController(chatActivity);
                        cC.deleteAndroidMessages(messages, chat);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        }
        else{
            builder = new AlertDialog.Builder(this);
        }

        if(messages.size()==1){
            builder.setMessage(R.string.confirmation_delete_one_message);
        }
        else{
            builder.setMessage(R.string.confirmation_delete_several_messages);
        }
        builder.setPositiveButton(R.string.context_remove, dialogClickListener)
                .setNegativeButton(R.string.general_cancel, dialogClickListener).show();
    }

    public void showConfirmationDeleteMessage(final long messageId, final long chatId){
        log("showConfirmationDeleteMessage");

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        ChatController cC = new ChatController(chatActivity);
                        cC.deleteMessageById(messageId, chatId);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        }
        else{
            builder = new AlertDialog.Builder(this);
        }

        builder.setMessage(R.string.confirmation_delete_one_message);

        builder.setPositiveButton(R.string.context_remove, dialogClickListener)
                .setNegativeButton(R.string.general_cancel, dialogClickListener).show();
    }

    /*
     * Clear all selected items
     */
    private void clearSelections() {
        if(adapter.isMultipleSelect()){
            adapter.clearSelections();
        }
        updateActionModeTitle();
    }

    private void updateActionModeTitle() {
        try {
            actionMode.setTitle(adapter.getSelectedItemCount()+"");
            actionMode.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
            log("oninvalidate error");
        }
    }

    /*
     * Disable selection
     */
    public void hideMultipleSelect() {
        log("hideMultipleSelect");
        adapter.setMultipleSelect(false);
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    public void selectAll() {
        if (adapter != null) {
            if (adapter.isMultipleSelect()) {
                adapter.selectAll();
            } else {
                adapter.setMultipleSelect(true);
                adapter.selectAll();

                actionMode = startSupportActionMode(new ActionBarCallBack());
            }

            updateActionModeTitle();
        }
    }

    public void itemClick(int positionInAdapter, int [] screenPosition) {
        log("itemClick");
        int position = positionInAdapter-1;

        if(position<messages.size()){
            AndroidMegaChatMessage m = messages.get(position);

            if (adapter.isMultipleSelect()) {
                if (!m.isUploading()) {
                    if (m.getMessage() != null) {
                        MegaChatContainsMeta meta = m.getMessage().getContainsMeta();
                        if (meta != null && meta.getType() == MegaChatContainsMeta.CONTAINS_META_INVALID) {
                        }else{
                            log("Message id: " + m.getMessage().getMsgId());
                            log("Timestamp: " + m.getMessage().getTimestamp());

                            adapter.toggleSelection(positionInAdapter);
                            List<AndroidMegaChatMessage> messages = adapter.getSelectedMessages();
                            if (messages.size() > 0) {
                                updateActionModeTitle();
                            }
                        }

                    }

//                    adapter.toggleSelection(positionInAdapter);

//                    List<AndroidMegaChatMessage> messages = adapter.getSelectedMessages();
//                    if (messages.size() > 0) {
//                        updateActionModeTitle();
////                adapter.notifyDataSetChanged();
//                    }
////                    else {
////                        hideMultipleSelect();
////                    }
                }
            }else{
                if(m!=null){
                    if(m.isUploading()){
                        showUploadingAttachmentBottomSheet(m, position);
                    }
                    else{

                        if((m.getMessage().getStatus()==MegaChatMessage.STATUS_SERVER_REJECTED)||(m.getMessage().getStatus()==MegaChatMessage.STATUS_SENDING_MANUAL)){
                            if(m.getMessage().getUserHandle()==megaChatApi.getMyUserHandle()) {
                                if (!(m.getMessage().isManagementMessage())) {
                                    log("selected message handle: " + m.getMessage().getTempId());
                                    log("selected message rowId: " + m.getMessage().getRowId());
                                    if ((m.getMessage().getStatus() == MegaChatMessage.STATUS_SERVER_REJECTED) || (m.getMessage().getStatus() == MegaChatMessage.STATUS_SENDING_MANUAL)) {
                                        log("show not sent message panel");
                                        showMsgNotSentPanel(m, position);
                                    }
                                }
                            }
                        }
                        else{
                            if(m.getMessage().getType()==MegaChatMessage.TYPE_NODE_ATTACHMENT){
                                log("TYPE_NODE_ATTACHMENT");
                                MegaNodeList nodeList = m.getMessage().getMegaNodeList();
                                if(nodeList.size()==1){
                                    MegaNode node = chatC.authorizeNodeIfPreview(nodeList.get(0), chatRoom);
                                    if (MimeTypeList.typeForName(node.getName()).isImage()){
                                        if(node.hasPreview()){
                                            log("Show full screen viewer");
                                            showFullScreenViewer(m.getMessage().getMsgId(), screenPosition);
                                        }
                                        else{
                                            log("Image without preview - show node attachment panel for one node");
                                            showNodeAttachmentBottomSheet(m, position);
                                        }
                                    }
                                    else if (MimeTypeList.typeForName(node.getName()).isVideoReproducible() || MimeTypeList.typeForName(node.getName()).isAudio() ){
                                        log("itemClick:isFile:isVideoReproducibleOrIsAudio");
                                        String mimeType = MimeTypeList.typeForName(node.getName()).getType();
                                        log("itemClick:FILENAME: " + node.getName() + " TYPE: "+mimeType);

                                        Intent mediaIntent;
                                        boolean internalIntent;
                                        boolean opusFile = false;
                                        if (MimeTypeList.typeForName(node.getName()).isVideoNotSupported() || MimeTypeList.typeForName(node.getName()).isAudioNotSupported()){
                                            mediaIntent = new Intent(Intent.ACTION_VIEW);
                                            internalIntent=false;
                                            String[] s = node.getName().split("\\.");
                                            if (s != null && s.length > 1 && s[s.length-1].equals("opus")) {
                                                opusFile = true;
                                            }
                                        }
                                        else {
                                            log("itemClick:setIntentToAudioVideoPlayer");
                                            mediaIntent = new Intent(this, AudioVideoPlayerLollipop.class);
                                            internalIntent=true;
                                        }
                                        mediaIntent.putExtra("screenPosition", screenPosition);
                                        mediaIntent.putExtra("adapterType", Constants.FROM_CHAT);
                                        mediaIntent.putExtra("isPlayList", false);
                                        mediaIntent.putExtra("msgId", m.getMessage().getMsgId());
                                        mediaIntent.putExtra("chatId", idChat);

                                        String downloadLocationDefaultPath = Util.getDownloadLocation(this);
                                        String localPath = Util.getLocalFile(this, node.getName(), node.getSize(), downloadLocationDefaultPath);
                                        File f = new File(downloadLocationDefaultPath, node.getName());
                                        boolean isOnMegaDownloads = false;
                                        if(f.exists() && (f.length() == node.getSize())){
                                            isOnMegaDownloads = true;
                                        }
                                        log("isOnMegaDownloads: "+isOnMegaDownloads);
                                        if (localPath != null && (isOnMegaDownloads || (megaApi.getFingerprint(node) != null && megaApi.getFingerprint(node).equals(megaApi.getFingerprint(localPath))))){
                                            File mediaFile = new File(localPath);
                                            //mediaIntent.setDataAndType(Uri.parse(localPath), mimeType);
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && localPath.contains(Environment.getExternalStorageDirectory().getPath())) {
                                                log("itemClick:FileProviderOption");
                                                Uri mediaFileUri = FileProvider.getUriForFile(this, "mega.privacy.android.app.providers.fileprovider", mediaFile);
                                                if(mediaFileUri==null){
                                                    log("itemClick:ERROR:NULLmediaFileUri");
                                                    showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.general_text_error), -1);
                                                }
                                                else{
                                                    mediaIntent.setDataAndType(mediaFileUri, MimeTypeList.typeForName(node.getName()).getType());
                                                }
                                            }
                                            else{
                                                Uri mediaFileUri = Uri.fromFile(mediaFile);
                                                if(mediaFileUri==null){
                                                    log("itemClick:ERROR:NULLmediaFileUri");
                                                    showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.general_text_error), -1);
                                                }
                                                else{
                                                    mediaIntent.setDataAndType(mediaFileUri, MimeTypeList.typeForName(node.getName()).getType());
                                                }
                                            }
                                            mediaIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        }
                                        else {
                                            log("itemClick:localPathNULL");
                                            if (Util.isOnline(this)){
                                                if (megaApi.httpServerIsRunning() == 0) {
                                                    megaApi.httpServerStart();
                                                }
                                                else{
                                                    log("itemClick:ERROR:httpServerAlreadyRunning");
                                                }

                                                ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                                                ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
                                                activityManager.getMemoryInfo(mi);

                                                if(mi.totalMem>Constants.BUFFER_COMP){
                                                    log("itemClick:total mem: "+mi.totalMem+" allocate 32 MB");
                                                    megaApi.httpServerSetMaxBufferSize(Constants.MAX_BUFFER_32MB);
                                                }
                                                else{
                                                    log("itemClick:total mem: "+mi.totalMem+" allocate 16 MB");
                                                    megaApi.httpServerSetMaxBufferSize(Constants.MAX_BUFFER_16MB);
                                                }

                                                String url = megaApi.httpServerGetLocalLink(node);
                                                if(url!=null){
                                                    log("URL generated: "+ url);
                                                    Uri parsedUri = Uri.parse(url);
                                                    if(parsedUri!=null){
                                                        mediaIntent.setDataAndType(parsedUri, mimeType);
                                                    }
                                                    else{
                                                        log("itemClick:ERROR:httpServerGetLocalLink");
                                                        showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.general_text_error), -1);
                                                    }
                                                }
                                                else{
                                                    log("itemClick:ERROR:httpServerGetLocalLink");
                                                    showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.general_text_error), -1);
                                                }
                                            }
                                            else {
                                                showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.error_server_connection_problem)+". "+ getString(R.string.no_network_connection_on_play_file), -1);
                                            }
                                        }
                                        mediaIntent.putExtra("HANDLE", node.getHandle());
                                        if (opusFile){
                                            mediaIntent.setDataAndType(mediaIntent.getData(), "audio/*");
                                        }
                                        if(internalIntent){
                                            startActivity(mediaIntent);
                                        }
                                        else{
                                            log("itemClick:externalIntent");
                                            if (MegaApiUtils.isIntentAvailable(this, mediaIntent)){
                                                startActivity(mediaIntent);
                                            }
                                            else{
                                                log("itemClick:noAvailableIntent");
                                                showNodeAttachmentBottomSheet(m, position);
                                            }
                                        }
                                        overridePendingTransition(0,0);
                                        if (adapter != null) {
                                            adapter.setNodeAttachmentVisibility(false, holder_imageDrag, position);
                                        }
                                    }
                                    else if (MimeTypeList.typeForName(node.getName()).isPdf()){
                                        log("itemClick:isFile:isPdf");
                                        String mimeType = MimeTypeList.typeForName(node.getName()).getType();
                                        log("itemClick:FILENAME: " + node.getName() + " TYPE: "+mimeType);
                                        Intent pdfIntent = new Intent(this, PdfViewerActivityLollipop.class);
                                        pdfIntent.putExtra("inside", true);
                                        pdfIntent.putExtra("adapterType", Constants.FROM_CHAT);
                                        pdfIntent.putExtra("msgId", m.getMessage().getMsgId());
                                        pdfIntent.putExtra("chatId", idChat);

                                        String downloadLocationDefaultPath = Util.getDownloadLocation(this);
                                        String localPath = Util.getLocalFile(this, node.getName(), node.getSize(), downloadLocationDefaultPath);
                                        File f = new File(downloadLocationDefaultPath, node.getName());
                                        boolean isOnMegaDownloads = false;
                                        if(f.exists() && (f.length() == node.getSize())){
                                            isOnMegaDownloads = true;
                                        }
                                        log("isOnMegaDownloads: "+isOnMegaDownloads);
                                        if (localPath != null && (isOnMegaDownloads || (megaApi.getFingerprint(node) != null && megaApi.getFingerprint(node).equals(megaApi.getFingerprint(localPath))))){
                                            File mediaFile = new File(localPath);
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && localPath.contains(Environment.getExternalStorageDirectory().getPath())) {
                                                log("itemClick:FileProviderOption");
                                                Uri mediaFileUri = FileProvider.getUriForFile(this, "mega.privacy.android.app.providers.fileprovider", mediaFile);
                                                if(mediaFileUri==null){
                                                    log("itemClick:ERROR:NULLmediaFileUri");
                                                    showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.general_text_error), -1);
                                                }
                                                else{
                                                    pdfIntent.setDataAndType(mediaFileUri, MimeTypeList.typeForName(node.getName()).getType());
                                                }
                                            }
                                            else{
                                                Uri mediaFileUri = Uri.fromFile(mediaFile);
                                                if(mediaFileUri==null){
                                                    log("itemClick:ERROR:NULLmediaFileUri");
                                                    showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.general_text_error), -1);
                                                }
                                                else{
                                                    pdfIntent.setDataAndType(mediaFileUri, MimeTypeList.typeForName(node.getName()).getType());
                                                }
                                            }
                                            pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        }
                                        else {
                                            log("itemClick:localPathNULL");
                                            if (Util.isOnline(this)){
                                                if (megaApi.httpServerIsRunning() == 0) {
                                                    megaApi.httpServerStart();
                                                }
                                                else{
                                                    log("itemClick:ERROR:httpServerAlreadyRunning");
                                                }
                                                ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                                                ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
                                                activityManager.getMemoryInfo(mi);
                                                if(mi.totalMem>Constants.BUFFER_COMP){
                                                    log("itemClick:total mem: "+mi.totalMem+" allocate 32 MB");
                                                    megaApi.httpServerSetMaxBufferSize(Constants.MAX_BUFFER_32MB);
                                                }
                                                else{
                                                    log("itemClick:total mem: "+mi.totalMem+" allocate 16 MB");
                                                    megaApi.httpServerSetMaxBufferSize(Constants.MAX_BUFFER_16MB);
                                                }
                                                String url = megaApi.httpServerGetLocalLink(node);
                                                if(url!=null){
                                                    log("URL generated: "+ url);
                                                    Uri parsedUri = Uri.parse(url);
                                                    if(parsedUri!=null){
                                                        pdfIntent.setDataAndType(parsedUri, mimeType);
                                                    }
                                                    else{
                                                        log("itemClick:ERROR:httpServerGetLocalLink");
                                                        showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.general_text_error), -1);
                                                    }
                                                }
                                                else{
                                                    log("itemClick:ERROR:httpServerGetLocalLink");
                                                    showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.general_text_error), -1);
                                                }
                                            }
                                            else {
                                                showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.error_server_connection_problem)+". "+ getString(R.string.no_network_connection_on_play_file), -1);
                                            }
                                        }
                                        pdfIntent.putExtra("HANDLE", node.getHandle());

                                        if (MegaApiUtils.isIntentAvailable(this, pdfIntent)){
                                            startActivity(pdfIntent);
                                        }
                                        else{
                                            log("itemClick:noAvailableIntent");
                                            showNodeAttachmentBottomSheet(m, position);
                                        }
                                        overridePendingTransition(0,0);
                                    }
                                    else{
                                        log("NOT Image, pdf, audio or video - show node attachment panel for one node");
                                        showNodeAttachmentBottomSheet(m, position);
                                    }
                                }
                                else{
                                    log("show node attachment panel");
                                    showNodeAttachmentBottomSheet(m, position);
                                }
                            }
                            else if(m.getMessage().getType()==MegaChatMessage.TYPE_CONTACT_ATTACHMENT){
                                log("TYPE_CONTACT_ATTACHMENT");
                                log("show contact attachment panel");
                                if (Util.isOnline(this)) {
                                    if (!chatC.isInAnonymousMode() && m != null) {
                                        if (m.getMessage().getUsersCount() == 1) {
                                            long userHandle = m.getMessage().getUserHandle(0);
                                            if(userHandle != megaChatApi.getMyUserHandle()){
                                                showContactAttachmentBottomSheet(m, position);
                                            }
                                        }
                                        else{
                                            showContactAttachmentBottomSheet(m, position);
                                        }
                                    }
                                }
                                else{
                                    //No shown - is not possible to know is it already contact or not - megaApi not working
                                    showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.error_server_connection_problem), -1);
                                }
                            }
                            else if(m.getMessage().getType()==MegaChatMessage.TYPE_CONTAINS_META){
                                log("TYPE_CONTAINS_META");
                                log("open rich link");

//                                if(!adapter.getforwardOwnRichLinksB()){
                                    MegaChatContainsMeta meta = m.getMessage().getContainsMeta();
                                    if((meta != null) && (meta.getType()!= MegaChatContainsMeta.CONTAINS_META_INVALID)){
                                        if(meta.getType()==MegaChatContainsMeta.CONTAINS_META_RICH_PREVIEW){
                                            String url = meta.getRichPreview().getUrl();
                                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                            startActivity(browserIntent);
                                        }
                                    }else{

                                    }


//                                }else{

//                                    ArrayList<AndroidMegaChatMessage> message = new ArrayList<>();
//                                    message.add(m);
//                                    prepareMessagesToForward(message);
//                                }

                            }else if((m.getMessage().getType() == MegaChatMessage.TYPE_NORMAL) && (m.getRichLinkMessage()!=null)){
                                log("TYPE_NORMAL");
                                AndroidMegaRichLinkMessage richLinkMessage = m.getRichLinkMessage();
                                String url = richLinkMessage.getUrl();

                                if(richLinkMessage.isChat()){
                                    loadChatLink(url);
                                }
                                else{
                                    if(richLinkMessage.getNode()!=null){
                                        if(richLinkMessage.getNode().isFile()){
                                            openMegaLink(url, true);
                                        }
                                        else{
                                            openMegaLink(url, false);
                                        }
                                    }
                                    else{
                                        if(richLinkMessage.isFile()){
                                            openMegaLink(url, true);
                                        }
                                        else{
                                            openMegaLink(url, false);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }else{
            log("DO NOTHING: Position ("+position+") is more than size in messages (size: "+messages.size()+")");
        }
    }

    public void loadChatLink(String link){
        log("loadChatLink: "+link);
        Intent intentOpenChat = new Intent(this, ChatActivityLollipop.class);
        intentOpenChat.setAction(Constants.ACTION_OPEN_CHAT_LINK);
        intentOpenChat.setData(Uri.parse(link));
        this.startActivity(intentOpenChat);
    }

    public void showFullScreenViewer(long msgId, int[] screenPosition){
        log("showFullScreenViewer");
        int position = 0;
        boolean positionFound = false;
        List<Long> ids = new ArrayList<>();
        for(int i=0; i<messages.size();i++){
            AndroidMegaChatMessage androidMessage = messages.get(i);
            if(!androidMessage.isUploading()){
                MegaChatMessage msg = androidMessage.getMessage();

                if(msg.getType()==MegaChatMessage.TYPE_NODE_ATTACHMENT){
                    ids.add(msg.getMsgId());

                    if(msg.getMsgId()==msgId){
                        positionFound=true;
                    }
                    if(!positionFound){
                        MegaNodeList nodeList = msg.getMegaNodeList();
                        if(nodeList.size()==1){
                            MegaNode node = nodeList.get(0);
                            if(MimeTypeList.typeForName(node.getName()).isImage()){
                                position++;
                            }
                        }
                    }
                }
            }
        }

        Intent intent = new Intent(this, ChatFullScreenImageViewer.class);
        intent.putExtra("position", position);
        intent.putExtra("chatId", idChat);
        intent.putExtra("screenPosition", screenPosition);
        long[] array = new long[ids.size()];
        for(int i = 0; i < ids.size(); i++) {
            array[i] = ids.get(i);
        }
        intent.putExtra("messageIds", array);
        startActivity(intent);
        overridePendingTransition(0,0);
        if (adapter !=  null) {
            adapter.setNodeAttachmentVisibility(false, holder_imageDrag, position);
        }
    }

    @Override
    public void onChatRoomUpdate(MegaChatApiJava api, MegaChatRoom chat) {
        log("onChatRoomUpdate!");
        this.chatRoom = chat;
        if(chat.hasChanged(MegaChatRoom.CHANGE_TYPE_CLOSED)){
            log("CHANGE_TYPE_CLOSED for the chat: "+chat.getChatId());
            int permission = chat.getOwnPrivilege();
            log("Permissions for the chat: "+permission);

            if(chat.isPreview()){
                if(permission==MegaChatRoom.PRIV_RM){
                    //Show alert to user
                    showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.alert_invalid_preview), -1);
                }
            }
            else{
                //Hide field to write
                setChatSubtitle();
                supportInvalidateOptionsMenu();
            }
        }
        else if(chat.hasChanged(MegaChatRoom.CHANGE_TYPE_STATUS)){
            log("CHANGE_TYPE_STATUS for the chat: "+chat.getChatId());
            if(!(chatRoom.isGroup())){
                long userHandle = chatRoom.getPeerHandle(0);
                setStatus(userHandle);
            }
        }
        else if(chat.hasChanged(MegaChatRoom.CHANGE_TYPE_PARTICIPANTS)){
            log("CHANGE_TYPE_PARTICIPANTS for the chat: "+chat.getChatId());
            setChatSubtitle();
        }
        else if(chat.hasChanged(MegaChatRoom.CHANGE_TYPE_OWN_PRIV)){
            log("CHANGE_TYPE_OWN_PRIV for the chat: "+chat.getChatId());
            setChatSubtitle();
            supportInvalidateOptionsMenu();
        }
        else if(chat.hasChanged(MegaChatRoom.CHANGE_TYPE_TITLE)){
            log("CHANGE_TYPE_TITLE for the chat: "+chat.getChatId());
        }
        else if(chat.hasChanged(MegaChatRoom.CHANGE_TYPE_USER_STOP_TYPING)){
            log("CHANGE_TYPE_USER_STOP_TYPING for the chat: "+chat.getChatId());

            long userHandleTyping = chat.getUserTyping();

            if(userHandleTyping==megaChatApi.getMyUserHandle()){
                return;
            }

            if(usersTypingSync==null){
                return;
            }

            //Find the item
            boolean found = false;
            for(UserTyping user : usersTypingSync) {
                if(user.getParticipantTyping().getHandle() == userHandleTyping) {
                    log("Found user typing!");
                    usersTypingSync.remove(user);
                    found=true;
                    break;
                }
            }

            if(!found){
                log("CHANGE_TYPE_USER_STOP_TYPING: Not found user typing");
            }
            else{
                updateUserTypingFromNotification();
            }

        }
        else if(chat.hasChanged(MegaChatRoom.CHANGE_TYPE_USER_TYPING)){
            log("CHANGE_TYPE_USER_TYPING for the chat: "+chat.getChatId());
            if(chat!=null){

                long userHandleTyping = chat.getUserTyping();

                if(userHandleTyping==megaChatApi.getMyUserHandle()){
                    return;
                }

                if(usersTyping==null){
                    usersTyping = new ArrayList<UserTyping>();
                    usersTypingSync = Collections.synchronizedList(usersTyping);
                }

                //Find if any notification arrives previously
                if(usersTypingSync.size()<=0){
                    log("No more users writing");
                    MegaChatParticipant participantTyping = new MegaChatParticipant(userHandleTyping);
                    UserTyping currentUserTyping = new UserTyping(participantTyping);

                    String nameTyping = chatC.getFirstName(userHandleTyping, chatRoom);

                    log("userHandleTyping: "+userHandleTyping);


                    if(nameTyping==null){
                        log("NULL name");
                        nameTyping = getString(R.string.transfer_unknown);
                    }
                    else{
                        if(nameTyping.trim().isEmpty()){
                            log("EMPTY name");
                            nameTyping = getString(R.string.transfer_unknown);
                        }
                    }
                    participantTyping.setFirstName(nameTyping);

                    userTypingTimeStamp = System.currentTimeMillis()/1000;
                    currentUserTyping.setTimeStampTyping(userTypingTimeStamp);

                    usersTypingSync.add(currentUserTyping);

                    String userTyping =  getResources().getQuantityString(R.plurals.user_typing, 1, toCDATA(usersTypingSync.get(0).getParticipantTyping().getFirstName()));

                    userTyping = userTyping.replace("[A]", "<font color=\'#8d8d94\'>");
                    userTyping = userTyping.replace("[/A]", "</font>");

                    Spanned result = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        result = Html.fromHtml(userTyping,Html.FROM_HTML_MODE_LEGACY);
                    } else {
                        result = Html.fromHtml(userTyping);
                    }

                    userTypingText.setText(result);

                    userTypingLayout.setVisibility(View.VISIBLE);
                }
                else{
                    log("More users writing or the same in different timestamp");

                    //Find the item
                    boolean found = false;
                    for(UserTyping user : usersTypingSync) {
                        if(user.getParticipantTyping().getHandle() == userHandleTyping) {
                            log("Found user typing!");
                            userTypingTimeStamp = System.currentTimeMillis()/1000;
                            user.setTimeStampTyping(userTypingTimeStamp);
                            found=true;
                            break;
                        }
                    }

                    if(!found){
                        log("It's a new user typing");
                        MegaChatParticipant participantTyping = new MegaChatParticipant(userHandleTyping);
                        UserTyping currentUserTyping = new UserTyping(participantTyping);

                        String nameTyping = chatC.getFirstName(userHandleTyping, chatRoom);
                        if(nameTyping==null){
                            log("NULL name");
                            nameTyping = getString(R.string.transfer_unknown);
                        }
                        else{
                            if(nameTyping.trim().isEmpty()){
                                log("EMPTY name");
                                nameTyping = getString(R.string.transfer_unknown);
                            }
                        }
                        participantTyping.setFirstName(nameTyping);

                        userTypingTimeStamp = System.currentTimeMillis()/1000;
                        currentUserTyping.setTimeStampTyping(userTypingTimeStamp);

                        usersTypingSync.add(currentUserTyping);

                        //Show the notification
                        int size = usersTypingSync.size();
                        switch (size){
                            case 1:{
                                String userTyping = getResources().getQuantityString(R.plurals.user_typing, 1, usersTypingSync.get(0).getParticipantTyping().getFirstName());
                                userTyping = toCDATA(userTyping);
                                userTyping = userTyping.replace("[A]", "<font color=\'#8d8d94\'>");
                                userTyping = userTyping.replace("[/A]", "</font>");

                                Spanned result = null;
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                    result = Html.fromHtml(userTyping,Html.FROM_HTML_MODE_LEGACY);
                                } else {
                                    result = Html.fromHtml(userTyping);
                                }

                                userTypingText.setText(result);
                                break;
                            }
                            case 2:{
                                String userTyping = getResources().getQuantityString(R.plurals.user_typing, 2, usersTypingSync.get(0).getParticipantTyping().getFirstName()+", "+usersTypingSync.get(1).getParticipantTyping().getFirstName());
                                userTyping = toCDATA(userTyping);
                                userTyping = userTyping.replace("[A]", "<font color=\'#8d8d94\'>");
                                userTyping = userTyping.replace("[/A]", "</font>");

                                Spanned result = null;
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                    result = Html.fromHtml(userTyping,Html.FROM_HTML_MODE_LEGACY);
                                } else {
                                    result = Html.fromHtml(userTyping);
                                }

                                userTypingText.setText(result);
                                break;
                            }
                            default:{
                                String names = usersTypingSync.get(0).getParticipantTyping().getFirstName()+", "+usersTypingSync.get(1).getParticipantTyping().getFirstName();
                                String userTyping = String.format(getString(R.string.more_users_typing),toCDATA(names));

                                userTyping = userTyping.replace("[A]", "<font color=\'#8d8d94\'>");
                                userTyping = userTyping.replace("[/A]", "</font>");

                                Spanned result = null;
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                    result = Html.fromHtml(userTyping,Html.FROM_HTML_MODE_LEGACY);
                                } else {
                                    result = Html.fromHtml(userTyping);
                                }

                                userTypingText.setText(result);
                                break;
                            }
                        }
                        userTypingLayout.setVisibility(View.VISIBLE);
                    }
                }

                int interval = 5000;
                IsTypingRunnable runnable = new IsTypingRunnable(userTypingTimeStamp, userHandleTyping);
                handlerReceive = new Handler();
                handlerReceive.postDelayed(runnable, interval);
            }
        }
        else if(chat.hasChanged(MegaChatRoom.CHANGE_TYPE_ARCHIVE)){
            log("CHANGE_TYPE_ARCHIVE for the chat: "+chat.getChatId());
            setChatSubtitle();
        }
        else if(chat.hasChanged(MegaChatRoom.CHANGE_TYPE_CHAT_MODE)){
            log("CHANGE_TYPE_CHAT_MODE for the chat: "+chat.getChatId());
        }
        else if(chat.hasChanged(MegaChatRoom.CHANGE_TYPE_UPDATE_PREVIEWERS)){
            log("CHANGE_TYPE_UPDATE_PREVIEWERS for the chat: "+chat.getChatId());
            setPreviewersView();
        }
    }

    void setPreviewersView () {
        if(chatRoom.getNumPreviewers()>0){
            observersNumberText.setText(chatRoom.getNumPreviewers()+"");
            observersLayout.setVisibility(View.VISIBLE);
        }
        else{
            observersLayout.setVisibility(View.GONE);
        }
    }

    private class IsTypingRunnable implements Runnable{

        long timeStamp;
        long userHandleTyping;

        public IsTypingRunnable(long timeStamp, long userHandleTyping) {
            this.timeStamp = timeStamp;
            this.userHandleTyping = userHandleTyping;
        }

        @Override
        public void run() {
            log("Run off notification typing");
            long timeNow = System.currentTimeMillis()/1000;
            if ((timeNow - timeStamp) > 4){
                log("Remove user from the list");

                boolean found = false;
                for(UserTyping user : usersTypingSync) {
                    if(user.getTimeStampTyping() == timeStamp) {
                        if(user.getParticipantTyping().getHandle() == userHandleTyping) {
                            log("Found user typing in runnable!");
                            usersTypingSync.remove(user);
                            found=true;
                            break;
                        }
                    }
                }

                if(!found){
                    log("Not found user typing in runnable!");
                }

                updateUserTypingFromNotification();
            }
        }
    }

    public void updateUserTypingFromNotification(){
        log("updateUserTypingFromNotification");

        int size = usersTypingSync.size();
        log("Size of typing: "+size);
        switch (size){
            case 0:{
                userTypingLayout.setVisibility(View.GONE);
                break;
            }
            case 1:{
                String userTyping = getResources().getQuantityString(R.plurals.user_typing, 1, usersTypingSync.get(0).getParticipantTyping().getFirstName());
                userTyping = toCDATA(userTyping);
                userTyping = userTyping.replace("[A]", "<font color=\'#8d8d94\'>");
                userTyping = userTyping.replace("[/A]", "</font>");

                Spanned result = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    result = Html.fromHtml(userTyping,Html.FROM_HTML_MODE_LEGACY);
                } else {
                    result = Html.fromHtml(userTyping);
                }

                userTypingText.setText(result);
                break;
            }
            case 2:{
                String userTyping = getResources().getQuantityString(R.plurals.user_typing, 2, usersTypingSync.get(0).getParticipantTyping().getFirstName()+", "+usersTypingSync.get(1).getParticipantTyping().getFirstName());
                userTyping = toCDATA(userTyping);
                userTyping = userTyping.replace("[A]", "<font color=\'#8d8d94\'>");
                userTyping = userTyping.replace("[/A]", "</font>");

                Spanned result = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    result = Html.fromHtml(userTyping,Html.FROM_HTML_MODE_LEGACY);
                } else {
                    result = Html.fromHtml(userTyping);
                }

                userTypingText.setText(result);
                break;
            }
            default:{
                String names = usersTypingSync.get(0).getParticipantTyping().getFirstName()+", "+usersTypingSync.get(1).getParticipantTyping().getFirstName();
                String userTyping = String.format(getString(R.string.more_users_typing), toCDATA(names));

                userTyping = userTyping.replace("[A]", "<font color=\'#8d8d94\'>");
                userTyping = userTyping.replace("[/A]", "</font>");

                Spanned result = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    result = Html.fromHtml(userTyping,Html.FROM_HTML_MODE_LEGACY);
                } else {
                    result = Html.fromHtml(userTyping);
                }

                userTypingText.setText(result);
                break;
            }
        }
    }

    public void setRichLinkInfo(long msgId, AndroidMegaRichLinkMessage richLinkMessage){
        log("setRichLinkInfo");

        int indexToChange = -1;
        ListIterator<AndroidMegaChatMessage> itr = messages.listIterator(messages.size());

        // Iterate in reverse.
        while(itr.hasPrevious()) {
            AndroidMegaChatMessage messageToCheck = itr.previous();

            if(!messageToCheck.isUploading()){
                if(messageToCheck.getMessage().getMsgId()==msgId){
                    indexToChange = itr.nextIndex();
                    log("Found index to change: "+indexToChange);
                    break;
                }
            }
        }

        if(indexToChange!=-1){

            AndroidMegaChatMessage androidMsg = messages.get(indexToChange);

            androidMsg.setRichLinkMessage(richLinkMessage);

            try{
                if(adapter!=null){
                    adapter.notifyItemChanged(indexToChange+1);
                }
            }
            catch(IllegalStateException e){
                log("IllegalStateException: do not update adapter");
            }

        }
        else{
            log("Error, rich link message not found!!");
        }
    }

    public void setRichLinkImage(long msgId){
        log("setRichLinkImage");

        int indexToChange = -1;
        ListIterator<AndroidMegaChatMessage> itr = messages.listIterator(messages.size());

        // Iterate in reverse.
        while(itr.hasPrevious()) {
            AndroidMegaChatMessage messageToCheck = itr.previous();

            if(!messageToCheck.isUploading()){
                if(messageToCheck.getMessage().getMsgId()==msgId){
                    indexToChange = itr.nextIndex();
                    log("Found index to change: "+indexToChange);
                    break;
                }
            }
        }

        if(indexToChange!=-1){

            if(adapter!=null){
                adapter.notifyItemChanged(indexToChange+1);
            }
        }
        else{
            log("Error, rich link message not found!!");
        }
    }

    public int checkMegaLink(MegaChatMessage msg){
        log("checkMegaLink");
        //Check if it is a MEGA link
        if(msg.getType()==MegaChatMessage.TYPE_NORMAL){
            if(msg.getContent()!=null){
                String link = AndroidMegaRichLinkMessage.extractMegaLink(msg.getContent());

                if(AndroidMegaRichLinkMessage.isChatLink(link)){

                    log("isChatLink");
                    ChatLinkInfoListener listener = new ChatLinkInfoListener(this, msg.getMsgId(), megaApi);
                    megaChatApi.checkChatLink(link, listener);

                    return MEGA_CHAT_LINK;
                }
                else{
                    boolean isFile = AndroidMegaRichLinkMessage.isFileLink(link);

                    if(link!=null){
                        log("The link found: "+link);
                        if(megaApi!=null && megaApi.getRootNode()!=null){
                            ChatLinkInfoListener listener = null;
                            if(isFile){
                                log("isFileLink");
                                listener = new ChatLinkInfoListener(this, msg.getMsgId(), megaApi);
                                megaApi.getPublicNode(link, listener);
                                return MEGA_FILE_LINK;
                            }
                            else{
                                log("isFolderLink");

                                MegaApiAndroid megaApiFolder = getLocalMegaApiFolder();
                                listener = new ChatLinkInfoListener(this, msg.getMsgId(), megaApi, megaApiFolder);
                                megaApiFolder.loginToFolder(link, listener);
                                return MEGA_FOLDER_LINK;
                            }

                        }
                    }
                }
            }
        }
        return -1;
    }

    @Override
    public void onMessageLoaded(MegaChatApiJava api, MegaChatMessage msg) {
        log("onMessageLoaded!------------------------");

        if(msg!=null){
            log("STATUS: "+msg.getStatus());
            log("TEMP ID: "+msg.getTempId());
            log("FINAL ID: "+msg.getMsgId());
            log("TIMESTAMP: "+msg.getTimestamp());
            log("TYPE: "+msg.getType());

            if(messages!=null){
                log("Messages size: "+messages.size());
            }

            if(msg.isDeleted()){
                log("DELETED MESSAGE!!!!");
                return;
            }

            if(msg.isEdited()){
                log("EDITED MESSAGE!!!!");
            }

            if(msg.getType()==MegaChatMessage.TYPE_REVOKE_NODE_ATTACHMENT) {
                log("TYPE_REVOKE_NODE_ATTACHMENT MESSAGE!!!!");
                return;
            }

            checkMegaLink(msg);

            if(msg.getType()==MegaChatMessage.TYPE_NODE_ATTACHMENT){
                log("TYPE_NODE_ATTACHMENT MESSAGE!!!!");
                MegaNodeList nodeList = msg.getMegaNodeList();
                int revokedCount = 0;

                for(int i=0; i<nodeList.size(); i++){
                    MegaNode node = nodeList.get(i);
                    boolean revoked = megaChatApi.isRevoked(idChat, node.getHandle());
                    if(revoked){
                        log("The node is revoked: "+node.getName());
                        revokedCount++;
                    }
                    else{
                        log("Node NOT revoked: "+node.getName());
                    }
                }

                if(revokedCount==nodeList.size()){
                    log("RETURN");
                    return;
                }
            }

            if(msg.getStatus()==MegaChatMessage.STATUS_SERVER_REJECTED){
                log("onMessageLoaded: STATUS_SERVER_REJECTED----- "+msg.getStatus());
            }

            if(msg.getStatus()==MegaChatMessage.STATUS_SENDING_MANUAL){

                log("MANUAL_S:onMessageLoaded: Getting messages not sent !!!-------------------------------------------------: "+msg.getStatus());
                AndroidMegaChatMessage androidMsg = new AndroidMegaChatMessage(msg);

                if(msg.isEdited()){
                    log("MESSAGE EDITED");

                    if(!noMoreNoSentMessages){
                        log("onMessageLoaded: NOT noMoreNoSentMessages");
                        bufferSending.add(0,androidMsg);
                    }
                    else{
                        log("Try to recover the initial msg");
                        if(msg.getMsgId()!=-1){
                            MegaChatMessage notEdited = megaChatApi.getMessage(idChat, msg.getMsgId());
                            log("Content not edited");
                            AndroidMegaChatMessage androidMsgNotEdited = new AndroidMegaChatMessage(notEdited);
                            int returnValue = modifyMessageReceived(androidMsgNotEdited, false);
                            if(returnValue!=-1){
                                log("onMessageLoaded: Message " + returnValue + " modified!");
                            }
                        }

                        appendMessageAnotherMS(androidMsg);
                    }
                }
                else{
                    log("NOOOT MESSAGE EDITED");
                    int resultModify = -1;
                    if(msg.getUserHandle()==megaChatApi.getMyUserHandle()){
                        if(msg.getType()==MegaChatMessage.TYPE_NODE_ATTACHMENT){
                            log("Modify my message and node attachment");

                            long idMsg =  dbH.findPendingMessageByIdTempKarere(msg.getTempId());
                            log("----The id of my pending message is: "+idMsg);
                            if(idMsg!=-1){
                                resultModify = modifyAttachmentReceived(androidMsg, idMsg);
                                dbH.removePendingMessageById(idMsg);
                                if(resultModify==-1){
                                    log("Node attachment message not in list -> resultModify -1");
//                            AndroidMegaChatMessage msgToAppend = new AndroidMegaChatMessage(msg);
//                            appendMessagePosition(msgToAppend);
                                }
                                else{
                                    log("onMessageLoaded: Modify attachment");
                                    return;
                                }
                            }
                        }
                    }

                    int returnValue = modifyMessageReceived(androidMsg, true);
                    if(returnValue!=-1){
                        log("onMessageLoaded: Message " + returnValue + " modified!");
                        return;
                    }

                    bufferSending.add(0,androidMsg);

                    if(!noMoreNoSentMessages){
                        log("onMessageLoaded: NOT noMoreNoSentMessages");
                    }
                }
            }
            else if(msg.getStatus()==MegaChatMessage.STATUS_SENDING){
                log("SENDING: onMessageLoaded: Getting messages not sent !!!-------------------------------------------------: "+msg.getStatus());
                AndroidMegaChatMessage androidMsg = new AndroidMegaChatMessage(msg);
                int returnValue = modifyMessageReceived(androidMsg, true);
                if(returnValue!=-1){
                    log("onMessageLoaded: Message " + returnValue + " modified!");
                    return;
                }

                bufferSending.add(0,androidMsg);

                if(!noMoreNoSentMessages){
                    log("onMessageLoaded: NOT noMoreNoSentMessages");
                }
            }
            else{
                if(!noMoreNoSentMessages){
                    log("First message with NORMAL status");
                    noMoreNoSentMessages=true;
                    //Copy to bufferMessages
                    for(int i=0;i<bufferSending.size();i++){
                        bufferMessages.add(bufferSending.get(i));
                    }
                    bufferSending.clear();
                }

                AndroidMegaChatMessage androidMsg = new AndroidMegaChatMessage(msg);

                if (lastIdMsgSeen != -1) {
                    if(lastIdMsgSeen ==msg.getMsgId()){
                        log("onMessageLoaded: Last message seen received!");
                        lastSeenReceived=true;
                        positionToScroll = 0;
                        log("positionToScroll: "+positionToScroll);
                    }
                }
                else{
                    log("lastMessageSeen is -1");
                    lastSeenReceived=true;
                }

//                megaChatApi.setMessageSeen(idChat, msg.getMsgId());

                if(positionToScroll>=0){
                    positionToScroll++;
                    log("positionToScroll:increase: "+positionToScroll);
                }

                bufferMessages.add(androidMsg);
                log("onMessageLoaded: Size of buffer: "+bufferMessages.size());
                log("onMessageLoaded: Size of messages: "+messages.size());
            }
        }
        else{
            log("onMessageLoaded:NULLmsg:REACH FINAL HISTORY:stateHistory "+stateHistory);

            if(bufferSending.size()!=0){
                for(int i=0;i<bufferSending.size();i++){
                    bufferMessages.add(bufferSending.get(i));
                }
                bufferSending.clear();
            }

            log("**onMessageLoaded:numberToLoad: "+numberToLoad+" bufferSize: "+bufferMessages.size()+" messagesSize: "+messages.size());
            if((bufferMessages.size()+messages.size())>=numberToLoad){
                log("**onMessageLoaded:******");
                fullHistoryReceivedOnLoad();
                isLoadingHistory = false;
            }
            else if(((bufferMessages.size()+messages.size())<numberToLoad) && (stateHistory==MegaChatApi.SOURCE_ERROR)){
                log("**onMessageLoaded:noMessagesLoaded&SOURCE_ERROR: wait to CHAT ONLINE connection");
                retryHistory = true;
            }
            else{
                log("**onMessageLoaded:lessNumberReceived");
                if((stateHistory!=MegaChatApi.SOURCE_NONE)&&(stateHistory!=MegaChatApi.SOURCE_ERROR)){
                    log("But more history exists --> loadMessages");
                    log("G->loadMessages unread is 0");
                    isLoadingHistory = true;
                    stateHistory = megaChatApi.loadMessages(idChat, NUMBER_MESSAGES_TO_LOAD);
                    log("New state of history: "+stateHistory);
                    getMoreHistory = false;
                    if(stateHistory==MegaChatApi.SOURCE_NONE || stateHistory==MegaChatApi.SOURCE_ERROR){
                        fullHistoryReceivedOnLoad();
                        isLoadingHistory = false;
                    }
                }
                else{
                    fullHistoryReceivedOnLoad();
                    isLoadingHistory = false;
                }
            }
        }
        log("END onMessageLoaded-----------messages.size="+messages.size());
    }

    public void fullHistoryReceivedOnLoad(){
        log("fullHistoryReceivedOnLoad");

        isOpeningChat = false;

        if(bufferMessages.size()!=0){
            log("fullHistoryReceivedOnLoad:buffer size: "+bufferMessages.size());
            loadBufferMessages();

            if(lastSeenReceived==false){
                log("fullHistoryReceivedOnLoad: last message seen NOT received");
                if(stateHistory!=MegaChatApi.SOURCE_NONE){
                    log("fullHistoryReceivedOnLoad:F->loadMessages");
                    isLoadingHistory = true;
                    stateHistory = megaChatApi.loadMessages(idChat, NUMBER_MESSAGES_TO_LOAD);
                }
            }
            else{
                log("fullHistoryReceivedOnLoad: last message seen received");
                if(positionToScroll>0){
                    log("fullHistoryReceivedOnLoad: Scroll to position: "+positionToScroll);
                    if(positionToScroll<messages.size()){
//                        mLayoutManager.scrollToPositionWithOffset(positionToScroll+1,Util.scaleHeightPx(50, outMetrics));
                        //Find last message
                        int positionLastMessage = -1;
                        for(int i=messages.size()-1; i>=0;i--) {
                            AndroidMegaChatMessage androidMessage = messages.get(i);

                            if (!androidMessage.isUploading()) {

                                MegaChatMessage msg = androidMessage.getMessage();
                                if (msg.getMsgId() == lastIdMsgSeen) {
                                    positionLastMessage = i;
                                    break;
                                }
                            }
                        }

                        //Check if it has no my messages after
                        positionLastMessage = positionLastMessage + 1;
                        AndroidMegaChatMessage message = messages.get(positionLastMessage);

                        while(message.getMessage().getUserHandle()==megaChatApi.getMyUserHandle()){
                            lastIdMsgSeen = message.getMessage().getMsgId();
                            positionLastMessage = positionLastMessage + 1;
                            message = messages.get(positionLastMessage);
                        }

                        if(isTurn){
                            scrollToMessage(-1);

                        }else{
                            scrollToMessage(lastIdMsgSeen);
                        }

                    }
                    else{
                        log("Error, the position to scroll is more than size of messages");
                    }
                }
            }

            setLastMessageSeen();
        }
        else{
            log("fullHistoryReceivedOnLoad:bufferEmpty");
        }

        log("fullHistoryReceivedOnLoad:getMoreHistoryTRUE");
        getMoreHistory = true;

        //Load pending messages
        if(!pendingMessagesLoaded){
            pendingMessagesLoaded = true;
            loadPendingMessages();
            if(positionToScroll<=0){
                mLayoutManager.scrollToPosition(messages.size());
            }
        }

        chatRelativeLayout.setVisibility(View.VISIBLE);
        emptyLayout.setVisibility(View.GONE);
    }

    @Override
    public void onMessageReceived(MegaChatApiJava api, MegaChatMessage msg) {
        log("onMessageReceived!");
        log("------------------------------------------"+api.getChatConnectionState(idChat));
        log("STATUS: "+msg.getStatus());
        log("TEMP ID: "+msg.getTempId());
        log("FINAL ID: "+msg.getMsgId());
        log("TIMESTAMP: "+msg.getTimestamp());
        log("TYPE: "+msg.getType());

        if(msg.getType()==MegaChatMessage.TYPE_REVOKE_NODE_ATTACHMENT) {
            log("TYPE_REVOKE_NODE_ATTACHMENT MESSAGE!!!!");
            return;
        }

        if(msg.getStatus()==MegaChatMessage.STATUS_SERVER_REJECTED){
            log("onMessageReceived: STATUS_SERVER_REJECTED----- "+msg.getStatus());
        }

        if(!msg.isManagementMessage()){
            if(positionNewMessagesLayout!=-1){
                log("Layout unread messages shown: "+generalUnreadCount);
                if(generalUnreadCount<0){
                    generalUnreadCount--;
                }
                else{
                    generalUnreadCount++;
                }

                if(adapter!=null){
                    adapter.notifyItemChanged(positionNewMessagesLayout);
                }
            }
        }
        else {
            int messageType = msg.getType();
            log("Message type: " + messageType);

            switch (messageType) {
                case MegaChatMessage.TYPE_ALTER_PARTICIPANTS:{
                    if(msg.getUserHandle()==myUserHandle) {
                        log("me alter participant");
                        hideNewMessagesLayout();
                    }
                    break;
                }
                case MegaChatMessage.TYPE_PRIV_CHANGE:{
                    if(msg.getUserHandle()==myUserHandle){
                        log("I change a privilege");
                        hideNewMessagesLayout();
                    }
                    break;
                }
                case MegaChatMessage.TYPE_CHAT_TITLE:{
                    if(msg.getUserHandle()==myUserHandle) {
                        log("I change the title");
                        hideNewMessagesLayout();
                    }
                    break;
                }
            }
        }

        if(setAsRead){
            markAsSeen(msg);
        }

        if(msg.getType()==MegaChatMessage.TYPE_CHAT_TITLE){
            log("Change of chat title");
            String newTitle = msg.getContent();
            if(newTitle!=null){

                titleToolbar.setText(newTitle);
            }
        }
        else if(msg.getType()==MegaChatMessage.TYPE_TRUNCATE){
            invalidateOptionsMenu();
        }

        AndroidMegaChatMessage androidMsg = new AndroidMegaChatMessage(msg);
        appendMessagePosition(androidMsg);

        if(mLayoutManager.findLastCompletelyVisibleItemPosition()==messages.size()-1){
            log("Do scroll to end");
            mLayoutManager.scrollToPosition(messages.size());
        }
        else{
            if(emojiKeyboard !=null){
                if((emojiKeyboard.getLetterKeyboardShown() || emojiKeyboard.getEmojiKeyboardShown())&&(messages.size()==1)){
                    mLayoutManager.scrollToPosition(messages.size());
                }
            }
            log("DONT scroll to end");
            if(typeMessageJump !=  TYPE_MESSAGE_NEW_MESSAGE){
                messageJumpText.setText(getResources().getString(R.string.message_new_messages));
                typeMessageJump = TYPE_MESSAGE_NEW_MESSAGE;
            }

            if(messageJumpLayout.getVisibility() != View.VISIBLE){
                messageJumpText.setText(getResources().getString(R.string.message_new_messages));
                messageJumpLayout.setVisibility(View.VISIBLE);
            }
        }

        checkMegaLink(msg);

//        mLayoutManager.setStackFromEnd(true);
//        mLayoutManager.scrollToPosition(0);
    }

    @Override
    public void onMessageUpdate(MegaChatApiJava api, MegaChatMessage msg) {
        log("onMessageUpdate!: "+ msg.getMsgId());

        int resultModify = -1;
        if(msg.isDeleted()){
            log("The message has been deleted");
            deleteMessage(msg, false);
            return;
        }

        AndroidMegaChatMessage androidMsg = new AndroidMegaChatMessage(msg);

        if(msg.hasChanged(MegaChatMessage.CHANGE_TYPE_ACCESS)){
            log("Change access of the message");

            MegaNodeList nodeList = msg.getMegaNodeList();
            int revokedCount = 0;

            for(int i=0; i<nodeList.size(); i++){
                MegaNode node = nodeList.get(i);
                boolean revoked = megaChatApi.isRevoked(idChat, node.getHandle());
                if(revoked){
                    log("The node is revoked: "+node.getName());
                    revokedCount++;
                }
                else{
                    log("Node not revoked: "+node.getName());
                }
            }

            if(revokedCount==nodeList.size()){
                log("All the attachments have been revoked");
                deleteMessage(msg, false);
            }
            else{
                log("One attachment revoked, modify message");
                resultModify = modifyMessageReceived(androidMsg, false);

                if(resultModify==-1) {
                    log("Modify result is -1");
                    int firstIndexShown = messages.get(0).getMessage().getMsgIndex();
                    log("The first index is: "+firstIndexShown+ " the index of the updated message: "+msg.getMsgIndex());
                    if(firstIndexShown<=msg.getMsgIndex()){
                        log("The message should be in the list");
                        if(msg.getType()==MegaChatMessage.TYPE_NODE_ATTACHMENT){

                            log("A - Node attachment message not in list -> append");
                            AndroidMegaChatMessage msgToAppend = new AndroidMegaChatMessage(msg);
                            reinsertNodeAttachmentNoRevoked(msgToAppend);
                        }
                    }
                    else{
                        if(messages.size()<NUMBER_MESSAGES_BEFORE_LOAD){
                            log("Show more message - add to the list");
                            if(msg.getType()==MegaChatMessage.TYPE_NODE_ATTACHMENT){

                                log("B - Node attachment message not in list -> append");
                                AndroidMegaChatMessage msgToAppend = new AndroidMegaChatMessage(msg);
                                reinsertNodeAttachmentNoRevoked(msgToAppend);
                            }
                        }
                    }

                }
            }
        }
        else if(msg.hasChanged(MegaChatMessage.CHANGE_TYPE_CONTENT)){
            log("Change content of the message");

            if(msg.getType()==MegaChatMessage.TYPE_TRUNCATE){
                log("TRUNCATE MESSAGE");
                clearHistory(androidMsg);
            }
            else{
                if(msg.isDeleted()){
                    log("Message deleted!!");
                }

                checkMegaLink(msg);

                resultModify = modifyMessageReceived(androidMsg, false);
                log("onMessageUpdate: resultModify: "+resultModify);
            }
        }
        else if(msg.hasChanged(MegaChatMessage.CHANGE_TYPE_STATUS)){

            int statusMsg = msg.getStatus();
            log("Status change: "+statusMsg + "T emporal id: "+msg.getTempId() + " Final id: "+msg.getMsgId());

            if(msg.getUserHandle()==megaChatApi.getMyUserHandle()){
                if(msg.getType()==MegaChatMessage.TYPE_NODE_ATTACHMENT){
                    log("Modify my message and node attachment");

                    long idMsg =  dbH.findPendingMessageByIdTempKarere(msg.getTempId());
                    log("----The id of my pending message is: "+idMsg);
                    if(idMsg!=-1){
                        resultModify = modifyAttachmentReceived(androidMsg, idMsg);
                        if(resultModify==-1){
                            log("Node attachment message not in list -> resultModify -1");
//                            AndroidMegaChatMessage msgToAppend = new AndroidMegaChatMessage(msg);
//                            appendMessagePosition(msgToAppend);
                        }
                        else{
                            dbH.removePendingMessageById(idMsg);
                        }
                        return;
                    }
                }
            }

            if(msg.getStatus()==MegaChatMessage.STATUS_SEEN){
                log("STATUS_SEEN");
            }
            else if(msg.getStatus()==MegaChatMessage.STATUS_SERVER_RECEIVED){
                log("STATUS_SERVER_RECEIVED");

                if(msg.getType()==MegaChatMessage.TYPE_NORMAL){
                    if(msg.getUserHandle()==megaChatApi.getMyUserHandle()){
                        checkMegaLink(msg);
                    }
                }

                resultModify = modifyMessageReceived(androidMsg, true);
                log("onMessageUpdate: resultModify: "+resultModify);
            }
            else if(msg.getStatus()==MegaChatMessage.STATUS_SERVER_REJECTED){
                log("onMessageUpdate: STATUS_SERVER_REJECTED----- "+msg.getStatus());
                deleteMessage(msg, true);
            }
            else{
                log("-----------Status : "+msg.getStatus());
                log("-----------Timestamp: "+msg.getTimestamp());

                resultModify = modifyMessageReceived(androidMsg, false);
                log("onMessageUpdate: resultModify: "+resultModify);
            }
        }
    }

    @Override
    public void onHistoryReloaded(MegaChatApiJava api, MegaChatRoom chat) {
        log("onHistoryReloaded");
        bufferMessages.clear();
        messages.clear();

        invalidateOptionsMenu();
        log("Load new history");

        long unread = chatRoom.getUnreadCount();
        //                        stateHistory = megaChatApi.loadMessages(idChat, NUMBER_MESSAGES_TO_LOAD);
        if (unread == 0) {
            lastIdMsgSeen = -1;
            generalUnreadCount = -1;
            lastSeenReceived = true;
            log("onHistoryReloaded: loadMessages unread is 0");
        } else {
            lastIdMsgSeen = megaChatApi.getLastMessageSeenId(idChat);
            generalUnreadCount = unread;

            if (lastIdMsgSeen != -1) {
                log("onHistoryReloaded: Id of last message seen: " + lastIdMsgSeen);
            } else {
                log("onHistoryReloaded: Error the last message seen shouldn't be NULL");
            }

            lastSeenReceived = false;
        }
    }

    public void deleteMessage(MegaChatMessage msg, boolean rejected){
        log("deleteMessage");

        int indexToChange = -1;

        ListIterator<AndroidMegaChatMessage> itr = messages.listIterator(messages.size());

        // Iterate in reverse.
        while(itr.hasPrevious()) {
            AndroidMegaChatMessage messageToCheck = itr.previous();
            log("Index: " + itr.nextIndex());

            if(!messageToCheck.isUploading()){
                if(rejected){
                    if (messageToCheck.getMessage().getTempId() == msg.getTempId()) {
                        indexToChange = itr.nextIndex();
                        break;
                    }
                }
                else{
                    if (messageToCheck.getMessage().getMsgId() == msg.getMsgId()) {
                        indexToChange = itr.nextIndex();
                        break;
                    }
                }
            }
        }

        if(indexToChange!=-1) {
            messages.remove(indexToChange);
            log("Removed index: " + indexToChange + " positionNewMessagesLayout: "+ positionNewMessagesLayout +" messages size: " + messages.size());
//                adapter.notifyDataSetChanged();

            if(positionNewMessagesLayout<=indexToChange){
                if(generalUnreadCount==1 || generalUnreadCount==-1){
                    log("Reset generalUnread:Position where new messages layout is show: " + positionNewMessagesLayout);
                    generalUnreadCount = 0;
                    lastIdMsgSeen = -1;
                }
                else{
                    log("Decrease generalUnread:Position where new messages layout is show: " + positionNewMessagesLayout);
                    generalUnreadCount--;
                }
                adapter.notifyItemChanged(positionNewMessagesLayout);
            }

            if(!messages.isEmpty()){
                //Update infoToShow of the next message also
                if (indexToChange == 0) {
                    messages.get(indexToChange).setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_ALL);
                    //Check if there is more messages and update the following one
                    if(messages.size()>1){
                        adjustInfoToShow(indexToChange+1);
                        setShowAvatar(indexToChange+1);
                    }
                }
                else{
                    //Not first element
                    if(indexToChange==messages.size()){
                        log("The last message removed, do not check more messages");
                        setShowAvatar(indexToChange-1);
                        return;
                    }

                    adjustInfoToShow(indexToChange);
                    setShowAvatar(indexToChange);
                    setShowAvatar(indexToChange-1);
                }
            }

            adapter.removeMessage(indexToChange+1, messages);
        }
        else{
            log("index to change not found");
        }
    }

    public int modifyAttachmentReceived(AndroidMegaChatMessage msg, long idPendMsg){
        log("modifyAttachmentReceived: id: "+msg.getMessage().getMsgId()+" tempID: "+msg.getMessage().getTempId()+" status: "+msg.getMessage().getStatus());
        int indexToChange = -1;
        ListIterator<AndroidMegaChatMessage> itr = messages.listIterator(messages.size());

        // Iterate in reverse.
        while(itr.hasPrevious()) {
            AndroidMegaChatMessage messageToCheck = itr.previous();

            if(messageToCheck.getPendingMessage()!=null){
                log("pending id: "+messageToCheck.getPendingMessage().getId() + " other: "+ idPendMsg);
                log("pending id: "+messageToCheck.getPendingMessage().getId() + " other: "+ idPendMsg);
                if(messageToCheck.getPendingMessage().getId()==idPendMsg){
                    indexToChange = itr.nextIndex();
                    log("Found index to change: "+indexToChange);
                    break;
                }
            }
        }

        if(indexToChange!=-1){

            log("modifyAttachmentReceived: INDEX change, need to reorder");
            messages.remove(indexToChange);
            log("Removed index: "+indexToChange);
            log("modifyAttachmentReceived: messages size: "+messages.size());
            adapter.removeMessage(indexToChange+1, messages);

            int scrollToP = appendMessagePosition(msg);
            if(scrollToP!=-1){
                if(msg.getMessage().getStatus()==MegaChatMessage.STATUS_SERVER_RECEIVED){
                    log("modifyAttachmentReceived: need to scroll to position: "+indexToChange);
                    final int indexToScroll = scrollToP+1;
                    mLayoutManager.scrollToPositionWithOffset(indexToScroll,Util.scaleHeightPx(20, outMetrics));

                }
            }
        }
        else{
            log("Error, id pending message message not found!!");
        }
        log("Index modified: "+indexToChange);
        return indexToChange;
    }


    public int modifyMessageReceived(AndroidMegaChatMessage msg, boolean checkTempId){
        log("modifyMessageReceived");
        log("Msg ID: "+msg.getMessage().getMsgId());
        log("Msg TEMP ID: "+msg.getMessage().getTempId());
        log("Msg status: "+msg.getMessage().getStatus());
        int indexToChange = -1;
        ListIterator<AndroidMegaChatMessage> itr = messages.listIterator(messages.size());

        // Iterate in reverse.
        while(itr.hasPrevious()) {
            AndroidMegaChatMessage messageToCheck = itr.previous();
            log("Index: "+itr.nextIndex());

            if(!messageToCheck.isUploading()){
                log("Checking with Msg ID: "+messageToCheck.getMessage().getMsgId());
                log("Checking with Msg TEMP ID: "+messageToCheck.getMessage().getTempId());

                if(checkTempId){
                    log("Check temporal IDS----");
                    if (messageToCheck.getMessage().getTempId() == msg.getMessage().getTempId()) {
                        log("modifyMessageReceived with idTemp");
                        indexToChange = itr.nextIndex();
                        break;
                    }
                }
                else{
                    if (messageToCheck.getMessage().getMsgId() == msg.getMessage().getMsgId()) {
                        log("modifyMessageReceived");
                        indexToChange = itr.nextIndex();
                        break;
                    }
                }
            }
            else{

                log("This message is uploading");
            }
        }

        log("---------------Index to change = "+indexToChange);
        if(indexToChange!=-1){
            log("indexToChange == "+indexToChange);

            AndroidMegaChatMessage messageToUpdate = messages.get(indexToChange);
            if(messageToUpdate.getMessage().getMsgIndex()==msg.getMessage().getMsgIndex()){
                log("modifyMessageReceived: The internal index not change");

                if(msg.getMessage().getStatus()==MegaChatMessage.STATUS_SENDING_MANUAL){
                    log("Modified a MANUAl SENDING msg");
                    //Check the message to change is not the last one
                    int lastI = messages.size()-1;
                    if(indexToChange<lastI){
                        //Check if there is already any MANUAL_SENDING in the queue
                        AndroidMegaChatMessage previousMessage = messages.get(lastI);
                        if(previousMessage.isUploading()){
                            log("Previous message is uploading");
                        }
                        else{
                            if(previousMessage.getMessage().getStatus()==MegaChatMessage.STATUS_SENDING_MANUAL){
                                log("More MANUAL SENDING in queue");
                                log("Removed index: "+indexToChange);
                                messages.remove(indexToChange);
                                appendMessageAnotherMS(msg);
                                adapter.notifyDataSetChanged();
                                return indexToChange;
                            }
                        }
                    }
                }

                log("Modified message keep going");
                messages.set(indexToChange, msg);

                //Update infoToShow also
                if (indexToChange == 0) {
                    messages.get(indexToChange).setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_ALL);
                    messages.get(indexToChange).setShowAvatar(true);
                }
                else{
                    //Not first element
                    adjustInfoToShow(indexToChange);
                    setShowAvatar(indexToChange);

                    //Create adapter
                    if (adapter == null) {
                        adapter = new MegaChatLollipopAdapter(this, chatRoom, messages, listView);
                        adapter.setHasStableIds(true);
                        listView.setAdapter(adapter);
                    } else {
                        adapter.modifyMessage(messages, indexToChange+1);
                    }
                }
            }
            else{
                log("modifyMessageReceived: INDEX change, need to reorder");
                messages.remove(indexToChange);
                log("Removed index: "+indexToChange);
                log("modifyMessageReceived: messages size: "+messages.size());
                adapter.removeMessage(indexToChange+1, messages);
                int scrollToP = appendMessagePosition(msg);
                if(scrollToP!=-1){
                    if(msg.getMessage().getStatus()==MegaChatMessage.STATUS_SERVER_RECEIVED){
                        mLayoutManager.scrollToPosition(scrollToP+1);
                        //mLayoutManager.scrollToPositionWithOffset(scrollToP, Util.scaleHeightPx(20, outMetrics));
                    }
                }
                log("modifyMessageReceived: messages size 2: "+messages.size());
            }

        }
        else{
            log("indexToChange == -1");

            log("Error, id temp message not found!!");
        }
        return indexToChange;
    }

    public void loadBufferMessages(){
        log("loadBufferMessages");
        ListIterator<AndroidMegaChatMessage> itr = bufferMessages.listIterator();
        while (itr.hasNext()) {
            int currentIndex = itr.nextIndex();
            AndroidMegaChatMessage messageToShow = itr.next();
            loadMessage(messageToShow);
        }

        //Create adapter
        if(adapter==null){
            adapter = new MegaChatLollipopAdapter(this, chatRoom, messages, listView);
            adapter.setHasStableIds(true);
            listView.setLayoutManager(mLayoutManager);
            listView.setAdapter(adapter);
            adapter.setMessages(messages);
        }
        else{
            adapter.loadPreviousMessages(messages, bufferMessages.size());

            log("addMessage: "+messages.size());
        }

        log("AFTER updateMessagesLoaded: "+messages.size()+" messages in list");

        bufferMessages.clear();
    }

    public void clearHistory(AndroidMegaChatMessage androidMsg){
        log("clearHistory");

        ListIterator<AndroidMegaChatMessage> itr = messages.listIterator(messages.size());

        int indexToChange=-1;
        // Iterate in reverse.
        while(itr.hasPrevious()) {
            AndroidMegaChatMessage messageToCheck = itr.previous();

            if(!messageToCheck.isUploading()){
                if(messageToCheck.getMessage().getStatus()!=MegaChatMessage.STATUS_SENDING){

                    indexToChange = itr.nextIndex();
                    log("Found index of last sent and confirmed message: "+indexToChange);
                    break;
                }
            }
        }

//        indexToChange = 2;
        if(indexToChange != messages.size()-1){
            log("Clear history of confirmed messages: "+indexToChange);

            List<AndroidMegaChatMessage> messagesCopy = new ArrayList<>(messages);
            messages.clear();
            messages.add(androidMsg);
            for(int i = indexToChange+1; i<messagesCopy.size();i++){
                messages.add(messagesCopy.get(i));
            }
        }
        else{
            log("Clear all messages");
            messages.clear();
            messages.add(androidMsg);
        }

        if(messages.size()==1){
            androidMsg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_ALL);
        }
        else{
            for(int i=0; i<messages.size();i++){
                adjustInfoToShow(i);
            }
        }

        adapter.setMessages(messages);
    }

    public void loadPendingMessages(){
        log("loadPendingMessages");
        ArrayList<AndroidMegaChatMessage> pendMsgs = dbH.findPendingMessagesNotSent(idChat);
//        dbH.findPendingMessagesBySent(1);
        log("Number of pending: "+pendMsgs.size());

        for(int i=0;i<pendMsgs.size();i++){
            AndroidMegaChatMessage pMsg = pendMsgs.get(i);
            if(pMsg!=null && pMsg.getPendingMessage()!=null){
                if(pMsg.getPendingMessage().getState()==PendingMessageSingle.STATE_PREPARING){
                    if(pMsg.getPendingMessage().getTransferTag()!=-1){
                        log("STATE_PREPARING:Transfer tag: "+pMsg.getPendingMessage().getTransferTag());
                        if(megaApi!=null) {
                            MegaTransfer t = megaApi.getTransferByTag(pMsg.getPendingMessage().getTransferTag());
                            if(t!=null){
                                if(t.getState()==MegaTransfer.STATE_COMPLETED){
                                    dbH.updatePendingMessageOnTransferFinish(pMsg.getPendingMessage().getId(), "-1", PendingMessageSingle.STATE_SENT);
                                }
                                else if(t.getState()==MegaTransfer.STATE_CANCELLED){
                                    dbH.updatePendingMessageOnTransferFinish(pMsg.getPendingMessage().getId(), "-1", PendingMessageSingle.STATE_SENT);
                                }
                                else if(t.getState()==MegaTransfer.STATE_FAILED){
                                    dbH.updatePendingMessageOnTransferFinish(pMsg.getPendingMessage().getId(), "-1", PendingMessageSingle.STATE_ERROR_UPLOADING);
                                    pMsg.getPendingMessage().setState(PendingMessageSingle.STATE_ERROR_UPLOADING);
                                    appendMessagePosition(pMsg);
                                }
                                else{
                                    log("STATE_PREPARING:Found transfer in progress for the message");
                                    appendMessagePosition(pMsg);
                                }
                            }
                            else{
                                log("STATE_PREPARING:Mark message as error uploading - no transfer in progress");
                                dbH.updatePendingMessageOnTransferFinish(pMsg.getPendingMessage().getId(), "-1", PendingMessageSingle.STATE_ERROR_UPLOADING);
                                pMsg.getPendingMessage().setState(PendingMessageSingle.STATE_ERROR_UPLOADING);
                                appendMessagePosition(pMsg);
                            }
                        }
                    }
                }
                else if(pMsg.getPendingMessage().getState()==PendingMessageSingle.STATE_PREPARING_FROM_EXPLORER){
                    log("STATE_PREPARING_FROM_EXPLORER: Convert to STATE_PREPARING");
                    dbH.updatePendingMessageOnTransferFinish(pMsg.getPendingMessage().getId(), "-1", PendingMessageSingle.STATE_PREPARING);
                    pMsg.getPendingMessage().setState(PendingMessageSingle.STATE_PREPARING);
                    appendMessagePosition(pMsg);
                }
                else if(pMsg.getPendingMessage().getState()==PendingMessageSingle.STATE_UPLOADING){
                    if(pMsg.getPendingMessage().getTransferTag()!=-1){
                        log("STATE_UPLOADING:Transfer tag: "+pMsg.getPendingMessage().getTransferTag());
                        if(megaApi!=null){
                            MegaTransfer t = megaApi.getTransferByTag(pMsg.getPendingMessage().getTransferTag());
                            if(t!=null){
                                if(t.getState()==MegaTransfer.STATE_COMPLETED){
                                    dbH.updatePendingMessageOnTransferFinish(pMsg.getPendingMessage().getId(), "-1", PendingMessageSingle.STATE_SENT);
                                }
                                else if(t.getState()==MegaTransfer.STATE_CANCELLED){
                                    dbH.updatePendingMessageOnTransferFinish(pMsg.getPendingMessage().getId(), "-1", PendingMessageSingle.STATE_SENT);
                                }
                                else if(t.getState()==MegaTransfer.STATE_FAILED){
                                    dbH.updatePendingMessageOnTransferFinish(pMsg.getPendingMessage().getId(), "-1", PendingMessageSingle.STATE_ERROR_UPLOADING);
                                    pMsg.getPendingMessage().setState(PendingMessageSingle.STATE_ERROR_UPLOADING);
                                    appendMessagePosition(pMsg);
                                }
                                else{
                                    log("STATE_UPLOADING:Found transfer in progress for the message");
                                    appendMessagePosition(pMsg);
                                }
                            }
                            else{
                                log("STATE_UPLOADING:Mark message as error uploading - no transfer in progress");
                                dbH.updatePendingMessageOnTransferFinish(pMsg.getPendingMessage().getId(), "-1", PendingMessageSingle.STATE_ERROR_UPLOADING);
                                pMsg.getPendingMessage().setState(PendingMessageSingle.STATE_ERROR_UPLOADING);
                                appendMessagePosition(pMsg);
                            }
                        }
                    }
                }
                else{
                    appendMessagePosition(pMsg);
                }
            }
            else{
                log("Null pending messages");
            }
        }
    }

    public void loadMessage(AndroidMegaChatMessage messageToShow){
        log("loadMessage");

        messageToShow.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_ALL);
        messages.add(0,messageToShow);

        if(messages.size()>1) {
            adjustInfoToShow(1);
        }

        setShowAvatar(0);

        if(adapter.isMultipleSelect()){
            adapter.updateSelectionOnScroll();
        }
    }

    public void appendMessageAnotherMS(AndroidMegaChatMessage msg){
        log("appendMessageAnotherMS");
        messages.add(msg);
        int lastIndex = messages.size()-1;

        if(lastIndex==0){
            messages.get(lastIndex).setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_ALL);
        }
        else{
            adjustInfoToShow(lastIndex);
        }

        //Create adapter
        if(adapter==null){
            log("Create adapter");
            adapter = new MegaChatLollipopAdapter(this, chatRoom, messages, listView);
            adapter.setHasStableIds(true);
            listView.setLayoutManager(mLayoutManager);
            listView.setAdapter(adapter);
            adapter.setMessages(messages);
        }
        else{
            log("Update adapter with last index: "+lastIndex);
            if(lastIndex==0){
                log("Arrives the first message of the chat");
                adapter.setMessages(messages);
            }
            else{
                adapter.addMessage(messages, lastIndex+1);
            }
        }
    }

    public int reinsertNodeAttachmentNoRevoked(AndroidMegaChatMessage msg){
        log("reinsertNodeAttachmentNoRevoked");
        int lastIndex = messages.size()-1;
        log("1lastIndex: "+lastIndex);
        if(messages.size()==-1){
            log("2lastIndex: "+lastIndex);
            msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_ALL);
            messages.add(msg);
        }
        else {
            log("Finding where to append the message");
            while(messages.get(lastIndex).getMessage().getMsgIndex()>msg.getMessage().getMsgIndex()){
                log("3lastIndex: "+lastIndex);
                lastIndex--;
                if (lastIndex == -1) {
                    break;
                }
            }
            log("4lastIndex: "+lastIndex);
            lastIndex++;
            log("Append in position: "+lastIndex);
            messages.add(lastIndex, msg);
            adjustInfoToShow(lastIndex);
            int nextIndex = lastIndex+1;
            if(nextIndex<=messages.size()-1){
                adjustInfoToShow(nextIndex);
            }
            int previousIndex = lastIndex-1;
            if(previousIndex>=0){
                adjustInfoToShow(previousIndex);
            }
        }

        //Create adapter
        if(adapter==null){
            log("Create adapter");
            adapter = new MegaChatLollipopAdapter(this, chatRoom, messages, listView);
            adapter.setHasStableIds(true);
            listView.setLayoutManager(mLayoutManager);
            listView.setAdapter(adapter);
            adapter.setMessages(messages);
        }
        else{
            log("Update adapter with last index: "+lastIndex);
            if(lastIndex<0){
                log("Arrives the first message of the chat");
                adapter.setMessages(messages);
            }
            else{
                adapter.addMessage(messages, lastIndex+1);
            }
        }
        return lastIndex;
    }

    public int appendMessagePosition(AndroidMegaChatMessage msg){
        log("appendMessagePosition: "+messages.size()+" messages");

        int lastIndex = messages.size()-1;
        if(messages.size()==0){
            msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_ALL);
            msg.setShowAvatar(true);
            messages.add(msg);
        }else{
            log("Finding where to append the message");

            if(msg.isUploading()){
                lastIndex++;
                log("The message is uploading add to index: "+lastIndex+ "with state: "+msg.getPendingMessage().getState());
            }else{
                log("status of message: "+msg.getMessage().getStatus());
                if(lastIndex>=0) {
                    while (messages.get(lastIndex).isUploading()) {
                        log("one less index is uploading");
                        lastIndex--;
                        if(lastIndex==-1){
                            break;
                        }
                    }
                }
                if(lastIndex>=0) {
                    while (messages.get(lastIndex).getMessage().getStatus() == MegaChatMessage.STATUS_SENDING_MANUAL) {
                        log("one less index is MANUAL SENDING");
                        lastIndex--;
                        if(lastIndex==-1){
                            break;
                        }
                    }
                }
                if(lastIndex>=0) {
                    if (msg.getMessage().getStatus() == MegaChatMessage.STATUS_SERVER_RECEIVED || msg.getMessage().getStatus() == MegaChatMessage.STATUS_NOT_SEEN) {
                        while (messages.get(lastIndex).getMessage().getStatus() == MegaChatMessage.STATUS_SENDING) {
                            log("one less index");
                            lastIndex--;
                            if(lastIndex==-1){
                                break;
                            }
                        }
                    }
                }

                lastIndex++;
                log("Append in position: "+lastIndex);
            }
            if(lastIndex>=0){
                messages.add(lastIndex, msg);
                adjustInfoToShow(lastIndex);
                msg.setShowAvatar(true);
                if(!messages.get(lastIndex).isUploading()){
                    int nextIndex = lastIndex+1;
                    if(nextIndex<messages.size()){
                        if(messages.get(nextIndex)!=null) {
                            if(messages.get(nextIndex).isUploading()){
                                adjustInfoToShow(nextIndex);
                            }
                        }
                    }
                }
                if(lastIndex>0){
                    setShowAvatar(lastIndex-1);
                }
            }
        }

        //Create adapter
        if(adapter==null){
            log("Create adapter");
            adapter = new MegaChatLollipopAdapter(this, chatRoom, messages, listView);
            adapter.setHasStableIds(true);
            listView.setLayoutManager(mLayoutManager);
            listView.setAdapter(adapter);
            adapter.setMessages(messages);
        }else{
            log("Update adapter with last index: "+lastIndex);
            if(lastIndex<0){
                log("Arrives the first message of the chat");
                adapter.setMessages(messages);
            }
            else{
                adapter.addMessage(messages, lastIndex+1);
                adapter.notifyItemChanged(lastIndex);
            }
        }
        return lastIndex;
    }

    public int adjustInfoToShow(int index) {
        log("adjustInfoToShow");

        AndroidMegaChatMessage msg = messages.get(index);

        long userHandleToCompare = -1;
        long previousUserHandleToCompare = -1;

        if(msg.isUploading()){
            userHandleToCompare = myUserHandle;
        }
        else{
            if ((msg.getMessage().getType() == MegaChatMessage.TYPE_CALL_ENDED) || (msg.getMessage().getType() == MegaChatMessage.TYPE_CALL_STARTED)){
                msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_TIME);
                return AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_TIME;
            }

            if ((msg.getMessage().getType() == MegaChatMessage.TYPE_PRIV_CHANGE) || (msg.getMessage().getType() == MegaChatMessage.TYPE_ALTER_PARTICIPANTS)) {
                userHandleToCompare = msg.getMessage().getHandleOfAction();
            } else {
                userHandleToCompare = msg.getMessage().getUserHandle();
            }
        }

        if(index==0){
            msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_ALL);
        }
        else{
            AndroidMegaChatMessage previousMessage = messages.get(index-1);

            if(previousMessage.isUploading()){

                log("The previous message is uploading");
                if(msg.isUploading()){
                    log("The message is also uploading");
                    if (compareDate(msg.getPendingMessage().getUploadTimestamp(), previousMessage.getPendingMessage().getUploadTimestamp()) == 0) {
                        //Same date
                        if (compareTime(msg.getPendingMessage().getUploadTimestamp(), previousMessage.getPendingMessage().getUploadTimestamp()) == 0) {
                            msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_NOTHING);
                        } else {
                            //Different minute
                            msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_TIME);
                        }
                    } else {
                        //Different date
                        msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_ALL);
                    }
                }
                else{
                    if (compareDate(msg.getMessage().getTimestamp(), previousMessage.getPendingMessage().getUploadTimestamp()) == 0) {
                        //Same date
                        if (compareTime(msg.getMessage().getTimestamp(), previousMessage.getPendingMessage().getUploadTimestamp()) == 0) {
                            msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_NOTHING);
                        } else {
                            //Different minute
                            msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_TIME);
                        }
                    } else {
                        //Different date
                        msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_ALL);
                    }
                }
            }
            else{
                log("The previous message is NOT uploading");

                if (userHandleToCompare == myUserHandle) {
                    log("MY message!!");
//                log("MY message!!: "+messageToShow.getContent());
                    if ((previousMessage.getMessage().getType() == MegaChatMessage.TYPE_PRIV_CHANGE) || (previousMessage.getMessage().getType() == MegaChatMessage.TYPE_ALTER_PARTICIPANTS)) {
                        previousUserHandleToCompare = previousMessage.getMessage().getHandleOfAction();
                    } else {
                        previousUserHandleToCompare = previousMessage.getMessage().getUserHandle();
                    }

//                    log("previous message: "+previousMessage.getContent());
                    if (previousUserHandleToCompare == myUserHandle) {
                        log("Last message and previous is mine");
                        //The last two messages are mine
                        if(msg.isUploading()){
                            log("The msg to append is uploading");
                            if (compareDate(msg.getPendingMessage().getUploadTimestamp(), previousMessage) == 0) {
                                //Same date
                                if (compareTime(msg.getPendingMessage().getUploadTimestamp(), previousMessage) == 0) {
                                    msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_NOTHING);
                                } else {
                                    //Different minute
                                    msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_TIME);
                                }
                            } else {
                                //Different date
                                msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_ALL);
                            }
                        }
                        else{
                            if (compareDate(msg, previousMessage) == 0) {
                                //Same date
                                if (compareTime(msg, previousMessage) == 0) {
                                    if ((msg.getMessage().isManagementMessage())) {
                                        msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_TIME);
                                    }
                                    else{
                                        msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_NOTHING);
                                    }
                                } else {
                                    //Different minute
                                    msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_TIME);
                                }
                            } else {
                                //Different date
                                msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_ALL);
                            }
                        }

                    } else {
                        //The last message is mine, the previous not
                        log("Last message is mine, NOT previous");
                        if(msg.isUploading()) {
                            log("The msg to append is uploading");
                            if (compareDate(msg.getPendingMessage().getUploadTimestamp(), previousMessage) == 0) {
                                msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_TIME);
                            } else {
                                //Different date
                                msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_ALL);
                            }
                        }
                        else{
                            if (compareDate(msg, previousMessage) == 0) {
                                msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_TIME);
                            } else {
                                //Different date
                                msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_ALL);
                            }
                        }
                    }

                } else {
                    log("NOT MY message!! - CONTACT");
//                    log("previous message: "+previousMessage.getContent());

                    if ((previousMessage.getMessage().getType() == MegaChatMessage.TYPE_PRIV_CHANGE) || (previousMessage.getMessage().getType() == MegaChatMessage.TYPE_ALTER_PARTICIPANTS)) {
                        previousUserHandleToCompare = previousMessage.getMessage().getHandleOfAction();
                    } else {
                        previousUserHandleToCompare = previousMessage.getMessage().getUserHandle();
                    }

                    if (previousUserHandleToCompare == userHandleToCompare) {
                        //The last message is also a contact's message
                        if(msg.isUploading()) {
                            if (compareDate(msg.getPendingMessage().getUploadTimestamp(), previousMessage) == 0) {
                                //Same date
                                if (compareTime(msg.getPendingMessage().getUploadTimestamp(), previousMessage) == 0) {
                                    log("Add with show nothing - same userHandle");
                                    msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_NOTHING);

                                } else {
                                    //Different minute
                                    msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_TIME);
                                }
                            } else {
                                //Different date
                                msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_ALL);
                            }
                        }
                        else{

                            if (compareDate(msg, previousMessage) == 0) {
                                //Same date
                                if (compareTime(msg, previousMessage) == 0) {
                                    if ((msg.getMessage().isManagementMessage())) {
                                        msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_TIME);
                                    }
                                    else{
                                        msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_NOTHING);
                                    }
                                } else {
                                    //Different minute
                                    msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_TIME);
                                }
                            } else {
                                //Different date
                                msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_ALL);
                            }
                        }

                    } else {
                        //The last message is from contact, the previous not
                        log("Different user handle");
                        if(msg.isUploading()) {
                            if (compareDate(msg.getPendingMessage().getUploadTimestamp(), previousMessage) == 0) {
                                //Same date
                                if (compareTime(msg.getPendingMessage().getUploadTimestamp(), previousMessage) == 0) {
                                    if(previousUserHandleToCompare==myUserHandle){
                                        msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_TIME);
                                    }
                                    else{
                                        msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_NOTHING);
                                    }

                                } else {
                                    //Different minute
                                    msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_TIME);
                                }

                            } else {
                                //Different date
                                msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_ALL);
                            }
                        }
                        else{
                            if (compareDate(msg, previousMessage) == 0) {
                                if (compareTime(msg, previousMessage) == 0) {
                                    if(previousUserHandleToCompare==myUserHandle){
                                        msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_TIME);
                                    }
                                    else{
                                        if ((msg.getMessage().isManagementMessage())) {
                                            msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_TIME);
                                        }
                                        else{
                                            msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_TIME);
                                        }
                                    }

                                } else {
                                    //Different minute
                                    msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_TIME);
                                }

                            } else {
                                //Different date
                                msg.setInfoToShow(AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_ALL);
                            }
                        }
                    }
                }

            }
        }
        return msg.getInfoToShow();
    }

    public void setShowAvatar(int index){
        log("setShowAvatar: "+index);

        AndroidMegaChatMessage msg = messages.get(index);

        long userHandleToCompare = -1;
        long previousUserHandleToCompare = -1;

        if(msg.isUploading()){
            msg.setShowAvatar(false);
            return;
        }

        if (userHandleToCompare == myUserHandle) {
            log("MY message!!");
        }
        else{
            if ((msg.getMessage().getType() == MegaChatMessage.TYPE_PRIV_CHANGE) || (msg.getMessage().getType() == MegaChatMessage.TYPE_ALTER_PARTICIPANTS)) {
                userHandleToCompare = msg.getMessage().getHandleOfAction();
            } else {
                userHandleToCompare = msg.getMessage().getUserHandle();
            }

            log("userHandleTocompare: "+userHandleToCompare);
            AndroidMegaChatMessage previousMessage = null;
            if(messages.size()-1>index){
                previousMessage = messages.get(index + 1);

                if(previousMessage==null){
                    msg.setShowAvatar(true);
                    log("2 - Previous message is null");
                    return;
                }

                if(previousMessage.isUploading()){
                    msg.setShowAvatar(true);
                    log("Previous is uploading");
                    return;
                }

                if ((previousMessage.getMessage().getType() == MegaChatMessage.TYPE_PRIV_CHANGE) || (previousMessage.getMessage().getType() == MegaChatMessage.TYPE_ALTER_PARTICIPANTS)) {
                    previousUserHandleToCompare = previousMessage.getMessage().getHandleOfAction();
                } else {
                    previousUserHandleToCompare = previousMessage.getMessage().getUserHandle();
                }

                log("previousUserHandleToCompare: "+previousUserHandleToCompare);

//                if(previousMessage.getInfoToShow()!=AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_NOTHING){
//                    msg.setShowAvatar(true);
//                }
//                else{
                    if ((previousMessage.getMessage().getType() == MegaChatMessage.TYPE_CALL_ENDED) || (previousMessage.getMessage().getType() == MegaChatMessage.TYPE_CALL_STARTED) || (previousMessage.getMessage().getType() == MegaChatMessage.TYPE_PRIV_CHANGE) || (previousMessage.getMessage().getType() == MegaChatMessage.TYPE_ALTER_PARTICIPANTS) || (previousMessage.getMessage().getType() == MegaChatMessage.TYPE_CHAT_TITLE)) {
                        msg.setShowAvatar(true);
                        log("Set: "+true);
                    } else {
                        if (previousUserHandleToCompare == userHandleToCompare) {

                            msg.setShowAvatar(false);
                            log("Set: "+false);
                        }
                        else{
                            msg.setShowAvatar(true);
                            log("Set: "+true);
                        }
                    }
//                }
            }
            else{
                log("No previous message");
                msg.setShowAvatar(true);
            }
        }
    }

    public boolean isGroup(){
        return chatRoom.isGroup();
    }

    public void showMsgNotSentPanel(AndroidMegaChatMessage message, int position){
        log("showMsgNotSentPanel: "+position);

        this.selectedPosition = position;
        this.selectedMessageId = message.getMessage().getRowId();
        log("Temporal id of MS message: "+message.getMessage().getTempId());

        if(message!=null){
            MessageNotSentBottomSheetDialogFragment bottomSheetDialogFragment = new MessageNotSentBottomSheetDialogFragment();
            bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
        }
    }

    public void showNodeAttachmentBottomSheet(AndroidMegaChatMessage message, int position){
        log("showNodeAttachmentBottomSheet: "+position);
        this.selectedPosition = position;

        if(message!=null){
            this.selectedMessageId = message.getMessage().getMsgId();
//            this.selectedChatItem = chat;
            NodeAttachmentBottomSheetDialogFragment bottomSheetDialogFragment = new NodeAttachmentBottomSheetDialogFragment();
            bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
        }
    }

    public void showUploadingAttachmentBottomSheet(AndroidMegaChatMessage message, int position){
        log("showUploadingAttachmentBottomSheet: "+position);
        this.selectedPosition = position;
        if(message!=null){
            this.selectedMessageId = message.getPendingMessage().getId();

            PendingMessageBottomSheetDialogFragment pendingMsgSheetDialogFragment = new PendingMessageBottomSheetDialogFragment();
            pendingMsgSheetDialogFragment.show(getSupportFragmentManager(), pendingMsgSheetDialogFragment.getTag());
        }
    }

    public void showContactAttachmentBottomSheet(AndroidMegaChatMessage message, int position){
        log("showContactAttachmentBottomSheet: "+position);
        this.selectedPosition = position;

        if(message!=null){
            this.selectedMessageId = message.getMessage().getMsgId();
//            this.selectedChatItem = chat;
            ContactAttachmentBottomSheetDialogFragment bottomSheetDialogFragment = new ContactAttachmentBottomSheetDialogFragment();
            bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
        }
    }

    public void removeMsgNotSent(){
        log("removeMsgNotSent: "+selectedPosition);
        messages.remove(selectedPosition);
        adapter.removeMessage(selectedPosition, messages);
    }

    public void removePendingMsg(long id){
        log("removePendingMsg: "+selectedMessageId);

        PendingMessageSingle pMsg = dbH.findPendingMessageById(id);
        if(pMsg!=null && pMsg.getState()==PendingMessageSingle.STATE_UPLOADING) {
            if (pMsg.getTransferTag() != -1) {
                log("Transfer tag: " + pMsg.getTransferTag());
                if (megaApi != null) {
                    megaApi.cancelTransferByTag(pMsg.getTransferTag(), this);
                }
            }
        }

        if(pMsg!=null && pMsg.getState()!=PendingMessageSingle.STATE_SENT){
            try{
                dbH.removePendingMessageById(id);
                messages.remove(selectedPosition);
                adapter.removeMessage(selectedPosition, messages);
            }
            catch (IndexOutOfBoundsException e){
                log("removePendingMsg: EXCEPTION: "+e.getMessage());
            }
        }
        else{
            showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.error_message_already_sent), -1);
        }
    }

    public void showSnackbar(int type, String s, long idChat){
        showSnackbar(type, fragmentContainer, s, idChat);
    }

    public void removeProgressDialog(){
        if (statusDialog != null) {
            try {
                statusDialog.dismiss();
            } catch (Exception ex) {}
        }
    }

    public void startConversation(long handle){
        log("startConversation");
        MegaChatRoom chat = megaChatApi.getChatRoomByUser(handle);
        MegaChatPeerList peers = MegaChatPeerList.createInstance();
        if(chat==null){
            log("No chat, create it!");
            peers.addPeer(handle, MegaChatPeerList.PRIV_STANDARD);
            megaChatApi.createChat(false, peers, this);
        }
        else{
            log("There is already a chat, open it!");
            Intent intentOpenChat = new Intent(this, ChatActivityLollipop.class);
            intentOpenChat.setAction(Constants.ACTION_CHAT_SHOW_MESSAGES);
            intentOpenChat.putExtra("CHAT_ID", chat.getChatId());
            this.startActivity(intentOpenChat);
            finish();
        }
    }

    public void startGroupConversation(ArrayList<Long> userHandles){
        log("startGroupConversation");

        MegaChatPeerList peers = MegaChatPeerList.createInstance();

        for(int i=0;i<userHandles.size();i++){
            long handle = userHandles.get(i);
            peers.addPeer(handle, MegaChatPeerList.PRIV_STANDARD);
        }
        megaChatApi.createChat(true, peers, this);
    }

    public void setMessages(ArrayList<AndroidMegaChatMessage> messages){
        if(dialog!=null){
            dialog.dismiss();
        }

        this.messages = messages;
        //Create adapter
        if (adapter == null) {
            adapter = new MegaChatLollipopAdapter(this, chatRoom, messages, listView);
            adapter.setHasStableIds(true);
            listView.setAdapter(adapter);
            adapter.setMessages(messages);
        } else {
            adapter.setMessages(messages);
        }
    }

    @Override
    public void onRequestStart(MegaChatApiJava api, MegaChatRequest request) {

    }

    @Override
    public void onRequestUpdate(MegaChatApiJava api, MegaChatRequest request) {

    }

    @Override
    public void onRequestFinish(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {
        log("onRequestFinish: "+request.getRequestString()+" "+request.getType());

        if(request.getType() == MegaChatRequest.TYPE_TRUNCATE_HISTORY){
            log("Truncate history request finish!!!");
            if(e.getErrorCode()==MegaChatError.ERROR_OK){
                log("Ok. Clear history done");
                showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.clear_history_success), -1);
                hideMessageJump();
            }else{
                log("Error clearing history: "+e.getErrorString());
                showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.clear_history_error), -1);
            }
        }else if(request.getType() == MegaChatRequest.TYPE_HANG_CHAT_CALL){
            if(e.getErrorCode()==MegaChatError.ERROR_OK){
                log("TYPE_HANG_CHAT_CALL finished with success  ---> answerChatCall chatid = "+idChat);
                if(megaChatApi!=null){
                    MegaChatCall call = megaChatApi.getChatCall(idChat);
                    if(call!=null) {
                        if(call.getStatus() == MegaChatCall.CALL_STATUS_RING_IN){
                            megaChatApi.answerChatCall(idChat, false, this);
                        }else if(call.getStatus() == MegaChatCall.CALL_STATUS_USER_NO_PRESENT ){
                            megaChatApi.startChatCall(idChat, false, this);
                        }
                    }
                }
            }else{
                log("ERROR WHEN TYPE_HANG_CHAT_CALL e.getErrorCode(): " + e.getErrorString());
            }

        }else if(request.getType() == MegaChatRequest.TYPE_START_CHAT_CALL){
            if(e.getErrorCode()==MegaChatError.ERROR_OK){
                log("TYPE_START_CHAT_CALL finished with success");
                //getFlag - Returns true if it is a video-audio call or false for audio call
            }else{
                log("ERROR WHEN TYPE_START_CHAT_CALL e.getErrorCode(): " + e.getErrorString());
                if(e.getErrorCode() == MegaChatError.ERROR_TOOMANY){
                    showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.call_error_too_many_participants), -1);
                }else{
                    showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.call_error), -1);
                }
            }

        }else if(request.getType() == MegaChatRequest.TYPE_ANSWER_CHAT_CALL){
            if(e.getErrorCode()==MegaChatError.ERROR_OK){
                log("TYPE_ANSWER_CHAT_CALL finished with success");
                //getFlag - Returns true if it is a video-audio call or false for audio call
            }else{
                log("ERROR WHEN TYPE_ANSWER_CHAT_CALL e.getErrorCode(): " + e.getErrorString());
                if(e.getErrorCode() == MegaChatError.ERROR_TOOMANY){

                    showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.call_error_too_many_participants), -1);
                }else{
                    showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.call_error), -1);
                }
            }

        }else if(request.getType() == MegaChatRequest.TYPE_REMOVE_FROM_CHATROOM){
            log("Remove participant: "+request.getUserHandle()+" my user: "+megaChatApi.getMyUserHandle());

            if(e.getErrorCode()==MegaChatError.ERROR_OK){
                log("Participant removed OK");
                invalidateOptionsMenu();

            }
            else{
                log("EEEERRRRROR WHEN TYPE_REMOVE_FROM_CHATROOM " + e.getErrorString());
                showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.remove_participant_error), -1);
            }

        }else if(request.getType() == MegaChatRequest.TYPE_INVITE_TO_CHATROOM){
            log("Request type: "+MegaChatRequest.TYPE_INVITE_TO_CHATROOM);
            if(e.getErrorCode()==MegaChatError.ERROR_OK){
                showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.add_participant_success), -1);
            }
            else{
                if(e.getErrorCode() == MegaChatError.ERROR_EXIST){
                    showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.add_participant_error_already_exists), -1);
                }
                else{
                    showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.add_participant_error), -1);
                }
            }

        }else if(request.getType() == MegaChatRequest.TYPE_ATTACH_NODE_MESSAGE){
            removeProgressDialog();

            if(adapter!=null){
                if(adapter.isMultipleSelect()){
                    clearSelections();
                    hideMultipleSelect();
                }
            }

            if(e.getErrorCode()==MegaChatError.ERROR_OK){
                log("File sent correctly");
                MegaNodeList nodeList = request.getMegaNodeList();

                for(int i = 0; i<nodeList.size();i++){
                    log("Node name: "+nodeList.get(i).getName());
                }
                AndroidMegaChatMessage androidMsgSent = new AndroidMegaChatMessage(request.getMegaChatMessage());
                sendMessageToUI(androidMsgSent);

            }else{
                log("File NOT sent: "+e.getErrorCode()+"___"+e.getErrorString());
                showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.error_attaching_node_from_cloud), -1);
            }

        }else if(request.getType() == MegaChatRequest.TYPE_REVOKE_NODE_MESSAGE){
            if(e.getErrorCode()==MegaChatError.ERROR_OK){
                log("Node revoked correctly, msg id: "+request.getMegaChatMessage().getMsgId());
            }
            else{
                log("NOT revoked correctly");
                showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.error_revoking_node), -1);
            }

        }else if(request.getType() == MegaChatRequest.TYPE_CREATE_CHATROOM){
            log("Create chat request finish!!!");
            if(e.getErrorCode()==MegaChatError.ERROR_OK){

                log("open new chat");
                Intent intent = new Intent(this, ChatActivityLollipop.class);
                intent.setAction(Constants.ACTION_CHAT_SHOW_MESSAGES);
                intent.putExtra("CHAT_ID", request.getChatHandle());
                this.startActivity(intent);
                finish();
            }
            else{
                log("EEEERRRRROR WHEN CREATING CHAT " + e.getErrorString());
                showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.create_chat_error), -1);
            }
        }
        else if(request.getType() == MegaChatRequest.TYPE_LOAD_PREVIEW){
            if(e.getErrorCode()==MegaChatError.ERROR_OK || e.getErrorCode()==MegaChatError.ERROR_EXIST){
                if (idChat != -1 && megaChatApi.getChatRoom(idChat) != null) {
                    log("Close previous chat");
                    megaChatApi.closeChatRoom(idChat, null);
                }
                idChat = request.getChatHandle();
                MegaApplication.setOpenChatId(idChat);
                showChat(null);
                if (e.getErrorCode() == MegaChatError.ERROR_EXIST) {
                    if (megaChatApi.getChatRoom(idChat).isActive()) {
                        log("ERROR: You are already a participant of the chat link or are trying to open it again");
                    } else {
                        showConfirmationRejoinChat(request.getUserHandle());
                    }
                }
            }
            else {
                if(e.getErrorCode()==MegaChatError.ERROR_NOENT){
                    emptyTextView.setText(getString(R.string.invalid_chat_link));
                }
                else{
                    showSnackbar(Constants.MESSAGE_SNACKBAR_TYPE, getString(R.string.error_general_nodes), -1);
                    emptyTextView.setText(getString(R.string.error_chat_link));
                }

                emptyTextView.setVisibility(View.VISIBLE);
                emptyLayout.setVisibility(View.VISIBLE);
                chatRelativeLayout.setVisibility(View.GONE);

                LinearLayout.LayoutParams emptyTextViewParams1 = (LinearLayout.LayoutParams)emptyImageView.getLayoutParams();
                if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                    emptyImageView.setImageResource(R.drawable.chat_empty_landscape);
                    emptyTextViewParams1.setMargins(0, Util.scaleHeightPx(40, outMetrics), 0, Util.scaleHeightPx(24, outMetrics));
                }else{
                    emptyImageView.setImageResource(R.drawable.ic_empty_chat_list);
                    emptyTextViewParams1.setMargins(0, Util.scaleHeightPx(100, outMetrics), 0, Util.scaleHeightPx(24, outMetrics));
                }

                emptyImageView.setLayoutParams(emptyTextViewParams1);
            }
        }
        else if(request.getType() == MegaChatRequest.TYPE_AUTOJOIN_PUBLIC_CHAT) {
            if (e.getErrorCode() == MegaChatError.ERROR_OK) {

                if (request.getUserHandle() != -1) {
                    //Rejoin option
                    showChat(null);
                } else {
                    //Join
                    setChatSubtitle();
                    setPreviewersView();
                    supportInvalidateOptionsMenu();
                }
            } else {
                log("EEEERRRRROR WHEN JOINING CHAT " + e.getErrorCode() + " " + e.getErrorString());
                showSnackbar(Constants.MESSAGE_SNACKBAR_TYPE, getString(R.string.error_general_nodes), -1);
            }
        }
        else if(request.getType() == MegaChatRequest.TYPE_LAST_GREEN){
            log("TYPE_LAST_GREEN requested");

        }else if(request.getType() == MegaChatRequest.TYPE_ARCHIVE_CHATROOM){
            long chatHandle = request.getChatHandle();
            chatRoom = megaChatApi.getChatRoom(chatHandle);
            String chatTitle = chatRoom.getTitle();

            if(chatTitle==null){
                chatTitle = "";
            }
            else if(!chatTitle.isEmpty() && chatTitle.length()>60){
                chatTitle = chatTitle.substring(0,59)+"...";
            }

            if(!chatTitle.isEmpty() && chatRoom.isGroup() && !chatRoom.hasCustomTitle()){
                chatTitle = "\""+chatTitle+"\"";
            }

            if(e.getErrorCode()==MegaChatError.ERROR_OK){
                if(request.getFlag()){
                    log("Chat archived");
                    showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.success_archive_chat, chatTitle), -1);
                }
                else{
                    log("Chat unarchived");
                    showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.success_unarchive_chat, chatTitle), -1);
                }

            }else{
                if(request.getFlag()){
                    log("EEEERRRRROR WHEN ARCHIVING CHAT " + e.getErrorString());
                    showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.error_archive_chat, chatTitle), -1);
                }else{
                    log("EEEERRRRROR WHEN UNARCHIVING CHAT " + e.getErrorString());
                    showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.error_unarchive_chat, chatTitle), -1);
                }
            }

            supportInvalidateOptionsMenu();
            setChatSubtitle();

            if(!chatRoom.isArchived()){
                requestLastGreen(INITIAL_PRESENCE_STATUS);
            }
        }
        else if (request.getType() == MegaChatRequest.TYPE_CHAT_LINK_HANDLE) {
            if(request.getFlag() && request.getNumRetry()==0){
                log("Removing chat link");
                if(e.getErrorCode()==MegaChatError.ERROR_OK){
                    showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.chat_link_deleted), -1);
                }
                else{
                    if (e.getErrorCode() == MegaChatError.ERROR_ARGS) {
                        log("The chatroom isn't grupal or public");
                    }
                    else if (e.getErrorCode()==MegaChatError.ERROR_NOENT){
                        log("The chatroom doesn't exist or the chatid is invalid");
                    }
                    else if(e.getErrorCode()==MegaChatError.ERROR_ACCESS){
                        log("The chatroom doesn't have a topic or the caller isn't an operator");
                    }
                    else{
                        log("Error TYPE_CHAT_LINK_HANDLE "+e.getErrorCode());
                    }
                    showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.general_error) + ": " + e.getErrorString(), -1);
                }
            }
        }
    }

    @Override
    public void onRequestTemporaryError(MegaChatApiJava api, MegaChatRequest request, MegaChatError e) {
        log("onRequestTemporaryError");
    }

    @Override
    protected void onStop() {
        log("onStop()");
        try{
            if(textChat!=null){
                String written = textChat.getText().toString();
                if(written!=null){
                    dbH.setWrittenTextItem(Long.toString(idChat), textChat.getText().toString());
                }
            }
            else{
                log("textChat is NULL");
            }
        }catch (Exception e){
            log("Written message not stored on DB");
        }
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        log("onDestroy()");

        if(emojiKeyboard!=null){
            emojiKeyboard.hideBothKeyboard(this);
        }
        if (handlerEmojiKeyboard != null){
            handlerEmojiKeyboard.removeCallbacksAndMessages(null);
        }
        if (handlerKeyboard != null){
            handlerKeyboard.removeCallbacksAndMessages(null);
        }

        if (megaChatApi != null) {
            megaChatApi.closeChatRoom(idChat, this);
            MegaApplication.setClosedChat(true);
            megaChatApi.removeChatListener(this);
            megaChatApi.removeChatCallListener(this);
        }

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        if(callInProgressChrono!=null){
            callInProgressChrono.stop();
            callInProgressChrono.setVisibility(View.GONE);
        }
        if(callInProgressLayout!=null){
            callInProgressLayout.setVisibility(View.GONE);
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(dialogConnectReceiver);

        super.onDestroy();
    }

    public void closeChat(boolean shouldLogout){
        log("closeChat");
        if(idChat!=-1){
            megaChatApi.closeChatRoom(idChat, this);
        }

        if(chatRoom!=null){
            if(chatRoom.isPreview()){
                megaChatApi.closeChatPreview(idChat);

                if(chatC.isInAnonymousMode() && shouldLogout){
                    megaChatApi.logout();
                }
            }
        }

        MegaApplication.setClosedChat(true);
    }

    @Override
    protected void onNewIntent(Intent intent){
        log("onNewIntent");

        if (intent != null){
            if (intent.getAction() != null){
                log("Intent is here!: "+intent.getAction());
                if (intent.getAction().equals(Constants.ACTION_CLEAR_CHAT)){
                    log("Intent to Clear history");
                    showConfirmationClearChat(chatRoom);
                }
                else if(intent.getAction().equals(Constants.ACTION_UPDATE_ATTACHMENT)){
                    log("Intent to update an attachment with error");

                    long idPendMsg = intent.getLongExtra("ID_MSG", -1);
                    if(idPendMsg!=-1){
                        int indexToChange = -1;
                        ListIterator<AndroidMegaChatMessage> itr = messages.listIterator(messages.size());

                        // Iterate in reverse.
                        while(itr.hasPrevious()) {
                            AndroidMegaChatMessage messageToCheck = itr.previous();

                            if(messageToCheck.isUploading()){
                                if(messageToCheck.getPendingMessage().getId()==idPendMsg){
                                    indexToChange = itr.nextIndex();
                                    log("Found index to change: "+indexToChange);
                                    break;
                                }
                            }
                        }

                        if(indexToChange!=-1){
                            log("Index modified: "+indexToChange);

                            PendingMessageSingle pendingMsg = null;
                            if(idPendMsg!=-1){
                                pendingMsg = dbH.findPendingMessageById(idPendMsg);

                                if(pendingMsg!=null){
                                    messages.get(indexToChange).setPendingMessage(pendingMsg);
                                    adapter.modifyMessage(messages, indexToChange+1);
                                }
                            }
                        }
                        else{
                            log("Error, id pending message message not found!!");
                        }
                    }
                    else{
                        log("Error. The idPendMsg is -1");
                    }

                    int isOverquota = intent.getIntExtra("IS_OVERQUOTA", 0);
                    if(isOverquota==1){
                        showOverquotaAlert(false);
                    }
                    else if (isOverquota==2){
                        showOverquotaAlert(true);
                    }

                    return;
                }
                else if((intent.getAction().equals(Constants.ACTION_CHAT_SHOW_MESSAGES))||(intent.getAction().equals(Constants.ACTION_OPEN_CHAT_LINK))){
                    log("Intent to open new chat: "+intent.getAction());
                    if(messages!=null){
                        messages.clear();
                    }

                    adapter.notifyDataSetChanged();
                    closeChat(false);
                    MegaApplication.setOpenChatId(-1);
                    initAfterIntent(intent, null);
                }
                else{
                    log("Other intent");
                }
            }
        }
        super.onNewIntent(intent);
        setIntent(intent);
        return;
    }

    public String getPeerFullName(long userHandle){
        return chatRoom.getPeerFullnameByHandle(userHandle);
    }

    public MegaChatRoom getChatRoom() {
        return chatRoom;
    }

    public void setChatRoom(MegaChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    public void revoke(){
        log("revoke");
        megaChatApi.revokeAttachmentMessage(idChat, selectedMessageId);
    }

    @Override
    public void onRequestStart(MegaApiJava api, MegaRequest request) {

    }

    @Override
    public void onRequestUpdate(MegaApiJava api, MegaRequest request) {

    }

    @Override
    public void onRequestFinish(MegaApiJava api, MegaRequest request, MegaError e) {
        removeProgressDialog();

        if (request.getType() == MegaRequest.TYPE_INVITE_CONTACT){
            log("MegaRequest.TYPE_INVITE_CONTACT finished: "+request.getNumber());

            if(request.getNumber()== MegaContactRequest.INVITE_ACTION_REMIND){
                showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.context_contact_invitation_resent), -1);
            }
            else{
                if (e.getErrorCode() == MegaError.API_OK){
                    log("OK INVITE CONTACT: "+request.getEmail());
                    if(request.getNumber()==MegaContactRequest.INVITE_ACTION_ADD)
                    {
                        showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.context_contact_request_sent, request.getEmail()), -1);
                    }
                }
                else{
                    log("Code: "+e.getErrorString());
                    if(e.getErrorCode()==MegaError.API_EEXIST)
                    {
                        showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.context_contact_already_invited, request.getEmail()), -1);
                    }
                    else if(request.getNumber()==MegaContactRequest.INVITE_ACTION_ADD && e.getErrorCode()==MegaError.API_EARGS)
                    {
                        showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.error_own_email_as_contact), -1);
                    }
                    else{
                        showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.general_error), -1);
                    }
                    log("ERROR: " + e.getErrorCode() + "___" + e.getErrorString());
                }
            }
        }
        else if(request.getType() == MegaRequest.TYPE_COPY){
            if (e.getErrorCode() != MegaError.API_OK) {

                log("e.getErrorCode() != MegaError.API_OK");

                if(e.getErrorCode()==MegaError.API_EOVERQUOTA){
                    log("OVERQUOTA ERROR: "+e.getErrorCode());
                    Intent intent = new Intent(this, ManagerActivityLollipop.class);
                    intent.setAction(Constants.ACTION_OVERQUOTA_STORAGE);
                    startActivity(intent);
                    finish();
                }
                else if(e.getErrorCode()==MegaError.API_EGOINGOVERQUOTA){
                    log("OVERQUOTA ERROR: "+e.getErrorCode());
                    Intent intent = new Intent(this, ManagerActivityLollipop.class);
                    intent.setAction(Constants.ACTION_PRE_OVERQUOTA_STORAGE);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.import_success_error), -1);
                }

            }else{
                showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.import_success_message), -1);
            }
        }
        else if (request.getType() == MegaRequest.TYPE_CANCEL_TRANSFER){
            if (e.getErrorCode() != MegaError.API_OK) {
                log("Error TYPE_CANCEL_TRANSFER: "+e.getErrorCode());
            }
            else{
                log("Chat upload cancelled");
            }
        }
    }

    @Override
    public void onRequestTemporaryError(MegaApiJava api, MegaRequest request, MegaError e) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        log("onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putLong("idChat", idChat);
        outState.putLong("selectedMessageId", selectedMessageId);
        outState.putInt("selectedPosition", selectedPosition);
        outState.putInt("typeMessageJump",typeMessageJump);

        if(messageJumpLayout.getVisibility() == View.VISIBLE){
            visibilityMessageJump = true;
        }else{
            visibilityMessageJump = false;
        }
        outState.putBoolean("visibilityMessageJump",visibilityMessageJump);
        outState.putLong("lastMessageSeen", lastIdMsgSeen);
        outState.putLong("generalUnreadCount", generalUnreadCount);
        outState.putBoolean("isHideJump",isHideJump);
        outState.putString("mOutputFilePath",mOutputFilePath);
        outState.putBoolean("isShareLinkDialogDismissed", isShareLinkDialogDismissed);
//        outState.putInt("position_imageDrag", position_imageDrag);
//        outState.putSerializable("holder_imageDrag", holder_imageDrag);
    }

    public void askSizeConfirmationBeforeChatDownload(String parentPath, ArrayList<MegaNode> nodeList, long size){
        log("askSizeConfirmationBeforeChatDownload");

        final String parentPathC = parentPath;
        final ArrayList<MegaNode> nodeListC = nodeList;
        final long sizeC = size;
        final ChatController chatC = new ChatController(this);

        android.support.v7.app.AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        }
        else{
            builder = new AlertDialog.Builder(this);
        }
        LinearLayout confirmationLayout = new LinearLayout(this);
        confirmationLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(Util.scaleWidthPx(20, outMetrics), Util.scaleHeightPx(10, outMetrics), Util.scaleWidthPx(17, outMetrics), 0);

        final CheckBox dontShowAgain =new CheckBox(this);
        dontShowAgain.setText(getString(R.string.checkbox_not_show_again));
        dontShowAgain.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));

        confirmationLayout.addView(dontShowAgain, params);

        builder.setView(confirmationLayout);

//				builder.setTitle(getString(R.string.confirmation_required));

        builder.setMessage(getString(R.string.alert_larger_file, Util.getSizeString(sizeC)));
        builder.setPositiveButton(getString(R.string.general_save_to_device),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(dontShowAgain.isChecked()){
                            dbH.setAttrAskSizeDownload("false");
                        }
                        chatC.download(parentPathC, nodeListC);
                    }
                });
        builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if(dontShowAgain.isChecked()){
                    dbH.setAttrAskSizeDownload("false");
                }
            }
        });

        downloadConfirmationDialog = builder.create();
        downloadConfirmationDialog.show();
    }

    /*
	 * Handle processed upload intent
	 */
    public void onIntentProcessed(List<ShareInfo> infos) {
        log("onIntentProcessedLollipop");

        if (infos == null) {
            showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.upload_can_not_open), -1);
        }
        else {
            log("Launch chat upload with files "+infos.size());
            for (ShareInfo info : infos) {
                Intent intent = new Intent(this, ChatUploadService.class);

                PendingMessageSingle pMsgSingle = new PendingMessageSingle();
                pMsgSingle.setChatId(idChat);
                long timestamp = System.currentTimeMillis()/1000;
                pMsgSingle.setUploadTimestamp(timestamp);

                String fingerprint = megaApi.getFingerprint(info.getFileAbsolutePath());

                pMsgSingle.setFilePath(info.getFileAbsolutePath());
                pMsgSingle.setName(info.getTitle());
                pMsgSingle.setFingerprint(fingerprint);

                long idMessage = dbH.addPendingMessage(pMsgSingle);
                pMsgSingle.setId(idMessage);

                if(idMessage!=-1){
                    intent.putExtra(ChatUploadService.EXTRA_ID_PEND_MSG, idMessage);

                    log("name of the file: "+info.getTitle());
                    log("size of the file: "+info.getSize());

                    AndroidMegaChatMessage newNodeAttachmentMsg = new AndroidMegaChatMessage(pMsgSingle, true);
                    sendMessageToUI(newNodeAttachmentMsg);

//                ArrayList<String> filePaths = newPendingMsg.getFilePaths();
//                filePaths.add("/home/jfjf.jpg");

                    intent.putExtra(ChatUploadService.EXTRA_CHAT_ID, idChat);

                    startService(intent);
                }
                else{
                    log("Error when adding pending msg to the database");
                }

                removeProgressDialog();
            }
        }
    }

    public void openChatAfterForward(long chatHandle, String text){
        log("openChatAfterForward");

        removeProgressDialog();

        if(chatHandle==idChat){
            log("Chat already opened");

            if(adapter!=null){
                if(adapter.isMultipleSelect()){
                    clearSelections();
                    hideMultipleSelect();
                }
            }

            if(text!=null){
                showSnackbar(Constants.SNACKBAR_TYPE, text, -1);
            }
        }
        else{
            if(chatHandle!=-1){
                log("Open chat to forward messages");

                Intent intentOpenChat = new Intent(this, ManagerActivityLollipop.class);
                intentOpenChat.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentOpenChat.setAction(Constants.ACTION_CHAT_NOTIFICATION_MESSAGE);
                intentOpenChat.putExtra("CHAT_ID", chatHandle);
                if(text!=null){
                    intentOpenChat.putExtra("showSnackbar", text);
                }
                startActivity(intentOpenChat);
                closeChat(true);
                finish();
            }
            else{
                if(adapter.isMultipleSelect()){
                    clearSelections();
                    hideMultipleSelect();
                }
                if(text!=null){
                    showSnackbar(Constants.SNACKBAR_TYPE, text, -1);
                }
            }
        }
    }

    public void markAsSeen(MegaChatMessage msg){
        log("markAsSeen");
        if(activityVisible){
            if(msg.getStatus()!=MegaChatMessage.STATUS_SEEN) {
                megaChatApi.setMessageSeen(chatRoom.getChatId(), msg.getMsgId());
            }
        }
    }

   @Override
    protected void onResume(){
        log("onResume");
        super.onResume();


        if(idChat!=-1 && chatRoom!=null) {

            setNodeAttachmentVisible();

            MegaApplication.setShowPinScreen(true);
            MegaApplication.setOpenChatId(idChat);

            supportInvalidateOptionsMenu();

            int chatConnection = megaChatApi.getChatConnectionState(idChat);
            log("Chat connection (" + idChat+ ") is: "+chatConnection);
            if(chatConnection==MegaChatApi.CHAT_CONNECTION_ONLINE) {
                setAsRead = true;
                if(!chatRoom.isGroup()) {
                    requestLastGreen(INITIAL_PRESENCE_STATUS);
                }
            }
            else{
                setAsRead=false;
            }

            setChatSubtitle();
            if(chatRoom.hasCustomTitle()){
                textChat.setHint(getString(R.string.type_message_hint_with_customized_title, chatRoom.getTitle()));
            }else{
                textChat.setHint(getString(R.string.type_message_hint_with_default_title, chatRoom.getTitle()));
            }

            if(emojiKeyboard!=null){
                emojiKeyboard.hideBothKeyboard(this);
            }
            //Update last seen position if different and there is unread messages
            //If the chat is being opened do not update, onLoad will do that

            //!isLoadingMessages
            if(!isOpeningChat) {
                log("Chat is NOT loading history");
                if(lastSeenReceived == true && messages != null){

                    long unreadCount = chatRoom.getUnreadCount();
                    if (unreadCount != 0) {
                        lastIdMsgSeen = megaChatApi.getLastMessageSeenId(idChat);

                        //Find last message
                        int positionLastMessage = -1;
                        for(int i=messages.size()-1; i>=0;i--) {
                            AndroidMegaChatMessage androidMessage = messages.get(i);

                            if (!androidMessage.isUploading()) {
                                MegaChatMessage msg = androidMessage.getMessage();
                                if (msg.getMsgId() == lastIdMsgSeen) {
                                    positionLastMessage = i;
                                    break;
                                }
                            }
                        }

                        if(positionLastMessage==-1){
                            scrollToMessage(-1);

                        }
                        else{
                            //Check if it has no my messages after

                            if(positionLastMessage >= messages.size()-1){
                                log("Nothing after, do not increment position");
                            }
                            else{
                                positionLastMessage = positionLastMessage + 1;
                            }

                            AndroidMegaChatMessage message = messages.get(positionLastMessage);
                            log("Position lastMessage found: "+positionLastMessage+" messages.size: "+messages.size());

                            while(message.getMessage().getUserHandle()==megaChatApi.getMyUserHandle()){
                                lastIdMsgSeen = message.getMessage().getMsgId();
                                positionLastMessage = positionLastMessage + 1;
                                message = messages.get(positionLastMessage);
                            }

                            generalUnreadCount = unreadCount;

                            scrollToMessage(lastIdMsgSeen);
                        }
                    }
                    else{
                        if(generalUnreadCount!=0){
                            scrollToMessage(-1);
                        }
                    }
                }
                setLastMessageSeen();
            }
            else{
                log("onResume:openingChat:doNotUpdateLastMessageSeen");
            }

            activityVisible = true;
            showCallLayout(megaChatApi.getChatCall(idChat));
            if(aB != null && aB.getTitle() != null){
                titleToolbar.setText(adjustForLargeFont(titleToolbar.getText().toString()));
            }
        }
    }

    public void scrollToMessage(long lastId){
        for(int i=messages.size()-1; i>=0;i--) {
            AndroidMegaChatMessage androidMessage = messages.get(i);

            if (!androidMessage.isUploading()) {
                MegaChatMessage msg = androidMessage.getMessage();
                if (msg.getMsgId() == lastId) {
                    log("scrollToPosition: "+i);
                    mLayoutManager.scrollToPositionWithOffset(i+1,Util.scaleHeightPx(30, outMetrics));
                    break;
                }
            }
        }

    }

    public void setLastMessageSeen(){
        log("setLastMessageSeen");

        if(messages!=null){
            if(!messages.isEmpty()){
                AndroidMegaChatMessage lastMessage = messages.get(messages.size()-1);
                int index = messages.size()-1;
                if((lastMessage!=null)&&(lastMessage.getMessage()!=null)){
                    if (!lastMessage.isUploading()) {
                        while (lastMessage.getMessage().getUserHandle() == megaChatApi.getMyUserHandle()) {
                            index--;
                            if (index == -1) {
                                break;
                            }
                            lastMessage = messages.get(index);
                        }

                        if (lastMessage.getMessage() != null) {
                            boolean resultMarkAsSeen = megaChatApi.setMessageSeen(idChat, lastMessage.getMessage().getMsgId());
                            log("(A)Result setMessageSeen: " + resultMarkAsSeen);
                        }

                    } else {
                        while (lastMessage.isUploading() == true) {
                            index--;
                            if (index == -1) {
                                break;
                            }
                            lastMessage = messages.get(index);
                        }
                        if((lastMessage!=null)&&(lastMessage.getMessage()!=null)){

                            while (lastMessage.getMessage().getUserHandle() == megaChatApi.getMyUserHandle()) {
                                index--;
                                if (index == -1) {
                                    break;
                                }
                                lastMessage = messages.get(index);
                            }

                            if (lastMessage.getMessage() != null) {
                                boolean resultMarkAsSeen = megaChatApi.setMessageSeen(idChat, lastMessage.getMessage().getMsgId());
                                log("(B)Result setMessageSeen: " + resultMarkAsSeen);
                            }
                        }
                    }
                }
                else{
                    log("ERROR:lastMessageNUll");
                }
            }
        }
    }

    @Override
    protected void onPause(){
        log("onPause");
        super.onPause();

        activityVisible = false;
        MegaApplication.setOpenChatId(-1);
    }

    @Override
    public void onChatListItemUpdate(MegaChatApiJava api, MegaChatListItem item) {
        if(item.hasChanged(MegaChatListItem.CHANGE_TYPE_UNREAD_COUNT)) {
            updateNavigationToolbarIcon();
        }
    }

    public void updateNavigationToolbarIcon(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            if(!chatC.isInAnonymousMode()){
                int numberUnread = megaChatApi.getUnreadChats();

                if(numberUnread==0){
                    aB.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
                }
                else{

                    badgeDrawable.setProgress(1.0f);

                    if(numberUnread>9){
                        badgeDrawable.setText("9+");
                    }
                    else{
                        badgeDrawable.setText(numberUnread+"");
                    }

                    aB.setHomeAsUpIndicator(badgeDrawable);
                }
            }
            else{
                aB.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
            }
        }
        else{
            aB.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        }
    }

    @Override
    public void onChatInitStateUpdate(MegaChatApiJava api, int newState) {

    }

    @Override
    public void onChatPresenceConfigUpdate(MegaChatApiJava api, MegaChatPresenceConfig config) {

    }

    @Override
    public void onChatOnlineStatusUpdate(MegaChatApiJava api, long userHandle, int status, boolean inProgress) {
        log("onChatOnlineStatusUpdate: " + status + "___" + inProgress);
        setChatSubtitle();
        requestLastGreen(status);
    }

    @Override
    public void onChatConnectionStateUpdate(MegaChatApiJava api, long chatid, int newState) {
        log("onChatConnectionStateUpdate: "+newState);

        supportInvalidateOptionsMenu();

        if (idChat == chatid) {
            if (newState == MegaChatApi.CHAT_CONNECTION_ONLINE) {
                log("Chat is now ONLINE");
                setAsRead = true;
                setLastMessageSeen();

                if (stateHistory == MegaChatApi.SOURCE_ERROR && retryHistory) {
                    log("onChatConnectionStateUpdate:SOURCE_ERROR:call to load history again");
                    retryHistory = false;
                    loadHistory();
                }

            } else {
                setAsRead = false;
            }

            setChatSubtitle();
        }
    }

    @Override
    public void onChatPresenceLastGreen(MegaChatApiJava api, long userhandle, int lastGreen) {
        log("onChatPresenceLastGreen: "+lastGreen);
        if(!chatRoom.isGroup() && userhandle == chatRoom.getPeerHandle(0)){
            log("Update last green");
            minutesLastGreen = lastGreen;

            int state = megaChatApi.getUserOnlineStatus(chatRoom.getPeerHandle(0));

            if(state != MegaChatApi.STATUS_ONLINE && state != MegaChatApi.STATUS_BUSY && state != MegaChatApi.STATUS_INVALID){
                String formattedDate = TimeUtils.lastGreenDate(this, lastGreen);

                setLastGreen(formattedDate);

                log("Date last green: "+formattedDate);
            }
        }
    }

    public void takePicture(){
        log("takePicture");

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            Uri photoURI;
            if(photoFile != null){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    photoURI = FileProvider.getUriForFile(this, "mega.privacy.android.app.providers.fileprovider", photoFile);
                }
                else{
                    photoURI = Uri.fromFile(photoFile);
                }

                isTakePicture = false;
                mOutputFilePath = photoFile.getAbsolutePath();
                if(mOutputFilePath!=null){
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(intent, Constants.TAKE_PHOTO_CODE);
                }
            }
        }

    }

    public void uploadPicture(String path){
        log("uploadPicture - Path: "+path);

        File selfie = new File(path);

        Intent intent = new Intent(this, ChatUploadService.class);

        PendingMessageSingle pMsgSingle = new PendingMessageSingle();
        pMsgSingle.setChatId(idChat);
        long timestamp = System.currentTimeMillis()/1000;
        pMsgSingle.setUploadTimestamp(timestamp);

        String fingerprint = megaApi.getFingerprint(selfie.getAbsolutePath());

        pMsgSingle.setFilePath(selfie.getAbsolutePath());
        pMsgSingle.setName(selfie.getName());
        pMsgSingle.setFingerprint(fingerprint);

        long idMessage = dbH.addPendingMessage(pMsgSingle);
        pMsgSingle.setId(idMessage);

        if(idMessage!=-1){
            intent.putExtra(ChatUploadService.EXTRA_ID_PEND_MSG, idMessage);

            if(!isLoadingHistory){
                AndroidMegaChatMessage newNodeAttachmentMsg = new AndroidMegaChatMessage(pMsgSingle, true);
                sendMessageToUI(newNodeAttachmentMsg);
            }

            intent.putExtra(ChatUploadService.EXTRA_CHAT_ID, idChat);
            startService(intent);
        }
        else{
            log("Error when adding pending msg to the database");
        }
    }

    public void multiselectActivated(boolean flag){
        String text = textChat.getText().toString();

        if(flag){
            //multiselect on
            if(!text.isEmpty()) {
                textChat.setTextColor(ContextCompat.getColor(this, R.color.transfer_progress));
            }
            if(!sendIcon.isEnabled()){
                sendIcon.setEnabled(true);
                sendIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_send_black));
            }
        }else if(!flag){
            //multiselect off
            if(sendIcon.isEnabled()) {

                textChat.setTextColor(ContextCompat.getColor(this, R.color.black));
                if(!text.isEmpty()) {
                    sendIcon.setEnabled(true);
                    sendIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_send_black));
                }else{
                    sendIcon.setEnabled(false);
                    sendIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_send_trans));
                }
            }
        }
    }

    private void showOverquotaAlert(boolean prewarning){
        log("showOverquotaAlert: prewarning: "+prewarning);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.overquota_alert_title));

        if(prewarning){
            builder.setMessage(getString(R.string.pre_overquota_alert_text));
        }
        else{
            builder.setMessage(getString(R.string.overquota_alert_text));
        }

        if(chatAlertDialog ==null){

            builder.setPositiveButton(getString(R.string.my_account_upgrade_pro), new android.content.DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showUpgradeAccount();
                }
            });
            builder.setNegativeButton(getString(R.string.general_cancel), new android.content.DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    chatAlertDialog =null;
                }
            });

            chatAlertDialog = builder.create();
            chatAlertDialog.setCanceledOnTouchOutside(false);
        }

        chatAlertDialog.show();
    }

    public void showUpgradeAccount(){
        log("showUpgradeAccount");
        Intent upgradeIntent = new Intent(this, ManagerActivityLollipop.class);
        upgradeIntent.setAction(Constants.ACTION_SHOW_UPGRADE_ACCOUNT);
        startActivity(upgradeIntent);
    }

    public void showJumpMessage(){
        if((!isHideJump)&&(typeMessageJump!=TYPE_MESSAGE_NEW_MESSAGE)){
            typeMessageJump = TYPE_MESSAGE_JUMP_TO_LEAST;
            messageJumpText.setText(getResources().getString(R.string.message_jump_latest));
            messageJumpLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onChatCallUpdate(MegaChatApiJava api, MegaChatCall call) {
        if (call.getChatid() == idChat) {
            if (call.hasChanged(MegaChatCall.CHANGE_TYPE_STATUS)) {

                int callStatus = call.getStatus();
                switch (callStatus) {
                    case MegaChatCall.CALL_STATUS_USER_NO_PRESENT:
                    case MegaChatCall.CALL_STATUS_RING_IN:
                    case MegaChatCall.CALL_STATUS_REQUEST_SENT:
                    case MegaChatCall.CALL_STATUS_IN_PROGRESS:{
                        log("onChatCallUpdate:STATUS: "+callStatus);
                        showCallLayout(call);
                        break;
                    }

                    case MegaChatCall.CALL_STATUS_DESTROYED: {
                        log("onChatCallUpdate:STATUS: DESTROYED");

                        setSubtitleVisibility();
                        if (chronoCall != null) {
                            chronoCall.stop();
                            chronoCall.setVisibility(View.GONE);
                        }
                        if (callInProgressLayout.getVisibility() != View.GONE) {
                            callInProgressLayout.setVisibility(View.GONE);
                            callInProgressLayout.setOnClickListener(null);
                            if (callInProgressChrono != null) {
                                callInProgressChrono.stop();
                                callInProgressChrono.setVisibility(View.GONE);
                            }
                        }
                        invalidateOptionsMenu();
                        break;
                    }

                    default:
                        break;
                }

            } else if ((call.hasChanged(MegaChatCall.CHANGE_TYPE_REMOTE_AVFLAGS))||(call.hasChanged(MegaChatCall.CHANGE_TYPE_LOCAL_AVFLAGS)) || (call.hasChanged(MegaChatCall.CHANGE_TYPE_CALL_COMPOSITION))) {
                log("onChatCallUpdate: REMOTE_AVFLAGS || LOCAL_AVFLAGS || COMPOSITION");
                usersWithVideo();
            }
        }else{
            log("onChatCallUpdate: different chat");
        }
    }

    private void showCallLayout(MegaChatCall call){
        log("showCallLayout");
        if((call==null)&&(megaChatApi!=null)){
            call = megaChatApi.getChatCall(idChat);
            if (call == null) {
                return;
            }
        }

        if((call.getStatus() == MegaChatCall.CALL_STATUS_USER_NO_PRESENT) || (call.getStatus() == MegaChatCall.CALL_STATUS_RING_IN)){
            log("showCallLayout: USER_NO_PRESENT || RING_IN");

            if(isGroup()){
                //Group:
                log("showCallLayout: USER_NO_PRESENT || RING_IN - Group");
                if(chronoCall!=null){
                    chronoCall.stop();
                    chronoCall.setVisibility(View.GONE);
                }
                usersWithVideo();

                long callerHandle = call.getCaller();
                if(callerHandle != -1){
                    String textJoinCall = getString(R.string.join_call_layout_in_group_call, getPeerFullName(callerHandle));
                    callInProgressText.setText(textJoinCall);
                }else{
                    callInProgressText.setText(getString(R.string.join_call_layout));
                }
                callInProgressLayout.setVisibility(View.VISIBLE);
                callInProgressLayout.setOnClickListener(this);

                if(callInProgressChrono!=null){
                    callInProgressChrono.stop();
                    callInProgressChrono.setVisibility(View.GONE);
                }
                usersWithVideo();

            }else{
                //Individual:
                log("showCallLayout: USER_NO_PRESENT || RING_IN - Individual");

                if(chronoCall!=null){
                    chronoCall.stop();
                    chronoCall.setVisibility(View.GONE);
                }

                callInProgressLayout.setVisibility(View.GONE);
                callInProgressLayout.setOnClickListener(this);
                callInProgressText.setText(getString(R.string.call_in_progress_layout));

                if(callInProgressChrono!=null){
                    callInProgressChrono.stop();
                    callInProgressChrono.setVisibility(View.GONE);
                }
            }

        }else if(call.getStatus() == MegaChatCall.CALL_STATUS_REQUEST_SENT){
            log("showCallLayout: REQUEST_SENT");

            if(chronoCall!=null){
                chronoCall.stop();
                chronoCall.setVisibility(View.GONE);
            }

            callInProgressLayout.setVisibility(View.VISIBLE);
            callInProgressLayout.setOnClickListener(this);
            callInProgressText.setText(getString(R.string.call_in_progress_layout));
            if(callInProgressChrono!=null){
                callInProgressChrono.stop();
                callInProgressChrono.setVisibility(View.GONE);
            }


        }else if(call.getStatus() == MegaChatCall.CALL_STATUS_IN_PROGRESS){
            log("showCallLayout: IN_PROGRESS");

            callInProgressLayout.setVisibility(View.VISIBLE);
            callInProgressLayout.setOnClickListener(this);
            callInProgressText.setText(getString(R.string.call_in_progress_layout));
            if(callInProgressChrono!=null){
                callInProgressChrono.setVisibility(View.VISIBLE);
                callInProgressChrono.setBase(SystemClock.elapsedRealtime() - (call.getDuration()*1000));
                callInProgressChrono.start();
                callInProgressChrono.setFormat("%s");
            }

            if(isGroup()) {
                log("showCallLayout: IN_PROGRESS - Group");

                //Group:
                subtitleCall.setVisibility(View.VISIBLE);
                individualSubtitleToobar.setVisibility(View.GONE);
                groupalSubtitleToolbar.setVisibility(View.GONE);
                if(chronoCall!=null){
                    chronoCall.setVisibility(View.VISIBLE);
                    chronoCall.setBase(SystemClock.elapsedRealtime() - (call.getDuration()*1000));
                    chronoCall.start();
                    chronoCall.setFormat("%s");
                }
                usersWithVideo();

            }else{
                log("showCallLayout: IN_PROGRESS - Individual");

                //Individual:
                if(chronoCall!=null){
                    chronoCall.stop();
                    chronoCall.setVisibility(View.GONE);
                }
            }
            invalidateOptionsMenu();
        }
    }

    private  void usersWithVideo(){
        if(megaChatApi!=null){
            MegaChatCall call = megaChatApi.getChatCall(idChat);
            if(call!=null){
                if(isGroup() && (subtitleCall.getVisibility() == View.VISIBLE)){
                    int usersWithVideo = call.getNumParticipants(MegaChatCall.VIDEO);
                    int totalVideosAllowed = megaChatApi.getMaxVideoCallParticipants();
                    if((usersWithVideo > 0) && (totalVideosAllowed != 0)){
                        participantsText.setText(usersWithVideo + "/" + totalVideosAllowed);
                        participantsLayout.setVisibility(View.VISIBLE);
                    }else{
                        participantsLayout.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    public void goToEnd(){
        log("goToEnd()");
        int infoToShow = -1;
        if((messages!=null)&&(messages.size()>0)){
            int index = messages.size()-1;

            AndroidMegaChatMessage msg = messages.get(index);

            while (!msg.isUploading() && msg.getMessage().getStatus() == MegaChatMessage.STATUS_SENDING_MANUAL) {
                index--;
                if (index == -1) {
                    break;
                }
                msg = messages.get(index);
            }

            if(index == (messages.size()-1)){
                //Scroll to end
                mLayoutManager.scrollToPositionWithOffset(index+1,Util.scaleHeightPx(20, outMetrics));
            }else{
                index++;
                infoToShow = adjustInfoToShow(index);
                if(infoToShow== AndroidMegaChatMessage.CHAT_ADAPTER_SHOW_ALL){
                    mLayoutManager.scrollToPositionWithOffset(index, Util.scaleHeightPx(50, outMetrics));
                }else{
                    mLayoutManager.scrollToPositionWithOffset(index, Util.scaleHeightPx(20, outMetrics));
                }
            }
        }

        hideMessageJump();
    }

    public void setNewVisibility(boolean vis){
        newVisibility = vis;
    }

    public void hideMessageJump(){
        isHideJump = true;
        visibilityMessageJump=false;
        if(messageJumpLayout.getVisibility() == View.VISIBLE){
            messageJumpLayout.animate()
                        .alpha(0.0f)
                        .setDuration(1000)
                        .withEndAction(new Runnable() {
                            @Override public void run() {
                                messageJumpLayout.setVisibility(View.GONE);
                                messageJumpLayout.setAlpha(1.0f);
                            }
                        })
                        .start();
        }
    }

    public MegaApiAndroid getLocalMegaApiFolder() {

        PackageManager m = getPackageManager();
        String s = getPackageName();
        PackageInfo p;
        String path = null;
        try {
            p = m.getPackageInfo(s, 0);
            path = p.applicationInfo.dataDir + "/";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        MegaApiAndroid megaApiFolder = new MegaApiAndroid(MegaApplication.APP_KEY,
                MegaApplication.USER_AGENT, path);

        megaApiFolder.setDownloadMethod(MegaApiJava.TRANSFER_METHOD_AUTO_ALTERNATIVE);
        megaApiFolder.setUploadMethod(MegaApiJava.TRANSFER_METHOD_AUTO_ALTERNATIVE);

        return megaApiFolder;
    }

    public File createImageFile() {
        log("createImageFile");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "picture" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        return new File(storageDir, imageFileName + ".jpg");
    }

    private void onCaptureImageResult() {
        log("onCaptureImageResult");
        if (mOutputFilePath != null) {
            File f = new File(mOutputFilePath);
            if(f!=null){
                try {
                    File publicFile = copyImageFile(f);
                    //Remove mOutputFilePath
                    if (f.exists()) {
                        if (f.isDirectory()) {
                            if(f.list().length <= 0){
                                f.delete();
                            }
                        }else{
                            f.delete();
                        }
                    }
                    if(publicFile!=null){
                        Uri finalUri = Uri.fromFile(publicFile);
                        galleryAddPic(finalUri);
                        uploadPicture(publicFile.getPath());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public File copyImageFile(File fileToCopy) throws IOException {
        log("copyImageFile");
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        File copyFile = new File(storageDir, fileToCopy.getName());
        copyFile.createNewFile();
        copy(fileToCopy, copyFile);
        return copyFile;
    }

    public static void copy(File src, File dst) throws IOException {
        log("copy");
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    private void galleryAddPic(Uri contentUri) {
        log("galleryAddPic");
        if(contentUri!=null){
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);
            sendBroadcast(mediaScanIntent);
        }
    }

    public int getGroupPermission(){
        int permission = chatRoom.getOwnPrivilege();
        return permission;
    }


    public void hideKeyboard() {
        if (fileStorageLayout.isShown()) {
            hideFileStorageSection();
        }
        if(emojiKeyboard!=null) {
            emojiKeyboard.hideBothKeyboard(this);
        }

    }

    public void showConfirmationConnect(){
        log("showConfirmationConnect");

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        startConnection();
                        finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        log("showConfirmationConnect: BUTTON_NEGATIVE");
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        try {
            builder.setMessage(R.string.confirmation_to_reconnect).setPositiveButton(R.string.cam_sync_ok, dialogClickListener)
                    .setNegativeButton(R.string.general_cancel, dialogClickListener).show().setCanceledOnTouchOutside(false);
        }
        catch (Exception e){}
    }

    public void startConnection() {
        log("startConnection: Broadcast to ManagerActivity");
        Intent intent = new Intent(Constants.BROADCAST_ACTION_INTENT_CONNECTIVITY_CHANGE);
        intent.putExtra("actionType", Constants.START_RECONNECTION);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    public int getDeviceDensity(){
        int screen = 0;
        switch (getResources().getDisplayMetrics().densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
                screen = 1;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                screen = 1;
                break;
            case DisplayMetrics.DENSITY_HIGH:
                screen = 1;
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                screen = 0;
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                screen = 0;
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                screen = 0;
                break;
            default:
                screen = 0;
        }
        return screen;
    }

    public void setNodeAttachmentVisible() {
        log("setNodeAttachmentVisible");
        if (adapter != null && holder_imageDrag != null && position_imageDrag != -1) {
            adapter.setNodeAttachmentVisibility(true, holder_imageDrag, position_imageDrag);
            holder_imageDrag = null;
            position_imageDrag = -1;
        }
    }

    public void hideFileStorageSection(){
        log("hideFileStorageSEctocioon");
        if (fileStorageF != null) {
            fileStorageF.clearSelections();
            fileStorageF.hideMultipleSelect();
        }
        fileStorageLayout.setVisibility(View.GONE);
        pickFileStorageButton.setImageResource(R.drawable.ic_b_select_image);
    }

    public void setShareLinkDialogDismissed (boolean dismissed) {
        isShareLinkDialogDismissed = dismissed;
    }
}