package org.spongycastle.openssl.jcajce;

import java.io.IOException;
import java.io.InputStream;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.Provider;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;

import org.spongycastle.asn1.pkcs.EncryptionScheme;
import org.spongycastle.asn1.pkcs.KeyDerivationFunc;
import org.spongycastle.asn1.pkcs.PBEParameter;
import org.spongycastle.asn1.pkcs.PBES2Parameters;
import org.spongycastle.asn1.pkcs.PBKDF2Params;
import org.spongycastle.asn1.pkcs.PKCS12PBEParams;
import org.spongycastle.asn1.x509.AlgorithmIdentifier;
import org.spongycastle.crypto.CharToByteConverter;
import org.spongycastle.jcajce.PBKDF1KeyWithParameters;
import org.spongycastle.jcajce.PKCS12KeyWithParameters;
import org.spongycastle.jcajce.util.DefaultJcaJceHelper;
import org.spongycastle.jcajce.util.JcaJceHelper;
import org.spongycastle.jcajce.util.NamedJcaJceHelper;
import org.spongycastle.jcajce.util.ProviderJcaJceHelper;
import org.spongycastle.openssl.PEMException;
import org.spongycastle.operator.InputDecryptor;
import org.spongycastle.operator.InputDecryptorProvider;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.util.Strings;

public class JceOpenSSLPKCS8DecryptorProviderBuilder
{
    private JcaJceHelper helper = new DefaultJcaJceHelper();

    public JceOpenSSLPKCS8DecryptorProviderBuilder()
    {
        helper = new DefaultJcaJceHelper();
    }

    public JceOpenSSLPKCS8DecryptorProviderBuilder setProvider(String providerName)
    {
        helper = new NamedJcaJceHelper(providerName);

        return this;
    }

    public JceOpenSSLPKCS8DecryptorProviderBuilder setProvider(Provider provider)
    {
        helper = new ProviderJcaJceHelper(provider);

        return this;
    }

    public InputDecryptorProvider build(final char[] password)
        throws OperatorCreationException
    {
        return new InputDecryptorProvider()
        {
            public InputDecryptor get(final AlgorithmIdentifier algorithm)
                throws OperatorCreationException
            {
                final Cipher cipher;

                try
                {
                    if (PEMUtilities.isPKCS5Scheme2(algorithm.getAlgorithm()))
                    {
                        PBES2Parameters params = PBES2Parameters.getInstance(algorithm.getParameters());
                        KeyDerivationFunc func = params.getKeyDerivationFunc();
                        EncryptionScheme scheme = params.getEncryptionScheme();
                        PBKDF2Params defParams = (PBKDF2Params)func.getParameters();

                        int iterationCount = defParams.getIterationCount().intValue();
                        byte[] salt = defParams.getSalt();

                        String oid = scheme.getAlgorithm().getId();

                        SecretKey key;

                        if (PEMUtilities.isHmacSHA1(defParams.getPrf()))
                        {
                            key = PEMUtilities.generateSecretKeyForPKCS5Scheme2(helper, oid, password, salt, iterationCount);
                        }
                        else
                        {
                            key = PEMUtilities.generateSecretKeyForPKCS5Scheme2(helper, oid, password, salt, iterationCount, defParams.getPrf());
                        }
                        
                        cipher = helper.createCipher(oid);
                        AlgorithmParameters algParams = helper.createAlgorithmParameters(oid);

                        algParams.init(scheme.getParameters().toASN1Primitive().getEncoded());

                        cipher.init(Cipher.DECRYPT_MODE, key, algParams);
                    }
                    else if (PEMUtilities.isPKCS12(algorithm.getAlgorithm()))
                    {
                        PKCS12PBEParams params = PKCS12PBEParams.getInstance(algorithm.getParameters());

                        cipher = helper.createCipher(algorithm.getAlgorithm().getId());

                        cipher.init(Cipher.DECRYPT_MODE, new PKCS12KeyWithParameters(password, params.getIV(), params.getIterations().intValue()));
                    }
                    else if (PEMUtilities.isPKCS5Scheme1(algorithm.getAlgorithm()))
                    {
                        PBEParameter params = PBEParameter.getInstance(algorithm.getParameters());

                        cipher = helper.createCipher(algorithm.getAlgorithm().getId());

                        cipher.init(Cipher.DECRYPT_MODE, new PBKDF1KeyWithParameters(password, new CharToByteConverter()
                        {
                            public String getType()
                            {
                                return "ASCII";
                            }

                            public byte[] convert(char[] password)
                            {
                                return Strings.toByteArray(password);     // just drop hi-order byte.
                            }
                        }, params.getSalt(), params.getIterationCount().intValue()));
                    }
                    else
                    {
                        throw new PEMException("Unknown algorithm: " + algorithm.getAlgorithm());
                    }

                    return new InputDecryptor()
                    {
                        public AlgorithmIdentifier getAlgorithmIdentifier()
                        {
                            return algorithm;
                        }

                        public InputStream getInputStream(InputStream encIn)
                        {
                            return new CipherInputStream(encIn, cipher);
                        }
                    };
                }
                catch (IOException e)
                {
                    throw new OperatorCreationException(algorithm.getAlgorithm() + " not available: " + e.getMessage(), e);
                }
                catch (GeneralSecurityException e)
                {
                    throw new OperatorCreationException(algorithm.getAlgorithm() + " not available: " + e.getMessage(), e);
                }
            };
        };
    }
}
