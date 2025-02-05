package com.skyd.rays.ui.screen.search.multiselect

import com.skyd.rays.base.mvi.MviIntent

sealed interface MultiSelectIntent : MviIntent {
    data object Init : MultiSelectIntent
    data class SendStickers(val stickersUuids: Collection<String>) : MultiSelectIntent
    data class DeleteStickerWithTags(val stickersUuids: Collection<String>) : MultiSelectIntent
    data class ExportStickers(val stickerUuids: Collection<String>) : MultiSelectIntent
}