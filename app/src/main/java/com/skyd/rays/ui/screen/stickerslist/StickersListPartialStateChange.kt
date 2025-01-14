package com.skyd.rays.ui.screen.stickerslist

import androidx.paging.PagingData
import com.skyd.rays.model.bean.StickerWithTags
import kotlinx.coroutines.flow.Flow

internal sealed interface StickersListPartialStateChange {
    fun reduce(oldState: StickersListState): StickersListState

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

    sealed interface ExportStickers : StickersListPartialStateChange {
        override fun reduce(oldState: StickersListState): StickersListState {
            return when (this) {
                is Success -> oldState.copy(loadingDialog = false)
                Loading -> oldState.copy(loadingDialog = false)
            }
        }

        data object Loading : ExportStickers
        data class Success(val stickerUuids: List<String>) : ExportStickers
    }
}
