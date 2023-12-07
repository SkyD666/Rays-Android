package com.skyd.rays.ui.screen.settings.searchconfig

import androidx.lifecycle.viewModelScope
import com.skyd.rays.base.mvi.AbstractMviViewModel
import com.skyd.rays.base.mvi.MviSingleEvent
import com.skyd.rays.ext.startWith
import com.skyd.rays.model.respository.SearchConfigRepository
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
class SearchConfigViewModel @Inject constructor(
    private var searchConfigRepo: SearchConfigRepository
) : AbstractMviViewModel<SearchConfigIntent, SearchConfigState, MviSingleEvent>() {

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
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
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
        )
    }
}