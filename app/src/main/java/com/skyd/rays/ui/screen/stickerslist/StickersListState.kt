package com.skyd.rays.ui.screen.stickerslist

import com.skyd.rays.base.mvi.MviViewState
import com.skyd.rays.model.bean.StickerWithTags

data class StickersListState(
    val listState: ListState,
    val loadingDialog: Boolean,
) : MviViewState {
    companion object {
        fun initial() = StickersListState(
            listState = ListState.Init,
            loadingDialog = false,
        )
    }
}

sealed class ListState {
    var loading: Boolean = false

    data object Init : ListState()
    data class Success(val stickerWithTagsList: List<StickerWithTags>) : ListState()
}
