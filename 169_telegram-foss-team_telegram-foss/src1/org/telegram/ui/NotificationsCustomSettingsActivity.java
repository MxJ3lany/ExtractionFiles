/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextColorCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NotificationsCustomSettingsActivity extends BaseFragment {

    private RecyclerListView listView;
    private ListAdapter adapter;
    private EmptyTextProgressView emptyView;
    private SearchAdapter searchListViewAdapter;
    private AnimatorSet animatorSet;

    private boolean searchWas;
    private boolean searching;

    private final static int search_button = 0;

    private int alertRow;
    private int alertSection2Row;
    private int messageSectionRow;
    private int previewRow;
    private int messageVibrateRow;
    private int messageSoundRow;
    private int messageLedRow;
    private int messagePopupNotificationRow;
    private int messagePriorityRow;
    private int groupSection2Row;
    private int exceptionsAddRow;
    private int exceptionsStartRow;
    private int exceptionsEndRow;
    private int exceptionsSection2Row;
    private int rowCount = 0;

    private int currentType;
    private ArrayList<NotificationsSettingsActivity.NotificationException> exceptions;

    public NotificationsCustomSettingsActivity(int type, ArrayList<NotificationsSettingsActivity.NotificationException> notificationExceptions) {
        this(type, notificationExceptions, false);
    }

    public NotificationsCustomSettingsActivity(int type, ArrayList<NotificationsSettingsActivity.NotificationException> notificationExceptions, boolean load) {
        super();
        currentType = type;
        exceptions = notificationExceptions;
        if (load) {
            loadExceptions();
        }
    }

    @Override
    public boolean onFragmentCreate() {
        updateRows();
        return super.onFragmentCreate();
    }

    @Override
    public View createView(Context context) {
        searching = false;
        searchWas = false;

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        if (currentType == -1) {
            actionBar.setTitle(LocaleController.getString("NotificationsExceptions", R.string.NotificationsExceptions));
        } else {
            actionBar.setTitle(LocaleController.getString("Notifications", R.string.Notifications));
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });
        if (exceptions != null && !exceptions.isEmpty()) {
            ActionBarMenu menu = actionBar.createMenu();
            ActionBarMenuItem searchItem = menu.addItem(search_button, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {
                @Override
                public void onSearchExpand() {
                    searching = true;
                    emptyView.setShowAtCenter(true);
                }

                @Override
                public void onSearchCollapse() {
                    searchListViewAdapter.searchDialogs(null);
                    searching = false;
                    searchWas = false;
                    emptyView.setText(LocaleController.getString("NoExceptions", R.string.NoExceptions));
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    listView.setFastScrollVisible(true);
                    listView.setVerticalScrollBarEnabled(false);
                    emptyView.setShowAtCenter(false);
                }

                @Override
                public void onTextChanged(EditText editText) {
                    if (searchListViewAdapter == null) {
                        return;
                    }
                    String text = editText.getText().toString();
                    if (text.length() != 0) {
                        searchWas = true;
                        if (listView != null) {
                            emptyView.setText(LocaleController.getString("NoResult", R.string.NoResult));
                            listView.setAdapter(searchListViewAdapter);
                            searchListViewAdapter.notifyDataSetChanged();
                            listView.setFastScrollVisible(false);
                            listView.setVerticalScrollBarEnabled(true);
                        }
                    }
                    searchListViewAdapter.searchDialogs(text);
                }
            });
            searchItem.setSearchFieldHint(LocaleController.getString("Search", R.string.Search));
        }

        searchListViewAdapter = new SearchAdapter(context);

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        emptyView = new EmptyTextProgressView(context);
        emptyView.setTextSize(18);
        emptyView.setText(LocaleController.getString("NoExceptions", R.string.NoExceptions));
        emptyView.showTextView();
        frameLayout.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView = new RecyclerListView(context);
        listView.setEmptyView(emptyView);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setAdapter(adapter = new ListAdapter(context));
        listView.setOnItemClickListener((view, position, x, y) -> {
            boolean enabled = false;
            if (getParentActivity() == null) {
                return;
            }
            if (listView.getAdapter() == searchListViewAdapter || position >= exceptionsStartRow && position < exceptionsEndRow) {
                ArrayList<NotificationsSettingsActivity.NotificationException> arrayList;
                int index = position;
                if (listView.getAdapter() == searchListViewAdapter) {
                    arrayList = searchListViewAdapter.searchResult;
                } else {
                    arrayList = exceptions;
                    index -= exceptionsStartRow;
                }
                if (index < 0 || index >= arrayList.size()) {
                    return;
                }
                NotificationsSettingsActivity.NotificationException exception = arrayList.get(index);
                AlertsCreator.showCustomNotificationsDialog(NotificationsCustomSettingsActivity.this, exception.did, -1, null, currentAccount, null, param -> {
                    if (param == 0) {
                        if (arrayList != exceptions) {
                            int idx = exceptions.indexOf(exception);
                            if (idx >= 0) {
                                exceptions.remove(idx);
                            }
                        }
                        arrayList.remove(exception);
                        if (exceptionsAddRow != -1 && arrayList.isEmpty() && arrayList == exceptions) {
                            listView.getAdapter().notifyItemChanged(exceptionsAddRow);
                        }
                        listView.getAdapter().notifyItemRemoved(position);

                        updateRows();
                        checkRowsEnabled();
                    } else {
                        SharedPreferences preferences = MessagesController.getNotificationsSettings(currentAccount);
                        exception.hasCustom = preferences.getBoolean("custom_" + exception.did, false);
                        exception.notify = preferences.getInt("notify2_" + exception.did, 0);
                        if (exception.notify != 0) {
                            int time = preferences.getInt("notifyuntil_" + exception.did, -1);
                            if (time != -1) {
                                exception.muteUntil = time;
                            }
                        }
                        listView.getAdapter().notifyItemChanged(position);
                    }
                });
                return;
            }
            if (position == exceptionsAddRow) {
                Bundle args = new Bundle();
                args.putBoolean("onlySelect", true);
                args.putBoolean("checkCanWrite", false);
                if (currentType == NotificationsController.TYPE_GROUP) {
                    args.putInt("dialogsType", 6);
                } else if (currentType == NotificationsController.TYPE_CHANNEL) {
                    args.putInt("dialogsType", 5);
                } else {
                    args.putInt("dialogsType", 4);
                }
                DialogsActivity activity = new DialogsActivity(args);
                activity.setDelegate((fragment, dids, message, param) -> {
                    Bundle args2 = new Bundle();
                    args2.putLong("dialog_id", dids.get(0));
                    args2.putBoolean("exception", true);
                    ProfileNotificationsActivity profileNotificationsActivity = new ProfileNotificationsActivity(args2);
                    profileNotificationsActivity.setDelegate(exception -> {
                        exceptions.add(0, exception);
                        updateRows();
                        adapter.notifyDataSetChanged();
                    });
                    presentFragment(profileNotificationsActivity, true);
                });
                presentFragment(activity);
            } else if (position == alertRow) {
                enabled = NotificationsController.getInstance(currentAccount).isGlobalNotificationsEnabled(currentType);

                NotificationsCheckCell checkCell = (NotificationsCheckCell) view;
                RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(position);
                if (!enabled) {
                    NotificationsController.getInstance(currentAccount).setGlobalNotificationsEnabled(currentType, 0);
                    checkCell.setChecked(!enabled);
                    if (holder != null) {
                        adapter.onBindViewHolder(holder, position);
                    }
                    checkRowsEnabled();
                } else {
                    AlertsCreator.showCustomNotificationsDialog(NotificationsCustomSettingsActivity.this, 0, currentType, exceptions, currentAccount, param -> {
                        int offUntil;
                        SharedPreferences preferences = MessagesController.getNotificationsSettings(currentAccount);
                        if (currentType == NotificationsController.TYPE_PRIVATE) {
                            offUntil = preferences.getInt("EnableAll2", 0);
                        } else if (currentType == NotificationsController.TYPE_GROUP) {
                            offUntil = preferences.getInt("EnableGroup2", 0);
                        } else {
                            offUntil = preferences.getInt("EnableChannel2", 0);
                        }
                        int currentTime = ConnectionsManager.getInstance(currentAccount).getCurrentTime();
                        int iconType;
                        if (offUntil < currentTime) {
                            iconType = 0;
                        } else if (offUntil - 60 * 60 * 24 * 365 >= currentTime) {
                            iconType = 0;
                        } else {
                            iconType = 2;
                        }
                        checkCell.setChecked(NotificationsController.getInstance(currentAccount).isGlobalNotificationsEnabled(currentType), iconType);
                        if (holder != null) {
                            adapter.onBindViewHolder(holder, position);
                        }
                        checkRowsEnabled();
                    });
                }
            } else if (position == previewRow) {
                if (!view.isEnabled()) {
                    return;
                }
                SharedPreferences preferences = MessagesController.getNotificationsSettings(currentAccount);
                SharedPreferences.Editor editor = preferences.edit();
                if (currentType == NotificationsController.TYPE_PRIVATE) {
                    enabled = preferences.getBoolean("EnablePreviewAll", true);
                    editor.putBoolean("EnablePreviewAll", !enabled);
                } else if (currentType == NotificationsController.TYPE_GROUP) {
                    enabled = preferences.getBoolean("EnablePreviewGroup", true);
                    editor.putBoolean("EnablePreviewGroup", !enabled);
                } else {
                    enabled = preferences.getBoolean("EnablePreviewChannel", true);
                    editor.putBoolean("EnablePreviewChannel", !enabled);
                }
                editor.commit();
                NotificationsController.getInstance(currentAccount).updateServerNotificationsSettings(currentType);
            } else if (position == messageSoundRow) {
                if (!view.isEnabled()) {
                    return;
                }
                try {
                    SharedPreferences preferences = MessagesController.getNotificationsSettings(currentAccount);
                    Intent tmpIntent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                    tmpIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                    tmpIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                    tmpIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                    tmpIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                    Uri currentSound = null;

                    String defaultPath = null;
                    Uri defaultUri = Settings.System.DEFAULT_NOTIFICATION_URI;
                    if (defaultUri != null) {
                        defaultPath = defaultUri.getPath();
                    }

                    String path;
                    if (currentType == NotificationsController.TYPE_PRIVATE) {
                        path = preferences.getString("GlobalSoundPath", defaultPath);
                    } else if (currentType == NotificationsController.TYPE_GROUP) {
                        path = preferences.getString("GroupSoundPath", defaultPath);
                    } else {
                        path = preferences.getString("ChannelSoundPath", defaultPath);
                    }

                    if (path != null && !path.equals("NoSound")) {
                        if (path.equals(defaultPath)) {
                            currentSound = defaultUri;
                        } else {
                            currentSound = Uri.parse(path);
                        }
                    }

                    tmpIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentSound);
                    startActivityForResult(tmpIntent, position);
                } catch (Exception e) {
                    FileLog.e(e);
                }
            } else if (position == messageLedRow) {
                if (!view.isEnabled()) {
                    return;
                }
                showDialog(AlertsCreator.createColorSelectDialog(getParentActivity(), 0, currentType, () -> {
                    RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(position);
                    if (holder != null) {
                        adapter.onBindViewHolder(holder, position);
                    }
                }));
            } else if (position == messagePopupNotificationRow) {
                if (!view.isEnabled()) {
                    return;
                }
                showDialog(AlertsCreator.createPopupSelectDialog(getParentActivity(), currentType, () -> {
                    RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(position);
                    if (holder != null) {
                        adapter.onBindViewHolder(holder, position);
                    }
                }));
            } else if (position == messageVibrateRow) {
                if (!view.isEnabled()) {
                    return;
                }
                String key;
                if (currentType == NotificationsController.TYPE_PRIVATE) {
                    key = "vibrate_messages";
                } else if (currentType == NotificationsController.TYPE_GROUP) {
                    key = "vibrate_group";
                } else {
                    key = "vibrate_channel";
                }
                showDialog(AlertsCreator.createVibrationSelectDialog(getParentActivity(), 0, key, () -> {
                    RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(position);
                    if (holder != null) {
                        adapter.onBindViewHolder(holder, position);
                    }
                }));
            } else if (position == messagePriorityRow) {
                if (!view.isEnabled()) {
                    return;
                }
                showDialog(AlertsCreator.createPrioritySelectDialog(getParentActivity(), 0, currentType, () -> {
                    RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(position);
                    if (holder != null) {
                        adapter.onBindViewHolder(holder, position);
                    }
                }));
            }
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(!enabled);
            }
        });

        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING && searching && searchWas) {
                    AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        return fragmentView;
    }

    private void checkRowsEnabled() {
        if (!exceptions.isEmpty()) {
            return;
        }
        int count = listView.getChildCount();
        ArrayList<Animator> animators = new ArrayList<>();
        boolean enabled = NotificationsController.getInstance(currentAccount).isGlobalNotificationsEnabled(currentType);
        for (int a = 0; a < count; a++) {
            View child = listView.getChildAt(a);
            RecyclerListView.Holder holder = (RecyclerListView.Holder) listView.getChildViewHolder(child);
            switch (holder.getItemViewType()) {
                case 0: {
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (holder.getAdapterPosition() == messageSectionRow) {
                        headerCell.setEnabled(enabled, animators);
                    }
                    break;
                }
                case 1: {
                    TextCheckCell textCell = (TextCheckCell) holder.itemView;
                    textCell.setEnabled(enabled, animators);
                    break;
                }
                case 3: {
                    TextColorCell textCell = (TextColorCell) holder.itemView;
                    textCell.setEnabled(enabled, animators);
                    break;
                }
                case 5: {
                    TextSettingsCell textCell = (TextSettingsCell) holder.itemView;
                    textCell.setEnabled(enabled, animators);
                    break;
                }
            }
        }
        if (!animators.isEmpty()) {
            if (animatorSet != null) {
                animatorSet.cancel();
            }
            animatorSet = new AnimatorSet();
            animatorSet.playTogether(animators);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    if (animator.equals(animatorSet)) {
                        animatorSet = null;
                    }
                }
            });
            animatorSet.setDuration(150);
            animatorSet.start();
        }
    }

    private void loadExceptions() {
        MessagesStorage.getInstance(currentAccount).getStorageQueue().postRunnable(() -> {
            ArrayList<NotificationsSettingsActivity.NotificationException> usersResult = new ArrayList<>();
            ArrayList<NotificationsSettingsActivity.NotificationException> chatsResult = new ArrayList<>();
            ArrayList<NotificationsSettingsActivity.NotificationException> channelsResult = new ArrayList<>();
            LongSparseArray<NotificationsSettingsActivity.NotificationException> waitingForLoadExceptions = new LongSparseArray<>();

            ArrayList<Integer> usersToLoad = new ArrayList<>();
            ArrayList<Integer> chatsToLoad = new ArrayList<>();
            ArrayList<Integer> encryptedChatsToLoad = new ArrayList<>();

            ArrayList<TLRPC.User> users = new ArrayList<>();
            ArrayList<TLRPC.Chat> chats = new ArrayList<>();
            ArrayList<TLRPC.EncryptedChat> encryptedChats = new ArrayList<>();
            int selfId = UserConfig.getInstance(currentAccount).clientUserId;

            SharedPreferences preferences = MessagesController.getNotificationsSettings(currentAccount);
            Map<String, ?> values = preferences.getAll();
            for (Map.Entry<String, ?> entry : values.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("notify2_")) {
                    key = key.replace("notify2_", "");

                    long did = Utilities.parseLong(key);
                    if (did != 0 && did != selfId) {
                        NotificationsSettingsActivity.NotificationException exception = new NotificationsSettingsActivity.NotificationException();
                        exception.did = did;
                        exception.hasCustom = preferences.getBoolean("custom_" + did, false);
                        exception.notify = (Integer) entry.getValue();
                        if (exception.notify != 0) {
                            Integer time = (Integer) values.get("notifyuntil_" + key);
                            if (time != null) {
                                exception.muteUntil = time;
                            }
                        }

                        int lower_id = (int) did;
                        int high_id = (int) (did << 32);
                        if (lower_id != 0) {
                            if (lower_id > 0) {
                                TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(lower_id);
                                if (user == null) {
                                    usersToLoad.add(lower_id);
                                    waitingForLoadExceptions.put(did, exception);
                                } else if (user.deleted) {
                                    continue;
                                }
                                usersResult.add(exception);
                            } else {
                                TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-lower_id);
                                if (chat == null) {
                                    chatsToLoad.add(-lower_id);
                                    waitingForLoadExceptions.put(did, exception);
                                    continue;
                                } else if (chat.left || chat.kicked || chat.migrated_to != null) {
                                    continue;
                                }
                                if (ChatObject.isChannel(chat) && !chat.megagroup) {
                                    channelsResult.add(exception);
                                } else {
                                    chatsResult.add(exception);
                                }
                            }
                        } else if (high_id != 0) {
                            TLRPC.EncryptedChat encryptedChat = MessagesController.getInstance(currentAccount).getEncryptedChat(high_id);
                            if (encryptedChat == null) {
                                encryptedChatsToLoad.add(high_id);
                                waitingForLoadExceptions.put(did, exception);
                            } else {
                                TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(encryptedChat.user_id);
                                if (user == null) {
                                    usersToLoad.add(encryptedChat.user_id);
                                    waitingForLoadExceptions.put(encryptedChat.user_id, exception);
                                } else if (user.deleted) {
                                    continue;
                                }
                            }
                            usersResult.add(exception);
                        }
                    }
                }
            }
            if (waitingForLoadExceptions.size() != 0) {
                try {
                    if (!encryptedChatsToLoad.isEmpty()) {
                        MessagesStorage.getInstance(currentAccount).getEncryptedChatsInternal(TextUtils.join(",", encryptedChatsToLoad), encryptedChats, usersToLoad);
                    }
                    if (!usersToLoad.isEmpty()) {
                        MessagesStorage.getInstance(currentAccount).getUsersInternal(TextUtils.join(",", usersToLoad), users);
                    }
                    if (!chatsToLoad.isEmpty()) {
                        MessagesStorage.getInstance(currentAccount).getChatsInternal(TextUtils.join(",", chatsToLoad), chats);
                    }
                } catch (Exception e) {
                    FileLog.e(e);
                }
                for (int a = 0, size = chats.size(); a < size; a++) {
                    TLRPC.Chat chat = chats.get(a);
                    if (chat.left || chat.kicked || chat.migrated_to != null) {
                        continue;
                    }
                    NotificationsSettingsActivity.NotificationException exception = waitingForLoadExceptions.get(-chat.id);
                    waitingForLoadExceptions.remove(-chat.id);

                    if (exception != null) {
                        if (ChatObject.isChannel(chat) && !chat.megagroup) {
                            channelsResult.add(exception);
                        } else {
                            chatsResult.add(exception);
                        }
                    }
                }
                for (int a = 0, size = users.size(); a < size; a++) {
                    TLRPC.User user = users.get(a);
                    if (user.deleted) {
                        continue;
                    }
                    waitingForLoadExceptions.remove(user.id);
                }
                for (int a = 0, size = encryptedChats.size(); a < size; a++) {
                    TLRPC.EncryptedChat encryptedChat = encryptedChats.get(a);
                    waitingForLoadExceptions.remove(((long) encryptedChat.id) << 32);
                }
                for (int a = 0, size = waitingForLoadExceptions.size(); a < size; a++) {
                    long did = waitingForLoadExceptions.keyAt(a);
                    if ((int) did < 0) {
                        chatsResult.remove(waitingForLoadExceptions.valueAt(a));
                        channelsResult.remove(waitingForLoadExceptions.valueAt(a));
                    } else {
                        usersResult.remove(waitingForLoadExceptions.valueAt(a));
                    }
                }
            }
            AndroidUtilities.runOnUIThread(() -> {
                MessagesController.getInstance(currentAccount).putUsers(users, true);
                MessagesController.getInstance(currentAccount).putChats(chats, true);
                MessagesController.getInstance(currentAccount).putEncryptedChats(encryptedChats, true);
                if (currentType == NotificationsController.TYPE_PRIVATE) {
                    exceptions = usersResult;
                } else if (currentType == NotificationsController.TYPE_GROUP) {
                    exceptions = chatsResult;
                } else {
                    exceptions = channelsResult;
                }
                updateRows();
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void updateRows() {
        rowCount = 0;
        if (currentType != -1) {
            alertRow = rowCount++;
            alertSection2Row = rowCount++;
            messageSectionRow = rowCount++;
            previewRow = rowCount++;
            messageLedRow = rowCount++;
            messageVibrateRow = rowCount++;
            if (currentType == NotificationsController.TYPE_CHANNEL) {
                messagePopupNotificationRow = -1;
            } else {
                messagePopupNotificationRow = rowCount++;
            }
            messageSoundRow = rowCount++;
            if (Build.VERSION.SDK_INT >= 21) {
                messagePriorityRow = rowCount++;
            } else {
                messagePriorityRow = -1;
            }
            groupSection2Row = rowCount++;
            exceptionsAddRow = rowCount++;
        } else {
            alertRow = -1;
            alertSection2Row = -1;
            messageSectionRow = -1;
            previewRow = -1;
            messageLedRow = -1;
            messageVibrateRow = -1;
            messagePopupNotificationRow = -1;
            messageSoundRow = -1;
            messagePriorityRow = -1;
            groupSection2Row = -1;
            exceptionsAddRow = -1;
        }
        if (exceptions != null && !exceptions.isEmpty()) {
            exceptionsStartRow = rowCount;
            rowCount += exceptions.size();
            exceptionsEndRow = rowCount;
        } else {
            exceptionsStartRow = -1;
            exceptionsEndRow = -1;
        }
        if (currentType != -1 || exceptions != null && !exceptions.isEmpty()) {
            exceptionsSection2Row = rowCount++;
        } else {
            exceptionsSection2Row = -1;
        }
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri ringtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            String name = null;
            if (ringtone != null) {
                Ringtone rng = RingtoneManager.getRingtone(getParentActivity(), ringtone);
                if (rng != null) {
                    if (ringtone.equals(Settings.System.DEFAULT_NOTIFICATION_URI)) {
                        name = LocaleController.getString("SoundDefault", R.string.SoundDefault);
                    } else {
                        name = rng.getTitle(getParentActivity());
                    }
                    rng.stop();
                }
            }

            SharedPreferences preferences = MessagesController.getNotificationsSettings(currentAccount);
            SharedPreferences.Editor editor = preferences.edit();

            if (currentType == NotificationsController.TYPE_PRIVATE) {
                if (name != null && ringtone != null) {
                    editor.putString("GlobalSound", name);
                    editor.putString("GlobalSoundPath", ringtone.toString());
                } else {
                    editor.putString("GlobalSound", "NoSound");
                    editor.putString("GlobalSoundPath", "NoSound");
                }
            } else if (currentType == NotificationsController.TYPE_GROUP) {
                if (name != null && ringtone != null) {
                    editor.putString("GroupSound", name);
                    editor.putString("GroupSoundPath", ringtone.toString());
                } else {
                    editor.putString("GroupSound", "NoSound");
                    editor.putString("GroupSoundPath", "NoSound");
                }
            } else if (currentType == NotificationsController.TYPE_CHANNEL) {
                if (name != null && ringtone != null) {
                    editor.putString("ChannelSound", name);
                    editor.putString("ChannelSoundPath", ringtone.toString());
                } else {
                    editor.putString("ChannelSound", "NoSound");
                    editor.putString("ChannelSoundPath", "NoSound");
                }
            }
            editor.commit();
            NotificationsController.getInstance(currentAccount).updateServerNotificationsSettings(currentType);
            RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(requestCode);
            if (holder != null) {
                adapter.onBindViewHolder(holder, requestCode);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private class SearchAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;
        private ArrayList<NotificationsSettingsActivity.NotificationException> searchResult = new ArrayList<>();
        private ArrayList<CharSequence> searchResultNames = new ArrayList<>();
        private Timer searchTimer;

        public SearchAdapter(Context context) {
            mContext = context;
        }

        public void searchDialogs(final String query) {
            try {
                if (searchTimer != null) {
                    searchTimer.cancel();
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
            if (query == null) {
                searchResult.clear();
                searchResultNames.clear();
                notifyDataSetChanged();
            } else {
                searchTimer = new Timer();
                searchTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            searchTimer.cancel();
                            searchTimer = null;
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                        processSearch(query);
                    }
                }, 200, 300);
            }
        }

        private void processSearch(final String query) {
            AndroidUtilities.runOnUIThread(() -> {
                final ArrayList<NotificationsSettingsActivity.NotificationException> contactsCopy = new ArrayList<>(exceptions);
                Utilities.searchQueue.postRunnable(() -> {
                    String search1 = query.trim().toLowerCase();
                    if (search1.length() == 0) {
                        updateSearchResults(new ArrayList<>(), new ArrayList<>());
                        return;
                    }
                    String search2 = LocaleController.getInstance().getTranslitString(search1);
                    if (search1.equals(search2) || search2.length() == 0) {
                        search2 = null;
                    }
                    String[] search = new String[1 + (search2 != null ? 1 : 0)];
                    search[0] = search1;
                    if (search2 != null) {
                        search[1] = search2;
                    }

                    ArrayList<NotificationsSettingsActivity.NotificationException> resultArray = new ArrayList<>();
                    ArrayList<CharSequence> resultArrayNames = new ArrayList<>();

                    String[] names = new String[2];
                    for (int a = 0; a < contactsCopy.size(); a++) {
                        NotificationsSettingsActivity.NotificationException exception = contactsCopy.get(a);

                        int lower_id = (int) exception.did;
                        int high_id = (int) (exception.did >> 32);

                        if (lower_id != 0) {
                            if (lower_id > 0) {
                                TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(lower_id);
                                if (user.deleted) {
                                    continue;
                                }
                                if (user != null) {
                                    names[0] = ContactsController.formatName(user.first_name, user.last_name);
                                    names[1] = user.username;
                                }
                            } else {
                                TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-lower_id);
                                if (chat != null) {
                                    if (chat.left || chat.kicked || chat.migrated_to != null) {
                                        continue;
                                    }
                                    names[0] = chat.title;
                                    names[1] = chat.username;
                                }
                            }
                        } else {
                            TLRPC.EncryptedChat encryptedChat = MessagesController.getInstance(currentAccount).getEncryptedChat(high_id);
                            if (encryptedChat != null) {
                                TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(encryptedChat.user_id);
                                if (user != null) {
                                    names[0] = ContactsController.formatName(user.first_name, user.last_name);
                                    names[1] = user.username;
                                }
                            }
                        }

                        String originalName = names[0];
                        names[0] = names[0].toLowerCase();
                        String tName = LocaleController.getInstance().getTranslitString(names[0]);
                        if (names[0] != null && names[0].equals(tName)) {
                            tName = null;
                        }

                        int found = 0;
                        for (int b = 0; b < search.length; b++) {
                            String q = search[b];
                            if (names[0] != null && (names[0].startsWith(q) || names[0].contains(" " + q)) || tName != null && (tName.startsWith(q) || tName.contains(" " + q))) {
                                found = 1;
                            } else if (names[1] != null && names[1].startsWith(q)) {
                                found = 2;
                            }

                            if (found != 0) {
                                if (found == 1) {
                                    resultArrayNames.add(AndroidUtilities.generateSearchName(originalName, null, q));
                                } else {
                                    resultArrayNames.add(AndroidUtilities.generateSearchName("@" + names[1], null, "@" + q));
                                }
                                resultArray.add(exception);
                                break;
                            }
                        }
                    }
                    updateSearchResults(resultArray, resultArrayNames);
                });
            });
        }

        private void updateSearchResults(final ArrayList<NotificationsSettingsActivity.NotificationException> users, final ArrayList<CharSequence> names) {
            AndroidUtilities.runOnUIThread(() -> {
                searchResult = users;
                searchResultNames = names;
                notifyDataSetChanged();
            });
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }

        @Override
        public int getItemCount() {
            return searchResult.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = new UserCell(mContext, 9, 0, false);
            view.setPadding(AndroidUtilities.dp(6), 0, AndroidUtilities.dp(6), 0);
            view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            UserCell cell = (UserCell) holder.itemView;
            cell.setException(searchResult.get(position), searchResultNames.get(position), position != searchResult.size() - 1);
        }

        @Override
        public int getItemViewType(int i) {
            return 0;
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type != 0 && type != 4;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 1:
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 2:
                    view = new UserCell(mContext, 6, 0, false);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 3:
                    view = new TextColorCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 4:
                    view = new ShadowSectionCell(mContext);
                    break;
                case 5:
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 6:
                    view = new NotificationsCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 7:
                default:
                    view = new TextCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0: {
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == messageSectionRow) {
                        headerCell.setText(LocaleController.getString("SETTINGS", R.string.SETTINGS));
                    }
                    break;
                }
                case 1: {
                    TextCheckCell checkCell = (TextCheckCell) holder.itemView;
                    SharedPreferences preferences = MessagesController.getNotificationsSettings(currentAccount);
                    if (position == previewRow) {
                        boolean enabled;
                        if (currentType == NotificationsController.TYPE_PRIVATE) {
                            enabled = preferences.getBoolean("EnablePreviewAll", true);
                        } else if (currentType == NotificationsController.TYPE_GROUP) {
                            enabled = preferences.getBoolean("EnablePreviewGroup", true);
                        } else {
                            enabled = preferences.getBoolean("EnablePreviewChannel", true);
                        }
                        checkCell.setTextAndCheck(LocaleController.getString("MessagePreview", R.string.MessagePreview), enabled, true);
                    }
                    break;
                }
                case 2: {
                    UserCell cell = (UserCell) holder.itemView;
                    NotificationsSettingsActivity.NotificationException exception = exceptions.get(position - exceptionsStartRow);
                    cell.setException(exception, null, position != exceptionsEndRow - 1);
                    break;
                }
                case 3: {
                    TextColorCell textColorCell = (TextColorCell) holder.itemView;
                    SharedPreferences preferences = MessagesController.getNotificationsSettings(currentAccount);
                    int color;
                    if (currentType == NotificationsController.TYPE_PRIVATE) {
                        color = preferences.getInt("MessagesLed", 0xff0000ff);
                    } else if (currentType == NotificationsController.TYPE_GROUP) {
                        color = preferences.getInt("GroupLed", 0xff0000ff);
                    } else {
                        color = preferences.getInt("ChannelLed", 0xff0000ff);
                    }
                    for (int a = 0; a < 9; a++) {
                        if (TextColorCell.colorsToSave[a] == color) {
                            color = TextColorCell.colors[a];
                            break;
                        }
                    }
                    textColorCell.setTextAndColor(LocaleController.getString("LedColor", R.string.LedColor), color, true);
                    break;
                }
                case 4: {
                    if (position == exceptionsSection2Row || position == groupSection2Row && exceptionsSection2Row == -1) {
                        holder.itemView.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                    } else {
                        holder.itemView.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    }
                    break;
                }
                case 5: {
                    TextSettingsCell textCell = (TextSettingsCell) holder.itemView;
                    SharedPreferences preferences = MessagesController.getNotificationsSettings(currentAccount);
                    if (position == messageSoundRow) {
                        String value;
                        if (currentType == NotificationsController.TYPE_PRIVATE) {
                            value = preferences.getString("GlobalSound", LocaleController.getString("SoundDefault", R.string.SoundDefault));
                        } else if (currentType == NotificationsController.TYPE_GROUP) {
                            value = preferences.getString("GroupSound", LocaleController.getString("SoundDefault", R.string.SoundDefault));
                        } else {
                            value = preferences.getString("ChannelSound", LocaleController.getString("SoundDefault", R.string.SoundDefault));
                        }
                        if (value.equals("NoSound")) {
                            value = LocaleController.getString("NoSound", R.string.NoSound);
                        }
                        textCell.setTextAndValue(LocaleController.getString("Sound", R.string.Sound), value, true);
                    } else if (position == messageVibrateRow) {
                        int value;
                        if (currentType == NotificationsController.TYPE_PRIVATE) {
                            value = preferences.getInt("vibrate_messages", 0);
                        } else if (currentType == NotificationsController.TYPE_GROUP) {
                            value = preferences.getInt("vibrate_group", 0);
                        } else {
                            value = preferences.getInt("vibrate_channel", 0);
                        }
                        if (value == 0) {
                            textCell.setTextAndValue(LocaleController.getString("Vibrate", R.string.Vibrate), LocaleController.getString("VibrationDefault", R.string.VibrationDefault), true);
                        } else if (value == 1) {
                            textCell.setTextAndValue(LocaleController.getString("Vibrate", R.string.Vibrate), LocaleController.getString("Short", R.string.Short), true);
                        } else if (value == 2) {
                            textCell.setTextAndValue(LocaleController.getString("Vibrate", R.string.Vibrate), LocaleController.getString("VibrationDisabled", R.string.VibrationDisabled), true);
                        } else if (value == 3) {
                            textCell.setTextAndValue(LocaleController.getString("Vibrate", R.string.Vibrate), LocaleController.getString("Long", R.string.Long), true);
                        } else if (value == 4) {
                            textCell.setTextAndValue(LocaleController.getString("Vibrate", R.string.Vibrate), LocaleController.getString("OnlyIfSilent", R.string.OnlyIfSilent), true);
                        }
                    } else if (position == messagePriorityRow) {
                        int value;
                        if (currentType == NotificationsController.TYPE_PRIVATE) {
                            value = preferences.getInt("priority_messages", 1);
                        } else if (currentType == NotificationsController.TYPE_GROUP) {
                            value = preferences.getInt("priority_group", 1);
                        } else {
                            value = preferences.getInt("priority_channel", 1);
                        }
                        if (value == 0) {
                            textCell.setTextAndValue(LocaleController.getString("NotificationsImportance", R.string.NotificationsImportance), LocaleController.getString("NotificationsPriorityHigh", R.string.NotificationsPriorityHigh), true);
                        } else if (value == 1 || value == 2) {
                            textCell.setTextAndValue(LocaleController.getString("NotificationsImportance", R.string.NotificationsImportance), LocaleController.getString("NotificationsPriorityUrgent", R.string.NotificationsPriorityUrgent), true);
                        } else if (value == 4) {
                            textCell.setTextAndValue(LocaleController.getString("NotificationsImportance", R.string.NotificationsImportance), LocaleController.getString("NotificationsPriorityLow", R.string.NotificationsPriorityLow), true);
                        } else if (value == 5) {
                            textCell.setTextAndValue(LocaleController.getString("NotificationsImportance", R.string.NotificationsImportance), LocaleController.getString("NotificationsPriorityMedium", R.string.NotificationsPriorityMedium), true);
                        }
                    } else if (position == messagePopupNotificationRow) {
                        int option;
                        if (currentType == NotificationsController.TYPE_PRIVATE) {
                            option = preferences.getInt("popupAll", 0);
                        } else if (currentType == NotificationsController.TYPE_GROUP) {
                            option = preferences.getInt("popupGroup", 0);
                        } else {
                            option = preferences.getInt("popupChannel", 0);
                        }
                        String value;
                        if (option == 0) {
                            value = LocaleController.getString("NoPopup", R.string.NoPopup);
                        } else if (option == 1) {
                            value = LocaleController.getString("OnlyWhenScreenOn", R.string.OnlyWhenScreenOn);
                        } else if (option == 2) {
                            value = LocaleController.getString("OnlyWhenScreenOff", R.string.OnlyWhenScreenOff);
                        } else {
                            value = LocaleController.getString("AlwaysShowPopup", R.string.AlwaysShowPopup);
                        }
                        textCell.setTextAndValue(LocaleController.getString("PopupNotification", R.string.PopupNotification), value, true);
                    }
                    break;
                }
                case 6: {
                    NotificationsCheckCell checkCell = (NotificationsCheckCell) holder.itemView;
                    checkCell.setDrawLine(false);
                    String text;
                    StringBuilder builder = new StringBuilder();
                    int offUntil;
                    SharedPreferences preferences = MessagesController.getNotificationsSettings(currentAccount);

                    if (currentType == NotificationsController.TYPE_PRIVATE) {
                        text = LocaleController.getString("NotificationsForPrivateChats", R.string.NotificationsForPrivateChats);
                        offUntil = preferences.getInt("EnableAll2", 0);
                    } else if (currentType == NotificationsController.TYPE_GROUP) {
                        text = LocaleController.getString("NotificationsForGroups", R.string.NotificationsForGroups);
                        offUntil = preferences.getInt("EnableGroup2", 0);
                    } else {
                        text = LocaleController.getString("NotificationsForChannels", R.string.NotificationsForChannels);
                        offUntil = preferences.getInt("EnableChannel2", 0);
                    }
                    int currentTime = ConnectionsManager.getInstance(currentAccount).getCurrentTime();
                    boolean enabled;
                    int iconType;
                    if (enabled = offUntil < currentTime) {
                        builder.append(LocaleController.getString("NotificationsOn", R.string.NotificationsOn));
                        iconType = 0;
                    } else if (offUntil - 60 * 60 * 24 * 365 >= currentTime) {
                        builder.append(LocaleController.getString("NotificationsOff", R.string.NotificationsOff));
                        iconType = 0;
                    } else {
                        builder.append(LocaleController.formatString("NotificationsOffUntil", R.string.NotificationsOffUntil, LocaleController.stringForMessageListDate(offUntil)));
                        iconType = 2;
                    }
                    checkCell.setTextAndValueAndCheck(text, builder, enabled, iconType, false);
                    break;
                }
                case 7: {
                    TextCell textCell = (TextCell) holder.itemView;
                    textCell.setColors(Theme.key_windowBackgroundWhiteBlueIcon, Theme.key_windowBackgroundWhiteBlueButton);
                    if (position == exceptionsAddRow) {
                        textCell.setTextAndIcon(LocaleController.getString("NotificationsAddAnException", R.string.NotificationsAddAnException), R.drawable.actions_addmember2, exceptionsStartRow != -1);
                    }
                    break;
                }
            }
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            if (exceptions == null || !exceptions.isEmpty()) {
                return;
            }
            boolean enabled = NotificationsController.getInstance(currentAccount).isGlobalNotificationsEnabled(currentType);
            switch (holder.getItemViewType()) {
                case 0: {
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (holder.getAdapterPosition() == messageSectionRow) {
                        headerCell.setEnabled(enabled, null);
                    } else {
                        headerCell.setEnabled(true, null);
                    }
                    break;
                }
                case 1: {
                    TextCheckCell textCell = (TextCheckCell) holder.itemView;
                    textCell.setEnabled(enabled, null);
                    break;
                }
                case 3: {
                    TextColorCell textCell = (TextColorCell) holder.itemView;
                    textCell.setEnabled(enabled, null);
                    break;
                }
                case 5: {
                    TextSettingsCell textCell = (TextSettingsCell) holder.itemView;
                    textCell.setEnabled(enabled, null);
                    break;
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == messageSectionRow) {
                return 0;
            } else if (position == previewRow) {
                return 1;
            } else if (position >= exceptionsStartRow && position < exceptionsEndRow) {
                return 2;
            } else if (position == messageLedRow) {
                return 3;
            } else if (position == groupSection2Row || position == alertSection2Row || position == exceptionsSection2Row) {
                return 4;
            } else if (position == alertRow) {
                return 6;
            } else if (position == exceptionsAddRow) {
                return 7;
            } else {
                return 5;
            }
        }
    }

    @Override
    public ThemeDescription[] getThemeDescriptions() {
        ThemeDescription.ThemeDescriptionDelegate cellDelegate = () -> {
            if (listView != null) {
                int count = listView.getChildCount();
                for (int a = 0; a < count; a++) {
                    View child = listView.getChildAt(a);
                    if (child instanceof UserCell) {
                        ((UserCell) child).update(0);
                    }
                }
            }
        };

        return new ThemeDescription[]{
                new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{HeaderCell.class, TextCheckCell.class, TextColorCell.class, TextSettingsCell.class, UserCell.class, NotificationsCheckCell.class}, null, null, null, Theme.key_windowBackgroundWhite),
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray),

                new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault),
                new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector),

                new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector),

                new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider),

                new ThemeDescription(listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader),

                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2),
                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack),
                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked),

                new ThemeDescription(listView, 0, new Class[]{UserCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon),
                new ThemeDescription(listView, 0, new Class[]{UserCell.class}, new String[]{"nameTextView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, 0, new Class[]{UserCell.class}, new String[]{"statusColor"}, null, null, cellDelegate, Theme.key_windowBackgroundWhiteGrayText),
                new ThemeDescription(listView, 0, new Class[]{UserCell.class}, new String[]{"statusOnlineColor"}, null, null, cellDelegate, Theme.key_windowBackgroundWhiteBlueText),
                new ThemeDescription(listView, 0, new Class[]{UserCell.class}, null, new Drawable[]{Theme.avatar_broadcastDrawable, Theme.avatar_savedDrawable}, null, Theme.key_avatar_text),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundRed),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundOrange),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundViolet),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundGreen),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundCyan),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundBlue),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundPink),

                new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2),
                new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack),
                new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked),

                new ThemeDescription(listView, 0, new Class[]{TextColorCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),

                new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText),

                new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow),

                new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueButton),
                new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{TextCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueIcon),
        };
    }
}
