package com.skyd.rays.ui.screen.minitool.selfiesegmentation

import android.graphics.Bitmap
import com.skyd.rays.base.IUiEvent

class SelfieSegmentationEvent(
    val exportUiEvent: ExportUiEvent? = null,
) : IUiEvent

sealed class ExportUiEvent {
    class Success(val bitmap: Bitmap) : ExportUiEvent()
    data object Init : ExportUiEvent()
}
