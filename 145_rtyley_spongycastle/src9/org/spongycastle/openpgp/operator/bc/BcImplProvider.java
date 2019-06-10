package org.spongycastle.openpgp.operator.bc;

import org.spongycastle.bcpg.HashAlgorithmTags;
import org.spongycastle.bcpg.PublicKeyAlgorithmTags;
import org.spongycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.spongycastle.crypto.AsymmetricBlockCipher;
import org.spongycastle.crypto.BlockCipher;
import org.spongycastle.crypto.Digest;
import org.spongycastle.crypto.Signer;
import org.spongycastle.crypto.Wrapper;
import org.spongycastle.crypto.digests.MD2Digest;
import org.spongycastle.crypto.digests.MD5Digest;
import org.spongycastle.crypto.digests.RIPEMD160Digest;
import org.spongycastle.crypto.digests.SHA1Digest;
import org.spongycastle.crypto.digests.SHA224Digest;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.digests.SHA384Digest;
import org.spongycastle.crypto.digests.SHA512Digest;
import org.spongycastle.crypto.digests.TigerDigest;
import org.spongycastle.crypto.encodings.PKCS1Encoding;
import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.engines.BlowfishEngine;
import org.spongycastle.crypto.engines.CAST5Engine;
import org.spongycastle.crypto.engines.CamelliaEngine;
import org.spongycastle.crypto.engines.DESEngine;
import org.spongycastle.crypto.engines.DESedeEngine;
import org.spongycastle.crypto.engines.ElGamalEngine;
import org.spongycastle.crypto.engines.IDEAEngine;
import org.spongycastle.crypto.engines.RFC3394WrapEngine;
import org.spongycastle.crypto.engines.RSABlindedEngine;
import org.spongycastle.crypto.engines.TwofishEngine;
import org.spongycastle.crypto.signers.DSADigestSigner;
import org.spongycastle.crypto.signers.DSASigner;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.crypto.signers.RSADigestSigner;
import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.PGPPublicKey;

class BcImplProvider
{
    static Digest createDigest(int algorithm)
        throws PGPException
    {
        switch (algorithm)
        {
        case HashAlgorithmTags.SHA1:
            return new SHA1Digest();
        case HashAlgorithmTags.SHA224:
            return new SHA224Digest();
        case HashAlgorithmTags.SHA256:
            return new SHA256Digest();
        case HashAlgorithmTags.SHA384:
            return new SHA384Digest();
        case HashAlgorithmTags.SHA512:
            return new SHA512Digest();
        case HashAlgorithmTags.MD2:
            return new MD2Digest();
        case HashAlgorithmTags.MD5:
            return new MD5Digest();
        case HashAlgorithmTags.RIPEMD160:
            return new RIPEMD160Digest();
        case HashAlgorithmTags.TIGER_192:
            return new TigerDigest();
        default:
            throw new PGPException("cannot recognise digest");
        }
    }

    static Signer createSigner(int keyAlgorithm, int hashAlgorithm)
        throws PGPException
    {
        switch(keyAlgorithm)
        {
        case PublicKeyAlgorithmTags.RSA_GENERAL:
        case PublicKeyAlgorithmTags.RSA_SIGN:
            return new RSADigestSigner(createDigest(hashAlgorithm));
        case PublicKeyAlgorithmTags.DSA:
            return new DSADigestSigner(new DSASigner(), createDigest(hashAlgorithm));
        case PublicKeyAlgorithmTags.ECDSA:
            return new DSADigestSigner(new ECDSASigner(), createDigest(hashAlgorithm));
        default:
            throw new PGPException("cannot recognise keyAlgorithm: " + keyAlgorithm);
        }
    }

    static BlockCipher createBlockCipher(int encAlgorithm)
        throws PGPException
    {
        BlockCipher engine;

        switch (encAlgorithm)
        {
        case SymmetricKeyAlgorithmTags.AES_128:
        case SymmetricKeyAlgorithmTags.AES_192:
        case SymmetricKeyAlgorithmTags.AES_256:
            engine = new AESEngine();
            break;
        case SymmetricKeyAlgorithmTags.CAMELLIA_128:
        case SymmetricKeyAlgorithmTags.CAMELLIA_192:
        case SymmetricKeyAlgorithmTags.CAMELLIA_256:
            engine = new CamelliaEngine();
            break;
        case SymmetricKeyAlgorithmTags.BLOWFISH:
            engine = new BlowfishEngine();
            break;
        case SymmetricKeyAlgorithmTags.CAST5:
            engine = new CAST5Engine();
            break;
        case SymmetricKeyAlgorithmTags.DES:
            engine = new DESEngine();
            break;
        case SymmetricKeyAlgorithmTags.IDEA:
            engine = new IDEAEngine();
            break;
        case SymmetricKeyAlgorithmTags.TWOFISH:
            engine = new TwofishEngine();
            break;
        case SymmetricKeyAlgorithmTags.TRIPLE_DES:
            engine = new DESedeEngine();
            break;
        default:
            throw new PGPException("cannot recognise cipher");
        }

        return engine;
    }

    static Wrapper createWrapper(int encAlgorithm)
        throws PGPException
    {
        switch (encAlgorithm)
        {
        case SymmetricKeyAlgorithmTags.AES_128:
        case SymmetricKeyAlgorithmTags.AES_192:
        case SymmetricKeyAlgorithmTags.AES_256:
            return new RFC3394WrapEngine(new AESEngine());
        case SymmetricKeyAlgorithmTags.CAMELLIA_128:
        case SymmetricKeyAlgorithmTags.CAMELLIA_192:
        case SymmetricKeyAlgorithmTags.CAMELLIA_256:
            return new RFC3394WrapEngine(new CamelliaEngine());
        default:
            throw new PGPException("unknown wrap algorithm: " + encAlgorithm);
        }
    }

    static AsymmetricBlockCipher createPublicKeyCipher(int encAlgorithm)
        throws PGPException
    {
        AsymmetricBlockCipher c;

        switch (encAlgorithm)
        {
        case PGPPublicKey.RSA_ENCRYPT:
        case PGPPublicKey.RSA_GENERAL:
            c = new PKCS1Encoding(new RSABlindedEngine());
            break;
        case PGPPublicKey.ELGAMAL_ENCRYPT:
        case PGPPublicKey.ELGAMAL_GENERAL:
            c = new PKCS1Encoding(new ElGamalEngine());
            break;
        case PGPPublicKey.DSA:
            throw new PGPException("Can't use DSA for encryption.");
        case PGPPublicKey.ECDSA:
            throw new PGPException("Can't use ECDSA for encryption.");
        case PGPPublicKey.ECDH:
            throw new PGPException("Not implemented.");
        default:
            throw new PGPException("unknown asymmetric algorithm: " + encAlgorithm);
        }

        return c;
    }
}
