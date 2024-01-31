package com.skyd.rays.ui.screen.detail

import com.skyd.rays.base.mvi.MviViewState
import com.skyd.rays.model.bean.StickerWithTags

data class DetailState(
    val stickerDetailState: StickerDetailState,
) : MviViewState {
    companion object {
        fun initial() = DetailState(
            stickerDetailState = StickerDetailState.Init,
        )
    }
}

sealed class StickerDetailState {
    data object Init : StickerDetailState()
    data object Loading : StickerDetailState()
    data class Success(val stickerWithTags: StickerWithTags) : StickerDetailState()
}
