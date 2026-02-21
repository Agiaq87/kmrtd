package kmrtd
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */

import javax.crypto.spec.SecretKeySpec

class PACESecretKeySpec(
    override val key: ByteArray,
    offset: Int,
    len: Int,
    override val algorithm: String,
    val keyReference: Byte
) : SecretKeySpec(key, offset, len, algorithm), AccessKeySpec {

    constructor(
        key: ByteArray,
        algorithm: String,
        keyReference: Byte
    ) : this(key, 0, key.size, algorithm, keyReference)

    override fun hashCode(): Int {
        val prime = 31
        var result = super.hashCode()
        result = prime * result + keyReference
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (!super.equals(other)) {
            return false
        }
        if (javaClass != other?.javaClass) {
            return false
        }

        val other = other as PACESecretKeySpec
        return keyReference == other.keyReference
    }

    companion object {
        private val serialVersionUID = -5181060361947453857L
    }
}