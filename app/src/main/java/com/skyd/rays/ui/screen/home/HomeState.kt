package com.skyd.rays.ui.screen.home

import com.skyd.rays.base.IUiState
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.preference.CurrentStickerUuidPreference

data class HomeState(
    val stickerDetailUiState: StickerDetailUiState,
    val searchResultUiState: SearchResultUiState,
) : IUiState

sealed class StickerDetailUiState {
    data class Init(val stickerUuid: String = CurrentStickerUuidPreference.default) :
        StickerDetailUiState()

    data class Success(val stickerWithTags: StickerWithTags) : StickerDetailUiState()
}

sealed class SearchResultUiState {
    object Init : SearchResultUiState()
    data class Success(val stickerWithTagsList: List<StickerWithTags>) : SearchResultUiState()
}