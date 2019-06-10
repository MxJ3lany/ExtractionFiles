package org.spongycastle.tls.crypto;

import org.spongycastle.tls.ProtocolVersion;
import org.spongycastle.tls.SecurityParameters;
import org.spongycastle.tls.TlsContext;

/**
 * Carrier class for context related parameters needed for creating secrets and cipher suites,
 */
public class TlsCryptoParameters
{
    private final TlsContext context;

    /**
     * Base constructor.
     *
     * @param context the context for this parameters object.
     */
    public TlsCryptoParameters(TlsContext context)
    {
        this.context = context;
    }

    public SecurityParameters getSecurityParameters()
    {
        return context.getSecurityParameters();
    }

    public ProtocolVersion getClientVersion()
    {
        return context.getClientVersion();
    }

    public ProtocolVersion getServerVersion()
    {
        return context.getServerVersion();
    }

    public boolean isServer()
    {
        return context.isServer();
    }

    public TlsNonceGenerator getNonceGenerator()
    {
        return context.getNonceGenerator();
    }
}
