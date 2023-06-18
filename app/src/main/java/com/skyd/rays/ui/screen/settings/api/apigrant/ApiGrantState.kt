package com.skyd.rays.ui.screen.settings.api.apigrant

import com.skyd.rays.base.IUiState
import com.skyd.rays.model.bean.ApiGrantDataBean

data class ApiGrantState(
    val apiGrantResultUiState: ApiGrantResultUiState,
) : IUiState

sealed class ApiGrantResultUiState {
    object Init : ApiGrantResultUiState()
    data class Success(val data: List<ApiGrantDataBean>) : ApiGrantResultUiState()
}