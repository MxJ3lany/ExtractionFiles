/*
 * Copyright (C) 2007-2008 Esmertec AG. Copyright (C) 2007-2008 The Android Open
 * Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.awesomeapp.messenger.service.adapters;

import org.awesomeapp.messenger.plugin.xmpp.XmppAddress;
import org.awesomeapp.messenger.service.IChatSessionManager;
import org.awesomeapp.messenger.service.IConnectionListener;
import org.awesomeapp.messenger.service.IContactListManager;
import org.awesomeapp.messenger.service.IInvitationListener;
import org.awesomeapp.messenger.ImApp;
import org.awesomeapp.messenger.model.ChatGroupManager;
import org.awesomeapp.messenger.model.ConnectionListener;
import org.awesomeapp.messenger.model.Contact;
import org.awesomeapp.messenger.model.ImConnection;
import org.awesomeapp.messenger.model.ImErrorInfo;
import org.awesomeapp.messenger.model.ImException;
import org.awesomeapp.messenger.model.Invitation;
import org.awesomeapp.messenger.model.InvitationListener;
import org.awesomeapp.messenger.model.Presence;
import org.awesomeapp.messenger.provider.Imps;
import org.awesomeapp.messenger.service.RemoteImService;

import org.awesomeapp.messenger.tasks.ChatSessionInitTask;
import org.awesomeapp.messenger.util.Debug;
import org.jivesoftware.smackx.httpfileupload.UploadProgressListener;

import java.io.InputStream;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

public class ImConnectionAdapter extends org.awesomeapp.messenger.service.IImConnection.Stub {

    private static final String[] SESSION_COOKIE_PROJECTION = { Imps.SessionCookies.NAME,
                                                               Imps.SessionCookies.VALUE, };

    private static final int COLUMN_SESSION_COOKIE_NAME = 0;
    private static final int COLUMN_SESSION_COOKIE_VALUE = 1;

    ImConnection mConnection;
    private ConnectionListenerAdapter mConnectionListener;
    private InvitationListenerAdapter mInvitationListener;

    final RemoteCallbackList<IConnectionListener> mRemoteConnListeners;

    ChatSessionManagerAdapter mChatSessionManager;
    ContactListManagerAdapter mContactListManager;

    ChatGroupManager mGroupManager;
    RemoteImService mService;

    long mProviderId = -1;
    long mAccountId = -1;
    boolean mAutoLoadContacts;
    int mConnectionState = ImConnection.DISCONNECTED;

    public ImConnectionAdapter(long providerId, long accountId, ImConnection connection, RemoteImService service) {
        mProviderId = providerId;
        mAccountId = accountId;
        mConnection = connection;
        mService = service;
        mConnectionListener = new ConnectionListenerAdapter();
        mConnection.addConnectionListener(mConnectionListener);
        if ((connection.getCapability() & ImConnection.CAPABILITY_GROUP_CHAT) != 0) {
            mGroupManager = mConnection.getChatGroupManager();
            mInvitationListener = new InvitationListenerAdapter();
            mGroupManager.setInvitationListener(mInvitationListener);
        }

        mChatSessionManager = new ChatSessionManagerAdapter(this);
        mContactListManager = new ContactListManagerAdapter(this);
        mRemoteConnListeners = new RemoteCallbackList<>();

    }

    public ImConnection getAdaptee() {
        return mConnection;
    }

    public RemoteImService getContext() {
        return mService;
    }

    @Override
    public long getProviderId() {
        return mProviderId;
    }

    @Override
    public long getAccountId() {
        return mAccountId;
    }

    @Override
    public boolean isUsingTor() {
        return mConnection.isUsingTor();
    }

    @Override
    public int[] getSupportedPresenceStatus() {
        return mConnection.getSupportedPresenceStatus();
    }

    public void networkTypeChanged() {
        mConnection.networkTypeChanged();
    }

    public boolean reestablishSession() {
        mConnectionState = ImConnection.LOGGING_IN;

        ContentResolver cr = mService.getContentResolver();
        if ((mConnection.getCapability() & ImConnection.CAPABILITY_SESSION_REESTABLISHMENT) != 0) {
            Map<String, String> cookie = querySessionCookie(cr);
            if (cookie != null) {
                RemoteImService.debug("re-establish session");
                try {
                    mConnection.reestablishSessionAsync(cookie);
                    return true;
                } catch (IllegalArgumentException e) {
                    RemoteImService.debug("Invalid session cookie, probably modified by others.");
                    clearSessionCookie(cr);
                }
            }
        }

        return false;
    }

    private Uri getSessionCookiesUri() {
        Uri.Builder builder = Imps.SessionCookies.CONTENT_URI_SESSION_COOKIES_BY.buildUpon();
        ContentUris.appendId(builder, mProviderId);
        ContentUris.appendId(builder, mAccountId);

        return builder.build();
    }

    @Override
    public void login(final String passwordTemp, final boolean autoLoadContacts, final boolean retry) {
        Debug.wrapExceptions(new Runnable() {
            @Override
            public void run() {
                do_login(passwordTemp, autoLoadContacts, retry);
            }
        });
    }

    public void do_login(String passwordTemp, boolean autoLoadContacts, boolean retry) {

        mAutoLoadContacts = autoLoadContacts;
        mConnectionState = ImConnection.LOGGING_IN;

        mConnection.loginAsync(mAccountId, passwordTemp, mProviderId, retry);


    }

   
    
    
    private void loadSavedPresence ()
    {
        ContentResolver cr =  mService.getContentResolver();
        // Imps.ProviderSettings.setPresence(cr, mProviderId, status, statusText);
         int presenceState = Imps.ProviderSettings.getIntValue(cr, mProviderId, Imps.ProviderSettings.PRESENCE_STATE);
         String presenceStatusMessage = Imps.ProviderSettings.getStringValue(cr, mProviderId, Imps.ProviderSettings.PRESENCE_STATUS_MESSAGE);

         if (presenceState != -1)
         {
             Presence presence = new Presence();
             presence.setStatus(presenceState);
             presence.setStatusText(presenceStatusMessage);
             try {
                 mConnection.updateUserPresenceAsync(presence);
             } catch (ImException e) {
                 Log.e(ImApp.LOG_TAG,"unable able to update presence",e);
             }
         }
    }

    @Override
    public void sendHeartbeat() throws RemoteException {

        if (mConnection != null)
            mConnection.sendHeartbeat(mService.getHeartbeatInterval());

    }

    @Override
    public void setProxy(String type, String host, int port) throws RemoteException {
        mConnection.setProxy(type, host, port);
    }

    private HashMap<String, String> querySessionCookie(ContentResolver cr) {
        Cursor c = cr.query(getSessionCookiesUri(), SESSION_COOKIE_PROJECTION, null, null, null);
        if (c == null) {
            return null;
        }

        HashMap<String, String> cookie = null;
        if (c.getCount() > 0) {
            cookie = new HashMap<String, String>();
            while (c.moveToNext()) {
                cookie.put(c.getString(COLUMN_SESSION_COOKIE_NAME),
                        c.getString(COLUMN_SESSION_COOKIE_VALUE));
            }
        }

        c.close();
        return cookie;
    }

    public void clearMemory ()
    {
        mChatSessionManager.closeAllChatSessions();
    }

    @Override
    public void logout() {
       // OtrChatManager.endSessionsForAccount(mConnection.getLoginUser());
        mConnectionState = ImConnection.LOGGING_OUT;
        mConnection.logout();
    }

    @Override
    public void cancelLogin() {
        if (mConnectionState >= ImConnection.LOGGED_IN) {
            // too late
            return;
        }
        mConnectionState = ImConnection.LOGGING_OUT;
        mConnection.logout();
    }

    public void suspend() {
        mConnectionState = ImConnection.SUSPENDING;
        mConnection.suspend();
    }

    @Override
    public void registerConnectionListener(IConnectionListener listener) {
        if (listener != null) {
            mRemoteConnListeners.register(listener);
        }
    }

    @Override
    public void unregisterConnectionListener(IConnectionListener listener) {
        if (listener != null) {
            mRemoteConnListeners.unregister(listener);
        }
    }

    @Override
    public void setInvitationListener(IInvitationListener listener) {
        if (mInvitationListener != null) {
            mInvitationListener.mRemoteListener = listener;
        }
    }



    @Override
    public IChatSessionManager getChatSessionManager() {
        return mChatSessionManager;
    }

    @Override
    public IContactListManager getContactListManager() {
        return mContactListManager;
    }

    @Override
    public int getChatSessionCount() {
        if (mChatSessionManager == null) {
            return 0;
        }
        return mChatSessionManager.getChatSessionCount();
    }

    public Contact getLoginUser() {
        return mConnection.getLoginUser();
    }

    @Override
    public Presence getUserPresence() {
        return mConnection.getUserPresence();
    }

    @Override
    public int updateUserPresence(Presence newPresence) {
        try {


            mConnection.updateUserPresenceAsync(newPresence);


        } catch (ImException e) {
            return e.getImError().getCode();
        }

        return ImErrorInfo.NO_ERROR;
    }

    @Override
    public int getState() {
        return mConnectionState;
    }

    @Override
    public void rejectInvitation(long id) {
        handleInvitation(id, false);
    }

    @Override
    public void acceptInvitation(long id) {
        handleInvitation(id, true);
    }

    private void handleInvitation(long id, boolean accept) {
        if (mGroupManager == null) {
            return;
        }
        ContentResolver cr = mService.getContentResolver();
        Cursor c = cr.query(ContentUris.withAppendedId(Imps.Invitation.CONTENT_URI, id), null,
                null, null, null);
        if (c == null) {
            return;
        }
        if (c.moveToFirst()) {
            String inviteId = c.getString(c.getColumnIndexOrThrow(Imps.Invitation.INVITE_ID));
            int status;
            if (accept) {
                mGroupManager.acceptInvitationAsync(inviteId);
                status = Imps.Invitation.STATUS_ACCEPTED;
            } else {
                mGroupManager.rejectInvitationAsync(inviteId);
                status = Imps.Invitation.STATUS_REJECTED;
            }
            // TODO c.updateInt(c.getColumnIndexOrThrow(Imps.Invitation.STATUS), status);
            // c.commitUpdates();
        }
        c.close();
    }

    void saveSessionCookie(ContentResolver cr) {
        Map<String, String> cookies = mConnection.getSessionContext();

        int i = 0;
        ContentValues[] valuesList = new ContentValues[cookies.size()];

        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            ContentValues values = new ContentValues(2);

            values.put(Imps.SessionCookies.NAME, entry.getKey());
            values.put(Imps.SessionCookies.VALUE, entry.getValue());

            valuesList[i++] = values;
        }

        cr.bulkInsert(getSessionCookiesUri(), valuesList);
    }

    void clearSessionCookie(ContentResolver cr) {
        cr.delete(getSessionCookiesUri(), null, null);
    }

    void updateAccountStatusInDb() {
        Presence p = getUserPresence();
        int presenceStatus = Imps.Presence.OFFLINE;
        int connectionStatus = convertConnStateForDb(mConnectionState);

        if (p != null) {
            presenceStatus = ContactListManagerAdapter.convertPresenceStatus(p);
        }

        ContentResolver cr = mService.getContentResolver();
        Uri uri = Imps.AccountStatus.CONTENT_URI;
        ContentValues values = new ContentValues();

        values.put(Imps.AccountStatus.ACCOUNT, mAccountId);
        values.put(Imps.AccountStatus.PRESENCE_STATUS, presenceStatus);
        values.put(Imps.AccountStatus.CONNECTION_STATUS, connectionStatus);

        cr.insert(uri, values);
    }

    private static int convertConnStateForDb(int state) {
        switch (state) {
        case ImConnection.DISCONNECTED:
        case ImConnection.LOGGING_OUT:
            return Imps.ConnectionStatus.OFFLINE;

        case ImConnection.LOGGING_IN:
            return Imps.ConnectionStatus.CONNECTING;

        case ImConnection.LOGGED_IN:
            return Imps.ConnectionStatus.ONLINE;

        case ImConnection.SUSPENDED:
        case ImConnection.SUSPENDING:
            return Imps.ConnectionStatus.SUSPENDED;

        default:
            return Imps.ConnectionStatus.OFFLINE;
        }
    }

    final class ConnectionListenerAdapter implements ConnectionListener {
        @Override
        public void onStateChanged(final int state, final ImErrorInfo error) {

            synchronized (this) {

                if (state != ImConnection.DISCONNECTED) {
                    mConnectionState = state;
                }
                if (state == ImConnection.LOGGED_IN && mConnectionState == ImConnection.LOGGING_OUT) {

                    // A bit tricky here. The engine did login successfully
                    // but the notification comes a bit late; user has already
                    // issued a cancelLogin() and that cannot be undone. Here
                    // we have to ignore the LOGGED_IN event and wait for
                    // the upcoming DISCONNECTED.
                    return;
                }

                updateAccountStatusInDb();

                synchronized (mRemoteConnListeners) {

                    final int N = mRemoteConnListeners.beginBroadcast();
                    for (int i = 0; i < N; i++) {
                        IConnectionListener listener = mRemoteConnListeners.getBroadcastItem(i);
                        try {
                            listener.onStateChanged(ImConnectionAdapter.this, state, error);
                        } catch (RemoteException e) {
                            // The RemoteCallbackList will take care of removing the
                            // dead listeners.
                        }
                    }

                    mRemoteConnListeners.finishBroadcast();
                }


                if (state == ImConnection.LOGGED_IN)
                {
                    //we need to reinit all group chat sessions here
                    Cursor c = null;

                    try {
                        Uri baseUri = Imps.Contacts.CONTENT_URI_CHAT_CONTACTS_BY;
                        String[] CHAT_PROJECTION = { Imps.Contacts._ID, Imps.Contacts.PROVIDER,
                                Imps.Contacts.ACCOUNT, Imps.Contacts.USERNAME,
                                Imps.Contacts.NICKNAME, Imps.Contacts.TYPE,
                        };

                        Uri.Builder builder = baseUri.buildUpon();
                        builder.appendQueryParameter(Imps.Contacts.PROVIDER,mProviderId+"");
                        builder.appendQueryParameter(Imps.Contacts.ACCOUNT,mAccountId+"");

                        StringBuffer buf = new StringBuffer();
                        buf.append("(" + Imps.Chats.CHAT_TYPE + " IS NULL")
                                .append(" OR " + Imps.Chats.CHAT_TYPE + '=' + Imps.Chats.CHAT_TYPE_MUTED)
                                .append(" OR " + Imps.Chats.CHAT_TYPE + '=' + Imps.Chats.CHAT_TYPE_ACTIVE + ")");

                        Uri uriChats = builder.build();
                        c = getContext().getContentResolver().query(uriChats, CHAT_PROJECTION, buf.toString(), null, Imps.Contacts.TIME_ORDER);

                        if (c != null) {
                            if (c.getCount() > 0) {
                                while (c.moveToNext()) {
                                    int chatType = c.getInt(5);
                                    String remoteAddress = c.getString(3);
                                    String nickname = c.getString(4);
                                    if (remoteAddress != null)
                                       new ChatSessionInitTask((ImApp) getContext().getApplication(), mProviderId, mAccountId, chatType, false)
                                               .execute(new Contact(new XmppAddress(remoteAddress),nickname,chatType));
                                }
                            }
                            c.close();
                        }

                    }
                    catch (Exception e)
                    {
                        Log.e(ImApp.LOG_TAG,"exception init chatsession",e);
                        if (c != null && (!c.isClosed()))
                            c.close();
                    }
                }

            }

            ContentResolver cr = mService.getContentResolver();
            if (state == ImConnection.LOGGED_IN) {
                if ((mConnection.getCapability() & ImConnection.CAPABILITY_SESSION_REESTABLISHMENT) != 0) {
                    saveSessionCookie(cr);
                }

                if (mAutoLoadContacts)
                {
                    mContactListManager.loadContactLists();
                }

                try {
                    Collection<ChatSessionAdapter> adapters = mChatSessionManager.mActiveChatSessionAdapters.values();

                    synchronized (adapters) {
                        for (ChatSessionAdapter session : adapters) {
                            session.sendPostponedMessages();
                        }
                    }
                }
                catch (ConcurrentModificationException cme)
                {
                    Log.w(ImApp.LOG_TAG,"concurrent mod exception on login",cme);
                }

                loadSavedPresence();
                

            } else if (state == ImConnection.DISCONNECTED) {
                clearSessionCookie(cr);
                // mContactListManager might still be null if we fail
                // immediately in loginAsync (say, an invalid host URL)
                if (mContactListManager != null) { // n8fr8 2015-01-21 Why are we clearing this?
                  mContactListManager.clearOnLogout();
                }

                mConnectionState = state;
            } else if (state == ImConnection.SUSPENDED && error != null) {

                // re-establish failed, schedule to retry
                mService.scheduleReconnect(5000);

            }



            if (state == ImConnection.DISCONNECTED) {
                // NOTE: if this logic is changed, the logic in ImApp.MyConnListener must be changed to match
                mService.removeConnection(ImConnectionAdapter.this);

            }
        }

        @Override
        public void onUserPresenceUpdated() {
            updateAccountStatusInDb();

            final int N = mRemoteConnListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                IConnectionListener listener = mRemoteConnListeners.getBroadcastItem(i);
                try {
                    listener.onUserPresenceUpdated(ImConnectionAdapter.this);
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing the
                    // dead listeners.
                }
            }
            mRemoteConnListeners.finishBroadcast();
        }

        @Override
        public void onUpdatePresenceError(final ImErrorInfo error) {
            final int N = mRemoteConnListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                IConnectionListener listener = mRemoteConnListeners.getBroadcastItem(i);
                try {
                    listener.onUpdatePresenceError(ImConnectionAdapter.this, error);
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing the
                    // dead listeners.
                }
            }
            mRemoteConnListeners.finishBroadcast();
        }
    }

    final class InvitationListenerAdapter implements InvitationListener {
        IInvitationListener mRemoteListener;

        @Override
        public void onGroupInvitation(Invitation invitation) {
            String sender = invitation.getSender().getUser();
            ContentValues values = new ContentValues(7);
            values.put(Imps.Invitation.PROVIDER, mProviderId);
            values.put(Imps.Invitation.ACCOUNT, mAccountId);
            values.put(Imps.Invitation.INVITE_ID, invitation.getInviteID());
            values.put(Imps.Invitation.SENDER, sender);
            values.put(Imps.Invitation.GROUP_NAME, invitation.getGroupAddress().getUser());
            values.put(Imps.Invitation.NOTE, invitation.getReason());
            values.put(Imps.Invitation.STATUS, Imps.Invitation.STATUS_PENDING);
            ContentResolver resolver = mService.getContentResolver();
            Uri uri = resolver.insert(Imps.Invitation.CONTENT_URI, values);
            long id = ContentUris.parseId(uri);
            try {
                if (mRemoteListener != null) {
                    mRemoteListener.onGroupInvitation(id);
                    return;
                }
            } catch (RemoteException e) {
                RemoteImService.debug("onGroupInvitation: dead listener " + mRemoteListener
                                      + "; removing", e);
                mRemoteListener = null;
            }
            // No listener registered or failed to notify the listener, send a
            // notification instead.
            mService.getStatusBarNotifier().notifyGroupInvitation(mProviderId, mAccountId, id,
                    sender);
        }
    }

    public void changeNickname (String nickname)
    {
        if (mConnection != null)
            mConnection.changeNickname(nickname);
    }

    public void sendTypingStatus (String to, boolean isTyping)
    {
        if (mConnection != null)
            mConnection.sendTypingStatus(to,isTyping);

    }

    public void broadcastMigrationIdentity(String address)
    {
        if (mConnection != null)
            mConnection.broadcastMigrationIdentity(address);

    }

    public List getFingerprints (String address)
    {
        if (mConnection != null)
            return mConnection.getFingerprints(address);
        else
            return null;
    }

    public String publishFile (String fileName, String mimeType, long fileSize, InputStream is, boolean doEncryption, UploadProgressListener listener)
    {
        return mConnection.publishFile(fileName, mimeType, fileSize, is, doEncryption, listener);
    }

}
