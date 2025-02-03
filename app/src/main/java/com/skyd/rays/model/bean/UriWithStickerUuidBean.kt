package com.skyd.rays.model.bean

import android.net.Uri
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.skyd.rays.base.BaseBean
import kotlinx.parcelize.Parcelize
import kotlin.random.Random

@Immutable
@Parcelize
data class UriWithStickerUuidBean(
    val uri: Uri? = null,
    val stickerUuid: String = "",
    val id: Long = System.currentTimeMillis() - Random.nextInt(),
) : BaseBean, Parcelable