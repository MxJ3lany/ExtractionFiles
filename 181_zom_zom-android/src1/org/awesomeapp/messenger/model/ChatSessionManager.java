/*
 * Copyright (C) 2007 Esmertec AG. Copyright (C) 2007 The Android Open Source
 * Project
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

package org.awesomeapp.messenger.model;

import org.awesomeapp.messenger.service.adapters.ChatSessionAdapter;
import org.awesomeapp.messenger.service.adapters.ChatSessionManagerAdapter;
import org.jxmpp.jid.FullJid;
import org.jxmpp.jid.Jid;

import java.util.Hashtable;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The ChatSessionManager keeps track of all current chat sessions and is also
 * responsible to dispatch the new incoming message to the right session.
 */
public abstract class ChatSessionManager {

    private CopyOnWriteArrayList<ChatSessionListener> mListeners;
    private ChatSessionManagerAdapter mAdapter;

    /** Map session to the participant communicate with. */
    protected Hashtable<String,ChatSession> mSessions;

    protected ChatSessionManager() {
        mListeners = new CopyOnWriteArrayList<ChatSessionListener>();
        mSessions = new Hashtable<String,ChatSession>();
    }

    public void setAdapter (ChatSessionManagerAdapter adapter)
    {
        mAdapter = adapter;
    }

    public ChatSessionManagerAdapter getAdapter ()
    {
        return mAdapter;
    }

    /**
     * Registers a ChatSessionListener with the ChatSessionManager to receive
     * events related to ChatSession.
     *
     * @param listener the listener
     */
    public void addChatSessionListener(ChatSessionListener listener) {
        if ((listener != null) && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    /**
     * Removes a ChatSessionListener so that it will no longer be notified.
     *
     * @param listener the listener to remove.
     */
    public void removeChatSessionListener(ChatSessionListener listener) {
        mListeners.remove(listener);
    }

    /**
     * Creates a new ChatSession with specified participant.
     *
     * @param participant the participant.
     * @return the created ChatSession.
     */
    public ChatSession createChatSession(ImEntity participant, boolean isNewSession) {

        ChatSession session = mSessions.get(participant.getAddress().getBareAddress());

        if (session == null)
        {
            session = new ChatSession(participant, this);
            ChatSessionAdapter csa = mAdapter.getChatSessionAdapter(session, isNewSession);

            //this is redundant, as the getAdapter() returns the session instance itself
            session.setMessageListener(csa.getAdaptee().getMessageListener());

            mSessions.put(participant.getAddress().getBareAddress(),session);

            for (ChatSessionListener listener : mListeners) {
                listener.onChatSessionCreated(session);
            }

        }
        else
        {
            ChatSessionAdapter csa = mAdapter.getChatSessionAdapter(session, isNewSession);

            //this is redundant, as the getAdapter() returns the session instance itself
            session.setMessageListener(csa.getAdaptee().getMessageListener());
            
        }

        return session;
    }

    /**
     * Closes a ChatSession. This only removes the session from the list; the
     * protocol implementation should override this if it has special work to
     * do.
     *
     * @param session the ChatSession to close.
     */
    public void closeChatSession(ChatSession session) {
        mSessions.remove(session.getParticipant().getAddress().getAddress());
    }

    /**
     * Sends a message to specified participant(s) asynchronously. TODO: more
     * docs on async callbacks.
     *
     * @param message the message to send.
     */
    public abstract void sendMessageAsync(ChatSession session, Message message);

    /**
     * The resource of this JID s
     * @param jid
     */
    public abstract boolean resourceSupportsOmemo (Jid jid);
}
