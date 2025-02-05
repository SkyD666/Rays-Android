package com.skyd.rays.ui.screen.stickerslist

import com.skyd.rays.base.mvi.MviIntent

sealed interface StickersListIntent : MviIntent {
    data class GetStickersList(val query: String) : StickersListIntent
    data class InverseSelectedStickers(
        val query: String,
        val selectedStickers: Collection<String>,
    ) : StickersListIntent

    data class AddSelectedStickers(val stickers: Collection<String>) : StickersListIntent
    data class RemoveSelectedStickers(val stickers: Collection<String>) :
        StickersListIntent
}