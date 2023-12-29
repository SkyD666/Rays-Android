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
            val popularTags: List<Pair<String, Float>>
        ) : SearchDataResult
    }

    sealed interface ExportStickers : SearchPartialStateChange {
        class Success(val successCount: Int) : ExportStickers {
            override fun reduce(oldState: SearchState) = oldState.copy(loadingDialog = false)
        }
    }

    sealed interface DeleteStickerWithTags : SearchPartialStateChange {
        class Success(val stickerUuids: List<String>) : DeleteStickerWithTags {
            override fun reduce(oldState: SearchState): SearchState = oldState.copy(
                searchDataState = oldState.searchDataState.let { searchResultState ->
                    if (searchResultState is SearchDataState.Success) {
                        searchResultState.copy(
                            stickerWithTagsList = searchResultState.stickerWithTagsList
                                .filter { it.sticker.uuid !in stickerUuids },
                        )
                    } else searchResultState
                },
                loadingDialog = false,
            )
        }
    }
}
