package com.skyd.rays.ui.screen.settings.api.apigrant

import com.skyd.rays.base.IUiEvent

data class ApiGrantEvent(
    val addPackageNameUiEvent: AddPackageNameUiEvent? = null,
) : IUiEvent

sealed class AddPackageNameUiEvent {
    object Success : AddPackageNameUiEvent()
    data class Failed(val msg: String) : AddPackageNameUiEvent()
}
