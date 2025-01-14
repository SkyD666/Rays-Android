package com.skyd.rays.ui.screen.settings.searchconfig

import com.skyd.rays.base.mvi.MviSingleEvent

sealed interface SearchConfigEvent : MviSingleEvent {
    sealed interface EnableAddScreenImageSearchUiEvent : SearchConfigEvent {
        class Failed(val msg: String) : EnableAddScreenImageSearchUiEvent
    }
}
