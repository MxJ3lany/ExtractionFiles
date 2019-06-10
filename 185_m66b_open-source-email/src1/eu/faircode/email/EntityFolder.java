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

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.Collator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        tableName = EntityFolder.TABLE_NAME,
        foreignKeys = {
                @ForeignKey(childColumns = "account", entity = EntityAccount.class, parentColumns = "id", onDelete = CASCADE)
        },
        indices = {
                @Index(value = {"account", "name"}, unique = true),
                @Index(value = {"account"}),
                @Index(value = {"name"}),
                @Index(value = {"type"}),
                @Index(value = {"unified"})
        }
)

public class EntityFolder extends EntityOrder implements Serializable {
    static final String TABLE_NAME = "folder";

    @PrimaryKey(autoGenerate = true)
    public Long id;
    public Long account; // Outbox = null
    public Long parent;
    @NonNull
    public String name;
    @NonNull
    public String type;
    @NonNull
    public Integer level = 0; // obsolete
    @NonNull
    public Boolean synchronize;
    @NonNull
    public Boolean poll = false;
    @NonNull
    public Boolean download = true;
    public Boolean subscribed;
    @NonNull
    public Integer sync_days;
    @NonNull
    public Integer keep_days;
    @NonNull
    public Boolean auto_delete = false;
    public String display;
    @NonNull
    public Boolean hide = false; // obsolete
    @NonNull
    public Boolean collapsed = false;
    @NonNull
    public Boolean unified = false;
    @NonNull
    public Boolean navigation = false;
    @NonNull
    public Boolean notify = false;

    public Integer total; // messages on server
    public String[] keywords;

    @NonNull
    public Boolean initialize = true;
    public Boolean tbc; // to be created
    public Boolean tbd; // to be deleted
    public String state;
    public String sync_state;
    @NonNull
    public Boolean read_only = false;
    public String error;
    public Long last_sync;

    static final String INBOX = "Inbox";
    static final String OUTBOX = "Outbox";
    static final String ARCHIVE = "All";
    static final String DRAFTS = "Drafts";
    static final String TRASH = "Trash";
    static final String JUNK = "Junk";
    static final String SENT = "Sent";
    static final String SYSTEM = "System";
    static final String USER = "User";

    // https://www.iana.org/assignments/imap-mailbox-name-attributes/imap-mailbox-name-attributes.xhtml
    private static final List<String> SYSTEM_FOLDER_ATTR = Collections.unmodifiableList(Arrays.asList(
            "All",
            "Archive",
            "Drafts",
            "Trash",
            "Junk",
            "Sent",
            "Important",
            "Flagged"
    ));
    private static final List<String> SYSTEM_FOLDER_TYPE = Collections.unmodifiableList(Arrays.asList(
            ARCHIVE, // All
            ARCHIVE,
            DRAFTS,
            TRASH,
            JUNK,
            SENT,
            SYSTEM, // Important
            SYSTEM // Flagged
    )); // MUST match SYSTEM_FOLDER_ATTR

    static final List<String> FOLDER_SORT_ORDER = Collections.unmodifiableList(Arrays.asList(
            INBOX,
            OUTBOX,
            DRAFTS,
            SENT,
            ARCHIVE,
            TRASH,
            JUNK,
            SYSTEM,
            USER
    ));

    static final int DEFAULT_INIT = 14; // days
    static final int DEFAULT_SYNC = 7; // days
    static final int DEFAULT_KEEP = 30; // days

    static final List<String> SYSTEM_FOLDER_SYNC = Collections.unmodifiableList(Arrays.asList(
            INBOX,
            DRAFTS,
            SENT,
            ARCHIVE,
            TRASH,
            JUNK
    ));
    static final List<Boolean> SYSTEM_FOLDER_DOWNLOAD = Collections.unmodifiableList(Arrays.asList(
            true, // inbox
            true, // drafts
            false, // sent
            false, // archive
            false, // trash
            false // junk
    )); // MUST match SYSTEM_FOLDER_SYNC

    public EntityFolder() {
    }

    static String getNotificationChannelId(long id) {
        return "notification.folder." + id;
    }

    JSONArray getSyncArgs() {
        int days = sync_days;
        if (last_sync != null) {
            int ago_days = (int) ((new Date().getTime() - last_sync) / (24 * 3600 * 1000L)) + 1;
            if (ago_days > days)
                days = ago_days;
        }

        JSONArray jargs = new JSONArray();
        jargs.put(initialize ? Math.min(DEFAULT_INIT, keep_days) : days);
        jargs.put(keep_days);
        jargs.put(download);
        jargs.put(auto_delete);

        return jargs;
    }

    static int getIcon(String type) {
        if (EntityFolder.INBOX.equals(type))
            return R.drawable.baseline_inbox_24;
        if (EntityFolder.DRAFTS.equals(type))
            return R.drawable.baseline_drafts_24;
        if (EntityFolder.SENT.equals(type))
            return R.drawable.baseline_send_24;
        if (EntityFolder.ARCHIVE.equals(type))
            return R.drawable.baseline_archive_24;
        if (EntityFolder.TRASH.equals(type))
            return R.drawable.baseline_delete_24;
        if (EntityFolder.JUNK.equals(type))
            return R.drawable.baseline_flag_24;
        return R.drawable.baseline_folder_24;
    }

    String getDisplayName(Context context) {
        return (display == null ? Helper.localizeFolderName(context, name) : display);
    }

    String getDisplayName(Context context, EntityFolder parent) {
        String n = name;
        if (parent != null && name.startsWith(parent.name))
            n = n.substring(parent.name.length() + 1);
        return (display == null ? Helper.localizeFolderName(context, n) : display);
    }

    @Override
    Long getSortId() {
        return id;
    }

    @Override
    String[] getSortTitle(Context context) {
        return new String[]{getDisplayName(context), null};
    }

    boolean isOutgoing() {
        return isOutgoing(this.type);
    }

    static boolean isOutgoing(String type) {
        return DRAFTS.equals(type) || OUTBOX.equals(type) || SENT.equals(type);
    }

    static String getType(String[] attrs, String fullName) {
        for (String attr : attrs) {
            if ("\\Noselect".equals(attr) || "\\NonExistent".equals(attr))
                return null;

            if (attr.startsWith("\\")) {
                int index = SYSTEM_FOLDER_ATTR.indexOf(attr.substring(1));
                if (index >= 0)
                    return SYSTEM_FOLDER_TYPE.get(index);
            }
        }

        // https://tools.ietf.org/html/rfc3501#section-5.1
        if ("INBOX".equals(fullName.toUpperCase()))
            return INBOX;

        return USER;
    }

    String getParentName(Character separator) {
        if (separator == null)
            return null;
        else {
            int p = name.lastIndexOf(separator);
            if (p < 0)
                return null;
            else
                return name.substring(0, p);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityFolder) {
            EntityFolder other = (EntityFolder) obj;
            return (this.id.equals(other.id) &&
                    Objects.equals(this.account, other.account) &&
                    this.name.equals(other.name) &&
                    this.type.equals(other.type) &&
                    this.synchronize.equals(other.synchronize) &&
                    this.poll.equals(other.poll) &&
                    this.download.equals(other.download) &&
                    Objects.equals(this.subscribed, other.subscribed) &&
                    this.sync_days.equals(other.sync_days) &&
                    this.keep_days.equals(other.keep_days) &&
                    Objects.equals(this.display, other.display) &&
                    Objects.equals(this.order, other.order) &&
                    this.collapsed == other.collapsed &&
                    this.unified == other.unified &&
                    this.notify == other.notify &&
                    Objects.equals(this.total, other.total) &&
                    Helper.equal(this.keywords, other.keywords) &&
                    Objects.equals(this.tbc, other.tbc) &&
                    Objects.equals(this.tbd, other.tbd) &&
                    Objects.equals(this.state, other.state) &&
                    Objects.equals(this.sync_state, other.sync_state) &&
                    Objects.equals(this.error, other.error) &&
                    Objects.equals(this.last_sync, other.last_sync));
        } else
            return false;
    }

    @Override
    public String toString() {
        return name;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("type", type);
        json.put("synchronize", synchronize);
        json.put("poll", poll);
        json.put("download", download);
        json.put("sync_days", sync_days);
        json.put("keep_days", keep_days);
        json.put("auto_delete", auto_delete);
        json.put("display", display);
        json.put("collapsed", collapsed);
        json.put("unified", unified);
        json.put("navigation", navigation);
        json.put("notify", notify);
        return json;
    }

    public static EntityFolder fromJSON(JSONObject json) throws JSONException {
        EntityFolder folder = new EntityFolder();
        if (json.has("id"))
            folder.id = json.getLong("id");

        folder.name = json.getString("name");
        folder.type = json.getString("type");

        folder.synchronize = json.getBoolean("synchronize");

        if (json.has("poll"))
            folder.poll = json.getBoolean("poll");

        if (json.has("download"))
            folder.download = json.getBoolean("download");

        if (json.has("after"))
            folder.sync_days = json.getInt("after");
        else
            folder.sync_days = json.getInt("sync_days");

        if (json.has("keep_days"))
            folder.keep_days = json.getInt("keep_days");
        else
            folder.keep_days = folder.sync_days;

        if (json.has("auto_delete"))
            folder.auto_delete = json.getBoolean("auto_delete");

        if (json.has("display") && !json.isNull("display"))
            folder.display = json.getString("display");

        if (json.has("collapsed"))
            folder.collapsed = json.getBoolean("collapsed");

        folder.unified = json.getBoolean("unified");

        if (json.has("navigation"))
            folder.navigation = json.getBoolean("navigation");

        if (json.has("notify"))
            folder.notify = json.getBoolean("notify");

        return folder;
    }

    @Override
    Comparator getComparator(final Context context) {
        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.SECONDARY); // Case insensitive, process accents etc

        return new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                EntityFolder f1 = (EntityFolder) o1;
                EntityFolder f2 = (EntityFolder) o2;

                int o = Integer.compare(
                        f1.order == null ? -1 : f1.order,
                        f2.order == null ? -1 : f2.order);
                if (o != 0)
                    return o;

                int i1 = EntityFolder.FOLDER_SORT_ORDER.indexOf(f1.type);
                int i2 = EntityFolder.FOLDER_SORT_ORDER.indexOf(f2.type);
                int s = Integer.compare(i1, i2);
                if (s != 0)
                    return s;

                int c = -f1.synchronize.compareTo(f2.synchronize);
                if (c != 0)
                    return c;

                String name1 = f1.getDisplayName(context);
                String name2 = f2.getDisplayName(context);
                return collator.compare(name1, name2);
            }
        };
    }
}
