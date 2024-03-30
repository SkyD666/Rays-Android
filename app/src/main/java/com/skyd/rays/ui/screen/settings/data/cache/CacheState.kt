package com.skyd.rays.ui.screen.settings.data.cache

import com.skyd.rays.base.mvi.MviViewState

data class CacheState(
    val loadingDialog: Boolean,
) : MviViewState {
    companion object {
        fun initial() = CacheState(
            loadingDialog = false,
        )
    }
}
