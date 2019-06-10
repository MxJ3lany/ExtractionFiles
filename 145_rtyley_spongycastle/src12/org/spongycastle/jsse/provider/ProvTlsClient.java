package org.spongycastle.jsse.provider;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSession;
import javax.net.ssl.X509KeyManager;
import javax.security.auth.x500.X500Principal;

import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.jsse.BCSNIServerName;
import org.spongycastle.tls.AlertDescription;
import org.spongycastle.tls.AlertLevel;
import org.spongycastle.tls.Certificate;
import org.spongycastle.tls.CertificateRequest;
import org.spongycastle.tls.CertificateStatusRequest;
import org.spongycastle.tls.CompressionMethod;
import org.spongycastle.tls.DefaultTlsClient;
import org.spongycastle.tls.DefaultTlsKeyExchangeFactory;
import org.spongycastle.tls.KeyExchangeAlgorithm;
import org.spongycastle.tls.NameType;
import org.spongycastle.tls.ProtocolVersion;
import org.spongycastle.tls.ServerName;
import org.spongycastle.tls.ServerNameList;
import org.spongycastle.tls.SignatureAndHashAlgorithm;
import org.spongycastle.tls.TlsAuthentication;
import org.spongycastle.tls.TlsCredentials;
import org.spongycastle.tls.TlsExtensionsUtils;
import org.spongycastle.tls.TlsFatalAlert;
import org.spongycastle.tls.TlsServerCertificate;
import org.spongycastle.tls.TlsSession;
import org.spongycastle.tls.TlsUtils;
import org.spongycastle.tls.crypto.TlsCrypto;
import org.spongycastle.tls.crypto.TlsCryptoParameters;
import org.spongycastle.tls.crypto.impl.jcajce.JcaDefaultTlsCredentialedSigner;
import org.spongycastle.tls.crypto.impl.jcajce.JcaTlsCrypto;
import org.spongycastle.tls.crypto.impl.jcajce.JceDefaultTlsCredentialedAgreement;
import org.spongycastle.util.IPAddress;

class ProvTlsClient
    extends DefaultTlsClient
    implements ProvTlsPeer
{
    private static Logger LOG = Logger.getLogger(ProvTlsClient.class.getName());

    private static final boolean provEnableSNIExtension = PropertyUtils.getBooleanSystemProperty("jsse.enableSNIExtension", true);

    protected final ProvTlsManager manager;
    protected final ProvSSLParameters sslParameters;

    protected boolean handshakeComplete = false;

    ProvTlsClient(ProvTlsManager manager)
    {
        super(manager.getContextData().getCrypto(), new DefaultTlsKeyExchangeFactory(), new ProvDHConfigVerifier());

        this.manager = manager;
        this.sslParameters = manager.getProvSSLParameters();
    }

    @Override
    protected CertificateStatusRequest getCertificateStatusRequest()
    {
        return null;
    }

    @Override
    protected Vector getSNIServerNames()
    {
        if (provEnableSNIExtension)
        {
            List<BCSNIServerName> sniServerNames = manager.getProvSSLParameters().getServerNames();
            if (sniServerNames == null)
            {
                String peerHost = manager.getPeerHost();
                if (peerHost != null && peerHost.indexOf('.') > 0 && !IPAddress.isValid(peerHost))
                {
                    Vector serverNames = new Vector(1);
                    serverNames.addElement(new ServerName(NameType.host_name, peerHost));
                    return serverNames;
                }
            }
            else
            {
                Vector serverNames = new Vector(sniServerNames.size());
                for (BCSNIServerName sniServerName : sniServerNames)
                {
                    /*
                     * TODO[jsse] Add support for constructing ServerName using
                     * BCSNIServerName.getEncoded() directly, then remove the 'host_name' limitation
                     * (although it's currently the only defined type).
                     */
                    if (sniServerName.getType() == NameType.host_name)
                    {
                        try
                        {
                            serverNames.addElement(new ServerName((short)sniServerName.getType(), new String(sniServerName.getEncoded(), "ASCII")));
                        }
                        catch (UnsupportedEncodingException e)
                        {
                            LOG.log(Level.WARNING, "Unable to include SNI server name", e);
                        }
                    }
                }

                // NOTE: We follow SunJSSE behaviour and disable SNI if there are no server names to send
                if (!serverNames.isEmpty())
                {
                    return serverNames;
                }
            }
        }
        return null;
    }

    @Override
    protected Vector getSupportedSignatureAlgorithms()
    {
        return JsseUtils.getSupportedSignatureAlgorithms(getCrypto());
    }

    public synchronized boolean isHandshakeComplete()
    {
        return handshakeComplete;
    }

    public TlsAuthentication getAuthentication() throws IOException
    {
        return new TlsAuthentication()
        {
            public TlsCredentials getClientCredentials(CertificateRequest certificateRequest) throws IOException
            {
                // TODO[jsse] What criteria determines whether we are willing to send client authentication?

                int keyExchangeAlgorithm = TlsUtils.getKeyExchangeAlgorithm(selectedCipherSuite);
                switch (keyExchangeAlgorithm)
                {
                case KeyExchangeAlgorithm.DH_DSS:
                case KeyExchangeAlgorithm.DH_RSA:
                case KeyExchangeAlgorithm.ECDH_ECDSA:
                case KeyExchangeAlgorithm.ECDH_RSA:
                    // TODO[jsse] Add support for the static key exchanges
                    return null;

                case KeyExchangeAlgorithm.DHE_DSS:
                case KeyExchangeAlgorithm.DHE_RSA:
                case KeyExchangeAlgorithm.ECDHE_ECDSA:
                case KeyExchangeAlgorithm.ECDHE_RSA:
                case KeyExchangeAlgorithm.RSA:
                    break;

                default:
                    /* Note: internal error here; selected a key exchange we don't implement! */
                    throw new TlsFatalAlert(AlertDescription.internal_error);
                }

                X509KeyManager km = manager.getContextData().getKeyManager();
                if (km == null)
                {
                    return null;
                }

                short[] certTypes = certificateRequest.getCertificateTypes();
                if (certTypes == null || certTypes.length == 0)
                {
                    // TODO[jsse] Or does this mean ANY type - or something else?
                    return null;
                }

                String[] keyTypes = new String[certTypes.length];
                for (int i = 0; i < certTypes.length; ++i)
                {
                    // TODO[jsse] Need to also take notice of certificateRequest.getSupportedSignatureAlgorithms(), if present
                    keyTypes[i] = JsseUtils.getAuthTypeClient(certTypes[i]);
                }

                Principal[] issuers = null;
                Vector<X500Name> cas = (Vector<X500Name>)certificateRequest.getCertificateAuthorities();
                if (cas != null && cas.size() > 0)
                {
                	X500Name[] names = cas.toArray(new X500Name[cas.size()]);
                	Set<X500Principal> principals = JsseUtils.toX500Principals(names);
                	issuers = principals.toArray(new Principal[principals.size()]);
                }

                // TODO[jsse] How is this used?
                Socket socket = null;

                String alias = km.chooseClientAlias(keyTypes, issuers, socket);
                if (alias == null)
                {
                    return null;
                }

                TlsCrypto crypto = getCrypto();
                if (!(crypto instanceof JcaTlsCrypto))
                {
                    // TODO[jsse] Need to have TlsCrypto construct the credentials from the certs/key
                    throw new UnsupportedOperationException();
                }

                PrivateKey privateKey = km.getPrivateKey(alias);
                Certificate certificate = JsseUtils.getCertificateMessage(crypto, km.getCertificateChain(alias));

                if (privateKey == null || certificate.isEmpty())
                {
                    // TODO[jsse] Log the probable misconfigured keystore
                    return null;
                }

                /*
                 * TODO[jsse] Before proceeding with EC credentials, should we check (TLS 1.2+) that
                 * the used curve was actually declared in the client's elliptic_curves/named_groups
                 * extension?
                 */

                switch (keyExchangeAlgorithm)
                {
                case KeyExchangeAlgorithm.DH_DSS:
                case KeyExchangeAlgorithm.DH_RSA:
                case KeyExchangeAlgorithm.ECDH_ECDSA:
                case KeyExchangeAlgorithm.ECDH_RSA:
                {
                    // TODO[jsse] Need to have TlsCrypto construct the credentials from the certs/key
                    return new JceDefaultTlsCredentialedAgreement((JcaTlsCrypto)crypto, certificate, privateKey);
                }

                case KeyExchangeAlgorithm.DHE_DSS:
                case KeyExchangeAlgorithm.DHE_RSA:
                case KeyExchangeAlgorithm.ECDHE_ECDSA:
                case KeyExchangeAlgorithm.ECDHE_RSA:
                case KeyExchangeAlgorithm.RSA:
                {
                    short certificateType = certificate.getCertificateAt(0).getClientCertificateType();
                    short signatureAlgorithm = TlsUtils.getSignatureAlgorithmClient(certificateType);
                    SignatureAndHashAlgorithm sigAlg = TlsUtils.chooseSignatureAndHashAlgorithm(context,
                        supportedSignatureAlgorithms, signatureAlgorithm);

                    // TODO[jsse] Need to have TlsCrypto construct the credentials from the certs/key
                    return new JcaDefaultTlsCredentialedSigner(new TlsCryptoParameters(context), (JcaTlsCrypto)crypto,
                        privateKey, certificate, sigAlg);
                }

                default:
                    /* Note: internal error here; selected a key exchange we don't implement! */
                    throw new TlsFatalAlert(AlertDescription.internal_error);
                }
            }

            public void notifyServerCertificate(TlsServerCertificate serverCertificate) throws IOException
            {
                boolean noServerCert = serverCertificate == null || serverCertificate.getCertificate() == null
                    || serverCertificate.getCertificate().isEmpty();
                if (noServerCert)
                {
                    throw new TlsFatalAlert(AlertDescription.handshake_failure);
                }
                else
                {
                    X509Certificate[] chain = JsseUtils.getX509CertificateChain(manager.getContextData().getCrypto(), serverCertificate.getCertificate());
                    String authType = JsseUtils.getAuthTypeServer(TlsUtils.getKeyExchangeAlgorithm(selectedCipherSuite));

                    if (!manager.isServerTrusted(chain, authType))
                    {
                        throw new TlsFatalAlert(AlertDescription.bad_certificate);
                    }
                }
            }
        };
    }

    @Override
    public int[] getCipherSuites()
    {
        return TlsUtils.getSupportedCipherSuites(manager.getContextData().getCrypto(),
            manager.getContext().convertCipherSuites(sslParameters.getCipherSuites()));
    }

    @Override
    public short[] getCompressionMethods()
    {
        return manager.getContext().isFips()
            ?   new short[]{ CompressionMethod._null }
            :   super.getCompressionMethods();
    }

    @Override
    public Hashtable getClientExtensions() throws IOException
    {
        Hashtable clientExtensions = TlsExtensionsUtils.ensureExtensionsInitialised(super.getClientExtensions());

        if (provEnableSNIExtension)
        {
            List<BCSNIServerName> sniServerNames = manager.getProvSSLParameters().getServerNames();
            if (sniServerNames == null)
            {
                String peerHost = manager.getPeerHost();
                if (peerHost != null && peerHost.indexOf('.') > 0 && !IPAddress.isValid(peerHost))
                {
                    Vector serverNames = new Vector(1);
                    serverNames.addElement(new ServerName(NameType.host_name, peerHost));
                    TlsExtensionsUtils.addServerNameExtension(clientExtensions, new ServerNameList(serverNames));
                }
            }
            else if (sniServerNames.isEmpty())
            {
                // NOTE: We follow SunJSSE behaviour and disable SNI in this case
            }
            else
            {
                Vector serverNames = new Vector(sniServerNames.size());
                for (BCSNIServerName sniServerName : sniServerNames)
                {
                    /*
                     * TODO[jsse] Add support for constructing ServerName using
                     * BCSNIServerName.getEncoded() directly, then remove the 'host_name' limitation
                     * (although it's currently the only defined type).
                     */
                    if (sniServerName.getType() == NameType.host_name)
                    {
                        serverNames.addElement(new ServerName((short)sniServerName.getType(), new String(sniServerName.getEncoded(), "ASCII")));
                    }
                }
                TlsExtensionsUtils.addServerNameExtension(clientExtensions, new ServerNameList(serverNames));
            }
        }

        return clientExtensions;
    }

//    public TlsKeyExchange getKeyExchange() throws IOException
//    {
//        // TODO[jsse] Check that all key exchanges used in JSSE supportedCipherSuites are handled
//        return super.getKeyExchange();
//    }

    @Override
    public ProtocolVersion getMinimumVersion()
    {
        return manager.getContext().getMinimumVersion(sslParameters.getProtocols());
    }

    @Override
    public ProtocolVersion getClientVersion()
    {
        return manager.getContext().getMaximumVersion(sslParameters.getProtocols());
    }

    @Override
    public TlsSession getSessionToResume()
    {
        // TODO[jsse] Search for a suitable session in the client session context
        return null;
    }

    @Override
    public void notifyAlertRaised(short alertLevel, short alertDescription, String message, Throwable cause)
    {
        super.notifyAlertRaised(alertLevel, alertDescription, message, cause);

        Level level = alertLevel == AlertLevel.warning                      ? Level.FINE
                    : alertDescription == AlertDescription.internal_error   ? Level.WARNING
                    :                                                         Level.INFO;

        if (LOG.isLoggable(level))
        {
            String msg = JsseUtils.getAlertLogMessage("Client raised", alertLevel, alertDescription);
            if (message != null)
            {
                msg = msg + ": " + message;
            }

            LOG.log(level, msg, cause);
        }
    }

    @Override
    public void notifyAlertReceived(short alertLevel, short alertDescription)
    {
        super.notifyAlertReceived(alertLevel, alertDescription);

        Level level = alertLevel == AlertLevel.warning  ? Level.FINE
                    :                                     Level.INFO;

        if (LOG.isLoggable(level))
        {
            String msg = JsseUtils.getAlertLogMessage("Client received", alertLevel, alertDescription);

            LOG.log(level, msg);
        }
    }

    @Override
    public synchronized void notifyHandshakeComplete() throws IOException
    {
        this.handshakeComplete = true;

        ProvSSLSessionContext sessionContext = manager.getContextData().getClientSessionContext();
        SSLSession session = sessionContext.reportSession(context.getSession());
        ProvSSLConnection connection = new ProvSSLConnection(context, session);

        manager.notifyHandshakeComplete(connection);
    }

    @Override
    public void notifySelectedCipherSuite(int selectedCipherSuite)
    {
        manager.getContext().validateNegotiatedCipherSuite(selectedCipherSuite);

        super.notifySelectedCipherSuite(selectedCipherSuite);

        LOG.fine("Client notified of selected cipher suite: " + manager.getContext().getCipherSuiteString(selectedCipherSuite));
    }

    @Override
    public void notifyServerVersion(ProtocolVersion serverVersion) throws IOException
    {
        String selected = manager.getContext().getProtocolString(serverVersion);
        if (selected != null)
        {
            for (String protocol : sslParameters.getProtocols())
            {
                if (selected.equals(protocol))
                {
                    LOG.fine("Client notified of selected protocol version: " + selected);
                    return;
                }
            }
        }
        throw new TlsFatalAlert(AlertDescription.protocol_version);
    }
}
