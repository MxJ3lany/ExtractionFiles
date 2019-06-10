package org.spongycastle.tls.crypto.impl.bc;

import org.spongycastle.crypto.DSA;
import org.spongycastle.crypto.Signer;
import org.spongycastle.crypto.params.AsymmetricKeyParameter;
import org.spongycastle.crypto.signers.DSADigestSigner;
import org.spongycastle.tls.DigitallySigned;
import org.spongycastle.tls.HashAlgorithm;
import org.spongycastle.tls.SignatureAndHashAlgorithm;

/**
 * BC light-weight base class for the verifiers supporting the two DSA style algorithms from FIPS PUB 186-4: DSA and ECDSA.
 */
public abstract class BcTlsDSSVerifier
    extends BcTlsVerifier
{
    protected BcTlsDSSVerifier(BcTlsCrypto crypto, AsymmetricKeyParameter publicKey)
    {
        super(crypto, publicKey);
    }

    protected abstract DSA createDSAImpl(short hashAlgorithm);

    protected abstract short getSignatureAlgorithm();

    public boolean verifyRawSignature(DigitallySigned signedParams, byte[] hash)
    {
        SignatureAndHashAlgorithm algorithm = signedParams.getAlgorithm();
        if (algorithm != null && algorithm.getSignature() != getSignatureAlgorithm())
        {
            throw new IllegalStateException();
        }

        short hashAlgorithm = algorithm == null ? HashAlgorithm.sha1 : algorithm.getHash();

        Signer signer = new DSADigestSigner(createDSAImpl(hashAlgorithm), crypto.createDigest(HashAlgorithm.none));
        signer.init(false, publicKey);
        if (algorithm == null)
        {
            // Note: Only use the SHA1 part of the (MD5/SHA1) hash
            signer.update(hash, 16, 20);
        }
        else
        {
            signer.update(hash, 0, hash.length);
        }
        return signer.verifySignature(signedParams.getSignature());
    }
}
