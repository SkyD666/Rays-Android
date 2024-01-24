package com.skyd.rays.model.bean

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.skyd.rays.base.BaseBean

@Immutable
data class ModelBean(
    val uri: Uri,
    val path: String,
    val name: String,
) : BaseBean