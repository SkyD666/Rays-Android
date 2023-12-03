package com.skyd.rays.ui.screen.search

import com.skyd.rays.base.mvi.MviSingleEvent

sealed interface SearchEvent : MviSingleEvent {
    sealed interface ExportStickers : SearchEvent {
        data class Success(val successCount: Int) : ExportStickers
    }

    sealed interface DeleteStickerWithTags : SearchEvent {
        data class Success(val stickerUuids: List<String>) : DeleteStickerWithTags
    }
}
