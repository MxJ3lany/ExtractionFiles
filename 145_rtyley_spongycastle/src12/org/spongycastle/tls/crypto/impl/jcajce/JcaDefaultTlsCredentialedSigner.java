package org.spongycastle.tls.crypto.impl.jcajce;

import java.security.PrivateKey;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateKey;

import org.spongycastle.tls.Certificate;
import org.spongycastle.tls.DefaultTlsCredentialedSigner;
import org.spongycastle.tls.SignatureAndHashAlgorithm;
import org.spongycastle.tls.crypto.TlsCryptoParameters;
import org.spongycastle.tls.crypto.TlsSigner;

/**
 * Credentialed class for generating signatures based on the use of primitives from the JCA.
 */
public class JcaDefaultTlsCredentialedSigner
    extends DefaultTlsCredentialedSigner
{
    private static TlsSigner makeSigner(JcaTlsCrypto crypto, PrivateKey privateKey)
    {
        TlsSigner signer;
        if (privateKey instanceof RSAPrivateKey || "RSA".equals(privateKey.getAlgorithm()))
        {
            signer = new JcaTlsRSASigner(crypto, privateKey);
        }
        else if (privateKey instanceof DSAPrivateKey || "DSA".equals(privateKey.getAlgorithm()))
        {
            signer = new JcaTlsDSASigner(crypto, privateKey);
        }
        else if (privateKey instanceof ECPrivateKey || "EC".equals(privateKey.getAlgorithm()))
        {
            signer = new JcaTlsECDSASigner(crypto, privateKey);
        }
        else
        {
            throw new IllegalArgumentException("'privateKey' type not supported: " + privateKey.getClass().getName());
        }

        return signer;
    }

    public JcaDefaultTlsCredentialedSigner(TlsCryptoParameters cryptoParams, JcaTlsCrypto crypto, PrivateKey privateKey, Certificate certificate, SignatureAndHashAlgorithm signatureAndHashAlgorithm)
    {
        super(cryptoParams, makeSigner(crypto, privateKey), certificate, signatureAndHashAlgorithm);
    }
}
