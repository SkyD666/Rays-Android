package com.skyd.rays.ui.screen.settings.data.importexport.cloud.webdav

import com.skyd.rays.base.BaseViewModel
import com.skyd.rays.base.IUIChange
import com.skyd.rays.config.refreshStickerData
import com.skyd.rays.model.respository.WebDavRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class WebDavViewModel @Inject constructor(private var webDavRepo: WebDavRepository) :
    BaseViewModel<WebDavState, WebDavEvent, WebDavIntent>() {
    override fun initUiState(): WebDavState {
        return WebDavState(
            getRemoteRecycleBinResultUiState = GetRemoteRecycleBinResultUiState.Init,
        )
    }

    override fun IUIChange.checkStateOrEvent() = this as? WebDavState to this as? WebDavEvent

    override fun Flow<WebDavIntent>.handleIntent(): Flow<IUIChange> = merge(
        doIsInstance<WebDavIntent.StartDownload> { intent ->
            webDavRepo.requestDownload(
                website = intent.website, username = intent.username, password = intent.password
            )
                .mapToUIChange { data ->
                    WebDavEvent(downloadResultUiEvent = DownloadResultUiEvent.Success(data))
                }
                .defaultFinally()
                .onCompletion {
                    refreshStickerData.tryEmit(Unit)
                }
        },

        doIsInstance<WebDavIntent.StartUpload> { intent ->
            webDavRepo.requestUpload(
                website = intent.website, username = intent.username, password = intent.password
            )
                .mapToUIChange { data ->
                    WebDavEvent(uploadResultUiEvent = UploadResultUiEvent.Success(data))
                }
                .defaultFinally()
        },

        doIsInstance<WebDavIntent.GetRemoteRecycleBin> { intent ->
            webDavRepo.requestRemoteRecycleBin(
                website = intent.website, username = intent.username, password = intent.password
            )
                .mapToUIChange { data ->
                    copy(
                        getRemoteRecycleBinResultUiState =
                        GetRemoteRecycleBinResultUiState.Success(data)
                    )
                }
                .defaultFinally()
        },

        doIsInstance<WebDavIntent.RestoreFromRemoteRecycleBin> { intent ->
            webDavRepo.requestRestoreFromRemoteRecycleBin(
                website = intent.website,
                username = intent.username,
                password = intent.password,
                uuid = intent.uuid
            ).map {
                webDavRepo.requestRemoteRecycleBin(
                    website = intent.website, username = intent.username, password = intent.password
                )
            }.flattenConcat().mapToUIChange { data ->
                copy(
                    getRemoteRecycleBinResultUiState =
                    GetRemoteRecycleBinResultUiState.Success(data)
                )
            }.defaultFinally().onCompletion {
                refreshStickerData.tryEmit(Unit)
            }
        },

        doIsInstance<WebDavIntent.DeleteFromRemoteRecycleBin> { intent ->
            webDavRepo.requestDeleteFromRemoteRecycleBin(
                website = intent.website,
                username = intent.username,
                password = intent.password,
                uuid = intent.uuid
            ).map {
                webDavRepo.requestRemoteRecycleBin(
                    website = intent.website, username = intent.username, password = intent.password
                )
            }.flattenConcat().mapToUIChange { data ->
                copy(
                    getRemoteRecycleBinResultUiState =
                    GetRemoteRecycleBinResultUiState.Success(data)
                )
            }.defaultFinally()
        },

        doIsInstance<WebDavIntent.ClearRemoteRecycleBin> { intent ->
            webDavRepo.requestClearRemoteRecycleBin(
                website = intent.website, username = intent.username, password = intent.password,
            )
            webDavRepo.requestRemoteRecycleBin(
                website = intent.website, username = intent.username, password = intent.password
            )
                .mapToUIChange { data ->
                    copy(
                        getRemoteRecycleBinResultUiState =
                        GetRemoteRecycleBinResultUiState.Success(data)
                    )
                }
                .defaultFinally()
        },
    )
}
