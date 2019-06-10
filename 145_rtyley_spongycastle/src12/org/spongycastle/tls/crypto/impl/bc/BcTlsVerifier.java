package org.spongycastle.tls.crypto.impl.bc;

import org.spongycastle.crypto.params.AsymmetricKeyParameter;
import org.spongycastle.tls.DigitallySigned;
import org.spongycastle.tls.crypto.TlsStreamVerifier;
import org.spongycastle.tls.crypto.TlsVerifier;

public abstract class BcTlsVerifier
    implements TlsVerifier
{
    protected final BcTlsCrypto crypto;
    protected final AsymmetricKeyParameter publicKey;

    protected BcTlsVerifier(BcTlsCrypto crypto, AsymmetricKeyParameter publicKey)
    {
        if (crypto == null)
        {
            throw new NullPointerException("'crypto' cannot be null");
        }
        if (publicKey == null)
        {
            throw new NullPointerException("'publicKey' cannot be null");
        }
        if (publicKey.isPrivate())
        {
            throw new IllegalArgumentException("'publicKey' must be public");
        }

        this.crypto = crypto;
        this.publicKey = publicKey;
    }

    public TlsStreamVerifier getStreamVerifier(DigitallySigned signature)
    {
        return null;
    }
}
