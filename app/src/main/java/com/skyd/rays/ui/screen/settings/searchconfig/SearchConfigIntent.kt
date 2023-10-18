package com.skyd.rays.ui.screen.settings.searchconfig

import com.skyd.rays.base.IUiIntent
import com.skyd.rays.model.bean.SearchDomainBean

sealed class SearchConfigIntent : IUiIntent {
    data object GetSearchDomain : SearchConfigIntent()
    data class SetSearchDomain(val searchDomainBean: SearchDomainBean) : SearchConfigIntent()
}