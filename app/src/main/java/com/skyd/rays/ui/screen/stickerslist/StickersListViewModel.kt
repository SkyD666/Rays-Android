package com.skyd.rays.ui.screen.stickerslist

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.skyd.rays.base.mvi.AbstractMviViewModel
import com.skyd.rays.base.mvi.MviSingleEvent
import com.skyd.rays.ext.catchMap
import com.skyd.rays.ext.startWith
import com.skyd.rays.model.respository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import javax.inject.Inject

@HiltViewModel
class StickersListViewModel @Inject constructor(
    private val searchRepo: SearchRepository,
) : AbstractMviViewModel<StickersListIntent, StickersListState, MviSingleEvent>() {

    override val viewState: StateFlow<StickersListState>

    init {
        val initialVS = StickersListState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<StickersListIntent.GetStickersList>().take(1),
            intentSharedFlow.filterNot { it is StickersListIntent.GetStickersList }
        )
            .shareWhileSubscribed()
            .toStickersListPartialStateChangeFlow()
            .debugLog("StickersListPartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    private fun Flow<StickersListPartialStateChange>.sendSingleEvent(): Flow<StickersListPartialStateChange> {
        return onEach { change ->
            val event = when (change) {
                is StickersListPartialStateChange.InverseSelectedStickers.Failed ->
                    StickersListEvent.InverseSelectedStickers.Failed(change.msg)

                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun SharedFlow<StickersListIntent>.toStickersListPartialStateChangeFlow(): Flow<StickersListPartialStateChange> {
        return merge(
            filterIsInstance<StickersListIntent.GetStickersList>().flatMapConcat { intent ->
                searchRepo.requestStickerWithTagsListWithAllSearchDomain(
                    keyword = intent.query
                ).map { it.flow.cachedIn(viewModelScope) }.map {
                    StickersListPartialStateChange.StickersList.Success(it)
                }.startWith(StickersListPartialStateChange.StickersList.Loading)
            },
            filterIsInstance<StickersListIntent.InverseSelectedStickers>().flatMapConcat { intent ->
                searchRepo.requestStickersNotIn(
                    keyword = intent.query,
                    selectedStickerUuids = intent.selectedStickers,
                ).map {
                    StickersListPartialStateChange.InverseSelectedStickers.Success(it)
                }.startWith(StickersListPartialStateChange.LoadingDialog).catchMap {
                    StickersListPartialStateChange.InverseSelectedStickers.Failed(it.message.orEmpty())
                }
            },
            filterIsInstance<StickersListIntent.AddSelectedStickers>().flatMapConcat { intent ->
                flowOf(StickersListPartialStateChange.AddSelectedStickers(intent.stickers))
                    .startWith(StickersListPartialStateChange.LoadingDialog)
            },
            filterIsInstance<StickersListIntent.RemoveSelectedStickers>().flatMapConcat { intent ->
                flowOf(StickersListPartialStateChange.RemoveSelectedStickers(intent.stickers))
                    .startWith(StickersListPartialStateChange.LoadingDialog)
            },
        )
    }
}