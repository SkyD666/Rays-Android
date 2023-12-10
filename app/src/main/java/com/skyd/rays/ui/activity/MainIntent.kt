package com.skyd.rays.ui.activity

import com.skyd.rays.base.mvi.MviIntent

sealed interface MainIntent : MviIntent {
    data object Init : MainIntent
    data class UpdateThemeColor(val stickerUuid: String) : MainIntent
}