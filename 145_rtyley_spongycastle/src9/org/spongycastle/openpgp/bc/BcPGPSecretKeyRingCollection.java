package org.spongycastle.openpgp.bc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.PGPSecretKeyRingCollection;
import org.spongycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;

public class BcPGPSecretKeyRingCollection
    extends PGPSecretKeyRingCollection
{
    public BcPGPSecretKeyRingCollection(byte[] encoding)
        throws IOException, PGPException
    {
        this(new ByteArrayInputStream(encoding));
    }

    public BcPGPSecretKeyRingCollection(InputStream in)
        throws IOException, PGPException
    {
        super(in, new BcKeyFingerprintCalculator());
    }

    public BcPGPSecretKeyRingCollection(Collection collection)
        throws IOException, PGPException
    {
        super(collection);
    }
}
