package org.spongycastle.tls.crypto.impl.jcajce;

import java.security.Provider;
import java.security.Security;

import org.spongycastle.tls.HashAlgorithm;
import org.spongycastle.tls.SignatureAlgorithm;
import org.spongycastle.tls.SignatureAndHashAlgorithm;

class JcaUtils
{
    static String getJcaAlgorithmName(SignatureAndHashAlgorithm algorithm)
    {
        return (HashAlgorithm.getName(algorithm.getHash()) + "WITH"
            + SignatureAlgorithm.getName(algorithm.getSignature())).toUpperCase();
    }

    static boolean isSunMSCAPIProviderActive()
    {
        return null != Security.getProvider("SunMSCAPI");
    }

    static boolean isSunMSCAPIProvider(Provider provider)
    {
        return null != provider && isSunMSCAPIProviderName(provider.getName());
    }

    static boolean isSunMSCAPIProviderName(String providerName)
    {
        return "SunMSCAPI".equals(providerName);
    }
}
