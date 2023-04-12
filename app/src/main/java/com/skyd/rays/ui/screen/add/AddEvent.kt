package com.skyd.rays.ui.screen.add

import com.skyd.rays.base.IUiEvent

data class AddEvent(
    val addStickersResultUiEvent: AddStickersResultUiEvent? = null,
) : IUiEvent

sealed class AddStickersResultUiEvent {
    object Duplicate : AddStickersResultUiEvent()
    data class Success(val stickerUuid: String) : AddStickersResultUiEvent()
}