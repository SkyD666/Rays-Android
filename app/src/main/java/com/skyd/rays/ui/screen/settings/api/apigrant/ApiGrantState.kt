package com.skyd.rays.ui.screen.settings.api.apigrant

import com.skyd.rays.base.mvi.MviViewState
import com.skyd.rays.model.bean.ApiGrantDataBean

data class ApiGrantState(
    val apiGrantResultState: ApiGrantResultState,
) : MviViewState {
    companion object {
        fun initial() = ApiGrantState(
            apiGrantResultState = ApiGrantResultState.Init,
        )
    }
}

sealed class ApiGrantResultState {
    // 当前页面的loading
    var loading: Boolean = false
    data object Init : ApiGrantResultState()
    data class Success(val data: List<ApiGrantDataBean>) : ApiGrantResultState()
}