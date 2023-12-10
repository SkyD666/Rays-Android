package com.skyd.rays.model.bean

import com.skyd.rays.base.BaseBean
import kotlinx.serialization.Serializable

@Serializable
class LicenseBean(
    val name: String,
    val license: String,
    val url: String
) : BaseBean