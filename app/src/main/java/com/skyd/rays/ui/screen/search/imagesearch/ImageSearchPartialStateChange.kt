package com.skyd.rays.ui.screen.search.imagesearch

import com.skyd.rays.model.bean.StickerWithTags

internal sealed interface ImageSearchPartialStateChange {
    fun reduce(oldState: ImageSearchState): ImageSearchState

    data object LoadingDialog : ImageSearchPartialStateChange {
        override fun reduce(oldState: ImageSearchState) =
            oldState.copy(loadingDialog = true)
    }

    data class Init(val stickers: List<StickerWithTags>) : ImageSearchPartialStateChange {
        override fun reduce(oldState: ImageSearchState): ImageSearchState {
            return oldState.copy(
                imageSearchResultState = ImageSearchResultState.Success(stickers),
                loadingDialog = false,
            )
        }
    }

    sealed interface UpdateImage : ImageSearchPartialStateChange {
        override fun reduce(oldState: ImageSearchState): ImageSearchState {
            return oldState.copy(loadingDialog = false)
        }

        data object Success : UpdateImage
        data class Failed(val msg: String) : UpdateImage
    }

    sealed interface DeleteStickerWithTags : ImageSearchPartialStateChange {
        override fun reduce(oldState: ImageSearchState) = oldState.copy(
            loadingDialog = false,
        )

        data object Success : DeleteStickerWithTags
        data class Failed(val msg: String) : DeleteStickerWithTags
    }

    data class AddSelectedStickers(
        val stickers: Collection<String>
    ) : ImageSearchPartialStateChange {
        override fun reduce(oldState: ImageSearchState) = oldState.copy(
            selectedStickers = oldState.selectedStickers + stickers,
            loadingDialog = false,
        )
    }

    data class RemoveSelectedStickers(
        val stickers: Collection<String>
    ) : ImageSearchPartialStateChange {
        override fun reduce(oldState: ImageSearchState) = oldState.copy(
            selectedStickers = oldState.selectedStickers - stickers.toSet(),
            loadingDialog = false,
        )
    }
}
