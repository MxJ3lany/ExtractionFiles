package org.spongycastle.pqc.jcajce.interfaces;

public interface XMSSMTKey
{
    int getHeight();

    int getLayers();

    String getTreeDigest();
}
