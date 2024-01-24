package com.skyd.rays.model.bean

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import com.skyd.rays.base.BaseBean

@Immutable
data class MoreBean(
    val title: String,
    val icon: ImageVector,
    val iconTint: Color,
    val shape: Shape,
    val shapeColor: Color,
    val action: () -> Unit
) : BaseBean

typealias More1Bean = MoreBean