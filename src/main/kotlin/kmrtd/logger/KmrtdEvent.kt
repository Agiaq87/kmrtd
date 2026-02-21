package kmrtd.logger

/**
 * Represents an observable event emitted during MRTD operations.
 * Consumers can collect these from [KmrtdEventBus.events] to
 * track progress, log diagnostics, or update UI.
 */
sealed class KmrtdEvent(
    val timestamp: Long = System.currentTimeMillis()
) {

    /** Which protocol/phase emitted this event. */
    sealed class Phase {
        data object PACE : Phase()
        data object BAC : Phase()
        data object ActiveAuth : Phase()
        data object ChipAuth : Phase()
        data object TerminalAuth : Phase()
        data object Reading : Phase()
    }

    /** A step within a protocol started. */
    data class StepStarted(
        val phase: Phase,
        val description: String
    ) : KmrtdEvent()

    /** A step completed successfully. */
    data class StepCompleted(
        val phase: Phase,
        val description: String,
        val durationMs: Long
    ) : KmrtdEvent()

    /** Something non-fatal worth noting. */
    data class Warning(
        val phase: Phase,
        val message: String,
        val cause: Throwable? = null
    ) : KmrtdEvent()

    /** A protocol failed. */
    data class Error(
        val phase: Phase,
        val message: String,
        val cause: Throwable
    ) : KmrtdEvent()

    /** APDU-level detail for debugging. */
    data class ApduExchanged(
        val command: ByteArray,
        val response: ByteArray,
        val durationMs: Long
    ) : KmrtdEvent()
}