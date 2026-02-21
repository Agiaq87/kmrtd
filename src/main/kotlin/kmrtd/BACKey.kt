package kmrtd

import kmrtd.support.DocumentNumber
import kmrtd.support.ICAODate
import kotlinx.serialization.Serializable
import java.security.GeneralSecurityException
import java.util.*

/**
 * BACKey data class
 */
@Serializable
data class BACKey(
    override val documentNumber: DocumentNumber,
    override val dateOfBirth: ICAODate,
    override val dateOfExpiry: ICAODate
) : BACKeySpec {

    override val algorithm: String = "BAC"

    override val key: ByteArray
        get() = try {
            Util.computeKeySeed(
                documentNumber.value,
                dateOfBirth.date,
                dateOfExpiry.date,
                "SHA-1",
                true
            )
        } catch (gse: GeneralSecurityException) {
            throw IllegalArgumentException("Unexpected exception", gse)
        }

    companion object {
        private const val serialVersionUID: Long = -1059774581180524710L

        /**
         * Factory methods
         */
        fun from(documentNumber: String, dateOfBirth: Date, dateOfExpiry: Date): BACKey =
            BACKey(
                DocumentNumber(documentNumber),
                ICAODate.from(dateOfBirth),
                ICAODate.from(dateOfExpiry)
            )

        fun from(documentNumber: String, dateOfBirthInMillis: Long, dateOfExpiryInMillis: Long): BACKey =
            BACKey(
                DocumentNumber(documentNumber),
                ICAODate.from(dateOfBirthInMillis),
                ICAODate.from(dateOfExpiryInMillis)
            )
    }
}
