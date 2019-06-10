package org.spongycastle.tls;

import org.spongycastle.tls.crypto.TlsECConfig;

/**
 * Interface a class for verifying EC config needs to conform to.
 */
public interface TlsECConfigVerifier
{
    /**
     * Check whether the given EC configuration is acceptable for use.
     * 
     * @param ecConfig the {@link TlsECConfig} to check
     * @return true if (and only if) the specified configuration is acceptable
     */
    boolean accept(TlsECConfig ecConfig);
}
