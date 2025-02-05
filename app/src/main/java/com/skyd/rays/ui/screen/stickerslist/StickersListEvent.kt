package com.skyd.rays.ui.screen.stickerslist

import com.skyd.rays.base.mvi.MviSingleEvent

sealed interface StickersListEvent : MviSingleEvent {
    sealed interface InverseSelectedStickers : StickersListEvent {
        data class Failed(val msg: String) : InverseSelectedStickers
    }
}
