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

package org.sufficientlysecure.keychain.operations;


import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.support.annotation.NonNull;

import org.sufficientlysecure.keychain.daos.KeyMetadataDao;
import org.sufficientlysecure.keychain.daos.KeyRepository.NotFoundException;
import org.sufficientlysecure.keychain.daos.KeyWritableRepository;
import org.sufficientlysecure.keychain.operations.results.CertifyResult;
import org.sufficientlysecure.keychain.operations.results.OperationResult.LogType;
import org.sufficientlysecure.keychain.operations.results.OperationResult.OperationLog;
import org.sufficientlysecure.keychain.operations.results.SaveKeyringResult;
import org.sufficientlysecure.keychain.operations.results.UploadResult;
import org.sufficientlysecure.keychain.pgp.CanonicalizedPublicKeyRing;
import org.sufficientlysecure.keychain.pgp.CanonicalizedSecretKey;
import org.sufficientlysecure.keychain.pgp.CanonicalizedSecretKeyRing;
import org.sufficientlysecure.keychain.pgp.PassphraseCacheInterface;
import org.sufficientlysecure.keychain.pgp.PgpCertifyOperation;
import org.sufficientlysecure.keychain.pgp.PgpCertifyOperation.PgpCertifyResult;
import org.sufficientlysecure.keychain.pgp.Progressable;
import org.sufficientlysecure.keychain.pgp.UncachedKeyRing;
import org.sufficientlysecure.keychain.pgp.exception.PgpGeneralException;
import org.sufficientlysecure.keychain.service.CertifyActionsParcel;
import org.sufficientlysecure.keychain.service.CertifyActionsParcel.CertifyAction;
import org.sufficientlysecure.keychain.service.ContactSyncAdapterService;
import org.sufficientlysecure.keychain.service.UploadKeyringParcel;
import org.sufficientlysecure.keychain.service.input.CryptoInputParcel;
import org.sufficientlysecure.keychain.service.input.RequiredInputParcel;
import org.sufficientlysecure.keychain.service.input.RequiredInputParcel.SecurityTokenSignOperationsBuilder;
import org.sufficientlysecure.keychain.ui.util.KeyFormattingUtils;
import org.sufficientlysecure.keychain.util.Passphrase;

/**
 * An operation which implements a high level user id certification operation.
 * <p/>
 * This operation takes a specific CertifyActionsParcel as its input. These
 * contain a masterKeyId to be used for certification, and a list of
 * masterKeyIds and related user ids to certify.
 *
 * @see CertifyActionsParcel
 */
public class CertifyOperation extends BaseReadWriteOperation<CertifyActionsParcel> {
    private final KeyMetadataDao keyMetadataDao;

    public CertifyOperation(Context context, KeyWritableRepository keyWritableRepository, Progressable progressable,
            AtomicBoolean cancelled) {
        super(context, keyWritableRepository, progressable, cancelled);

        this.keyMetadataDao = KeyMetadataDao.create(context);
    }

    @NonNull
    @Override
    public CertifyResult execute(CertifyActionsParcel parcel, CryptoInputParcel cryptoInput) {

        OperationLog log = new OperationLog();
        log.add(LogType.MSG_CRT, 0);

        // Retrieve and unlock secret key
        CanonicalizedSecretKey certificationKey;
        long masterKeyId = parcel.getMasterKeyId();
        try {

            log.add(LogType.MSG_CRT_MASTER_FETCH, 1);

            Passphrase passphrase;
            switch (mKeyRepository.getSecretKeyType(masterKeyId)) {
                case PASSPHRASE:
                    passphrase = cryptoInput.getPassphrase();
                    if (passphrase == null) {
                        try {
                            passphrase = getCachedPassphrase(masterKeyId, masterKeyId);
                        } catch (PassphraseCacheInterface.NoSecretKeyException ignored) {
                            // treat as a cache miss for error handling purposes
                        }
                    }

                    if (passphrase == null) {
                        return new CertifyResult(log,
                                RequiredInputParcel.createRequiredSignPassphrase(masterKeyId, masterKeyId, null),
                                cryptoInput
                        );
                    }
                    break;

                case PASSPHRASE_EMPTY:
                    passphrase = new Passphrase("");
                    break;

                case DIVERT_TO_CARD:
                    // the unlock operation will succeed for passphrase == null in a divertToCard key
                    passphrase = null;
                    break;

                default:
                    log.add(LogType.MSG_CRT_ERROR_UNLOCK, 2);
                    return new CertifyResult(CertifyResult.RESULT_ERROR, log);
            }

            // Get actual secret key
            CanonicalizedSecretKeyRing secretKeyRing =
                    mKeyRepository.getCanonicalizedSecretKeyRing(parcel.getMasterKeyId());
            certificationKey = secretKeyRing.getSecretKey();

            log.add(LogType.MSG_CRT_UNLOCK, 1);
            boolean unlockSuccessful = certificationKey.unlock(passphrase);
            if (!unlockSuccessful) {
                log.add(LogType.MSG_CRT_ERROR_UNLOCK, 2);
                return new CertifyResult(CertifyResult.RESULT_ERROR, log);
            }
        } catch (PgpGeneralException e) {
            log.add(LogType.MSG_CRT_ERROR_UNLOCK, 2);
            return new CertifyResult(CertifyResult.RESULT_ERROR, log);
        } catch (NotFoundException e) {
            log.add(LogType.MSG_CRT_ERROR_MASTER_NOT_FOUND, 2);
            return new CertifyResult(CertifyResult.RESULT_ERROR, log);
        }

        ArrayList<UncachedKeyRing> certifiedKeys = new ArrayList<>();

        log.add(LogType.MSG_CRT_CERTIFYING, 1);

        int certifyOk = 0, certifyError = 0, uploadOk = 0, uploadError = 0;

        SecurityTokenSignOperationsBuilder allRequiredInput = new SecurityTokenSignOperationsBuilder(
                cryptoInput.getSignatureTime(), masterKeyId, masterKeyId);

        // Work through all requested certifications
        for (CertifyAction action : parcel.getCertifyActions()) {

            // Check if we were cancelled
            if (checkCancelled()) {
                log.add(LogType.MSG_OPERATION_CANCELLED, 0);
                return new CertifyResult(CertifyResult.RESULT_CANCELLED, log);
            }

            try {

                if (action.getMasterKeyId() == parcel.getMasterKeyId()) {
                    log.add(LogType.MSG_CRT_ERROR_SELF, 2);
                    certifyError += 1;
                    continue;
                }

                CanonicalizedPublicKeyRing publicRing =
                        mKeyRepository.getCanonicalizedPublicKeyRing(action.getMasterKeyId());

                PgpCertifyOperation op = new PgpCertifyOperation();
                PgpCertifyResult result = op.certify(certificationKey, publicRing,
                        log, 2, action, cryptoInput.getCryptoData(), cryptoInput.getSignatureTime());

                if (!result.success()) {
                    certifyError += 1;
                    continue;
                }
                if (result.nfcInputRequired()) {
                    RequiredInputParcel requiredInput = result.getRequiredInput();
                    allRequiredInput.addAll(requiredInput);
                    continue;
                }

                certifiedKeys.add(result.getCertifiedRing());

            } catch (NotFoundException e) {
                certifyError += 1;
                log.add(LogType.MSG_CRT_WARN_NOT_FOUND, 3);
            }

        }

        if (!allRequiredInput.isEmpty()) {
            log.add(LogType.MSG_CRT_NFC_RETURN, 1);
            return new CertifyResult(log, allRequiredInput.build(), cryptoInput);
        }

        log.add(LogType.MSG_CRT_SAVING, 1);

        // Check if we were cancelled
        if (checkCancelled()) {
            log.add(LogType.MSG_OPERATION_CANCELLED, 0);
            return new CertifyResult(CertifyResult.RESULT_CANCELLED, log);
        }

        // these variables are used inside the following loop, but they need to be created only once
        UploadOperation uploadOperation = null;
        if (parcel.getParcelableKeyServer() != null) {
            uploadOperation = new UploadOperation(mContext, mKeyRepository, mProgressable, mCancelled);
        }

        // Write all certified keys into the database
        for (UncachedKeyRing certifiedKey : certifiedKeys) {

            // Check if we were cancelled
            if (checkCancelled()) {
                log.add(LogType.MSG_OPERATION_CANCELLED, 0);
                return new CertifyResult(CertifyResult.RESULT_CANCELLED, log, certifyOk, certifyError, uploadOk,
                        uploadError);
            }

            log.add(LogType.MSG_CRT_SAVE, 2,
                    KeyFormattingUtils.convertKeyIdToHex(certifiedKey.getMasterKeyId()));
            // store the signed key in our local cache
            mKeyRepository.clearLog();
            SaveKeyringResult result = mKeyWritableRepository.savePublicKeyRing(certifiedKey);

            if (uploadOperation != null) {
                UploadKeyringParcel uploadInput = UploadKeyringParcel.createWithKeyId(
                        parcel.getParcelableKeyServer(), certifiedKey.getMasterKeyId());
                UploadResult uploadResult = uploadOperation.execute(uploadInput, cryptoInput);
                log.add(uploadResult, 2);

                if (uploadResult.success()) {
                    keyMetadataDao.renewKeyLastUpdatedTime(certifiedKey.getMasterKeyId(), true);

                    uploadOk += 1;
                } else {
                    uploadError += 1;
                }
            }

            if (result.success()) {
                certifyOk += 1;
            } else {
                log.add(LogType.MSG_CRT_WARN_SAVE_FAILED, 3);
            }

            log.add(result, 2);
        }

        if (certifyOk == 0) {
            log.add(LogType.MSG_CRT_ERROR_NOTHING, 0);
            return new CertifyResult(CertifyResult.RESULT_ERROR, log, certifyOk, certifyError,
                    uploadOk, uploadError);
        }

        // since only verified keys are synced to contacts, we need to initiate a sync now
        ContactSyncAdapterService.requestContactsSync();

        log.add(LogType.MSG_CRT_SUCCESS, 0);
        if (uploadError != 0) {
            return new CertifyResult(CertifyResult.RESULT_WARNINGS, log, certifyOk, certifyError, uploadOk,
                    uploadError);
        } else {
            return new CertifyResult(CertifyResult.RESULT_OK, log, certifyOk, certifyError, uploadOk, uploadError);
        }

    }

}
