package com.skyd.rays.ui.screen.add

import com.skyd.rays.base.mvi.MviViewState
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.UriWithStickerUuidBean

data class AddState(
    val waitingList: List<UriWithStickerUuidBean>,
    val getStickersWithTagsState: GetStickersWithTagsState,
    val suggestTags: List<String>,
    val addedTags: List<String>,
    val addToAllTags: List<String>,
    val loadingDialog: Boolean,
) : MviViewState {
    val currentSticker: UriWithStickerUuidBean? = waitingList.firstOrNull()

    companion object {
        fun initial() = AddState(
            waitingList = emptyList(),
            getStickersWithTagsState = GetStickersWithTagsState.Init,
            suggestTags = emptyList(),
            addedTags = emptyList(),
            addToAllTags = emptyList(),
            loadingDialog = false,
        )
    }
}

sealed interface GetStickersWithTagsState {
    data class Success(val stickerWithTags: StickerWithTags) : GetStickersWithTagsState
    data object Init : GetStickersWithTagsState
    data object Failed : GetStickersWithTagsState
}