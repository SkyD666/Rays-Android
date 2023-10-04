package com.skyd.rays.ui.screen.settings.data.importexport.file.exportfiles

import com.skyd.rays.base.IUiEvent
import com.skyd.rays.model.bean.ImportExportInfo

class ExportFilesEvent(
    val exportResultUiEvent: ExportResultUiEvent? = null,
) : IUiEvent

sealed class ExportResultUiEvent {
    class Success(val info: ImportExportInfo) : ExportResultUiEvent()
}