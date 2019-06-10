package org.spongycastle.tls.crypto.impl.jcajce;

import java.security.PrivateKey;

import org.spongycastle.tls.SignatureAlgorithm;

/**
 * Implementation class for generation of the raw ECDSA signature type using the JCA.
 */
public class JcaTlsECDSASigner
    extends JcaTlsDSSSigner
{
    public JcaTlsECDSASigner(JcaTlsCrypto crypto, PrivateKey privateKey)
    {
        super(crypto, privateKey, SignatureAlgorithm.ecdsa, "NoneWithECDSA");
    }
}
