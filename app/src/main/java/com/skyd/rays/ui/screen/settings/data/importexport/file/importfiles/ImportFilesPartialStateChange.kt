package com.skyd.rays.ui.screen.settings.data.importexport.file.importfiles

import com.skyd.rays.model.bean.ImportExportResultInfo
import com.skyd.rays.model.bean.ImportExportWaitingInfo

internal sealed interface ImportFilesPartialStateChange {
    fun reduce(oldState: ImportFilesState): ImportFilesState

    data object Init : ImportFilesPartialStateChange {
        override fun reduce(oldState: ImportFilesState) = oldState.copy(loadingDialog = false)
    }

    data object LoadingDialog : ImportFilesPartialStateChange {
        override fun reduce(oldState: ImportFilesState) = oldState.copy(loadingDialog = true)
    }

    sealed interface ImportFilesProgress : ImportFilesPartialStateChange {
        data class Error(val msg: String) : ImportFilesProgress {
            override fun reduce(oldState: ImportFilesState): ImportFilesState = oldState.copy(
                importProgressEvent = ImportProgressState.None,
                loadingDialog = false,
            )
        }

        data class Finish(val info: ImportExportResultInfo) : ImportFilesProgress {
            override fun reduce(oldState: ImportFilesState): ImportFilesState = oldState.copy(
                importProgressEvent = ImportProgressState.None,
                loadingDialog = false,
            )
        }

        data class Progressing(val info: ImportExportWaitingInfo) : ImportFilesProgress {
            override fun reduce(oldState: ImportFilesState): ImportFilesState = oldState.copy(
                importProgressEvent = ImportProgressState.Progress(info),
                loadingDialog = true,
            )
        }
    }
}
