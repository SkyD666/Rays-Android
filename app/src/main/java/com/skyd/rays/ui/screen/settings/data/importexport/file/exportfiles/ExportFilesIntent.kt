package com.skyd.rays.ui.screen.settings.data.importexport.file.exportfiles

import android.net.Uri
import com.skyd.rays.base.mvi.MviIntent

sealed interface ExportFilesIntent : MviIntent {
    data object Init : ExportFilesIntent
    data class Export(val dirUri: Uri) : ExportFilesIntent
}