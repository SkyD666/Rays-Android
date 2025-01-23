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

    sealed interface DeleteStickerShareTime : DataPartialStateChange {
        data class Success(val time: Long) : DeleteStickerShareTime {
            override fun reduce(oldState: DataState): DataState =
                oldState.copy(loadingDialog = false)
        }
    }

    sealed interface DeleteVectorDbFiles : DataPartialStateChange {
        override fun reduce(oldState: DataState) = oldState.copy(loadingDialog = false)

        data class Success(val time: Long) : DeleteVectorDbFiles
        data class Failed(val msg: String) : DeleteVectorDbFiles
    }
}
