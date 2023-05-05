package com.skyd.rays.ui.screen.home

import com.skyd.rays.base.IUiIntent
import com.skyd.rays.model.bean.StickerWithTags

sealed class HomeIntent : IUiIntent {
    data class GetStickerDetails(val stickerUuid: String) : HomeIntent()
    data class DeleteStickerWithTags(val stickerUuid: String) : HomeIntent()
    data class GetStickerWithTagsList(val keyword: String) : HomeIntent()
    data class SortStickerWithTagsList(val data: List<StickerWithTags>) : HomeIntent()
    data class ReverseStickerWithTagsList(val data: List<StickerWithTags>) : HomeIntent()
}