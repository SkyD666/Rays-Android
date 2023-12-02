package com.skyd.rays.ui.screen.minitool.styletransfer

import android.net.Uri
import com.skyd.rays.base.mvi.MviIntent

sealed interface StyleTransferIntent : MviIntent {
    data object Initial : StyleTransferIntent
    data class Transfer(val style: Uri, val content: Uri) : StyleTransferIntent
}