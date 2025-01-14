package com.skyd.rays.ui.screen.stickerslist

import com.skyd.rays.base.mvi.MviIntent

sealed interface StickersListIntent : MviIntent {
    data class GetStickersList(val query: String) : StickersListIntent
    data class ExportStickers(val query: String) : StickersListIntent
}