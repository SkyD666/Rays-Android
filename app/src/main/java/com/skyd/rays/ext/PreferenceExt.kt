package com.skyd.rays.ext

import androidx.datastore.preferences.core.Preferences
import com.skyd.rays.model.preference.CurrentStickerUuidPreference
import com.skyd.rays.model.preference.IgnoreUpdateVersionPreference
import com.skyd.rays.model.preference.IntersectSearchBySpacePreference
import com.skyd.rays.model.preference.QueryPreference
import com.skyd.rays.model.preference.Settings
import com.skyd.rays.model.preference.StickerClassificationModelPreference
import com.skyd.rays.model.preference.StickerScalePreference
import com.skyd.rays.model.preference.UseRegexSearchPreference
import com.skyd.rays.model.preference.WebDavServerPreference
import com.skyd.rays.model.preference.theme.CustomPrimaryColorPreference
import com.skyd.rays.model.preference.theme.DarkModePreference
import com.skyd.rays.model.preference.theme.ThemeNamePreference

fun Preferences.toSettings(): Settings {
    return Settings(
        // Theme
        themeName = ThemeNamePreference.fromPreferences(this),
        customPrimaryColor = CustomPrimaryColorPreference.fromPreferences(this),
        darkMode = DarkModePreference.fromPreferences(this),

        // Theme
        ignoreUpdateVersion = IgnoreUpdateVersionPreference.fromPreferences(this),

        // Sticker
        currentStickerUuid = CurrentStickerUuidPreference.fromPreferences(this),
        query = QueryPreference.fromPreferences(this),

        // Search
        useRegexSearch = UseRegexSearchPreference.fromPreferences(this),
        intersectSearchBySpace = IntersectSearchBySpacePreference.fromPreferences(this),

        // WebDav
        webDavServer = WebDavServerPreference.fromPreferences(this),

        // ML
        stickerClassificationModel = StickerClassificationModelPreference.fromPreferences(this),

        // Style
        stickerScale = StickerScalePreference.fromPreferences(this),
    )
}
