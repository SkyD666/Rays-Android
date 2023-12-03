package com.skyd.rays.ui.screen.settings.shareconfig.uristringshare

import com.skyd.rays.base.mvi.MviSingleEvent

sealed interface UriStringShareEvent : MviSingleEvent {

    sealed interface AddPackageNameUiEvent : UriStringShareEvent {
        data object Success : AddPackageNameUiEvent
        class Failed(val msg: String) : AddPackageNameUiEvent
    }
}
