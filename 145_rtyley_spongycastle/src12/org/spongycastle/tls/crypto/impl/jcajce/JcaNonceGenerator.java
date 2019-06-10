package org.spongycastle.tls.crypto.impl.jcajce;

import java.security.SecureRandom;

import org.spongycastle.crypto.digests.SHA512Digest;
import org.spongycastle.crypto.prng.SP800SecureRandom;
import org.spongycastle.crypto.prng.SP800SecureRandomBuilder;
import org.spongycastle.tls.crypto.TlsNonceGenerator;

class JcaNonceGenerator
    implements TlsNonceGenerator
{
    private final SP800SecureRandom random;

    JcaNonceGenerator(SecureRandom entropySource, byte[] additionalData)
    {
        byte[] nonce = new byte[32];

        entropySource.nextBytes(nonce);

        this.random = new SP800SecureRandomBuilder(entropySource, false)
            .setPersonalizationString(additionalData)
            .buildHash(new SHA512Digest(), nonce, false);
    }

    public byte[] generateNonce(int size)
    {
        byte[] nonce = new byte[size];
        random.nextBytes(nonce);
        return nonce;
    }
}
