package com.skyd.rays.ui.screen.add

import androidx.lifecycle.viewModelScope
import com.skyd.rays.appContext
import com.skyd.rays.base.mvi.AbstractMviViewModel
import com.skyd.rays.ext.catchMap
import com.skyd.rays.ext.checkUriReadPermission
import com.skyd.rays.ext.endWith
import com.skyd.rays.ext.startWith
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.preference.CurrentStickerUuidPreference
import com.skyd.rays.model.respository.AddRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
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
class AddViewModel @Inject constructor(private var addRepository: AddRepository) :
    AbstractMviViewModel<AddIntent, AddState, AddEvent>() {


    override val viewState: StateFlow<AddState>

    init {
        val initialVS = AddState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<AddIntent.Init>().take(1),
            intentSharedFlow.filterNot { it is AddIntent.Init }
        )
            .shareWhileSubscribed()
            .toAddPartialStateChangeFlow()
            .debugLog("AddPartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }


    private fun Flow<AddPartialStateChange>.sendSingleEvent(): Flow<AddPartialStateChange> {
        return onEach { change ->
            val event = when (change) {
                is AddPartialStateChange.AddStickers.Success -> {
                    AddEvent.AddStickersResultEvent.Success(change.stickerUuid)
                }

                is AddPartialStateChange.AddStickers.Duplicate -> {
                    AddEvent.AddStickersResultEvent.Duplicate(change.stickerWithTags)
                }

                is AddPartialStateChange.AddStickers.Failed -> {
                    AddEvent.AddStickersResultEvent.Failed(change.msg)
                }

                is AddPartialStateChange.GetStickersWithTagsStateChanged -> {
                    AddEvent.GetStickersWithTagsStateChanged
                }

                is AddPartialStateChange.Init,
                is AddPartialStateChange.ProcessNext,
                is AddPartialStateChange.ReplaceWaitingListSingleSticker,
                is AddPartialStateChange.AddToWaitingList -> AddEvent.CurrentStickerChanged

                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun SharedFlow<AddIntent>.toAddPartialStateChangeFlow(): Flow<AddPartialStateChange> {
        return merge(
            filterIsInstance<AddIntent.Init>().map { intent ->
                intent.copy(initStickers = intent.initStickers.filter {
                    appContext.checkUriReadPermission(it.uri)
                })
            }.filter {
                it.initStickers.isNotEmpty()
            }.flatMapConcat { intent ->
                combine(
                    addRepository.requestGetStickerWithTags(
                        intent.initStickers.first().stickerUuid
                    ).catchMap { null },
                    addRepository.requestSuggestTags(
                        intent.initStickers.first().uri!!
                    ).catchMap { emptySet() }
                ) { stickerWithTags, suggestTags ->
                    AddPartialStateChange.Init(
                        stickerWithTags,
                        intent.initStickers,
                        suggestTags,
                    )
                }.startWith(AddPartialStateChange.LoadingDialog.Show)
            },
            filterIsInstance<AddIntent.ProcessNext>().map {
                AddPartialStateChange.ProcessNext
            },
            filterIsInstance<AddIntent.ReplaceWaitingListSingleSticker>().map { intent ->
                AddPartialStateChange.ReplaceWaitingListSingleSticker(
                    sticker = intent.sticker,
                    index = intent.index,
                )
            },
            filterIsInstance<AddIntent.AddTag>().map { intent ->
                AddPartialStateChange.AddTag(intent.text)
            },
            filterIsInstance<AddIntent.RemoveTag>().map { intent ->
                AddPartialStateChange.RemoveTag(intent.text)
            },
            filterIsInstance<AddIntent.AddToWaitingList>().map { intent ->
                AddPartialStateChange.AddToWaitingList(intent.stickers)
            },
            filterIsInstance<AddIntent.AddAddToAllTag>().map { intent ->
                AddPartialStateChange.AllToAllTag.Add(intent.text)
            },
            filterIsInstance<AddIntent.RemoveAddToAllTag>().map { intent ->
                AddPartialStateChange.AllToAllTag.Remove(intent.text)
            },
            filterIsInstance<AddIntent.RemoveSuggestTag>().map { intent ->
                AddPartialStateChange.GetSuggestTags.Remove(intent.text)
            },

            filterIsInstance<AddIntent.GetStickerWithTags>().flatMapConcat { intent ->
                addRepository.requestGetStickerWithTags(intent.stickerUuid).map {
                    if (it == null) AddPartialStateChange.GetStickersWithTags.Failed(intent.stickerUuid)
                    else AddPartialStateChange.GetStickersWithTags.Success(it)
                }.startWith(AddPartialStateChange.LoadingDialog.Show)
                    .endWith(AddPartialStateChange.GetStickersWithTagsStateChanged)
            },

            filterIsInstance<AddIntent.AddNewStickerWithTags>().flatMapConcat { intent ->
                addRepository.requestAddStickerWithTags(
                    intent.stickerWithTags,
                    intent.stickerUri
                ).map {
                    when (it) {
                        is String -> {
                            CurrentStickerUuidPreference.put(
                                context = appContext,
                                scope = viewModelScope,
                                value = it,
                            )
                            AddPartialStateChange.AddStickers.Success(it)
                        }

                        is StickerWithTags -> AddPartialStateChange.AddStickers.Duplicate(it)
                        else -> AddPartialStateChange.AddStickers.Failed("")
                    }
                }.startWith(AddPartialStateChange.LoadingDialog.Show)
                    .endWith(AddPartialStateChange.LoadingDialog.Close)
                    .catchMap { AddPartialStateChange.AddStickers.Failed(it.message.orEmpty()) }
            },

            filterIsInstance<AddIntent.GetSuggestTags>().flatMapConcat { intent ->
                addRepository.requestSuggestTags(intent.sticker).map { result ->
                    AddPartialStateChange.GetSuggestTags.Success(result)
                }.catchMap<AddPartialStateChange> {
                    AddPartialStateChange.GetSuggestTags.Failed(it.message.orEmpty())
                }
            },
        )
    }
}