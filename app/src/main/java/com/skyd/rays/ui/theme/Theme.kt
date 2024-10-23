package com.skyd.rays.ui.theme

import android.app.WallpaperManager
import android.os.Build
import android.view.View
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.materialkolor.dynamicColorScheme
import com.materialkolor.rememberDynamicColorScheme
import com.skyd.rays.ext.activity
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            setSystemBarsColor(view, darkTheme)
        }
    }

    val themeName = LocalThemeName.current
    val amoledDarkMode = LocalAmoledDarkMode.current

    MaterialTheme(
        colorScheme = remember(themeName, LocalCustomPrimaryColor.current, amoledDarkMode) {
            wallpaperColors.getOrElse(themeName) {
                dynamicColorScheme(
                    seedColor = ThemeNamePreference.values[0].keyColor,
                    isDark = darkTheme,
                    isAmoled = amoledDarkMode,
                )
            }
        },
        typography = Typography,
        content = content
    )
}

private fun setSystemBarsColor(view: View, darkMode: Boolean) {
    val window = view.context.activity.window
    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.apply {
        statusBarColor = android.graphics.Color.TRANSPARENT
        navigationBarColor = android.graphics.Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            navigationBarDividerColor = android.graphics.Color.TRANSPARENT
        }
        // 状态栏和导航栏字体颜色
        WindowInsetsControllerCompat(this, view).apply {
            isAppearanceLightStatusBars = !darkMode
            isAppearanceLightNavigationBars = !darkMode
        }
    }
}

@Composable
fun extractAllColors(darkTheme: Boolean): Map<String, ColorScheme> {
    return extractColors(darkTheme) + extractColorsFromWallpaper(darkTheme)
}

@Composable
fun extractColors(darkTheme: Boolean): Map<String, ColorScheme> {
    return ThemeNamePreference.values.associate {
        it.name to rememberDynamicColorScheme(
            primary = it.keyColor,
            isDark = darkTheme,
            isAmoled = LocalAmoledDarkMode.current,
        )
    }.toMutableMap().also { map ->
        val customPrimaryColor =
            LocalCustomPrimaryColor.current.toColorOrNull() ?: Color.Transparent
        map[ThemeNamePreference.CUSTOM_THEME_NAME] = rememberDynamicColorScheme(
            primary = customPrimaryColor,
            isDark = darkTheme,
            isAmoled = LocalAmoledDarkMode.current,
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                preset["WallpaperPrimary"] = rememberSystemDynamicColorScheme(isDark = darkTheme)
            } else {
                preset["WallpaperPrimary"] = rememberDynamicColorScheme(
                    primary = Color(primary),
                    isDark = darkTheme,
                    isAmoled = LocalAmoledDarkMode.current,
                )
            }
        }
        if (secondary != null) {
            preset["WallpaperSecondary"] = rememberDynamicColorScheme(
                primary = Color(secondary),
                isDark = darkTheme,
                isAmoled = LocalAmoledDarkMode.current,
            )
        }
        if (tertiary != null) {
            preset["WallpaperTertiary"] = rememberDynamicColorScheme(
                primary = Color(tertiary),
                isDark = darkTheme,
                isAmoled = LocalAmoledDarkMode.current,
            )
        }
    }
    return preset
}