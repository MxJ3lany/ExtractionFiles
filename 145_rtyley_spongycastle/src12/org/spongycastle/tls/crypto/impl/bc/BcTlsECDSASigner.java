package org.spongycastle.tls.crypto.impl.bc;

import org.spongycastle.crypto.DSA;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.crypto.signers.HMacDSAKCalculator;
import org.spongycastle.tls.SignatureAlgorithm;

/**
 * Implementation class for generation of the raw ECDSA signature type using the BC light-weight API.
 */
public class BcTlsECDSASigner
    extends BcTlsDSSSigner
{
    public BcTlsECDSASigner(BcTlsCrypto crypto, ECPrivateKeyParameters privateKey)
    {
        super(crypto, privateKey);
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
