package com.skyd.rays.ui.screen.settings.data.importexport.file.exportfiles

import android.net.Uri
import com.skyd.rays.base.mvi.MviIntent

sealed interface ExportFilesIntent : MviIntent {
    data object Init : ExportFilesIntent
    data class Export(
        val dirUri: Uri,
        val excludeClickCount: Boolean = false,
        val excludeShareCount: Boolean = false,
        val excludeCreateTime: Boolean = false,
        val excludeModifyTime: Boolean = false,
        val exportStickers: List<String>? = null,
    ) : ExportFilesIntent
}