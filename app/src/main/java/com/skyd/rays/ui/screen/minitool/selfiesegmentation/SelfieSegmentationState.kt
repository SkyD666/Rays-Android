package com.skyd.rays.ui.screen.minitool.selfiesegmentation

import android.graphics.Bitmap
import com.skyd.rays.base.mvi.MviViewState

data class SelfieSegmentationState(
    val selfieSegmentationResultState: SelfieSegmentationResultState,
    val loadingDialog: Boolean,
) : MviViewState {
    companion object {
        fun initial() = SelfieSegmentationState(
            selfieSegmentationResultState = SelfieSegmentationResultState.Init,
            loadingDialog = false,
        )
    }
}

sealed class SelfieSegmentationResultState {
    data object Init : SelfieSegmentationResultState()
    data class Success(val image: Bitmap) : SelfieSegmentationResultState()
}