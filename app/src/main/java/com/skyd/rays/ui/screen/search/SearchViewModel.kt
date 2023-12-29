package com.skyd.rays.ui.screen.search

import androidx.lifecycle.viewModelScope
import com.skyd.rays.base.mvi.AbstractMviViewModel
import com.skyd.rays.ext.catchMap
import com.skyd.rays.ext.startWith
import com.skyd.rays.model.respository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepo: SearchRepository,
) : AbstractMviViewModel<SearchIntent, SearchState, SearchEvent>() {

    override val viewState: StateFlow<SearchState>

    init {
        val initialVS = SearchState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<SearchIntent.GetSearchData>().take(1),
            intentSharedFlow.filterNot { it is SearchIntent.GetSearchData }
        )
            .shareWhileSubscribed()
            .toPartialStateChangeFlow()
            .debugLog("SearchPartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    private fun Flow<SearchPartialStateChange>.sendSingleEvent(): Flow<SearchPartialStateChange> {
        return onEach { change ->
            val event = when (change) {
                is SearchPartialStateChange.SearchDataResult.Failed ->
                    SearchEvent.SearchData.Failed(change.msg)

                is SearchPartialStateChange.DeleteStickerWithTags.Success ->
                    SearchEvent.DeleteStickerWithTags.Success(change.stickerUuids)

                is SearchPartialStateChange.ExportStickers.Success ->
                    SearchEvent.ExportStickers.Success(change.successCount)

                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun SharedFlow<SearchIntent>.toPartialStateChangeFlow(): Flow<SearchPartialStateChange> {
        return merge(
            filterIsInstance<SearchIntent.GetSearchData>().flatMapConcat {
                combine(
                    searchRepo.requestStickerWithTagsList(),
                    searchRepo.requestSearchBarPopularTags(count = 20),
                ) { searchResult, popularTags ->
                    if (searchResult.stickerWithTagsList == null) {
                        if (searchResult.isRegexInvalid == null) {
                            SearchPartialStateChange.SearchDataResult.Failed(searchResult.msg.toString())
                        } else SearchPartialStateChange.SearchDataResult.ClearResultData
                    } else {
                        SearchPartialStateChange.SearchDataResult.Success(
                            stickerWithTagsList = searchResult.stickerWithTagsList,
                            popularTags = popularTags,
                        )
                    }
                }
                    .flowOn(Dispatchers.IO)
                    .startWith(SearchPartialStateChange.SearchDataResult.Loading)
                    .catchMap {
                        SearchPartialStateChange.SearchDataResult.Failed(it.message.toString())
                    }
            },

            filterIsInstance<SearchIntent.ExportStickers>().flatMapConcat { intent ->
                searchRepo.requestExportStickers(stickerUuids = intent.stickerUuids)
                    .map { SearchPartialStateChange.ExportStickers.Success(it) }
                    .startWith(SearchPartialStateChange.LoadingDialog)
            },

            filterIsInstance<SearchIntent.DeleteStickerWithTags>().flatMapConcat { intent ->
                searchRepo.requestDeleteStickerWithTagsDetail(stickerUuids = intent.stickerUuids)
                    .map { SearchPartialStateChange.DeleteStickerWithTags.Success(it) }
                    .startWith(SearchPartialStateChange.LoadingDialog)
            },
        )
    }
}