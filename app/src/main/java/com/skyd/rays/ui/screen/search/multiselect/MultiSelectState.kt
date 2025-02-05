package com.skyd.rays.ui.screen.search.multiselect

import com.skyd.rays.base.mvi.MviViewState

data class MultiSelectState(
    val loadingDialog: Boolean,
) : MviViewState {
    companion object {
        fun initial() = MultiSelectState(
            loadingDialog = false,
        )
    }
}
