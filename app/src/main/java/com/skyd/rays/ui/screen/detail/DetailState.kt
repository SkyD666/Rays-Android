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
    // 当前页面的loading
    var loading: Boolean = false
    data object Init : StickerDetailState()
    data class Success(val stickerWithTags: StickerWithTags) : StickerDetailState()
}
