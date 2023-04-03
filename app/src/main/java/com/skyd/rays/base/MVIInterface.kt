package com.skyd.rays.base

import androidx.annotation.Keep

@Keep
interface IUIChange

@Keep
interface IUiState : IUIChange

@Keep
interface IUiEvent : IUIChange

@Keep
interface IUiIntent

sealed class LoadUiIntent {
    data class Loading(var isShow: Boolean) : LoadUiIntent()
    object ShowMainView : LoadUiIntent()
    data class Error(val msg: String) : LoadUiIntent()
}