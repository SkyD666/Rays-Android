package com.skyd.rays.ui.theme

import android.app.WallpaperManager
import android.os.Build
import android.view.View
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.materialkolor.rememberDynamicColorScheme
import com.skyd.rays.ext.activity
import com.skyd.rays.ext.toColorOrNull
import com.skyd.rays.model.preference.theme.DarkModePreference
import com.skyd.rays.model.preference.theme.ThemeNamePreference
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
    wallpaperColors: Map<String, Color> = extractAllColors(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            setSystemBarsColor(view, darkTheme)
        }
    }

    MaterialTheme(
        colorScheme = rememberDynamicColorScheme(
            seedColor = wallpaperColors.getOrElse(LocalThemeName.current) {
                ThemeNamePreference.values[0].keyColor
            },
            isDark = darkTheme,
        ),
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
fun extractAllColors(): Map<String, Color> {
    return extractColors() + extractColorsFromWallpaper()
}

@Composable
fun extractColors(): Map<String, Color> {
    return ThemeNamePreference.values.associate { it.name to it.keyColor }
        .toMutableMap().also { map ->
            val customPrimaryColor =
                LocalCustomPrimaryColor.current.toColorOrNull() ?: Color.Transparent
            map[ThemeNamePreference.CUSTOM_THEME_NAME] = customPrimaryColor
        }
}

@Composable
fun extractColorsFromWallpaper(): Map<String, Color> {
    val context = LocalContext.current
    val preset = mutableMapOf<String, Color>()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && !LocalView.current.isInEditMode) {
        val colors = WallpaperManager.getInstance(context)
            .getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
        val primary = colors?.primaryColor?.toArgb()
        val secondary = colors?.secondaryColor?.toArgb()
        val tertiary = colors?.tertiaryColor?.toArgb()
        if (primary != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                preset["WallpaperPrimary"] = primarySystem(context = context)
            } else {
                preset["WallpaperPrimary"] = (Color(primary))
            }
        }
        if (secondary != null) preset["WallpaperSecondary"] = Color(secondary)
        if (tertiary != null) preset["WallpaperTertiary"] = Color(tertiary)
    }
    return preset
}