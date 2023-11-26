package com.skyd.rays.ui.screen.home

import android.util.Log
import com.skyd.rays.base.IUiState
import com.skyd.rays.base.PartialChange
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.TagBean

data class HomeState(
    val homeUiState: HomeUiState,
    val searchResultUiState: SearchResultUiState,
    val popularTagsUiState: PopularTagsUiState
) : IUiState

sealed class HomeUiState : PartialChange<HomeState> {
    override fun reduce(oldState: HomeState): HomeState = when (this) {
        Init -> oldState
        is Success -> oldState.copy(
            homeUiState = Success(
                recommendTagsList = recommendTagsList,
                randomTagsList = randomTagsList,
                recentCreatedStickersList = recentCreatedStickersList,
            )
        )
    }.apply { Log.e("TAG", "reduce: HomeUiState", ) }

    data object Init : HomeUiState()

    data class Success(
        val recommendTagsList: List<TagBean>,
        val randomTagsList: List<TagBean>,
        val recentCreatedStickersList: List<StickerWithTags>,
    ) : HomeUiState()
}

sealed class SearchResultUiState : PartialChange<HomeState> {
    override fun reduce(oldState: HomeState): HomeState = when (this) {
        Init -> oldState
        is Success -> oldState.copy(
            searchResultUiState = Success(
                stickerWithTagsList = stickerWithTagsList,
            )
        )
    }.apply { Log.e("TAG", "reduce: SearchResultUiState", ) }

    data object Init : SearchResultUiState()
    data class Success(val stickerWithTagsList: List<StickerWithTags>) : SearchResultUiState()
}

sealed class PopularTagsUiState : PartialChange<HomeState> {
    override fun reduce(oldState: HomeState): HomeState = when (this) {
        Init -> oldState
        is Success -> oldState.copy(
            popularTagsUiState = Success(
                popularTags = popularTags,
            )
        )
    }

    data object Init : PopularTagsUiState()
    data class Success(val popularTags: List<Pair<String, Float>>) : PopularTagsUiState()
}