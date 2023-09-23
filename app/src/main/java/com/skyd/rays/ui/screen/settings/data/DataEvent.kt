package com.skyd.rays.ui.screen.settings.data

import com.skyd.rays.base.IUiEvent

class DataEvent(
    val deleteAllResultUiEvent: DeleteAllResultUiEvent? = null,
) : IUiEvent

sealed class DeleteAllResultUiEvent {
    class Success(val time: Long) : DeleteAllResultUiEvent()
}