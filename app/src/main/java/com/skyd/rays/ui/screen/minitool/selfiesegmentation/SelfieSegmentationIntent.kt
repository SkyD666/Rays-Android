package com.skyd.rays.ui.screen.minitool.selfiesegmentation

import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import com.skyd.rays.base.IUiIntent

sealed class SelfieSegmentationIntent : IUiIntent {
    data class Segment(val foregroundUri: Uri) : SelfieSegmentationIntent()
    data class Export(
        val foregroundBitmap: Bitmap,
        val backgroundUri: Uri?,
        val foregroundRect: RectF,
    ) : SelfieSegmentationIntent()
}