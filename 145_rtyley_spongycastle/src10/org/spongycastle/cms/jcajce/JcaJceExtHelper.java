package org.spongycastle.cms.jcajce;

import java.security.PrivateKey;

import javax.crypto.SecretKey;

import org.spongycastle.asn1.x509.AlgorithmIdentifier;
import org.spongycastle.jcajce.util.JcaJceHelper;
import org.spongycastle.operator.SymmetricKeyUnwrapper;
import org.spongycastle.operator.jcajce.JceAsymmetricKeyUnwrapper;
import org.spongycastle.operator.jcajce.JceKTSKeyUnwrapper;

interface JcaJceExtHelper
    extends JcaJceHelper
{
    JceAsymmetricKeyUnwrapper createAsymmetricUnwrapper(AlgorithmIdentifier keyEncryptionAlgorithm, PrivateKey keyEncryptionKey);

    JceKTSKeyUnwrapper createAsymmetricUnwrapper(AlgorithmIdentifier keyEncryptionAlgorithm, PrivateKey keyEncryptionKey, byte[] partyUInfo, byte[] partyVInfo);

    SymmetricKeyUnwrapper createSymmetricUnwrapper(AlgorithmIdentifier keyEncryptionAlgorithm, SecretKey keyEncryptionKey);
}
