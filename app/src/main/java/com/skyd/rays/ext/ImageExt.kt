package com.skyd.rays.ext

import androidx.compose.ui.layout.ContentScale

fun ContentScale.toDisplayName(): String = when (this) {
    ContentScale.Crop -> "裁剪填充"
    ContentScale.Inside -> "内部"
    ContentScale.FillWidth -> "填充宽度"
    ContentScale.FillHeight -> "填充高度"
    ContentScale.Fit -> "合适"
    ContentScale.FillBounds -> "拉伸填充"
    else -> ""
}