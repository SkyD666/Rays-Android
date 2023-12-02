package com.skyd.rays.ui.screen.settings.api.apigrant

import com.skyd.rays.base.mvi.MviSingleEvent

sealed interface ApiGrantEvent : MviSingleEvent {
    sealed interface AddPackageName : ApiGrantEvent {
        data object Success : AddPackageName
        class Failed(val msg: String) : AddPackageName
    }
}
