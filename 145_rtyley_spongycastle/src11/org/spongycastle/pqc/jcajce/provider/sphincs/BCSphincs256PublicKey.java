package org.spongycastle.pqc.jcajce.provider.sphincs;

import java.io.IOException;
import java.security.PublicKey;

import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.asn1.x509.AlgorithmIdentifier;
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;
import org.spongycastle.crypto.CipherParameters;
import org.spongycastle.pqc.asn1.PQCObjectIdentifiers;
import org.spongycastle.pqc.asn1.SPHINCS256KeyParams;
import org.spongycastle.pqc.crypto.sphincs.SPHINCSPublicKeyParameters;
import org.spongycastle.pqc.jcajce.interfaces.SPHINCSKey;
import org.spongycastle.util.Arrays;

public class BCSphincs256PublicKey
    implements PublicKey, SPHINCSKey
{
    private static final long serialVersionUID = 1L;

    private final ASN1ObjectIdentifier treeDigest;
    private final SPHINCSPublicKeyParameters params;

    public BCSphincs256PublicKey(
        ASN1ObjectIdentifier treeDigest,
        SPHINCSPublicKeyParameters params)
    {
        this.treeDigest = treeDigest;
        this.params = params;
    }

    public BCSphincs256PublicKey(SubjectPublicKeyInfo keyInfo)
    {
        this.treeDigest = SPHINCS256KeyParams.getInstance(keyInfo.getAlgorithm().getParameters()).getTreeDigest().getAlgorithm();
        this.params = new SPHINCSPublicKeyParameters(keyInfo.getPublicKeyData().getBytes());
    }

    /**
     * Compare this SPHINCS-256 public key with another object.
     *
     * @param o the other object
     * @return the result of the comparison
     */
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }

        if (o instanceof BCSphincs256PublicKey)
        {
            BCSphincs256PublicKey otherKey = (BCSphincs256PublicKey)o;

            return treeDigest.equals(otherKey.treeDigest) && Arrays.areEqual(params.getKeyData(), otherKey.params.getKeyData());
        }

        return false;
    }

    public int hashCode()
    {
        return treeDigest.hashCode() + 37 * Arrays.hashCode(params.getKeyData());
    }

    /**
     * @return name of the algorithm - "SPHINCS-256"
     */
    public final String getAlgorithm()
    {
        return "SPHINCS-256";
    }

    public byte[] getEncoded()
    {
        SubjectPublicKeyInfo pki;
        try
        {
            AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(PQCObjectIdentifiers.sphincs256, new SPHINCS256KeyParams(new AlgorithmIdentifier(treeDigest)));
            pki = new SubjectPublicKeyInfo(algorithmIdentifier, params.getKeyData());

            return pki.getEncoded();
        }
        catch (IOException e)
        {
            return null;
        }
    }

    public String getFormat()
    {
        return "X.509";
    }

    public byte[] getKeyData()
    {
        return params.getKeyData();
    }

    CipherParameters getKeyParams()
    {
        return params;
    }
}
