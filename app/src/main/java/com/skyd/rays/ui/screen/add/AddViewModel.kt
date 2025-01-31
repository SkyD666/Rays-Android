package com.skyd.rays.ui.screen.add

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.skyd.rays.appContext
import com.skyd.rays.base.mvi.AbstractMviViewModel
import com.skyd.rays.ext.catchMap
import com.skyd.rays.ext.checkUriReadPermission
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.endWith
import com.skyd.rays.ext.getOrDefault
import com.skyd.rays.ext.startWith
import com.skyd.rays.model.bean.StickerWithTags
import com.skyd.rays.model.bean.UriWithStickerUuidBean
import com.skyd.rays.model.preference.CurrentStickerUuidPreference
import com.skyd.rays.model.preference.search.imagesearch.AddScreenImageSearchPreference
import com.skyd.rays.model.preference.search.imagesearch.ImageSearchMaxResultCountPreference
import com.skyd.rays.model.respository.AddRepository
import com.skyd.rays.model.respository.ImageSearchRepository
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import javax.inject.Inject

@HiltViewModel
class AddViewModel @Inject constructor(
    private var addRepository: AddRepository,
    private var imageSearchRepository: ImageSearchRepository,
) : AbstractMviViewModel<AddIntent, AddState, AddEvent>() {

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
                    Log.w("AddViewModel", "Fuck Flow<AddPartialStateChange>.sendSingleEvent()")
                    AddEvent.AddStickersResultEvent.Success(change.stickerUuid)
                }

                is AddPartialStateChange.AddStickers.Duplicate -> {
                    AddEvent.AddStickersResultEvent.Duplicate(change.stickerWithTags)
                }

                is AddPartialStateChange.AddStickers.Failed -> {
                    AddEvent.AddStickersResultEvent.Failed(change.msg)
                }

                is AddPartialStateChange.RemoveWaitingListSingleSticker.Failed -> {
                    AddEvent.RemoveWaitingListSingleStickerFailedEvent(change.msg)
                }

                is AddPartialStateChange.Init.Failed -> {
                    AddEvent.InitFailed(change.msg)
                }

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
                val firstSticker = intent.initStickers.first()
                combine(
                    addRepository.requestGetStickerWithTags(firstSticker.stickerUuid),
                    addRepository.requestSuggestTags(firstSticker.uri!!)
                        .catchMap { emptySet() },
                    if (appContext.dataStore.getOrDefault(AddScreenImageSearchPreference)) {
                        imageSearchRepository.imageSearch(
                            base = firstSticker.uri,
                            baseUuid = firstSticker.stickerUuid,
                            maxResultCount = appContext.dataStore.getOrDefault(
                                ImageSearchMaxResultCountPreference
                            )
                        )
                    } else {
                        flowOf(emptyList())
                    },
                ) { stickerWithTags, suggestTags, similarStickers ->
                    AddPartialStateChange.Init.Success(
                        stickerWithTags,
                        intent.initStickers,
                        suggestTags,
                        similarStickers,
                    )
                }.startWith(AddPartialStateChange.LoadingDialog.Show)
                    .catchMap { AddPartialStateChange.Init.Failed(it.message.orEmpty()) }
            },
            filterIsInstance<AddIntent.UpdateTitleText>().map { intent ->
                AddPartialStateChange.UpdateTitleText(intent.title)
            },
            filterIsInstance<AddIntent.UpdateCurrentTagText>().map { intent ->
                AddPartialStateChange.UpdateCurrentTagText(intent.currentTag)
            },
            filterIsInstance<AddIntent.ReplaceWaitingListSingleSticker>().flatMapConcat { intent ->
                val currentStickerChanged = intent.index == 0
                if (currentStickerChanged) {
                    currentStickerChange(intent.sticker)
                } else {
                    flowOf(Triple(null, null, null))
                }.map { (stickersWithTags, suggestTags, similarStickers) ->
                    AddPartialStateChange.ReplaceWaitingListSingleSticker(
                        sticker = intent.sticker,
                        index = intent.index,
                        getStickersWithTagsState = {
                            if (currentStickerChanged) {
                                GetStickersWithTagsState.fromStickersWithTags(stickersWithTags)
                            } else it.getStickersWithTagsState
                        },
                        suggestTags = if (currentStickerChanged) {
                            suggestTags.orEmpty().toList()
                        } else null,
                        similarStickers = if (currentStickerChanged) {
                            similarStickers.orEmpty()
                        } else null,
                        currentStickerChanged = currentStickerChanged,
                    )
                }.startWith(AddPartialStateChange.LoadingDialog.Show).catchMap {
                    AddPartialStateChange.ReplaceWaitingListSingleSticker(
                        sticker = intent.sticker,
                        index = intent.index,
                    )
                }.endWith(AddPartialStateChange.LoadingDialog.Close)
            },
            filterIsInstance<AddIntent.RemoveWaitingListSingleSticker>().flatMapConcat { intent ->
                val currentStickerChanged = intent.index == 0
                val willRemove = intent.onSticker(intent.index)!!
                if (currentStickerChanged) {
                    currentStickerChange(intent.onSticker(1))
                } else {
                    flowOf(Triple(null, null, null))
                }.map { (stickersWithTags, suggestTags, similarStickers) ->
                    AddPartialStateChange.RemoveWaitingListSingleSticker.Success(
                        willSticker = willRemove,
                        getStickersWithTagsState = {
                            if (currentStickerChanged) {
                                GetStickersWithTagsState.fromStickersWithTags(stickersWithTags)
                            } else it.getStickersWithTagsState
                        },
                        suggestTags = if (currentStickerChanged) {
                            suggestTags.orEmpty().toList()
                        } else null,
                        similarStickers = if (currentStickerChanged) {
                            similarStickers.orEmpty()
                        } else null,
                        currentStickerChanged = currentStickerChanged,
                    )
                }.startWith(AddPartialStateChange.LoadingDialog.Show).catchMap {
                    AddPartialStateChange.RemoveWaitingListSingleSticker.Failed(it.message.orEmpty())
                }.endWith(AddPartialStateChange.LoadingDialog.Close)
            },
            filterIsInstance<AddIntent.AddTag>().map { intent ->
                AddPartialStateChange.AddTag(intent.text)
            },
            filterIsInstance<AddIntent.RemoveTag>().map { intent ->
                AddPartialStateChange.RemoveTag(intent.text)
            },
            filterIsInstance<AddIntent.AddToWaitingList>().flatMapConcat { intent ->
                val currentStickerChanged = intent.currentListIsEmpty
                if (currentStickerChanged) {
                    currentStickerChange(intent.stickers[0])
                } else {
                    flowOf(Triple(null, null, null))
                }.map { (stickersWithTags, suggestTags, similarStickers) ->
                    AddPartialStateChange.AddToWaitingList(
                        stickers = intent.stickers,
                        getStickersWithTagsState = {
                            if (currentStickerChanged) {
                                GetStickersWithTagsState.fromStickersWithTags(stickersWithTags)
                            } else it.getStickersWithTagsState
                        },
                        suggestTags = if (currentStickerChanged) {
                            suggestTags.orEmpty().toList()
                        } else null,
                        similarStickers = if (currentStickerChanged) {
                            similarStickers.orEmpty()
                        } else null,
                        currentStickerChanged = currentStickerChanged,
                    )
                }.startWith(AddPartialStateChange.LoadingDialog.Show).catchMap {
                    AddPartialStateChange.AddToWaitingList(stickers = intent.stickers)
                }.endWith(AddPartialStateChange.LoadingDialog.Close)
            },
            filterIsInstance<AddIntent.AddAddToAllTag>().map { intent ->
                AddPartialStateChange.AllToAllTag.Add(intent.text)
            },
            filterIsInstance<AddIntent.RemoveAddToAllTag>().map { intent ->
                AddPartialStateChange.AllToAllTag.Remove(intent.text)
            },
            filterIsInstance<AddIntent.RemoveSuggestTag>().map { intent ->
                AddPartialStateChange.RemoveSuggestTag.Success(intent.text)
            },

            filterIsInstance<AddIntent.AddNewStickerWithTags>().flatMapConcat { intent ->
                addRepository.requestAddStickerWithTags(
                    intent.stickerWithTags,
                    intent.stickerUri
                ).map {
                    Log.w("AddViewModel", "Fuck it: $it")
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
                    .catchMap { AddPartialStateChange.AddStickers.Failed(it.message.orEmpty()) }
            },
        )
    }

    private suspend fun currentStickerChange(newCurrentSticker: UriWithStickerUuidBean?):
            Flow<Triple<StickerWithTags?, Set<String>?, List<StickerWithTags>?>> {
        if (newCurrentSticker == null) return flowOf(Triple(null, null, null))
        var getStickerWithTags: Flow<StickerWithTags?> = flowOf(null)
        if (newCurrentSticker.stickerUuid.isNotBlank()) {
            getStickerWithTags =
                addRepository.requestGetStickerWithTags(newCurrentSticker.stickerUuid)
        }
        return combine(
            getStickerWithTags,
            addRepository.requestSuggestTags(newCurrentSticker.uri!!),
            if (appContext.dataStore.getOrDefault(AddScreenImageSearchPreference)) {
                imageSearchRepository.imageSearch(
                    base = newCurrentSticker.uri,
                    baseUuid = newCurrentSticker.stickerUuid,
                    maxResultCount = appContext.dataStore.getOrDefault(
                        ImageSearchMaxResultCountPreference
                    )
                )
            } else {
                flowOf(emptyList())
            },
        ) { stickerWithTags, suggestTags, similarStickers ->
            Triple(stickerWithTags, suggestTags, similarStickers)
        }
    }
}