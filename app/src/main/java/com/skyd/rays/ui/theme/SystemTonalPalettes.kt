package com.skyd.rays.ui.theme

import android.content.Context
import android.os.Build
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.kyant.monet.TonalPalettes

private fun getColorFromTheme(context: Context, @ColorRes id: Int): Color {
    return Color(context.resources.getColor(id, context.theme))
}

@RequiresApi(Build.VERSION_CODES.S)
private fun primarySystem(context: Context, tone: Int = 100): Color = when (tone) {
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

@RequiresApi(Build.VERSION_CODES.S)
private fun secondarySystem(context: Context, tone: Int = 100): Color = when (tone) {
    0 -> getColorFromTheme(context, android.R.color.system_accent2_1000)
    10 -> getColorFromTheme(context, android.R.color.system_accent2_900)
    20 -> getColorFromTheme(context, android.R.color.system_accent2_800)
    30 -> getColorFromTheme(context, android.R.color.system_accent2_700)
    40 -> getColorFromTheme(context, android.R.color.system_accent2_600)
    50 -> getColorFromTheme(context, android.R.color.system_accent2_500)
    60 -> getColorFromTheme(context, android.R.color.system_accent2_400)
    70 -> getColorFromTheme(context, android.R.color.system_accent2_300)
    80 -> getColorFromTheme(context, android.R.color.system_accent2_200)
    90 -> getColorFromTheme(context, android.R.color.system_accent2_100)
    95 -> getColorFromTheme(context, android.R.color.system_accent2_50)
    99 -> getColorFromTheme(context, android.R.color.system_accent2_10)
    100 -> getColorFromTheme(context, android.R.color.system_accent2_0)
    else -> throw IllegalArgumentException("Unknown secondary tone: $tone")
}

@RequiresApi(Build.VERSION_CODES.S)
private fun tertiarySystem(context: Context, tone: Int = 100): Color = when (tone) {
    0 -> getColorFromTheme(context, android.R.color.system_accent3_1000)
    10 -> getColorFromTheme(context, android.R.color.system_accent3_900)
    20 -> getColorFromTheme(context, android.R.color.system_accent3_800)
    30 -> getColorFromTheme(context, android.R.color.system_accent3_700)
    40 -> getColorFromTheme(context, android.R.color.system_accent3_600)
    50 -> getColorFromTheme(context, android.R.color.system_accent3_500)
    60 -> getColorFromTheme(context, android.R.color.system_accent3_400)
    70 -> getColorFromTheme(context, android.R.color.system_accent3_300)
    80 -> getColorFromTheme(context, android.R.color.system_accent3_200)
    90 -> getColorFromTheme(context, android.R.color.system_accent3_100)
    95 -> getColorFromTheme(context, android.R.color.system_accent3_50)
    99 -> getColorFromTheme(context, android.R.color.system_accent3_10)
    100 -> getColorFromTheme(context, android.R.color.system_accent3_0)
    else -> throw IllegalArgumentException("Unknown tertiary tone: $tone")
}

@RequiresApi(Build.VERSION_CODES.S)
private fun neutralSystem(context: Context, tone: Int = 100): Color = when (tone) {
    0 -> getColorFromTheme(context, android.R.color.system_neutral1_1000)
    10 -> getColorFromTheme(context, android.R.color.system_neutral1_900)
    20 -> getColorFromTheme(context, android.R.color.system_neutral1_800)
    30 -> getColorFromTheme(context, android.R.color.system_neutral1_700)
    40 -> getColorFromTheme(context, android.R.color.system_neutral1_600)
    50 -> getColorFromTheme(context, android.R.color.system_neutral1_500)
    60 -> getColorFromTheme(context, android.R.color.system_neutral1_400)
    70 -> getColorFromTheme(context, android.R.color.system_neutral1_300)
    80 -> getColorFromTheme(context, android.R.color.system_neutral1_200)
    90 -> getColorFromTheme(context, android.R.color.system_neutral1_100)
    95 -> getColorFromTheme(context, android.R.color.system_neutral1_50)
    99 -> getColorFromTheme(context, android.R.color.system_neutral1_10)
    100 -> getColorFromTheme(context, android.R.color.system_neutral1_0)
    else -> throw IllegalArgumentException("Unknown neutral tone: $tone")
}

@RequiresApi(Build.VERSION_CODES.S)
private fun neutralVariantSystem(context: Context, tone: Int = 100): Color = when (tone) {
    0 -> getColorFromTheme(context, android.R.color.system_neutral2_1000)
    10 -> getColorFromTheme(context, android.R.color.system_neutral2_900)
    20 -> getColorFromTheme(context, android.R.color.system_neutral2_800)
    30 -> getColorFromTheme(context, android.R.color.system_neutral2_700)
    40 -> getColorFromTheme(context, android.R.color.system_neutral2_600)
    50 -> getColorFromTheme(context, android.R.color.system_neutral2_500)
    60 -> getColorFromTheme(context, android.R.color.system_neutral2_400)
    70 -> getColorFromTheme(context, android.R.color.system_neutral2_300)
    80 -> getColorFromTheme(context, android.R.color.system_neutral2_200)
    90 -> getColorFromTheme(context, android.R.color.system_neutral2_100)
    95 -> getColorFromTheme(context, android.R.color.system_neutral2_50)
    99 -> getColorFromTheme(context, android.R.color.system_neutral2_10)
    100 -> getColorFromTheme(context, android.R.color.system_neutral2_0)
    else -> throw IllegalArgumentException("Unknown neutral variant tone: $tone")
}


@RequiresApi(Build.VERSION_CODES.S)
@Composable
@Stable
fun Context.getSystemTonalPalettes(): TonalPalettes {
    val m3TonalValues = remember {
        doubleArrayOf(
            0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 95.0, 99.0, 100.0
        )
    }
    return TonalPalettes(
        keyColor = primarySystem(context = this),
        accent1 = m3TonalValues.associateWith { primarySystem(this, it.toInt()) },
        accent2 = m3TonalValues.associateWith { secondarySystem(this, it.toInt()) },
        accent3 = m3TonalValues.associateWith { tertiarySystem(this, it.toInt()) },
        neutral1 = m3TonalValues.associateWith { neutralSystem(this, it.toInt()) },
        neutral2 = m3TonalValues.associateWith { neutralVariantSystem(this, it.toInt()) },
    )
}