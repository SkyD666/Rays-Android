package com.skyd.rays.ui.screen.detail

import com.skyd.rays.base.IUiEvent

class DetailEvent(
    val detailResultUiEvent: DetailResultUiEvent? = null,
    val deleteResultUiEvent: DeleteResultUiEvent? = null,
) : IUiEvent

sealed class DetailResultUiEvent {
    class Success(val successCount: Int) : DetailResultUiEvent()
}

sealed class DeleteResultUiEvent {
    data object Success : DetailResultUiEvent()
}
