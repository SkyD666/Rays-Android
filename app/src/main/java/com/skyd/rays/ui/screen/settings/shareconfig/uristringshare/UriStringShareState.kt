package com.skyd.rays.ui.screen.settings.shareconfig.uristringshare

import com.skyd.rays.base.mvi.MviViewState
import com.skyd.rays.model.bean.UriStringShareDataBean

data class UriStringShareState(
    val uriStringShareResultState: UriStringShareResultState,
    val loadingDialog: Boolean,
) : MviViewState {
    companion object {
        fun initial() = UriStringShareState(
            uriStringShareResultState = UriStringShareResultState.Init,
            loadingDialog = false,
        )
    }
}

sealed class UriStringShareResultState {
    data object Init : UriStringShareResultState()
    data class Success(val data: List<UriStringShareDataBean>) : UriStringShareResultState()
}