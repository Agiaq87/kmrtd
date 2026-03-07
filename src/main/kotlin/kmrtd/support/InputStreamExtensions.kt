package kmrtd.support

import net.sf.scuba.tlv.TLVInputStream
import java.io.InputStream

fun InputStream.asTLV(): TLVInputStream =
    this as? TLVInputStream ?: TLVInputStream(this)