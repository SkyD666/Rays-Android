package com.skyd.rays.ui.screen.settings.data.importexport.cloud.webdav

import com.skyd.rays.base.mvi.MviSingleEvent
import com.skyd.rays.model.bean.WebDavResultInfo

sealed interface WebDavEvent : MviSingleEvent {
    sealed interface GetRemoteRecycleBinResultEvent : WebDavEvent {
        data class Error(val msg: String) : GetRemoteRecycleBinResultEvent
    }

    sealed interface UploadResultEvent : WebDavEvent {
        data class Success(val result: WebDavResultInfo) : UploadResultEvent
        data class Error(val msg: String) : UploadResultEvent
    }

    sealed interface DownloadResultEvent : WebDavEvent {
        data class Success(val result: WebDavResultInfo) : DownloadResultEvent
        data class Error(val msg: String) : DownloadResultEvent
    }
}
