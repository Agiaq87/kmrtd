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
 * $Id: DG14File.java 1885 2024-11-07 09:17:29Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds.icao

import kmrtd.lds.*
import kmrtd.lds.SecurityInfo.Companion.getInstance
import org.bouncycastle.asn1.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Data Group 14 stores a set of SecurityInfos for EAC and PACE, see
 * BSI EAC 1.11 and ICAO TR-SAC-1.01.
 * To us the interesting bits are: the map of public keys (EC or DH),
 * the map of protocol identifiers which should match the key's map (not
 * checked here!), and the file identifier of the efCVCA file.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1885 $
 */
class DG14File : DataGroup {
    /** The security infos that make up this file.  */
    private var securityInfos: MutableSet<SecurityInfo?>? = null

    /**
     * Constructs a new DG14 file from the provided data.
     * 
     * @param securityInfos a list of security infos
     */
    constructor(securityInfos: MutableCollection<SecurityInfo?>) : super(LDSFile.EF_DG14_TAG) {
        requireNotNull(securityInfos) { "Null securityInfos" }
        this.securityInfos = HashSet<SecurityInfo?>(securityInfos)
    }

    /**
     * Constructs a new DG14 file from the data in an input stream.
     * 
     * @param inputStream the input stream to parse the data from
     * 
     * @throws IOException on error reading from input stream
     */
    constructor(inputStream: InputStream?) : super(LDSFile.EF_DG14_TAG, inputStream)

    @Throws(IOException::class)
    override fun readContent(inputStream: InputStream) {
        val asn1In = ASN1InputStream(inputStream, true)
        val asn1Primitive = asn1In.readObject()
        val set = ASN1Set.getInstance(asn1Primitive)
        securityInfos = HashSet<SecurityInfo?>(set.size())
        for (i in 0..<set.size()) {
            val `object` = set.getObjectAt(i).toASN1Primitive()
            try {
                val securityInfo = getInstance(`object`)
                if (securityInfo == null) {
                    LOGGER.warning("Skipping this unsupported SecurityInfo")
                    continue
                }
                securityInfos!!.add(securityInfo)
            } catch (e: Exception) {
                LOGGER.log(Level.WARNING, "Skipping Security Info", e)
            }
        }
    }

    /* FIXME: rewrite (using writeObject instead of getDERObject) to remove interface dependency on BC. */
    @Throws(IOException::class)
    override fun writeContent(outputStream: OutputStream) {
        val vector = ASN1EncodableVector()
        for (securityInfo in securityInfos!!) {
            if (securityInfo == null) {
                continue
            }

            val derObject: ASN1Primitive? = securityInfo.dERObject
            vector.add(derObject)
        }
        val derSet: ASN1Set = DLSet(vector)
        outputStream.write(derSet.getEncoded(ASN1Encoding.DER))
    }

    @get:Deprecated("Clients should use {@link #getSecurityInfos()} and filter that collection")
    val terminalAuthenticationInfos: MutableList<TerminalAuthenticationInfo?>
        /**
         * Returns the  Terminal Authentication infos.
         * 
         * @return the Terminal Authentication infos.
         * 
         */
        get() {
            val terminalAuthenticationInfos: MutableList<TerminalAuthenticationInfo?> =
                ArrayList<TerminalAuthenticationInfo?>()
            for (securityInfo in securityInfos!!) {
                if (securityInfo is TerminalAuthenticationInfo) {
                    terminalAuthenticationInfos.add(securityInfo)
                }
            }
            return terminalAuthenticationInfos
        }

    @get:Deprecated("Clients should use {@link #getSecurityInfos()} and filter that collection")
    val chipAuthenticationInfos: MutableList<ChipAuthenticationInfo?>
        /**
         * Returns the Chip Authentication infos.
         * 
         * @return the Chip Authentication infos
         * 
         */
        get() {
            val map: MutableList<ChipAuthenticationInfo?> =
                ArrayList<ChipAuthenticationInfo?>()
            for (securityInfo in securityInfos!!) {
                if (securityInfo is ChipAuthenticationInfo) {
                    val chipAuthNInfo =
                        securityInfo
                    map.add(chipAuthNInfo)
                    if (chipAuthNInfo.keyId == null) {
                        return map
                    }
                }
            }
            return map
        }

    @get:Deprecated("Clients should use {@link #getSecurityInfos()} and filter that collection")
    val chipAuthenticationPublicKeyInfos: MutableList<ChipAuthenticationPublicKeyInfo?>
        /**
         * Returns the mapping of key identifiers to public keys.
         * The key identifier may be -1 if there is only one key.
         * 
         * @return the mapping of key identifiers to public keys
         * 
         */
        get() {
            val publicKeys: MutableList<ChipAuthenticationPublicKeyInfo?> =
                ArrayList<ChipAuthenticationPublicKeyInfo?>()
            for (securityInfo in securityInfos!!) {
                if (securityInfo is ChipAuthenticationPublicKeyInfo) {
                    publicKeys.add(securityInfo)
                }
            }
            return publicKeys
        }

    @get:Deprecated("Clients should use {@link #getSecurityInfos()} and filter that collection")
    val activeAuthenticationInfos: MutableList<ActiveAuthenticationInfo?>
        /**
         * Returns the Active Authentication security infos.
         * 
         * @return the Active Authentication security infos
         * 
         */
        get() {
            val resultList: MutableList<ActiveAuthenticationInfo?> =
                ArrayList<ActiveAuthenticationInfo?>()
            for (securityInfo in securityInfos!!) {
                if (securityInfo is ActiveAuthenticationInfo) {
                    val activeAuthenticationInfo =
                        securityInfo
                    resultList.add(activeAuthenticationInfo)
                }
            }
            return resultList
        }

    /**
     * Returns the security infos as an unordered collection.
     * 
     * @return security infos
     */
    fun getSecurityInfos(): MutableCollection<SecurityInfo?>? {
        return securityInfos
    }

    public override fun toString(): String {
        return "DG14File [$securityInfos]"
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (!(obj.javaClass == this.javaClass)) {
            return false
        }

        val other = obj as DG14File
        if (securityInfos == null) {
            return other.securityInfos == null
        }
        if (other.securityInfos == null) {
            return securityInfos == null
        }

        return securityInfos == other.securityInfos
    }

    override fun hashCode(): Int {
        return 5 * securityInfos.hashCode() + 41
    }

    companion object {
        private val serialVersionUID = -3536507558193769953L

        private val LOGGER: Logger = Logger.getLogger("kmrtd")
    }
}
