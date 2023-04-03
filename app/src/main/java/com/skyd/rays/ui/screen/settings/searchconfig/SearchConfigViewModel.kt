package com.skyd.rays.ui.screen.settings.searchconfig

import com.skyd.rays.base.BaseViewModel
import com.skyd.rays.base.IUIChange
import com.skyd.rays.base.IUiEvent
import com.skyd.rays.model.respository.SearchConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

@HiltViewModel
class SearchConfigViewModel @Inject constructor(private var searchConfigRepo: SearchConfigRepository) :
    BaseViewModel<SearchConfigState, IUiEvent, SearchConfigIntent>() {
    override fun initUiState(): SearchConfigState {
        return SearchConfigState(
            searchDomainResultUiState = SearchDomainResultUiState.Init
        )
    }

    override fun IUIChange.checkStateOrEvent() = this as? SearchConfigState to this as? IUiEvent

    override fun Flow<SearchConfigIntent>.handleIntent(): Flow<IUIChange> = merge(
        doIsInstance<SearchConfigIntent.GetSearchDomain> {
            searchConfigRepo.requestGetSearchDomain()
                .mapToUIChange { data ->
                    copy(searchDomainResultUiState = SearchDomainResultUiState.Success(data))
                }
                .defaultFinally()
        },

        doIsInstance<SearchConfigIntent.SetSearchDomain> { intent ->
            searchConfigRepo.requestSetSearchDomain(intent.searchDomainBean)
                .mapToUIChange { data ->
                    copy(searchDomainResultUiState = SearchDomainResultUiState.Success(data))
                }
                .defaultFinally()
        },
    )
}