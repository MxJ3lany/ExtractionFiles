package org.spongycastle.tls;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

import org.spongycastle.tls.crypto.DHGroup;
import org.spongycastle.tls.crypto.DHStandardGroups;
import org.spongycastle.tls.crypto.TlsDHConfig;
import org.spongycastle.util.BigIntegers;

public class TlsDHUtils
{
    public static boolean containsDHECipherSuites(int[] cipherSuites)
    {
        for (int i = 0; i < cipherSuites.length; ++i)
        {
            if (isDHECipherSuite(cipherSuites[i]))
            {
                return true;
            }
        }
        return false;
    }

    public static DHGroup getDHGroup(TlsDHConfig dhConfig)
    {
        int namedGroup = dhConfig.getNamedGroup();
        if (namedGroup >= 0)
        {
            return getNamedDHGroup(namedGroup); 
        }

        return dhConfig.getExplicitGroup();
    }

    public static DHGroup getNamedDHGroup(int namedGroup)
    {
        switch (namedGroup)
        {
        case NamedGroup.ffdhe2048:
            return DHStandardGroups.rfc7919_ffdhe2048;
        case NamedGroup.ffdhe3072:
            return DHStandardGroups.rfc7919_ffdhe3072;
        case NamedGroup.ffdhe4096:
            return DHStandardGroups.rfc7919_ffdhe4096;
        case NamedGroup.ffdhe6144:
            return DHStandardGroups.rfc7919_ffdhe6144;
        case NamedGroup.ffdhe8192:
            return DHStandardGroups.rfc7919_ffdhe8192;
        default:
            return null;
        }
    }

    public static boolean isDHECipherSuite(int cipherSuite)
    {
        switch (TlsUtils.getKeyExchangeAlgorithm(cipherSuite))
        {
        case KeyExchangeAlgorithm.DHE_DSS:
        case KeyExchangeAlgorithm.DHE_DSS_EXPORT:
        case KeyExchangeAlgorithm.DHE_PSK:
        case KeyExchangeAlgorithm.DHE_RSA:
        case KeyExchangeAlgorithm.DHE_RSA_EXPORT:
            return true;

        default:
            return false;
        }
    }

    public static TlsDHConfig readDHConfig(InputStream input) throws IOException
    {
        BigInteger p = readDHParameter(input);
        BigInteger g = readDHParameter(input);

        return new TlsDHConfig(new DHGroup(p, null, g, 0));
    }

    public static TlsDHConfig receiveDHConfig(TlsDHConfigVerifier dhConfigVerifier, InputStream input) throws IOException
    {
        TlsDHConfig dhConfig = TlsDHUtils.readDHConfig(input);
        if (!dhConfigVerifier.accept(dhConfig))
        {
            throw new TlsFatalAlert(AlertDescription.insufficient_security);
        }
        return dhConfig;
    }

    public static BigInteger readDHParameter(InputStream input) throws IOException
    {
        return new BigInteger(1, TlsUtils.readOpaque16(input));
    }

    public static void writeDHConfig(TlsDHConfig dhConfig, OutputStream output)
        throws IOException
    {
        // TODO[rfc7919] Support selection of named groups
        if (dhConfig.getNamedGroup() >= 0)
        {
            throw new TlsFatalAlert(AlertDescription.internal_error);
        }

        DHGroup explicitGroup = dhConfig.getExplicitGroup();
        writeDHParameter(explicitGroup.getP(), output);
        writeDHParameter(explicitGroup.getG(), output);
    }

    public static void writeDHParameter(BigInteger x, OutputStream output) throws IOException
    {
        TlsUtils.writeOpaque16(BigIntegers.asUnsignedByteArray(x), output);
    }
}
