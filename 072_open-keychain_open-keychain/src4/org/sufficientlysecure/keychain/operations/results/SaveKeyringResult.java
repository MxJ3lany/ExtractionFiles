/*
 * Copyright (C) 2017 Schürmann & Breitmoser GbR
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.sufficientlysecure.keychain.operations.results;

import android.os.Parcel;

import org.sufficientlysecure.keychain.Constants;
import org.sufficientlysecure.keychain.pgp.CanonicalizedKeyRing;

public class SaveKeyringResult extends OperationResult {

    public final Long savedMasterKeyId;

    public SaveKeyringResult(int result, OperationLog log,
                             CanonicalizedKeyRing ring) {
        super(result, log);
        savedMasterKeyId = ring != null ? ring.getMasterKeyId() : null;
    }

    // Some old key was updated
    public static final int UPDATED = 4;

    // Public key was saved
    public static final int SAVED_PUBLIC = 8;
    // Secret key was saved (not exclusive with public!)
    public static final int SAVED_SECRET = 16;

    public boolean updated() {
        return (mResult & UPDATED) == UPDATED;
    }

    public SaveKeyringResult(Parcel source) {
        super(source);
        savedMasterKeyId = source.readInt() != 0 ? source.readLong() : null;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        if (savedMasterKeyId != null) {
            dest.writeInt(1);
            dest.writeLong(savedMasterKeyId);
        } else {
            dest.writeInt(0);
        }
    }

    public static Creator<SaveKeyringResult> CREATOR = new Creator<SaveKeyringResult>() {
        public SaveKeyringResult createFromParcel(final Parcel source) {
            return new SaveKeyringResult(source);
        }

        public SaveKeyringResult[] newArray(final int size) {
            return new SaveKeyringResult[size];
        }
    };
}
