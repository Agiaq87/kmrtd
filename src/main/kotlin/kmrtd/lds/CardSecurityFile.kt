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
 * $Id: CardSecurityFile.java 1894 2025-03-19 20:00:46Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds

import kmrtd.lds.SecurityInfo.Companion.getInstance
import kmrtd.lds.SignedDataUtil.getCertificates
import kmrtd.lds.SignedDataUtil.getContent
import kmrtd.lds.SignedDataUtil.getDigestEncryptionAlgorithm
import kmrtd.lds.SignedDataUtil.getEncryptedDigest
import kmrtd.lds.SignedDataUtil.getSignerInfoDigestAlgorithm
import kmrtd.lds.SignedDataUtil.readSignedData
import kmrtd.lds.SignedDataUtil.signData
import kmrtd.lds.SignedDataUtil.writeData
import org.bouncycastle.asn1.*
import org.bouncycastle.asn1.cms.ContentInfo
import org.bouncycastle.asn1.cms.SignedData
import java.io.*
import java.security.GeneralSecurityException
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Card security file stores a set of SecurityInfos for PACE with Chip Authentication Mapping (CAM).
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1894 $
 * 
 * @since 0.5.6
 */
class CardSecurityFile : Serializable {
    /**
     * Returns the digest algorithm.
     * 
     * @return the digest algorithm
     */
    var digestAlgorithm: String? = null
        private set

    /**
     * Returns the signature algorithm.
     * 
     * @return the signature algorithm
     */
    var digestEncryptionAlgorithm: String? = null
        private set

    /** The security infos that make up this file.  */
    private var securityInfos: MutableSet<SecurityInfo>? = null

    /** The signature bytes.  */
    private var encryptedDigest: ByteArray? = null

    /** The embedded document signer certificate.  */
    private var certificate: X509Certificate? = null

    /**
     * Constructs a new file from the provided data.
     * 
     * @param digestAlgorithm the digest algorithm as Java mnemonic
     * @param digestEncryptionAlgorithm the signature algorithm as Java mnemonic
     * @param securityInfos a non-empty list of security infos
     * @param privateKey the private signing key
     * @param certificate the certificate to embed, which should correspond to the given private key
     * @param provider the security provider to use
     */
    /**
     * Constructs a new file from the provided data.
     * 
     * @param digestAlgorithm the digest algorithm as Java mnemonic
     * @param digestEncryptionAlgorithm the signature algorithm as Java mnemonic
     * @param securityInfos a non-empty list of security infos
     * @param privateKey the private signing key
     * @param certificate the certificate to embed, which should correspond to the given private key
     */
    @JvmOverloads
    constructor(
        digestAlgorithm: String?,
        digestEncryptionAlgorithm: String,
        securityInfos: MutableCollection<SecurityInfo>,
        privateKey: PrivateKey?,
        certificate: X509Certificate,
        provider: String? = null
    ) : this(digestAlgorithm, digestEncryptionAlgorithm, securityInfos, null as ByteArray?, certificate) {
        val contentInfo: ContentInfo = toContentInfo(CONTENT_TYPE_OID, securityInfos)
        this.encryptedDigest =
            signData(digestAlgorithm, digestEncryptionAlgorithm, CONTENT_TYPE_OID, contentInfo, privateKey, provider)
    }

    /**
     * Constructs a new file from the provided data.
     * 
     * @param digestAlgorithm the digest algorithm as Java mnemonic
     * @param digestEncryptionAlgorithm the signature algorithm as Java mnemonic
     * @param securityInfos a non-empty list of security infos
     * @param encryptedDigest the signature
     * @param certificate the certificate to embed
     */
    constructor(
        digestAlgorithm: String?,
        digestEncryptionAlgorithm: String?,
        securityInfos: MutableCollection<SecurityInfo>,
        encryptedDigest: ByteArray?,
        certificate: X509Certificate
    ) {
        //requireNotNull(securityInfos) { "Null securityInfos" }
        //requireNotNull(certificate) { "Null certificate" }

        this.digestAlgorithm = digestAlgorithm
        this.digestEncryptionAlgorithm = digestEncryptionAlgorithm
        this.securityInfos = HashSet<SecurityInfo>(securityInfos)
        this.encryptedDigest = encryptedDigest
        this.certificate = certificate
    }

    /**
     * Constructs a new file from the data in an input stream.
     * 
     * @param inputStream the input stream to parse the data from
     * 
     * @throws IOException on error reading input stream
     */
    constructor(inputStream: InputStream?) {
        readContent(inputStream)
    }

    /**
     * Returns the encrypted digest (signature bytes).
     * 
     * @return the encrypted digest
     */
    fun getEncryptedDigest(): ByteArray? {
        return if (encryptedDigest == null) null else encryptedDigest!!.copyOf(encryptedDigest!!.size)
    }

    /**
     * Reads the contents of this file from a stream.
     * 
     * @param inputStream the stream to read from
     * 
     * @throws IOException on error reading from the stream
     */
    @Throws(IOException::class)
    protected fun readContent(inputStream: InputStream?) {
        val signedData = readSignedData(inputStream)

        this.digestAlgorithm = getSignerInfoDigestAlgorithm(signedData)
        this.digestEncryptionAlgorithm = getDigestEncryptionAlgorithm(signedData)

        val certificates = getCertificates(signedData)
        this.certificate =
            if (certificates.isEmpty()) null else certificates[certificates.size - 1]
        this.securityInfos = getSecurityInfos(signedData)

        this.encryptedDigest = getEncryptedDigest(signedData)
    }

    /**
     * Writes the contents of this file to a stream.
     * 
     * @param outputStream the stream to write to
     * 
     * @throws IOException on error writing to the stream
     */
    @Throws(IOException::class)
    protected fun writeContent(outputStream: OutputStream) {
        try {
            val contentInfo: ContentInfo =  toContentInfo(CONTENT_TYPE_OID, securityInfos!!)
            val signedData = SignedDataUtil.createSignedData(
                digestAlgorithm,
                digestEncryptionAlgorithm!!,
                CONTENT_TYPE_OID,
                contentInfo,
                encryptedDigest!!,
                certificate!!
            )
            writeData(signedData, outputStream)
        } catch (ce: CertificateException) {
            throw IOException("Certificate exception during SignedData creation", ce)
        } catch (nsae: NoSuchAlgorithmException) {
            throw IOException("Unsupported algorithm", nsae)
        } catch (gse: GeneralSecurityException) {
            throw IOException("General security exception", gse)
        }
    }

    val encoded: ByteArray?
        /**
         * Returns a DER encoded of this file.
         * 
         * @return the encoded file
         */
        get() {
            val byteArrayOutputStream = ByteArrayOutputStream()
            try {
                writeContent(byteArrayOutputStream)
                byteArrayOutputStream.flush()
                return byteArrayOutputStream.toByteArray()
            } catch (ioe: IOException) {
                LOGGER.log(
                    Level.WARNING,
                    "Exception while encoding",
                    ioe
                )
                return null
            } finally {
                try {
                    byteArrayOutputStream.close()
                } catch (ioe: IOException) {
                    LOGGER.log(
                        Level.FINE,
                        "Error closing stream"
                    )
                }
            }
        }

    /**
     * Returns the security infos as an unordered collection.
     * 
     * @return security infos
     */
    fun getSecurityInfos(): MutableCollection<SecurityInfo?> {
        return Collections.unmodifiableCollection<SecurityInfo?>(securityInfos)
    }

    @get:Deprecated("Use filter utility functions in {@code SignedDataUtil} instead.")
    val pACEInfos: MutableCollection<PACEInfo?>
        /**
         * Returns the PACE infos embedded in this card access file.
         * If no infos are present, an empty list is returned.
         * 
         * @return a list of PACE infos
         * 
         */
        get() {
            val paceInfos: MutableList<PACEInfo?> =
                ArrayList<PACEInfo?>(securityInfos!!.size)
            for (securityInfo in securityInfos!!) {
                if (securityInfo is PACEInfo) {
                    paceInfos.add(securityInfo)
                }
            }
            return paceInfos
        }

    @get:Deprecated("Use filter utility functions in {@code SignedDataUtil} instead.")
    val chipAuthenticationInfos: MutableCollection<ChipAuthenticationInfo?>
        /**
         * Returns the CA public key infos embedded in this card access file.
         * If no infos are present, an empty list is returned.
         * 
         * @return a list of CA public key infos
         * 
         */
        get() {
            val chipAuthenticationInfos: MutableList<ChipAuthenticationInfo?> =
                ArrayList<ChipAuthenticationInfo?>(securityInfos!!.size)
            for (securityInfo in securityInfos!!) {
                if (securityInfo is ChipAuthenticationInfo) {
                    chipAuthenticationInfos.add(securityInfo)
                }
            }
            return chipAuthenticationInfos
        }

    @get:Deprecated("Use filter utility functions in {@code SignedDataUtil} instead.")
    val chipAuthenticationPublicKeyInfos: MutableCollection<ChipAuthenticationPublicKeyInfo?>
        /**
         * Returns the CA public key infos embedded in this card access file.
         * If no infos are present, an empty list is returned.
         * 
         * @return a list of CA public key infos
         * 
         */
        get() {
            val chipAuthenticationPublicKeyInfos: MutableList<ChipAuthenticationPublicKeyInfo?> =
                ArrayList<ChipAuthenticationPublicKeyInfo?>(securityInfos!!.size)
            for (securityInfo in securityInfos!!) {
                if (securityInfo is ChipAuthenticationPublicKeyInfo) {
                    chipAuthenticationPublicKeyInfos.add(securityInfo)
                }
            }
            return chipAuthenticationPublicKeyInfos
        }

    /**
     * Returns the signature algorithm object identifier.
     * 
     * @return signature algorithm OID
     */
    override fun toString(): String {
        return "CardSecurityFile [$securityInfos]"
    }

    /**
     * Tests equality with respect to another object.
     * 
     * @param otherObj another object
     * 
     * @return whether this object equals the other object
     */
    override fun equals(otherObj: Any?): Boolean {
        if (otherObj == null) {
            return false
        }
        if (!(otherObj.javaClass == this.javaClass)) {
            return false
        }
        val other = otherObj as CardSecurityFile
        if (securityInfos == null) {
            return other.securityInfos == null
        }
        if (other.securityInfos == null) {
            return securityInfos == null
        }
        return securityInfos == other.securityInfos
    }

    /**
     * Returns a hash code of this object.
     * 
     * @return the hash code
     */
    override fun hashCode(): Int {
        return 3 * securityInfos.hashCode() + 63
    }

    companion object {
        private const val serialVersionUID = -3535507558193769952L

        private val LOGGER: Logger = Logger.getLogger("kmrtd")

        private const val CONTENT_TYPE_OID = "0.4.0.127.0.7.3.2.1" // FIXME

        /* FIXME: rewrite (using writeObject instead of getDERObject) to remove interface dependency on BC. */
        /**
         * Computes content info from the given list of security infos.
         * 
         * @param contentTypeOID the object identifier to use
         * @param securityInfos the list of security infos
         * 
         * @return the content info
         */
        private fun toContentInfo(contentTypeOID: String, securityInfos: MutableCollection<SecurityInfo>): ContentInfo {
            try {
                val vector = ASN1EncodableVector()
                for (securityInfo in securityInfos) {
                    vector.add(securityInfo.dERObject)
                }
                val derSet: ASN1Set = DLSet(vector)

                return ContentInfo(ASN1ObjectIdentifier(contentTypeOID), DEROctetString(derSet))
            } catch (ioe: IOException) {
                LOGGER.log(Level.WARNING, "Error creating signedData", ioe)
                throw IllegalArgumentException("Error DER encoding the security infos")
            }
        }

        /**
         * Attempts to interpret the contents of the given signed data structure as a collection of security infos.
         * If the data does not contain any security infos, the empty set is returned.
         * 
         * @param signedData the signed data structure to parse
         * 
         * @return the set of security infos inside the signed data structure
         * 
         * @throws IOException on parse error
         */
        @Throws(IOException::class)
        private fun getSecurityInfos(signedData: SignedData): MutableSet<SecurityInfo> {
            val encapsulatedContent = getContent(signedData)

            if (encapsulatedContent !is ASN1Set) {
                throw IOException("Was expecting an ASN1Set, found " + encapsulatedContent!!.javaClass)
            }

            val securityInfos: MutableSet<SecurityInfo> = HashSet<SecurityInfo>()
            for (i in 0..<encapsulatedContent.size()) {
                val `object` = encapsulatedContent.getObjectAt(i)
                try {
                    val securityInfo = getInstance(`object`)
                    if (securityInfo == null) {
                        LOGGER.log(Level.WARNING, "Could not parse, skipping security info")
                        continue
                    }
                    securityInfos.add(securityInfo)
                } catch (e: Exception) {
                    LOGGER.log(Level.WARNING, "Exception while parsing, skipping security info", e)
                }
            }

            return securityInfos
        }
    }
}
