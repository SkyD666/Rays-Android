package com.skyd.rays.ui.screen.minitool.selfiesegmentation

import android.net.Uri
import com.skyd.rays.base.IUiIntent

sealed class SelfieSegmentationIntent : IUiIntent {
    data class Segment(val foregroundUri: Uri) : SelfieSegmentationIntent()
}