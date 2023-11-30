package com.skyd.rays.ui.screen.home

import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.TagBean

internal sealed interface HomePartialStateChange {
    fun reduce(oldState: HomeState): HomeState

    data object LoadingDialog : HomePartialStateChange {
        override fun reduce(oldState: HomeState): HomeState = oldState.copy(loadingDialog = true)
    }

    sealed interface HomeList : HomePartialStateChange {
        override fun reduce(oldState: HomeState): HomeState {
            return when (this) {
                Loading -> oldState.copy(
                    homeListState = oldState.homeListState.apply { loading = true }
                )

                is Success -> oldState.copy(
                    homeListState = HomeListState.Success(
                        recommendTagsList = recommendTagsList,
                        randomTagsList = randomTagsList,
                        recentCreatedStickersList = recentCreatedStickersList,
                        mostSharedStickersList = mostSharedStickersList,
                    ).apply { loading = true }
                )
            }
        }

        data object Loading : HomeList
        data class Success(
            val recommendTagsList: List<TagBean>,
            val randomTagsList: List<TagBean>,
            val recentCreatedStickersList: List<StickerWithTags>,
            val mostSharedStickersList: List<StickerWithTags>,
        ) : HomeList
    }

    sealed interface SearchResult : HomePartialStateChange {
        override fun reduce(oldState: HomeState): HomeState {
            return when (this) {
                is Success -> oldState.copy(
                    searchResultState = SearchResultState.Success(stickerWithTagsList),
                    loadingDialog = false,
                )

                Loading -> oldState.copy(
                    searchResultState = oldState.searchResultState.apply { loading = true }
                )
            }
        }

        data object Loading : SearchResult
        data class Success(val stickerWithTagsList: List<StickerWithTags>) : SearchResult
    }

    sealed interface PopularTags : HomePartialStateChange {
        data object Loading : PopularTags
        data class Success(val popularTags: List<Pair<String, Float>>) : PopularTags

        override fun reduce(oldState: HomeState) = when (this) {
            is Loading -> oldState.copy(
                popularTagsState = PopularTagsState.Loading
            )

            is Success -> oldState.copy(
                popularTagsState = PopularTagsState.Success(popularTags)
            )
        }
    }

    sealed interface ExportStickers : HomePartialStateChange {
        override fun reduce(oldState: HomeState): HomeState = oldState
        class Success(val successCount: Int) : ExportStickers
    }

    sealed interface DeleteStickerWithTags : HomePartialStateChange {
        class Success(val stickerUuids: List<String>) : DeleteStickerWithTags {
            override fun reduce(oldState: HomeState): HomeState = oldState.copy(
                homeListState = oldState.homeListState.let { homeListState ->
                    if (homeListState is HomeListState.Success) {
                        homeListState.copy(
                            recentCreatedStickersList = homeListState.recentCreatedStickersList
                                .filter { it.sticker.uuid !in stickerUuids },
                            mostSharedStickersList = homeListState.mostSharedStickersList
                                .filter { it.sticker.uuid !in stickerUuids },
                        )
                    } else homeListState
                },
                searchResultState = oldState.searchResultState.let { searchResultState ->
                    if (searchResultState is SearchResultState.Success) {
                        searchResultState.copy(
                            stickerWithTagsList = searchResultState.stickerWithTagsList
                                .filter { it.sticker.uuid !in stickerUuids },
                        )
                    } else searchResultState
                },
            )
        }
    }

    sealed interface AddClickCount : HomePartialStateChange {
        data object Success : AddClickCount {
            override fun reduce(oldState: HomeState): HomeState = oldState
        }
    }
}
