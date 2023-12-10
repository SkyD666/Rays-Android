package com.skyd.rays.ui.screen.settings.data.importexport.file.importfiles

import com.skyd.rays.base.mvi.MviSingleEvent
import com.skyd.rays.model.bean.ImportExportResultInfo

sealed interface ImportFilesEvent : MviSingleEvent {
    sealed interface ImportResultEvent : ImportFilesEvent {
        class Success(val info: ImportExportResultInfo) : ImportResultEvent
        data class Error(val msg: String) : ImportResultEvent
    }
}