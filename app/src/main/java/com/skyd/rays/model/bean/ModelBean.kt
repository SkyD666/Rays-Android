package com.skyd.rays.model.bean

import android.net.Uri

data class ModelBean(
    val uri: Uri,
    val path: String,
    val name: String,
) : BaseBean