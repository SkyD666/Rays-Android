package com.skyd.rays.ui.screen.home

import com.skyd.rays.base.mvi.MviSingleEvent

sealed interface HomeEvent : MviSingleEvent {
    sealed interface ExportStickers : HomeEvent {
        class Success(val successCount: Int) : ExportStickers
    }

    sealed interface DeleteStickerWithTags : HomeEvent {
        class Success(val stickerUuids: List<String>) : DeleteStickerWithTags
    }

    sealed interface AddClickCount : HomeEvent {
        data object Success : AddClickCount
    }
}
