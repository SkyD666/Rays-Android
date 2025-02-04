package com.skyd.rays.ui.screen.mergestickers

import com.skyd.rays.base.mvi.MviViewState
import com.skyd.rays.model.bean.StickerWithTags

data class MergeStickersState(
    val stickersState: StickersState,
    val selectedTags: List<String>,
    val loadingDialog: Boolean,
) : MviViewState {
    companion object {
        fun initial() = MergeStickersState(
            stickersState = StickersState.Init,
            selectedTags = emptyList(),
            loadingDialog = false,
        )
    }
}

sealed interface StickersState {
    data object Init : StickersState
    data class Failed(val msg: String) : StickersState
    data class Success(val stickersList: List<StickerWithTags>) : StickersState
}
