package com.skyd.rays.ui.screen.search

import com.skyd.rays.model.bean.StickerWithTags

internal sealed interface SearchPartialStateChange {
    fun reduce(oldState: SearchState): SearchState

    data object LoadingDialog : SearchPartialStateChange {
        override fun reduce(oldState: SearchState): SearchState =
            oldState.copy(loadingDialog = true)
    }

    sealed interface SearchDataResult : SearchPartialStateChange {
        override fun reduce(oldState: SearchState): SearchState {
            return when (this) {
                is Success -> oldState.copy(
                    searchDataState = SearchDataState.Success(
                        stickerWithTagsList = stickerWithTagsList,
                        popularTags = popularTags,
                    ),
                    loadingDialog = false,
                )

                is Failed,
                ClearResultData -> oldState.copy(
                    searchDataState = oldState.searchDataState.let { searchDataState ->
                        when (searchDataState) {
                            is SearchDataState.Success -> SearchDataState.Success(
                                stickerWithTagsList = emptyList(),
                                popularTags = searchDataState.popularTags,
                            )

                            SearchDataState.Init -> searchDataState
                        }
                    },
                    loadingDialog = false,
                )

                Loading -> oldState.copy(
                    searchDataState = oldState.searchDataState.apply { loading = true }
                )
            }
        }

        data object Loading : SearchDataResult
        data object ClearResultData : SearchDataResult
        data class Failed(val msg: String) : SearchDataResult
        data class Success(
            val stickerWithTagsList: List<StickerWithTags>,
            val popularTags: List<String>
        ) : SearchDataResult
    }

    data class AddSelectedStickers(
        val stickers: Collection<String>
    ) : SearchPartialStateChange {
        override fun reduce(oldState: SearchState) = oldState.copy(
            selectedStickers = oldState.selectedStickers + stickers,
            loadingDialog = false,
        )
    }

    data class RemoveSelectedStickers(
        val stickers: Collection<String>
    ) : SearchPartialStateChange {
        override fun reduce(oldState: SearchState) = oldState.copy(
            selectedStickers = oldState.selectedStickers - stickers.toSet(),
            loadingDialog = false,
        )
    }
}
