package com.skyd.rays.ui.screen.minitool.styletransfer

import android.graphics.Bitmap
import com.skyd.rays.base.IUiState

data class StyleTransferState(
    val styleTransferResultUiState: StyleTransferResultUiState,
) : IUiState

sealed class StyleTransferResultUiState {
    object Init : StyleTransferResultUiState()
    data class Success(val image: Bitmap) : StyleTransferResultUiState()
}
