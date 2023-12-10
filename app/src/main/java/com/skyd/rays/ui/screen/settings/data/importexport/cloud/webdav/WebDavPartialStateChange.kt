package com.skyd.rays.ui.screen.settings.data.importexport.cloud.webdav

import com.skyd.rays.model.bean.BackupInfo
import com.skyd.rays.model.bean.WebDavResultInfo
import com.skyd.rays.model.bean.WebDavWaitingInfo

internal sealed interface WebDavPartialStateChange {
    fun reduce(oldState: WebDavState): WebDavState

    data object Init : WebDavPartialStateChange {
        override fun reduce(oldState: WebDavState) = oldState.copy(loadingDialog = false)
    }

    data object LoadingDialog : WebDavPartialStateChange {
        override fun reduce(oldState: WebDavState) = oldState.copy(loadingDialog = true)
    }

    sealed interface DownloadProgress : WebDavPartialStateChange {
        data class Error(val msg: String) : DownloadProgress {
            override fun reduce(oldState: WebDavState): WebDavState = oldState.copy(
                downloadProgressState = DownloadProgressState.None,
                loadingDialog = false,
            )
        }

        data class Finish(val info: WebDavResultInfo) : DownloadProgress {
            override fun reduce(oldState: WebDavState): WebDavState = oldState.copy(
                downloadProgressState = DownloadProgressState.None,
                loadingDialog = false,
            )
        }

        data class Progressing(val info: WebDavWaitingInfo) : DownloadProgress {
            override fun reduce(oldState: WebDavState): WebDavState = oldState.copy(
                downloadProgressState = DownloadProgressState.Progress(info),
                loadingDialog = true,
            )
        }
    }

    sealed interface UploadProgress : WebDavPartialStateChange {
        data class Error(val msg: String) : UploadProgress {
            override fun reduce(oldState: WebDavState): WebDavState = oldState.copy(
                uploadProgressState = UploadProgressState.None,
                loadingDialog = false,
            )
        }

        data class Finish(val info: WebDavResultInfo) : UploadProgress {
            override fun reduce(oldState: WebDavState): WebDavState = oldState.copy(
                uploadProgressState = UploadProgressState.None,
                loadingDialog = false,
            )
        }

        data class Progressing(val info: WebDavWaitingInfo) : UploadProgress {
            override fun reduce(oldState: WebDavState): WebDavState = oldState.copy(
                uploadProgressState = UploadProgressState.Progress(info),
                loadingDialog = true,
            )
        }
    }

    sealed interface GetRemoteRecycleBinResult : WebDavPartialStateChange {
        data class Error(val msg: String) : GetRemoteRecycleBinResult {
            override fun reduce(oldState: WebDavState): WebDavState = oldState.copy(
                getRemoteRecycleBinResultState = GetRemoteRecycleBinResultState.Init,
                loadingDialog = false,
            )
        }

        data class Success(val result: List<BackupInfo>) : GetRemoteRecycleBinResult {
            override fun reduce(oldState: WebDavState): WebDavState = oldState.copy(
                getRemoteRecycleBinResultState = GetRemoteRecycleBinResultState.Success(result),
                loadingDialog = false,
            )
        }
    }
}
