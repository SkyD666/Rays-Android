package com.skyd.rays.ui.screen.settings.searchconfig

import com.skyd.rays.base.mvi.MviViewState

data class SearchConfigState(
    val searchDomainResultState: SearchDomainResultState,
    val loadingDialog: Boolean,
) : MviViewState {
    companion object {
        fun initial() = SearchConfigState(
            searchDomainResultState = SearchDomainResultState.Init,
            loadingDialog = false,
        )
    }
}

sealed class SearchDomainResultState {
    data object Init : SearchDomainResultState()
    data class Success(val searchDomainMap: Map<String, Boolean>) : SearchDomainResultState()
}