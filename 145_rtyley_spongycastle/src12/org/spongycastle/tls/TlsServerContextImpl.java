package org.spongycastle.tls;

import org.spongycastle.tls.crypto.TlsCrypto;

class TlsServerContextImpl
    extends AbstractTlsContext
    implements TlsServerContext
{
    TlsServerContextImpl(TlsCrypto crypto, SecurityParameters securityParameters)
    {
        super(crypto, securityParameters);
    }

    public boolean isServer()
    {
        return true;
    }
}
