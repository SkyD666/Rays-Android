package com.skyd.rays.ui.screen.search.imagesearch

import com.skyd.rays.base.mvi.MviSingleEvent

sealed interface ImageSearchEvent : MviSingleEvent {
    sealed interface SearchUiEvent : ImageSearchEvent {
        class Failed(val msg: String) : SearchUiEvent
    }
}
