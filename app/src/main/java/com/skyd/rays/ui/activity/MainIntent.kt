package com.skyd.rays.ui.activity

import com.skyd.rays.base.IUiIntent

sealed class MainIntent : IUiIntent {
    override val showLoading: Boolean = false

    data class UpdateThemeColor(val stickerUuid: String) : MainIntent()
}