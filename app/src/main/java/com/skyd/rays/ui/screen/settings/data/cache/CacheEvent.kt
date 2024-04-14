package com.skyd.rays.ui.screen.settings.data.cache

import com.skyd.rays.base.mvi.MviSingleEvent

sealed interface CacheEvent : MviSingleEvent {
    sealed interface DeleteDocumentsProviderThumbnailsResultEvent : CacheEvent {
        data class Success(val time: Long) : DeleteDocumentsProviderThumbnailsResultEvent
    }

    sealed interface DeleteAllMimetypesResultEvent : CacheEvent {
        data class Success(val time: Long) : DeleteAllMimetypesResultEvent
    }
}
