package com.skyd.rays.ui.screen.settings.data.importexport.file.importfiles

import com.skyd.rays.base.IUiEvent
import com.skyd.rays.model.bean.ImportExportInfo

class ImportFilesEvent(
    val importResultUiEvent: ImportResultUiEvent? = null,
) : IUiEvent

sealed class ImportResultUiEvent {
    class Success(val info: ImportExportInfo) : ImportResultUiEvent()
}