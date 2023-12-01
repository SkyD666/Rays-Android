package com.skyd.rays.ui.screen.detail

import com.skyd.rays.base.mvi.MviSingleEvent

sealed interface DetailEvent : MviSingleEvent {
    sealed interface ExportResult : DetailEvent {
        data object Success : ExportResult
        data object Failed : ExportResult
    }

    sealed interface DeleteResult : DetailEvent {
        data object Success : DeleteResult
        data object Failed : DeleteResult
    }
}
