package com.skyd.rays.ui.screen.settings.data.importexport.file.exportfiles

import com.skyd.rays.model.bean.ImportExportResultInfo
import com.skyd.rays.model.bean.ImportExportWaitingInfo

internal sealed interface ExportFilesPartialStateChange {
    fun reduce(oldState: ExportFilesState): ExportFilesState

    data object Init : ExportFilesPartialStateChange {
        override fun reduce(oldState: ExportFilesState) = oldState.copy(loadingDialog = false)
    }

    data object LoadingDialog : ExportFilesPartialStateChange {
        override fun reduce(oldState: ExportFilesState) = oldState.copy(loadingDialog = true)
    }

    sealed interface ExportFilesProgress : ExportFilesPartialStateChange {
        data class Error(val msg: String) : ExportFilesProgress {
            override fun reduce(oldState: ExportFilesState): ExportFilesState = oldState.copy(
                exportProgressEvent = ExportProgressState.None,
                loadingDialog = false,
            )
        }

        data class Finish(val info: ImportExportResultInfo) : ExportFilesProgress {
            override fun reduce(oldState: ExportFilesState): ExportFilesState = oldState.copy(
                exportProgressEvent = ExportProgressState.None,
                loadingDialog = false,
            )
        }

        data class Progressing(val info: ImportExportWaitingInfo) : ExportFilesProgress {
            override fun reduce(oldState: ExportFilesState): ExportFilesState = oldState.copy(
                exportProgressEvent = ExportProgressState.Progress(info),
                loadingDialog = true,
            )
        }
    }
}
