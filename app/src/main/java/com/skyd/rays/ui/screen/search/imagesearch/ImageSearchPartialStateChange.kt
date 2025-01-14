package com.skyd.rays.ui.screen.search.imagesearch

import com.skyd.rays.model.bean.StickerWithTags

internal sealed interface ImageSearchPartialStateChange {
    fun reduce(oldState: ImageSearchState): ImageSearchState

    data object LoadingDialog : ImageSearchPartialStateChange {
        override fun reduce(oldState: ImageSearchState) =
            oldState.copy(loadingDialog = true)
    }

    data object Init : ImageSearchPartialStateChange {
        override fun reduce(oldState: ImageSearchState) = oldState.copy(
            imageSearchResultState = ImageSearchResultState.Init,
            loadingDialog = false,
        )
    }

    sealed interface ImageSearch : ImageSearchPartialStateChange {
        data class Success(val stickers: List<StickerWithTags>) : ImageSearch {
            override fun reduce(oldState: ImageSearchState): ImageSearchState {
                return oldState.copy(
                    imageSearchResultState = ImageSearchResultState.Success(stickers),
                    loadingDialog = false,
                )
            }
        }

        data class Failed(val msg: String) : ImageSearch {
            override fun reduce(oldState: ImageSearchState): ImageSearchState {
                return oldState.copy(
                    imageSearchResultState = ImageSearchResultState.Init,
                    loadingDialog = false,
                )
            }
        }
    }
}
