package org.spongycastle.jsse;

import org.spongycastle.tls.TlsUtils;

public abstract class BCSNIMatcher
{
    private final int nameType;

    protected BCSNIMatcher(int nameType)
    {
        if (!TlsUtils.isValidUint8(nameType))
        {
            throw new IllegalArgumentException("'nameType' should be between 0 and 255");
        }

        this.nameType = nameType;
    }

    public final int getType()
    {
        return nameType;
    }

    public abstract boolean matches(BCSNIServerName serverName);
}
