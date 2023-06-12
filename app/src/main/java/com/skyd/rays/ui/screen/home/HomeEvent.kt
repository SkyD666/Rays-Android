package com.skyd.rays.ui.screen.home

import com.skyd.rays.base.IUiEvent

data class HomeEvent(
    val homeResultUiEvent: HomeResultUiEvent? = null,
) : IUiEvent

sealed class HomeResultUiEvent {
    data class Success(val successCount: Int) : HomeResultUiEvent()
}
