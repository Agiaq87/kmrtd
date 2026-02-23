/*
 * JMRTD - A Java API for accessing machine readable travel documents.
 *
 * Copyright (C) 2006 - 2018  The JMRTD team
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * $Id: SODFile.java 1861 2021-10-26 09:12:59Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds

import kmrtd.lds.LDSFile.Companion.EF_SOD_TAG
import kmrtd.lds.SignedDataUtil.createSignedData
import kmrtd.lds.SignedDataUtil.lookupMnemonicByOID
import kmrtd.lds.SignedDataUtil.lookupOIDByMnemonic
import kmrtd.lds.SignedDataUtil.readSignedData
import kmrtd.lds.SignedDataUtil.signData
import kmrtd.lds.SignedDataUtil.writeData
import org.bouncycastle.asn1.*
import org.bouncycastle.asn1.cms.ContentInfo
import org.bouncycastle.asn1.cms.SignedData
import org.bouncycastle.asn1.icao.DataGroupHash
import org.bouncycastle.asn1.icao.LDSSecurityObject
import org.bouncycastle.asn1.icao.LDSVersionInfo
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.math.BigInteger
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.SignatureException
import java.security.cert.X509Certificate
import java.security.spec.AlgorithmParameterSpec
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import javax.security.auth.x500.X500Principal

/**
 * File structure for the EF_SOD file (the Document Security Object).
 * Based on Appendix 3 of Doc 9303 Part 1 Vol 2.
 * 
 * Basically the Document Security Object is a SignedData type as specified in
 * [RFC 3369](http://www.ietf.org/rfc/rfc3369.txt).
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1861 $
 */
class SODFile : AbstractTaggedLDSFile {
    /*
       * FIXME: This field is now transient, but probably shouldn't be!
       *
       * - We can either leave this transient and explicitly (de)serialize it in
       *   readObject/writeObject (using BC's getEncoded())
       * - Or replace this field with something that implements Serializable and that we control.
       */
    @Transient
    private var signedData: SignedData? = null

    /**
     * Constructs a Security Object data structure using a specified signature provider.
     * 
     * @param digestAlgorithm a digest algorithm, such as "SHA-1" or "SHA-256"
     * @param digestEncryptionAlgorithm a digest encryption algorithm, such as "SHA256withRSA"
     * @param dataGroupHashes maps datagroup numbers (1 to 16) to hashes of the data groups
     * @param privateKey private key to sign the data
     * @param docSigningCertificate the document signing certificate
     * @param provider specific signature provider that should be used to create the signature
     * @param ldsVersion LDS version
     * @param unicodeVersion Unicode version
     * 
     * @throws GeneralSecurityException if either of the algorithm parameters is not recognized, or if the document signing certificate cannot be used
     */
    /**
     * Constructs a Security Object data structure using a specified signature provider.
     * 
     * @param digestAlgorithm a digest algorithm, such as "SHA-1" or "SHA-256"
     * @param digestEncryptionAlgorithm a digest encryption algorithm, such as "SHA256withRSA"
     * @param dataGroupHashes maps datagroup numbers (1 to 16) to hashes of the data groups
     * @param privateKey private key to sign the contents
     * @param docSigningCertificate the document signing certificate to embed
     * @param provider specific signature provider that should be used to create the signature
     * 
     * @throws GeneralSecurityException if either of the algorithm parameters is not recognized, or if the document signing certificate cannot be used
     */
    /**
     * Constructs a Security Object data structure.
     * 
     * @param digestAlgorithm a digest algorithm, such as "SHA1" or "SHA256"
     * @param digestEncryptionAlgorithm a digest encryption algorithm, such as "SHA256withRSA"
     * @param dataGroupHashes maps datagroup numbers (1 to 16) to hashes of the data groups
     * @param privateKey private key to sign the data
     * @param docSigningCertificate the document signing certificate
     * 
     * @throws GeneralSecurityException if either of the algorithm parameters is not recognized, or if the document signing certificate cannot be used
     */
    @JvmOverloads
    constructor(
        digestAlgorithm: String?, digestEncryptionAlgorithm: String,
        dataGroupHashes: MutableMap<Int?, ByteArray>,
        privateKey: PrivateKey?,
        docSigningCertificate: X509Certificate, provider: String? = null,
        ldsVersion: String? = null, unicodeVersion: String = null
    ) : super(EF_SOD_TAG) {
        try {
            val contentInfo: ContentInfo =
                toContentInfo(ICAO_LDS_SOD_OID, digestAlgorithm, dataGroupHashes, ldsVersion, unicodeVersion)
            val encryptedDigest = signData(
                digestAlgorithm,
                digestEncryptionAlgorithm,
                ICAO_LDS_SOD_OID,
                contentInfo,
                privateKey,
                provider
            )

            signedData = SignedDataUtil.createSignedData(
                digestAlgorithm,
                digestEncryptionAlgorithm,
                ICAO_LDS_SOD_OID, contentInfo,
                encryptedDigest!!, docSigningCertificate
            )
        } catch (ioe: IOException) {
            throw IllegalArgumentException("Error creating signedData", ioe)
        }
    }

    /**
     * Constructs a Security Object data structure using a specified signature provider.
     * 
     * @param digestAlgorithm a digest algorithm, such as "SHA-1" or "SHA-256"
     * @param digestEncryptionAlgorithm a digest encryption algorithm, such as "SHA256withRSA"
     * @param digestEncryptionParameters the digest encryption algorithm parameters
     * @param dataGroupHashes maps datagroup numbers (1 to 16) to hashes of the data groups
     * @param privateKey private key to sign the data
     * @param docSigningCertificate the document signing certificate
     * @param provider specific signature provider that should be used to create the signature
     * @param ldsVersion LDS version
     * @param unicodeVersion Unicode version
     * 
     * @throws GeneralSecurityException if either of the algorithm parameters is not recognized, or if the document signing certificate cannot be used
     */
    /**
     * Constructs a Security Object data structure using a specified signature provider.
     * 
     * @param digestAlgorithm a digest algorithm, such as "SHA-1" or "SHA-256"
     * @param digestEncryptionAlgorithm a digest encryption algorithm, such as "SHA256withRSA"
     * @param digestEncryptionParameters the digest encryption algorithm parameters
     * @param dataGroupHashes maps datagroup numbers (1 to 16) to hashes of the data groups
     * @param privateKey private key to sign the contents
     * @param docSigningCertificate the document signing certificate to embed
     * @param provider specific signature provider that should be used to create the signature
     * 
     * @throws GeneralSecurityException if either of the algorithm parameters is not recognized, or if the document signing certificate cannot be used
     */
    /**
     * Constructs a Security Object data structure.
     * 
     * @param digestAlgorithm a digest algorithm, such as "SHA1" or "SHA256"
     * @param digestEncryptionAlgorithm a digest encryption algorithm, such as "SHA256withRSA"
     * @param digestEncryptionParameters the digest encryption algorithm parameters
     * @param dataGroupHashes maps datagroup numbers (1 to 16) to hashes of the data groups
     * @param privateKey private key to sign the data
     * @param docSigningCertificate the document signing certificate
     * 
     * @throws GeneralSecurityException if either of the algorithm parameters is not recognized, or if the document signing certificate cannot be used
     */
    @JvmOverloads
    constructor(
        digestAlgorithm: String?, digestEncryptionAlgorithm: String,
        digestEncryptionParameters: AlgorithmParameterSpec?,
        dataGroupHashes: MutableMap<Int?, ByteArray>,
        privateKey: PrivateKey?,
        docSigningCertificate: X509Certificate, provider: String? = null,
        ldsVersion: String? = null, unicodeVersion: String = null
    ) : super(EF_SOD_TAG) {
        try {
            val contentInfo: ContentInfo =
                toContentInfo(ICAO_LDS_SOD_OID, digestAlgorithm, dataGroupHashes, ldsVersion, unicodeVersion)
            val encryptedDigest = signData(
                digestAlgorithm,
                digestEncryptionAlgorithm,
                digestEncryptionParameters,
                ICAO_LDS_SOD_OID,
                contentInfo,
                privateKey,
                provider
            )

            signedData = createSignedData(
                digestAlgorithm,
                digestEncryptionAlgorithm,
                digestEncryptionParameters,
                ICAO_LDS_SOD_OID, contentInfo,
                encryptedDigest!!, docSigningCertificate
            )
        } catch (ioe: IOException) {
            throw IllegalArgumentException("Error creating signedData", ioe)
        }
    }

    /**
     * Constructs a Security Object data structure.
     * 
     * @param digestAlgorithm a digest algorithm, such as "SHA-1" or "SHA-256"
     * @param digestEncryptionAlgorithm a digest encryption algorithm, such as "SHA256withRSA"
     * @param dataGroupHashes maps datagroup numbers (1 to 16) to hashes of the data groups
     * @param encryptedDigest externally signed contents
     * @param docSigningCertificate the document signing certificate
     * 
     * @throws GeneralSecurityException if either of the algorithm parameters is not recognized, or if the document signing certificate cannot be used
     */
    constructor(
        digestAlgorithm: String?, digestEncryptionAlgorithm: String,
        dataGroupHashes: MutableMap<Int?, ByteArray>,
        encryptedDigest: ByteArray,
        docSigningCertificate: X509Certificate
    ) : super(EF_SOD_TAG) {
        requireNotNull(dataGroupHashes) { "Cannot construct security object from null datagroup hashes" }

        try {
            signedData = createSignedData(
                digestAlgorithm,
                digestEncryptionAlgorithm,
                ICAO_LDS_SOD_OID,
                Companion.toContentInfo(ICAO_LDS_SOD_OID, digestAlgorithm, dataGroupHashes, null, null),
                encryptedDigest,
                docSigningCertificate
            )
        } catch (ioe: IOException) {
            throw IllegalArgumentException("Error creating signedData", ioe)
        }
    }

    /**
     * Constructs a Security Object data structure.
     * 
     * @param digestAlgorithm a digest algorithm, such as "SHA-1" or "SHA-256"
     * @param digestEncryptionAlgorithm a digest encryption algorithm, such as "SHA256withRSA"
     * @param digestEncryptionParameters the digest encryption algorithm parameters
     * @param dataGroupHashes maps datagroup numbers (1 to 16) to hashes of the data groups
     * @param encryptedDigest externally signed contents
     * @param docSigningCertificate the document signing certificate
     * 
     * @throws GeneralSecurityException if either of the algorithm parameters is not recognized, or if the document signing certificate cannot be used
     */
    constructor(
        digestAlgorithm: String?, digestEncryptionAlgorithm: String,
        digestEncryptionParameters: AlgorithmParameterSpec?,
        dataGroupHashes: MutableMap<Int?, ByteArray>,
        encryptedDigest: ByteArray,
        docSigningCertificate: X509Certificate
    ) : super(EF_SOD_TAG) {
        requireNotNull(dataGroupHashes) { "Cannot construct security object from null datagroup hashes" }

        try {
            signedData = createSignedData(
                digestAlgorithm,
                digestEncryptionAlgorithm,
                digestEncryptionParameters,
                ICAO_LDS_SOD_OID,
                Companion.toContentInfo(ICAO_LDS_SOD_OID, digestAlgorithm, dataGroupHashes, null, null),
                encryptedDigest,
                docSigningCertificate
            )
        } catch (ioe: IOException) {
            throw IllegalArgumentException("Error creating signedData", ioe)
        }
    }


    /**
     * Constructs a Security Object data structure.
     * 
     * @param inputStream some inputstream
     * 
     * @throws IOException if something goes wrong
     */
    constructor(inputStream: InputStream?) : super(EF_SOD_TAG, inputStream) {
        /* Will throw IAE if no signer info. */
        SignedDataUtil.getSignerInfo(signedData!!)
    }

    @Throws(IOException::class)
    override fun readContent(inputStream: InputStream?) {
        this.signedData = readSignedData(inputStream)
    }

    @Throws(IOException::class)
    override fun writeContent(outputStream: OutputStream) {
        writeData(this.signedData, outputStream)
    }

    val dataGroupHashes: MutableMap<Int?, ByteArray?>
        /**
         * Returns the stored data group hashes indexed by data group number.
         * 
         * @return data group hashes indexed by data group number (1 to 16)
         */
        get() {
            val hashObjects: Array<DataGroupHash> =
                Companion.getLDSSecurityObject(signedData!!).getDatagroupHash()
            val hashMap: MutableMap<Int?, ByteArray?> =
                TreeMap<Int?, ByteArray?>() /* HashMap... get it? :D (not funny anymore, now that it's a TreeMap.) */
            for (hashObject in hashObjects) {
                val number = hashObject.getDataGroupNumber()
                val hashValue = hashObject.getDataGroupHashValue().getOctets()
                hashMap.put(number, hashValue)
            }
            return hashMap
        }

    val encryptedDigest: ByteArray?
        /**
         * Returns the signature (the encrypted digest) over the hashes.
         * 
         * @return the encrypted digest
         */
        get() = SignedDataUtil.getEncryptedDigest(signedData!!)

    val digestEncryptionAlgorithmParams: AlgorithmParameterSpec?
        /**
         * Returns the parameters of the digest encryption (signature) algorithm.
         * For instance for `"RSASSA/PSS"` this includes the hash algorithm
         * and the salt length.
         * 
         * @return the algorithm parameters
         */
        get() = SignedDataUtil.getDigestEncryptionAlgorithmParams(signedData!!)

    @get:Throws(SignatureException::class)
    val eContent: ByteArray?
        /**
         * Returns the encoded contents of the signed data over which the
         * signature is to be computed.
         * 
         * @return the encoded contents
         * 
         * @throws SignatureException if the contents do not check out
         */
        get() = SignedDataUtil.getEContent(signedData!!)

    val digestAlgorithm: String?
        /**
         * Returns the name of the algorithm used in the data group hashes.
         * 
         * @return an algorithm string such as "SHA-1" or "SHA-256"
         */
        get() = getDigestAlgorithm(
            Companion.getLDSSecurityObject(
                signedData!!
            )
        )

    val signerInfoDigestAlgorithm: String?
        /**
         * Returns the name of the digest algorithm used in the signature.
         * 
         * @return an algorithm string such as "SHA-1" or "SHA-256"
         */
        get() = SignedDataUtil.getSignerInfoDigestAlgorithm(signedData!!)

    val digestEncryptionAlgorithm: String?
        /**
         * Returns the name of the digest encryption algorithm used in the signature.
         * 
         * @return an algorithm string such as "SHA256withRSA"
         */
        get() = SignedDataUtil.getDigestEncryptionAlgorithm(signedData!!)

    val lDSVersion: String?
        /**
         * Returns the version of the LDS if stored in the Security Object (SOd).
         * 
         * @return the version of the LDS in "aabb" format or null if LDS &lt; V1.8
         * 
         * @since LDS V1.8
         */
        get() {
            val ldsVersionInfo: LDSVersionInfo? =
                Companion.getLDSSecurityObject(signedData!!).versionInfo
            if (ldsVersionInfo == null) {
                return null
            } else {
                return ldsVersionInfo.ldsVersion
            }
        }

    val unicodeVersion: String?
        /**
         * Returns the version of unicode if stored in the Security Object (SOd).
         * 
         * @return the unicode version in "aabbcc" format or null if LDS &lt; V1.8
         * 
         * @since LDS V1.8
         */
        get() {
            val ldsVersionInfo: LDSVersionInfo? =
                Companion.getLDSSecurityObject(signedData!!).versionInfo
            if (ldsVersionInfo == null) {
                return null
            } else {
                return ldsVersionInfo.unicodeVersion
            }
        }

    val docSigningCertificates: MutableList<X509Certificate>
        /**
         * Returns any embedded (document signing) certificates.
         * 
         * If the document signing certificate is embedded, a list of size 1 is returned.
         * If a document signing certificate is not embedded, the empty list is returned.
         * 
         * Doc 9303 part 10 (in our interpretation) does not allow multiple certificates
         * here, PKCS7 does allow this.
         * 
         * @return the document signing certificate
         */
        get() = SignedDataUtil.getCertificates(signedData!!)

    val docSigningCertificate: X509Certificate?
        /**
         * Returns the embedded document signing certificate (if present) or
         * `null` if not present.
         * Use this certificate to verify that *eSignature* is a valid
         * signature for *eContent*. This certificate itself is signed
         * using the country signing certificate.
         * 
         * @return the document signing certificate
         */
        get() {
            val certificates =
                this.docSigningCertificates
            if (certificates == null || certificates.isEmpty()) {
                return null
            }

            return certificates[certificates.size - 1]
        }

    val issuerX500Principal: X500Principal?
        /**
         * Returns the issuer name of the document signing certificate
         * as it appears in the signer-info in the signed-data structure
         * This returns `null` when the signer is identified through
         * subject-key-identifier instead.
         * 
         * @return a certificate issuer, or `null` if not present
         */
        get() {
            try {
                val issuerAndSerialNumber =
                    SignedDataUtil.getIssuerAndSerialNumber(signedData!!)
                if (issuerAndSerialNumber == null) {
                    return null
                }

                val name = issuerAndSerialNumber.name
                if (name == null) {
                    return null
                }

                return X500Principal(name.getEncoded(ASN1Encoding.DER))
            } catch (ioe: IOException) {
                LOGGER.log(Level.WARNING, "Could not get issuer", ioe)
                return null
            }
        }

    val serialNumber: BigInteger?
        /**
         * Returns the serial number as it appears in the signer-info in the
         * signed-data structure.
         * This returns `null` when the signer is identified through
         * subject-key-identifier instead.
         * 
         * @return a certificate serial number, or `null` if not present
         */
        get() {
            val issuerAndSerialNumber =
                SignedDataUtil.getIssuerAndSerialNumber(signedData!!)
            if (issuerAndSerialNumber == null) {
                return null
            }

            return issuerAndSerialNumber.serialNumber.value
        }

    val subjectKeyIdentifier: ByteArray?
        /**
         * Returns the signer's subject-key-identifier as it appears in the signer-info
         * in the signed-data structure.
         * This returns `null` when the signer is identified through
         * issuer name and serial instead.
         * 
         * @return the subject-key-identifier, or `null` if not present
         */
        get() = SignedDataUtil.getSubjectKeyIdentifier(signedData!!)

    /**
     * Returns a textual representation of this file.
     * 
     * @return a textual representation of this file
     */
    override fun toString(): String {
        try {
            val result = StringBuilder()
            result.append("SODFile ")
            val certificates =
                this.docSigningCertificates
            for (certificate in certificates) {
                result.append(certificate.issuerX500Principal.name)
                result.append(", ")
            }
            return result.toString()
        } catch (e: Exception) {
            LOGGER.log(Level.WARNING, "Unexpected exception", e)
            return "SODFile"
        }
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (obj === this) {
            return true
        }
        if (obj.javaClass != this.javaClass) {
            return false
        }

        val other = obj as SODFile
        return getEncoded().contentEquals(other.getEncoded())
    }

    override fun hashCode(): Int {
        return 11 * getEncoded().contentHashCode() + 111
    }

    companion object {
        private val serialVersionUID = -1081347374739311111L

        //  private static final String SHA1_HASH_ALG_OID = "1.3.14.3.2.26";
        //  private static final String SHA1_WITH_RSA_ENC_OID = "1.2.840.113549.1.1.5";
        //  private static final String SHA256_HASH_ALG_OID = "2.16.840.1.101.3.4.2.1";
        //  private static final String E_CONTENT_TYPE_OID = "1.2.528.1.1006.1.20.1";
        /**
         * The object identifier to indicate content-type in encapContentInfo.
         * 
         * <pre>
         * id-icao-ldsSecurityObject OBJECT IDENTIFIER ::=
         * {joint-iso-itu-t(2) international-organizations(23) icao(136) mrtd(1) security(1) ldsSecurityObject(1)}
        </pre> * 
         */
        private const val ICAO_LDS_SOD_OID = "2.23.136.1.1.1"

        /**
         * This TC_SOD_IOD is apparently used in
         * "PKI for Machine Readable Travel Documents Offering ICC Read-Only Access Version - 1.1, Annex C".
         * Seen in live French and Belgian MRTDs.
         * 
         * <pre>
         * id-icao-ldsSecurityObjectid OBJECT IDENTIFIER ::=
         * {iso(1) identified-organization(3) icao(27) atn-end-system-air(1) security(1) ldsSecurityObject(1)}
        </pre> * 
         */
        private const val ICAO_LDS_SOD_ALT_OID = "1.3.27.1.1.1"

        /**
         * This is used in some test MRTDs.
         * Appears to have been included in a "worked example" somewhere and perhaps used in live documents.
         * 
         * <pre>
         * id-sdu-ldsSecurityObjectid OBJECT IDENTIFIER :=
         * {iso(1) member-body(2) nl(528) nederlandse-organisatie(1) enschede-sdu(1006) 1 20 1}
        </pre> * 
         */
        private const val SDU_LDS_SOD_OID = "1.2.528.1.1006.1.20.1"

        private val LOGGER: Logger = Logger.getLogger("kmrtd")

        /**
         * Extracts the digest algorithm from the security object.
         * 
         * @param ldsSecurityObject the security object
         * 
         * @return a mnemonic (Java JCE) string representation of the digest algorithm
         */
        private fun getDigestAlgorithm(ldsSecurityObject: LDSSecurityObject): String? {
            try {
                return lookupMnemonicByOID(ldsSecurityObject.getDigestAlgorithmIdentifier().getAlgorithm().getId())
            } catch (nsae: NoSuchAlgorithmException) {
                LOGGER.log(Level.WARNING, "Exception", nsae)
                return null
            }
        }

        /* ONLY PRIVATE METHODS BELOW */
        /**
         * Encodes a content info for the hash table.
         * 
         * @param contentTypeOID the content info OID to use
         * @param digestAlgorithm the digest algorithm
         * @param dataGroupHashes the hash table
         * @param ldsVersion the LDS version
         * @param unicodeVersion the Unicode version
         * 
         * @return the content info
         * 
         * @throws NoSuchAlgorithmException on error
         * @throws IOException on error writing to memory
         */
        @Throws(NoSuchAlgorithmException::class, IOException::class)
        private fun toContentInfo(
            contentTypeOID: String, digestAlgorithm: String?,
            dataGroupHashes: MutableMap<Int?, ByteArray>,
            ldsVersion: String?, unicodeVersion: String
        ): ContentInfo {
            val dataGroupHashesArray = arrayOfNulls<DataGroupHash>(dataGroupHashes.size)

            var i = 0
            for (entry in dataGroupHashes.entries) {
                val dataGroupNumber: Int = entry.key!!
                val hashBytes: ByteArray = dataGroupHashes.get(dataGroupNumber)!!
                val hash = DataGroupHash(dataGroupNumber, DEROctetString(hashBytes))
                dataGroupHashesArray[i++] = hash
            }

            val digestAlgorithmIdentifier =
                AlgorithmIdentifier(ASN1ObjectIdentifier(lookupOIDByMnemonic(digestAlgorithm)))
            var securityObject: LDSSecurityObject? = null
            if (ldsVersion == null) {
                securityObject = LDSSecurityObject(digestAlgorithmIdentifier, dataGroupHashesArray)
            } else {
                securityObject = LDSSecurityObject(
                    digestAlgorithmIdentifier,
                    dataGroupHashesArray,
                    LDSVersionInfo(ldsVersion, unicodeVersion)
                )
            }

            return ContentInfo(ASN1ObjectIdentifier(contentTypeOID), DEROctetString(securityObject))
        }

        /**
         * Reads the security object (containing the hashes
         * of the data groups) found in the `SignedData` field.
         * 
         * @param signedData the signed data to read from
         * 
         * @return the security object
         * 
         * @throws IOException on error parsing the signed data
         */
        private fun getLDSSecurityObject(signedData: SignedData): LDSSecurityObject {
            try {
                val encapContentInfo = signedData.encapContentInfo
                val contentType = encapContentInfo.contentType.getId()
                val eContent = encapContentInfo.content as ASN1OctetString
                if (!(ICAO_LDS_SOD_OID == contentType
                            || SDU_LDS_SOD_OID == contentType
                            || ICAO_LDS_SOD_ALT_OID == contentType)
                ) {
                    LOGGER.warning("SignedData does not appear to contain an LDS SOd. (content type is $contentType, was expecting $ICAO_LDS_SOD_OID)")
                }
                val inputStream = ASN1InputStream(ByteArrayInputStream(eContent.octets))
                try {
                    val firstObject: Any = inputStream.readObject()
                    check(firstObject is ASN1Sequence) { "Expected ASN1Sequence, found " + firstObject.javaClass.simpleName }
                    val sod = LDSSecurityObject.getInstance(firstObject)
                    val nextObject: Any? = inputStream.readObject()
                    if (nextObject != null) {
                        LOGGER.warning("Ignoring extra object found after LDSSecurityObject...")
                    }
                    return sod
                } finally {
                    inputStream.close()
                }
            } catch (ioe: IOException) {
                throw IllegalStateException("Could not read security object in signedData", ioe)
            }
        }
    }
}
