package com.skyd.rays.ui.screen.about.update

import com.skyd.rays.base.mvi.MviIntent

sealed interface UpdateIntent : MviIntent {
    data object CloseDialog : UpdateIntent
    data object CheckUpdate : UpdateIntent
    data class Update(val url: String?) : UpdateIntent
}