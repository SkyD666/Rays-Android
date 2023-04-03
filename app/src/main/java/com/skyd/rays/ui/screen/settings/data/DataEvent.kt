package com.skyd.rays.ui.screen.settings.data

import com.skyd.rays.base.IUiEvent

data class DataEvent(
    val deleteAllResultUiEvent: DeleteAllResultUiEvent? = null,
) : IUiEvent

sealed class DeleteAllResultUiEvent {
    data class Success(val time: Long) : DeleteAllResultUiEvent()
}