package com.skyd.rays.ui.screen.home

import com.skyd.rays.base.mvi.MviIntent
import com.skyd.rays.model.bean.StickerWithTags

sealed interface HomeIntent : MviIntent {
    data object Initial : HomeIntent
    data object RefreshHomeList : HomeIntent
    data class DeleteStickerWithTags(val stickerUuids: List<String>) : HomeIntent
    data class GetStickerWithTagsList(val keyword: String) : HomeIntent
    data class SortStickerWithTagsList(val data: List<StickerWithTags>) : HomeIntent
    data class ReverseStickerWithTagsList(val data: List<StickerWithTags>) : HomeIntent
    data class AddClickCount(val stickerUuid: String, val count: Int = 1) : HomeIntent
    data class ExportStickers(val stickerUuids: List<String>) : HomeIntent
    data object GetSearchBarPopularTagsList : HomeIntent
}