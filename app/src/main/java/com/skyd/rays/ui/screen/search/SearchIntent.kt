package com.skyd.rays.ui.screen.search

import com.skyd.rays.base.mvi.MviIntent

sealed interface SearchIntent : MviIntent {
    data class DeleteStickerWithTags(val stickerUuids: List<String>) : SearchIntent
    data object GetSearchData : SearchIntent
    data class ExportStickers(val stickerUuids: List<String>) : SearchIntent
}