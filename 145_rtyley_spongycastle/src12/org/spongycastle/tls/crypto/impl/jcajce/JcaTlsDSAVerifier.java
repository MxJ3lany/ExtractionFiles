package org.spongycastle.tls.crypto.impl.jcajce;

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.interfaces.DSAPublicKey;

import org.spongycastle.jcajce.util.JcaJceHelper;
import org.spongycastle.tls.DigitallySigned;
import org.spongycastle.tls.SignatureAlgorithm;
import org.spongycastle.tls.SignatureAndHashAlgorithm;
import org.spongycastle.tls.crypto.TlsStreamVerifier;
import org.spongycastle.tls.crypto.TlsVerifier;

/**
 * Implementation class for the verification of the raw DSA signature type using the JCA.
 */
public class JcaTlsDSAVerifier
    implements TlsVerifier
{
    private final JcaJceHelper helper;
    private final DSAPublicKey pubKey;

    protected JcaTlsDSAVerifier(DSAPublicKey pubKey, JcaJceHelper helper)
    {
        if (pubKey == null)
        {
            throw new IllegalArgumentException("'pubKey' cannot be null");
        }

        this.pubKey = pubKey;
        this.helper = helper;
    }

    public TlsStreamVerifier getStreamVerifier(DigitallySigned signature)
    {
        return null;
    }

    public boolean verifyRawSignature(DigitallySigned signedParams, byte[] hash)
    {
        SignatureAndHashAlgorithm algorithm = signedParams.getAlgorithm();
        if (algorithm != null && algorithm.getSignature() != SignatureAlgorithm.dsa)
        {
            throw new IllegalStateException();
        }

        try
        {
            Signature signer = helper.createSignature("NoneWithDSA");

            signer.initVerify(pubKey);
            if (algorithm == null)
            {
                // Note: Only use the SHA1 part of the (MD5/SHA1) hash
                signer.update(hash, 16, 20);
            }
            else
            {
                signer.update(hash, 0, hash.length);
            }
            return signer.verify(signedParams.getSignature());
        }
        catch (GeneralSecurityException e)
        {
            throw new IllegalStateException("unable to process signature: " + e.getMessage(), e);
        }
    }
}
