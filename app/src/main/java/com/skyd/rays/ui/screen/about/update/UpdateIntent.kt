package com.skyd.rays.ui.screen.about.update

import com.skyd.rays.base.IUiIntent

sealed class UpdateIntent : IUiIntent {
    data object CloseDialog : UpdateIntent()
    data object CheckUpdate : UpdateIntent()
    data class Update(val url: String?) : UpdateIntent()
}