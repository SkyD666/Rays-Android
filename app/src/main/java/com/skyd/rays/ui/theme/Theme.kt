package com.skyd.rays.ui.theme

import android.app.UiModeManager
import android.app.WallpaperManager
import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.materialkolor.Contrast
import com.materialkolor.dynamicColorScheme
import com.materialkolor.rememberDynamicColorScheme
import com.skyd.rays.ext.toColorOrNull
import com.skyd.rays.model.preference.theme.DarkModePreference
import com.skyd.rays.model.preference.theme.ThemeNamePreference
import com.skyd.rays.ui.local.LocalAmoledDarkMode
import com.skyd.rays.ui.local.LocalCustomPrimaryColor
import com.skyd.rays.ui.local.LocalThemeName

@Composable
fun RaysTheme(
    darkTheme: Int,
    content: @Composable () -> Unit
) {
    RaysTheme(
        darkTheme = DarkModePreference.isInDark(darkTheme),
        content = content
    )
}

@Composable
fun RaysTheme(
    darkTheme: Boolean,
    wallpaperColors: Map<String, ColorScheme> = extractAllColors(darkTheme),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themeName = LocalThemeName.current
    val amoledDarkMode = LocalAmoledDarkMode.current

    MaterialTheme(
        colorScheme = remember(themeName, LocalCustomPrimaryColor.current, amoledDarkMode) {
            wallpaperColors.getOrElse(themeName) {
                dynamicColorScheme(
                    seedColor = ThemeNamePreference.values[0].keyColor,
                    isDark = darkTheme,
                    isAmoled = amoledDarkMode,
                    contrastLevel = context.contrastLevel,
                )
            }
        },
        typography = Typography,
        content = content
    )
}

private val Context.contrastLevel: Double
    get() {
        var contrastLevel: Double = Contrast.Default.value
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val uiModeManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
            contrastLevel = uiModeManager.contrast.toDouble()
        }
        return contrastLevel
    }

@Composable
fun extractAllColors(darkTheme: Boolean): Map<String, ColorScheme> {
    return extractColorsFromWallpaper(darkTheme) + extractColors(darkTheme)
}

@Composable
fun extractColors(darkTheme: Boolean): Map<String, ColorScheme> {
    val context = LocalContext.current
    return ThemeNamePreference.values.associate {
        it.name to rememberDynamicColorScheme(
            primary = it.keyColor,
            isDark = darkTheme,
            isAmoled = LocalAmoledDarkMode.current,
            contrastLevel = context.contrastLevel,
        )
    }.toMutableMap().also { map ->
        val customPrimaryColor =
            LocalCustomPrimaryColor.current.toColorOrNull() ?: Color.Transparent
        map[ThemeNamePreference.CUSTOM_THEME_NAME] = rememberDynamicColorScheme(
            primary = customPrimaryColor,
            isDark = darkTheme,
            isAmoled = LocalAmoledDarkMode.current,
            contrastLevel = context.contrastLevel,
        )
    }
}

@Composable
fun extractColorsFromWallpaper(darkTheme: Boolean): Map<String, ColorScheme> {
    val context = LocalContext.current
    val preset = mutableMapOf<String, ColorScheme>()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && !LocalView.current.isInEditMode) {
        val colors = WallpaperManager.getInstance(context)
            .getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
        val primary = colors?.primaryColor?.toArgb()
        val secondary = colors?.secondaryColor?.toArgb()
        val tertiary = colors?.tertiaryColor?.toArgb()
        if (primary != null) {
            preset["WallpaperPrimary"] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (darkTheme) dynamicDarkColorScheme(context)
                else dynamicLightColorScheme(context)
            } else {
                rememberDynamicColorScheme(
                    primary = Color(primary),
                    isDark = darkTheme,
                    isAmoled = LocalAmoledDarkMode.current,
                    contrastLevel = context.contrastLevel,
                )
            }
        }
        if (secondary != null) {
            preset["WallpaperSecondary"] = rememberDynamicColorScheme(
                primary = Color(secondary),
                isDark = darkTheme,
                isAmoled = LocalAmoledDarkMode.current,
                contrastLevel = context.contrastLevel,
            )
        }
        if (tertiary != null) {
            preset["WallpaperTertiary"] = rememberDynamicColorScheme(
                primary = Color(tertiary),
                isDark = darkTheme,
                isAmoled = LocalAmoledDarkMode.current,
                contrastLevel = context.contrastLevel,
            )
        }
    }
    return preset
}