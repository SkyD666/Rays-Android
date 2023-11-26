package com.skyd.rays.ui.screen.detail

import com.skyd.rays.base.IUiIntent

sealed class DetailIntent : IUiIntent {
    data class GetStickerDetails(val stickerUuid: String) : DetailIntent()

    data class DeleteStickerWithTags(val stickerUuids: List<String>) : DetailIntent()

    data class ExportStickers(val stickerUuids: List<String>) : DetailIntent()
}