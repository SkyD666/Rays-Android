package com.skyd.rays.ui.screen.mergestickers

import com.skyd.rays.base.mvi.MviSingleEvent

sealed interface MergeStickersEvent : MviSingleEvent {
    sealed interface MergeResult : MergeStickersEvent {
        data object Success : MergeResult
        data class Failed(val msg: String) : MergeResult
    }
}
