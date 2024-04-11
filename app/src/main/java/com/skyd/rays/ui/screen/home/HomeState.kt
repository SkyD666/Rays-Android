package com.skyd.rays.ui.screen.home

import com.skyd.rays.base.mvi.MviViewState
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.TagBean

data class HomeState(
    val homeListState: HomeListState,
    val loadingDialog: Boolean,
) : MviViewState {
    companion object {
        fun initial() = HomeState(
            homeListState = HomeListState.Init,
            loadingDialog = false,
        )
    }
}

sealed class HomeListState {
    var loading: Boolean = false

    data object Init : HomeListState()
    data class Success(
        val recommendTagsList: List<TagBean>,
        val randomTagsList: List<TagBean>,
        val recentCreatedStickersList: List<StickerWithTags>,
        val mostSharedStickersList: List<StickerWithTags>,
        val recentSharedStickersList: List<StickerWithTags>,
    ) : HomeListState()
}
