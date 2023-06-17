package com.skyd.rays.model.bean

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Keep
sealed interface WebDavInfo : BaseBean

@Serializable
data class WebDavResultInfo(
    var time: Long,
    var count: Int,
) : WebDavInfo

@Parcelize
@Serializable
data class WebDavWaitingInfo(
    var current: Int,
    var total: Int,
    var msg: String,
) : WebDavInfo, Parcelable