package com.skyd.rays.ui.screen.settings.data.importexport.file.exportfiles

import com.skyd.rays.base.mvi.MviViewState
import com.skyd.rays.model.bean.ImportExportWaitingInfo

data class ExportFilesState(
    val exportProgressEvent: ExportProgressState,
    val loadingDialog: Boolean,
) : MviViewState {
    companion object {
        fun initial() = ExportFilesState(
            exportProgressEvent = ExportProgressState.None,
            loadingDialog = false,
        )
    }
}

sealed interface ExportProgressState {
    data object None : ExportProgressState
    data class Progress(val info: ImportExportWaitingInfo) : ExportProgressState
}