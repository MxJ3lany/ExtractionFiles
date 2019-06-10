package org.spongycastle.jsse.provider;

import org.spongycastle.tls.DefaultTlsDHConfigVerifier;
import org.spongycastle.tls.crypto.TlsDHConfig;

class ProvDHConfigVerifier
    extends DefaultTlsDHConfigVerifier
{
    private static final int provMinimumPrimeBits = PropertyUtils.getIntegerSystemProperty("org.spongycastle.jsse.client.dh.minimumPrimeBits", 2048, 1024, 16384);
    private static final boolean provUnrestrictedGroups = PropertyUtils.getBooleanSystemProperty("org.spongycastle.jsse.client.dh.unrestrictedGroups", false);

    ProvDHConfigVerifier()
    {
        super(provMinimumPrimeBits);
    }

    @Override
    protected boolean checkGroup(TlsDHConfig dhConfig)
    {
        return provUnrestrictedGroups || super.checkGroup(dhConfig);
    }
}
