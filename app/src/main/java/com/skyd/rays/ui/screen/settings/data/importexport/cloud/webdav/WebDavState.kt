package com.skyd.rays.ui.screen.settings.data.importexport.cloud.webdav

import com.skyd.rays.base.mvi.MviViewState
import com.skyd.rays.model.bean.BackupInfo
import com.skyd.rays.model.bean.WebDavWaitingInfo

data class WebDavState(
    val downloadProgressState: DownloadProgressState,
    val uploadProgressState: UploadProgressState,
    val getRemoteRecycleBinResultState: GetRemoteRecycleBinResultState,
    val loadingDialog: Boolean,
) : MviViewState {
    companion object {
        fun initial() = WebDavState(
            downloadProgressState = DownloadProgressState.None,
            uploadProgressState = UploadProgressState.None,
            getRemoteRecycleBinResultState = GetRemoteRecycleBinResultState.Init,
            loadingDialog = false,
        )
    }
}

sealed interface DownloadProgressState {
    data object None : DownloadProgressState
    data class Progress(val info: WebDavWaitingInfo) : DownloadProgressState
}

sealed interface UploadProgressState {
    data object None : UploadProgressState
    data class Progress(val info: WebDavWaitingInfo) : UploadProgressState
}

sealed class GetRemoteRecycleBinResultState {
    data object Init : GetRemoteRecycleBinResultState()
    data class Success(val result: List<BackupInfo>) : GetRemoteRecycleBinResultState()
}