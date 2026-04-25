package com.mattukouta.foreground_service.event

import com.mattukouta.foreground_service.vo.TakeEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object TakeEventFlow {
    private val _event = MutableSharedFlow<TakeEvent>()
    val event = _event.asSharedFlow()

    suspend fun emitEvent(value: TakeEvent) {
        _event.emit(value)
    }
}