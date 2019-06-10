package org.spongycastle.pqc.crypto.newhope;

import java.security.SecureRandom;

import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.spongycastle.crypto.KeyGenerationParameters;

public class NHKeyPairGenerator
    implements AsymmetricCipherKeyPairGenerator
{
    private SecureRandom random;

    public void init(KeyGenerationParameters param)
    {
        this.random = param.getRandom();
    }

    public AsymmetricCipherKeyPair generateKeyPair()
    {
        byte[] pubData = new byte[NewHope.SENDA_BYTES];
        short[] secData = new short[NewHope.POLY_SIZE];

        NewHope.keygen(random, pubData, secData);

        return new AsymmetricCipherKeyPair(new NHPublicKeyParameters(pubData), new NHPrivateKeyParameters(secData));
    }
}
