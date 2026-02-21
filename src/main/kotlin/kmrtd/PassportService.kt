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
 * $Id: PassportService.java 1850 2021-05-21 06:25:03Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd

import net.sf.scuba.smartcards.*
import org.jmrtd.AccessKeySpec
import org.jmrtd.BACKeySpec
import org.jmrtd.cert.CVCPrincipal
import org.jmrtd.cert.CardVerifiableCertificate
import org.jmrtd.protocol.*
import java.math.BigInteger
import java.security.GeneralSecurityException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.AlgorithmParameterSpec
import java.util.logging.Logger
import javax.crypto.SecretKey

/**
 * Card service for reading files (such as data groups) and using the various
 * access control protocols (BAC, PACE, EAC-TA), clone-detection verification
 * protocols (AA, EAC-CA), and the resulting secure messaging as implemented
 * by the MRTD ICC.
 * 
 * Based on ICAO Doc 9303 2015.
 * Originally based on ICAO-TR-PKI and ICAO-TR-LDS.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision:352 $
 */
class PassportService(
    private val service: CardService, private val maxTranceiveLengthForPACEProtocol: Int,
    /**
     * Returns the maximum tranceive length of (protected) APDUs.
     * 
     * @return the maximum APDU tranceive length
     */
    val maxTranceiveLength: Int,
    /**
     * The file read block size, some passports cannot handle large values.
     */
    private val maxBlockSize: Int, isSFIEnabled: Boolean, private val shouldCheckMAC: Boolean
) : AbstractMRTDCardService() {
    private var isOpen = false

    private var wrapper: SecureMessagingWrapper? = null

    private var isAppletSelected = false

    private val rootFileSystem: DefaultFileSystem

    private val appletFileSystem: DefaultFileSystem?

    private val bacSender: BACAPDUSender?
    private val paceSender: PACEAPDUSender?
    private val aaSender: AAAPDUSender?
    private val eacCASender: EACCAAPDUSender?
    private val eacTASender: EACTAAPDUSender?
    private val readBinarySender: ReadBinaryAPDUSender

    /**
     * Creates a new passport service for accessing the passport.
     * 
     * @param service another service which will deal with sending the APDUs to the card
     * @param maxTranceiveLengthForSecureMessaging maximum length to use in secure messaging APDUs, `256` or `65536`
     * @param maxBlockSize maximum buffer size for plain text APDUs
     * @param isSFIEnabled whether short file identifiers should be used for read binaries when possible
     * @param shouldCheckMAC whether the secure messaging channels, resulting from BAC, PACE, EAC-CA, should
     * check MACs on response APDUs
     */
    constructor(
        service: CardService,
        maxTranceiveLengthForSecureMessaging: Int,
        maxBlockSize: Int,
        isSFIEnabled: Boolean,
        shouldCheckMAC: Boolean
    ) : this(
        service,
        NORMAL_MAX_TRANCEIVE_LENGTH,
        maxTranceiveLengthForSecureMessaging,
        maxBlockSize,
        isSFIEnabled,
        shouldCheckMAC
    )

    /**
     * Creates a new passport service for accessing the passport.
     * 
     * @param service another service which will deal with sending the APDUs to the card
     * @param maxTranceiveLengthForPACEProtocol maximum length  to use in PACE protocol steps, `256` or `65536`
     * @param maxTranceiveLength maximum length to use in secure messaging APDUs, `256` or `65536`
     * @param maxBlockSize maximum buffer size for plain text APDUs
     * @param isSFIEnabled whether short file identifiers should be used for read binaries when possible
     * @param shouldCheckMAC whether the secure messaging channels, resulting from BAC, PACE, EAC-CA, should
     * check MACs on response APDUs
     */
    init {
        this.bacSender = BACAPDUSender(service)
        this.paceSender = PACEAPDUSender(service)
        this.aaSender = AAAPDUSender(service)
        this.eacCASender = EACCAAPDUSender(service)
        this.eacTASender = EACTAAPDUSender(service)
        this.readBinarySender = ReadBinaryAPDUSender(service)

        this.rootFileSystem = DefaultFileSystem(
            readBinarySender,
            false
        ) // Some passports (UK?) don't support SFI for EF.CardAccess. -- MO
        this.appletFileSystem = DefaultFileSystem(readBinarySender, isSFIEnabled)
    }

    /**
     * Opens a session to the card. As of 0.4.10 this no longer auto selects the passport application,
     * caller is responsible to call #sendSelectApplet(boolean) now.
     * 
     * @throws CardServiceException on error
     */
    @Throws(CardServiceException::class)
    override fun open() {
        if (isOpen()) {
            return
        }
        synchronized(this) {
            service.open()
            isOpen = true
        }
    }

    /**
     * Selects the card side applet. If PACE has been executed successfully previously, then the ICC has authenticated
     * us and a secure messaging channel has already been established. If not, then the caller should request BAC execution as a next
     * step.
     * 
     * @param hasPACESucceeded indicates whether PACE has been executed successfully (in which case a secure messaging channel has been established)
     * 
     * @throws CardServiceException on error
     */
    @Throws(CardServiceException::class)
    override fun sendSelectApplet(hasPACESucceeded: Boolean) {
        if (isAppletSelected) {
            LOGGER.info("Re-selecting ICAO applet")
        }

        if (hasPACESucceeded) {
            /* Use SM as set up by doPACE() */
            readBinarySender.sendSelectApplet(wrapper, APPLET_AID)
        } else {
            /* Use plain messaging to select the applet, caller will have to do doBAC. */
            readBinarySender.sendSelectApplet(null, APPLET_AID)
        }

        isAppletSelected = true
    }

    /**
     * Sends a `SELECT MF` command to the card.
     * 
     * @throws CardServiceException on tranceive error
     */
    @Throws(CardServiceException::class)
    override fun sendSelectMF() {
        readBinarySender.sendSelectMF()
        wrapper = null
    }

    /**
     * Returns a boolean that indicates whether this service is open.
     * 
     * @return a boolean that indicates whether this service is open
     */
    override fun isOpen(): Boolean {
        return isOpen
    }

    /**
     * Performs the *Basic Access Control* protocol.
     * 
     * @param bacKey the key based on the document number,
     * the card holder's birth date,
     * and the document's expiration date
     * 
     * @return the BAC result
     * 
     * @throws CardServiceException if authentication failed
     */
    @Synchronized
    @Throws(CardServiceException::class)
    override fun doBAC(bacKey: AccessKeySpec?): BACResult {
        require(bacKey is BACKeySpec) { "Unsupported key type" }
        val bacResult = (BACProtocol(
            bacSender,
            this.maxTranceiveLength, shouldCheckMAC
        )).doBAC(bacKey)
        wrapper = bacResult.getWrapper()
        appletFileSystem!!.setWrapper(wrapper)
        return bacResult
    }

    /**
     * Performs the *Basic Access Control* protocol.
     * It does BAC using kEnc and kMac keys, usually calculated
     * from the document number, the card holder's date of birth,
     * and the card's date of expiry.
     * 
     * A secure messaging channel is set up as a result.
     * 
     * @param kEnc static 3DES key required for BAC
     * @param kMac static 3DES key required for BAC
     * 
     * @return the result
     * 
     * @throws CardServiceException if authentication failed
     * @throws GeneralSecurityException on security primitives related problems
     */
    @Synchronized
    @Throws(CardServiceException::class, GeneralSecurityException::class)
    override fun doBAC(kEnc: SecretKey?, kMac: SecretKey?): BACResult {
        val bacResult = (BACProtocol(
            bacSender,
            this.maxTranceiveLength, shouldCheckMAC
        )).doBAC(kEnc, kMac)
        wrapper = bacResult.getWrapper()
        appletFileSystem!!.setWrapper(wrapper)
        return bacResult
    }

    /**
     * Performs the PACE 2.0 / SAC protocol.
     * A secure messaging channel is set up as a result.
     * 
     * @param keySpec the MRZ
     * @param oid as specified in the PACEInfo, indicates GM or IM or CAM, DH or ECDH, cipher, digest, length
     * @param params explicit static domain parameters the domain params for DH or ECDH
     * @param parameterId parameter identifier or `null`
     * 
     * @return the result
     * 
     * @throws CardServiceException on error
     */
    @Synchronized
    @Throws(CardServiceException::class)
    override fun doPACE(
        keySpec: AccessKeySpec?,
        oid: String?,
        params: AlgorithmParameterSpec?,
        parameterId: BigInteger?
    ): PACEResult {
        val paceResult = (PACEProtocol(
            paceSender, wrapper, maxTranceiveLengthForPACEProtocol,
            this.maxTranceiveLength, shouldCheckMAC
        )).doPACE(keySpec, oid, params, parameterId)
        wrapper = paceResult.getWrapper()
        appletFileSystem!!.setWrapper(wrapper)
        return paceResult
    }

    /**
     * Perform CA (Chip Authentication) part of EAC (version 1). For details see TR-03110
     * ver. 1.11. In short, we authenticate the chip with (EC)DH key agreement
     * protocol and create new secure messaging keys.
     * A new secure messaging channel is set up as a result.
     * 
     * @param keyId passport's public key id (stored in DG14), `null` if none
     * @param oid the object identifier indicating the Chip Authentication protocol
     * @param publicKeyOID the object identifier indicating the public key algorithm used
     * @param publicKey passport's public key (stored in DG14)
     * 
     * @return the Chip Authentication result
     * 
     * @throws CardServiceException if CA failed or some error occurred
     */
    @Synchronized
    @Throws(CardServiceException::class)
    override fun doEACCA(keyId: BigInteger?, oid: String?, publicKeyOID: String?, publicKey: PublicKey?): EACCAResult {
        val caResult = (EACCAProtocol(
            eacCASender, getWrapper(),
            this.maxTranceiveLength, shouldCheckMAC
        )).doCA(keyId, oid, publicKeyOID, publicKey)
        wrapper = caResult.getWrapper()
        appletFileSystem!!.setWrapper(wrapper)
        return caResult
    }

    /* From BSI-03110 v1.1, B.2:
   *
   * <pre>
   * The following sequence of commands SHALL be used to implement Terminal Authentication:
   *   1. MSE:Set DST
   *   2. PSO:Verify Certificate
   *   3. MSE:Set AT
   *   4. Get Challenge
   *   5. External Authenticate
   * Steps 1 and 2 are repeated for every CV certificate to be verified
   * (CVCA Link Certificates, DV Certificate, IS Certificate).
   * </pre>
   */
    /**
     * Performs *Terminal Authentication* (TA) part of EAC (version 1). For details see
     * TR-03110 ver. 1.11.
     * 
     * In short, we feed the sequence of terminal certificates to the card for verification,
     * get a challenge from the card, sign it with the terminal private key, and send the result
     * back to the card for verification.
     * 
     * @param caReference reference issuer
     * @param terminalCertificates terminal certificate chain
     * @param terminalKey terminal private key
     * @param taAlg algorithm
     * @param chipAuthenticationResult the chip authentication result
     * @param documentNumber the document number
     * 
     * @return the Terminal Authentication result
     * 
     * @throws CardServiceException on error
     */
    @Synchronized
    @Throws(CardServiceException::class)
    override fun doEACTA(
        caReference: CVCPrincipal?, terminalCertificates: MutableList<CardVerifiableCertificate?>?,
        terminalKey: PrivateKey?, taAlg: String?, chipAuthenticationResult: EACCAResult?, documentNumber: String?
    ): EACTAResult? {
        return (EACTAProtocol(eacTASender, getWrapper())).doEACTA(
            caReference,
            terminalCertificates,
            terminalKey,
            taAlg,
            chipAuthenticationResult,
            documentNumber
        )
    }

    /**
     * Performs *Terminal Authentication* (TA) part of EAC (version 1). For details see
     * TR-03110 ver. 1.11.
     * 
     * In short, we feed the sequence of terminal certificates to the card for verification,
     * get a challenge from the card, sign it with the terminal private key, and send the result
     * back to the card for verification.
     * 
     * @param caReference reference issuer
     * @param terminalCertificates terminal certificate chain
     * @param terminalKey terminal private key
     * @param taAlg algorithm
     * @param chipAuthenticationResult the chip authentication result
     * @param paceResult the PACE result
     * 
     * @return the Terminal Authentication result
     * 
     * @throws CardServiceException on error
     */
    @Synchronized
    @Throws(CardServiceException::class)
    override fun doEACTA(
        caReference: CVCPrincipal?, terminalCertificates: MutableList<CardVerifiableCertificate?>?,
        terminalKey: PrivateKey?, taAlg: String?, chipAuthenticationResult: EACCAResult?, paceResult: PACEResult
    ): EACTAResult? {
        return (EACTAProtocol(eacTASender, getWrapper())).doTA(
            caReference,
            terminalCertificates,
            terminalKey,
            taAlg,
            chipAuthenticationResult,
            paceResult
        )
    }

    /**
     * Performs the *Active Authentication* protocol.
     * 
     * @param publicKey the public key to use (usually read from the card)
     * @param digestAlgorithm the digest algorithm to use, or null
     * @param signatureAlgorithm signature algorithm
     * @param challenge challenge
     * 
     * @return a boolean indicating whether the card was authenticated
     * 
     * @throws CardServiceException on error
     */
    @Throws(CardServiceException::class)
    override fun doAA(
        publicKey: PublicKey?,
        digestAlgorithm: String?,
        signatureAlgorithm: String?,
        challenge: ByteArray?
    ): AAResult? {
        return (AAProtocol(aaSender, getWrapper())).doAA(publicKey, digestAlgorithm, signatureAlgorithm, challenge)
    }

    /**
     * Closes this service.
     */
    override fun close() {
        try {
            service.close()
            wrapper = null
        } finally {
            isOpen = false
        }
    }

    /**
     * Returns the secure messaging wrapper currently in use.
     * Returns `null` until access control has been performed.
     * 
     * @return the wrapper
     */
    override fun getWrapper(): SecureMessagingWrapper? {
        val ldsSecureMessagingWrapper = appletFileSystem!!.getWrapper() as SecureMessagingWrapper?
        if (ldsSecureMessagingWrapper != null && ldsSecureMessagingWrapper.getSendSequenceCounter() > wrapper!!.getSendSequenceCounter()) {
            wrapper = ldsSecureMessagingWrapper
        }
        return wrapper
    }

    @Throws(CardServiceException::class)
    override fun transmit(commandAPDU: CommandAPDU?): ResponseAPDU? {
        return service.transmit(commandAPDU)
    }

    /**
     * Returns the answer to reset.
     * 
     * @return the answer to reset
     * 
     * @throws CardServiceException on error
     */
    @Throws(CardServiceException::class)
    override fun getATR(): ByteArray? {
        return service.getATR()
    }

    /**
     * Determines whether an exception indicates a tag is lost event.
     * 
     * @param e an exception
     * 
     * @return whether the exception indicates a tag is lost event
     */
    override fun isConnectionLost(e: Exception?): Boolean {
        return service.isConnectionLost(e)
    }

    /**
     * Whether secure channels should check the MAC on response APDUs sent by the ICC.
     * 
     * @return a boolean indicating whether the MAC should be checked
     */
    fun shouldCheckMAC(): Boolean {
        return shouldCheckMAC
    }

    /**
     * Returns the file indicated by the file identifier as an input stream.
     * The resulting input stream will send APDUs to the card as it is being read.
     * 
     * @param fid the file identifier
     * 
     * @return the file as an input stream
     * 
     * @throws CardServiceException if the file cannot be read
     * 
     */
    @Deprecated("Use the other method with explicit max block size")
    @Synchronized
    @Throws(CardServiceException::class)
    override fun getInputStream(fid: Short): CardFileInputStream {
        return getInputStream(fid, maxBlockSize)
    }

    /**
     * Returns the file indicated by the file identifier as an input stream.
     * The resulting input stream will send APDUs to the card as it is being read.
     * 
     * @param fid the file identifier
     * @param maxBlockSize the blocksize to request in plain READ BINARY commands
     * 
     * @return the file as an input stream
     * 
     * @throws CardServiceException if the file cannot be read
     */
    @Synchronized
    @Throws(CardServiceException::class)
    override fun getInputStream(fid: Short, maxBlockSize: Int): CardFileInputStream {
        if (!isAppletSelected) {
            synchronized(rootFileSystem) {
                rootFileSystem.selectFile(fid)
                return CardFileInputStream(maxBlockSize, rootFileSystem)
            }
        } else {
            synchronized(appletFileSystem!!) {
                appletFileSystem.selectFile(fid)
                return CardFileInputStream(maxBlockSize, appletFileSystem)
            }
        }
    }

    /**
     * Returns the currently set maximum length to be requested in READ BINARY commands.
     * If the applet file system has not been selected, this will return
     * [.NORMAL_MAX_TRANCEIVE_LENGTH].
     * 
     * @return the currently set maximum length to be requested in READ BINARY commands
     */
    override fun getMaxReadBinaryLength(): Int {
        if (appletFileSystem == null) {
            return NORMAL_MAX_TRANCEIVE_LENGTH
        }

        return appletFileSystem.maxReadBinaryLength
    }

    override fun addAPDUListener(l: APDUListener?) {
        service.addAPDUListener(l)
    }

    override fun removeAPDUListener(l: APDUListener?) {
        service.removeAPDUListener(l)
    }

    override fun getAPDUListeners(): MutableCollection<APDUListener>? {
        return service.getAPDUListeners()
    }

    override fun notifyExchangedAPDU(event: APDUEvent?) {
        val apduListeners = getAPDUListeners()
        if (apduListeners == null || apduListeners.isEmpty()) {
            return
        }

        for (apduListener in apduListeners) {
            apduListener.exchangedAPDU(event)
        }
    }

    companion object {
        /** Shared secret type for non-PACE key.  */
        const val NO_PACE_KEY_REFERENCE: Byte = 0x00

        /** Shared secret type for PACE according to BSI TR-03110 v2.03 B.11.1.  */
        const val MRZ_PACE_KEY_REFERENCE: Byte = 0x01

        /** Shared secret type for PACE according to BSI TR-03110 v2.03 B.11.1.  */
        const val CAN_PACE_KEY_REFERENCE: Byte = 0x02

        /** Shared secret type for PACE according to BSI TR-03110 v2.03 B.11.1.  */
        const val PIN_PACE_KEY_REFERENCE: Byte = 0x03

        /** Shared secret type for PACE according to BSI TR-03110 v2.03 B.11.1.  */
        const val PUK_PACE_KEY_REFERENCE: Byte = 0x04

        private val LOGGER: Logger = Logger.getLogger("org.jmrtd")

        /** Card Access.  */
        const val EF_CARD_ACCESS: Short = 0x011C

        /** Card Security.  */
        const val EF_CARD_SECURITY: Short = 0x011D

        /** File identifier for data group 1. Data group 1 contains the MRZ.  */
        const val EF_DG1: Short = 0x0101

        /** File identifier for data group 2. Data group 2 contains face image data.  */
        const val EF_DG2: Short = 0x0102

        /** File identifier for data group 3. Data group 3 contains finger print data.  */
        const val EF_DG3: Short = 0x0103

        /** File identifier for data group 4. Data group 4 contains iris data.  */
        const val EF_DG4: Short = 0x0104

        /** File identifier for data group 5. Data group 5 contains displayed portrait.  */
        const val EF_DG5: Short = 0x0105

        /** File identifier for data group 6. Data group 6 is RFU.  */
        const val EF_DG6: Short = 0x0106

        /** File identifier for data group 7. Data group 7 contains displayed signature.  */
        const val EF_DG7: Short = 0x0107

        /** File identifier for data group 8. Data group 8 contains data features.  */
        const val EF_DG8: Short = 0x0108

        /** File identifier for data group 9. Data group 9 contains structure features.  */
        const val EF_DG9: Short = 0x0109

        /** File identifier for data group 10. Data group 10 contains substance features.  */
        const val EF_DG10: Short = 0x010A

        /** File identifier for data group 11. Data group 11 contains additional personal details.  */
        const val EF_DG11: Short = 0x010B

        /** File identifier for data group 12. Data group 12 contains additional document details.  */
        const val EF_DG12: Short = 0x010C

        /** File identifier for data group 13. Data group 13 contains optional details.  */
        const val EF_DG13: Short = 0x010D

        /** File identifier for data group 14. Data group 14 contains security infos.  */
        const val EF_DG14: Short = 0x010E

        /** File identifier for data group 15. Data group 15 contains the public key used for Active Authentication.  */
        const val EF_DG15: Short = 0x010F

        /** File identifier for data group 16. Data group 16 contains person(s) to notify.  */
        const val EF_DG16: Short = 0x0110

        /** The security document.  */
        const val EF_SOD: Short = 0x011D

        /** The data group presence list.  */
        const val EF_COM: Short = 0x011E

        /**
         * Contains EAC CVA references. Note: this can be overridden by a file
         * identifier in the DG14 file (in a TerminalAuthenticationInfo). Check DG14
         * first. Also, this file does not have a header tag, like the others.
         */
        const val EF_CVCA: Short = 0x011C

        /** Short file identifier for card access file.  */
        const val SFI_CARD_ACCESS: Byte = 0x1C

        /** Short file identifier for card security file.  */
        const val SFI_CARD_SECURITY: Byte = 0x1D

        /** Short file identifier for file.  */
        const val SFI_DG1: Byte = 0x01

        /** Short file identifier for file.  */
        const val SFI_DG2: Byte = 0x02

        /** Short file identifier for file.  */
        const val SFI_DG3: Byte = 0x03

        /** Short file identifier for file.  */
        const val SFI_DG4: Byte = 0x04

        /** Short file identifier for file.  */
        const val SFI_DG5: Byte = 0x05

        /** Short file identifier for file.  */
        const val SFI_DG6: Byte = 0x06

        /** Short file identifier for file.  */
        const val SFI_DG7: Byte = 0x07

        /** Short file identifier for file.  */
        const val SFI_DG8: Byte = 0x08

        /** Short file identifier for file.  */
        const val SFI_DG9: Byte = 0x09

        /** Short file identifier for file.  */
        const val SFI_DG10: Byte = 0x0A

        /** Short file identifier for file.  */
        const val SFI_DG11: Byte = 0x0B

        /** Short file identifier for file.  */
        const val SFI_DG12: Byte = 0x0C

        /** Short file identifier for file.  */
        const val SFI_DG13: Byte = 0x0D

        /** Short file identifier for file.  */
        const val SFI_DG14: Byte = 0x0E

        /** Short file identifier for file.  */
        const val SFI_DG15: Byte = 0x0F

        /** Short file identifier for file.  */
        const val SFI_DG16: Byte = 0x10

        /** Short file identifier for file.  */
        const val SFI_COM: Byte = 0x1E

        /** Short file identifier for file.  */
        const val SFI_SOD: Byte = 0x1D

        /** Short file identifier for file.  */
        const val SFI_CVCA: Byte = 0x1C

        /** The default maximal blocksize used for unencrypted APDUs.  */
        const val DEFAULT_MAX_BLOCKSIZE: Int = 223

        /** The normal maximal tranceive length of APDUs.  */
        const val NORMAL_MAX_TRANCEIVE_LENGTH: Int = 256

        /** The extended maximal tranceive length of APDUs.  */
        const val EXTENDED_MAX_TRANCEIVE_LENGTH: Int = 65536

        /** The applet we select when we start a session.  */
        protected val APPLET_AID: ByteArray = byteArrayOf(0xA0.toByte(), 0x00, 0x00, 0x02, 0x47, 0x10, 0x01)
    }
}
