package com.skyd.rays.ui.screen.add

import com.skyd.rays.base.mvi.MviSingleEvent
import com.skyd.rays.model.bean.StickerWithTags

sealed interface AddEvent : MviSingleEvent {
    sealed interface AddStickersResultEvent : AddEvent {
        data class Duplicate(val stickerWithTags: StickerWithTags) : AddStickersResultEvent
        data class Success(val stickerUuid: String) : AddStickersResultEvent
        data class Failed(val msg: String) : AddStickersResultEvent
    }

    data class InitFailed(val msg: String) : AddEvent
}