package com.skyd.rays.ui.screen.stickerslist

import androidx.lifecycle.viewModelScope
import com.skyd.rays.base.mvi.AbstractMviViewModel
import com.skyd.rays.ext.startWith
import com.skyd.rays.model.respository.HomeRepository
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
class StickersListViewModel @Inject constructor(
    private val homeRepo: HomeRepository,
) : AbstractMviViewModel<StickersListIntent, StickersListState, StickersListEvent>() {

    override val viewState: StateFlow<StickersListState>

    init {
        val initialVS = StickersListState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<StickersListIntent.Initial>().take(1),
            intentSharedFlow.filterNot { it is StickersListIntent.Initial }
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
                StickersListPartialStateChange.AddClickCount.Success -> {
                    StickersListEvent.AddClickCount.Success
                }

                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun SharedFlow<StickersListIntent>.toStickersListPartialStateChangeFlow(): Flow<StickersListPartialStateChange> {
        return merge(
            merge(
                filterIsInstance<StickersListIntent.Initial>(),
                filterIsInstance<StickersListIntent.RefreshStickersList>()
            ).flatMapConcat { intent ->
                val keyword = if (intent is StickersListIntent.RefreshStickersList) intent.query
                else ""
                homeRepo.requestStickerWithTagsList(keyword = keyword).map {
                    StickersListPartialStateChange.StickersList.Success(stickerWithTagsList = it)
                }.startWith(StickersListPartialStateChange.StickersList.Loading)
            },

            filterIsInstance<StickersListIntent.AddClickCount>()
                .flatMapConcat { homeRepo.requestAddClickCount(stickerUuid = it.stickerUuid) }
                .map { StickersListPartialStateChange.AddClickCount.Success },
        )
    }
}