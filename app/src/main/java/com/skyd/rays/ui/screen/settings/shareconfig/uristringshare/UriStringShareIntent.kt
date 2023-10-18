package com.skyd.rays.ui.screen.settings.shareconfig.uristringshare

import com.skyd.rays.base.IUiIntent
import com.skyd.rays.model.bean.UriStringSharePackageBean

sealed class UriStringShareIntent : IUiIntent {
    data object GetAllUriStringShare : UriStringShareIntent()
    data class UpdateUriStringShare(val bean: UriStringSharePackageBean) : UriStringShareIntent()
    data class DeleteUriStringShare(val packageName: String) : UriStringShareIntent()
}