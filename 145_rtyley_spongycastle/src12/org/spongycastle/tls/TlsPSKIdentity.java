package org.spongycastle.tls;

public interface TlsPSKIdentity
{
    void skipIdentityHint();

    void notifyIdentityHint(byte[] psk_identity_hint);

    byte[] getPSKIdentity();

    byte[] getPSK();
}
