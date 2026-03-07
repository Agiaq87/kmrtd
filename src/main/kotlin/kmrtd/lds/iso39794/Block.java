/*
 * JMRTD - A Java API for accessing machine readable travel documents.
 *
 * Copyright (C) 2006 - 2025  The JMRTD team
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
 * $Id: Block.java 1892 2025-03-18 15:15:52Z martijno $
 */

package kmrtd.lds.iso39794;

import org.bouncycastle.asn1.ASN1Encodable;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

abstract class Block implements Serializable {

    protected static final Logger LOGGER = Logger.getLogger("org.jmrtd.lds.iso39794");
    private static final long serialVersionUID = -8585852930916738115L;

    abstract ASN1Encodable getASN1Object();

    public byte[] getEncoded() {
        try {
            return getASN1Object().toASN1Primitive().getEncoded("DER");
        } catch (IOException ioe) {
            LOGGER.log(Level.WARNING, "Error decoding", ioe);
            return null;
        }
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object other);
}
