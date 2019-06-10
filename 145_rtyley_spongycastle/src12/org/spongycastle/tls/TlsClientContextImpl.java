package org.spongycastle.tls;

import org.spongycastle.tls.crypto.TlsCrypto;

class TlsClientContextImpl
    extends AbstractTlsContext
    implements TlsClientContext
{
    TlsClientContextImpl(TlsCrypto crypto, SecurityParameters securityParameters)
    {
        super(crypto, securityParameters);
    }

    public boolean isServer()
    {
        return false;
    }
}
