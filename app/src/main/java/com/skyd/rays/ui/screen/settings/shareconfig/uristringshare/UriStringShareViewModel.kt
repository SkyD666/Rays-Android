package com.skyd.rays.ui.screen.settings.shareconfig.uristringshare

import androidx.lifecycle.viewModelScope
import com.skyd.rays.base.mvi.AbstractMviViewModel
import com.skyd.rays.ext.startWith
import com.skyd.rays.model.bean.EmptyUriStringShareDataBean
import com.skyd.rays.model.bean.UriStringShareDataBean
import com.skyd.rays.model.respository.UriStringShareRepository
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

class UriStringShareViewModel(
    private var uriStringShareRepo: UriStringShareRepository
) : AbstractMviViewModel<UriStringShareIntent, UriStringShareState, UriStringShareEvent>() {

    override val viewState: StateFlow<UriStringShareState>

    init {
        val initialVS = UriStringShareState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<UriStringShareIntent.GetAllUriStringShare>().take(1),
            intentSharedFlow.filterNot { it is UriStringShareIntent.GetAllUriStringShare }
        )
            .shareWhileSubscribed()
            .toStickersListPartialStateChangeFlow()
            .debugLog("UriStringSharePartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    private fun Flow<UriStringSharePartialStateChange>.sendSingleEvent(): Flow<UriStringSharePartialStateChange> {
        return onEach { change ->
            val event = when (change) {
                is UriStringSharePartialStateChange.UpdateUriStringShare.Success -> {
                    UriStringShareEvent.AddPackageNameUiEvent.Success
                }

                is UriStringSharePartialStateChange.UpdateUriStringShare.Failed -> {
                    UriStringShareEvent.AddPackageNameUiEvent.Failed(change.msg)
                }

                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun SharedFlow<UriStringShareIntent>.toStickersListPartialStateChangeFlow(): Flow<UriStringSharePartialStateChange> {
        return merge(
            filterIsInstance<UriStringShareIntent.GetAllUriStringShare>().flatMapConcat {
                uriStringShareRepo.requestAllPackages().map {
                    UriStringSharePartialStateChange.GetAllUriStringShare.Success(it.reversed())
                }.startWith(UriStringSharePartialStateChange.GetAllUriStringShare.Loading)
            },

            filterIsInstance<UriStringShareIntent.UpdateUriStringShare>().flatMapConcat { intent ->
                uriStringShareRepo.requestUpdate(intent.bean).map { data ->
                    when (data) {
                        is EmptyUriStringShareDataBean -> {
                            UriStringSharePartialStateChange.UpdateUriStringShare.Failed(data.msg)
                        }

                        is UriStringShareDataBean -> {
                            UriStringSharePartialStateChange.UpdateUriStringShare.Success(data)
                        }
                    }
                }.startWith(UriStringSharePartialStateChange.GetAllUriStringShare.Loading)
            },

            filterIsInstance<UriStringShareIntent.DeleteUriStringShare>().flatMapConcat { intent ->
                uriStringShareRepo.requestDelete(intent.packageName).map {
                    UriStringSharePartialStateChange.Delete.Success(it.first, it.second)
                }.startWith(UriStringSharePartialStateChange.Delete.Loading)
            },
        )
    }
}