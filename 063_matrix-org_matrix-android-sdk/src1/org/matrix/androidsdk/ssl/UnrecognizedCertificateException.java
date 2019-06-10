/*
 * Copyright 2016 OpenMarket Ltd
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

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Thrown when we are given a certificate that does match the certificate we were told to
 * expect.
 */
public class UnrecognizedCertificateException extends CertificateException {
    private final X509Certificate mCert;
    private final Fingerprint mFingerprint;

    public UnrecognizedCertificateException(X509Certificate cert, Fingerprint fingerprint, Throwable cause) {
        super("Unrecognized certificate with unknown fingerprint: " + cert.getSubjectDN(), cause);
        mCert = cert;
        mFingerprint = fingerprint;
    }

    public X509Certificate getCertificate() {
        return mCert;
    }

    public Fingerprint getFingerprint() {
        return mFingerprint;
    }
}
