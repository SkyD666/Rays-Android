package com.skyd.rays.model.bean

import androidx.compose.runtime.Immutable
import com.skyd.rays.base.BaseBean
import kotlinx.serialization.Serializable

@Serializable
@Immutable
class LicenseBean(
    val name: String,
    val license: String,
    val url: String
) : BaseBean