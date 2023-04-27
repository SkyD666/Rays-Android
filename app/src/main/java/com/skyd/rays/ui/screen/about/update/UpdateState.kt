package com.skyd.rays.ui.screen.about.update

import com.skyd.rays.base.IUiState
import com.skyd.rays.model.bean.UpdateBean

data class UpdateState(
    var updateUiState: UpdateUiState,
) : IUiState

sealed class UpdateUiState {
    data class OpenNewerDialog(val data: UpdateBean) : UpdateUiState()
    object OpenNoUpdateDialog : UpdateUiState()
    object Init : UpdateUiState()
}