package com.skyd.rays.ui.screen.stickerslist

import androidx.lifecycle.viewModelScope
import com.skyd.rays.base.mvi.AbstractMviViewModel
import com.skyd.rays.base.mvi.MviSingleEvent
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
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
            intentSharedFlow.filterNot { it is StickersListIntent.RefreshStickersList }
        )
            .shareWhileSubscribed()
            .toStickersListPartialStateChangeFlow()
            .debugLog("StickersListPartialStateChange")
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    private fun SharedFlow<StickersListIntent>.toStickersListPartialStateChangeFlow(): Flow<StickersListPartialStateChange> {
        return merge(
            merge(
                filterIsInstance<StickersListIntent.GetStickersList>(),
                filterIsInstance<StickersListIntent.RefreshStickersList>()
            ).flatMapConcat { intent ->
                val keyword = when (intent) {
                    is StickersListIntent.GetStickersList -> intent.query
                    is StickersListIntent.RefreshStickersList -> intent.query
                }
                searchRepo.requestStickerWithTagsList(keyword = keyword).map {
                    StickersListPartialStateChange.StickersList.Success(stickerWithTagsList = it)
                }.startWith(StickersListPartialStateChange.StickersList.Loading)
            },
        )
    }
}