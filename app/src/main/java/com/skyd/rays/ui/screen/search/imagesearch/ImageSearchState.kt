package com.skyd.rays.ui.screen.search.imagesearch

import com.skyd.rays.base.mvi.MviViewState
import com.skyd.rays.model.bean.StickerWithTags

data class ImageSearchState(
    val imageSearchResultState: ImageSearchResultState,
    val selectedStickers: Set<String>,
    val loadingDialog: Boolean,
) : MviViewState {
    companion object {
        fun initial() = ImageSearchState(
            imageSearchResultState = ImageSearchResultState.Init,
            selectedStickers = emptySet(),
            loadingDialog = false,
        )
    }
}

sealed interface ImageSearchResultState {
    data object Init : ImageSearchResultState
    data class Success(val stickers: List<StickerWithTags>) : ImageSearchResultState
}