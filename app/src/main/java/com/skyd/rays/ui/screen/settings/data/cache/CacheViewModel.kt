package com.skyd.rays.ui.screen.settings.data.cache

import androidx.lifecycle.viewModelScope
import com.skyd.rays.base.mvi.AbstractMviViewModel
import com.skyd.rays.ext.startWith
import com.skyd.rays.model.respository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject

@HiltViewModel
class CacheViewModel @Inject constructor(private var dataRepo: DataRepository) :
    AbstractMviViewModel<CacheIntent, CacheState, CacheEvent>() {

    override val viewState: StateFlow<CacheState>

    init {
        val initialVS = CacheState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<CacheIntent.Init>().take(1),
            intentSharedFlow.filterNot { it is CacheIntent.Init }
        )
            .shareWhileSubscribed()
            .toPartialStateChangeFlow()
            .debugLog("CachePartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }


    private fun Flow<CachePartialStateChange>.sendSingleEvent(): Flow<CachePartialStateChange> {
        return onEach { change ->
            val event = when (change) {
                is CachePartialStateChange.DeleteDocumentsProviderThumbnails.Success ->
                    CacheEvent.DeleteDocumentsProviderThumbnailsResultEvent.Success(change.time)

                is CachePartialStateChange.DeleteAllMimetypes.Success ->
                    CacheEvent.DeleteAllMimetypesResultEvent.Success(change.time)

                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun SharedFlow<CacheIntent>.toPartialStateChangeFlow(): Flow<CachePartialStateChange> {
        return merge(
            filterIsInstance<CacheIntent.Init>().map { CachePartialStateChange.Init },

            filterIsInstance<CacheIntent.DeleteDocumentsProviderThumbnails>().flatMapConcat {
                dataRepo.requestDeleteDocumentsProviderThumbnails()
                    .map { CachePartialStateChange.DeleteDocumentsProviderThumbnails.Success(it) }
                    .startWith(CachePartialStateChange.LoadingDialog)
            },

            filterIsInstance<CacheIntent.DeleteAllMimetypes>().flatMapConcat {
                dataRepo.requestDeleteAllMimetypes()
                    .map { CachePartialStateChange.DeleteAllMimetypes.Success(it) }
                    .startWith(CachePartialStateChange.LoadingDialog)
            },
        )
    }
}