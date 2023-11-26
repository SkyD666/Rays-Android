package com.skyd.rays.ui.screen.detail

import com.skyd.rays.base.IUiState
import com.skyd.rays.model.bean.StickerWithTags

data class DetailState(
    val stickerDetailUiState: StickerDetailUiState,
) : IUiState

sealed class StickerDetailUiState {
    data object Empty : StickerDetailUiState()

    data class Success(val stickerWithTags: StickerWithTags) : StickerDetailUiState()
}
