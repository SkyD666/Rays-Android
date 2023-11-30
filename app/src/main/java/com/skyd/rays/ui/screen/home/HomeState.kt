package com.skyd.rays.ui.screen.home

import com.skyd.rays.base.mvi.MviViewState
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.TagBean

data class HomeState(
    val homeListState: HomeListState,
    val searchResultState: SearchResultState,
    val popularTagsState: PopularTagsState,
    val loadingDialog: Boolean,
) : MviViewState {
    companion object {
        fun initial() = HomeState(
            homeListState = HomeListState.Init,
            searchResultState = SearchResultState.Init,
            popularTagsState = PopularTagsState.Init,
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
    ) : HomeListState()
}

sealed class SearchResultState {
    var loading: Boolean = false

    data object Init : SearchResultState()
    data class Success(val stickerWithTagsList: List<StickerWithTags>) : SearchResultState()
}

sealed class PopularTagsState {
    data object Init : PopularTagsState()
    data object Loading : PopularTagsState()
    data class Success(val popularTags: List<Pair<String, Float>>) : PopularTagsState()
}
