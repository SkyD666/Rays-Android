package com.skyd.rays.ui.screen.settings.data

internal sealed interface DataPartialStateChange {
    fun reduce(oldState: DataState): DataState

    data object LoadingDialog : DataPartialStateChange {
        override fun reduce(oldState: DataState): DataState = oldState.copy(loadingDialog = true)
    }

    data object Init : DataPartialStateChange {
        override fun reduce(oldState: DataState) = oldState
    }

    sealed interface DeleteAllData : DataPartialStateChange {
        data class Success(val time: Long) : DeleteAllData {
            override fun reduce(oldState: DataState): DataState =
                oldState.copy(loadingDialog = false)
        }
    }
}
