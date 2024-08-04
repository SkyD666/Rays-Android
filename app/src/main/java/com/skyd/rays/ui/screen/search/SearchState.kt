package com.skyd.rays.ui.screen.search

import com.skyd.rays.base.mvi.MviViewState
import com.skyd.rays.model.bean.StickerWithTags

data class SearchState(
    val searchDataState: SearchDataState,
    val loadingDialog: Boolean,
) : MviViewState {
    companion object {
        fun initial() = SearchState(
            searchDataState = SearchDataState.Init,
            loadingDialog = false,
        )
    }
}

sealed class SearchDataState {
    var loading: Boolean = false

    data object Init : SearchDataState()
    data class Success(
        val stickerWithTagsList: List<StickerWithTags>,
        val popularTags: List<String>,
    ) : SearchDataState()
}
