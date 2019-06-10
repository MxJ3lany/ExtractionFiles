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
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        tableName = EntityRule.TABLE_NAME,
        foreignKeys = {
                @ForeignKey(childColumns = "folder", entity = EntityFolder.class, parentColumns = "id", onDelete = CASCADE),
        },
        indices = {
                @Index(value = {"folder"}),
                @Index(value = {"order"})
        }
)
public class EntityRule {
    static final String TABLE_NAME = "rule";

    @PrimaryKey(autoGenerate = true)
    public Long id;
    @NonNull
    public Long folder;
    @NonNull
    public String name;
    @NonNull
    public int order;
    @NonNull
    public boolean enabled;
    @NonNull
    public boolean stop;
    @NonNull
    public String condition;
    @NonNull
    public String action;

    static final int TYPE_SEEN = 1;
    static final int TYPE_UNSEEN = 2;
    static final int TYPE_MOVE = 3;
    static final int TYPE_ANSWER = 4;
    static final int TYPE_AUTOMATION = 5;
    static final int TYPE_FLAG = 6;
    static final int TYPE_COPY = 7;
    static final int TYPE_SNOOZE = 8;

    static final String ACTION_AUTOMATION = BuildConfig.APPLICATION_ID + ".AUTOMATION";
    static final String EXTRA_RULE = "rule";
    static final String EXTRA_SENDER = "sender";
    static final String EXTRA_SUBJECT = "subject";

    boolean matches(Context context, EntityMessage message, Message imessage) throws MessagingException {
        try {
            JSONObject jcondition = new JSONObject(condition);

            // Sender
            JSONObject jsender = jcondition.optJSONObject("sender");
            if (jsender != null) {
                String value = jsender.getString("value");
                boolean regex = jsender.getBoolean("regex");

                boolean matches = false;
                if (message.from != null) {
                    for (Address from : message.from) {
                        InternetAddress ia = (InternetAddress) from;
                        String personal = ia.getPersonal();
                        String formatted = ((personal == null ? "" : personal + " ") + "<" + ia.getAddress() + ">");
                        if (matches(context, value, formatted, regex)) {
                            matches = true;
                            break;
                        }
                    }
                }
                if (!matches)
                    return false;
            }

            // Recipient
            JSONObject jrecipient = jcondition.optJSONObject("recipient");
            if (jrecipient != null) {
                String value = jrecipient.getString("value");
                boolean regex = jrecipient.getBoolean("regex");

                boolean matches = false;
                List<Address> recipients = new ArrayList<>();
                if (message.to != null)
                    recipients.addAll(Arrays.asList(message.to));
                if (message.cc != null)
                    recipients.addAll(Arrays.asList(message.cc));
                for (Address recipient : recipients) {
                    InternetAddress ia = (InternetAddress) recipient;
                    String personal = ia.getPersonal();
                    String formatted = ((personal == null ? "" : personal + " ") + "<" + ia.getAddress() + ">");
                    if (matches(context, value, formatted, regex)) {
                        matches = true;
                        break;
                    }
                }
                if (!matches)
                    return false;
            }

            JSONObject jsubject = jcondition.optJSONObject("subject");
            if (jsubject != null) {
                String value = jsubject.getString("value");
                boolean regex = jsubject.getBoolean("regex");

                if (!matches(context, value, message.subject, regex))
                    return false;
            }

            JSONObject jheader = jcondition.optJSONObject("header");
            if (jheader != null && imessage != null) {
                String value = jheader.getString("value");
                boolean regex = jheader.getBoolean("regex");

                boolean matches = false;
                Enumeration<Header> headers = imessage.getAllHeaders();
                while (headers.hasMoreElements()) {
                    Header header = headers.nextElement();
                    String formatted = header.getName() + ": " + header.getValue();
                    if (matches(context, value, formatted, regex)) {
                        matches = true;
                        break;
                    }
                }
                if (!matches)
                    return false;
            }

            // Safeguard
            if (jsender == null && jrecipient == null && jsubject == null && jheader == null)
                return false;
        } catch (JSONException ex) {
            Log.e(ex);
            return false;
        }

        return true;
    }

    private boolean matches(Context context, String needle, String haystack, boolean regex) {
        boolean matched = false;
        if (needle != null && haystack != null)
            if (regex) {
                Pattern pattern = Pattern.compile(needle);
                matched = pattern.matcher(haystack).matches();
            } else
                matched = haystack.toLowerCase().contains(needle.trim().toLowerCase());

        EntityLog.log(context, "Rule=" + name + " matched=" + matched +
                " needle=" + needle + " haystack=" + haystack + " regex=" + regex);
        return matched;
    }

    void execute(Context context, EntityMessage message) throws IOException {
        try {
            JSONObject jaction = new JSONObject(action);
            int type = jaction.getInt("type");
            Log.i("Executing rule=" + type + ":" + name + " message=" + message.id);

            switch (type) {
                case TYPE_SEEN:
                    onActionSeen(context, message, true);
                    break;
                case TYPE_UNSEEN:
                    onActionSeen(context, message, false);
                    break;
                case TYPE_SNOOZE:
                    onActionSnooze(context, message, jaction);
                    break;
                case TYPE_FLAG:
                    onActionFlag(context, message, jaction);
                    break;
                case TYPE_MOVE:
                    onActionMove(context, message, jaction);
                    break;
                case TYPE_COPY:
                    onActionCopy(context, message, jaction);
                    break;
                case TYPE_ANSWER:
                    onActionAnswer(context, message, jaction);
                    break;
                case TYPE_AUTOMATION:
                    onActionAutomation(context, message, jaction);
                    break;
            }
        } catch (JSONException ex) {
            Log.e(ex);
        }
    }

    private void onActionSeen(Context context, EntityMessage message, boolean seen) {
        EntityOperation.queue(context, message, EntityOperation.SEEN, seen);
        message.seen = seen;
    }

    private void onActionMove(Context context, EntityMessage message, JSONObject jargs) throws JSONException {
        long target = jargs.getLong("target");

        DB db = DB.getInstance(context);
        EntityFolder folder = db.folder().getFolder(target);
        if (folder == null)
            throw new IllegalArgumentException("Rule move to folder not found");

        EntityOperation.queue(context, message, EntityOperation.MOVE, target, false);
    }

    private void onActionCopy(Context context, EntityMessage message, JSONObject jargs) throws JSONException {
        long target = jargs.getLong("target");

        DB db = DB.getInstance(context);
        EntityFolder folder = db.folder().getFolder(target);
        if (folder == null)
            throw new IllegalArgumentException("Rule copy to folder not found");

        EntityOperation.queue(context, message, EntityOperation.COPY, target, false);
    }

    private void onActionAnswer(Context context, EntityMessage message, JSONObject jargs) throws JSONException, IOException {
        long iid = jargs.getLong("identity");
        long aid = jargs.getLong("answer");
        boolean cc = (jargs.has("cc") && jargs.getBoolean("cc"));

        DB db = DB.getInstance(context);

        EntityIdentity identity = db.identity().getIdentity(iid);
        if (identity == null)
            throw new IllegalArgumentException("Rule identity not found");

        String body = EntityAnswer.getAnswerText(db, aid, message.from);
        if (body == null)
            throw new IllegalArgumentException("Rule answer not found");

        EntityMessage reply = new EntityMessage();
        reply.account = message.account;
        reply.folder = db.folder().getOutbox().id;
        reply.identity = identity.id;
        reply.msgid = EntityMessage.generateMessageId();
        reply.references = (message.references == null ? "" : message.references + " ") + message.msgid;
        reply.inreplyto = message.msgid;
        reply.thread = message.thread;
        reply.to = (message.reply == null || message.reply.length == 0 ? message.from : message.reply);
        reply.from = new InternetAddress[]{new InternetAddress(identity.email, identity.name)};
        if (cc)
            reply.cc = message.cc;
        reply.subject = context.getString(R.string.title_subject_reply, message.subject == null ? "" : message.subject);
        reply.received = new Date().getTime();

        reply.sender = MessageHelper.getSortKey(reply.from);
        Uri lookupUri = ContactInfo.getLookupUri(context, reply.from);
        reply.avatar = (lookupUri == null ? null : lookupUri.toString());

        reply.id = db.message().insertMessage(reply);
        Helper.writeText(reply.getFile(context), body);
        db.message().setMessageContent(reply.id,
                true,
                false,
                HtmlHelper.getPreview(body),
                null);

        EntityOperation.queue(context, reply, EntityOperation.SEND);
    }

    private void onActionAutomation(Context context, EntityMessage message, JSONObject jargs) throws JSONException {
        String sender = (message.from == null || message.from.length == 0
                ? null : ((InternetAddress) message.from[0]).getAddress());

        Intent automation = new Intent(ACTION_AUTOMATION);
        automation.putExtra(EXTRA_RULE, name);
        automation.putExtra(EXTRA_SENDER, sender);
        automation.putExtra(EXTRA_SUBJECT, message.subject);

        Log.i("Sending " + automation);
        try {
            context.sendBroadcast(automation);
        } catch (Throwable ex) {
            Log.e(ex);
        }
    }

    private void onActionSnooze(Context context, EntityMessage message, JSONObject jargs) throws JSONException {
        int duration = jargs.getInt("duration");
        long wakeup = new Date().getTime() + duration * 3600 * 1000L;

        DB db = DB.getInstance(context);
        db.message().setMessageSnoozed(message.id, wakeup);
        EntityMessage.snooze(context, message.id, wakeup);
        onActionSeen(context, message, true);
    }

    private void onActionFlag(Context context, EntityMessage message, JSONObject jargs) throws JSONException {
        Integer color = (jargs.has("color") && !jargs.isNull("color")
                ? jargs.getInt("color") : null);
        EntityOperation.queue(context, message, EntityOperation.FLAG, true, color);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityRule) {
            EntityRule other = (EntityRule) obj;
            return this.folder.equals(other.folder) &&
                    this.name.equals(other.name) &&
                    this.order == other.order &&
                    this.enabled == other.enabled &&
                    this.stop == other.stop &&
                    this.condition.equals(other.condition) &&
                    this.action.equals(other.action);
        } else
            return false;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("order", order);
        json.put("enabled", enabled);
        json.put("stop", stop);
        json.put("condition", condition);
        json.put("action", action);
        return json;
    }

    public static EntityRule fromJSON(JSONObject json) throws JSONException {
        EntityRule rule = new EntityRule();
        // id
        rule.name = json.getString("name");
        rule.order = json.getInt("order");
        rule.enabled = json.getBoolean("enabled");
        rule.stop = json.getBoolean("stop");
        rule.condition = json.getString("condition");
        rule.action = json.getString("action");
        return rule;
    }
}
