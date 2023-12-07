package com.skyd.rays.ui.screen.settings.searchconfig

import com.skyd.rays.model.bean.SearchDomainBean

internal sealed interface SearchConfigPartialStateChange {
    fun reduce(oldState: SearchConfigState): SearchConfigState

    data object LoadingDialog : SearchConfigPartialStateChange {
        override fun reduce(oldState: SearchConfigState) = oldState.copy(loadingDialog = true)
    }

    sealed interface SearchDomainResult : SearchConfigPartialStateChange {
        override fun reduce(oldState: SearchConfigState): SearchConfigState {
            return when (this) {
                is Success -> oldState.copy(
                    searchDomainResultState = SearchDomainResultState.Success(searchDomainMap),
                    loadingDialog = false,
                )
            }
        }

        data class Success(val searchDomainMap: Map<String, Boolean>) : SearchDomainResult
    }

    sealed interface SetSearchDomain : SearchConfigPartialStateChange {
        data class Success(val setSearchDomain: SearchDomainBean) : SetSearchDomain {
            override fun reduce(oldState: SearchConfigState): SearchConfigState {
                val searchDomainResultState = oldState.searchDomainResultState
                return if (searchDomainResultState is SearchDomainResultState.Success) {
                    val newMap = searchDomainResultState.searchDomainMap.toMutableMap().also {
                        it["${setSearchDomain.tableName}/${setSearchDomain.columnName}"] =
                            setSearchDomain.search
                    }
                    oldState.copy(
                        searchDomainResultState = searchDomainResultState.copy(
                            searchDomainMap = newMap
                        ),
                        loadingDialog = false,
                    )
                } else oldState
            }
        }
    }
}
