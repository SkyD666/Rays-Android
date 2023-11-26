package com.skyd.rays.ui.screen.home

import com.skyd.rays.base.IUiIntent
import com.skyd.rays.model.bean.StickerWithTags

sealed class HomeIntent : IUiIntent {
    override val showLoading: Boolean = false

    data object GetHomeList : HomeIntent()

    data class DeleteStickerWithTags(val stickerUuids: List<String>) : HomeIntent() {
        override val showLoading: Boolean = true
    }

    data class GetStickerWithTagsList(val keyword: String) : HomeIntent()
    data class SortStickerWithTagsList(val data: List<StickerWithTags>) : HomeIntent()
    data class ReverseStickerWithTagsList(val data: List<StickerWithTags>) : HomeIntent()
    data class AddClickCount(val stickerUuid: String, val count: Int = 1) : HomeIntent()

    data class ExportStickers(val stickerUuids: List<String>) : HomeIntent() {
        override val showLoading: Boolean = true
    }

    data object GetSearchBarPopularTagsList : HomeIntent()
}