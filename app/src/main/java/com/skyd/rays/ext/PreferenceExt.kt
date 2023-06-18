package com.skyd.rays.ext

import androidx.datastore.preferences.core.Preferences
import com.skyd.rays.model.preference.ApiGrantPreference
import com.skyd.rays.model.preference.CurrentStickerUuidPreference
import com.skyd.rays.model.preference.ExportStickerDirPreference
import com.skyd.rays.model.preference.HomeShareButtonAlignmentPreference
import com.skyd.rays.model.preference.IgnoreUpdateVersionPreference
import com.skyd.rays.model.preference.Settings
import com.skyd.rays.model.preference.StickerClassificationModelPreference
import com.skyd.rays.model.preference.StickerScalePreference
import com.skyd.rays.model.preference.WebDavServerPreference
import com.skyd.rays.model.preference.search.IntersectSearchBySpacePreference
import com.skyd.rays.model.preference.search.QueryPreference
import com.skyd.rays.model.preference.search.SearchResultReversePreference
import com.skyd.rays.model.preference.search.SearchResultSortPreference
import com.skyd.rays.model.preference.search.UseRegexSearchPreference
import com.skyd.rays.model.preference.share.StickerExtNamePreference
import com.skyd.rays.model.preference.share.UriStringSharePreference
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
        exportStickerDir = ExportStickerDirPreference.fromPreferences(this),

        // Search
        useRegexSearch = UseRegexSearchPreference.fromPreferences(this),
        intersectSearchBySpace = IntersectSearchBySpacePreference.fromPreferences(this),
        searchResultSort = SearchResultSortPreference.fromPreferences(this),
        searchResultReverse = SearchResultReversePreference.fromPreferences(this),

        // WebDav
        webDavServer = WebDavServerPreference.fromPreferences(this),

        // ML
        stickerClassificationModel = StickerClassificationModelPreference.fromPreferences(this),

        // Style
        stickerScale = StickerScalePreference.fromPreferences(this),
        homeShareButtonAlignment = HomeShareButtonAlignmentPreference.fromPreferences(this),

        // Share
        uriStringShare = UriStringSharePreference.fromPreferences(this),
        stickerExtName = StickerExtNamePreference.fromPreferences(this),

        // Api
        apiGrant = ApiGrantPreference.fromPreferences(this),
    )
}
