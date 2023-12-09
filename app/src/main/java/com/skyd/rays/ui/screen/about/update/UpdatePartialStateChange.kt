package com.skyd.rays.ui.screen.about.update

import com.skyd.rays.model.bean.UpdateBean

internal sealed interface UpdatePartialStateChange {
    fun reduce(oldState: UpdateState): UpdateState

    data object LoadingDialog : UpdatePartialStateChange {
        override fun reduce(oldState: UpdateState) = oldState.copy(loadingDialog = true)
    }

    data object RequestUpdate : UpdatePartialStateChange {
        override fun reduce(oldState: UpdateState): UpdateState = oldState.copy(
            loadingDialog = false,
        )
    }

    sealed interface CheckUpdate : UpdatePartialStateChange {
        override fun reduce(oldState: UpdateState): UpdateState {
            return when (this) {
                is HasUpdate -> oldState.copy(
                    updateUiState = UpdateUiState.OpenNewerDialog(data),
                    loadingDialog = false,
                )

                NoUpdate -> oldState.copy(
                    updateUiState = UpdateUiState.OpenNoUpdateDialog,
                    loadingDialog = false,
                )
            }
        }

        data object NoUpdate : CheckUpdate
        data class HasUpdate(val data: UpdateBean) : CheckUpdate
    }
}
