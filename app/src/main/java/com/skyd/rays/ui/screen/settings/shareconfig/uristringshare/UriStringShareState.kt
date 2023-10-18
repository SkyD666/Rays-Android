package com.skyd.rays.ui.screen.settings.shareconfig.uristringshare

import com.skyd.rays.base.IUiState
import com.skyd.rays.model.bean.UriStringShareDataBean

data class UriStringShareState(
    val uriStringShareResultUiState: UriStringShareResultUiState,
) : IUiState

sealed class UriStringShareResultUiState {
    data object Init : UriStringShareResultUiState()
    data class Success(val data: List<UriStringShareDataBean>) : UriStringShareResultUiState()
}