package com.skyd.rays.model.bean

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
sealed interface WebDavInfo : BaseBean

@Serializable
data class WebDavResultInfo(
    var time: Long,
    var count: Int,
) : WebDavInfo

@Serializable
data class WebDavWaitingInfo(
    var current: Int,
    var total: Int,
    var msg: String,
) : WebDavInfo