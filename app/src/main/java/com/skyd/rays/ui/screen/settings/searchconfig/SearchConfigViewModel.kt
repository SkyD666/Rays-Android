package com.skyd.rays.ui.screen.settings.searchconfig

import androidx.lifecycle.viewModelScope
import com.skyd.rays.base.mvi.AbstractMviViewModel
import com.skyd.rays.ext.catchMap
import com.skyd.rays.ext.startWith
import com.skyd.rays.model.respository.ImageSearchRepository
import com.skyd.rays.model.respository.SearchConfigRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take

class SearchConfigViewModel(
    private var searchConfigRepo: SearchConfigRepository,
    private var imageSearchRepo: ImageSearchRepository,
) : AbstractMviViewModel<SearchConfigIntent, SearchConfigState, SearchConfigEvent>() {

    override val viewState: StateFlow<SearchConfigState>

    init {
        val initialVS = SearchConfigState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<SearchConfigIntent.GetSearchDomain>().take(1),
            intentSharedFlow.filterNot { it is SearchConfigIntent.GetSearchDomain }
        )
            .shareWhileSubscribed()
            .toSearchConfigPartialStateChangeFlow()
            .debugLog("SearchConfigPartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    private fun Flow<SearchConfigPartialStateChange>.sendSingleEvent(): Flow<SearchConfigPartialStateChange> {
        return onEach { change ->
            val event = when (change) {
                is SearchConfigPartialStateChange.EnableAddScreenImageSearchResult.Failed ->
                    SearchConfigEvent.EnableAddScreenImageSearchUiEvent.Failed(change.msg)

                else -> return@onEach
            }
            sendEvent(event)
        }
    }

    private fun SharedFlow<SearchConfigIntent>.toSearchConfigPartialStateChangeFlow(): Flow<SearchConfigPartialStateChange> {
        return merge(
            filterIsInstance<SearchConfigIntent.GetSearchDomain>().flatMapConcat {
                searchConfigRepo.requestGetSearchDomain().map {
                    SearchConfigPartialStateChange.SearchDomainResult.Success(it)
                }.startWith(SearchConfigPartialStateChange.LoadingDialog)
            },
            filterIsInstance<SearchConfigIntent.SetSearchDomain>().flatMapConcat { intent ->
                searchConfigRepo.requestSetSearchDomain(intent.searchDomainBean).map {
                    SearchConfigPartialStateChange.SetSearchDomain.Success(it)
                }.startWith(SearchConfigPartialStateChange.LoadingDialog)
            },
            filterIsInstance<SearchConfigIntent.EnableAddScreenImageSearch>().flatMapConcat { intent ->
                flow {
                    emit(imageSearchRepo.preprocessingEmbedding())
                }.flowOn(Dispatchers.IO).map {
                    SearchConfigPartialStateChange.EnableAddScreenImageSearchResult.Success
                }.startWith(SearchConfigPartialStateChange.LoadingDialog).catchMap {
                    SearchConfigPartialStateChange.EnableAddScreenImageSearchResult.Failed(it.message.orEmpty())
                }
            },
        )
    }
}