package com.skyd.rays.ext

import androidx.datastore.preferences.core.Preferences
import com.skyd.rays.model.preference.*
import com.skyd.rays.model.preference.theme.CustomPrimaryColorPreference
import com.skyd.rays.model.preference.theme.DarkModePreference
import com.skyd.rays.model.preference.theme.ThemeNamePreference

fun Preferences.toSettings(): Settings {
    return Settings(
        // Theme
        themeName = ThemeNamePreference.fromPreferences(this),
        customPrimaryColor = CustomPrimaryColorPreference.fromPreferences(this),
        darkMode = DarkModePreference.fromPreferences(this),

        // Sticker
        currentStickerUuid = CurrentStickerUuidPreference.fromPreferences(this),
        query = QueryPreference.fromPreferences(this),

        // Search
        useRegexSearch = UseRegexSearchPreference.fromPreferences(this),
        intersectSearchBySpace = IntersectSearchBySpacePreference.fromPreferences(this),

        // WebDav
        webDavServer = WebDavServerPreference.fromPreferences(this),
    )
}
