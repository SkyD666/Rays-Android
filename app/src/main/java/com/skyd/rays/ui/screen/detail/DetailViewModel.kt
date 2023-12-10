package com.skyd.rays.ui.screen.detail

import androidx.lifecycle.viewModelScope
import com.skyd.rays.appContext
import com.skyd.rays.base.mvi.AbstractMviViewModel
import com.skyd.rays.ext.startWith
import com.skyd.rays.model.preference.CurrentStickerUuidPreference
import com.skyd.rays.model.respository.DetailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
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
class DetailViewModel @Inject constructor(private var detailRepo: DetailRepository) :
    AbstractMviViewModel<DetailIntent, DetailState, DetailEvent>() {

    override val viewState: StateFlow<DetailState>

    init {
        val initialVS = DetailState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<DetailIntent.GetStickerDetailsAndAddClickCount>()
                .take(1),
            intentSharedFlow.filterNot { it is DetailIntent.GetStickerDetailsAndAddClickCount }
        )
            .shareWhileSubscribed()
            .toDetailPartialStateChangeFlow()
            .debugLog("DetailPartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    private fun Flow<DetailPartialStateChange>.sendSingleEvent(): Flow<DetailPartialStateChange> {
        return onEach { change ->
            val event = when (change) {
                DetailPartialStateChange.Export.Success -> DetailEvent.ExportResult.Success
                DetailPartialStateChange.Export.Failed -> DetailEvent.ExportResult.Failed
                DetailPartialStateChange.Delete.Success -> DetailEvent.DeleteResult.Success
                DetailPartialStateChange.Delete.Failed -> DetailEvent.DeleteResult.Failed
                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun SharedFlow<DetailIntent>.toDetailPartialStateChangeFlow(): Flow<DetailPartialStateChange> {
        return merge(
            merge(
                filterIsInstance<DetailIntent.GetStickerDetailsAndAddClickCount>().distinctUntilChanged(),
                filterIsInstance<DetailIntent.RefreshStickerDetails>(),
            ).flatMapConcat { intent ->
                val (stickerUuid, addClickCount) = when (intent) {
                    is DetailIntent.GetStickerDetailsAndAddClickCount -> intent.stickerUuid to 1
                    is DetailIntent.RefreshStickerDetails -> intent.stickerUuid to 0
                    else -> error("DetailIntent type error")
                }
                detailRepo.requestStickerWithTagsDetail(
                    stickerUuid = stickerUuid,
                    addClickCount = addClickCount
                ).map {
                    if (it == null) DetailPartialStateChange.DetailInfo.Empty
                    else {
                        CurrentStickerUuidPreference.put(
                            context = appContext,
                            scope = viewModelScope,
                            value = it.sticker.uuid,
                        )
                        DetailPartialStateChange.DetailInfo.Success(it)
                    }
                }.startWith(DetailPartialStateChange.DetailInfo.Loading)
            },

            filterIsInstance<DetailIntent.ExportStickers>().flatMapConcat { intent ->
                detailRepo.requestExportStickers(intent.stickerUuid).map {
                    if (it > 0) DetailPartialStateChange.Export.Success
                    else DetailPartialStateChange.Export.Failed
                }
            },

            filterIsInstance<DetailIntent.DeleteStickerWithTags>().flatMapConcat {
                detailRepo.requestDeleteStickerWithTagsDetail(it.stickerUuid).map { result ->
                    if (result > 0) DetailPartialStateChange.Delete.Success
                    else DetailPartialStateChange.Delete.Failed
                }
            },
        )
    }
}