package org.spongycastle.pqc.crypto.newhope;

import org.spongycastle.crypto.engines.ChaChaEngine;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;

class ChaCha20
{
    static void process(byte[] key, byte[] nonce, byte[] buf, int off, int len)
    {
        ChaChaEngine e = new ChaChaEngine(20);
        e.init(true, new ParametersWithIV(new KeyParameter(key), nonce));
        e.processBytes(buf, off, len, buf, off);
    }
}
