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

import androidx.room.Ignore;

import java.util.Objects;

public class TupleMessageEx extends EntityMessage {
    public String accountName;
    public Integer accountColor;
    public boolean accountNotify;
    public String folderName;
    public String folderDisplay;
    public String folderType;
    public String identityName;
    public String identityEmail;
    public int count;
    public int unseen;
    public int unflagged;
    public int attachments;
    public int drafts;
    public int visible;
    public Long totalSize;
    @Ignore
    public boolean duplicate;

    @Override
    public boolean uiEquals(Object obj) {
        if (obj instanceof TupleMessageEx) {
            TupleMessageEx other = (TupleMessageEx) obj;
            return (super.uiEquals(obj) &&
                    Objects.equals(this.accountName, other.accountName) &&
                    Objects.equals(this.accountColor, other.accountColor) &&
                    //this.accountNotify == other.accountNotify &&
                    this.folderName.equals(other.folderName) &&
                    Objects.equals(this.folderDisplay, other.folderDisplay) &&
                    this.folderType.equals(other.folderType) &&
                    Objects.equals(this.identityName, other.identityName) &&
                    Objects.equals(this.identityEmail, other.identityEmail) &&
                    this.count == other.count &&
                    this.unseen == other.unseen &&
                    this.unflagged == other.unflagged &&
                    this.attachments == other.attachments &&
                    this.drafts == other.drafts &&
                    this.visible == other.visible &&
                    Objects.equals(this.totalSize, other.totalSize) &&
                    this.duplicate == other.duplicate);
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TupleMessageEx) {
            TupleMessageEx other = (TupleMessageEx) obj;
            return (super.equals(obj) &&
                    Objects.equals(this.accountName, other.accountName) &&
                    Objects.equals(this.accountColor, other.accountColor) &&
                    this.accountNotify == other.accountNotify &&
                    this.folderName.equals(other.folderName) &&
                    Objects.equals(this.folderDisplay, other.folderDisplay) &&
                    this.folderType.equals(other.folderType) &&
                    this.count == other.count &&
                    this.unseen == other.unseen &&
                    this.unflagged == other.unflagged &&
                    this.attachments == other.attachments &&
                    this.drafts == other.drafts &&
                    this.visible == other.visible &&
                    Objects.equals(this.totalSize, other.totalSize) &&
                    this.duplicate == other.duplicate);
        }
        return false;
    }
}
