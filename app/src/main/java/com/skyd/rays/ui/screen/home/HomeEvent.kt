package com.skyd.rays.ui.screen.home

import com.skyd.rays.base.IUiEvent

class HomeEvent(
    val homeResultUiEvent: HomeResultUiEvent? = null,
) : IUiEvent

sealed class HomeResultUiEvent {
    class Success(val successCount: Int) : HomeResultUiEvent()
}
