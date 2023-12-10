package com.skyd.rays.model.bean

import android.net.Uri
import com.skyd.rays.base.BaseBean

data class ModelBean(
    val uri: Uri,
    val path: String,
    val name: String,
) : BaseBean