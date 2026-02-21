package kmrtd
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
import kmrtd.protocol.PACEProtocol
import kotlinx.serialization.Serializable
import net.sf.scuba.util.Hex
import java.security.GeneralSecurityException

@Serializable
/**
 * Constructs a key.
 *
 * @param key CAN, MRZ, PIN, PUK password bytes
 * @param keyReference indicates the type of key, valid values are
 * `MRZ_PACE_KEY_REFERENCE`, `CAN_PACE_KEY_REFERENCE`,
 * `PIN_PACE_KEY_REFERENCE`, `PUK_PACE_KEY_REFERENCE`
 */
data class PACEKeySpec(
    override val key: ByteArray,
    val keyReference: Byte,
) : AccessKeySpec {
    /**
     * Constructs a PACE key from a string value.
     *
     * @param key the string value containing CAN, PIN or PUK
     * @param keyReference indicates the type of key, valid values are
     * `MRZ_PACE_KEY_REFERENCE`, `CAN_PACE_KEY_REFERENCE`,
     * `PIN_PACE_KEY_REFERENCE`, `PUK_PACE_KEY_REFERENCE`
     */
    constructor(
        key: String,
        keyReference: Byte
    ) : this(key.toByteArray(), keyReference)

    override val algorithm: String = "PACE"

    override fun toString(): String =
        buildString {
            append("PACEKeySpec [")
            append("key: ${Hex.bytesToHexString(key)}, ")
            append("keyReference: ${keyReferenceToString(keyReference)}")
            append("]")
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PACEKeySpec

        if (keyReference != other.keyReference) return false
        if (!key.contentEquals(other.key)) return false
        if (algorithm != other.algorithm) return false

        return true
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + key.contentHashCode()
        result = prime * result + keyReference
        return result
    }

    /**
     * Returns a textual representation of the given key reference parameter.
     *
     * @param keyReference a key reference parameter
     *
     * @return a textual representation of the key reference
     */
    private fun keyReferenceToString(keyReference: Byte): String =
        when (keyReference) {
            PassportService.MRZ_PACE_KEY_REFERENCE -> "MRZ"
            PassportService.CAN_PACE_KEY_REFERENCE -> "CAN"
            PassportService.PIN_PACE_KEY_REFERENCE -> "PIN"
            PassportService.PUK_PACE_KEY_REFERENCE -> "PUK"
            PassportService.NO_PACE_KEY_REFERENCE -> "NO"
            else -> keyReference.toInt().toString()
    }

    companion object {
        private const val serialVersionUID = -7113246293247012560L

        //  Factory methods
        /**
         * Creates a PACE key from relevant details from a Machine Readable Zone.
         *
         * @param mrz the details from the Machine Readable Zone
         *
         * @return the PACE key
         *
         * @throws GeneralSecurityException on error
         */
        @Throws(GeneralSecurityException::class)
        fun createMRZKey(mrz: BACKeySpec): PACEKeySpec =
            PACEKeySpec(
                PACEProtocol.computeKeySeedForPACE(mrz),
                PassportService.MRZ_PACE_KEY_REFERENCE
            )

        /**
         * Creates a PACE key from a Card Access Number.
         *
         * @param can the Card Access Number
         *
         * @return the PACE key
         */
        @Throws(GeneralSecurityException::class)
        fun createCANKey(can: String): PACEKeySpec =
            PACEKeySpec(
                can.toByteArray(),
                PassportService.CAN_PACE_KEY_REFERENCE
            )

        /**
         * Creates a PACE key from a PIN.
         *
         * @param pin the PIN
         *
         * @return the PACE key
         */
        @Throws(GeneralSecurityException::class)
        fun createPINKey(pin: String): PACEKeySpec =
            PACEKeySpec(
                pin.toByteArray(),
                PassportService.PIN_PACE_KEY_REFERENCE
            )

        /**
         * Creates a PACE key from a PUK.
         *
         * @param puk the PUK
         *
         * @return the PACE key
         */
        @Throws(GeneralSecurityException::class)
        fun createPUKKey(puk: String): PACEKeySpec =
            PACEKeySpec(
                puk.toByteArray(),
                PassportService.PUK_PACE_KEY_REFERENCE
            )

    }
}