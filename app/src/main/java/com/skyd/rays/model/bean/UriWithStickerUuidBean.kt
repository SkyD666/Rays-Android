package com.skyd.rays.model.bean

import android.net.Uri
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.skyd.rays.base.BaseBean
import com.skyd.rays.model.serializer.UriSerializer
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Immutable
@Parcelize
@Serializable
data class UriWithStickerUuidBean(
    @Serializable(with = UriSerializer::class)
    val uri: Uri? = null,
    val stickerUuid: String = "",
    val id: Long = System.currentTimeMillis() - Random.nextInt(),
) : BaseBean, Parcelable