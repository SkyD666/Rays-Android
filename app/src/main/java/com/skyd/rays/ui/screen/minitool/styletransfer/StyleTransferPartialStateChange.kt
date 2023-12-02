package com.skyd.rays.ui.screen.minitool.styletransfer

import android.graphics.Bitmap

internal sealed interface StyleTransferPartialStateChange {
    fun reduce(oldState: StyleTransferState): StyleTransferState

    data object LoadingDialog : StyleTransferPartialStateChange {
        override fun reduce(oldState: StyleTransferState) =
            oldState.copy(loadingDialog = true)
    }

    data object Init : StyleTransferPartialStateChange {
        override fun reduce(oldState: StyleTransferState) = oldState.copy(
            styleTransferResultState = StyleTransferResultState.Init,
            loadingDialog = false,
        )
    }

    sealed interface StyleTransfer : StyleTransferPartialStateChange {
        override fun reduce(oldState: StyleTransferState): StyleTransferState {
            return when (this) {
                is Success -> oldState.copy(
                    styleTransferResultState = StyleTransferResultState.Success(image = image),
                    loadingDialog = false,
                )
            }
        }

        data class Success(val image: Bitmap) : StyleTransfer
    }
}
