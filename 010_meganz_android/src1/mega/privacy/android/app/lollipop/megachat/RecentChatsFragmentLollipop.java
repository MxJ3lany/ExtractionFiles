package mega.privacy.android.app.lollipop.megachat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.ChatDividerItemDecoration;
import mega.privacy.android.app.components.scrollBar.FastScroller;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.controllers.ChatController;
import mega.privacy.android.app.lollipop.listeners.ChatNonContactNameListener;
import mega.privacy.android.app.lollipop.megachat.calls.ChatCallActivity;
import mega.privacy.android.app.lollipop.megachat.chatAdapters.MegaListChatLollipopAdapter;
import mega.privacy.android.app.utils.ChatUtil;
import mega.privacy.android.app.utils.Constants;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaChatApi;
import nz.mega.sdk.MegaChatApiAndroid;
import nz.mega.sdk.MegaChatCall;
import nz.mega.sdk.MegaChatListItem;
import nz.mega.sdk.MegaChatRoom;

import static mega.privacy.android.app.utils.Util.adjustForLargeFont;

public class RecentChatsFragmentLollipop extends Fragment implements View.OnClickListener {

    private static final String BUNDLE_RECYCLER_LAYOUT = "classname.recycler.layout";

    MegaApiAndroid megaApi;
    MegaChatApiAndroid megaChatApi;

    DatabaseHandler dbH;

    Context context;
    ActionBar aB;
    MegaListChatLollipopAdapter adapterList;
    RelativeLayout mainRelativeLayout;

    RecyclerView listView;
    LinearLayoutManager mLayoutManager;
    FastScroller fastScroller;

    ArrayList<MegaChatListItem> chats;

    FilterChatsTask filterChatsTask;

    int lastFirstVisiblePosition;

    int numberOfClicks = 0;

    //Empty screen
    TextView emptyTextView;
    LinearLayout emptyLayout;
    TextView emptyTextViewInvite;
    ImageView emptyImageView;

    //Call
    RelativeLayout callInProgressLayout;
    Chronometer callInProgressChrono;

    Button inviteButton;
    int chatStatus;

    float density;
    DisplayMetrics outMetrics;
    Display display;

    private ActionMode actionMode;

    public void activateActionMode(){
        log("activateActionMode");
        if (!adapterList.isMultipleSelect()){
            adapterList.setMultipleSelect(true);
            actionMode = ((AppCompatActivity)context).startSupportActionMode(new ActionBarCallBack());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log("onCreate");

        if (megaApi == null){
            megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
        }

        dbH = DatabaseHandler.getDbHandler(getActivity());

        if(Util.isChatEnabled()){
            if (megaChatApi == null){
                megaChatApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaChatApi();
            }
        }
        else{
            log("Chat not enabled!");
        }
    }

    public void checkScroll() {
        if (listView != null) {
            if (context instanceof ManagerActivityLollipop) {
                if (listView.canScrollVertically(-1) || (adapterList != null && adapterList.isMultipleSelect())) {
                    ((ManagerActivityLollipop) context).changeActionBarElevation(true);
                }
                else {
                    ((ManagerActivityLollipop) context).changeActionBarElevation(false);
                }
            }
            else if (context instanceof ArchivedChatsActivity) {
                if (listView.canScrollVertically(-1) || (adapterList != null && adapterList.isMultipleSelect())) {
                    ((ArchivedChatsActivity) context).changeActionBarElevation(true);
                }
                else {
                    ((ArchivedChatsActivity) context).changeActionBarElevation(false);
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        log("onCreateView");

        display = ((Activity) context).getWindowManager().getDefaultDisplay();
        outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        density = getResources().getDisplayMetrics().density;

        View v = inflater.inflate(R.layout.chat_recent_tab, container, false);

        listView = (RecyclerView) v.findViewById(R.id.chat_recent_list_view);
        fastScroller = (FastScroller) v.findViewById(R.id.fastscroll_chat);
        listView.setPadding(0, 0, 0, Util.scaleHeightPx(85, outMetrics));
        listView.setClipToPadding(false);
        listView.addItemDecoration(new ChatDividerItemDecoration(context, outMetrics));
        mLayoutManager = new LinearLayoutManager(context);
        listView.setLayoutManager(mLayoutManager);
        listView.setHasFixedSize(true);
        listView.setItemAnimator(new DefaultItemAnimator());
        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                checkScroll();
            }
        });
//        listView.setClipToPadding(false);

        emptyLayout = (LinearLayout) v.findViewById(R.id.linear_empty_layout_chat_recent);
        emptyTextViewInvite = (TextView) v.findViewById(R.id.empty_text_chat_recent_invite);
        emptyTextViewInvite.setWidth(Util.scaleWidthPx(236, outMetrics));
        emptyTextView = (TextView) v.findViewById(R.id.empty_text_chat_recent);

        LinearLayout.LayoutParams emptyTextViewParams1 = (LinearLayout.LayoutParams)emptyTextViewInvite.getLayoutParams();
        emptyTextViewParams1.setMargins(0, Util.scaleHeightPx(50, outMetrics), 0, Util.scaleHeightPx(24, outMetrics));
        emptyTextViewInvite.setLayoutParams(emptyTextViewParams1);

        LinearLayout.LayoutParams emptyTextViewParams2 = (LinearLayout.LayoutParams)emptyTextView.getLayoutParams();
        emptyTextViewParams2.setMargins(Util.scaleWidthPx(20, outMetrics), Util.scaleHeightPx(20, outMetrics), Util.scaleWidthPx(20, outMetrics), Util.scaleHeightPx(20, outMetrics));
        emptyTextView.setLayoutParams(emptyTextViewParams2);

        emptyImageView = (ImageView) v.findViewById(R.id.empty_image_view_recent);
        emptyImageView.setOnClickListener(this);

        if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            emptyImageView.setImageResource(R.drawable.chat_empty_landscape);
        }else{
            emptyImageView.setImageResource(R.drawable.ic_empty_chat_list);
        }

        inviteButton = (Button) v.findViewById(R.id.invite_button);
        inviteButton.setOnClickListener(this);

        callInProgressLayout = (RelativeLayout) v.findViewById(R.id.call_in_progress_layout);
        callInProgressLayout.setOnClickListener(this);
        callInProgressChrono = (Chronometer) v.findViewById(R.id.call_in_progress_chrono);
        callInProgressLayout.setVisibility(View.GONE);
        callInProgressChrono.setVisibility(View.GONE);

        mainRelativeLayout = (RelativeLayout) v.findViewById(R.id.main_relative_layout);

        if(Util.isChatEnabled()){
            log("Chat ENABLED");

            if(context instanceof ManagerActivityLollipop){
                setStatus();
                if(!emptyArchivedChats()){
                    listView.setPadding(0,Util.scaleHeightPx(8, outMetrics),0,Util.scaleHeightPx(16, outMetrics));
                }
                else{
                    listView.setPadding(0,Util.scaleHeightPx(8, outMetrics),0,Util.scaleHeightPx(78, outMetrics));
                }
            }
            else{
                //Archived chats section
                aB.setSubtitle(null);
                listView.setPadding(0,Util.scaleHeightPx(8, outMetrics),0,0);
            }

            this.setChats();

            if(megaChatApi.isSignalActivityRequired()){
                megaChatApi.signalPresenceActivity();
            }
        }
        else{
            log("Chat DISABLED");
            if(Util.isOnline(context)){
                showDisableChatScreen();
            }
            else{
                showNoConnectionScreen();
            }
        }

        return v;
    }

    public static RecentChatsFragmentLollipop newInstance() {
        log("newInstance");
        RecentChatsFragmentLollipop fragment = new RecentChatsFragmentLollipop();
        return fragment;
    }

    public void setChats(){
        log("setChats");

        if(listView==null){
            log("setChats:listView is null - do not update");
            return;
        }

        if(isAdded()){
            if(Util.isChatEnabled()){

                int initState = megaChatApi.getInitState();
                log("Init state is: "+initState);

                if((initState==MegaChatApi.INIT_ONLINE_SESSION)){
                    log("Connected state is: "+megaChatApi.getConnectionState());

                    if(megaChatApi.getConnectionState()==MegaChatApi.CONNECTED){
                        if(chats!=null){
                            chats.clear();
                        }
                        else{
                            chats = new ArrayList<MegaChatListItem>();
                        }

                        if(context instanceof ManagerActivityLollipop){
                            chats = megaChatApi.getChatListItems();
                        }
                        else{
                            chats = megaChatApi.getArchivedChatListItems();
                        }

                        if((chats==null || chats.isEmpty()) && emptyArchivedChats()){
                            if(Util.isOnline(context)){
                                showEmptyChatScreen();
                            }
                            else{
                                showNoConnectionScreen();
                            }
                        }
                        else{
                            log("setChats:chats size: "+chats.size());

                            //Order by last interaction
                            Collections.sort(chats, new Comparator<MegaChatListItem> (){

                                public int compare(MegaChatListItem c1, MegaChatListItem c2) {
                                    long timestamp1 = c1.getLastTimestamp();
                                    long timestamp2 = c2.getLastTimestamp();

                                    long result = timestamp2 - timestamp1;
                                    return (int)result;
                                }
                            });

                            if (adapterList == null){
                                log("adapterList is NULL");
                                adapterList = new MegaListChatLollipopAdapter(context, this, chats, listView, MegaListChatLollipopAdapter.ADAPTER_RECENT_CHATS);
                            }
                            else{
                                adapterList.setChats(chats);
                            }

                            listView.setAdapter(adapterList);
                            fastScroller.setRecyclerView(listView);
                            visibilityFastScroller();

                            adapterList.setPositionClicked(-1);

                            listView.setVisibility(View.VISIBLE);
                            emptyLayout.setVisibility(View.GONE);
                        }
                    }
                    else{
                        log("Show chat screen connecting...");
                        showConnectingChatScreen();
                    }
                }
                else if(initState == MegaChatApi.INIT_OFFLINE_SESSION){
                    log("Init with OFFLINE session");
                    if(chats!=null){
                        chats.clear();
                    }
                    else{
                        chats = new ArrayList<MegaChatListItem>();
                    }

                    if(context instanceof ManagerActivityLollipop){
                        chats = megaChatApi.getChatListItems();
                    }
                    else{
                        chats = megaChatApi.getArchivedChatListItems();
                    }

                    if(chats==null || chats.isEmpty()){
                        showNoConnectionScreen();
                    }
                    else{
                        log("chats no: "+chats.size());

                        //Order by last interaction
                        Collections.sort(chats, new Comparator<MegaChatListItem> (){

                            public int compare(MegaChatListItem c1, MegaChatListItem c2) {
                                long timestamp1 = c1.getLastTimestamp();
                                long timestamp2 = c2.getLastTimestamp();

                                long result = timestamp2 - timestamp1;
                                return (int)result;
                            }
                        });

                        if(listView==null){
                            log("setChats: INIT_OFFLINE_SESSION: listView is null");
                        }

                        listView.setVisibility(View.VISIBLE);
                        emptyLayout.setVisibility(View.GONE);

                        if (adapterList == null){
                            log("adapterList is NULL");
                            adapterList = new MegaListChatLollipopAdapter(context, this, chats, listView, MegaListChatLollipopAdapter.ADAPTER_RECENT_CHATS);
                            listView.setAdapter(adapterList);
                        }
                        else{
                            adapterList.setChats(chats);
                        }

                        fastScroller.setRecyclerView(listView);
                        visibilityFastScroller();
                        adapterList.setPositionClicked(-1);
                    }
                }
                else{
                    log("Show chat screen connecting...");
                    showConnectingChatScreen();
                }
            }
            else{
                if(Util.isOnline(context)){
                    showDisableChatScreen();
                }
                else{
                    showNoConnectionScreen();
                }
            }
        }
    }

    public void showCallLayout(){
        if(Util.isChatEnabled() && (context instanceof ManagerActivityLollipop) && (megaChatApi!=null) &&(ChatUtil.participatingInACall(megaChatApi))){
            log("showCallLayout");

            if((callInProgressLayout!=null) && (callInProgressLayout.getVisibility() != View.VISIBLE)){
                callInProgressLayout.setVisibility(View.VISIBLE);
            }
            if((callInProgressChrono!=null) && (callInProgressChrono.getVisibility() != View.VISIBLE)){
                long chatId = ChatUtil.getChatCallInProgress(megaChatApi);
                if((megaChatApi!=null) && (chatId != -1)){
                    MegaChatCall call = megaChatApi.getChatCall(chatId);
                    if(call!=null){
                        callInProgressChrono.setVisibility(View.VISIBLE);
                        callInProgressChrono.setBase(SystemClock.elapsedRealtime() - (call.getDuration()*1000));
                        callInProgressChrono.start();
                        callInProgressChrono.setFormat("%s");
                    }
                }
            }
        }else{

            if(callInProgressChrono!=null){
                callInProgressChrono.stop();
                callInProgressChrono.setVisibility(View.GONE);
            }
            if(callInProgressLayout!=null){
                callInProgressLayout.setVisibility(View.GONE);
            }
        }
    }

    public void showEmptyChatScreen(){
        log("showEmptyChatScreen");

        listView.setVisibility(View.GONE);

        String textToShowB = "";

        if(context instanceof ArchivedChatsActivity){
            textToShowB = String.format(context.getString(R.string.archived_chats_empty));
            emptyTextViewInvite.setVisibility(View.INVISIBLE);
            inviteButton.setVisibility(View.GONE);
        }
        else{
            String textToShow = String.format(context.getString(R.string.context_empty_chat_recent));

            try{
                textToShow = textToShow.replace("[A]", "<font color=\'#000000\'>");
                textToShow = textToShow.replace("[/A]", "</font>");
                textToShow = textToShow.replace("[B]", "<font color=\'#7a7a7a\'>");
                textToShow = textToShow.replace("[/B]", "</font>");
            }
            catch (Exception e){}
            Spanned result = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
            } else {
                result = Html.fromHtml(textToShow);
            }

            emptyTextViewInvite.setText(result);
            emptyTextViewInvite.setVisibility(View.VISIBLE);

            inviteButton.setText(getString(R.string.contact_invite));
            inviteButton.setVisibility(View.VISIBLE);


        }

        textToShowB = String.format(context.getString(R.string.recent_chat_empty).toUpperCase());
        try{
            textToShowB = textToShowB.replace("[A]", "<font color=\'#7a7a7a\'>");
            textToShowB = textToShowB.replace("[/A]", "</font>");
            textToShowB = textToShowB.replace("[B]", "<font color=\'#000000\'>");
            textToShowB = textToShowB.replace("[/B]", "</font>");
        }
        catch (Exception e){}
        Spanned resultB = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            resultB = Html.fromHtml(textToShowB,Html.FROM_HTML_MODE_LEGACY);
        } else {
            resultB = Html.fromHtml(textToShowB);
        }
        emptyTextView.setText(resultB);
        if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            emptyTextView.setVisibility(View.GONE);
        }else{
            emptyTextView.setVisibility(View.VISIBLE);
        }
        emptyLayout.setVisibility(View.VISIBLE);
    }

    public void showDisableChatScreen(){
        log("showDisableChatScreen");

        listView.setVisibility(View.GONE);
        if(context instanceof ManagerActivityLollipop){
            ((ManagerActivityLollipop)context).hideFabButton();
        }

        String textToShow = String.format(context.getString(R.string.recent_chat_empty_enable_chat));

        try{
            textToShow = textToShow.replace("[A]", "<br />");
            textToShow = textToShow.replace("[B]", "<font color=\'#000000\'>");
            textToShow = textToShow.replace("[/B]", "</font>");
            textToShow = textToShow.replace("[C]", "<font color=\'#7a7a7a\'>");
            textToShow = textToShow.replace("[/C]", "</font>");

        }
        catch (Exception e){}
        Spanned result = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(textToShow);

        }
        emptyTextViewInvite.setText(result);
        emptyTextViewInvite.setVisibility(View.VISIBLE);

        inviteButton.setText(getString(R.string.recent_chat_enable_chat_button));
        inviteButton.setVisibility(View.VISIBLE);

        emptyTextView.setText(R.string.recent_chat_enable_chat);
        if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            emptyTextView.setVisibility(View.GONE);
        }else{
            emptyTextView.setVisibility(View.VISIBLE);
        }
        emptyLayout.setVisibility(View.VISIBLE);
    }

    public void showConnectingChatScreen(){
        log("showConnectingChatScreen");

        listView.setVisibility(View.GONE);
        if(context instanceof ManagerActivityLollipop){
            ((ManagerActivityLollipop)context).hideFabButton();
        }

        String textToShow = String.format(context.getString(R.string.context_empty_chat_recent));

        try{
            textToShow = textToShow.replace("[A]", "<font color=\'#000000\'>");
            textToShow = textToShow.replace("[/A]", "</font>");
            textToShow = textToShow.replace("[B]", "<font color=\'#7a7a7a\'>");
            textToShow = textToShow.replace("[/B]", "</font>");
        }
        catch (Exception e){}
        Spanned result = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(textToShow);

        }

        emptyTextViewInvite.setText(result);
        emptyTextViewInvite.setVisibility(View.INVISIBLE);

        inviteButton.setVisibility(View.GONE);

        String textToShowB = String.format(context.getString(R.string.recent_chat_loading_conversations));
        try{
            textToShowB = textToShowB.replace("[A]", "<font color=\'#7a7a7a\'>");
            textToShowB = textToShowB.replace("[/A]", "</font>");
            textToShowB = textToShowB.replace("[B]", "<font color=\'#000000\'>");
            textToShowB = textToShowB.replace("[/B]", "</font>");
        }
        catch (Exception e){}
        Spanned resultB = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            resultB = Html.fromHtml(textToShowB,Html.FROM_HTML_MODE_LEGACY);
        }else{
            resultB = Html.fromHtml(textToShowB);
        }
        emptyTextView.setText(resultB);
        emptyTextView.setVisibility(View.VISIBLE);
    }

    public void showNoConnectionScreen(){
        log("showNoConnectionScreen");

        listView.setVisibility(View.GONE);
        if(context instanceof ManagerActivityLollipop){
            ((ManagerActivityLollipop)context).hideFabButton();
        }

        emptyTextViewInvite.setText(getString(R.string.error_server_connection_problem));
        emptyTextViewInvite.setVisibility(View.VISIBLE);
        inviteButton.setVisibility(View.GONE);

        emptyTextView.setText(R.string.recent_chat_empty_no_connection_text);
        if(Util.isChatEnabled()){
            emptyTextView.setVisibility(View.GONE);
        }
        else{
            emptyTextView.setVisibility(View.VISIBLE);
        }

        emptyLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        log("onClick");

        switch (v.getId()) {
            case R.id.invite_button:{
                if(Util.isChatEnabled()){
                    //((ManagerActivityLollipop)context).chooseAddContactDialog(false);
                    if(Util.isOnline(context)){
                        ((ManagerActivityLollipop)context).addContactFromPhone();
                        if(megaChatApi.isSignalActivityRequired()){
                            megaChatApi.signalPresenceActivity();
                        }
                    }
                    else{
                        ((ManagerActivityLollipop)context).showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.error_server_connection_problem), -1);
                    }
                }
                else{
                    if(Util.isOnline(context)){
                        if(megaApi!=null){
                            if(megaApi.isLoggedIn()==0){
                                ((ManagerActivityLollipop)context).showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.error_enable_chat_before_login), -1);
                            }
                            else{
                                ChatController chatController = new ChatController(context);
                                log("onCLick: enableChat");
                                chatController.enableChat();
                                getActivity().invalidateOptionsMenu();
                                ((ManagerActivityLollipop)context).enableChat();
                            }
                        }
                        else{
                            ((ManagerActivityLollipop)context).showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.error_enable_chat_before_login), -1);
                        }
                    }
                    else{
                        ((ManagerActivityLollipop)context).showSnackbar(Constants.SNACKBAR_TYPE, getString(R.string.error_server_connection_problem), -1);
                        showNoConnectionScreen();
                    }
//                    setChats();
                }

                break;
            }
            case R.id.empty_image_view_chat:{
                numberOfClicks++;
                log("Number of clicks: "+numberOfClicks);
                if (numberOfClicks >= 5){
                    numberOfClicks = 0;
                    showStateInfo();
                }

                break;
            }
            case R.id.call_in_progress_layout:{
                log("onClick:call_in_progress_layout");
                if(checkPermissionsCall()){
                    ChatUtil.returnCall(context, megaChatApi);
                }
                break;
            }
        }
    }

    public void showStateInfo(){

        StringBuilder builder = new StringBuilder();

        if(Util.isChatEnabled()){
            if(megaChatApi!=null){
                builder.append("INIT STATE: " +megaChatApi.getInitState());
                builder.append("\nCONNECT STATE: " +megaChatApi.getConnectionState());
                if(Util.isOnline(context)){
                    builder.append("\nNetwork OK");
                }
                else{
                    builder.append("\nNo network connection");
                }
            }
            else{
                builder.append("MegaChatApi: false");
            }
        }
        else{
            builder.append("Chat is disabled");
            if(megaChatApi!=null){
                builder.append("\nINIT STATE: " +megaChatApi.getInitState());
                builder.append("\nCONNECT STATE: " +megaChatApi.getConnectionState());
                if(Util.isOnline(context)){
                    builder.append("\nNetwork OK");
                }
                else{
                    builder.append("\nNo network connection");
                }
            }
        }

        Toast.makeText(context, builder, Toast.LENGTH_LONG).show();
        log("showStateInfo: "+builder);
    }

    /////Multiselect/////
    private class ActionBarCallBack implements ActionMode.Callback {

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            ArrayList<MegaChatListItem> chats = adapterList.getSelectedChats();

            if(megaChatApi.isSignalActivityRequired()){
                megaChatApi.signalPresenceActivity();
            }

            switch(item.getItemId()){
                case R.id.cab_menu_select_all:{
                    selectAll();
                    actionMode.invalidate();
                    break;
                }
                case R.id.cab_menu_unselect_all:{
                    clearSelections();
                    hideMultipleSelect();
                    actionMode.invalidate();
                    break;
                }
                case R.id.cab_menu_mute:{
                    clearSelections();
                    hideMultipleSelect();
                    ChatController chatC = new ChatController(context);
                    chatC.muteChats(chats);
//                    setChats();
                    break;
                }
                case R.id.cab_menu_unmute:{
                    clearSelections();
                    hideMultipleSelect();
                    ChatController chatC = new ChatController(context);
                    chatC.unmuteChats(chats);
//                    setChats();
                    break;
                }
                case R.id.cab_menu_archive:
                case R.id.cab_menu_unarchive:{
                    clearSelections();
                    hideMultipleSelect();
                    ChatController chatC = new ChatController(context);
                    chatC.archiveChats(chats);
                    break;
                }
                case R.id.cab_menu_delete:{
                    clearSelections();
                    hideMultipleSelect();
                    //Delete
                    Toast.makeText(context, "Not yet implemented! Delete: "+chats.size()+" chats",Toast.LENGTH_SHORT).show();
                    break;
                }
                case R.id.chat_list_leave_chat_layout:{
                    //Leave group chat
                    ((ManagerActivityLollipop)context).showConfirmationLeaveChats(chats);
                    clearSelections();
                    hideMultipleSelect();
                    break;
                }
            }
            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.recent_chat_action, menu);
            adapterList.setMultipleSelect(true);
            if(context instanceof ManagerActivityLollipop){
                ((ManagerActivityLollipop)context).hideFabButton();
                ((ManagerActivityLollipop) context).showHideBottomNavigationView(true);
                ((ManagerActivityLollipop) context).changeStatusBarColor(Constants.COLOR_STATUS_BAR_ACCENT);
                checkScroll();
            }
            else if(context instanceof ArchivedChatsActivity){
                ((ArchivedChatsActivity)context).changeStatusBarColor(1);
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode arg0) {
            clearSelections();
            adapterList.setMultipleSelect(false);
            if(context instanceof ManagerActivityLollipop){
                ((ManagerActivityLollipop)context).showFabButton();
                ((ManagerActivityLollipop) context).showHideBottomNavigationView(false);
                ((ManagerActivityLollipop) context).changeStatusBarColor(Constants.COLOR_STATUS_BAR_ZERO_DELAY);
                checkScroll();
            }
            else if(context instanceof ArchivedChatsActivity){
                ((ArchivedChatsActivity)context).changeStatusBarColor(0);
            }
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            List<MegaChatListItem> selected = adapterList.getSelectedChats();
            boolean showMute = false;
            boolean showUnmute = false;
            boolean showLeaveChat =false;

            if(context instanceof ManagerActivityLollipop){
                if (selected.size() != 0) {

                    menu.findItem(R.id.cab_menu_archive).setVisible(false);
                    menu.findItem(R.id.cab_menu_delete).setVisible(false);

                    menu.findItem(R.id.cab_menu_archive).setVisible(true);
                    menu.findItem(R.id.cab_menu_unarchive).setVisible(false);

                    MenuItem unselect = menu.findItem(R.id.cab_menu_unselect_all);

                    if(!emptyArchivedChats() && selected.size()==adapterList.getItemCount()-1) {
                        menu.findItem(R.id.cab_menu_select_all).setVisible(false);
                        unselect.setTitle(getString(R.string.action_unselect_all));
                        unselect.setVisible(true);
                    }
                    else if(selected.size()==adapterList.getItemCount()){
                        menu.findItem(R.id.cab_menu_select_all).setVisible(false);
                        unselect.setTitle(getString(R.string.action_unselect_all));
                        unselect.setVisible(true);
                    }
                    else{
                        menu.findItem(R.id.cab_menu_select_all).setVisible(true);
                        unselect.setTitle(getString(R.string.action_unselect_all));
                        unselect.setVisible(true);
                    }

                    for(int i=0;i<selected.size();i++){
                        MegaChatListItem chat = selected.get(i);
                        if(chat!=null){
                            if (chat.isGroup() && (chat.getOwnPrivilege() == MegaChatRoom.PRIV_RO || chat.getOwnPrivilege() == MegaChatRoom.PRIV_STANDARD
                                    || chat.getOwnPrivilege() == MegaChatRoom.PRIV_MODERATOR)) {
                                log("Chat Group permissions: "+chat.getOwnPrivilege());
                                showLeaveChat=true;
                            }
                            else{
                                showLeaveChat=false;
                                break;
                            }
                        }
                    }

                    for(int i=0;i<selected.size();i++){
                        MegaChatListItem chat = selected.get(i);
                        if(chat!=null){

                            String chatHandle = String.valueOf(chat.getChatId());
                            if (dbH.areNotificationsEnabled(chatHandle)) {
                                log("Chat UNMUTED");
                                showUnmute=true;
                                break;
                            }
                        }
                    }

                    for(int i=0;i<selected.size();i++){
                        MegaChatListItem chat = selected.get(i);
                        if(chat!=null){

                            String chatHandle = String.valueOf(chat.getChatId());
                            if (!(dbH.areNotificationsEnabled(chatHandle))) {
                                log("Chat MUTED");
                                showMute=true;
                                break;
                            }
                        }
                    }

                    menu.findItem(R.id.cab_menu_mute).setVisible(showUnmute);
                    menu.findItem(R.id.cab_menu_unmute).setVisible(showMute);
                    menu.findItem(R.id.chat_list_leave_chat_layout).setVisible(showLeaveChat);
                    if(showLeaveChat){
                        menu.findItem(R.id.chat_list_leave_chat_layout).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    }

                }
                else{
                    menu.findItem(R.id.cab_menu_select_all).setVisible(true);
                    menu.findItem(R.id.cab_menu_unselect_all).setVisible(false);
                    menu.findItem(R.id.cab_menu_mute).setVisible(false);
                    menu.findItem(R.id.cab_menu_unmute).setVisible(false);

                    menu.findItem(R.id.chat_list_leave_chat_layout).setVisible(false);

                }
            }
            else if(context instanceof ArchivedChatsActivity){
                menu.findItem(R.id.cab_menu_delete).setVisible(false);
                menu.findItem(R.id.cab_menu_mute).setVisible(showUnmute);
                menu.findItem(R.id.cab_menu_unmute).setVisible(showMute);
                menu.findItem(R.id.chat_list_leave_chat_layout).setVisible(showLeaveChat);

                if (selected.size() != 0) {
                    MenuItem unselect = menu.findItem(R.id.cab_menu_unselect_all);
                    if(selected.size()==adapterList.getItemCount()){
                        menu.findItem(R.id.cab_menu_select_all).setVisible(false);
                        unselect.setTitle(getString(R.string.action_unselect_all));
                        unselect.setVisible(true);
                    }
                    else{
                        menu.findItem(R.id.cab_menu_select_all).setVisible(true);
                        unselect.setTitle(getString(R.string.action_unselect_all));
                        unselect.setVisible(true);
                    }

                    menu.findItem(R.id.cab_menu_unarchive).setVisible(true);
                    menu.findItem(R.id.cab_menu_archive).setVisible(false);
                }
                else{
                    menu.findItem(R.id.cab_menu_select_all).setVisible(true);
                    menu.findItem(R.id.cab_menu_unselect_all).setVisible(false);
                }
            }

            return false;
        }

    }

    public boolean showSelectMenuItem(){
        if (adapterList != null){
            return adapterList.isMultipleSelect();
        }

        return false;
    }

    /*
     * Clear all selected items
     */
    public void clearSelections() {
        log("clearSelections");
        if(adapterList.isMultipleSelect()){
            adapterList.clearSelections();
        }
    }

    private void updateActionModeTitle() {
        if (actionMode == null || getActivity() == null) {
            return;
        }
        List<MegaChatListItem> chats = adapterList.getSelectedChats();

        actionMode.setTitle(chats.size()+"");

        try {
            actionMode.invalidate();
        } catch (NullPointerException e) {
            e.printStackTrace();
            log("oninvalidate error");
        }
    }

    /*
     * Disable selection
     */
    public void hideMultipleSelect() {
        log("hideMultipleSelect");
        adapterList.setMultipleSelect(false);

        if (actionMode != null) {
            actionMode.finish();
        }
    }

    public void selectAll() {
        if (adapterList != null) {
            if (adapterList.isMultipleSelect()) {
                adapterList.selectAll();
            } else {
                adapterList.setMultipleSelect(true);
                adapterList.selectAll();

                actionMode = ((AppCompatActivity) context).startSupportActionMode(new ActionBarCallBack());
            }

            updateActionModeTitle();
        }
    }

    public void itemClick(int position) {
        log("itemClick: "+position);
        if(megaChatApi.isSignalActivityRequired()){
            megaChatApi.signalPresenceActivity();
        }
        if (adapterList.isMultipleSelect()){
            adapterList.toggleSelection(position);
            List<MegaChatListItem> chats = adapterList.getSelectedChats();
            if (chats.size() > 0){
                updateActionModeTitle();
            }
        }
        else{
//            clearHandlers();
            log("open chat: position: "+position+" chatID: "+chats.get(position).getChatId());
            Intent intent = new Intent(context, ChatActivityLollipop.class);
            intent.setAction(Constants.ACTION_CHAT_SHOW_MESSAGES);
//            intent.putExtra("CHAT_ID", chats.get(position).getChatId());
            intent.putExtra("CHAT_ID", adapterList.getChatAt(position).getChatId());
            this.startActivity(intent);
            if (context instanceof ManagerActivityLollipop) {
                if (((ManagerActivityLollipop) context).searchQuery != null  && !((ManagerActivityLollipop) context).searchQuery.isEmpty()) {
                    closeSearch();
                    ((ManagerActivityLollipop) context).closeSearchView();
                }
            }
            else if (context instanceof ArchivedChatsActivity) {
                if (((ArchivedChatsActivity) context).querySearch != null && !((ArchivedChatsActivity) context).querySearch.isEmpty()) {
                    closeSearch();
                    ((ArchivedChatsActivity) context).closeSearchView();
                }
            }
        }
    }
    /////END Multiselect/////

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
        aB = ((AppCompatActivity)activity).getSupportActionBar();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        aB = ((AppCompatActivity)context).getSupportActionBar();
    }

    public void listItemUpdate(MegaChatListItem item) {
        log("listItemUpdate: "+item.getTitle());

        if(!isAdded()){
            log("return!");
            return;
        }

        if(listView==null){
            log("listItemUpdate:listView is null - do not update");
            return;
        }

        if(context instanceof ManagerActivityLollipop){
            if(!(((ManagerActivityLollipop)context).getDrawerItem()== ManagerActivityLollipop.DrawerItem.CHAT)){
                log("not CHAT shown!");
                return;
            }
        }

        if(item.hasChanged(MegaChatListItem.CHANGE_TYPE_STATUS)){
            log("listItemUpdate: Change status: MegaChatListItem.CHANGE_TYPE_STATUS");
        }
        else if(item.hasChanged(MegaChatListItem.CHANGE_TYPE_OWN_PRIV)){

            log("listItemUpdate: Change status: MegaChatListItem.CHANGE_TYPE_OWN_PRIV");
        }
        else if(item.hasChanged(MegaChatListItem.CHANGE_TYPE_PARTICIPANTS)){

            log("listItemUpdate: Change participants");
            MegaChatRoom chatToCheck = megaChatApi.getChatRoom(item.getChatId());
            updateCacheForNonContacts(chatToCheck);
        }
        else if(item.hasChanged(MegaChatListItem.CHANGE_TYPE_UNREAD_COUNT)){

            log("listItemUpdate: Change unread count: "+item.getTitle());
            if(item!=null){

                if (adapterList == null || adapterList.getItemCount()==0){
                    setChats();
                }
                else{
                    long chatHandleToUpdate = item.getChatId();
                    int indexToReplace = -1;
                    ListIterator<MegaChatListItem> itrReplace = chats.listIterator();
                    while (itrReplace.hasNext()) {
                        MegaChatListItem chat = itrReplace.next();
                        if(chat!=null){
                            if(chat.getChatId()==chatHandleToUpdate){
                                indexToReplace = itrReplace.nextIndex()-1;
                                break;
                            }
                        }
                        else{
                            break;
                        }
                    }
                    if(indexToReplace!=-1){
                        log("Index to replace: "+indexToReplace);
                        log("Unread count: "+item.getUnreadCount());

                        chats.set(indexToReplace, item);
                        if(item.getUnreadCount()==0){
                            onUnreadCountChange(indexToReplace, false);
                            onLastMessageChange(indexToReplace);
                        }
                        else{
                            onUnreadCountChange(indexToReplace, true);
                        }

                    }
                }
            }
        }
        else if(item.hasChanged(MegaChatListItem.CHANGE_TYPE_LAST_TS)){

            log("Change last ts: "+item.getChanges());

            long chatHandleToUpdate = item.getChatId();
            int indexToReplace = -1;

            if(chats!=null && !chats.isEmpty()){
                ListIterator<MegaChatListItem> itrReplace = chats.listIterator();
                while (itrReplace.hasNext()) {
                    MegaChatListItem chat = itrReplace.next();
                    if(chat!=null){
                        if(chat.getChatId()==chatHandleToUpdate){
                            indexToReplace = itrReplace.nextIndex()-1;
                            break;
                        }
                    }
                    else{
                        break;
                    }
                }
            }

            if(indexToReplace!=-1){
                log("Index to replace: "+indexToReplace);
                chats.set(indexToReplace, item);
                if(indexToReplace==0){
                    onLastTsChange(indexToReplace, false);
                }
                else{
                    onLastTsChange(indexToReplace, true);
                }
            }

        }
        else if((item.hasChanged(MegaChatListItem.CHANGE_TYPE_TITLE))){

            log("listItemUpdate: Change title: "+item.getTitle());

            if(item!=null){

                if (adapterList == null || adapterList.getItemCount()==0){
                    setChats();
                }
                else{
                    long chatHandleToUpdate = item.getChatId();
                    int indexToReplace = -1;
                    ListIterator<MegaChatListItem> itrReplace = chats.listIterator();
                    while (itrReplace.hasNext()) {
                        MegaChatListItem chat = itrReplace.next();
                        if(chat!=null){
                            if(chat.getChatId()==chatHandleToUpdate){
                                indexToReplace = itrReplace.nextIndex()-1;
                                break;
                            }
                        }
                        else{
                            break;
                        }
                    }
                    if(indexToReplace!=-1){
                        log("Index to replace: "+indexToReplace);
                        log("New title: "+item.getTitle());

                        chats.set(indexToReplace, item);
                        onTitleChange(indexToReplace);
                    }
                }
            }
        }
        else if(item.hasChanged(MegaChatListItem.CHANGE_TYPE_LAST_MSG)){

            log("listItemUpdate: Change last message: "+item.getTitle());

            if(item!=null){

                if (adapterList == null || adapterList.getItemCount()==0){
                    setChats();
                }
                else{
                    long chatHandleToUpdate = item.getChatId();
                    int indexToReplace = -1;
                    ListIterator<MegaChatListItem> itrReplace = chats.listIterator();
                    while (itrReplace.hasNext()) {
                        MegaChatListItem chat = itrReplace.next();
                        if(chat!=null){
                            if(chat.getChatId()==chatHandleToUpdate){
                                indexToReplace = itrReplace.nextIndex()-1;
                                break;
                            }
                        }
                        else{
                            break;
                        }
                    }
                    if(indexToReplace!=-1){
                        log("Index to replace: "+indexToReplace);

                        if(item.getLastMessage()!=null){
                            log("New last content: "+item.getLastMessage());
                        }
                        else{
                            log("New last content is NULL");
                        }

                        chats.set(indexToReplace, item);
                        onLastMessageChange(indexToReplace);
                    }
                }
            }
        }
        else if(item.hasChanged(MegaChatListItem.CHANGE_TYPE_CLOSED)){

            log("listItemUpdate: Change closed: MegaChatListItem.CHANGE_TYPE_CLOSED");
            log("listItemUpdate: Own privilege: "+item.getOwnPrivilege());

            if(item!=null){

                if (adapterList.getItemCount()!=0){

                    long chatHandleToRemove = item.getChatId();
                    int indexToRemove = -1;
                    ListIterator<MegaChatListItem> itrReplace = chats.listIterator();
                    while (itrReplace.hasNext()) {
                        MegaChatListItem chat = itrReplace.next();
                        if(chat!=null){
                            if(chat.getChatId()==chatHandleToRemove){
                                indexToRemove = itrReplace.nextIndex()-1;
                                break;
                            }
                        }
                        else{
                            break;
                        }
                    }
                    if(indexToRemove!=-1){
                        log("Index to replace: "+indexToRemove);
                        chats.remove(indexToRemove);

                        adapterList.removeChat(chats, indexToRemove);
                        adapterList.setPositionClicked(-1);

                        if (adapterList.getItemCount() == 0 && emptyArchivedChats()){
                            log("adapterList.getItemCount() == 0");
                            listView.setVisibility(View.GONE);
                            emptyLayout.setVisibility(View.VISIBLE);
                        }
                        else{
                            log("adapterList.getItemCount() NOT = 0");
                            listView.setVisibility(View.VISIBLE);
                            emptyLayout.setVisibility(View.GONE);
                        }
                    }
                }
            }
        }
        else if(item.hasChanged(MegaChatListItem.CHANGE_TYPE_ARCHIVE)){
            log("listItemUpdate: Change: MegaChatListItem.CHANGE_TYPE_ARCHIVE");

            if(item!=null){

                if(context instanceof ManagerActivityLollipop){
                    if(item.isArchived()){
                        log("New archived element:remove from list");
                        if (adapterList.getItemCount()!=0){

                            long chatHandleToRemove = item.getChatId();
                            int indexToRemove = -1;
                            ListIterator<MegaChatListItem> itrReplace = chats.listIterator();
                            while (itrReplace.hasNext()) {
                                MegaChatListItem chat = itrReplace.next();
                                if(chat!=null){
                                    if(chat.getChatId()==chatHandleToRemove){
                                        indexToRemove = itrReplace.nextIndex()-1;
                                        break;
                                    }
                                }
                                else{
                                    break;
                                }
                            }
                            if(indexToRemove!=-1){
                                log("Index to replace: "+indexToRemove);
                                chats.remove(indexToRemove);

                                adapterList.removeChat(chats, indexToRemove);
                                adapterList.setPositionClicked(-1);

                                if (adapterList.getItemCount() == 0 && emptyArchivedChats()){
                                    log("adapterList.getItemCount() == 0");
                                    listView.setVisibility(View.GONE);
                                    emptyLayout.setVisibility(View.VISIBLE);
                                }
                                else{
                                    log("adapterList.getItemCount() NOT = 0");
                                    listView.setVisibility(View.VISIBLE);
                                    emptyLayout.setVisibility(View.GONE);
                                }

                                if (chats.isEmpty()) {
                                    ((ManagerActivityLollipop) context).invalidateOptionsMenu();
                                }
                            }
                        }
                    }
                    else{
                        log("New unarchived element:refresh chat list");
                        setChats();
                        if (chats.size() == 1) {
                            ((ManagerActivityLollipop) context).invalidateOptionsMenu();
                        }
                    }
                    //Update last position
                    if(adapterList!=null){
                        adapterList.notifyItemChanged(chats.size()+1);
                    }

                    if(!emptyArchivedChats()){
                        listView.setPadding(0,Util.scaleHeightPx(8, outMetrics),0,Util.scaleHeightPx(16, outMetrics));
                    }
                    else{
                        listView.setPadding(0,Util.scaleHeightPx(8, outMetrics),0,Util.scaleHeightPx(78, outMetrics));
                    }

                    checkScroll();
                }
                else if(context instanceof ArchivedChatsActivity){
                    if(item.isArchived()){
                        log("New archived element:refresh chat list");
                        setChats();
                    }
                    else{
                        log("New unarchived element:remove from Archive list");
                        if (adapterList.getItemCount()!=0){

                            long chatHandleToRemove = item.getChatId();
                            int indexToRemove = -1;
                            ListIterator<MegaChatListItem> itrReplace = chats.listIterator();
                            while (itrReplace.hasNext()) {
                                MegaChatListItem chat = itrReplace.next();
                                if(chat!=null){
                                    if(chat.getChatId()==chatHandleToRemove){
                                        indexToRemove = itrReplace.nextIndex()-1;
                                        break;
                                    }
                                }
                                else{
                                    break;
                                }
                            }
                            if(indexToRemove!=-1){
                                log("Index to replace: "+indexToRemove);
                                chats.remove(indexToRemove);

                                adapterList.removeChat(chats, indexToRemove);
                                adapterList.setPositionClicked(-1);

                                if (adapterList.getItemCount() == 0){
                                    log("adapterList.getItemCount() == 0");
                                    showEmptyChatScreen();
                                    ((ArchivedChatsActivity) context).invalidateOptionsMenu();
                                }
                                else{
                                    log("adapterList.getItemCount() NOT = 0");
                                    listView.setVisibility(View.VISIBLE);
                                    emptyLayout.setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                    checkScroll();
                }
            }
        }
        else if(item.hasChanged(MegaChatListItem.CHANGE_TYPE_CALL)||item.hasChanged(MegaChatListItem.CHANGE_TYPE_CHAT_MODE)){
            log("listItemUpdate: Change: MegaChatListItem.CHANGE_TYPE_CALL or CHANGE_TYPE_CHAT_MODE");

            if (adapterList == null || adapterList.getItemCount()==0){
                setChats();
            }
            else{
                long chatHandleToUpdate = item.getChatId();
                int indexToReplace = -1;
                ListIterator<MegaChatListItem> itrReplace = chats.listIterator();
                while (itrReplace.hasNext()) {
                    MegaChatListItem chat = itrReplace.next();
                    if(chat!=null){
                        if(chat.getChatId()==chatHandleToUpdate){
                            indexToReplace = itrReplace.nextIndex()-1;
                            break;
                        }
                    }
                    else{
                        break;
                    }
                }
                if(indexToReplace!=-1){
                    log("Index to replace: "+indexToReplace);
                    chats.set(indexToReplace, item);
                    adapterList.notifyItemChanged(indexToReplace);
                }
            }
        }
        else{
            log("listItemUpdate: Other change: "+item.getChanges());

            if(item!=null){
                log("New chat: "+item.getTitle());
                setChats();
                MegaChatRoom chatToCheck = megaChatApi.getChatRoom(item.getChatId());
                updateCacheForNonContacts(chatToCheck);
            }
            else{
                log("The chat is NULL");
            }
        }
    }

    boolean emptyArchivedChats () {
        ArrayList<MegaChatListItem> archivedChats = megaChatApi.getArchivedChatListItems();

        if (archivedChats == null || archivedChats.isEmpty()) {
            return true;
        }

        return false;
    }

    public void setStatus() {
        log("setStatus");
        if(Util.isChatEnabled()) {
            chatStatus = megaChatApi.getOnlineStatus();
            log("chatStatus --> getOnlineStatus with megaChatApi: "+chatStatus);

            onlineStatusUpdate(chatStatus);
        }
    }

    public void onlineStatusUpdate(int status) {
        log("onlineStatusUpdate: "+status);

        chatStatus = status;

        if (isAdded()) {
            if(aB!=null){
                switch(status){
                    case MegaChatApi.STATUS_ONLINE:{
                        aB.setSubtitle(adjustForLargeFont(getString(R.string.online_status)));
                        break;
                    }
                    case MegaChatApi.STATUS_AWAY:{
                        aB.setSubtitle(adjustForLargeFont(getString(R.string.away_status)));
                        break;
                    }
                    case MegaChatApi.STATUS_BUSY:{
                        aB.setSubtitle(adjustForLargeFont(getString(R.string.busy_status)));
                        break;
                    }
                    case MegaChatApi.STATUS_OFFLINE:{
                        aB.setSubtitle(adjustForLargeFont(getString(R.string.offline_status)));
                        break;
                    }
                    case MegaChatApi.STATUS_INVALID:{
                        if(!Util.isOnline(context)){
                            aB.setSubtitle(adjustForLargeFont(getString(R.string.error_server_connection_problem)));
                        }
                        else {
                            if(megaChatApi == null){
                                aB.setSubtitle(adjustForLargeFont(getString(R.string.invalid_connection_state)));
                            }
                            else if(megaChatApi.getConnectionState()==MegaChatApi.CONNECTING){
                                aB.setSubtitle(adjustForLargeFont(getString(R.string.chat_connecting)));
                            }
                            else if(megaChatApi.getConnectionState()==MegaChatApi.DISCONNECTED){
                                aB.setSubtitle(adjustForLargeFont(getString(R.string.invalid_connection_state)));
                            }
                            else{
                                aB.setSubtitle(null);
                            }
                        }
                        break;
                    }
                    default:{

                        if(!Util.isOnline(context) || megaApi == null || megaApi.getRootNode()==null){
                            aB.setSubtitle(adjustForLargeFont(getString(R.string.error_server_connection_problem)));
                        }
                        else{
                            if(megaChatApi == null){
                                aB.setSubtitle(adjustForLargeFont(getString(R.string.invalid_connection_state)));
                            }
                            else if(megaChatApi.getConnectionState()==MegaChatApi.CONNECTING){
                                aB.setSubtitle(adjustForLargeFont(getString(R.string.chat_connecting)));
                            }
                            else if(megaChatApi.getConnectionState()==MegaChatApi.DISCONNECTED){
                                aB.setSubtitle(adjustForLargeFont(getString(R.string.invalid_connection_state)));
                            }
                            else{
                                int initStatus = megaChatApi.getInitState();
                                if (initStatus == MegaChatApi.INIT_WAITING_NEW_SESSION || initStatus == MegaChatApi.INIT_NO_CACHE){
                                    aB.setSubtitle(adjustForLargeFont(getString(R.string.chat_connecting)));
                                }
                                else{
                                    aB.setSubtitle(null);
                                }
                            }
                        }
                        break;
                    }
                }
            }
            else{
                log("aB is NULL");
            }
        }
        else{
            log("RecentChats not added");
        }
    }

    public void contactStatusUpdate(long userHandle, int status) {
        log("contactStatusUpdate: "+userHandle);

        long chatHandleToUpdate = -1;
        MegaChatRoom chatToUpdate = megaChatApi.getChatRoomByUser(userHandle);
        if(chatToUpdate!=null){
            chatHandleToUpdate = chatToUpdate.getChatId();
            log("Update chat: "+chatHandleToUpdate);
            if(chatHandleToUpdate !=-1){
                log("The user has a one to one chat: "+chatHandleToUpdate);

                int indexToReplace = -1;
                if(chats!=null){
                    ListIterator<MegaChatListItem> itrReplace = chats.listIterator();
                    while (itrReplace.hasNext()) {
                        MegaChatListItem chat = itrReplace.next();
                        if (chat != null) {
                            if (chat.getChatId() == chatHandleToUpdate) {
                                indexToReplace = itrReplace.nextIndex() - 1;
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                    if (indexToReplace != -1) {
                        log("Index to replace: " + indexToReplace);
                        onStatusChange(indexToReplace, userHandle, status);
                    }
                }
                else{
                    log("No chat list loaded");
                }
            }
        }
    }

    public void onStatusChange(int position, long userHandle, int status){
        log("onStatusChange: "+position+" for the user: "+userHandle+" with new presence: "+status);

        adapterList.updateContactStatus(position, userHandle, status);
    }

    public void onTitleChange(int position){
        log("onTitleChange");

        adapterList.setTitle(position, null);

        interactionUpdate(position);
    }

    public void onUnreadCountChange(int position, boolean updateOrder){
        log("onUnreadCountChange");

        adapterList.setPendingMessages(position, null);

        if(updateOrder){
            interactionUpdate(position);
        }
    }

    public void onLastTsChange(int position, boolean updateOrder){
        log("onLastTsChange");

        adapterList.setTs(position, null);

        if(updateOrder){
            interactionUpdate(position);
        }
    }

    public void onLastMessageChange(int position){
        log("onLastMessageChange");

        adapterList.setLastMessage(position, null);

//        if(updateOrder){
//            interactionUpdate(position);
//        }
    }

    public void showMuteIcon(MegaChatListItem item){
        log("showMuteIcon");

        long chatHandleToUpdate = item.getChatId();
        int indexToReplace = -1;
        ListIterator<MegaChatListItem> itrReplace = chats.listIterator();
        while (itrReplace.hasNext()) {
            MegaChatListItem chat = itrReplace.next();
            if(chat!=null){
                if(chat.getChatId()==chatHandleToUpdate){
                    indexToReplace = itrReplace.nextIndex()-1;
                    break;
                }
            }
            else{
                break;
            }
        }
        if(indexToReplace!=-1){
            log("Index to replace: "+indexToReplace);
            if(adapterList!=null){
                adapterList.showMuteIcon(indexToReplace);
            }
        }
    }

    public void refreshNode(MegaChatListItem item){
        log("refreshNode -> showCallLayout");
        //call in progress layout:
        showCallLayout();

        //elements of adapter
        long chatHandleToUpdate = item.getChatId();
        int indexToUpdate = -1;
        if(chats!=null){
            ListIterator<MegaChatListItem> itrReplace = chats.listIterator();
            while (itrReplace.hasNext()) {
                MegaChatListItem chat = itrReplace.next();
                if(chat!=null){
                    if(chat.getChatId() == chatHandleToUpdate){
                        indexToUpdate = itrReplace.nextIndex()-1;
                        break;
                    }
                }
                else{
                    break;
                }
            }
            if(indexToUpdate!=-1){
                log("Index to replace: "+indexToUpdate);
                if(adapterList!=null){
                    adapterList.notifyItemChanged(indexToUpdate);
                }
            }
        }
    }

    public void interactionUpdate(int position){
        log("interactionUpdate");
        MegaChatListItem chat = chats.remove(position);
        chats.add(0, chat);
        adapterList.notifyItemMoved(position, 0);
        if(lastFirstVisiblePosition==position){
            log("Interaction - change lastFirstVisiblePosition");
            lastFirstVisiblePosition=0;
        }

        if(adapterList.isMultipleSelect()){
            adapterList.updateMultiselectionPosition(position);
        }
    }

    public String getParticipantFullName(MegaChatRoom chat, long i){
        String participantFirstName = chat.getPeerFirstname(i);
        String participantLastName = chat.getPeerLastname(i);

        if(participantFirstName==null){
            participantFirstName="";
        }
        if(participantLastName == null){
            participantLastName="";
        }

        if (participantFirstName.trim().length() <= 0){
            log("Participant1: "+participantFirstName);
            return participantLastName;
        }
        else{
            log("Participant2: "+participantLastName);
            return participantFirstName + " " + participantLastName;
        }
    }

    public void updateCacheForNonContacts(MegaChatRoom chatToCheck){
        if(chatToCheck!=null){
            long peers = chatToCheck.getPeerCount();
            for(int i=0; i<peers; i++){
//                    long peerHandle = chatToCheck.getPeerHandle(i);
                String fullName = getParticipantFullName(chatToCheck, i);
                if(fullName!=null){
                    if(fullName.trim().length()<=0){
                        log("Ask for name!");
                        ChatNonContactNameListener listener = new ChatNonContactNameListener(context);
                        megaChatApi.getUserFirstname(chatToCheck.getPeerHandle(i), chatToCheck.getAuthorizationToken(), listener);
                        megaChatApi.getUserLastname(chatToCheck.getPeerHandle(i), chatToCheck.getAuthorizationToken(), listener);
                    }
                    else{
                        log("Exists name!");
                    }
                }
                else{
                    log("Ask for name!");
                    ChatNonContactNameListener listener = new ChatNonContactNameListener(context);
                    megaChatApi.getUserFirstname(chatToCheck.getPeerHandle(i), chatToCheck.getAuthorizationToken(), listener);
                    megaChatApi.getUserLastname(chatToCheck.getPeerHandle(i), chatToCheck.getAuthorizationToken(), listener);
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        log("onSaveInstanceState");
        super.onSaveInstanceState(outState);
        if(listView.getLayoutManager()!=null){
            outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, listView.getLayoutManager().onSaveInstanceState());
        }
    }

    @Override
    public void onPause() {
        log("onPause");
        lastFirstVisiblePosition = ((LinearLayoutManager)listView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        MegaApplication.setRecentChatVisible(false);
        super.onPause();
    }

    @Override
    public void onResume() {
        log("onResume: lastFirstVisiblePosition " +lastFirstVisiblePosition);
        if(lastFirstVisiblePosition>0){
            (listView.getLayoutManager()).scrollToPosition(lastFirstVisiblePosition);
        }else{
            (listView.getLayoutManager()).scrollToPosition(0);
        }
        lastFirstVisiblePosition=0;
    
        if(aB != null && aB.getTitle() != null){
            aB.setTitle(adjustForLargeFont(aB.getTitle().toString()));
        }


        showCallLayout();

        if (context instanceof ManagerActivityLollipop) {
            String searchQuery = ((ManagerActivityLollipop) context).searchQuery;
            if (searchQuery != null && ((ManagerActivityLollipop) context).searchExpand) {
                filterChats(searchQuery);
            }
            ((ManagerActivityLollipop) context).invalidateOptionsMenu();
        }

        super.onResume();
    }

    public int getItemCount(){
        if(adapterList != null){
            return adapterList.getItemCount();
        }
        return 0;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        log("onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
            listView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }

    public void visibilityFastScroller(){
        if(chats == null){
           fastScroller.setVisibility(View.GONE);
        }else{
           if(chats.size() < Constants.MIN_ITEMS_SCROLLBAR_CHAT) {
                fastScroller.setVisibility(View.GONE);
           }else{
               fastScroller.setVisibility(View.VISIBLE);
           }
        }
    }

    public void filterChats (String s) {
        if (adapterList != null && adapterList.isMultipleSelect()) {
            hideMultipleSelect();
        }

        if (filterChatsTask != null && filterChatsTask.getStatus() != AsyncTask.Status.FINISHED) {
            filterChatsTask.cancel(true);
        }
        filterChatsTask = new FilterChatsTask();
        filterChatsTask.execute(s);
    }

    class FilterChatsTask extends AsyncTask<String, Void, Void> {

        ArrayList<MegaChatListItem> filteredChats;

        @Override
        protected Void doInBackground(String... strings) {
            if (chats != null && !chats.isEmpty()) {
                if (filteredChats == null) {
                    filteredChats = new ArrayList<>();
                } else {
                    filteredChats.clear();
                }
                for (MegaChatListItem chat : chats) {
                    if (chat.getTitle().toLowerCase().contains(strings[0].toLowerCase())) {
                        filteredChats.add(chat);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (adapterList == null) {
                return;
            }
            adapterList.setChats(filteredChats);

            if (adapterList.getItemCount() == 0){
                log("adapterList.getItemCount() == 0");
                listView.setVisibility(View.GONE);
                emptyLayout.setVisibility(View.VISIBLE);
                inviteButton.setVisibility(View.GONE);
                String textToShow = String.format(context.getString(R.string.recent_chat_empty));

                try{
                    textToShow = textToShow.replace("[A]", "<font color=\'#000000\'>");
                    textToShow = textToShow.replace("[/A]", "</font>");
                    textToShow = textToShow.replace("[B]", "<font color=\'#7a7a7a\'>");
                    textToShow = textToShow.replace("[/B]", "</font>");
                }
                catch (Exception e){}
                Spanned result = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    result = Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY);
                } else {
                    result = Html.fromHtml(textToShow);
                }

                emptyTextViewInvite.setText(result);
                emptyTextViewInvite.setVisibility(View.VISIBLE);
            }
            else{
                log("adapterList.getItemCount() NOT = 0");
                listView.setVisibility(View.VISIBLE);
                emptyLayout.setVisibility(View.GONE);
            }
        }
    }

    public void closeSearch() {
        if (filterChatsTask != null && filterChatsTask.getStatus() != AsyncTask.Status.FINISHED) {
            filterChatsTask.cancel(true);
        }

        if (adapterList == null) {
            return;
        }

        adapterList.setChats(chats);

        if (adapterList.getItemCount() == 0 && emptyArchivedChats()) {
            log("adapterList.getItemCount() == 0");
            listView.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
        } else {
            log("adapterList.getItemCount() NOT = 0");
            listView.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.GONE);

        }
    }

    public boolean checkPermissionsCall(){
        log("checkPermissionsCall() ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            boolean hasCameraPermission = (ContextCompat.checkSelfPermission(((ManagerActivityLollipop) context), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
            if (!hasCameraPermission) {
                ActivityCompat.requestPermissions(((ManagerActivityLollipop) context), new String[]{Manifest.permission.CAMERA}, Constants.REQUEST_CAMERA);
                return false;
            }

            boolean hasRecordAudioPermission = (ContextCompat.checkSelfPermission(((ManagerActivityLollipop) context), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
            if (!hasRecordAudioPermission) {
                ActivityCompat.requestPermissions(((ManagerActivityLollipop) context), new String[]{Manifest.permission.RECORD_AUDIO}, Constants.RECORD_AUDIO);
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
                    if(checkPermissionsCall()){
                        log("REQUEST_CAMERA -> returnTheCall");
                        ChatUtil.returnCall(context, megaChatApi);
                    }
                }
                break;
            }
            case Constants.RECORD_AUDIO: {
                log("RECORD_AUDIO");
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(checkPermissionsCall()){
                        log("RECORD_AUDIO -> returnTheCall");
                        ChatUtil.returnCall(context, megaChatApi);
                    }
                }
                break;
            }

        }
    }


    private static void log(String log) {
        Util.log("RecentChatsFragmentLollipop", log);
    }
}