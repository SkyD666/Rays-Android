package com.skyd.rays.ui.screen.settings.api.apigrant

import com.skyd.rays.base.mvi.MviIntent
import com.skyd.rays.model.bean.ApiGrantPackageBean

sealed interface ApiGrantIntent : MviIntent {
    data object GetAllApiGrant : ApiGrantIntent
    data class UpdateApiGrant(val bean: ApiGrantPackageBean) : ApiGrantIntent
    data class DeleteApiGrant(val packageName: String) : ApiGrantIntent
}