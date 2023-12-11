package com.skyd.rays.model.bean

import android.net.Uri
import android.os.Parcelable
import com.skyd.rays.base.BaseBean
import kotlinx.parcelize.Parcelize

@Parcelize
data class UriWithStickerUuidBean(
    val uri: Uri? = null,
    val stickerUuid: String = "",
) : BaseBean, Parcelable