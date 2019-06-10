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

package org.sufficientlysecure.keychain.service;


import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import org.sufficientlysecure.keychain.keyimport.HkpKeyserverAddress;


@AutoValue
public abstract class UploadKeyringParcel implements Parcelable {
    public abstract HkpKeyserverAddress getKeyserver();
    @Nullable
    public abstract Long getMasterKeyId();
    @Nullable
    @SuppressWarnings("mutable")
    public abstract byte[] getUncachedKeyringBytes();


    public static UploadKeyringParcel createWithKeyId(HkpKeyserverAddress keyserver, long masterKeyId) {
        return new AutoValue_UploadKeyringParcel(keyserver, masterKeyId, null);
    }

    public static UploadKeyringParcel createWithKeyringBytes(HkpKeyserverAddress keyserver,
            @NonNull byte[] uncachedKeyringBytes) {
        return new AutoValue_UploadKeyringParcel(keyserver, null, uncachedKeyringBytes);
    }
}