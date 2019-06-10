package org.spongycastle.tls.crypto.impl.jcajce;

import java.io.IOException;
import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

import org.spongycastle.tls.crypto.TlsAgreement;
import org.spongycastle.tls.crypto.TlsSecret;

/**
 * Support class for ephemeral Elliptic Curve Diffie-Hellman using the JCE.
 */
public class JceTlsECDH
    implements TlsAgreement
{
    protected final JceTlsECDomain domain;

    protected KeyPair localKeyPair;
    protected ECPublicKey peerPublicKey;

    public JceTlsECDH(JceTlsECDomain domain)
    {
        this.domain = domain;
    }

    public byte[] generateEphemeral() throws IOException
    {
        this.localKeyPair = domain.generateKeyPair();

        return domain.encodePublicKey((ECPublicKey)localKeyPair.getPublic());
    }

    public void receivePeerValue(byte[] peerValue) throws IOException
    {
        this.peerPublicKey = domain.decodePublicKey(peerValue);
    }

    public TlsSecret calculateSecret() throws IOException
    {
        return domain.calculateECDHAgreement((ECPrivateKey)localKeyPair.getPrivate(), peerPublicKey);
    }
}
