package com.skyd.rays.ui.screen.settings.convfeat.uristringshare

import com.skyd.rays.base.IUiEvent

data class UriStringShareEvent(
    val addPackageNameUiEvent: AddPackageNameUiEvent? = null,
) : IUiEvent

sealed class AddPackageNameUiEvent {
    object Success : AddPackageNameUiEvent()
    data class Failed(val msg: String) : AddPackageNameUiEvent()
}
