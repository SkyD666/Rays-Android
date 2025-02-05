package com.skyd.rays.ui.screen.search

import com.skyd.rays.base.mvi.MviSingleEvent

sealed interface SearchEvent : MviSingleEvent {
    sealed interface SearchData : SearchEvent {
        class Failed(val msg: String) : SearchData
    }
}
