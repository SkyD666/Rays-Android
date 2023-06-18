package com.skyd.rays.ui.screen.settings.api.apigrant

import com.skyd.rays.base.IUiIntent
import com.skyd.rays.model.bean.ApiGrantPackageBean

sealed class ApiGrantIntent : IUiIntent {
    object GetAllApiGrant : ApiGrantIntent()
    data class UpdateApiGrant(val bean: ApiGrantPackageBean) : ApiGrantIntent()
    data class DeleteApiGrant(val packageName: String) : ApiGrantIntent()
}