package com.skyd.rays.ui.screen.minitool.selfiesegmentation

import android.graphics.Bitmap
import com.skyd.rays.base.mvi.MviSingleEvent

sealed interface SelfieSegmentationEvent : MviSingleEvent {
    sealed interface ExportUiEvent : SelfieSegmentationEvent {
        class Success(val bitmap: Bitmap) : ExportUiEvent
    }

    sealed interface SegmentUiEvent : SelfieSegmentationEvent {
        class Failed(val msg: String) : ExportUiEvent
    }
}
