package org.spongycastle.openssl.bc;

import org.spongycastle.openssl.PEMDecryptor;
import org.spongycastle.openssl.PEMDecryptorProvider;
import org.spongycastle.openssl.PEMException;
import org.spongycastle.openssl.PasswordException;

public class BcPEMDecryptorProvider
    implements PEMDecryptorProvider
{
    private final char[] password;

    public BcPEMDecryptorProvider(char[] password)
    {
        this.password = password;
    }

    public PEMDecryptor get(final String dekAlgName)
    {
        return new PEMDecryptor()
        {
            public byte[] decrypt(byte[] keyBytes, byte[] iv)
                throws PEMException
            {
                if (password == null)
                {
                    throw new PasswordException("Password is null, but a password is required");
                }

                return PEMUtilities.crypt(false, keyBytes, password, dekAlgName, iv);
            }
        };
    }
}
