package com.skyd.rays.ui.screen.search.multiselect

import com.skyd.rays.base.mvi.MviSingleEvent

sealed interface MultiSelectEvent : MviSingleEvent {
    sealed interface DeleteStickerWithTags : MultiSelectEvent {
        class Failed(val msg: String) : DeleteStickerWithTags
    }

    sealed interface ExportStickers : MultiSelectEvent {
        data class Success(val successCount: Int) : ExportStickers
    }
}
