package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomWarnings;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DaoMessage {

    // About 'dummy': "When the min() or max() aggregate functions are used in an aggregate query,
    // all bare columns in the result set take values from the input row which also contains the minimum or maximum."
    // https://www.sqlite.org/lang_select.html

    String folder_in = "folder.type IN ('" + EntityFolder.ARCHIVE + "', '" + EntityFolder.OUTBOX + "', '" + EntityFolder.DRAFTS + "')";

    String unseen_unified = "SUM(CASE WHEN message.ui_seen OR " + folder_in + " THEN 0 ELSE 1 END)";
    String unflagged_unified = "SUM(CASE WHEN message.ui_flagged AND NOT " + folder_in + " THEN 0 ELSE 1 END)";

    @Query("SELECT message.*" +
            ", account.name AS accountName, IFNULL(identity.color, account.color) AS accountColor, account.notify AS accountNotify" +
            ", folder.name AS folderName, folder.display AS folderDisplay, folder.type AS folderType" +
            ", identity.name AS identityName, identity.email AS identityEmail" +
            ", COUNT(message.id) AS count" +
            ", " + unseen_unified + " AS unseen" +
            ", " + unflagged_unified + " AS unflagged" +
            ", (SELECT COUNT(a.id) FROM attachment a WHERE a.message = message.id) AS attachments" +
            ", SUM(CASE WHEN folder.type = '" + EntityFolder.DRAFTS + "' THEN 1 ELSE 0 END) AS drafts" +
            ", COUNT(DISTINCT CASE WHEN message.msgid IS NULL THEN message.id ELSE message.msgid END) AS visible" +
            ", SUM(message.size) AS totalSize" +
            ", MAX(CASE WHEN :found OR folder.unified THEN message.received ELSE 0 END) AS dummy" +
            " FROM message" +
            " JOIN account ON account.id = message.account" +
            " LEFT JOIN identity ON identity.id = message.identity" +
            " JOIN folder ON folder.id = message.folder" +
            " WHERE account.`synchronize`" +
            " AND (NOT message.ui_hide OR :debug)" +
            " AND (NOT :found OR ui_found = :found)" +
            " GROUP BY account.id, CASE WHEN message.thread IS NULL OR NOT :threading THEN message.id ELSE message.thread END" +
            " HAVING (:found OR SUM(folder.unified) > 0)" +
            " AND (NOT :filter_seen OR " + unseen_unified + " > 0)" +
            " AND (NOT :filter_unflagged OR COUNT(message.id) - " + unflagged_unified + " > 0)" +
            " AND (NOT :filter_snoozed OR message.ui_snoozed IS NULL)" +
            " ORDER BY" +
            " CASE" +
            "  WHEN 'unread' = :sort THEN " + unseen_unified + " = 0" +
            "  WHEN 'starred' = :sort THEN COUNT(message.id) - " + unflagged_unified + " = 0" +
            "  WHEN 'sender' = :sort THEN LOWER(message.sender)" +
            "  WHEN 'subject' = :sort THEN LOWER(message.subject)" +
            "  WHEN 'size' = :sort THEN -SUM(message.size)" +
            "  WHEN 'snoozed' = :sort THEN SUM(CASE WHEN message.ui_snoozed IS NULL THEN 0 ELSE 1 END) = 0" +
            "  ELSE 0" +
            " END, message.received DESC")
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    DataSource.Factory<Integer, TupleMessageEx> pagedUnifiedInbox(
            boolean threading,
            String sort,
            boolean filter_seen, boolean filter_unflagged, boolean filter_snoozed,
            boolean found,
            boolean debug);

    String is_outbox = "folder.type = '" + EntityFolder.OUTBOX + "'";
    String unseen_folder = "SUM(CASE WHEN message.ui_seen OR (folder.id <> :folder AND " + folder_in + ") THEN 0 ELSE 1 END)";
    String unflagged_folder = "SUM(CASE WHEN message.ui_flagged AND NOT (folder.id <> :folder AND " + folder_in + ") THEN 0 ELSE 1 END)";

    @Query("SELECT message.*" +
            ", account.name AS accountName, IFNULL(identity.color, account.color) AS accountColor, account.notify AS accountNotify" +
            ", folder.name AS folderName, folder.display AS folderDisplay, folder.type AS folderType" +
            ", identity.name AS identityName, identity.email AS identityEmail" +
            ", COUNT(message.id) AS count" +
            ", " + unseen_folder + " AS unseen" +
            ", " + unflagged_folder + " AS unflagged" +
            ", (SELECT COUNT(a.id) FROM attachment a WHERE a.message = message.id) AS attachments" +
            ", SUM(CASE WHEN folder.type = '" + EntityFolder.DRAFTS + "' THEN 1 ELSE 0 END) AS drafts" +
            ", COUNT(DISTINCT CASE WHEN message.msgid IS NULL THEN message.id ELSE message.msgid END) AS visible" +
            ", SUM(message.size) AS totalSize" +
            ", MAX(CASE WHEN folder.id = :folder THEN message.received ELSE 0 END) AS dummy" +
            " FROM message" +
            " JOIN account ON account.id = message.account" +
            " LEFT JOIN identity ON identity.id = message.identity" +
            " JOIN folder ON folder.id = message.folder" +
            " JOIN folder AS f ON f.id = :folder" +
            " WHERE (message.account = f.account OR " + is_outbox + ")" +
            " AND (NOT message.ui_hide OR :debug)" +
            " AND (NOT :found OR ui_found = :found)" +
            " GROUP BY CASE WHEN message.thread IS NULL OR NOT :threading THEN message.id ELSE message.thread END" +
            " HAVING SUM(CASE WHEN folder.id = :folder THEN 1 ELSE 0 END) > 0" +
            " AND (NOT :filter_seen OR " + unseen_folder + " > 0 OR " + is_outbox + ")" +
            " AND (NOT :filter_unflagged OR COUNT(message.id) - " + unflagged_folder + " > 0 OR " + is_outbox + ")" +
            " AND (NOT :filter_snoozed OR message.ui_snoozed IS NULL OR " + is_outbox + ")" +
            " ORDER BY" +
            " CASE" +
            "  WHEN 'unread' = :sort THEN " + unseen_folder + " = 0" +
            "  WHEN 'starred' = :sort THEN COUNT(message.id) - " + unflagged_folder + " = 0" +
            "  WHEN 'sender' = :sort THEN LOWER(message.sender)" +
            "  WHEN 'subject' = :sort THEN LOWER(message.subject)" +
            "  WHEN 'size' = :sort THEN -SUM(message.size)" +
            "  WHEN 'snoozed' = :sort THEN SUM(CASE WHEN message.ui_snoozed IS NULL THEN 0 ELSE 1 END) = 0" +
            "  ELSE 0" +
            " END, message.received DESC")
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    DataSource.Factory<Integer, TupleMessageEx> pagedFolder(
            long folder, boolean threading,
            String sort,
            boolean filter_seen, boolean filter_unflagged, boolean filter_snoozed,
            boolean found,
            boolean debug);

    @Query("SELECT message.*" +
            ", account.name AS accountName, IFNULL(identity.color, account.color) AS accountColor, account.notify AS accountNotify" +
            ", folder.name AS folderName, folder.display AS folderDisplay, folder.type AS folderType" +
            ", identity.name AS identityName, identity.email AS identityEmail" +
            ", 1 AS count" +
            ", CASE WHEN message.ui_seen THEN 0 ELSE 1 END AS unseen" +
            ", CASE WHEN message.ui_flagged THEN 0 ELSE 1 END AS unflagged" +
            ", (SELECT COUNT(a.id) FROM attachment a WHERE a.message = message.id) AS attachments" +
            ", CASE WHEN folder.type = '" + EntityFolder.DRAFTS + "' THEN 1 ELSE 0 END AS drafts" +
            ", 1 AS visible" +
            ", message.size AS totalSize" +
            " FROM message" +
            " JOIN account ON account.id = message.account" +
            " LEFT JOIN identity ON identity.id = message.identity" +
            " JOIN folder ON folder.id = message.folder" +
            " WHERE message.account = :account" +
            " AND message.thread = :thread" +
            " AND (:id IS NULL OR message.id = :id)" +
            " AND (NOT message.ui_hide OR :debug)" +
            " ORDER BY message.received DESC" +
            ", CASE WHEN folder.type = '" + EntityFolder.ARCHIVE + "' THEN 1 ELSE 0 END")
    DataSource.Factory<Integer, TupleMessageEx> pagedThread(long account, String thread, Long id, boolean debug);

    @Query("SELECT account.name AS accountName" +
            ", COUNT(message.id) AS count" +
            ", SUM(message.ui_seen) AS seen" +
            " FROM message" +
            " JOIN account ON account.id = message.account" +
            " WHERE message.account = :account" +
            " AND message.thread = :thread" +
            " AND (:id IS NULL OR message.id = :id)" +
            " AND NOT message.ui_hide" +
            " GROUP BY account.id")
    LiveData<TupleThreadStats> liveThreadStats(long account, String thread, Long id);

    @Query("SELECT message.id FROM folder" +
            " JOIN message ON message.folder = folder.id" +
            " WHERE CASE WHEN :folder IS NULL THEN folder.unified ELSE folder.id = :folder END" +
            " AND ui_hide")
    LiveData<List<Long>> liveHidden(Long folder);

    @Query("SELECT id FROM message" +
            " WHERE account = :account" +
            " AND thread = :thread" +
            " AND ui_hide")
    LiveData<List<Long>> liveHidden(long account, String thread);

    @Query("SELECT COUNT(id)" +
            " FROM message" +
            " WHERE id = :id")
    int countMessage(long id);

    @Query("SELECT *" +
            " FROM message" +
            " WHERE id = :id")
    EntityMessage getMessage(long id);

    @Query("SELECT *" +
            " FROM message" +
            " WHERE folder = :folder" +
            " AND uid = :uid")
    EntityMessage getMessageByUid(long folder, long uid);

    @Query("SELECT id" +
            " FROM message" +
            " WHERE folder = :folder" +
            " AND NOT ui_hide" +
            " ORDER BY message.received DESC")
    List<Long> getMessageByFolder(long folder);

    @Query("SELECT id" +
            " FROM message" +
            " WHERE (:folder IS NULL OR folder = :folder)" +
            " AND NOT ui_hide" +
            " ORDER BY message.received DESC")
    List<Long> getMessageIdsByFolder(Long folder);

    @Query("SELECT message.id" +
            " FROM folder" +
            " JOIN message ON message.folder = folder.id" +
            " WHERE CASE WHEN :folder IS NULL THEN folder.unified ELSE folder.id = :folder END" +
            " AND NOT ui_hide" +
            " AND (ui_snoozed IS NULL OR :snoozed)" +
            " ORDER BY message.received DESC")
    List<Long> getMessageAll(Long folder, boolean snoozed);

    @Query("SELECT *" +
            " FROM message" +
            " WHERE account = :account" +
            " AND thread = :thread" +
            " AND (:id IS NULL OR message.id = :id)" +
            " AND (:folder IS NULL OR message.folder = :folder)" +
            " AND NOT uid IS NULL" +
            " AND NOT ui_hide")
    List<EntityMessage> getMessageByThread(long account, String thread, Long id, Long folder);

    @Query("SELECT * FROM message" +
            " WHERE account = :account" +
            " AND msgid = :msgid")
    List<EntityMessage> getMessageByMsgId(long account, String msgid);

    @Query("SELECT COUNT(*) FROM message" +
            " WHERE folder = :folder" +
            " AND msgid = :msgid")
    int countMessageByMsgId(long folder, String msgid);

    @Query("SELECT message.*" +
            ", account.name AS accountName, identity.color AS accountColor, account.notify AS accountNotify" +
            ", folder.name AS folderName, folder.display AS folderDisplay, folder.type AS folderType" +
            ", identity.name AS identityName, identity.email AS identityEmail" +
            ", 1 AS count" +
            ", CASE WHEN message.ui_seen THEN 0 ELSE 1 END AS unseen" +
            ", CASE WHEN message.ui_flagged THEN 0 ELSE 1 END AS unflagged" +
            ", (SELECT COUNT(a.id) FROM attachment a WHERE a.message = message.id) AS attachments" +
            ", CASE WHEN folder.type = '" + EntityFolder.DRAFTS + "' THEN 1 ELSE 0 END AS drafts" +
            ", 1 AS visible" +
            ", message.size AS totalSize" +
            " FROM message" +
            " JOIN account ON account.id = message.account" +
            " LEFT JOIN identity ON identity.id = message.identity" +
            " JOIN folder ON folder.id = message.folder" +
            " WHERE message.id = :id")
    LiveData<TupleMessageEx> liveMessage(long id);

    @Query("SELECT message.*" +
            ", account.name AS accountName, IFNULL(identity.color, account.color) AS accountColor, account.notify AS accountNotify" +
            ", folder.name AS folderName, folder.display AS folderDisplay, folder.type AS folderType" +
            ", identity.name AS identityName, identity.email AS identityEmail" +
            ", 1 AS count" +
            ", 1 AS unseen" +
            ", 0 AS unflagged" +
            ", 0 AS attachments" +
            ", 0 AS drafts" +
            ", 1 AS visible" +
            ", message.size AS totalSize" +
            " FROM message" +
            " JOIN account ON account.id = message.account" +
            " LEFT JOIN identity ON identity.id = message.identity" +
            " JOIN folder ON folder.id = message.folder" +
            " WHERE account.`synchronize`" +
            " AND folder.notify" +
            " AND (account.created IS NULL OR message.received > account.created)" +
            " AND NOT message.ui_seen" +
            " AND NOT message.ui_hide" +
            " AND NOT message.ui_ignored" +
            " ORDER BY message.received")
    LiveData<List<TupleMessageEx>> liveUnseenNotify();

    @Query("SELECT COUNT(message.id) FROM message" +
            " JOIN account ON account.id = message.account" +
            " JOIN folder ON folder.id = message.folder" +
            " WHERE account.`synchronize`" +
            " AND folder.unified" +
            " AND (account.created IS NULL OR message.received > account.created)" +
            " AND NOT message.ui_seen" +
            " AND NOT message.ui_hide" +
            " AND NOT message.ui_ignored" +
            " ORDER BY message.received")
    int getUnseenUnified();

    @Query("SELECT uid FROM message" +
            " WHERE folder = :folder" +
            " AND (:received IS NULL OR received >= :received)" +
            " AND NOT uid IS NULL" +
            " AND NOT ui_browsed")
    List<Long> getUids(long folder, Long received);

    @Query("SELECT message.* FROM message" +
            " JOIN folder on folder.id = message.folder" +
            " WHERE  message.account = :account" +
            " AND folder.type = '" + EntityFolder.OUTBOX + "'" +
            " AND sent IS NOT NULL")
    List<EntityMessage> getSentOrphans(long account);

    @Query("SELECT * FROM message WHERE NOT ui_snoozed IS NULL")
    List<EntityMessage> getSnoozed();

    @Query("SELECT id, `from`, avatar FROM message" +
            " WHERE folder = :folder" +
            " AND received >= :before")
    List<TupleMessageLookup> getAvatars(long folder, long before);

    @Insert
    long insertMessage(EntityMessage message);

    @Update
    int updateMessage(EntityMessage message);

    @Query("UPDATE message SET folder = :folder WHERE id = :id")
    int setMessageFolder(long id, long folder);

    @Query("UPDATE message SET uid = :uid WHERE id = :id")
    int setMessageUid(long id, Long uid);

    @Query("UPDATE message SET seen = :seen WHERE id = :id")
    int setMessageSeen(long id, boolean seen);

    @Query("UPDATE message SET flagged = :flagged WHERE id = :id")
    int setMessageFlagged(long id, boolean flagged);

    @Query("UPDATE message SET answered = :answered WHERE id = :id")
    int setMessageAnswered(long id, boolean answered);

    @Query("UPDATE message SET keywords = :keywords WHERE id = :id")
    int setMessageKeywords(long id, String keywords);

    @Query("UPDATE message SET ui_seen = :ui_seen WHERE id = :id")
    int setMessageUiSeen(long id, boolean ui_seen);

    @Query("UPDATE message SET ui_flagged = :ui_flagged WHERE id = :id")
    int setMessageUiFlagged(long id, boolean ui_flagged);

    @Query("UPDATE message SET ui_answered = :ui_answered WHERE id = :id")
    int setMessageUiAnswered(long id, boolean ui_answered);

    @Query("UPDATE message SET ui_hide = :ui_hide WHERE id = :id")
    int setMessageUiHide(long id, boolean ui_hide);

    @Query("UPDATE message SET ui_hide = :ui_hide WHERE folder = :folder AND uid = :uid")
    int setMessageUiHide(long folder, long uid, boolean ui_hide);

    @Query("UPDATE message SET ui_ignored = :ui_ignored WHERE id = :id")
    int setMessageUiIgnored(long id, boolean ui_ignored);

    @Query("UPDATE message SET color = :color WHERE id = :id")
    int setMessageColor(long id, Integer color);

    @Query("UPDATE message SET received = :sent, sent = :sent WHERE id = :id")
    int setMessageSent(long id, Long sent);

    @Query("UPDATE message SET receipt_request = :receipt_request WHERE id = :id")
    int setMessageReceiptRequested(long id, Boolean receipt_request);

    @Query("UPDATE message SET avatar = :avatar WHERE id = :id")
    int setMessageAvatar(long id, String avatar);

    @Query("UPDATE message SET error = :error WHERE id = :id")
    int setMessageError(long id, String error);

    @Query("UPDATE message SET revision = :revision WHERE id = :id")
    int setMessageRevision(long id, Integer revision);

    @Query("UPDATE message SET revisions = :revisions WHERE id = :id")
    int setMessageRevisions(long id, Integer revisions);

    @Query("UPDATE message SET content = :content, plain_only = :plain_only, preview = :preview, warning = :warning WHERE id = :id")
    int setMessageContent(long id, boolean content, Boolean plain_only, String preview, String warning);

    @Query("UPDATE message SET size = :size WHERE id = :id")
    int setMessageSize(long id, Long size);

    @Query("UPDATE message SET headers = :headers WHERE id = :id")
    int setMessageHeaders(long id, String headers);

    @Query("UPDATE message SET raw = :raw WHERE id = :id")
    int setMessageRaw(long id, Boolean raw);

    @Query("UPDATE message SET stored = :stored WHERE id = :id")
    int setMessageStored(long id, long stored);

    @Query("UPDATE message SET plain_only = :plain_only WHERE id = :id")
    int setMessagePlainOnly(long id, boolean plain_only);

    @Query("UPDATE message SET last_attempt = :last_attempt WHERE id = :id")
    int setMessageLastAttempt(long id, long last_attempt);

    @Query("UPDATE message SET ui_ignored = 1" +
            " WHERE NOT ui_ignored" +
            " AND folder IN (SELECT id FROM folder WHERE type = '" + EntityFolder.INBOX + "')")
    int ignoreAll();

    @Query("UPDATE message SET ui_found = 1" +
            " WHERE account = :account" +
            " AND thread = :thread")
    int setMessageFound(long account, String thread);

    @Query("UPDATE message SET ui_found = 0")
    int resetSearch();

    @Query("UPDATE message SET ui_snoozed = :wakeup" +
            " WHERE id = :id")
    int setMessageSnoozed(long id, Long wakeup);

    @Query("DELETE FROM message WHERE id = :id")
    int deleteMessage(long id);

    @Query("DELETE FROM message" +
            " WHERE folder = :folder" +
            " AND uid = :uid")
    int deleteMessage(long folder, long uid);

    @Query("DELETE FROM message" +
            " WHERE folder = :folder" +
            " AND NOT uid IS NULL")
    int deleteLocalMessages(long folder);

    @Query("DELETE FROM message" +
            " WHERE folder = :folder" +
            " AND ui_browsed" +
            " AND NOT uid IS NULL")
    int deleteBrowsedMessages(long folder);

    @Query("DELETE FROM message" +
            " WHERE folder = :folder" +
            " AND uid IS NULL" +
            " AND NOT EXISTS" +
            "  (SELECT * FROM operation" +
            "  WHERE operation.message = message.id" +
            "  AND operation.name = '" + EntityOperation.ADD + "')")
    int deleteOrphans(long folder);

    @Query("SELECT id FROM message" +
            " WHERE folder = :folder" +
            " AND received < :received" +
            " AND NOT uid IS NULL" +
            " AND (ui_seen OR :unseen)" +
            " AND NOT ui_flagged" +
            " AND NOT ui_browsed")
    List<Long> getMessagesBefore(long folder, long received, boolean unseen);

    @Query("DELETE FROM message" +
            " WHERE folder = :folder" +
            " AND received < :received" +
            " AND NOT uid IS NULL" +
            " AND (ui_seen OR :unseen)" +
            " AND NOT ui_flagged" +
            " AND NOT ui_browsed")
    int deleteMessagesBefore(long folder, long received, boolean unseen);
}