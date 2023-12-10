package com.skyd.rays.ui.screen.detail

import com.skyd.rays.base.mvi.MviIntent

sealed interface DetailIntent : MviIntent {
    data class GetStickerDetailsAndAddClickCount(val stickerUuid: String) : DetailIntent
    data class RefreshStickerDetails(val stickerUuid: String) : DetailIntent
    data class DeleteStickerWithTags(val stickerUuid: String) : DetailIntent
    data class ExportStickers(val stickerUuid: String) : DetailIntent
}