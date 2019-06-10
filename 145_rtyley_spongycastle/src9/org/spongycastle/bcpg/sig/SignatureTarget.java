package org.spongycastle.bcpg.sig;

import org.spongycastle.bcpg.SignatureSubpacket;
import org.spongycastle.bcpg.SignatureSubpacketTags;
import org.spongycastle.util.Arrays;

/**
 * RFC 4880, Section 5.2.3.25 - Signature Target subpacket.
 */
public class SignatureTarget
    extends SignatureSubpacket
{
    public SignatureTarget(
        boolean    critical,
        boolean    isLongLength,
        byte[]     data)
    {
        super(SignatureSubpacketTags.SIGNATURE_TARGET, critical, isLongLength, data);
    }

    public SignatureTarget(
        boolean    critical,
        int        publicKeyAlgorithm,
        int        hashAlgorithm,
        byte[]     hashData)
    {
        super(SignatureSubpacketTags.SIGNATURE_TARGET, critical, false, Arrays.concatenate(new byte[] { (byte)publicKeyAlgorithm, (byte)hashAlgorithm }, hashData));
    }

    public int getPublicKeyAlgorithm()
    {
        return data[0] & 0xff;
    }

    public int getHashAlgorithm()
    {
        return data[1] & 0xff;
    }

    public byte[] getHashData()
    {
        return Arrays.copyOfRange(data, 2, data.length);
    }
}
