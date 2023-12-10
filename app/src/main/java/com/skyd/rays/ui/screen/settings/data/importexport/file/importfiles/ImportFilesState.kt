package com.skyd.rays.ui.screen.settings.data.importexport.file.importfiles

import com.skyd.rays.base.mvi.MviViewState
import com.skyd.rays.model.bean.ImportExportWaitingInfo

data class ImportFilesState(
    val importProgressEvent: ImportProgressState,
    val loadingDialog: Boolean,
) : MviViewState {
    companion object {
        fun initial() = ImportFilesState(
            importProgressEvent = ImportProgressState.None,
            loadingDialog = false,
        )
    }
}

sealed interface ImportProgressState {
    data object None : ImportProgressState
    data class Progress(val info: ImportExportWaitingInfo) : ImportProgressState
}