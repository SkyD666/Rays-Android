package com.skyd.rays.ui.screen.settings.data.importexport.cloud.webdav

import com.skyd.rays.base.mvi.MviIntent

sealed interface WebDavIntent : MviIntent {
    data object Init : WebDavIntent
    data class StartUpload(val data: WebDavAccountData) : WebDavIntent

    data class StartDownload(val data: WebDavAccountData) : WebDavIntent

    data class GetRemoteRecycleBin(val data: WebDavAccountData) : WebDavIntent

    data class RestoreFromRemoteRecycleBin(
        val data: WebDavAccountData,
        val uuid: String
    ) : WebDavIntent

    data class DeleteFromRemoteRecycleBin(
        val data: WebDavAccountData,
        val uuid: String
    ) : WebDavIntent

    data class ClearRemoteRecycleBin(val data: WebDavAccountData) : WebDavIntent
}

data class WebDavAccountData(
    val website: String,
    val username: String,
    val password: String,
)