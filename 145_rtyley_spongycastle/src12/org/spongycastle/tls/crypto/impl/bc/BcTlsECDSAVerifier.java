package org.spongycastle.tls.crypto.impl.bc;

import org.spongycastle.crypto.DSA;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.crypto.signers.HMacDSAKCalculator;
import org.spongycastle.tls.SignatureAlgorithm;

/**
 * Implementation class for the verification of the raw ECDSA signature type using the BC light-weight API.
 */
public class BcTlsECDSAVerifier
    extends BcTlsDSSVerifier
{
    public BcTlsECDSAVerifier(BcTlsCrypto crypto, ECPublicKeyParameters publicKey)
    {
        super(crypto, publicKey);
    }

    protected DSA createDSAImpl(short hashAlgorithm)
    {
        return new ECDSASigner(new HMacDSAKCalculator(crypto.createDigest(hashAlgorithm)));
    }

    protected short getSignatureAlgorithm()
    {
        return SignatureAlgorithm.ecdsa;
    }
}
