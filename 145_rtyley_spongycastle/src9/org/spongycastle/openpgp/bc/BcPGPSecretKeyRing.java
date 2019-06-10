package org.spongycastle.openpgp.bc;

import java.io.IOException;
import java.io.InputStream;

import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.PGPSecretKeyRing;
import org.spongycastle.openpgp.operator.KeyFingerPrintCalculator;
import org.spongycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;

public class BcPGPSecretKeyRing
    extends PGPSecretKeyRing
{
    private static KeyFingerPrintCalculator fingerPrintCalculator = new BcKeyFingerprintCalculator();

    public BcPGPSecretKeyRing(byte[] encoding)
        throws IOException, PGPException
    {
        super(encoding, fingerPrintCalculator);
    }

    public BcPGPSecretKeyRing(InputStream in)
        throws IOException, PGPException
    {
        super(in, fingerPrintCalculator);
    }
}
