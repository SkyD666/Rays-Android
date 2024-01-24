package com.skyd.rays.model.bean

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.skyd.rays.base.BaseBean

@Immutable
data class MiniToolBean(
    val title: String,
    val icon: ImageVector,
    val experimental: Boolean = false,
    val action: () -> Unit
) : BaseBean

typealias MiniTool1Bean = MiniToolBean