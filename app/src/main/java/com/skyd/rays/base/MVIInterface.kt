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

// 部分变化
interface PartialChange<T> {
    // 描述如何从老状态变化为新状态
    fun reduce(oldState: T): T
}