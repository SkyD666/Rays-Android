package com.skyd.rays.ui.screen.settings.shareconfig.uristringshare

import com.skyd.rays.base.IUiEvent

class UriStringShareEvent(
    val addPackageNameUiEvent: AddPackageNameUiEvent? = null,
) : IUiEvent

sealed class AddPackageNameUiEvent {
    data object Success : AddPackageNameUiEvent()
    class Failed(val msg: String) : AddPackageNameUiEvent()
}
