package org.spongycastle.tls.crypto.impl.bc;

import java.io.IOException;

import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.params.DHPrivateKeyParameters;
import org.spongycastle.crypto.params.DHPublicKeyParameters;
import org.spongycastle.tls.crypto.TlsAgreement;
import org.spongycastle.tls.crypto.TlsSecret;

/**
 * Support class for ephemeral Diffie-Hellman using the BC light-weight library.
 */
public class BcTlsDH implements TlsAgreement
{
    protected final BcTlsDHDomain domain;

    protected AsymmetricCipherKeyPair localKeyPair;
    protected DHPublicKeyParameters peerPublicKey;

    public BcTlsDH(BcTlsDHDomain domain)
    {
        this.domain = domain;
    }

    public byte[] generateEphemeral() throws IOException
    {
        this.localKeyPair = domain.generateKeyPair();

        return domain.encodePublicKey((DHPublicKeyParameters)localKeyPair.getPublic());
    }

    public void receivePeerValue(byte[] peerValue) throws IOException
    {
        this.peerPublicKey = domain.decodePublicKey(peerValue);
    }

    public TlsSecret calculateSecret() throws IOException
    {
        return domain.calculateDHAgreement((DHPrivateKeyParameters)localKeyPair.getPrivate(), peerPublicKey);
    }
}
