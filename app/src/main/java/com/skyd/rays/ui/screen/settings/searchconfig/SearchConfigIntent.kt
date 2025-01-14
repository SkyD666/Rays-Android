package com.skyd.rays.ui.screen.settings.searchconfig

import com.skyd.rays.base.mvi.MviIntent
import com.skyd.rays.model.bean.SearchDomainBean

sealed interface SearchConfigIntent : MviIntent {
    data object GetSearchDomain : SearchConfigIntent
    data class SetSearchDomain(val searchDomainBean: SearchDomainBean) : SearchConfigIntent
    data object EnableAddScreenImageSearch : SearchConfigIntent
}