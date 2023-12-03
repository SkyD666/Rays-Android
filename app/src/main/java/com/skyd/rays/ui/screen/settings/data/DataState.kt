package com.skyd.rays.ui.screen.settings.data

import com.skyd.rays.base.mvi.MviViewState

data class DataState(
    val loadingDialog: Boolean,
) : MviViewState {
    companion object {
        fun initial() = DataState(
            loadingDialog = false,
        )
    }
}
