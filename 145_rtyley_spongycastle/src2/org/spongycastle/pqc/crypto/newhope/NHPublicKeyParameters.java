package org.spongycastle.pqc.crypto.newhope;

import org.spongycastle.crypto.params.AsymmetricKeyParameter;
import org.spongycastle.util.Arrays;

public class NHPublicKeyParameters
    extends AsymmetricKeyParameter
{
    final byte[] pubData;

    public NHPublicKeyParameters(byte[] pubData)
    {
        super(false);
        this.pubData = Arrays.clone(pubData);
    }

    /**
     * Return the public key data.
     *
     * @return the public key values.
     */
    public byte[] getPubData()
    {
        return Arrays.clone(pubData);
    }
}
