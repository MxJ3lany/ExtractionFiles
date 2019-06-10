package org.spongycastle.openpgp.bc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.PGPPublicKeyRingCollection;
import org.spongycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;

public class BcPGPPublicKeyRingCollection
    extends PGPPublicKeyRingCollection
{
    public BcPGPPublicKeyRingCollection(byte[] encoding)
        throws IOException, PGPException
    {
        this(new ByteArrayInputStream(encoding));
    }

    public BcPGPPublicKeyRingCollection(InputStream in)
        throws IOException, PGPException
    {
        super(in, new BcKeyFingerprintCalculator());
    }

    public BcPGPPublicKeyRingCollection(Collection collection)
        throws IOException, PGPException
    {
        super(collection);
    }
}
