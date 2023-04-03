package com.skyd.rays.ui.screen.settings.data.importexport.cloud.webdav

import com.skyd.rays.base.IUiEvent
import com.skyd.rays.model.bean.WebDavInfo

data class WebDavEvent(
    val uploadResultUiEvent: UploadResultUiEvent? = null,
    val downloadResultUiEvent: DownloadResultUiEvent? = null,
) : IUiEvent

sealed class UploadResultUiEvent {
    data class Success(val result: WebDavInfo) : UploadResultUiEvent()
}

sealed class DownloadResultUiEvent {
    data class Success(val result: WebDavInfo) : DownloadResultUiEvent()
}