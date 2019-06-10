package org.spongycastle.openpgp.jcajce;

import java.io.IOException;
import java.io.InputStream;

import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.PGPSecretKeyRing;
import org.spongycastle.openpgp.operator.KeyFingerPrintCalculator;
import org.spongycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;

public class JcaPGPSecretKeyRing
    extends PGPSecretKeyRing
{
    private static KeyFingerPrintCalculator fingerPrintCalculator = new JcaKeyFingerprintCalculator();

    public JcaPGPSecretKeyRing(byte[] encoding)
        throws IOException, PGPException
    {
        super(encoding, fingerPrintCalculator);
    }

    public JcaPGPSecretKeyRing(InputStream in)
        throws IOException, PGPException
    {
        super(in, fingerPrintCalculator);
    }
}
