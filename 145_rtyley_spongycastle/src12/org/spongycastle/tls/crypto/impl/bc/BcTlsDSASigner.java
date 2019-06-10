package org.spongycastle.tls.crypto.impl.bc;

import org.spongycastle.crypto.DSA;
import org.spongycastle.crypto.params.DSAPrivateKeyParameters;
import org.spongycastle.crypto.signers.DSASigner;
import org.spongycastle.crypto.signers.HMacDSAKCalculator;
import org.spongycastle.tls.SignatureAlgorithm;

/**
 * Implementation class for generation of the raw DSA signature type using the BC light-weight API.
 */
public class BcTlsDSASigner
    extends BcTlsDSSSigner
{
    public BcTlsDSASigner(BcTlsCrypto crypto, DSAPrivateKeyParameters privateKey)
    {
        super(crypto, privateKey);
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
