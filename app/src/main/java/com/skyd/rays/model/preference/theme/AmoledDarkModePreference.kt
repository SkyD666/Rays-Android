package com.skyd.rays.model.preference.theme

import androidx.datastore.preferences.core.booleanPreferencesKey
import com.skyd.rays.model.preference.BasePreference

object AmoledDarkModePreference : BasePreference<Boolean> {
    private const val AMOLED_DARK_MODE = "amoledDarkMode"

    override val default = false
    override val key = booleanPreferencesKey(AMOLED_DARK_MODE)
}