package com.skyd.rays.ui.screen.minitool.selfiesegmentation

import android.graphics.Bitmap
import com.skyd.rays.base.IUiState

data class SelfieSegmentationState(
    val selfieSegmentationResultUiState: SelfieSegmentationResultUiState,
) : IUiState

sealed class SelfieSegmentationResultUiState {
    data object Init : SelfieSegmentationResultUiState()
    data class Success(val image: Bitmap) : SelfieSegmentationResultUiState()
}
