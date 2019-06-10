package org.spongycastle.est.jcajce;

import java.util.Set;

import javax.net.ssl.SSLSocketFactory;

import org.spongycastle.est.ESTClient;
import org.spongycastle.est.ESTClientProvider;
import org.spongycastle.est.ESTException;

class DefaultESTHttpClientProvider
    implements ESTClientProvider
{

    private final JsseHostnameAuthorizer hostNameAuthorizer;
    private final SSLSocketFactoryCreator socketFactoryCreator;

    private final int timeout;
    private final ChannelBindingProvider bindingProvider;
    private final Set<String> cipherSuites;
    private final Long absoluteLimit;
    private final boolean filterCipherSuites;


    public DefaultESTHttpClientProvider(
        JsseHostnameAuthorizer hostNameAuthorizer,
        SSLSocketFactoryCreator socketFactoryCreator, int timeout,
        ChannelBindingProvider bindingProvider,
        Set<String> cipherSuites, Long absoluteLimit, boolean filterCipherSuites)
    {

        this.hostNameAuthorizer = hostNameAuthorizer;
        this.socketFactoryCreator = socketFactoryCreator;
        this.timeout = timeout;
        this.bindingProvider = bindingProvider;
        this.cipherSuites = cipherSuites;
        this.absoluteLimit = absoluteLimit;
        this.filterCipherSuites = filterCipherSuites;
    }

    public ESTClient makeClient()
        throws ESTException
    {
        try
        {
            SSLSocketFactory socketFactory = socketFactoryCreator.createFactory();
            return new DefaultESTClient(
                new DefaultESTClientSourceProvider(socketFactory, hostNameAuthorizer, timeout, bindingProvider, cipherSuites, absoluteLimit, filterCipherSuites));
        }
        catch (Exception e)
        {
            throw new ESTException(e.getMessage(), e.getCause());
        }
    }


    public boolean isTrusted()
    {
        return socketFactoryCreator.isTrusted();
    }
}
