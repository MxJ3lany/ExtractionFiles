package org.spongycastle.tls.crypto.impl.bc;

import org.spongycastle.crypto.params.AsymmetricKeyParameter;
import org.spongycastle.crypto.params.DSAPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.RSAKeyParameters;
import org.spongycastle.tls.Certificate;
import org.spongycastle.tls.DefaultTlsCredentialedSigner;
import org.spongycastle.tls.SignatureAndHashAlgorithm;
import org.spongycastle.tls.crypto.TlsCryptoParameters;
import org.spongycastle.tls.crypto.TlsSigner;

/**
 * Credentialed class for generating signatures based on the use of primitives from the BC light-weight API.
 */
public class BcDefaultTlsCredentialedSigner
    extends DefaultTlsCredentialedSigner
{
    private static TlsSigner makeSigner(BcTlsCrypto crypto, AsymmetricKeyParameter privateKey)
    {
        TlsSigner signer;
        if (privateKey instanceof RSAKeyParameters)
        {
            signer = new BcTlsRSASigner(crypto, (RSAKeyParameters)privateKey);
        }
        else if (privateKey instanceof DSAPrivateKeyParameters)
        {
            signer = new BcTlsDSASigner(crypto, (DSAPrivateKeyParameters)privateKey);
        }
        else if (privateKey instanceof ECPrivateKeyParameters)
        {
            signer = new BcTlsECDSASigner(crypto, (ECPrivateKeyParameters)privateKey);
        }
        else
        {
            throw new IllegalArgumentException("'privateKey' type not supported: " + privateKey.getClass().getName());
        }

        return signer;
    }

    public BcDefaultTlsCredentialedSigner(TlsCryptoParameters cryptoParams, BcTlsCrypto crypto, AsymmetricKeyParameter privateKey, Certificate certificate, SignatureAndHashAlgorithm signatureAndHashAlgorithm)
    {
        super(cryptoParams, makeSigner(crypto, privateKey), certificate, signatureAndHashAlgorithm);
    }
}
