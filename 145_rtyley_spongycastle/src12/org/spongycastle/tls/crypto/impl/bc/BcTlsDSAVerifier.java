package org.spongycastle.tls.crypto.impl.bc;

import org.spongycastle.crypto.DSA;
import org.spongycastle.crypto.params.DSAPublicKeyParameters;
import org.spongycastle.crypto.signers.DSASigner;
import org.spongycastle.crypto.signers.HMacDSAKCalculator;
import org.spongycastle.tls.SignatureAlgorithm;

/**
 * Implementation class for the verification of the raw DSA signature type using the BC light-weight API.
 */
public class BcTlsDSAVerifier
    extends BcTlsDSSVerifier
{
    public BcTlsDSAVerifier(BcTlsCrypto crypto, DSAPublicKeyParameters publicKey)
    {
        super(crypto, publicKey);
    }

    protected DSA createDSAImpl(short hashAlgorithm)
    {
        return new DSASigner(new HMacDSAKCalculator(crypto.createDigest(hashAlgorithm)));
    }

    protected short getSignatureAlgorithm()
    {
        return SignatureAlgorithm.dsa;
    }
}
