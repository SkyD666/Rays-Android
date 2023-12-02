package com.skyd.rays.ui.screen.minitool.styletransfer

import android.graphics.Bitmap
import com.skyd.rays.base.mvi.MviViewState
import com.skyd.rays.ui.screen.minitool.selfiesegmentation.SelfieSegmentationResultState
import com.skyd.rays.ui.screen.minitool.selfiesegmentation.SelfieSegmentationState

data class StyleTransferState(
    val styleTransferResultState: StyleTransferResultState,
    val loadingDialog: Boolean,
) : MviViewState {
    companion object {
        fun initial() = StyleTransferState(
            styleTransferResultState = StyleTransferResultState.Init,
            loadingDialog = false,
        )
    }
}

sealed interface StyleTransferResultState {
    data object Init : StyleTransferResultState
    data class Success(val image: Bitmap) : StyleTransferResultState
}
