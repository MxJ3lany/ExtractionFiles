package org.spongycastle.tls;

public interface TlsServerCertificate
{
    Certificate getCertificate();

    CertificateStatus getCertificateStatus();
}
