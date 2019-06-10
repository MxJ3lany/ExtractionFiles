package org.spongycastle.tls;

import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.asn1.x509.X509ObjectIdentifiers;

public interface TlsObjectIdentifiers
{
    /**
     * RFC 7633
     */
    static final ASN1ObjectIdentifier id_pe_tlsfeature = X509ObjectIdentifiers.id_pe.branch("24");
}
