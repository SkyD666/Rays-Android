package com.skyd.rays.ui.screen.settings.data.importexport.cloud.webdav

import com.skyd.rays.base.IUiState
import com.skyd.rays.model.bean.BackupInfo

data class WebDavState(
    val getRemoteRecycleBinResultUiState: GetRemoteRecycleBinResultUiState,
) : IUiState

sealed class GetRemoteRecycleBinResultUiState {
    object Init : GetRemoteRecycleBinResultUiState()
    data class Success(val result: List<BackupInfo>) : GetRemoteRecycleBinResultUiState()
}