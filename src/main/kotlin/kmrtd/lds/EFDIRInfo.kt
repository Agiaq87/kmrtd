/*
 * JMRTD - A Java API for accessing machine readable travel documents.
 *
 * Copyright (C) 2006 - 2022  The JMRTD team
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
 * $Id: $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.lds

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.DLSequence

/*
 * EFDIRInfo ::= SEQUENCE {
 *   protocol OBJECT IDENTIFIER(id-EFDIR),
 *   eFDIR OCTET STRING
 * }
 *
 * id-EFDIR OBJECT IDENTIFIER ::= {
 *   id-icao-mrtd-security 13
 * }
 */
/**
 * Encapsulates a full copy of the content of the
 * transparent elementary file EF-DIR contained in the Master File.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: $
 */
class EFDIRInfo(efDIR: ByteArray) : SecurityInfo() {
    private val efDIR: ByteArray

    init {
        requireNotNull(efDIR) { "Cannot create EFDIRInfo for null" }
        this.efDIR = efDIR.copyOf(efDIR.size)
    }

    val eFDIR: ByteArray
        /**
         * The contents of the EF-DIR file.
         * 
         * @return the contents of the EF-DIR file
         */
        get() = efDIR.copyOf(efDIR.size)

    val dERObject: ASN1Primitive?
        get() {
            val v = ASN1EncodableVector()
            v.add(ASN1ObjectIdentifier(Companion.objectIdentifier))
            v.add(ASN1OctetString.getInstance(efDIR))
            return DLSequence.getInstance(v)
        }

    val protocolOIDString: String?
        get() = "id-EFDIR"

    companion object {
        private const val serialVersionUID = 6778691696414558842L

        val objectIdentifier: String = "2.23.136.1.1.13"
            get() = Companion.field
    }
}
