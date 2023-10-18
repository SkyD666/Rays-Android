package com.skyd.rays.ui.screen.about.update

import com.skyd.rays.base.IUiState
import com.skyd.rays.model.bean.UpdateBean

data class UpdateState(
    var updateUiState: UpdateUiState,
) : IUiState

sealed class UpdateUiState {
    data class OpenNewerDialog(val data: UpdateBean) : UpdateUiState()
    data object OpenNoUpdateDialog : UpdateUiState()
    data object Init : UpdateUiState()
}