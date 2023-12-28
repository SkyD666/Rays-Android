package com.skyd.rays.ui.local

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController
import com.skyd.rays.model.preference.ApiGrantPreference
import com.skyd.rays.model.preference.AutoShareIgnoreStrategyPreference
import com.skyd.rays.model.preference.CurrentStickerUuidPreference
import com.skyd.rays.model.preference.ExportStickerDirPreference
import com.skyd.rays.model.preference.IgnoreUpdateVersionPreference
import com.skyd.rays.model.preference.PickImageMethodPreference
import com.skyd.rays.model.preference.ShowPopularTagsPreference
import com.skyd.rays.model.preference.StickerClassificationModelPreference
import com.skyd.rays.model.preference.StickerScalePreference
import com.skyd.rays.model.preference.WebDavServerPreference
import com.skyd.rays.model.preference.ai.ClassificationThresholdPreference
import com.skyd.rays.model.preference.ai.TextRecognizeThresholdPreference
import com.skyd.rays.model.preference.privacy.BlurStickerPreference
import com.skyd.rays.model.preference.privacy.DisableScreenshotPreference
import com.skyd.rays.model.preference.search.IntersectSearchBySpacePreference
import com.skyd.rays.model.preference.search.QueryPreference
import com.skyd.rays.model.preference.search.SearchResultReversePreference
import com.skyd.rays.model.preference.search.SearchResultSortPreference
import com.skyd.rays.model.preference.search.ShowLastQueryPreference
import com.skyd.rays.model.preference.search.UseRegexSearchPreference
import com.skyd.rays.model.preference.share.CopyStickerToClipboardWhenSharingPreference
import com.skyd.rays.model.preference.share.StickerExtNamePreference
import com.skyd.rays.model.preference.share.UriStringSharePreference
import com.skyd.rays.model.preference.theme.CustomPrimaryColorPreference
import com.skyd.rays.model.preference.theme.DarkModePreference
import com.skyd.rays.model.preference.theme.StickerColorThemePreference
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
val LocalStickerColorTheme = compositionLocalOf { StickerColorThemePreference.default }

// Update
val LocalIgnoreUpdateVersion = compositionLocalOf { IgnoreUpdateVersionPreference.default }

// Sticker
val LocalCurrentStickerUuid = compositionLocalOf { CurrentStickerUuidPreference.default }
val LocalQuery = compositionLocalOf { QueryPreference.default }
val LocalExportStickerDir = compositionLocalOf { ExportStickerDirPreference.default }

// Search
val LocalUseRegexSearch = compositionLocalOf { UseRegexSearchPreference.default }
val LocalIntersectSearchBySpace = compositionLocalOf { IntersectSearchBySpacePreference.default }
val LocalSearchResultSort = compositionLocalOf { SearchResultSortPreference.default }
val LocalSearchResultReverse = compositionLocalOf { SearchResultReversePreference.default }
val LocalShowPopularTags = compositionLocalOf { ShowPopularTagsPreference.default }
val LocalShowLastQuery = compositionLocalOf { ShowLastQueryPreference.default }

// WebDav
val LocalWebDavServer = compositionLocalOf { WebDavServerPreference.default }

// ML
val LocalStickerClassificationModel =
    compositionLocalOf { StickerClassificationModelPreference.default }

// Style
val LocalStickerScale = compositionLocalOf { StickerScalePreference.default }

// Share
val LocalUriStringShare = compositionLocalOf { UriStringSharePreference.default }
val LocalStickerExtName = compositionLocalOf { StickerExtNamePreference.default }
val LocalCopyStickerToClipboardWhenSharing =
    compositionLocalOf { CopyStickerToClipboardWhenSharingPreference.default }
val LocalAutoShareIgnoreStrategy = compositionLocalOf { AutoShareIgnoreStrategyPreference.default }

// Api
val LocalApiGrant = compositionLocalOf { ApiGrantPreference.default }

// Ai
val LocalClassificationThreshold = compositionLocalOf { ClassificationThresholdPreference.default }
val LocalTextRecognizeThreshold = compositionLocalOf { TextRecognizeThresholdPreference.default }

// Privacy
val LocalDisableScreenshot = compositionLocalOf { DisableScreenshotPreference.default }
val LocalBlurSticker = compositionLocalOf { BlurStickerPreference.default }

// Pick image
val LocalPickImageMethod = compositionLocalOf { PickImageMethodPreference.default }