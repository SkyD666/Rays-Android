package com.skyd.rays.model.bean

import androidx.compose.ui.graphics.vector.ImageVector

data class MiniToolBean(
    val title: String,
    val icon: ImageVector,
    val experimental: Boolean = false,
    val action: () -> Unit
) : BaseBean

typealias MiniTool1Bean = MiniToolBean