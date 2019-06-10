package org.spongycastle.est.jcajce;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.spongycastle.est.LimitedSource;
import org.spongycastle.est.Source;
import org.spongycastle.est.TLSUniqueProvider;


class LimitedSSLSocketSource
    implements Source<SSLSession>, TLSUniqueProvider, LimitedSource
{
    protected final SSLSocket socket;
    private final ChannelBindingProvider bindingProvider;
    private final Long absoluteReadLimit;

    public LimitedSSLSocketSource(SSLSocket sock, ChannelBindingProvider bindingProvider, Long absoluteReadLimit)
    {
        this.socket = sock;
        this.bindingProvider = bindingProvider;
        this.absoluteReadLimit = absoluteReadLimit;
    }

    public InputStream getInputStream()
        throws IOException
    {
        return socket.getInputStream();
    }

    public OutputStream getOutputStream()
        throws IOException
    {
        return socket.getOutputStream();
    }

    public SSLSession getSession()
    {
        return socket.getSession();
    }

    public byte[] getTLSUnique()
    {
        if (isTLSUniqueAvailable())
        {
            return bindingProvider.getChannelBinding(socket, "tls-unique");
        }
        throw new IllegalStateException("No binding provider.");
    }

    public boolean isTLSUniqueAvailable()
    {
        return bindingProvider.canAccessChannelBinding(socket);
    }

    public void close()
        throws IOException
    {
        socket.close();
    }

    public Long getAbsoluteReadLimit()
    {
        return absoluteReadLimit;
    }
}
