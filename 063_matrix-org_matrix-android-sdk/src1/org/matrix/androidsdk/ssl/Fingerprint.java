/*
 * Copyright 2016 OpenMarket Ltd
 * Copyright 2018 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.matrix.androidsdk.ssl;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * Represents a X509 Certificate fingerprint.
 */
public class Fingerprint {
    public enum HashType {
        SHA1,
        SHA256
    }

    private final HashType mHashType;
    private final byte[] mBytes;
    private String mDisplayableHexRepr;

    public Fingerprint(HashType hashType, byte[] bytes) {
        mHashType = hashType;
        mBytes = bytes;
        mDisplayableHexRepr = null;
    }

    public static Fingerprint newSha256Fingerprint(X509Certificate cert) throws CertificateException {
        return new Fingerprint(HashType.SHA256, CertUtil.generateSha256Fingerprint(cert));
    }

    public static Fingerprint newSha1Fingerprint(X509Certificate cert) throws CertificateException {
        return new Fingerprint(HashType.SHA1, CertUtil.generateSha1Fingerprint(cert));
    }

    public HashType getType() {
        return mHashType;
    }

    public byte[] getBytes() {
        return mBytes;
    }

    public String getBytesAsHexString() {
        if (mDisplayableHexRepr == null) {
            mDisplayableHexRepr = CertUtil.fingerprintToHexString(mBytes);
        }

        return mDisplayableHexRepr;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("bytes", Base64.encodeToString(getBytes(), Base64.DEFAULT));
        obj.put("hash_type", mHashType.toString());
        return obj;
    }

    public static Fingerprint fromJson(JSONObject obj) throws JSONException {
        String hashTypeStr = obj.getString("hash_type");
        byte[] fingerprintBytes = Base64.decode(obj.getString("bytes"), Base64.DEFAULT);

        final HashType hashType;
        if ("SHA256".equalsIgnoreCase(hashTypeStr)) {
            hashType = HashType.SHA256;
        } else if ("SHA1".equalsIgnoreCase(hashTypeStr)) {
            hashType = HashType.SHA1;
        } else {
            throw new JSONException("Unrecognized hash type: " + hashTypeStr);
        }

        return new Fingerprint(hashType, fingerprintBytes);
    }

    public boolean matchesCert(X509Certificate cert) throws CertificateException {
        Fingerprint o = null;
        switch (mHashType) {
            case SHA256:
                o = Fingerprint.newSha256Fingerprint(cert);
                break;
            case SHA1:
                o = Fingerprint.newSha1Fingerprint(cert);
                break;
        }

        return equals(o);
    }

    public String toString() {
        return String.format("Fingerprint{type: '%s', fingeprint: '%s'}", mHashType.toString(), getBytesAsHexString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Fingerprint that = (Fingerprint) o;

        if (!Arrays.equals(mBytes, that.mBytes)) return false;
        return mHashType == that.mHashType;

    }

    @Override
    public int hashCode() {
        int result = mBytes != null ? Arrays.hashCode(mBytes) : 0;
        result = 31 * result + (mHashType != null ? mHashType.hashCode() : 0);
        return result;
    }
}
