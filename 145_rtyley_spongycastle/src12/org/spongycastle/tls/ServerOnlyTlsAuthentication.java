package org.spongycastle.tls;

public abstract class ServerOnlyTlsAuthentication
    implements TlsAuthentication
{
    public final TlsCredentials getClientCredentials(CertificateRequest certificateRequest)
    {
        return null;
    }
}
