package com.mattukouta.foreground_service.vo

sealed class TakeEvent {
    data object Failed: TakeEvent()
    data object Success: TakeEvent()
}
