package com.skyd.rays.ui.screen.home

import com.skyd.rays.base.IUiIntent
import com.skyd.rays.model.bean.StickerWithTags

sealed class HomeIntent : IUiIntent {
    data class UpdateThemeColor(val stickerUuid: String, val primaryColor: Int) : HomeIntent()
    data class GetStickerDetails(val stickerUuid: String) :
        HomeIntent()

    data class DeleteStickerWithTags(val stickerUuids: List<String>) : HomeIntent()
    data class GetStickerWithTagsList(val keyword: String) : HomeIntent()
    data class SortStickerWithTagsList(val data: List<StickerWithTags>) : HomeIntent()
    data class ReverseStickerWithTagsList(val data: List<StickerWithTags>) : HomeIntent()
    data class AddClickCountAndGetStickerDetails(val stickerUuid: String, val count: Int = 1) :
        HomeIntent()

    data class ExportStickers(val stickerUuids: List<String>) : HomeIntent()
}