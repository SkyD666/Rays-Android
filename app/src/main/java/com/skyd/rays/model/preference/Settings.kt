package com.skyd.rays.model.preference

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.platform.LocalContext
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.toSettings
import com.skyd.rays.model.preference.ai.ClassificationThresholdPreference
import com.skyd.rays.model.preference.ai.TextRecognizeThresholdPreference
import com.skyd.rays.model.preference.search.IntersectSearchBySpacePreference
import com.skyd.rays.model.preference.search.QueryPreference
import com.skyd.rays.model.preference.search.SearchResultReversePreference
import com.skyd.rays.model.preference.search.SearchResultSortPreference
import com.skyd.rays.model.preference.search.UseRegexSearchPreference
import com.skyd.rays.model.preference.share.StickerExtNamePreference
import com.skyd.rays.model.preference.share.UriStringSharePreference
import com.skyd.rays.model.preference.theme.CustomPrimaryColorPreference
import com.skyd.rays.model.preference.theme.DarkModePreference
import com.skyd.rays.model.preference.theme.StickerColorThemePreference
import com.skyd.rays.model.preference.theme.ThemeNamePreference
import com.skyd.rays.ui.local.LocalApiGrant
import com.skyd.rays.ui.local.LocalAutoShareIgnoreStrategy
import com.skyd.rays.ui.local.LocalClassificationThreshold
import com.skyd.rays.ui.local.LocalCurrentStickerUuid
import com.skyd.rays.ui.local.LocalCustomPrimaryColor
import com.skyd.rays.ui.local.LocalDarkMode
import com.skyd.rays.ui.local.LocalDisableScreenshot
import com.skyd.rays.ui.local.LocalExportStickerDir
import com.skyd.rays.ui.local.LocalHomeShareButtonAlignment
import com.skyd.rays.ui.local.LocalIgnoreUpdateVersion
import com.skyd.rays.ui.local.LocalIntersectSearchBySpace
import com.skyd.rays.ui.local.LocalQuery
import com.skyd.rays.ui.local.LocalSearchResultReverse
import com.skyd.rays.ui.local.LocalSearchResultSort
import com.skyd.rays.ui.local.LocalShowPopularTags
import com.skyd.rays.ui.local.LocalStickerClassificationModel
import com.skyd.rays.ui.local.LocalStickerColorTheme
import com.skyd.rays.ui.local.LocalStickerExtName
import com.skyd.rays.ui.local.LocalStickerScale
import com.skyd.rays.ui.local.LocalTextRecognizeThreshold
import com.skyd.rays.ui.local.LocalThemeName
import com.skyd.rays.ui.local.LocalUriStringShare
import com.skyd.rays.ui.local.LocalUseRegexSearch
import com.skyd.rays.ui.local.LocalWebDavServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map

data class Settings(
    // Theme
    val themeName: String = ThemeNamePreference.default,
    val customPrimaryColor: String = CustomPrimaryColorPreference.default,
    val darkMode: Int = DarkModePreference.default,
    val stickerColorTheme: Boolean = StickerColorThemePreference.default,
    // Update
    val ignoreUpdateVersion: Long = IgnoreUpdateVersionPreference.default,
    // Sticker
    val currentStickerUuid: String = CurrentStickerUuidPreference.default,
    val query: String = QueryPreference.default,
    val exportStickerDir: String = ExportStickerDirPreference.default,
    // Search
    val useRegexSearch: Boolean = UseRegexSearchPreference.default,
    val intersectSearchBySpace: Boolean = IntersectSearchBySpacePreference.default,
    val searchResultSort: String = SearchResultSortPreference.default,
    val searchResultReverse: Boolean = SearchResultReversePreference.default,
    val showPopularTags: Boolean = ShowPopularTagsPreference.default,
    // WebDav
    val webDavServer: String = WebDavServerPreference.default,
    // ML
    val stickerClassificationModel: String = StickerClassificationModelPreference.default,
    // Style
    val stickerScale: String = StickerScalePreference.default,
    val homeShareButtonAlignment: BiasAlignment = HomeShareButtonAlignmentPreference.default,
    // Share
    val uriStringShare: Boolean = UriStringSharePreference.default,
    val stickerExtName: Boolean = StickerExtNamePreference.default,
    val autoShareIgnoreStrategy: String = AutoShareIgnoreStrategyPreference.default,
    // Api
    val apiGrant: Boolean = ApiGrantPreference.default,
    // Ai
    val classificationThreshold: Float = ClassificationThresholdPreference.default,
    val textRecognizeThreshold: Float = TextRecognizeThresholdPreference.default,
    // Privacy
    val disableScreenshot: Boolean = DisableScreenshotPreference.default,
)

@Composable
fun SettingsProvider(
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val settings by remember { context.dataStore.data.map { it.toSettings() } }
        .collectAsState(initial = Settings(), context = Dispatchers.Default)

    CompositionLocalProvider(
        // Theme
        LocalThemeName provides settings.themeName,
        LocalCustomPrimaryColor provides settings.customPrimaryColor,
        LocalDarkMode provides settings.darkMode,
        LocalStickerColorTheme provides settings.stickerColorTheme,
        // Update
        LocalIgnoreUpdateVersion provides settings.ignoreUpdateVersion,
        // Sticker
        LocalCurrentStickerUuid provides settings.currentStickerUuid,
        LocalQuery provides settings.query,
        LocalExportStickerDir provides settings.exportStickerDir,
        // Search
        LocalUseRegexSearch provides settings.useRegexSearch,
        LocalIntersectSearchBySpace provides settings.intersectSearchBySpace,
        LocalSearchResultSort provides settings.searchResultSort,
        LocalSearchResultReverse provides settings.searchResultReverse,
        LocalShowPopularTags provides settings.showPopularTags,
        // WebDav
        LocalWebDavServer provides settings.webDavServer,
        // ML
        LocalStickerClassificationModel provides settings.stickerClassificationModel,
        // Style
        LocalStickerScale provides settings.stickerScale,
        LocalHomeShareButtonAlignment provides settings.homeShareButtonAlignment,
        // Share
        LocalUriStringShare provides settings.uriStringShare,
        LocalStickerExtName provides settings.stickerExtName,
        LocalAutoShareIgnoreStrategy provides settings.autoShareIgnoreStrategy,
        // Api
        LocalApiGrant provides settings.apiGrant,
        // Ai
        LocalClassificationThreshold provides settings.classificationThreshold,
        LocalTextRecognizeThreshold provides settings.textRecognizeThreshold,
        // Privacy
        LocalDisableScreenshot provides settings.disableScreenshot,
    ) {
        content()
    }
}