package com.skyd.rays.ui.screen.settings.data.importexport.cloud.webdav

import androidx.lifecycle.viewModelScope
import com.skyd.rays.base.mvi.AbstractMviViewModel
import com.skyd.rays.ext.catchMap
import com.skyd.rays.ext.startWith
import com.skyd.rays.model.bean.WebDavResultInfo
import com.skyd.rays.model.bean.WebDavWaitingInfo
import com.skyd.rays.model.respository.WebDavRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take

class WebDavViewModel(private var webDavRepo: WebDavRepository) :
    AbstractMviViewModel<WebDavIntent, WebDavState, WebDavEvent>() {

    override val viewState: StateFlow<WebDavState>

    init {
        val initialVS = WebDavState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<WebDavIntent.Init>().take(1),
            intentSharedFlow.filterNot { it is WebDavIntent.Init }
        )
            .shareWhileSubscribed()
            .toPartialStateChangeFlow()
            .debugLog("WebDavPartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }


    private fun Flow<WebDavPartialStateChange>.sendSingleEvent(): Flow<WebDavPartialStateChange> {
        return onEach { change ->
            val event = when (change) {
                is WebDavPartialStateChange.DownloadProgress.Finish ->
                    WebDavEvent.DownloadResultEvent.Success(change.info)

                is WebDavPartialStateChange.DownloadProgress.Error ->
                    WebDavEvent.DownloadResultEvent.Error(change.msg)

                is WebDavPartialStateChange.UploadProgress.Finish ->
                    WebDavEvent.UploadResultEvent.Success(change.info)

                is WebDavPartialStateChange.UploadProgress.Error ->
                    WebDavEvent.UploadResultEvent.Error(change.msg)

                is WebDavPartialStateChange.GetRemoteRecycleBinResult.Error ->
                    WebDavEvent.GetRemoteRecycleBinResultEvent.Error(change.msg)

                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun SharedFlow<WebDavIntent>.toPartialStateChangeFlow(): Flow<WebDavPartialStateChange> {
        return merge(
            filterIsInstance<WebDavIntent.Init>().map { WebDavPartialStateChange.Init },

            filterIsInstance<WebDavIntent.StartDownload>().flatMapConcat { intent ->
                webDavRepo.requestDownload(
                    website = intent.data.website,
                    username = intent.data.username,
                    password = intent.data.password
                ).map {
                    when (it) {
                        is WebDavResultInfo -> {
                            WebDavPartialStateChange.DownloadProgress.Finish(it)
                        }

                        is WebDavWaitingInfo -> {
                            WebDavPartialStateChange.DownloadProgress.Progressing(it)
                        }
                    }
                }.startWith(WebDavPartialStateChange.LoadingDialog).catchMap {
                    WebDavPartialStateChange.DownloadProgress.Error(it.message.orEmpty())
                }
            },

            filterIsInstance<WebDavIntent.StartUpload>().flatMapConcat { intent ->
                webDavRepo.requestUpload(
                    website = intent.data.website,
                    username = intent.data.username,
                    password = intent.data.password
                ).map {
                    when (it) {
                        is WebDavResultInfo -> {
                            WebDavPartialStateChange.UploadProgress.Finish(it)
                        }

                        is WebDavWaitingInfo -> {
                            WebDavPartialStateChange.UploadProgress.Progressing(it)
                        }
                    }
                }.startWith(WebDavPartialStateChange.LoadingDialog).catchMap {
                    WebDavPartialStateChange.UploadProgress.Error(it.message.orEmpty())
                }
            },

            filterIsInstance<WebDavIntent.GetRemoteRecycleBin>().flatMapConcat { intent ->
                webDavRepo.requestRemoteRecycleBin(
                    website = intent.data.website,
                    username = intent.data.username,
                    password = intent.data.password
                ).map {
                    WebDavPartialStateChange.GetRemoteRecycleBinResult.Success(it)
                }.startWith(WebDavPartialStateChange.LoadingDialog).catchMap {
                    WebDavPartialStateChange.GetRemoteRecycleBinResult.Error(it.message.orEmpty())
                }
            },

            merge(
                filterIsInstance<WebDavIntent.RestoreFromRemoteRecycleBin>().flatMapConcat { intent ->
                    webDavRepo.requestRestoreFromRemoteRecycleBin(
                        website = intent.data.website,
                        username = intent.data.username,
                        password = intent.data.password,
                        uuid = intent.uuid,
                    ).startWith(WebDavPartialStateChange.LoadingDialog).map { intent.data }
                },
                filterIsInstance<WebDavIntent.DeleteFromRemoteRecycleBin>().flatMapConcat { intent ->
                    webDavRepo.requestDeleteFromRemoteRecycleBin(
                        website = intent.data.website,
                        username = intent.data.username,
                        password = intent.data.password,
                        uuid = intent.uuid
                    ).startWith(WebDavPartialStateChange.LoadingDialog).map { intent.data }
                },
                filterIsInstance<WebDavIntent.ClearRemoteRecycleBin>().flatMapConcat { intent ->
                    webDavRepo.requestClearRemoteRecycleBin(
                        website = intent.data.website,
                        username = intent.data.username,
                        password = intent.data.password,
                    ).startWith(WebDavPartialStateChange.LoadingDialog).map { intent.data }
                },
            ).flatMapConcat {
                webDavRepo.requestRemoteRecycleBin(
                    website = it.website,
                    username = it.username,
                    password = it.password,
                ).map { list ->
                    WebDavPartialStateChange.GetRemoteRecycleBinResult.Success(list)
                }.startWith(WebDavPartialStateChange.LoadingDialog).catchMap { e ->
                    WebDavPartialStateChange.GetRemoteRecycleBinResult.Error(e.message.orEmpty())
                }
            }.catchMap { e ->
                WebDavPartialStateChange.GetRemoteRecycleBinResult.Error(e.message.orEmpty())
            }
        )
    }
}
