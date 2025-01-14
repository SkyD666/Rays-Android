package com.skyd.rays.ui.screen.stickerslist

import com.skyd.rays.base.mvi.MviSingleEvent

sealed interface StickersListEvent : MviSingleEvent {
    sealed interface ExportStickers : StickersListEvent {
        data class Success(val stickerUuids: List<String>) : ExportStickers
    }
}
