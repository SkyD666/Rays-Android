package com.skyd.rays.ui.screen.home

import com.skyd.rays.base.IUiEvent
import com.skyd.rays.base.PartialChange

class HomeEvent(
    val homeResultUiEvent: HomeResultUiEvent? = null,
) : IUiEvent

sealed class HomeResultUiEvent : PartialChange<HomeState> {
    override fun reduce(oldState: HomeState): HomeState = when (this) {
        is Success -> oldState
    }

    class Success(val successCount: Int) : HomeResultUiEvent()
}

sealed class DeleteStickerWithTagsResultUiEvent : PartialChange<HomeState> {
    override fun reduce(oldState: HomeState): HomeState = when (this) {
        is Success -> {
            val searchResultUiState = oldState.searchResultUiState
            if (searchResultUiState is SearchResultUiState.Success) {
                oldState.copy(
                    searchResultUiState = searchResultUiState.copy(
                        stickerWithTagsList = searchResultUiState.stickerWithTagsList
                            .filter { !stickerUuids.contains(it.sticker.uuid) }
                    )
                )
            } else {
                oldState
            }
        }
    }

    class Success(val stickerUuids: List<String>) : DeleteStickerWithTagsResultUiEvent()
}

sealed class AddClickCountResultUiEvent : PartialChange<HomeState> {
    override fun reduce(oldState: HomeState): HomeState = when (this) {
        Success -> oldState
    }

    data object Success : AddClickCountResultUiEvent()
}