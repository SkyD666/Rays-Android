package com.skyd.rays.base

import androidx.annotation.Keep

@Keep
interface IUIChange

@Keep
interface IUiState : IUIChange

@Keep
interface IUiEvent : IUIChange

@Keep
interface IUiIntent {
    // 是否触发 sendLoadUiIntent(LoadUiIntent.Loading(true))
    val showLoading: Boolean
        get() = true
}

sealed class LoadUiIntent {
    data class Loading(var isShow: Boolean) : LoadUiIntent()
    data class Error(val msg: String) : LoadUiIntent()
}