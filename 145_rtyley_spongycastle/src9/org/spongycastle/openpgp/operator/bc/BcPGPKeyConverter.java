package org.spongycastle.openpgp.operator.bc;

import java.io.IOException;
import java.util.Date;

import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.asn1.ASN1OctetString;
import org.spongycastle.asn1.DEROctetString;
import org.spongycastle.asn1.nist.NISTNamedCurves;
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.asn1.x9.X9ECPoint;
import org.spongycastle.bcpg.BCPGKey;
import org.spongycastle.bcpg.DSAPublicBCPGKey;
import org.spongycastle.bcpg.DSASecretBCPGKey;
import org.spongycastle.bcpg.ECDHPublicBCPGKey;
import org.spongycastle.bcpg.ECDSAPublicBCPGKey;
import org.spongycastle.bcpg.ECPublicBCPGKey;
import org.spongycastle.bcpg.ECSecretBCPGKey;
import org.spongycastle.bcpg.ElGamalPublicBCPGKey;
import org.spongycastle.bcpg.ElGamalSecretBCPGKey;
import org.spongycastle.bcpg.HashAlgorithmTags;
import org.spongycastle.bcpg.PublicKeyAlgorithmTags;
import org.spongycastle.bcpg.PublicKeyPacket;
import org.spongycastle.bcpg.RSAPublicBCPGKey;
import org.spongycastle.bcpg.RSASecretBCPGKey;
import org.spongycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.spongycastle.crypto.params.AsymmetricKeyParameter;
import org.spongycastle.crypto.params.DSAParameters;
import org.spongycastle.crypto.params.DSAPrivateKeyParameters;
import org.spongycastle.crypto.params.DSAPublicKeyParameters;
import org.spongycastle.crypto.params.ECNamedDomainParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.crypto.params.ElGamalParameters;
import org.spongycastle.crypto.params.ElGamalPrivateKeyParameters;
import org.spongycastle.crypto.params.ElGamalPublicKeyParameters;
import org.spongycastle.crypto.params.RSAKeyParameters;
import org.spongycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.spongycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.spongycastle.openpgp.PGPAlgorithmParameters;
import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.PGPKdfParameters;
import org.spongycastle.openpgp.PGPPrivateKey;
import org.spongycastle.openpgp.PGPPublicKey;

public class BcPGPKeyConverter
{
    /**
     * Create a PGPPublicKey from the passed in JCA one.
     * <p>
     * Note: the time passed in affects the value of the key's keyID, so you probably only want
     * to do this once for a JCA key, or make sure you keep track of the time you used.
     * </p>
     * @param algorithm asymmetric algorithm type representing the public key.
     * @param pubKey    actual public key to associate.
     * @param time      date of creation.
     * @throws PGPException on key creation problem.
     */
    public PGPPublicKey getPGPPublicKey(int algorithm, PGPAlgorithmParameters algorithmParameters, AsymmetricKeyParameter pubKey, Date time)
        throws PGPException
    {
        BCPGKey bcpgKey;

        if (pubKey instanceof RSAKeyParameters)
        {
            RSAKeyParameters rK = (RSAKeyParameters)pubKey;

            bcpgKey = new RSAPublicBCPGKey(rK.getModulus(), rK.getExponent());
        }
        else if (pubKey instanceof DSAPublicKeyParameters)
        {
            DSAPublicKeyParameters dK = (DSAPublicKeyParameters)pubKey;
            DSAParameters dP = dK.getParameters();

            bcpgKey = new DSAPublicBCPGKey(dP.getP(), dP.getQ(), dP.getG(), dK.getY());
        }
        else if (pubKey instanceof ElGamalPublicKeyParameters)
        {
            ElGamalPublicKeyParameters eK = (ElGamalPublicKeyParameters)pubKey;
            ElGamalParameters eS = eK.getParameters();

            bcpgKey = new ElGamalPublicBCPGKey(eS.getP(), eS.getG(), eK.getY());
        }
        else if (pubKey instanceof ECPublicKeyParameters)
        {
            SubjectPublicKeyInfo keyInfo;
            try
            {
                keyInfo = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(pubKey);
            }
            catch (IOException e)
            {
                throw new PGPException("Unable to encode key: " + e.getMessage(), e);
            }

            // TODO: should probably match curve by comparison as well
            ASN1ObjectIdentifier curveOid = ASN1ObjectIdentifier.getInstance(keyInfo.getAlgorithm().getParameters());

            X9ECParameters params = NISTNamedCurves.getByOID(curveOid);

            ASN1OctetString key = new DEROctetString(keyInfo.getPublicKeyData().getBytes());
            X9ECPoint derQ = new X9ECPoint(params.getCurve(), key);

            if (algorithm == PGPPublicKey.ECDH)
            {
                PGPKdfParameters kdfParams = (PGPKdfParameters)algorithmParameters;
                if (kdfParams == null)
                {
                    // We default to these as they are specified as mandatory in RFC 6631.
                    kdfParams = new PGPKdfParameters(HashAlgorithmTags.SHA256, SymmetricKeyAlgorithmTags.AES_128);
                }
                bcpgKey = new ECDHPublicBCPGKey(curveOid, derQ.getPoint(), kdfParams.getHashAlgorithm(), kdfParams.getSymmetricWrapAlgorithm());
            }
            else if (algorithm == PGPPublicKey.ECDSA)
            {
                bcpgKey = new ECDSAPublicBCPGKey(curveOid, derQ.getPoint());
            }
            else
            {
                throw new PGPException("unknown EC algorithm");
            }
        }
        else
        {
            throw new PGPException("unknown key class");
        }

        return new PGPPublicKey(new PublicKeyPacket(algorithm, time, bcpgKey), new BcKeyFingerprintCalculator());
    }

    public PGPPrivateKey getPGPPrivateKey(PGPPublicKey pubKey, AsymmetricKeyParameter privKey)
        throws PGPException
    {
        BCPGKey privPk;

        switch (pubKey.getAlgorithm())
        {
        case PGPPublicKey.RSA_ENCRYPT:
        case PGPPublicKey.RSA_SIGN:
        case PGPPublicKey.RSA_GENERAL:
            RSAPrivateCrtKeyParameters rsK = (RSAPrivateCrtKeyParameters)privKey;

            privPk = new RSASecretBCPGKey(rsK.getExponent(), rsK.getP(), rsK.getQ());
            break;
        case PGPPublicKey.DSA:
            DSAPrivateKeyParameters dsK = (DSAPrivateKeyParameters)privKey;

            privPk = new DSASecretBCPGKey(dsK.getX());
            break;
        case PGPPublicKey.ELGAMAL_ENCRYPT:
        case PGPPublicKey.ELGAMAL_GENERAL:
            ElGamalPrivateKeyParameters esK = (ElGamalPrivateKeyParameters)privKey;

            privPk = new ElGamalSecretBCPGKey(esK.getX());
            break;
        case PGPPublicKey.ECDH:
        case PGPPublicKey.ECDSA:
            ECPrivateKeyParameters ecK = (ECPrivateKeyParameters)privKey;

            privPk = new ECSecretBCPGKey(ecK.getD());
            break;
        default:
            throw new PGPException("unknown key class");
        }
        return new PGPPrivateKey(pubKey.getKeyID(), pubKey.getPublicKeyPacket(), privPk);
    }

    public AsymmetricKeyParameter getPublicKey(PGPPublicKey publicKey)
        throws PGPException
    {
        PublicKeyPacket publicPk = publicKey.getPublicKeyPacket();

        try
        {
            switch (publicPk.getAlgorithm())
            {
            case PublicKeyAlgorithmTags.RSA_ENCRYPT:
            case PublicKeyAlgorithmTags.RSA_GENERAL:
            case PublicKeyAlgorithmTags.RSA_SIGN:
                RSAPublicBCPGKey rsaK = (RSAPublicBCPGKey)publicPk.getKey();

                return new RSAKeyParameters(false, rsaK.getModulus(), rsaK.getPublicExponent());
            case PublicKeyAlgorithmTags.DSA:
                DSAPublicBCPGKey dsaK = (DSAPublicBCPGKey)publicPk.getKey();

                return new DSAPublicKeyParameters(dsaK.getY(), new DSAParameters(dsaK.getP(), dsaK.getQ(), dsaK.getG()));
            case PublicKeyAlgorithmTags.ELGAMAL_ENCRYPT:
            case PublicKeyAlgorithmTags.ELGAMAL_GENERAL:
                ElGamalPublicBCPGKey elK = (ElGamalPublicBCPGKey)publicPk.getKey();

                return new ElGamalPublicKeyParameters(elK.getY(), new ElGamalParameters(elK.getP(), elK.getG()));
            case PGPPublicKey.ECDH:
            case PGPPublicKey.ECDSA:
                ECPublicBCPGKey ecPub = (ECPublicBCPGKey)publicPk.getKey();
                X9ECParameters x9 = BcUtil.getX9Parameters(ecPub.getCurveOID());

                return new ECPublicKeyParameters(BcUtil.decodePoint(ecPub.getEncodedPoint(), x9.getCurve()),
                    new ECNamedDomainParameters(ecPub.getCurveOID(), x9.getCurve(), x9.getG(), x9.getN(), x9.getH()));
            default:
                throw new PGPException("unknown public key algorithm encountered");
            }
        }
        catch (PGPException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new PGPException("exception constructing public key", e);
        }
    }

    public AsymmetricKeyParameter getPrivateKey(PGPPrivateKey privKey)
        throws PGPException
    {
        PublicKeyPacket pubPk = privKey.getPublicKeyPacket();
        BCPGKey privPk = privKey.getPrivateKeyDataPacket();

        try
        {
            switch (pubPk.getAlgorithm())
            {
            case PGPPublicKey.RSA_ENCRYPT:
            case PGPPublicKey.RSA_GENERAL:
            case PGPPublicKey.RSA_SIGN:
                RSAPublicBCPGKey rsaPub = (RSAPublicBCPGKey)pubPk.getKey();
                RSASecretBCPGKey rsaPriv = (RSASecretBCPGKey)privPk;

                return new RSAPrivateCrtKeyParameters(rsaPriv.getModulus(), rsaPub.getPublicExponent(), rsaPriv.getPrivateExponent(), rsaPriv.getPrimeP(), rsaPriv.getPrimeQ(), rsaPriv.getPrimeExponentP(), rsaPriv.getPrimeExponentQ(), rsaPriv.getCrtCoefficient());
            case PGPPublicKey.DSA:
                DSAPublicBCPGKey dsaPub = (DSAPublicBCPGKey)pubPk.getKey();
                DSASecretBCPGKey dsaPriv = (DSASecretBCPGKey)privPk;

                return new DSAPrivateKeyParameters(dsaPriv.getX(), new DSAParameters(dsaPub.getP(), dsaPub.getQ(), dsaPub.getG()));
            case PGPPublicKey.ELGAMAL_ENCRYPT:
            case PGPPublicKey.ELGAMAL_GENERAL:
                ElGamalPublicBCPGKey elPub = (ElGamalPublicBCPGKey)pubPk.getKey();
                ElGamalSecretBCPGKey elPriv = (ElGamalSecretBCPGKey)privPk;

                return new ElGamalPrivateKeyParameters(elPriv.getX(), new ElGamalParameters(elPub.getP(), elPub.getG()));
            case PGPPublicKey.ECDH:
            case PGPPublicKey.ECDSA:
                ECPublicBCPGKey ecPub = (ECPublicBCPGKey)pubPk.getKey();
                ECSecretBCPGKey ecPriv = (ECSecretBCPGKey)privPk;

                X9ECParameters x9 = BcUtil.getX9Parameters(ecPub.getCurveOID());

                return new ECPrivateKeyParameters(ecPriv.getX(),
                    new ECNamedDomainParameters(ecPub.getCurveOID(), x9.getCurve(), x9.getG(), x9.getN(), x9.getH()));
            default:
                throw new PGPException("unknown public key algorithm encountered");
            }
        }
        catch (PGPException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new PGPException("Exception constructing key", e);
        }
    }
}
