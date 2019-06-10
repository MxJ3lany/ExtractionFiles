package org.spongycastle.openpgp.jcajce;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.PGPSecretKeyRingCollection;
import org.spongycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;

public class JcaPGPSecretKeyRingCollection
    extends PGPSecretKeyRingCollection
{
    public JcaPGPSecretKeyRingCollection(byte[] encoding)
        throws IOException, PGPException
    {
        this(new ByteArrayInputStream(encoding));
    }

    public JcaPGPSecretKeyRingCollection(InputStream in)
        throws IOException, PGPException
    {
        super(in, new JcaKeyFingerprintCalculator());
    }

    public JcaPGPSecretKeyRingCollection(Collection collection)
        throws IOException, PGPException
    {
        super(collection);
    }
}
