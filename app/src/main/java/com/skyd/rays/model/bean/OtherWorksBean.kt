package com.skyd.rays.model.bean

import androidx.compose.runtime.Immutable
import com.skyd.rays.base.BaseBean

@Immutable
data class OtherWorksBean(
    val name: String,
    val icon: Int,
    val description: String,
    val url: String,
) : BaseBean
