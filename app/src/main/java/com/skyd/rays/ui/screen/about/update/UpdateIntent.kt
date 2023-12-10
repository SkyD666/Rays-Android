package com.skyd.rays.ui.screen.about.update

import com.skyd.rays.base.mvi.MviIntent

sealed interface UpdateIntent : MviIntent {
    data object CloseDialog : UpdateIntent
    data class CheckUpdate(val isRetry: Boolean) : UpdateIntent
    data class Update(val url: String?) : UpdateIntent
}