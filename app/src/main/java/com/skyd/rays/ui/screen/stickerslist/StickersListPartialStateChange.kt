package com.skyd.rays.ui.screen.stickerslist

import androidx.paging.PagingData
import com.skyd.rays.model.bean.StickerWithTags
import kotlinx.coroutines.flow.Flow

internal sealed interface StickersListPartialStateChange {
    fun reduce(oldState: StickersListState): StickersListState

    data object LoadingDialog : StickersListPartialStateChange {
        override fun reduce(oldState: StickersListState) =
            oldState.copy(loadingDialog = true)
    }

    sealed interface StickersList : StickersListPartialStateChange {
        override fun reduce(oldState: StickersListState): StickersListState {
            return when (this) {
                is Success -> oldState.copy(
                    listState = ListState.Success(stickerWithTagsPagingFlow),
                    loadingDialog = false,
                )

                Loading -> oldState.copy(
                    listState = oldState.listState.apply { loading = true }
                )
            }
        }

        data object Loading : StickersList
        data class Success(val stickerWithTagsPagingFlow: Flow<PagingData<StickerWithTags>>) :
            StickersList
    }

    sealed interface InverseSelectedStickers : StickersListPartialStateChange {
        override fun reduce(oldState: StickersListState): StickersListState {
            return when (this) {
                is Success -> oldState.copy(
                    selectedStickers = stickers.toSet(),
                    loadingDialog = false,
                )

                is Failed -> oldState.copy(loadingDialog = false)
            }
        }

        data class Success(val stickers: List<String>) : InverseSelectedStickers
        data class Failed(val msg: String) : InverseSelectedStickers
    }

    data class AddSelectedStickers(
        val stickers: Collection<String>
    ) : StickersListPartialStateChange {
        override fun reduce(oldState: StickersListState) = oldState.copy(
            selectedStickers = oldState.selectedStickers + stickers,
            loadingDialog = false,
        )
    }

    data class RemoveSelectedStickers(
        val stickers: Collection<String>
    ) : StickersListPartialStateChange {
        override fun reduce(oldState: StickersListState) = oldState.copy(
            selectedStickers = oldState.selectedStickers - stickers.toSet(),
            loadingDialog = false,
        )
    }
}
