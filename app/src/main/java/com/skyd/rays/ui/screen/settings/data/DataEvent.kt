package com.skyd.rays.ui.screen.settings.data

import com.skyd.rays.base.mvi.MviSingleEvent

sealed interface DataEvent : MviSingleEvent {
    sealed interface DeleteAllResultEvent : DataEvent {
        data class Success(val time: Long) : DeleteAllResultEvent
    }
}
