package org.spongycastle.cms.jcajce;

import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.PrivateKey;

import javax.crypto.Cipher;

import org.spongycastle.asn1.x509.AlgorithmIdentifier;
import org.spongycastle.cms.CMSException;
import org.spongycastle.cms.KeyTransRecipientId;
import org.spongycastle.cms.RecipientOperator;
import org.spongycastle.jcajce.io.CipherInputStream;
import org.spongycastle.operator.InputDecryptor;

/**
 * the KeyTransRecipient class for a recipient who has been sent secret
 * key material encrypted using their public key that needs to be used to
 * derive a key and extract a message.
 */
public class JceKTSKeyTransEnvelopedRecipient
    extends JceKTSKeyTransRecipient
{
    public JceKTSKeyTransEnvelopedRecipient(PrivateKey recipientKey, KeyTransRecipientId recipientId)
        throws IOException
    {
        super(recipientKey, getPartyVInfoFromRID(recipientId));
    }

    public RecipientOperator getRecipientOperator(AlgorithmIdentifier keyEncryptionAlgorithm, final AlgorithmIdentifier contentEncryptionAlgorithm, byte[] encryptedContentEncryptionKey)
        throws CMSException
    {
        Key secretKey = extractSecretKey(keyEncryptionAlgorithm, contentEncryptionAlgorithm, encryptedContentEncryptionKey);

        final Cipher dataCipher = contentHelper.createContentCipher(secretKey, contentEncryptionAlgorithm);

        return new RecipientOperator(new InputDecryptor()
        {
            public AlgorithmIdentifier getAlgorithmIdentifier()
            {
                return contentEncryptionAlgorithm;
            }

            public InputStream getInputStream(InputStream dataIn)
            {
                return new CipherInputStream(dataIn, dataCipher);
            }
        });
    }
}
