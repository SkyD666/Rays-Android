package com.skyd.rays.model.bean

import com.skyd.rays.base.BaseBean

data class OtherWorksBean(
    val name: String,
    val icon: Int,
    val description: String,
    val url: String,
) : BaseBean
