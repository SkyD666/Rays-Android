package com.skyd.rays.ui.screen.stickerslist

import com.skyd.rays.base.mvi.MviSingleEvent

sealed interface StickersListEvent : MviSingleEvent {

    sealed interface AddClickCount : StickersListEvent {
        data object Success : AddClickCount
    }
}