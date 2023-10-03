package com.skyd.rays.model.bean

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UriWithStickerUuidBean(
    val uri: Uri? = null,
    val stickerUuid: String = "",
) : BaseBean, Parcelable {
    fun isEmpty(): Boolean = uri == null
}

val EmptyUriWithStickerUuidBean = UriWithStickerUuidBean()