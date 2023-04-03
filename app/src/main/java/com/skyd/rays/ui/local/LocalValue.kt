package com.skyd.rays.ui.local

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController
import com.skyd.rays.model.preference.*
import com.skyd.rays.model.preference.theme.CustomPrimaryColorPreference
import com.skyd.rays.model.preference.theme.DarkModePreference
import com.skyd.rays.model.preference.theme.ThemeNamePreference

val LocalNavController = compositionLocalOf<NavHostController> {
    error("LocalNavController not initialized!")
}

val LocalWindowSizeClass = compositionLocalOf<WindowSizeClass> {
    error("LocalWindowSizeClass not initialized!")
}

// Theme
val LocalDarkMode = compositionLocalOf { DarkModePreference.default }
val LocalThemeName = compositionLocalOf { ThemeNamePreference.default }
val LocalCustomPrimaryColor = compositionLocalOf { CustomPrimaryColorPreference.default }

// Sticker
val LocalCurrentStickerUuid = compositionLocalOf { CurrentStickerUuidPreference.default }
val LocalQuery = compositionLocalOf { QueryPreference.default }

// Search
val LocalUseRegexSearch = compositionLocalOf { UseRegexSearchPreference.default }
val LocalIntersectSearchBySpace = compositionLocalOf { IntersectSearchBySpacePreference.default }

// WebDav
val LocalWebDavServer = compositionLocalOf { WebDavServerPreference.default }
