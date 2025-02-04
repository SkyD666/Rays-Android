package com.skyd.rays.ui.screen.mergestickers

import com.skyd.rays.base.mvi.MviIntent
import com.skyd.rays.model.bean.StickerWithTags

sealed interface MergeStickersIntent : MviIntent {
    data class Init(val stickers: List<String>) : MergeStickersIntent
    data class Merge(
        val oldStickerUuid: String,
        val sticker: StickerWithTags,
        val deleteUuids: List<String>,
    ) : MergeStickersIntent
    data class RemoveSelectedTag(val tag: String) : MergeStickersIntent
    data class AddSelectedTag(val tag: String) : MergeStickersIntent
    data class ReplaceAllSelectedTags(val tags: List<String>) : MergeStickersIntent
}