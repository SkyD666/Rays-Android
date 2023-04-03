package com.skyd.rays.ui.screen.settings.data.importexport.cloud.webdav

import com.skyd.rays.base.IUiIntent

sealed class WebDavIntent : IUiIntent {
    data class StartUpload(val website: String, val username: String, val password: String) :
        WebDavIntent()

    data class StartDownload(val website: String, val username: String, val password: String) :
        WebDavIntent()

    data class GetRemoteRecycleBin(
        val website: String,
        val username: String,
        val password: String
    ) : WebDavIntent()

    data class RestoreFromRemoteRecycleBin(
        val website: String,
        val username: String,
        val password: String,
        val uuid: String
    ) : WebDavIntent()

    data class DeleteFromRemoteRecycleBin(
        val website: String,
        val username: String,
        val password: String,
        val uuid: String
    ) : WebDavIntent()

    data class ClearRemoteRecycleBin(
        val website: String,
        val username: String,
        val password: String,
    ) : WebDavIntent()
}