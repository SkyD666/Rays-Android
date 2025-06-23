package com.skyd.rays.ui.screen.mergestickers

import androidx.lifecycle.viewModelScope
import com.skyd.rays.base.mvi.AbstractMviViewModel
import com.skyd.rays.ext.catchMap
import com.skyd.rays.ext.startWith
import com.skyd.rays.model.respository.MergeStickersRepository
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


class MergeStickersViewModel(
    private var mergeStickersRepo: MergeStickersRepository
) : AbstractMviViewModel<MergeStickersIntent, MergeStickersState, MergeStickersEvent>() {

    override val viewState: StateFlow<MergeStickersState>

    init {
        val initialVS = MergeStickersState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<MergeStickersIntent.Init>().take(1),
            intentSharedFlow.filterNot { it is MergeStickersIntent.Init }
        )
            .shareWhileSubscribed()
            .toMergeStickersPartialStateChangeFlow()
            .debugLog("MergeStickersPartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    private fun Flow<MergeStickersPartialStateChange>.sendSingleEvent(): Flow<MergeStickersPartialStateChange> {
        return onEach { change ->
            val event = when (change) {
                is MergeStickersPartialStateChange.Merge.Failed ->
                    MergeStickersEvent.MergeResult.Failed(change.msg)

                MergeStickersPartialStateChange.Merge.Success ->
                    MergeStickersEvent.MergeResult.Success

                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun SharedFlow<MergeStickersIntent>.toMergeStickersPartialStateChangeFlow(): Flow<MergeStickersPartialStateChange> {
        return merge(
            filterIsInstance<MergeStickersIntent.Init>().flatMapConcat { intent ->
                mergeStickersRepo.requestStickers(intent.stickers).map {
                    MergeStickersPartialStateChange.Init.Success(it)
                }.startWith(MergeStickersPartialStateChange.LoadingDialog).catchMap {
                    MergeStickersPartialStateChange.Init.Failed(it.message.orEmpty())
                }
            },

            filterIsInstance<MergeStickersIntent.Merge>().flatMapConcat { intent ->
                mergeStickersRepo.requestMerge(
                    intent.oldStickerUuid, intent.sticker, intent.deleteUuids
                ).map {
                    MergeStickersPartialStateChange.Merge.Success
                }.startWith(MergeStickersPartialStateChange.LoadingDialog).catchMap {
                    MergeStickersPartialStateChange.Merge.Failed(it.message.orEmpty())
                }
            },

            filterIsInstance<MergeStickersIntent.AddSelectedTag>().flatMapConcat { intent ->
                flowOf(MergeStickersPartialStateChange.AddSelectedTag(intent.tag))
            },
            filterIsInstance<MergeStickersIntent.RemoveSelectedTag>().flatMapConcat { intent ->
                flowOf(MergeStickersPartialStateChange.RemoveSelectedTag(intent.tag))
            },
            filterIsInstance<MergeStickersIntent.ReplaceAllSelectedTags>().flatMapConcat { intent ->
                flowOf(MergeStickersPartialStateChange.ReplaceAllSelectedTags(intent.tags))
            },
        )
    }
}