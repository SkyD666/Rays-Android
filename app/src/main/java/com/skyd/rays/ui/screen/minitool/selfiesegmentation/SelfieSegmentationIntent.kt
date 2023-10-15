package com.skyd.rays.ui.screen.minitool.selfiesegmentation

import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import com.skyd.rays.base.IUiIntent

sealed class SelfieSegmentationIntent : IUiIntent {
    data class Segment(val foregroundUri: Uri) : SelfieSegmentationIntent()
    data class Export(
        val foregroundBitmap: Bitmap,
        val backgroundUri: Uri?,
        val backgroundSize: IntSize,
        val foregroundScale: Float,
        val foregroundOffset: Offset,
        val foregroundRotation: Float,
        val foregroundSize: IntSize,
        val borderSize: IntSize,
    ) : SelfieSegmentationIntent()
}