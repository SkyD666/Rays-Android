package com.skyd.rays.ui.screen.stickerslist

import com.skyd.rays.base.mvi.MviIntent

sealed interface StickersListIntent : MviIntent {
    data object Initial : StickersListIntent
    data class RefreshStickersList(val query: String) : StickersListIntent
    data class AddClickCount(val stickerUuid: String, val count: Int = 1) : StickersListIntent
}