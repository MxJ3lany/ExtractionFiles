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

package org.sufficientlysecure.keychain.service.input;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

import org.sufficientlysecure.keychain.util.Passphrase;


public class RequiredInputParcel implements Parcelable {

    public boolean hasPassphraseBegin() {
        return mInputData != null && mInputData.length == 1 && mInputData[0].length == 2;
    }

    public String getPassphraseBegin() {
        return new String(mInputData[0]);
    }

    public enum RequiredInputType {
        PASSPHRASE, PASSPHRASE_SYMMETRIC, PASSPHRASE_AUTH,
        BACKUP_CODE, NUMERIC_9X4, NUMERIC_9X4_AUTOCRYPT,
        SECURITY_TOKEN_SIGN, SECURITY_TOKEN_AUTH, SECURITY_TOKEN_DECRYPT,
        SECURITY_TOKEN_MOVE_KEY_TO_CARD, SECURITY_TOKEN_RESET_CARD,
        ENABLE_ORBOT, UPLOAD_FAIL_RETRY
    }

    public Date mSignatureTime;

    public final RequiredInputType mType;

    public final byte[][] mInputData;
    public final int[] mSignAlgos;

    private long[] mMasterKeyIds;
    private long[] mSubKeyIds;

    public boolean mSkipCaching = false;

    private RequiredInputParcel(RequiredInputType type, byte[][] inputData,
            int[] signAlgos, Date signatureTime, long[] masterKeyIds, long[] subKeyIds) {
        mType = type;
        mInputData = inputData;
        mSignAlgos = signAlgos;
        mSignatureTime = signatureTime;
        mMasterKeyIds = masterKeyIds;
        mSubKeyIds = subKeyIds;
    }

    private RequiredInputParcel(RequiredInputType type, byte[][] inputData,
            int[] signAlgos, Date signatureTime, Long masterKeyId, Long subKeyId) {
        this(type, inputData, signAlgos, signatureTime, masterKeyId != null ? new long[] { masterKeyId } : null,
                subKeyId != null ? new long[] { subKeyId } : null);
    }

    public RequiredInputParcel(Parcel source) {
        mType = RequiredInputType.values()[source.readInt()];

        // 0 = none, 1 = signAlgos + inputData, 2 = only inputData (decrypt)
        int inputDataType = source.readInt();
        if (inputDataType != 0) {
            int count = source.readInt();
            mInputData = new byte[count][];
            if (inputDataType == 1) {
                mSignAlgos = new int[count];
                for (int i = 0; i < count; i++) {
                    mInputData[i] = source.createByteArray();
                    mSignAlgos[i] = source.readInt();
                }
            } else {
                mSignAlgos = null;
                for (int i = 0; i < count; i++) {
                    mInputData[i] = source.createByteArray();
                }
            }
        } else {
            mInputData = null;
            mSignAlgos = null;
        }

        mSignatureTime = source.readInt() != 0 ? new Date(source.readLong()) : null;
        mMasterKeyIds = source.readInt() != 0 ? source.createLongArray() : null;
        mSubKeyIds = source.readInt() != 0 ? source.createLongArray() : null;
        mSkipCaching = source.readInt() != 0;

    }

    public Long getMasterKeyId() {
        return mMasterKeyIds == null ? null : mMasterKeyIds[0];
    }

    public Long getSubKeyId() {
        return mSubKeyIds == null ? null : mSubKeyIds[0];
    }

    public long[] getMasterKeyIds() {
        return mMasterKeyIds;
    }

    public long[] getSubKeyIds() {
        return mSubKeyIds;
    }

    public static RequiredInputParcel createRetryUploadOperation() {
        return new RequiredInputParcel(RequiredInputType.UPLOAD_FAIL_RETRY,
                null, null, null, 0L, 0L);
    }

    public static RequiredInputParcel createOrbotRequiredOperation() {
        return new RequiredInputParcel(RequiredInputType.ENABLE_ORBOT, null, null, null, 0L, 0L);
    }

    public static RequiredInputParcel createSecurityTokenSignOperation(
            long masterKeyId, long subKeyId,
            byte[] inputHash, int signAlgo, Date signatureTime) {
        return new RequiredInputParcel(RequiredInputType.SECURITY_TOKEN_SIGN,
                new byte[][] { inputHash }, new int[] { signAlgo },
                signatureTime, masterKeyId, subKeyId);
    }

    public static RequiredInputParcel createSecurityTokenAuthenticationOperation(
            long masterKeyId, long subKeyId,
            byte[] inputHash, int signAlgo) {
        return new RequiredInputParcel(RequiredInputType.SECURITY_TOKEN_AUTH,
                new byte[][] { inputHash }, new int[] { signAlgo },
                null, masterKeyId, subKeyId);
    }

    public static RequiredInputParcel createSecurityTokenDecryptOperation(
            long masterKeyId, long subKeyId, byte[] encryptedSessionKey) {
        return new RequiredInputParcel(RequiredInputType.SECURITY_TOKEN_DECRYPT,
                new byte[][] { encryptedSessionKey }, null, null, masterKeyId, subKeyId);
    }

    public static RequiredInputParcel createSecurityTokenReset() {
        return new RequiredInputParcel(RequiredInputType.SECURITY_TOKEN_RESET_CARD,
                null, null, null, (long[]) null, null);
    }

    public static RequiredInputParcel createRequiredAuthenticationPassphrase(
            long masterKeyId, long subKeyId) {
        return new RequiredInputParcel(RequiredInputType.PASSPHRASE_AUTH,
                null, null, null, masterKeyId, subKeyId);
    }

    public static RequiredInputParcel createRequiredSignPassphrase(
            long masterKeyId, long subKeyId, Date signatureTime) {
        return new RequiredInputParcel(RequiredInputType.PASSPHRASE,
                null, null, signatureTime, masterKeyId, subKeyId);
    }

    public static RequiredInputParcel createRequiredDecryptPassphrase(
            long masterKeyId, long subKeyId) {
        return new RequiredInputParcel(RequiredInputType.PASSPHRASE,
                null, null, null, masterKeyId, subKeyId);
    }

    public static RequiredInputParcel createRequiredSymmetricPassphrase() {
        return new RequiredInputParcel(RequiredInputType.PASSPHRASE_SYMMETRIC,
                null, null, null, (long[]) null, null);
    }

    public static RequiredInputParcel createRequiredBackupCode() {
        return new RequiredInputParcel(RequiredInputType.BACKUP_CODE,
                null, null, null, (long[]) null, null);
    }

    public static RequiredInputParcel createRequiredNumeric9x4(String beginChars) {
        byte[][] inputData = beginChars != null ? new byte[][] { beginChars.getBytes() } : null;
        return new RequiredInputParcel(RequiredInputType.NUMERIC_9X4,
                inputData, null, null, (long[]) null, null);
    }

    public static RequiredInputParcel createRequiredNumeric9x4Autocrypt(String beginChars) {
        byte[][] inputData = beginChars != null ? new byte[][] { beginChars.getBytes() } : null;
        return new RequiredInputParcel(RequiredInputType.NUMERIC_9X4_AUTOCRYPT,
                inputData, null, null, (long[]) null, null);
    }

    public static RequiredInputParcel createRequiredPassphrase(
            RequiredInputParcel req) {
        return new RequiredInputParcel(RequiredInputType.PASSPHRASE,
                null, null, req.mSignatureTime, req.mMasterKeyIds, req.mSubKeyIds);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mType.ordinal());
        if (mInputData != null) {
            dest.writeInt(mSignAlgos != null ? 1 : 2);
            dest.writeInt(mInputData.length);
            for (int i = 0; i < mInputData.length; i++) {
                dest.writeByteArray(mInputData[i]);
                if (mSignAlgos != null) {
                    dest.writeInt(mSignAlgos[i]);
                }
            }
        } else {
            dest.writeInt(0);
        }
        if (mSignatureTime != null) {
            dest.writeInt(1);
            dest.writeLong(mSignatureTime.getTime());
        } else {
            dest.writeInt(0);
        }
        if (mMasterKeyIds != null) {
            dest.writeInt(1);
            dest.writeLongArray(mMasterKeyIds);
        } else {
            dest.writeInt(0);
        }
        if (mSubKeyIds != null) {
            dest.writeInt(1);
            dest.writeLongArray(mSubKeyIds);
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(mSkipCaching ? 1 : 0);

    }

    public static final Creator<RequiredInputParcel> CREATOR = new Creator<RequiredInputParcel>() {
        public RequiredInputParcel createFromParcel(final Parcel source) {
            return new RequiredInputParcel(source);
        }

        public RequiredInputParcel[] newArray(final int size) {
            return new RequiredInputParcel[size];
        }
    };

    public static class SecurityTokenSignOperationsBuilder {
        Date mSignatureTime;
        ArrayList<Integer> mSignAlgos = new ArrayList<>();
        ArrayList<byte[]> mInputHashes = new ArrayList<>();
        long mMasterKeyId;
        long mSubKeyId;

        public SecurityTokenSignOperationsBuilder(Date signatureTime, long masterKeyId, long subKeyId) {
            mSignatureTime = signatureTime;
            mMasterKeyId = masterKeyId;
            mSubKeyId = subKeyId;
        }

        public RequiredInputParcel build() {
            byte[][] inputHashes = new byte[mInputHashes.size()][];
            mInputHashes.toArray(inputHashes);
            int[] signAlgos = new int[mSignAlgos.size()];
            for (int i = 0; i < mSignAlgos.size(); i++) {
                signAlgos[i] = mSignAlgos.get(i);
            }

            return new RequiredInputParcel(RequiredInputType.SECURITY_TOKEN_SIGN,
                    inputHashes, signAlgos, mSignatureTime, mMasterKeyId, mSubKeyId);
        }

        public void addHash(byte[] hash, int algo) {
            mInputHashes.add(hash);
            mSignAlgos.add(algo);
        }

        public void addAll(RequiredInputParcel input) {
            if (!mSignatureTime.equals(input.mSignatureTime)) {
                throw new AssertionError("input times must match, this is a programming error!");
            }
            if (input.mType != RequiredInputType.SECURITY_TOKEN_SIGN) {
                throw new AssertionError("operation types must match, this is a progrmming error!");
            }

            Collections.addAll(mInputHashes, input.mInputData);
            for (int signAlgo : input.mSignAlgos) {
                mSignAlgos.add(signAlgo);
            }
        }

        public boolean isEmpty() {
            return mInputHashes.isEmpty();
        }

    }

    public static class SecurityTokenKeyToCardOperationsBuilder {
        ArrayList<byte[]> mSubkeysToExport = new ArrayList<>();
        Long mMasterKeyId;
        byte[] mPin;
        byte[] mAdminPin;

        public SecurityTokenKeyToCardOperationsBuilder(Long masterKeyId) {
            mMasterKeyId = masterKeyId;
        }

        public RequiredInputParcel build() {
            byte[][] inputData = new byte[mSubkeysToExport.size() + 2][];

            // encode all subkeys into inputData
            byte[][] subkeyData = new byte[mSubkeysToExport.size()][];
            mSubkeysToExport.toArray(subkeyData);

            // first two are PINs
            inputData[0] = mPin;
            inputData[1] = mAdminPin;
            // then subkeys
            System.arraycopy(subkeyData, 0, inputData, 2, subkeyData.length);

            ByteBuffer buf = ByteBuffer.wrap(mSubkeysToExport.get(0));

            // We need to pass in a subkey here...
            return new RequiredInputParcel(RequiredInputType.SECURITY_TOKEN_MOVE_KEY_TO_CARD,
                    inputData, null, null, mMasterKeyId, buf.getLong());
        }

        public void addSubkey(long subkeyId) {
            byte[] subKeyId = new byte[8];
            ByteBuffer buf = ByteBuffer.wrap(subKeyId);
            buf.putLong(subkeyId).rewind();
            mSubkeysToExport.add(subKeyId);
        }

        public void setPin(Passphrase pin) {
            mPin = pin.toStringUnsafe().getBytes();
        }

        public void setAdminPin(Passphrase adminPin) {
            mAdminPin = adminPin.toStringUnsafe().getBytes();
        }

        public void addAll(RequiredInputParcel input) {
            if (!mMasterKeyId.equals(input.mMasterKeyIds)) {
                throw new AssertionError("Master keys must match, this is a programming error!");
            }
            if (input.mType != RequiredInputType.SECURITY_TOKEN_MOVE_KEY_TO_CARD) {
                throw new AssertionError("Operation types must match, this is a programming error!");
            }

            Collections.addAll(mSubkeysToExport, input.mInputData);
        }

        public boolean isEmpty() {
            return mSubkeysToExport.isEmpty();
        }

    }

    public static class RequireAnyDecryptPassphraseBuilder {
        private final ArrayList<Long> masterKeyIds = new ArrayList<>();
        private final ArrayList<Long> subKeyIds = new ArrayList<>();

        public RequiredInputParcel build() {
            int numIds = masterKeyIds.size();
            long[] masterKeyIdsArr = new long[numIds];
            long[] subKeyIdsArr = new long[numIds];
            for (int i = 0; i < numIds; i++) {
                masterKeyIdsArr[i] = masterKeyIds.get(i);
                subKeyIdsArr[i] = subKeyIds.get(i);
            }

            return new RequiredInputParcel(RequiredInputType.PASSPHRASE,
                    null, null, null, masterKeyIdsArr, subKeyIdsArr);
        }

        public void add(long masterKeyId, long subKeyId) {
            masterKeyIds.add(masterKeyId);
            subKeyIds.add(subKeyId);
        }

        public boolean isEmpty() {
            return masterKeyIds.isEmpty();
        }
    }
}
