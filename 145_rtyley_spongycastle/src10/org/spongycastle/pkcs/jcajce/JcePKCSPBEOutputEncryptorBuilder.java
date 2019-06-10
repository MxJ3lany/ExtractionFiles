package org.spongycastle.pkcs.jcajce;

import java.io.OutputStream;
import java.security.Provider;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.asn1.ASN1Primitive;
import org.spongycastle.asn1.DERNull;
import org.spongycastle.asn1.bc.BCObjectIdentifiers;
import org.spongycastle.asn1.pkcs.EncryptionScheme;
import org.spongycastle.asn1.pkcs.KeyDerivationFunc;
import org.spongycastle.asn1.pkcs.PBES2Parameters;
import org.spongycastle.asn1.pkcs.PBKDF2Params;
import org.spongycastle.asn1.pkcs.PKCS12PBEParams;
import org.spongycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.spongycastle.asn1.x509.AlgorithmIdentifier;
import org.spongycastle.jcajce.PKCS12KeyWithParameters;
import org.spongycastle.jcajce.util.DefaultJcaJceHelper;
import org.spongycastle.jcajce.util.JcaJceHelper;
import org.spongycastle.jcajce.util.NamedJcaJceHelper;
import org.spongycastle.jcajce.util.ProviderJcaJceHelper;
import org.spongycastle.operator.DefaultSecretKeySizeProvider;
import org.spongycastle.operator.GenericKey;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.operator.OutputEncryptor;
import org.spongycastle.operator.SecretKeySizeProvider;

public class JcePKCSPBEOutputEncryptorBuilder
{
    private JcaJceHelper helper = new DefaultJcaJceHelper();
    private ASN1ObjectIdentifier algorithm;
    private ASN1ObjectIdentifier keyEncAlgorithm;
    private SecureRandom random;
    private SecretKeySizeProvider keySizeProvider = DefaultSecretKeySizeProvider.INSTANCE;
    private int iterationCount = 1024;
    private AlgorithmIdentifier prf = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_hmacWithSHA1, DERNull.INSTANCE);

    public JcePKCSPBEOutputEncryptorBuilder(ASN1ObjectIdentifier algorithm)
    {
        if (isPKCS12(algorithm))
        {
            this.algorithm = algorithm;
            this.keyEncAlgorithm = algorithm;
        }
        else
        {
            this.algorithm = PKCSObjectIdentifiers.id_PBES2;
            this.keyEncAlgorithm = algorithm;
        }
    }

    public JcePKCSPBEOutputEncryptorBuilder setProvider(Provider provider)
    {
        this.helper = new ProviderJcaJceHelper(provider);

        return this;
    }

    public JcePKCSPBEOutputEncryptorBuilder setProvider(String providerName)
    {
        this.helper = new NamedJcaJceHelper(providerName);

        return this;
    }

    /**
     * Set the PRF to use for key generation. By default this is HmacSHA1.
     *
     * @param prf algorithm id for PRF.
     *
     * @return the current builder.
     */
    public JcePKCSPBEOutputEncryptorBuilder setPRF(AlgorithmIdentifier prf)
    {
        this.prf = prf;

        return this;
    }

    /**
     * Set the lookup provider of AlgorithmIdentifier returning key_size_in_bits used to
     * handle PKCS5 decryption.
     *
     * @param keySizeProvider  a provider of integer secret key sizes.
     *
     * @return the current builder.
     */
    public JcePKCSPBEOutputEncryptorBuilder setKeySizeProvider(SecretKeySizeProvider keySizeProvider)
    {
        this.keySizeProvider = keySizeProvider;

        return this;
    }

    /**
     * Set the iteration count for the PBE calculation.
     *
     * @param iterationCount the iteration count to apply to the key creation.
     * @return the current builder.
     */
    public JcePKCSPBEOutputEncryptorBuilder setIterationCount(int iterationCount)
    {
        this.iterationCount = iterationCount;

        return this;
    }

    public OutputEncryptor build(final char[] password)
        throws OperatorCreationException
    {
        final Cipher cipher;
        SecretKey key;

        if (random == null)
        {
            random = new SecureRandom();
        }

        final AlgorithmIdentifier encryptionAlg;


        try
        {
            if (isPKCS12(algorithm))
            {
                byte[] salt = new byte[20];

                random.nextBytes(salt);

                cipher = helper.createCipher(algorithm.getId());

                cipher.init(Cipher.ENCRYPT_MODE, new PKCS12KeyWithParameters(password, salt, iterationCount));

                encryptionAlg = new AlgorithmIdentifier(algorithm, new PKCS12PBEParams(salt, iterationCount));
            }
            else if (algorithm.equals(PKCSObjectIdentifiers.id_PBES2))
            {
                byte[] salt = new byte[JceUtils.getSaltSize(prf.getAlgorithm())];

                random.nextBytes(salt);

                SecretKeyFactory keyFact = helper.createSecretKeyFactory(JceUtils.getAlgorithm(prf.getAlgorithm()));

                key = keyFact.generateSecret(new PBEKeySpec(password, salt, iterationCount, keySizeProvider.getKeySize(new AlgorithmIdentifier(keyEncAlgorithm))));

                cipher = helper.createCipher(keyEncAlgorithm.getId());

                cipher.init(Cipher.ENCRYPT_MODE, key, random);

                PBES2Parameters algParams = new PBES2Parameters(
                                   new KeyDerivationFunc(PKCSObjectIdentifiers.id_PBKDF2, new PBKDF2Params(salt, iterationCount, prf)),
                                   new EncryptionScheme(keyEncAlgorithm, ASN1Primitive.fromByteArray(cipher.getParameters().getEncoded())));

                encryptionAlg = new AlgorithmIdentifier(algorithm, algParams);
            }
            else
            {
                throw new OperatorCreationException("unrecognised algorithm");
            }

            return new OutputEncryptor()
            {
                public AlgorithmIdentifier getAlgorithmIdentifier()
                {
                    return encryptionAlg;
                }

                public OutputStream getOutputStream(OutputStream out)
                {
                    return new CipherOutputStream(out, cipher);
                }

                public GenericKey getKey()
                {
                    if (isPKCS12(encryptionAlg.getAlgorithm()))
                    {
                        return new GenericKey(encryptionAlg, PKCS12PasswordToBytes(password));
                    }
                    else
                    {
                        return new GenericKey(encryptionAlg, PKCS5PasswordToBytes(password));
                    }
                }
            };
        }
        catch (Exception e)
        {
            throw new OperatorCreationException("unable to create OutputEncryptor: " + e.getMessage(), e);
        }
    }

    private boolean isPKCS12(ASN1ObjectIdentifier algorithm)
    {
        return algorithm.on(PKCSObjectIdentifiers.pkcs_12PbeIds)
            || algorithm.on(BCObjectIdentifiers.bc_pbe_sha1_pkcs12)
            || algorithm.on(BCObjectIdentifiers.bc_pbe_sha256_pkcs12);
    }

    /**
     * converts a password to a byte array according to the scheme in
     * PKCS5 (ascii, no padding)
     *
     * @param password a character array representing the password.
     * @return a byte array representing the password.
     */
    private static byte[] PKCS5PasswordToBytes(
        char[]  password)
    {
        if (password != null)
        {
            byte[]  bytes = new byte[password.length];

            for (int i = 0; i != bytes.length; i++)
            {
                bytes[i] = (byte)password[i];
            }

            return bytes;
        }
        else
        {
            return new byte[0];
        }
    }

    /**
     * converts a password to a byte array according to the scheme in
     * PKCS12 (unicode, big endian, 2 zero pad bytes at the end).
     *
     * @param password a character array representing the password.
     * @return a byte array representing the password.
     */
    private static byte[] PKCS12PasswordToBytes(
        char[]  password)
    {
        if (password != null && password.length > 0)
        {
                                       // +1 for extra 2 pad bytes.
            byte[]  bytes = new byte[(password.length + 1) * 2];

            for (int i = 0; i != password.length; i ++)
            {
                bytes[i * 2] = (byte)(password[i] >>> 8);
                bytes[i * 2 + 1] = (byte)password[i];
            }

            return bytes;
        }
        else
        {
            return new byte[0];
        }
    }
}
