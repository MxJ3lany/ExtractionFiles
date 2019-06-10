package org.spongycastle.tls.crypto.impl;

import org.spongycastle.tls.ExporterLabel;
import org.spongycastle.tls.ProtocolVersion;
import org.spongycastle.tls.SecurityParameters;
import org.spongycastle.tls.crypto.TlsCryptoParameters;
import org.spongycastle.tls.crypto.TlsSecret;
import org.spongycastle.util.Arrays;

/**
 * Useful utility methods.
 */
public class TlsImplUtils
{
    public static boolean isTLSv11(ProtocolVersion version)
    {
        return ProtocolVersion.TLSv11.isEqualOrEarlierVersionOf(version.getEquivalentTLSVersion());
    }

    public static boolean isTLSv11(TlsCryptoParameters cryptoParams)
    {
        return isTLSv11(cryptoParams.getServerVersion());
    }

    public static boolean isTLSv12(ProtocolVersion version)
    {
        return ProtocolVersion.TLSv12.isEqualOrEarlierVersionOf(version.getEquivalentTLSVersion());
    }

    public static boolean isTLSv12(TlsCryptoParameters cryptoParams)
    {
        return isTLSv12(cryptoParams.getServerVersion());
    }

    public static byte[] calculateKeyBlock(TlsCryptoParameters cryptoParams, int length)
    {
        SecurityParameters securityParameters = cryptoParams.getSecurityParameters();
        TlsSecret master_secret = securityParameters.getMasterSecret();
        byte[] seed = Arrays.concatenate(securityParameters.getServerRandom(), securityParameters.getClientRandom());
        return PRF(cryptoParams, master_secret, ExporterLabel.key_expansion, seed, length).extract();
    }

    public static TlsSecret PRF(TlsCryptoParameters cryptoParams, TlsSecret secret, String asciiLabel, byte[] seed, int length)
    {
        int prfAlgorithm = cryptoParams.getSecurityParameters().getPrfAlgorithm();

        return secret.deriveUsingPRF(prfAlgorithm, asciiLabel, seed, length);
    }
}
