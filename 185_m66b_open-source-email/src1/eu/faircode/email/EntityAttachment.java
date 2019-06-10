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
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.mail.Part;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        tableName = EntityAttachment.TABLE_NAME,
        foreignKeys = {
                @ForeignKey(childColumns = "message", entity = EntityMessage.class, parentColumns = "id", onDelete = CASCADE)
        },
        indices = {
                @Index(value = {"message"}),
                @Index(value = {"message", "sequence"}, unique = true),
                @Index(value = {"message", "cid"})
        }
)
public class EntityAttachment {
    static final String TABLE_NAME = "attachment";

    static final Integer PGP_MESSAGE = 1;
    static final Integer PGP_SIGNATURE = 2;

    // https://developer.android.com/guide/topics/media/media-formats#image-formats
    private static final List<String> IMAGE_TYPES = Collections.unmodifiableList(Arrays.asList(
            "image/bmp",
            "image/gif",
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    ));

    private static final List<String> IMAGE_TYPES8 = Collections.unmodifiableList(Arrays.asList(
            "image/heic",
            "image/heif"
    ));

    @PrimaryKey(autoGenerate = true)
    public Long id;
    @NonNull
    public Long message;
    @NonNull
    public Integer sequence;
    public String name;
    @NonNull
    public String type;
    public String disposition;
    public String cid; // Content-ID
    public Integer encryption;
    public Long size;
    public Integer progress;
    @NonNull
    public Boolean available = false;
    public String error;

    boolean isInline() {
        return (disposition != null && disposition.equalsIgnoreCase(Part.INLINE));
    }

    boolean isImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            if (IMAGE_TYPES8.contains(type))
                return true;

        return IMAGE_TYPES.contains(type);
    }

    File getFile(Context context) {
        return getFile(context, id, name);
    }

    static File getFile(Context context, long id, String name) {
        File dir = new File(context.getFilesDir(), "attachments");
        if (!dir.exists())
            dir.mkdir();
        String filename = Long.toString(id);
        if (!TextUtils.isEmpty(name))
            filename += "." + Helper.sanitizeFilename(name);
        return new File(dir, filename);
    }

    static void copy(Context context, long oldid, long newid) {
        DB db = DB.getInstance(context);
        List<EntityAttachment> attachments = db.attachment().getAttachments(oldid);
        for (EntityAttachment attachment : attachments) {
            File source = attachment.getFile(context);

            attachment.id = null;
            attachment.message = newid;
            attachment.progress = null;
            attachment.id = db.attachment().insertAttachment(attachment);

            if (attachment.available) {
                File target = attachment.getFile(context);
                try {
                    Helper.copy(source, target);
                } catch (IOException ex) {
                    Log.e(ex);
                    db.attachment().setProgress(attachment.id, null);
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityAttachment) {
            EntityAttachment other = (EntityAttachment) obj;
            return (this.message.equals(other.message) &&
                    this.sequence.equals(other.sequence) &&
                    Objects.equals(this.name, other.name) &&
                    this.type.equals(other.type) &&
                    Objects.equals(this.disposition, other.disposition) &&
                    Objects.equals(this.cid, other.cid) &&
                    Objects.equals(this.encryption, other.encryption) &&
                    Objects.equals(this.size, other.size) &&
                    Objects.equals(this.progress, other.progress) &&
                    this.available.equals(other.available));
        } else
            return false;
    }

    @NonNull
    @Override
    public String toString() {
        return (this.name +
                " type=" + this.type +
                " disposition=" + this.disposition +
                " cid=" + this.cid +
                " encryption=" + this.encryption +
                " size=" + this.size);
    }
}
