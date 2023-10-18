package com.skyd.rays.ui.screen.settings.searchconfig

import com.skyd.rays.base.IUiState

data class SearchConfigState(
    val searchDomainResultUiState: SearchDomainResultUiState,
) : IUiState

sealed class SearchDomainResultUiState {
    data object Init : SearchDomainResultUiState()
    data class Success(val searchDomainMap: Map<String, Boolean>) : SearchDomainResultUiState()
}