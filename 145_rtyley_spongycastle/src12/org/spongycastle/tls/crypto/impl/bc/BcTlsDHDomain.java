package org.spongycastle.tls.crypto.impl.bc;

import java.io.IOException;
import java.math.BigInteger;

import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.agreement.DHBasicAgreement;
import org.spongycastle.crypto.generators.DHBasicKeyPairGenerator;
import org.spongycastle.crypto.params.DHKeyGenerationParameters;
import org.spongycastle.crypto.params.DHParameters;
import org.spongycastle.crypto.params.DHPrivateKeyParameters;
import org.spongycastle.crypto.params.DHPublicKeyParameters;
import org.spongycastle.tls.AlertDescription;
import org.spongycastle.tls.TlsDHUtils;
import org.spongycastle.tls.TlsFatalAlert;
import org.spongycastle.tls.crypto.DHGroup;
import org.spongycastle.tls.crypto.TlsAgreement;
import org.spongycastle.tls.crypto.TlsDHConfig;
import org.spongycastle.tls.crypto.TlsDHDomain;
import org.spongycastle.util.BigIntegers;

/**
 * BC light-weight support class for Diffie-Hellman key pair generation and key agreement over a specified Diffie-Hellman configuration.
 */
public class BcTlsDHDomain implements TlsDHDomain
{
    public static byte[] calculateBasicAgreement(DHPrivateKeyParameters privateKey, DHPublicKeyParameters publicKey)
    {
        DHBasicAgreement basicAgreement = new DHBasicAgreement();
        basicAgreement.init(privateKey);
        BigInteger agreementValue = basicAgreement.calculateAgreement(publicKey);

        /*
         * RFC 5246 8.1.2. Leading bytes of Z that contain all zero bits are stripped before it is
         * used as the pre_master_secret.
         */
        return BigIntegers.asUnsignedByteArray(agreementValue);
    }

    public static DHParameters getParameters(TlsDHConfig dhConfig)
    {
        DHGroup dhGroup = TlsDHUtils.getDHGroup(dhConfig);
        if (dhGroup == null)
        {
            throw new IllegalArgumentException("No DH configuration provided");
        }

        return new DHParameters(dhGroup.getP(), dhGroup.getG(), dhGroup.getQ(), dhGroup.getL());
    }

    protected BcTlsCrypto crypto;
    protected TlsDHConfig dhConfig;
    protected DHParameters dhParameters;

    public BcTlsDHDomain(BcTlsCrypto crypto, TlsDHConfig dhConfig)
    {
        this.crypto = crypto;
        this.dhConfig = dhConfig;
        this.dhParameters = getParameters(dhConfig);
    }

    public BcTlsSecret calculateDHAgreement(DHPrivateKeyParameters privateKey, DHPublicKeyParameters publicKey)
    {
        return crypto.adoptLocalSecret(calculateBasicAgreement(privateKey, publicKey));
    }

    public TlsAgreement createDH()
    {
        return new BcTlsDH(this);
    }

    public BigInteger decodeParameter(byte[] encoding) throws IOException
    {
        return new BigInteger(1, encoding);
    }

    public DHPublicKeyParameters decodePublicKey(byte[] encoding) throws IOException
    {
        /*
         * RFC 7919 3. [..] the client MUST verify that dh_Ys is in the range 1 < dh_Ys < dh_p - 1.
         * If dh_Ys is not in this range, the client MUST terminate the connection with a fatal
         * handshake_failure(40) alert.
         */
        try
        {
            BigInteger y = decodeParameter(encoding);

            return new DHPublicKeyParameters(y, dhParameters);
        }
        catch (RuntimeException e)
        {
            throw new TlsFatalAlert(AlertDescription.handshake_failure, e);
        }
    }

    public byte[] encodeParameter(BigInteger x) throws IOException
    {
        return BigIntegers.asUnsignedByteArray(x);
    }

    public byte[] encodePublicKey(DHPublicKeyParameters publicKey) throws IOException
    {
        return encodeParameter(publicKey.getY());
    }

    public AsymmetricCipherKeyPair generateKeyPair()
    {
        DHBasicKeyPairGenerator keyPairGenerator = new DHBasicKeyPairGenerator();
        keyPairGenerator.init(new DHKeyGenerationParameters(crypto.getSecureRandom(), dhParameters));
        return keyPairGenerator.generateKeyPair();
    }
}
