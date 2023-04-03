package com.skyd.rays.ui.screen.add

import com.skyd.rays.base.IUiState
import com.skyd.rays.model.bean.StickerWithTags

data class AddState(
    val getStickersWithTagsUiState: GetStickersWithTagsUiState,
) : IUiState

sealed class GetStickersWithTagsUiState {
    object Init : GetStickersWithTagsUiState()
    object Failed : GetStickersWithTagsUiState()
    data class Success(val stickerWithTags: StickerWithTags) : GetStickersWithTagsUiState()
}