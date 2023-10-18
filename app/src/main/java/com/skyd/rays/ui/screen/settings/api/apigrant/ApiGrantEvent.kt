package com.skyd.rays.ui.screen.settings.api.apigrant

import com.skyd.rays.base.IUiEvent

class ApiGrantEvent(
    val addPackageNameUiEvent: AddPackageNameUiEvent? = null,
) : IUiEvent

sealed class AddPackageNameUiEvent {
    data object Success : AddPackageNameUiEvent()
    class Failed(val msg: String) : AddPackageNameUiEvent()
}
