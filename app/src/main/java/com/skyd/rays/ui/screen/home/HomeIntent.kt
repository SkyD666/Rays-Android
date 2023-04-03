package com.skyd.rays.ui.screen.home

import com.skyd.rays.base.IUiIntent

sealed class HomeIntent : IUiIntent {
    data class GetStickerDetails(val stickerUuid: String) : HomeIntent()
    data class DeleteStickerWithTags(val stickerUuid: String) : HomeIntent()
    data class GetStickerWithTagsList(val keyword: String) : HomeIntent()
}