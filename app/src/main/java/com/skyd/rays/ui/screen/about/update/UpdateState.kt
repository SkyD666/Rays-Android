package com.skyd.rays.ui.screen.about.update

import com.skyd.rays.base.mvi.MviViewState
import com.skyd.rays.model.bean.UpdateBean

data class UpdateState(
    var updateUiState: UpdateUiState,
    var loadingDialog: Boolean,
) : MviViewState {
    companion object {
        fun initial() = UpdateState(
            updateUiState = UpdateUiState.Init,
            loadingDialog = false,
        )
    }
}

sealed class UpdateUiState {
    data class OpenNewerDialog(val data: UpdateBean) : UpdateUiState()
    data object OpenNoUpdateDialog : UpdateUiState()
    data object Init : UpdateUiState()
}