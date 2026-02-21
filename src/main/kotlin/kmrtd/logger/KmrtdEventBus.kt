package kmrtd.logger

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object KmrtdEventBus {
    private val _events = MutableSharedFlow<KmrtdEvent>(
        replay = 0,
        extraBufferCapacity = 12,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val events: SharedFlow<KmrtdEvent> = _events.asSharedFlow()

    internal fun emit(event: KmrtdEvent) =
        _events.tryEmit(event)
}