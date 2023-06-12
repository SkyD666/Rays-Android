package com.skyd.rays.ui.screen.minitool.styletransfer

import android.net.Uri
import com.skyd.rays.base.IUiIntent

sealed class StyleTransferIntent : IUiIntent {
    data class Transfer(val style: Uri, val content: Uri) : StyleTransferIntent()
}