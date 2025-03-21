package com.skyd.rays.model.preference.privacy

import androidx.datastore.preferences.core.booleanPreferencesKey
import com.skyd.rays.model.preference.BasePreference

object DisableScreenshotPreference : BasePreference<Boolean> {
    private const val DISABLE_SCREENSHOT = "disableScreenshot"

    override val default = false
    override val key = booleanPreferencesKey(DISABLE_SCREENSHOT)
}