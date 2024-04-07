package com.skyd.rays.ui.theme

import android.content.Context
import android.os.Build
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color

private fun getColorFromTheme(context: Context, @ColorRes id: Int): Color {
    return Color(context.resources.getColor(id, context.theme))
}

@RequiresApi(Build.VERSION_CODES.S)
fun primarySystem(context: Context, tone: Int = 100): Color = when (tone) {
    0 -> getColorFromTheme(context, android.R.color.system_accent1_1000)
    10 -> getColorFromTheme(context, android.R.color.system_accent1_900)
    20 -> getColorFromTheme(context, android.R.color.system_accent1_800)
    30 -> getColorFromTheme(context, android.R.color.system_accent1_700)
    40 -> getColorFromTheme(context, android.R.color.system_accent1_600)
    50 -> getColorFromTheme(context, android.R.color.system_accent1_500)
    60 -> getColorFromTheme(context, android.R.color.system_accent1_400)
    70 -> getColorFromTheme(context, android.R.color.system_accent1_300)
    80 -> getColorFromTheme(context, android.R.color.system_accent1_200)
    90 -> getColorFromTheme(context, android.R.color.system_accent1_100)
    95 -> getColorFromTheme(context, android.R.color.system_accent1_50)
    99 -> getColorFromTheme(context, android.R.color.system_accent1_10)
    100 -> getColorFromTheme(context, android.R.color.system_accent1_0)
    else -> throw IllegalArgumentException("Unknown primary tone: $tone")
}