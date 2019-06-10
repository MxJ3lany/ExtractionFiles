package org.spongycastle.pqc.jcajce.interfaces;

import java.security.PrivateKey;

public interface NHPrivateKey
    extends NHKey, PrivateKey
{
    short[] getSecretData();
}
