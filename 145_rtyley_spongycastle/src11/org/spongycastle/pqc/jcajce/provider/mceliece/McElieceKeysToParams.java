package org.spongycastle.pqc.jcajce.provider.mceliece;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.spongycastle.crypto.params.AsymmetricKeyParameter;
import org.spongycastle.pqc.crypto.mceliece.McEliecePrivateKeyParameters;

/**
 * utility class for converting jce/jca McEliece objects
 * objects into their org.spongycastle.crypto counterparts.
 */
public class McElieceKeysToParams
{


    static public AsymmetricKeyParameter generatePublicKeyParameter(
        PublicKey key)
        throws InvalidKeyException
    {
        if (key instanceof BCMcEliecePublicKey)
        {
            BCMcEliecePublicKey k = (BCMcEliecePublicKey)key;

            return k.getKeyParams();
        }

        throw new InvalidKeyException("can't identify McEliece public key: " + key.getClass().getName());
    }


    static public AsymmetricKeyParameter generatePrivateKeyParameter(
        PrivateKey key)
        throws InvalidKeyException
    {
        if (key instanceof BCMcEliecePrivateKey)
        {
            BCMcEliecePrivateKey k = (BCMcEliecePrivateKey)key;
            return new McEliecePrivateKeyParameters(k.getN(), k.getK(), k.getField(), k.getGoppaPoly(),
                 k.getP1(), k.getP2(), k.getSInv());
        }

        throw new InvalidKeyException("can't identify McEliece private key.");
    }
}
