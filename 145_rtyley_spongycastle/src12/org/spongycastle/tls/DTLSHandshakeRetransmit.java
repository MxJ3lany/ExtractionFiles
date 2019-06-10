package org.spongycastle.tls;

import java.io.IOException;

interface DTLSHandshakeRetransmit
{
    void receivedHandshakeRecord(int epoch, byte[] buf, int off, int len)
        throws IOException;
}
