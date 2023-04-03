package com.skyd.rays.model.bean

import kotlinx.serialization.Serializable

@Serializable
class LicenseBean(
    val name: String,
    val license: String,
    val url: String
) : BaseBean