package com.skyd.rays.ui.screen.settings.data.importexport.file.exportfiles

import android.net.Uri
import com.skyd.rays.base.IUiIntent

sealed class ExportFilesIntent : IUiIntent {
    data class Export(val dirUri: Uri) : ExportFilesIntent()
}