package com.skyd.rays.ui.screen.search.multiselect

import androidx.lifecycle.viewModelScope
import com.skyd.rays.appContext
import com.skyd.rays.base.mvi.AbstractMviViewModel
import com.skyd.rays.ext.catchMap
import com.skyd.rays.ext.startWith
import com.skyd.rays.model.respository.SearchRepository
import com.skyd.rays.util.sendStickersByUuids
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class MultiSelectViewModel(
    private val searchRepo: SearchRepository
) : AbstractMviViewModel<MultiSelectIntent, MultiSelectState, MultiSelectEvent>() {

    override val viewState: StateFlow<MultiSelectState>

    init {
        val initialVS = MultiSelectState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<MultiSelectIntent.Init>().take(1),
            intentSharedFlow.filterNot { it is MultiSelectIntent.Init }
        )
            .shareWhileSubscribed()
            .toMultiSelectPartialStateChangeFlow()
            .debugLog("MultiSelectPartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    private fun Flow<MultiSelectPartialStateChange>.sendSingleEvent(): Flow<MultiSelectPartialStateChange> {
        return onEach { change ->
            val event = when (change) {
                is MultiSelectPartialStateChange.DeleteStickerWithTags.Failed ->
                    MultiSelectEvent.DeleteStickerWithTags.Failed(change.msg)

                is MultiSelectPartialStateChange.ExportStickers.Success ->
                    MultiSelectEvent.ExportStickers.Success(change.successCount)

                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun SharedFlow<MultiSelectIntent>.toMultiSelectPartialStateChangeFlow()
            : Flow<MultiSelectPartialStateChange> {
        return merge(
            filterIsInstance<MultiSelectIntent.Init>().flatMapConcat {
                flowOf(MultiSelectPartialStateChange.Init)
            },
            filterIsInstance<MultiSelectIntent.SendStickers>().flatMapConcat { intent ->
                flow {
                    emit(suspendCoroutine { cont ->
                        appContext.sendStickersByUuids(
                            uuids = intent.stickersUuids,
                            onSuccess = { cont.resume(Unit) }
                        )
                    })
                }.map {
                    MultiSelectPartialStateChange.SendStickers.Success
                }.startWith(MultiSelectPartialStateChange.LoadingDialog)
            },
            filterIsInstance<MultiSelectIntent.DeleteStickerWithTags>().flatMapConcat { intent ->
                searchRepo.requestDeleteStickerWithTagsDetail(intent.stickersUuids.toList()).map {
                    MultiSelectPartialStateChange.DeleteStickerWithTags.Success
                }.startWith(MultiSelectPartialStateChange.LoadingDialog)
                    .catchMap { MultiSelectPartialStateChange.DeleteStickerWithTags.Failed(it.message.toString()) }
            },
            filterIsInstance<MultiSelectIntent.ExportStickers>().flatMapConcat { intent ->
                searchRepo.requestExportStickers(stickerUuids = intent.stickerUuids.toList())
                    .map { MultiSelectPartialStateChange.ExportStickers.Success(it) }
                    .startWith(MultiSelectPartialStateChange.LoadingDialog)
            },
        )
    }
}