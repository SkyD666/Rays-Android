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
}
