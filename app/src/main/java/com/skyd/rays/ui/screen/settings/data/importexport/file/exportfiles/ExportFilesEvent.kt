package com.skyd.rays.ui.screen.settings.data.importexport.file.exportfiles

import com.skyd.rays.base.mvi.MviSingleEvent
import com.skyd.rays.model.bean.ImportExportResultInfo

sealed interface ExportFilesEvent : MviSingleEvent {
    sealed class ExportResultEvent : ExportFilesEvent {
        data class Success(val info: ImportExportResultInfo) : ExportResultEvent()
        data class Error(val msg: String) : ExportResultEvent()
    }
}