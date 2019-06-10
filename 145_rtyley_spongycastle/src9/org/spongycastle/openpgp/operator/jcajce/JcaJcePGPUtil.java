package org.spongycastle.openpgp.operator.jcajce;

import java.io.IOException;
import java.math.BigInteger;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.asn1.x9.ECNamedCurveTable;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.math.ec.ECCurve;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.openpgp.PGPException;
import org.spongycastle.util.BigIntegers;

/**
 * Basic utility class
 */
class JcaJcePGPUtil
{
    public static SecretKey makeSymmetricKey(
        int             algorithm,
        byte[]          keyBytes)
        throws PGPException
    {
        String    algName = org.spongycastle.openpgp.PGPUtil.getSymmetricCipherName(algorithm);

        if (algName == null)
        {
            throw new PGPException("unknown symmetric algorithm: " + algorithm);
        }

        return new SecretKeySpec(keyBytes, algName);
    }

    static ECPoint decodePoint(
        BigInteger encodedPoint,
        ECCurve curve)
        throws IOException
    {
        return curve.decodePoint(BigIntegers.asUnsignedByteArray(encodedPoint));
    }

    static X9ECParameters getX9Parameters(ASN1ObjectIdentifier curveOID)
    {
        return ECNamedCurveTable.getByOID(curveOID);
    }
}
